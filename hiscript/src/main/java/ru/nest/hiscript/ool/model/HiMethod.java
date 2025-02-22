package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.HasModifiers;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeNative;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.util.List;

import static ru.nest.hiscript.ool.model.nodes.NodeVariable.*;

public class HiMethod implements HiNodeIF, HasModifiers {
	public final static String LAMBDA_METHOD_NAME = "lambda$$";

	public int argsCount;

	public HiClass clazz;

	public NodeAnnotation[] annotations;

	private Modifiers modifiers;

	public NodeGenerics generics;

	public Type returnType;

	public String name;

	public NodeArgument[] arguments;

	public Type[] throwsTypes;

	public HiClass[] throwsClasses;

	public HiNode body;

	public HiClass[] argsClasses;

	public String[] argsNames;

	public HiClass returnClass;

	public MethodSignature signature;

	private Token token;

	private String descr;

	public boolean isAnnotationArgument = false;

	// TODO unused?
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

	/**
	 * functional method
	 */
	public HiMethod(NodeArgument[] arguments, HiNode body) {
		set(null, null, Modifiers.PUBLIC, null, null, LAMBDA_METHOD_NAME, arguments, null, body);
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
		this.argsCount = arguments != null ? arguments.length : 0;
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	@Override
	public boolean isStatement() {
		return true;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(RuntimeContext.METHOD, this);
		boolean valid = HiNode.validateAnnotations(validationInfo, ctx, annotations);

		// @unnamed
		if (UNNAMED.equals(name)) {
			validationInfo.error("keyword '_' cannot be used as an identifier", token);
			valid = false;
		}

		// @generics
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

				// @unnamed
				if (UNNAMED.equals(argNode1.name)) {
					continue;
				}

				for (int j = i + 1; j < arguments.length; j++) {
					NodeArgument argNode2 = arguments[j];
					if (argNode1.name.equals(argNode2.name)) {
						validationInfo.error("argument with name '" + argNode2.name + "' already exists", argNode1.getToken());
						valid = false;
					}
				}
			}
		}

		resolve(ctx);

