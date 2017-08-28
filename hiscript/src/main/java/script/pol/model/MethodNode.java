package script.pol.model;

public class MethodNode extends Node {
	public MethodNode(String name, int type, int dimension, ArgumentsNode arguments, BlockNode body) {
		super("method");
		this.name = name;
		this.type = type;
		this.dimension = dimension;
		this.arguments = arguments;
		this.body = body;
		isBlock = true;

		if (arguments != null) {
			arguments.setParent(this);
		}

		if (body != null) {
			body.setParent(this);
		}
	}

	private String name;

	public String getMethodName() {
		return name;
	}

	private int type;

	public int getType() {
		return type;
	}

	private int dimension;

	public int getDimension() {
		return dimension;
	}

	private ArgumentsNode arguments;

	public ArgumentsNode getArguments() {
		return arguments;
	}

	private BlockNode body;

	public BlockNode getBody() {
		return body;
	}

	private Method method;

	public Method getMethod() {
		return method;
	}

	public void compile() throws ExecuteException {
		int[] types;
		int[] dimensions;
		if (arguments != null) {
			arguments.compile();
			types = arguments.getTypes();
			dimensions = arguments.getDimensions();
		} else {
			types = new int[0];
			dimensions = new int[0];
		}

		if (body != null) {
			body.compile();
		}

		method = new Method(null, name, types, dimensions, type, dimension) {
			public void invoke(RuntimeContext ctx, Node parent, Object... values) throws ExecuteException {
				super.invoke(ctx, parent, values);

				int argCount;
				if (arguments != null) {
					arguments.execute(ctx);
					argCount = arguments.getNames().length;
				} else {
					argCount = 0;
				}

				for (int i = 0; i < argCount; i++) {
					int type = Types.getType(values[i].getClass());
					ctx.value.setValue(values[i], type);

					Variable var = getVariable(arguments.getNames()[i]);
					Operations.doOperation(var.getValue(), ctx.value, Operations.EQUATE);
				}

				if (body != null) {
					body.execute(ctx);
				}
				removeVariables();

				ctx.isExit = false;
				ctx.value.cast(type, dimension);
			}
		};
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		Node parent = getParent();
		if (parent != null) {
			parent.addMethod(ctx, method);
		}
	}
}
