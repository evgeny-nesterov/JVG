package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.classes.HiClassRecord;
import ru.nest.hiscript.ool.model.fields.HiFieldInt;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.HasModifiers;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.RuntimeContext.StackLevel;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.util.List;

public class HiConstructor implements HiNodeIF, HasModifiers {
	public enum BodyConstructorType {
		NONE(0), THIS(1), SUPER(2);

		final static BodyConstructorType[] types = {NONE, THIS, SUPER};

		final int intType;

		BodyConstructorType(int intType) {
			this.intType = intType;
		}

		public int getType() {
			return intType;
		}

		public static BodyConstructorType get(int intType) {
			return types[intType];
		}
	}

	public HiConstructor(HiClass clazz, Type type, NodeAnnotation[] annotations, Modifiers modifiers, NodeGenerics generics, List<NodeArgument> arguments, Type[] throwsTypes, HiNode body, NodeConstructor bodyConstructor, BodyConstructorType bodyConstructorType) {
		if (type == null) {
			type = Type.getType(clazz);
		}
		this.clazz = clazz;
		this.type = type;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.generics = generics;

		if (arguments != null) {
			this.arguments = new NodeArgument[arguments.size()];
			arguments.toArray(this.arguments);
		}

		this.throwsTypes = throwsTypes;
		this.body = body;
		this.bodyConstructor = bodyConstructor;
		this.bodyConstructorType = bodyConstructorType;
	}

	public HiConstructor(HiClass clazz, Type type, NodeAnnotation[] annotations, Modifiers modifiers, NodeGenerics generics, NodeArgument[] arguments, Type[] throwsTypes, HiNode body, NodeConstructor bodyConstructor, BodyConstructorType bodyConstructorType) {
		if (type == null) {
			type = Type.getType(clazz);
		}
		this.clazz = clazz;
		this.type = type;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.generics = generics;
		this.arguments = arguments;
		this.throwsTypes = throwsTypes;
		this.body = body;
		this.bodyConstructor = bodyConstructor;
		this.bodyConstructorType = bodyConstructorType;
	}

	// for anonymous
	public HiConstructor(HiClass clazz, HiConstructor delegateConstructor, ClassResolver classResolver) {
		NodeArgument[] arguments = null;
		if (delegateConstructor.arguments != null) {
			arguments = new NodeArgument[delegateConstructor.arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				NodeArgument arg = delegateConstructor.arguments[i];
				Type argType = arg.getType();

				// @generic
				if (delegateConstructor.clazz.generics != null) {
					HiClassGeneric genericClass = delegateConstructor.clazz.generics.getGenericClass(classResolver, arg.getVariableType());
					HiClass argClass = clazz.resolveGenericClass(classResolver, null, genericClass);
					argType = Type.getType(argClass);
				}

				arguments[i] = new NodeArgument(argType, arg.name, arg.getModifiers(), arg.annotations);
			}
		}

		this.clazz = clazz;
		this.type = Type.getType(clazz);
		this.annotations = null;
		this.modifiers = delegateConstructor.modifiers;
		this.generics = null;
		this.arguments = arguments;
		this.throwsTypes = delegateConstructor.throwsTypes;
		this.body = null;
		this.bodyConstructor = new NodeConstructor(delegateConstructor);
		this.bodyConstructorType = BodyConstructorType.SUPER;
	}

	public static HiConstructor createDefaultConstructor(HiClass clazz, Type type) {
		return new HiConstructor(clazz, type, null, new Modifiers(), null, (List<NodeArgument>) null, null, null, null, HiConstructor.BodyConstructorType.NONE);
	}

	public NodeAnnotation[] annotations;

	private Modifiers modifiers;

	public NodeGenerics generics;

	public HiClass[] argsClasses;

	private Token token;

	public HiClass clazz;

	public Type type;

	public NodeArgument[] arguments;

	public NodeConstructor bodyConstructor;

	public BodyConstructorType bodyConstructorType;

	public Type[] throwsTypes;

	public HiClass[] throwsClasses;

	public HiNode body;

