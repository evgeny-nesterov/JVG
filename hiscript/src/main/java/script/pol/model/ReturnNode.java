package script.pol.model;

public class ReturnNode extends Node {
	public ReturnNode(Node value) {
		super("return");
		this.value = value;

		if (value != null) {
			value.setParent(this);
		}
	}

	private Node value;

	public Node getValue() {
		return value;
	}

	public void compile() throws ExecuteException {
		if (value != null) {
			value.compile();
		}
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.isExit = true;

		if (value != null) {
			value.execute(ctx);
		} else {
			ctx.value.dimension = 0;
			ctx.value.type = Types.VOID;
		}
	}
}
