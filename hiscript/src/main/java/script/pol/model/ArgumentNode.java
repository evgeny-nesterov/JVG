package script.pol.model;

public class ArgumentNode extends Node {
	public ArgumentNode(int type, int dimension, String name) {
		super("argument");
		this.type = type;
		this.dimension = dimension;
		this.name = name;
	}

	private int type;

	public int getType() {
		return type;
	}

	private int dimension;

	public int getDimension() {
		return dimension;
	}

	private String name;

	public String getArgName() {
		return name;
	}

	private Node statement;

	private Variable var;

	public void compile() throws ExecuteException {
		statement = getTopStatement();
		var = new Variable(null, name, type, dimension);
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		statement.addVariable(var);
	}
}