		// check modifiers
		boolean isResolved = false;
		if (clazz != null) {
			if (returnClass == null && returnType != null) {
				returnClass = returnType.getClass(ctx);
			}
			if (!clazz.isInterface && !clazz.isAbstract() && modifiers.isAbstract()) {
				validationInfo.error("abstract method in non-abstract class", token);
				valid = false;
			}
			if (clazz.isInterface && modifiers.isProtected()) {
				validationInfo.error("modifier 'protected' not allowed here", token);
				valid = false;
			}
			if (modifiers.isDefault() && (!clazz.isInterface || modifiers.isStatic() || modifiers.isNative() || modifiers.isAbstract())) {
				validationInfo.error("invalid 'default' modification", token);
				valid = false;
			}
			if (clazz.isInterface) {
				if (modifiers.isNative()) {
					validationInfo.error("interface methods cannot be native", token);
					valid = false;
				}
				if (!modifiers.isStatic() && !modifiers.isDefault()) {
					if (modifiers.isAbstract()) {
						if (modifiers.isPrivate()) {
							validationInfo.error("modifier 'private' not allowed here", token);
							valid = false;
						} else if (modifiers.isProtected()) {
							validationInfo.error("modifier 'protected' not allowed here", token);
							valid = false;
						}
					} else if (!modifiers.isPrivate() && !clazz.isAnnotation()) {
						validationInfo.error("modifier 'private' is expected", token);
						valid = false;
					}
				}
				if (!clazz.isAnnotation() && !modifiers.isAbstract() && !modifiers.isDefault() && !modifiers.isStatic() && !modifiers.isPrivate()) {
					validationInfo.error("interface abstract methods cannot have body", token);
					valid = false;
				}
			}
			if (modifiers.isAbstract() && modifiers.isStatic()) {
				validationInfo.error("static method cannot be abstract", token);
				valid = false;
			}
			if (modifiers.isAbstract() && modifiers.isFinal()) {
				validationInfo.error("illegal combination of modifiers: 'abstract' and 'final'", token);
				valid = false;
			}
			boolean rewriteValid = true;
			if (clazz.superClass != null) {
				resolve(ctx);
				rewrittenMethod = clazz.superClass.searchMethod(ctx, signature);
				if (rewrittenMethod != null) {
					if (rewrittenMethod.returnClass.isGeneric() ? !returnClass.isInstanceof(((HiClassGeneric) rewrittenMethod.returnClass).clazz) : !returnClass.isInstanceof(rewrittenMethod.returnClass)) {
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
					rewrittenMethod = intf.searchMethod(ctx, signature);
					if (rewrittenMethod != null) {
						boolean match;
						if (returnClass.isGeneric() && rewrittenMethod.returnClass.isGeneric()) {
							// @generics
							match = ((HiClassGeneric) returnClass).clazz.isInstanceof(((HiClassGeneric) rewrittenMethod.returnClass).clazz);
						} else {
							match = returnClass.isInstanceof(rewrittenMethod.returnClass);
						}
						if (!match) {
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
					argsCount = implementedMethod.argsCount;
					boolean isVarargs = false;
					HiClass[] newArgsClasses = new HiClass[argsCount];
					if (arguments != null && argsCount > 0) {
						for (int i = 0; i < argsCount; i++) {
							NodeArgument receivedArgument = arguments[i];
							HiClass receivedClass = argsClasses[i];
							NodeArgument methodArgument = implementedMethod.arguments[i];
							HiClass requiredArgumentClass = methodArgument.clazz;

							// @generic
							if (requiredArgumentClass.isGeneric()) {
								requiredArgumentClass = ctx.getDeclaredGenericClass((HiClassGeneric) requiredArgumentClass);
							}

							if (!receivedClass.isVar()) {
								HiClass checkClass = requiredArgumentClass;

								// @generic
								if (checkClass.isGeneric()) {
									checkClass = ((HiClassGeneric) checkClass).clazz;
								}

								if (receivedClass != checkClass) {
									validationInfo.error("incompatible parameter types in lambda expression: expected " + checkClass.getNameDescr() + " but found " + receivedClass.getNameDescr(), receivedArgument.getToken());
								}
							}
							receivedArgument.clazz = requiredArgumentClass;
							receivedArgument.typeArgument = methodArgument.typeArgument;
							ctx.level.addField(receivedArgument);
							ctx.initializedNodes.add(receivedArgument);
							newArgsClasses[i] = requiredArgumentClass;
						}
						isVarargs = implementedMethod.arguments[argsCount - 1].isVarargs();
					}
					argsClasses = newArgsClasses;
					returnType = implementedMethod.returnType;
					returnClass = implementedMethod.returnClass;

					// @generic
					if (returnClass.isGeneric()) {
						returnClass = ctx.getDeclaredGenericClass((HiClassGeneric) returnClass);
						returnType = Type.getType(returnClass);
					}

					signature = new MethodSignature(name, argsClasses, isVarargs);
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
				returnClass = body.getValueClass(validationInfo, ctx);
				returnType = Type.getType(returnClass);
			}

			if (returnType != null && returnType.isVar()) {
				validationInfo.error("'var' not allowed here", getToken());
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
			lambdaClassName += ctx.level.enclosingClass.getNameDescr() + "/";
		}
		lambdaClassName += ctx.lambdasCount.getAndIncrement();

		Type type = Type.getType(interfaceClass);
		Type[] interfaces = interfaceClass != null ? new Type[] {type} : null;
		HiClass clazz = new HiClass(ctx.getClassLoader(), Type.objectType, ctx.level.enclosingClass, interfaces, lambdaClassName, null, HiClass.CLASS_TYPE_ANONYMOUS, ctx);
		clazz.modifiers = Modifiers.PUBLIC;
		clazz.functionalMethod = this;
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
					argsCount = implementedMethod.argsCount;
					argsClasses = implementedMethod.argsClasses;
					arguments = implementedMethod.arguments;
					returnType = implementedMethod.returnType;
					returnClass = implementedMethod.returnClass;
					boolean isVarargs = arguments != null && arguments.length > 0 ? arguments[arguments.length - 1].isVarargs() : false;
					signature = new MethodSignature(name, argsClasses, isVarargs);

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
		return argsCount > 0 && arguments[argsCount - 1].isVarargs();
	}

	public void resolve(ClassResolver classResolver) {
		if (signature == null) {
			if (arguments != null) {
				int length = arguments.length;
				if (argsClasses == null) {
					argsClasses = new HiClass[length];
					for (int i = 0; i < length; i++) {
						argsClasses[i] = arguments[i].getType().getClass(classResolver);
					}
				}
				argsNames = new String[length];
				for (int i = 0; i < length; i++) {
					argsNames[i] = arguments[i].name;
				}
			}
			boolean isVarargs = arguments != null && arguments.length > 0 ? arguments[arguments.length - 1].isVarargs() : false;
			signature = new MethodSignature(name, argsClasses, isVarargs);

			if (returnType != null && returnClass == null) {
				returnClass = returnType.getClass(classResolver);
			}

			if (modifiers.isNative() && body == null) {
				body = new NodeNative(clazz, returnClass, name, argsClasses, argsNames);
			}
		}
	}

	/**
	 * Arguments has to be added to ctx: ctx.addVariables(argsFields)
	 * arguments is used only in HiClassJava
	 */
	public void invoke(RuntimeContext ctx, HiClass clazz, Object object, HiField<?>[] arguments) {
		if (body != null) {
			if (modifiers.isNative()) {
				ctx.value.setObjectOrArrayValue(clazz, clazz, object);
			}
			if (modifiers.isSynchronized()) {
				synchronized (object) {
					body.execute(ctx);
				}
			} else {
				body.execute(ctx);
			}

			if (ctx.isExit || ctx.exception != null) {
				return;
			}
			ctx.isReturn = false;

			// @autoboxing
			if (returnClass != null && returnClass.isObject() && ctx.value.valueClass.isPrimitive()) {
				HiObject boxedObject = ((HiClassPrimitive) ctx.value.valueClass).box(ctx, ctx.value);
				ctx.value.object = boxedObject;
				ctx.value.valueClass = returnClass;
			}
		}
	}

	public HiClass getReturnClass(ClassResolver classResolver, HiClass invocationClass, Type invocationType, HiClass[] argumentsClasses) {
		return resolveGenericClass(classResolver, returnClass, invocationClass, invocationType, argumentsClasses);
	}

	// @generics
	public HiClass resolveGenericClassByArgument(HiClass clazz, HiClass[] invokeArgumentsClasses) {
		if (clazz != null && clazz.isGeneric()) {
			HiClassGeneric genericClass = (HiClassGeneric) clazz;
			if (argsClasses != null) {
				for (int i = 0; i < argsClasses.length; i++) {
					HiClass argClass = argsClasses[i];
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

	// @generics
	public HiClass resolveGenericClass(ClassResolver classResolver, HiClass clazz, HiClass invocationClass, Type invocationType, HiClass[] invokeArgumentsClasses) {
		if (clazz != null && clazz.isGeneric()) {
			HiClass resolvedClass = resolveGenericClassByArgument(clazz, invokeArgumentsClasses);
			if (resolvedClass != clazz) {
				return resolvedClass;
			}
			if (resolvedClass.isGeneric() && invocationType != null && invocationType.parameters != null) {
				Type resolvedType = invocationType.getParameterType((HiClassGeneric) resolvedClass);
				return resolvedType.getClass(classResolver);
			}
			assert invocationClass != null;
			return invocationClass.resolveGenericClass(classResolver, invocationType, (HiClassGeneric) clazz);
		}
		return clazz != null ? clazz : HiClass.OBJECT_CLASS;
	}

	public String getSignatureText(Type invocationType) {
		StringBuilder buf = new StringBuilder();
		buf.append(name);
		buf.append('(');
		for (int i = 0; i < argsCount; i++) {
			if (i != 0) {
				buf.append(", ");
			}
			HiClass methodArgumentClass = argsClasses[i];
			if (methodArgumentClass.isGeneric()) { // not primitive
				Type argumentType = invocationType.getParameterType((HiClassGeneric) methodArgumentClass);
				buf.append(argumentType.fullName);
			} else {
				buf.append(arguments[i].getTypeName());
			}
			buf.append(' ');
			buf.append(arguments[i].name);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String toString() {
		if (descr == null) {
			StringBuilder buf = new StringBuilder();
			buf.append(name);
			buf.append('(');
			for (int i = 0; i < argsCount; i++) {
				if (i != 0) {
					buf.append(", ");
				}
				buf.append(arguments[i].clazz.getNameDescr());
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
		// ignore argsNames, signature, descr, annotationDefaultValue
		os.writeByte(HiNode.TYPE_METHOD);
		os.writeToken(token);

		os.writeShortArray(annotations);
		modifiers.code(os);
		os.writeNullable(generics);
		os.writeType(returnType);
		os.writeUTF(name);
		os.writeByte(argsCount);
		os.writeNullable(arguments);
		os.writeByte(throwsTypes != null ? throwsTypes.length : 0);
		os.writeNullable(throwsTypes);
		os.writeNullable(body);
		os.writeBoolean(isAnnotationArgument);
		os.writeClass(returnClass); // null for void
		os.writeClasses(throwsClasses);
		os.writeClasses(argsClasses);
		os.writeClass(clazz);
		// TODO rewrittenMethod
	}

	public static HiMethod decode(DecodeContext os) throws IOException {
		NodeAnnotation[] annotations = os.readShortNodeArray(NodeAnnotation.class);
		Modifiers modifiers = Modifiers.decode(os);
		NodeGenerics generics = os.readNullable(NodeGenerics.class);
		Type returnType = os.readType();
		String name = os.readUTF();
		NodeArgument[] arguments = os.readNullableNodeArray(NodeArgument.class, os.readByte());
		Type[] throwsTypes = os.readNullableArray(Type.class, os.readByte());
		HiNode body = os.readNullable(HiNode.class);

		HiMethod method = new HiMethod(os.getHiClass(), annotations, modifiers, generics, returnType, name, arguments, throwsTypes, body);
		method.isAnnotationArgument = os.readBoolean();
		os.readClass(clazz -> method.returnClass = clazz);
		method.throwsClasses = os.readClasses();
		method.argsClasses = os.readClasses();
		os.readClass(clazz -> method.clazz = clazz);
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

	public boolean hasArguments(int count) {
		return count == (arguments == null ? 0 : arguments.length);
	}
}
