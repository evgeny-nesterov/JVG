package script.pol.model;

public class StringNode extends Node implements Value {
	public StringNode(String value) {
		super("string");
		this.value = value;
	}

	private String value;

	public String getString() {
		return value;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Types.STRING;
		ctx.value.dimension = 0;
		ctx.value.string = value;
	}
}
