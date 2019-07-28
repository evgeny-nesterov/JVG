package script.ool.model;

import java.io.IOException;
import java.util.List;

import script.ool.model.RuntimeContext.StackLevel;
import script.ool.model.nodes.CodeContext;
import script.ool.model.nodes.DecodeContext;
import script.ool.model.nodes.NodeArgument;
import script.ool.model.nodes.NodeConstructor;
import script.ool.model.nodes.NodeVariable;

public class Constructor implements Codable {
	public static enum BodyConstructorType {
		NONE(0), THIS(1), SUPER(2);
		static BodyConstructorType[] types = { NONE, THIS, SUPER };

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

	public Constructor(Clazz clazz, Modifiers modifiers, List<NodeArgument> arguments, Node body, NodeConstructor bodyConstructor, BodyConstructorType bodyConstructorType) {
		this.clazz = clazz;
		this.modifiers = modifiers;

		this.arguments = new NodeArgument[arguments != null ? arguments.size() : 0];
		if (arguments != null) {
			arguments.toArray(this.arguments);
		}

		this.body = body;
		this.bodyConstructor = bodyConstructor;
		this.bodyConstructorType = bodyConstructorType;
	}

	public Constructor(Clazz clazz, Modifiers modifiers, NodeArgument[] arguments, Node body, NodeConstructor bodyConstructor, BodyConstructorType bodyConstructorType) {
		this.clazz = clazz;
		this.modifiers = modifiers;
		this.arguments = arguments;
		this.body = body;
		this.bodyConstructor = bodyConstructor;
		this.bodyConstructorType = bodyConstructorType;
	}

	public Modifiers modifiers;

	public Clazz[] argClasses;

	public void resolve(RuntimeContext ctx) {
		if (argClasses == null) {
			argClasses = new Clazz[arguments != null ? arguments.length : 0];
			for (int i = 0; i < argClasses.length; i++) {
				argClasses[i] = arguments[i].type.getClazz(ctx);
			}
		}
	}

	public Clazz clazz;

	public NodeArgument[] arguments;

	public NodeConstructor bodyConstructor;

	public BodyConstructorType bodyConstructorType;

	public Node body;

	public Obj newInstance(RuntimeContext ctx, Field<?>[] arguments, Obj outboundObject) {
		return newInstance(ctx, arguments, null, outboundObject);
	}

	public Obj newInstance(RuntimeContext ctx, Field<?>[] arguments, Obj object, Obj outboundObject) {
		if (object == null) {
			object = new Obj(clazz, outboundObject);
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
		ctx.enterConstructor(this, object, -1);
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
				Obj superOutboundObject = ctx.getOutboundObject(clazz.superClass);
				Obj superObject = null;
				if (bodyConstructorType == BodyConstructorType.SUPER) {
					NodeConstructor.invokeConstructor(ctx, clazz.superClass, bodyConstructor.argValues, null, superOutboundObject);
					if (ctx.exitFromBlock()) {
						return null;
					}

					superObject = ctx.value.getObject();
				} else {
					// get default constructor from super classes
					Constructor superDefaultConstructor = clazz.superClass.getConstructor(ctx);
					if (superDefaultConstructor == null) {
						ctx.throwException("Constructor " + getConstructorDescr(clazz.fullName, null) + " not found");
						return null;
					}

					if (superDefaultConstructor == this) {
						ctx.throwException("cyclyc dependence for constructor " + superDefaultConstructor);
						return null;
					}

					superObject = superDefaultConstructor.newInstance(ctx, null, superOutboundObject);
					if (ctx.exitFromBlock()) {
						return null;
					}
				}
				object.setSuperObject(superObject);
			}

			// exit from constructor
			StackLevel constructorLevel = ctx.exit(true);

			// enter in object initialization
			ctx.enterInitialization(clazz, object, -1);
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

					object.fields = new Field[count];
					int index = 0;
					for (int i = 0; i < fields_count; i++) {
						if (!clazz.fields[i].isStatic()) {
							object.fields[index++] = (Field<?>) clazz.fields[i].clone();
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
								if (initializer instanceof Field<?>) {
									Field<?> field = (Field<?>) initializer;
									initializer = object.getField(field.name);
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
		modifiers.code(os);

		int count = arguments != null ? arguments.length : 0;
		os.writeByte(count);
		for (int i = 0; i < count; i++) {
			arguments[i].code(os);
		}

		os.writeNullable(body);
		os.writeNullable(bodyConstructor);
		os.writeInt(bodyConstructorType.getType());
	}

	public static Constructor decode(DecodeContext os) throws IOException {
		Modifiers modifiers = Modifiers.decode(os);

		int count = os.readByte();
		NodeArgument[] arguments = new NodeArgument[count];
		for (int i = 0; i < count; i++) {
			arguments[i] = NodeArgument.decode(os);
		}

		return new Constructor(os.getClazz(), modifiers, arguments, os.readNullable(Node.class), os.readNullable(NodeConstructor.class), BodyConstructorType.get(os.readByte()));
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