	public ArgumentsSignature signature;

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	public void resolve(ClassResolver classResolver) {
		if (signature == null) {
			argsClasses = new HiClass[arguments != null ? arguments.length : 0];
			for (int i = 0; i < argsClasses.length; i++) {
				NodeArgument arg = arguments[i];
				if (arg.clazz != null) {
					argsClasses[i] = arg.clazz;
				} else {
					argsClasses[i] = arg.getArgClass(classResolver);
				}
			}
			boolean isVarargs = arguments != null && arguments.length > 0 ? arguments[arguments.length - 1].isVarargs() : false;
			signature = new ArgumentsSignature(argsClasses, isVarargs);
		}
	}

	public boolean hasVarargs() {
		return arguments != null && arguments.length > 0 && arguments[arguments.length - 1].isVarargs();
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(RuntimeContext.CONSTRUCTOR, this);
		boolean valid = HiNode.validateAnnotations(validationInfo, ctx, annotations);

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

		if (arguments != null) {
			for (NodeArgument argument : arguments) {
				valid &= argument.validate(validationInfo, ctx);
				ctx.initializedNodes.add(argument);
			}
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

		resolve(ctx);

		switch (bodyConstructorType) {
			case THIS:
				// TODO check
				break;
			case SUPER:
				// TODO check
				break;
		}
		if (bodyConstructor != null) {
			valid &= bodyConstructor.validate(validationInfo, ctx, clazz);
		}

		if (body != null) {
			valid &= body.validate(validationInfo, ctx);
		}
		ctx.exit();
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// not supported
	}

	public HiObject newInstance(RuntimeContext ctx, Type type, HiField<?>[] arguments, HiObject outboundObject) {
		return newInstance(ctx, type, arguments, null, outboundObject);
	}

	public HiObject newInstance(RuntimeContext ctx, Type type, HiField<?>[] arguments, HiObject object, HiObject outboundObject) {
		if (object == null) {
			object = new HiObject(ctx, clazz, type, outboundObject);
		}
		if (type == null) {
			type = this.type;
		}

		// enter in constructor
		ctx.enterConstructor(this, object, null);
		try {
			// register argument variables in constructor
			if (this.arguments != null) {
				for (int i = 0; i < this.arguments.length; i++) {
					TypeArgumentIF typeArgumentIf = this.arguments[i].typeArgument;
					Type typeArgument = typeArgumentIf.getType();
					if (typeArgumentIf.isVarargs()) {
						typeArgument = typeArgument.getCellType();
					}
					HiClass argValueClass = arguments[i].getClass(ctx);
					if (typeArgument.isPrimitive()) {
						if (!argValueClass.isPrimitive() && arguments[i].get() == null) {
							ctx.throwRuntimeException("null pointer");
							return null;
						}
					} else if (!argValueClass.isNull()) {
// TODO check in validation
//						// @generic
//						HiClass argDefinedClass = this.arguments[i].clazz;
//						if (argDefinedClass.isGeneric()) {
//							HiClassGeneric argDefinedGenericClass = (HiClassGeneric) argDefinedClass;
//							if (clazz.typeParameters != null) {
//								argDefinedClass = clazz.typeParameters[argDefinedGenericClass.index];
//							} else {
//								argDefinedClass = argDefinedGenericClass.clazz;
//							}
//						}
//						if (!argValueClass.boxed().isInstanceof(argDefinedClass)) {
//							ctx.throwRuntimeException("inconvertible types; cannot cast " + argValueClass.getNameDescr() + " to " + argDefinedClass.getNameDescr());
//							return null;
//						}
					}
				}
			}
			ctx.addVariables(arguments);

			// execute constructor this(...)
			if (bodyConstructorType == BodyConstructorType.THIS) {
				NodeConstructor.invokeConstructor(ctx, clazz, type, bodyConstructor.argsValues, object, outboundObject);
				if (ctx.exitFromBlock()) {
					return null;
				}
			}

			// init object for super class
			if (bodyConstructorType != BodyConstructorType.THIS && clazz.superClass != null) {
				HiObject superOutboundObject = ctx.getOutboundObject(clazz.superClass);
				HiObject superObject;
				if (bodyConstructorType == BodyConstructorType.SUPER) {
					Type superType = type;

					// @generic
					if (clazz.superClass.generics != null && type.parameters != null) {
						int genericParametersCount = clazz.superClass.generics.generics.length;
						Type[] superTypeParameters = new Type[genericParametersCount];
						for (int i = 0; i < genericParametersCount; i++) {
							// NodeGeneric superGeneric = clazz.superClass.generics.generics[i];
							superTypeParameters[i] = type.parameters[i];
							// TODO check cast superTypeParameters[i]=>superGeneric
						}
						superType = Type.getParameterizedType(superType, superTypeParameters);
					}

					NodeConstructor.invokeConstructor(ctx, clazz.superClass, superType, bodyConstructor.argsValues, null, superOutboundObject);
					if (ctx.exitFromBlock()) {
						return null;
					}

					superObject = (HiObject) ctx.value.getObject();
				} else {
					// get default constructor from super classes
					if (HiClass.ENUM_CLASS_NAME.equals(clazz.superClass.fullName)) {
						HiConstructor enumDefaultConstructor = clazz.superClass.getConstructor(ctx, HiClass.forName(ctx, HiClass.STRING_CLASS_NAME), HiClassPrimitive.getPrimitiveClass("int"));

						HiFieldObject enumName = HiFieldObject.createStringField(ctx, "name", ctx.initializingEnumValue.getName());
						HiFieldInt enumOrdinal = new HiFieldInt("ordinal", ctx.initializingEnumValue.getOrdinal());
						superObject = enumDefaultConstructor.newInstance(ctx, type, new HiField<?>[] {enumName, enumOrdinal}, null);
						if (ctx.exitFromBlock()) {
							return null;
						}
					} else {
						HiConstructor superDefaultConstructor;
						if (clazz.superClass.isInterface) {
							superDefaultConstructor = HiClass.OBJECT_CLASS.getConstructor(ctx);
						} else {
							superDefaultConstructor = clazz.superClass.getConstructor(ctx);
							assert superDefaultConstructor != null; // checked in validation (constructor not found)
							assert superDefaultConstructor != this; // checked in validation (cyclic dependence for constructor)
						}

						superObject = superDefaultConstructor.newInstance(ctx, type, null, superOutboundObject);
						if (ctx.exitFromBlock()) {
							return null;
						}
					}
				}
				object.setSuperObject(superObject);
			}

			// exit from constructor
			StackLevel constructorLevel = ctx.exit(true);

			// enter in object initialization
			ctx.enterInitialization(clazz, object, null);
			try {
				// init object: copy not static fields from class
				if (clazz.fields != null) {
					int fieldsCount = clazz.fields.length;
					int nonStaticCount = 0;
					for (int i = 0; i < fieldsCount; i++) {
						if (!clazz.fields[i].isStatic()) {
							nonStaticCount++;
						}
					}

					object.fields = new HiField[nonStaticCount];
					int index = 0;
					for (int i = 0; i < fieldsCount; i++) {
						if (!clazz.fields[i].isStatic()) {
							object.fields[index++] = (HiField<?>) clazz.fields[i].clone();
						}
					}

					// @generics (after set field to object.fields[])
					index = 0;
					for (int i = 0; i < fieldsCount; i++) {
						if (!clazz.fields[i].isStatic()) {
							HiField<?> field = object.fields[index++];
							field.setGenericClass(ctx, type);
						}
					}

					// add fields
					ctx.addVariables(object.fields);
				}

				if (clazz.initializers != null) {
					// init fields and execute initializers blocks
					int size = clazz.initializers.length;
					for (int i = 0; i < size; i++) {
						NodeInitializer initializer = clazz.initializers[i];
						if (!initializer.isStatic() && initializer instanceof HiField) {
							HiField<?> field = (HiField<?>) initializer;
							field = object.getField(ctx, field.name);
							field.declared = true;
							field.initialized = true;
						}
					}
					for (int i = 0; i < size; i++) {
						NodeInitializer initializer = clazz.initializers[i];
						if (!initializer.isStatic()) {
							if (initializer instanceof HiField<?>) {
								HiField<?> field = (HiField<?>) initializer;
								field = object.getField(ctx, field.name);
								field.initialized = false;
								field.execute(ctx);
								field.initialized = true;
							} else {
								initializer.execute(ctx);
							}
							if (ctx.exitFromBlock()) {
								break;
							}
						}
					}
				}
			} finally {
				// exit from object initialization
				ctx.exit();

				// enter in constructor again
				ctx.enter(constructorLevel);
			}

			// execute constructor body
			if (body != null) {
				body.execute(ctx);
			}

			// at the end of initialization
			if (object.fields != null) {
				for (int i = 0; i < object.fields.length; i++) {
					object.fields[i].initialized = true;
				}
			}
		} finally {
			// exit from constructor
			ctx.exit();
			ctx.isReturn = false;
		}

		ctx.value.setObjectValue(clazz, object);
		return object;
	}

	@Override
	public String toString() {
		return getConstructorDescr(clazz.getNameDescr(), arguments);
	}

	public static String getConstructorDescr(String name, NodeVariable[] arguments) {
		StringBuilder buf = new StringBuilder();
		buf.append(name);
		appendVariables(buf, arguments);
		return buf.toString();
	}

	public static void appendVariables(StringBuilder buf, NodeVariable[] vars) {
		buf.append('(');
		if (vars != null) {
			int argsCount = vars.length;
			for (int i = 0; i < argsCount; i++) {
				if (i != 0) {
					buf.append(", ");
				}
				buf.append(vars[i].getVariableType());
				buf.append(' ');
				buf.append(vars[i].getVariableName());
			}
		}
		buf.append(')');
	}

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeToken(token);
		os.writeType(type);
		os.writeShortArray(annotations);
		modifiers.code(os);
		os.writeNullable(generics);
		os.writeByte(arguments != null ? arguments.length : 0);
		os.writeNullable(arguments);
		os.writeByte(throwsTypes != null ? throwsTypes.length : 0);
		os.writeNullable(throwsTypes);
		os.writeNullable(body);
		os.writeNullable(bodyConstructor);
		os.writeByte(bodyConstructorType.getType());
		os.writeClasses(throwsClasses);
		os.writeClasses(argsClasses);
	}

