package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeNative;
import ru.nest.hiscript.ool.model.nodes.NodeReturn;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HiMethod implements HiNodeIF {
	public int argCount;

	public HiClass clazz;

	public NodeAnnotation[] annotations;

	public Modifiers modifiers;

	public NodeGenerics generics;

	public Type returnType;

	public String name;

	public NodeArgument[] arguments;

	public Type[] throwsTypes;

	public HiClass[] throwsClasses;

	public HiNode body;

	public HiClass[] argClasses;

	public String[] argNames;

	public HiClass returnClass;

	public MethodSignature signature;

	private Token token;

	private String descr;

	public boolean isAnnotationArgument = false;

	/**
	 * HiFieldObject for variables
	 */
	public Object annotationDefaultValue;

	public HiMethod rewrittenMethod;

	public HiMethod(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, NodeGenerics generics, Type returnType, String name, List<NodeArgument> arguments, Type[] throwsTypes, HiNode body) {
		NodeArgument[] _arguments = null;
		if (arguments != null) {
			_arguments = new NodeArgument[arguments.size()];
			arguments.toArray(_arguments);
		}
		set(clazz, annotations, modifiers, generics, returnType, name, _arguments, throwsTypes, body);
	}

	public HiMethod(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, NodeGenerics generics, Type returnType, String name, NodeArgument[] arguments, Type[] throwsTypes, HiNode body) {
		set(clazz, annotations, modifiers, generics, returnType, name, arguments, throwsTypes, body);
	}

	public final static String LAMBDA_METHOD_NAME = "lambda$$";

	/**
	 * functional method
	 */
	public HiMethod(NodeArgument[] arguments, HiNode body) {
		set(null, null, Modifiers.PUBLIC(), null, null, LAMBDA_METHOD_NAME, arguments, null, body);
	}

	private void set(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, NodeGenerics generics, Type returnType, String name, NodeArgument[] arguments, Type[] throwsTypes, HiNode body) {
		this.clazz = clazz;
		this.annotations = annotations;
		this.modifiers = modifiers != null ? modifiers : new Modifiers();
		this.generics = generics;
		this.returnType = returnType;
		this.name = name.intern();
		this.arguments = arguments;
		this.throwsTypes = throwsTypes;
		this.body = body;
		this.argCount = arguments != null ? arguments.length : 0;
	}

	@Override
	public boolean isStatement() {
		return true;
	}

	private static final AtomicInteger lambdasCount = new AtomicInteger();

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(RuntimeContext.METHOD, this);
		boolean valid = HiNode.validateAnnotations(validationInfo, ctx, annotations);

		// generics
		if (generics != null) {
			if (generics.generics.length == 0) {
				validationInfo.error("type parameter expected", generics.getToken());
				valid = false;
			} else {
				valid &= generics.validate(validationInfo, ctx);
			}
			for (int i = 0; i < generics.generics.length; i++) {
				NodeGeneric generic = generics.generics[i];
				if (generic.isWildcard()) {
					validationInfo.error("unexpected wildcard", generic.getToken());
					valid = false;
				} else if (generic.isSuper) {
					validationInfo.error("super is unsupported", generic.getToken());
					valid = false;
				}
			}
		}

		// check arguments
		if (arguments != null) {
			for (NodeArgument argument : arguments) {
				valid &= argument.validate(validationInfo, ctx);
			}
			for (int i = 0; i < arguments.length - 1; i++) {
				NodeArgument argNode1 = arguments[i];
				for (int j = i + 1; j < arguments.length; j++) {
					NodeArgument argNode2 = arguments[j];
					if (argNode1.name.equals(argNode2.name)) {
						validationInfo.error("argument with name '" + argNode2.name + "' already exists", argNode1.getToken());
						valid = false;
					}
				}
			}
		}

		// check modifiers
		boolean isResolved = false;
		if (clazz != null) {
			if (!clazz.isInterface && !clazz.isAbstract() && modifiers.isAbstract()) {
				validationInfo.error("abstract method in non-abstract class", token);
				valid = false;
			}
			if (modifiers.isDefault() && (!clazz.isInterface || modifiers.isStatic() || modifiers.isNative() || modifiers.isAbstract())) {
				validationInfo.error("invalid 'default' modification", token);
				valid = false;
			}
			if (clazz.isInterface && modifiers.isNative()) {
				validationInfo.error("interface methods cannot be native", token);
				valid = false;
			}
			if (modifiers.isAbstract() && modifiers.isStatic()) {
				validationInfo.error("static method cannot be abstract", token);
				valid = false;
			}
			if (clazz.isInterface && !clazz.isAnnotation() && !modifiers.isAbstract() && !modifiers.isDefault() && !modifiers.isStatic()) {
				validationInfo.error("interface abstract methods cannot have body", token);
				valid = false;
			}
			boolean rewriteValid = true;
			if (clazz.superClass != null) {
				resolve(ctx);
				rewrittenMethod = clazz.superClass.getMethod(ctx, signature);
				if (rewrittenMethod != null) {
					if (!returnClass.isInstanceof(rewrittenMethod.returnClass)) {
						validationInfo.error("incompatible return type", getToken());
						rewriteValid = false;
						valid = false;
					}
					if (rewrittenMethod.modifiers.isFinal()) {
						validationInfo.error("cannot rewrite final method", getToken());
						valid = false;
					}
					valid &= modifiers.validateRewriteAccess(rewrittenMethod.modifiers, validationInfo, getToken());
				}
			}
			if (rewriteValid && clazz.interfaces != null) {
				for (HiClass intf : clazz.interfaces) {
					resolve(ctx);
					rewrittenMethod = intf.getMethod(ctx, signature);
					if (rewrittenMethod != null) {
						if (!returnClass.isInstanceof(rewrittenMethod.returnClass)) {
							validationInfo.error("incompatible return type", getToken());
							valid = false;
							break;
						}
						valid &= modifiers.validateRewriteAccess(rewrittenMethod.modifiers, validationInfo, getToken());
					}
				}
			}
			isResolved = true;
		} else {
			// lambda class
			HiClass variableClass = ctx.level.parent.variableClass;
			HiNodeIF variableNode = ctx.level.parent.variableNode;
			if (variableClass != null) {
				if (variableClass.isInterface) {
					int methodsCount = variableClass.getAbstractMethodsCount(ctx);
					if (methodsCount > 1) {
						validationInfo.error("multiple non-overriding abstract methods found in interface " + variableClass.getNameDescr(), variableNode.getToken());
						valid = false;
					} else if (methodsCount == 0) {
						validationInfo.error("no abstract methods found in interface " + variableClass.getNameDescr(), variableNode.getToken());
						valid = false;
					}
				} else {
					validationInfo.error("target type of a lambda conversion must be an interface", variableNode.getToken());
					valid = false;
				}

				resolve(ctx);

				HiMethod implementedMethod = variableClass.searchMethod(ctx, signature);
				if (implementedMethod != null) {
					name += implementedMethod.name;
					argCount = implementedMethod.argCount;
					argClasses = implementedMethod.argClasses;
					arguments = implementedMethod.arguments;
					if (arguments != null) {
						for (NodeArgument argument : arguments) {
							ctx.level.addField(argument);
							ctx.initializedNodes.add(argument);
						}
					}
					returnType = implementedMethod.returnType;
					returnClass = implementedMethod.returnClass;
					signature = new MethodSignature(name, argClasses);
				} else {
					validationInfo.error("incompatible parameters signature in lambda expression", variableNode.getToken());
					valid = false;
				}
				isResolved = true;
			}
			clazz = createLambdaClass(ctx, variableClass);
		}

		if (throwsTypes != null) {
			throwsClasses = new HiClass[throwsTypes.length];
			for (int i = 0; i < throwsTypes.length; i++) {
				throwsClasses[i] = throwsTypes[i].getClass(ctx);
				if (throwsClasses[i] != null && !throwsClasses[i].isInstanceof(HiClass.EXCEPTION_CLASS_NAME)) {
					validationInfo.error("incompatible types: " + throwsClasses[i].getNameDescr() + " cannot be converted to " + HiClass.EXCEPTION_CLASS_NAME, token);
					valid = false;
				}
			}
		}

		if (isResolved) {
			if (body != null) {
				valid &= body.validate(validationInfo, ctx);
			}

			// define returnType
			if (returnType == null) {
				if (body instanceof NodeBlock) {
					NodeBlock block = (NodeBlock) body;
					NodeReturn returnNode = block.getReturnNode();
					if (returnNode != null) {
						returnClass = returnNode.getValueClass(validationInfo, ctx);
						returnType = Type.getType(returnClass);
					} else {
						returnClass = HiClassPrimitive.VOID;
						returnType = Type.voidType;
					}
				} else if (body != null) {
					returnClass = body.getValueClass(validationInfo, ctx);
					returnType = Type.getType(returnClass);
				}
			}

			if (arguments != null) {
				for (NodeArgument argument : arguments) {
					if (argument.getValueClass(validationInfo, ctx).isVar()) {
						validationInfo.error("'var' not allowed here", argument.getToken());
						valid = false;
					}
				}
			}
		}

		ctx.exit();
		return valid;
	}

	public HiClass createLambdaClass(CompileClassContext ctx, HiClass interfaceClass) {
		String lambdaClassName = LAMBDA_METHOD_NAME;
		if (ctx.level.enclosingClass != null) {
			lambdaClassName += ctx.level.enclosingClass.fullName + "/";
		}
		lambdaClassName += lambdasCount.getAndIncrement();

		Type type = Type.getType(interfaceClass);
		Type[] interfaces = interfaceClass != null ? new Type[] {type} : null;
		HiClass clazz = new HiClass(ctx.getClassLoader(), Type.objectType, ctx.level.enclosingClass, ctx.level.enclosingType, interfaces, lambdaClassName, null, HiClass.CLASS_TYPE_ANONYMOUS, ctx);
		HiConstructor defaultConstructor = new HiConstructor(clazz, type, null, Modifiers.PUBLIC(), null, (List<NodeArgument>) null, null, null, null, HiConstructor.BodyConstructorType.NONE);
		clazz.modifiers = Modifiers.PUBLIC();
		clazz.functionalMethod = this;
		clazz.constructors = new HiConstructor[] {defaultConstructor};
		clazz.setToken(token);
		clazz.init(ctx);
		return clazz;
	}

	public boolean isLambda() {
		return name.startsWith(LAMBDA_METHOD_NAME);
	}

	public void applyLambdaImplementedMethod(ClassResolver classResolver, HiClass variableClass, NodeArgument variableNode) {
		if (isLambda() && variableClass.isInterface) {
			int methodsCount = variableClass.getAbstractMethodsCount(classResolver);
			if (methodsCount == 1) {
				resolve(classResolver);
				HiMethod implementedMethod = variableClass.searchMethod(classResolver, signature);
				if (implementedMethod != null) {
					clazz.interfaces = new HiClass[] {variableClass};
					clazz.interfaceTypes = new Type[] {Type.getType(variableClass)};

					name += implementedMethod.name;
					argCount = implementedMethod.argCount;
					argClasses = implementedMethod.argClasses;
					arguments = implementedMethod.arguments;
					returnType = implementedMethod.returnType;
					returnClass = implementedMethod.returnClass;
					signature = new MethodSignature(name, argClasses);

					if (body != null && classResolver instanceof CompileClassContext) {
						CompileClassContext ctx = (CompileClassContext) classResolver;
						ctx.enter(RuntimeContext.METHOD, this);
						if (arguments != null) {
							for (NodeArgument argument : arguments) {
								ctx.level.addField(argument);
								ctx.initializedNodes.add(argument);
							}
						}
						body.validate(ctx.getCompiler().getValidationInfo(), ctx);

						ctx.exit();
					}
				}
			}
		}
	}

	@Override
	public void execute(RuntimeContext ctx) {
		execute(ctx, clazz, null, null);
	}

	public static void execute(RuntimeContext ctx, HiClass clazz, Type type, HiObject object) {
		ctx.addClass(clazz);
		HiObject outboundObject = ctx.getOutboundObject(clazz);
		NodeConstructor.invokeConstructor(ctx, clazz, type, null, object, outboundObject);
	}

	public boolean hasVarargs() {
		return argCount > 0 && arguments[argCount - 1].isVarargs();
	}

	public void resolve(ClassResolver classResolver) {
		if (signature == null) {
			if (arguments != null) {
				int length = arguments.length;
				argClasses = new HiClass[length];
				argNames = new String[length];
				for (int i = 0; i < length; i++) {
					argClasses[i] = arguments[i].getType().getClass(classResolver);
					argNames[i] = arguments[i].name;
				}
			}
			signature = new MethodSignature(name, argClasses);

			if (returnType != null) {
				returnClass = returnType.getClass(classResolver);
			}

			if (modifiers.isNative()) {
				body = new NodeNative(clazz, returnClass, name, argClasses, argNames);
			}
		}
	}

	public void invoke(RuntimeContext ctx, HiClass type, Object object, HiField<?>[] arguments) {
		if (body != null) {
			if (modifiers.isNative()) {
				ctx.value.valueType = Value.VALUE;
				ctx.value.valueClass = type;
				ctx.value.lambdaClass = null;
				if (type.isArray()) {
					ctx.value.array = object;
				} else {
					ctx.value.object = (HiObject) object;
				}
			}
			body.execute(ctx);
		}
	}

	public HiClass getReturnClass(ClassResolver classResolver, HiClass invocationClass, HiClass[] argumentsClasses) {
		return resolveGenericClass(classResolver, returnClass, invocationClass, argumentsClasses);
	}

	// generic
	public HiClass resolveGenericClassByArgument(HiClass clazz, HiClass[] invokeArgumentsClasses) {
		if (clazz != null && clazz.isGeneric()) {
			HiClassGeneric genericClass = (HiClassGeneric) clazz;
			if (argClasses.length > 0) {
				for (int i = 0; i < argClasses.length; i++) {
					HiClass argClass = argClasses[i];
					if (argClass == genericClass) {
						HiClass resolveClass = invokeArgumentsClasses[i];
						if (resolveClass.isPrimitive()) {
							resolveClass = resolveClass.getAutoboxClass();
						}
						return resolveClass;
					}
				}
			}
		}
		return clazz;
	}

	// generic
	public HiClass resolveGenericClass(ClassResolver classResolver, HiClass clazz, HiClass invocationClass, HiClass[] invokeArgumentsClasses) {
		if (clazz != null && clazz.isGeneric()) {
			HiClass resolvedClass = resolveGenericClassByArgument(clazz, invokeArgumentsClasses);
			if (resolvedClass != clazz) {
				return resolvedClass;
			}
			if (invocationClass == null) {
				invocationClass = this.clazz;
			}
			return invocationClass.resolveGenericClass(classResolver, (HiClassGeneric) clazz);
		}
		return clazz;
	}

	@Override
	public String toString() {
		if (descr == null) {
			StringBuilder buf = new StringBuilder();
			buf.append(name);
			buf.append('(');
			for (int i = 0; i < argCount; i++) {
				if (i != 0) {
					buf.append(", ");
				}
				buf.append(arguments[i].getTypeName());
				buf.append(' ');
				buf.append(arguments[i].name);
			}
			buf.append(')');
			descr = buf.toString();
		}
		return descr;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// do not write class as when method will being read the class will not
		// be yet created
		// os.writeClass(clazz);
		os.writeToken(token);
		os.writeShortArray(annotations);
		modifiers.code(os);
		os.writeNullable(generics);
		os.writeType(returnType);
		os.writeUTF(name);
		os.writeByte(argCount);
		os.writeNullable(arguments);
		os.writeByte(throwsTypes != null ? throwsTypes.length : 0);
		os.writeNullable(throwsTypes);
		os.writeNullable(body);
	}

	public static HiMethod decode(DecodeContext os) throws IOException {
		Token token = os.readToken();
		NodeAnnotation[] annotations = os.readShortNodeArray(NodeAnnotation.class);
		Modifiers modifiers = Modifiers.decode(os);
		NodeGenerics generics = os.readNullable(NodeGenerics.class);
		Type returnType = os.readType();
		String name = os.readUTF();
		NodeArgument[] arguments = os.readNullableNodeArray(NodeArgument.class, os.readByte());
		Type[] throwsTypes = os.readNullableArray(Type.class, os.readByte());
		HiNode body = os.readNullable(HiNode.class);

		HiMethod method = new HiMethod(os.getHiClass(), annotations, modifiers, generics, returnType, name, arguments, throwsTypes, body);
		method.token = token;
		return method;
	}

	public boolean isJava() {
		return false;
	}

	@Override
	public Token getToken() {
		return token;
	}

	@Override
	public void setToken(Token token) {
		this.token = token;
	}

	@Override
	public NodeValueType getNodeValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.clazz = getValueClass(validationInfo, ctx);
		if (ctx.nodeValueType.clazz == null || ctx.nodeValueType.clazz == HiClassPrimitive.VOID) {
			ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.noValue;
		} else {
			ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		}
		return ctx.nodeValueType;
	}

	@Override
	public HiClass getValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return clazz;
	}
}
