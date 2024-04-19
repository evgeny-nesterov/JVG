package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.RuntimeContext.StackLevel;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldInt;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.util.List;

public class HiConstructor implements HiNodeIF {
	public final static String METHOD_NAME = "<init>";

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

	public HiConstructor(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, NodeGenerics generics, List<NodeArgument> arguments, Type[] throwsTypes, HiNode body, NodeConstructor bodyConstructor, BodyConstructorType bodyConstructorType) {
		this.clazz = clazz;
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

	public HiConstructor(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, NodeGenerics generics, NodeArgument[] arguments, Type[] throwsTypes, HiNode body, NodeConstructor bodyConstructor, BodyConstructorType bodyConstructorType) {
		this.clazz = clazz;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.generics = generics;
		this.arguments = arguments;
		this.throwsTypes = throwsTypes;
		this.body = body;
		this.bodyConstructor = bodyConstructor;
		this.bodyConstructorType = bodyConstructorType;
	}

	public NodeAnnotation[] annotations;

	public Modifiers modifiers;

	public NodeGenerics generics;

	public HiClass[] argClasses;

	private Token token;

	public void resolve(ClassResolver classResolver) {
		if (argClasses == null) {
			argClasses = new HiClass[arguments != null ? arguments.length : 0];
			for (int i = 0; i < argClasses.length; i++) {
				argClasses[i] = arguments[i].getType().getClass(classResolver);
			}
		}
	}

	public HiClass clazz;

	public NodeArgument[] arguments;

	public NodeConstructor bodyConstructor;

	public BodyConstructorType bodyConstructorType;

	public Type[] throwsTypes;

	public HiClass[] throwsClasses;

	public HiNode body;

	public boolean hasVarargs() {
		return arguments != null && arguments.length > 0 && arguments[arguments.length - 1].isVarargs();
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(RuntimeContext.CONSTRUCTOR, this);
		boolean valid = HiNode.validateAnnotations(validationInfo, ctx, annotations);
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
					validationInfo.error("incompatible types: " + throwsClasses[i].fullName + " cannot be converted to " + HiClass.EXCEPTION_CLASS_NAME, token);
					valid = false;
				}
			}
		}
		switch (bodyConstructorType) {
			case THIS:
				// TODO check
				break;
			case SUPER:
				// TODO check
				break;
		}
		if (bodyConstructor != null) {
			valid &= bodyConstructor.validate(validationInfo, ctx);
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

	public HiObject newInstance(RuntimeContext ctx, HiField<?>[] arguments, HiObject outboundObject) {
		return newInstance(ctx, arguments, null, outboundObject);
	}

	public HiObject newInstance(RuntimeContext ctx, HiField<?>[] arguments, HiObject object, HiObject outboundObject) {
		if (object == null) {
			object = new HiObject(clazz, outboundObject);
		}

		// enter in constructor
		ctx.enterConstructor(this, object, null);
		try {
			// register argument variables in constructor
			ctx.addVariables(arguments);

			// execute constructor this(...)
			if (bodyConstructorType == BodyConstructorType.THIS) {
				NodeConstructor.invokeConstructor(ctx, clazz, bodyConstructor.argValues, object, outboundObject);
				if (ctx.exitFromBlock()) {
					return null;
				}
			}

			// init object for super class
			if (bodyConstructorType != BodyConstructorType.THIS && clazz.superClass != null) {
				HiObject superOutboundObject = ctx.getOutboundObject(clazz.superClass);
				HiObject superObject;
				if (bodyConstructorType == BodyConstructorType.SUPER) {
					NodeConstructor.invokeConstructor(ctx, clazz.superClass, bodyConstructor.argValues, null, superOutboundObject);
					if (ctx.exitFromBlock()) {
						return null;
					}

					superObject = ctx.value.getObject();
				} else {
					// get default constructor from super classes
					if (HiClass.ENUM_CLASS_NAME.equals(clazz.superClass.fullName)) {
						HiConstructor enumDefaultConstructor = clazz.superClass.getConstructor(ctx, HiClass.forName(ctx, HiClass.STRING_CLASS_NAME), HiClassPrimitive.getPrimitiveClass("int"));

						HiFieldObject enumName = HiFieldObject.createStringField(ctx, "name", ctx.initializingEnumValue.getName());
						HiFieldInt enumOrdinal = new HiFieldInt("ordinal", ctx.initializingEnumValue.getOrdinal());
						superObject = enumDefaultConstructor.newInstance(ctx, new HiField<?>[] {enumName, enumOrdinal}, null);
						if (ctx.exitFromBlock()) {
							return null;
						}
					} else {
						HiConstructor superDefaultConstructor;
						if (clazz.superClass.isInterface) {
							superDefaultConstructor = HiClass.OBJECT_CLASS.getConstructor(ctx);
						} else {
							superDefaultConstructor = clazz.superClass.getConstructor(ctx);
							if (superDefaultConstructor == null) {
								ctx.throwRuntimeException("constructor " + getConstructorDescr(clazz.fullName, null) + " not found");
								return null;
							}

							if (superDefaultConstructor == this) {
								ctx.throwRuntimeException("cyclic dependence for constructor " + superDefaultConstructor);
								return null;
							}
						}

						superObject = superDefaultConstructor.newInstance(ctx, null, superOutboundObject);
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
					int count = 0;
					for (int i = 0; i < fieldsCount; i++) {
						if (!clazz.fields[i].isStatic()) {
							count++;
						}
					}

					object.fields = new HiField[count];
					int index = 0;
					for (int i = 0; i < fieldsCount; i++) {
						if (!clazz.fields[i].isStatic()) {
							object.fields[index++] = (HiField<?>) clazz.fields[i].clone();
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

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = clazz;
		ctx.value.lambdaClass = null;
		ctx.value.object = object;
		return object;
	}

	@Override
	public String toString() {
		return getConstructorDescr(clazz.fullName, arguments);
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
			int argCount = vars.length;
			for (int i = 0; i < argCount; i++) {
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
	}

	public static HiConstructor decode(DecodeContext os) throws IOException {
		Token token = os.readToken();
		NodeAnnotation[] annotations = os.readShortNodeArray(NodeAnnotation.class);
		Modifiers modifiers = Modifiers.decode(os);
		NodeGenerics generics = os.readNullable(NodeGenerics.class);
		NodeArgument[] arguments = os.readNullableNodeArray(NodeArgument.class, os.readByte());
		Type[] throwsTypes = os.readNullableArray(Type.class, os.readByte());
		HiNode body = os.readNullable(HiNode.class);
		NodeConstructor bodyConstructor = (NodeConstructor) os.readNullable(HiNode.class);
		BodyConstructorType constructorType = BodyConstructorType.get(os.readByte());

		HiConstructor constructor = new HiConstructor(os.getHiClass(), annotations, modifiers, generics, arguments, throwsTypes, body, bodyConstructor, constructorType);
		constructor.token = token;
		return constructor;
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
