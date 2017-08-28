package script.pol.model;

public class FloatNode extends Node implements Value {
	public FloatNode(float value) {
		super("float");
		this.value = value;
	}

	private float value;

	public float getNumber() {
		return value;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Types.FLOAT;
		ctx.value.dimension = 0;
		ctx.value.floatNumber = value;
	}
}
