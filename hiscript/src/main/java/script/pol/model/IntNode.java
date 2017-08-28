package script.pol.model;

public class IntNode extends Node implements Value {
	public IntNode(int value) {
		super("int");
		this.value = value;
	}

	private int value;

	public int getNumber() {
		return value;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Types.INT;
		ctx.value.dimension = 0;
		ctx.value.intNumber = value;
	}
}
