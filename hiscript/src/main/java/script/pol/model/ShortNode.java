package script.pol.model;

public class ShortNode extends Node implements Value {
	public ShortNode(short value) {
		super("int");
		this.value = value;
	}

	private short value;

	public short getNumber() {
		return value;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Types.SHORT;
		ctx.value.dimension = 0;
		ctx.value.shortNumber = value;
	}
}
