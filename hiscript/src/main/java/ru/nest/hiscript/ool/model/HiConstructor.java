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
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.util.List;

public class HiConstructor implements Codeable, TokenAccessible {
	public enum BodyConstructorType {
		NONE(0), THIS(1), SUPER(2);

		static BodyConstructorType[] types = {NONE, THIS, SUPER};

		int intType;

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

	public HiConstructor(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, List<NodeArgument> arguments, HiNode body, NodeConstructor bodyConstructor, BodyConstructorType bodyConstructorType) {
		this.clazz = clazz;
		this.annotations = annotations;
		this.modifiers = modifiers;

		if (arguments != null) {
			this.arguments = new NodeArgument[arguments.size()];
			arguments.toArray(this.arguments);
		}

		this.body = body;
		this.bodyConstructor = bodyConstructor;
		this.bodyConstructorType = bodyConstructorType;
	}

	public HiConstructor(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, NodeArgument[] arguments, HiNode body, NodeConstructor bodyConstructor, BodyConstructorType bodyConstructorType) {
		this.clazz = clazz;
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.arguments = arguments;
		this.body = body;
		this.bodyConstructor = bodyConstructor;
		this.bodyConstructorType = bodyConstructorType;
	}

	public NodeAnnotation[] annotations;

	public Modifiers modifiers;

	public HiClass[] argClasses;

	public Token token;

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

	public HiNode body;

	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(RuntimeContext.CONSTRUCTOR, this);
		boolean valid = true;
		if (arguments != null) {
			for (NodeArgument argument : arguments) {
				valid &= argument.validate(validationInfo, ctx);
				ctx.initializedNodes.add(argument);
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

	public HiObject newInstance(RuntimeContext ctx, HiField<?>[] arguments, HiObject outboundObject) {
		return newInstance(ctx, arguments, null, outboundObject);
	}

	public HiObject newInstance(RuntimeContext ctx, HiField<?>[] arguments, HiObject object, HiObject outboundObject) {
		if (object == null) {
			object = new HiObject(clazz, outboundObject);
		}

		// Check on valid outboundObject
		// TODO: use only for DEBUGGING
		if (clazz.hasOutboundObject()) {
			if (outboundObject == null) {
				throw new IllegalStateException("outboundObject must be not null: " + this);
			}
			if (outboundObject.clazz != clazz.enclosingClass) {
				throw new IllegalStateException("outboundObject must be of type " + clazz.enclosingClass + ": " + this);
			}
		} else {
			if (outboundObject != null) {
				throw new IllegalStateException("outboundObject must be null: " + this);
			}
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
					if (Type.enumType.name.equals(clazz.superClass.fullName)) {
						HiConstructor enumDefaultConstructor = clazz.superClass.getConstructor(ctx, HiClass.forName(ctx, HiClass.STRING_CLASS_NAME), HiClassPrimitive.getPrimitiveClass("int"));

						HiFieldObject enumName = HiFieldObject.createStringField(ctx, "name", ctx.initializingEnumValue.getName());
						HiFieldInt enumOrdinal = new HiFieldInt("ordinal", ctx.initializingEnumValue.getOrdinal());
						superObject = enumDefaultConstructor.newInstance(ctx, new HiField<?>[] {enumName, enumOrdinal}, null);
						if (ctx.exitFromBlock()) {
							return null;
						}
					} else {
						HiConstructor superDefaultConstructor = clazz.superClass.getConstructor(ctx);
						if (superDefaultConstructor == null) {
							ctx.throwRuntimeException("Constructor " + getConstructorDescr(clazz.fullName, null) + " not found");
							return null;
						}

						if (superDefaultConstructor == this) {
							ctx.throwRuntimeException("Cyclic dependence for constructor " + superDefaultConstructor);
							return null;
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
					int fields_count = clazz.fields.length;
					int count = 0;
					for (int i = 0; i < fields_count; i++) {
						if (!clazz.fields[i].isStatic()) {
							count++;
						}
					}

					object.fields = new HiField[count];
					int index = 0;
					for (int i = 0; i < fields_count; i++) {
						if (!clazz.fields[i].isStatic()) {
							object.fields[index++] = (HiField<?>) clazz.fields[i].clone();
						}
					}

					if (clazz.initializers != null) {
						// add fields
						ctx.addVariables(object.fields);

						// init fields and execute initializers blocks
						int size = clazz.initializers.length;
						for (int i = 0; i < size; i++) {
							NodeInitializer initializer = clazz.initializers[i];
							if (!initializer.isStatic()) {
								if (initializer instanceof HiField<?>) {
									HiField<?> field = (HiField<?>) initializer;
									initializer = object.getField(ctx, field.name);
								}

								initializer.execute(ctx);
								if (ctx.exitFromBlock()) {
									break;
								}
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
		os.writeShortArray(annotations);
		os.writeToken(token);
		modifiers.code(os);

		int count = arguments != null ? arguments.length : 0;
		os.writeByte(count);
		for (int i = 0; i < count; i++) {
			arguments[i].code(os);
		}

		os.writeNullable(body);
		os.writeNullable(bodyConstructor);
		os.writeByte(bodyConstructorType.getType());
	}

	public static HiConstructor decode(DecodeContext os) throws IOException {
		NodeAnnotation[] annotations = os.readShortNodeArray(NodeAnnotation.class);
		Token token = os.readToken();
		Modifiers modifiers = Modifiers.decode(os);

		int count = os.readByte();
		NodeArgument[] arguments = count > 0 ? new NodeArgument[count] : null;
		for (int i = 0; i < count; i++) {
			arguments[i] = (NodeArgument) HiNode.decode(os);
		}
		HiConstructor constructor = new HiConstructor(os.getHiClass(), annotations, modifiers, arguments, os.readNullable(HiNode.class), (NodeConstructor) os.readNullable(HiNode.class), BodyConstructorType.get(os.readByte()));
		constructor.token = token;
		return constructor;
	}

	@Override
	public Token getToken() {
		return token;
	}

	public static void main(String[] a) {
		class S {
			{
				System.out.println("S<init>");
			}

			S(String s) {
				System.out.println("S(String s): " + s);
			}
		}
		class A extends S {
			{
				System.out.println("A<init>");
			}

			A(int x) {
				this("[" + (x + 1) + "]", x);
				System.out.println("A(int x): " + x);
			}

			A(String v, int x) {
				super(v);
				System.out.println("A(String v, int x): " + v + ", " + x);
			}
		}
		new A(1);
	}
}
