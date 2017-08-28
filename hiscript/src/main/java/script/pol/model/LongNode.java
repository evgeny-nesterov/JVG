package script.pol.model;

public class LongNode extends Node implements Value {
	public LongNode(long value) {
		super("long");
		this.value = value;
	}

	private long value;

	public long getNumber() {
		return value;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Types.LONG;
		ctx.value.dimension = 0;
		ctx.value.longNumber = value;
	}
}
