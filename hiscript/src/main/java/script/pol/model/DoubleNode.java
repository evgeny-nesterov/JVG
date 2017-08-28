package script.pol.model;

public class DoubleNode extends Node implements Value {
	public DoubleNode(double value) {
		super("double");
		this.value = value;
	}

	private double value;

	public double getNumber() {
		return value;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Types.DOUBLE;
		ctx.value.dimension = 0;
		ctx.value.doubleNumber = value;
	}
}
