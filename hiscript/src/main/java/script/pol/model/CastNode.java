package script.pol.model;

public class CastNode extends Node implements Value {
	public CastNode(int type, int dimension) {
		super("cast");
		this.type = type;
		this.dimension = dimension;
	}

	private int type;

	public int getType() {
		return type;
	}

	private int dimension;

	public int getDimension() {
		return dimension;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.cast(type, dimension);
	}
}
