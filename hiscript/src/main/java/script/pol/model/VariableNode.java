package script.pol.model;

public class VariableNode extends Node implements Value {
	public VariableNode(String namespace, String varName) {
		super("variable");
		if (namespace != null && namespace.length() > 0) {
			this.fullname = namespace + "." + varName;
		} else {
			this.fullname = varName.intern();
			namespace = "";
		}
		this.namespace = namespace.intern();
		this.varName = varName.intern();
	}

	private String fullname;

	public String getFullname() {
		return fullname;
	}

	private String namespace;

	public String getNamespace() {
		return namespace;
	}

	private String varName;

	public String getVarName() {
		return varName;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		Variable var = getVariable(fullname);
		if (var != null) {
			ctx.value.type = var.getValue().type;
			ctx.value.dimension = var.getValue().dimension;
			var.getValue().copy(ctx.value);
		} else {
			throw new ExecuteException("can not find variable '" + varName + "'");
		}
	}
}