	public static HiConstructor decode(DecodeContext os) throws IOException {
		Token token = os.readToken();
		Type type = os.readType();
		NodeAnnotation[] annotations = os.readShortNodeArray(NodeAnnotation.class);
		Modifiers modifiers = Modifiers.decode(os);
		NodeGenerics generics = os.readNullable(NodeGenerics.class);
		NodeArgument[] arguments = os.readNullableNodeArray(NodeArgument.class, os.readByte());
		Type[] throwsTypes = os.readNullableArray(Type.class, os.readByte());
		HiNode body = os.readNullable(HiNode.class);
		NodeConstructor bodyConstructor = (NodeConstructor) os.readNullable(HiNode.class);
		BodyConstructorType constructorType = BodyConstructorType.get(os.readByte());

		HiConstructor constructor = new HiConstructor(os.getHiClass(), type, annotations, modifiers, generics, arguments, throwsTypes, body, bodyConstructor, constructorType);
		constructor.throwsClasses = os.readClasses();
		constructor.argsClasses = os.readClasses();
		constructor.token = token;
		return constructor;
	}

	public void codeLink(CodeContext os) throws IOException {
		int index = -1; // not found
		if (clazz.constructors != null) {
			for (int i = 0; i < clazz.constructors.length; i++) {
				if (clazz.constructors[i] == this) {
					index = i;
					break;
				}
			}
		}
		if (index == -1 && clazz.isRecord() && ((HiClassRecord) clazz).defaultConstructor == this) {
			index = -2; // default record constructor
		}
		os.writeShort(index);
		if (index != -1) {
			os.writeClass(clazz);
		}
	}

	public static void decodeLink(DecodeContext os, ClassLoadListener<HiConstructor> callback) throws IOException {
		int index = os.readShort();
		if (index != -1) {
			os.readClass(clazz -> {
				HiConstructor constructor;
				if (index == -2 && clazz.isRecord()) {
					constructor = ((HiClassRecord) clazz).defaultConstructor;
				} else {
					constructor = clazz.constructors[index];
				}
				callback.classLoaded(constructor);
			});
		}
	}

	@Override
	public Token getToken() {
		return token;
	}

	@Override
	public void setToken(Token token) {
		this.token = token;
	}
}
