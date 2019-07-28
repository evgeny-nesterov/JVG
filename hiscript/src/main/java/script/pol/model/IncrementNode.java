package script.pol.model;

public class IncrementNode extends Node implements Value {
	public final static int INCREMENT_PREFIX = 0;

	public final static int INCREMENT_POSTFIX = 1;

	public IncrementNode(VariableNode value, int incrementType, int incrementOperation) {
		super("increment");
		this.value = value;
		this.incrementType = incrementType;
		this.incrementOperation = incrementOperation;
	}

	private VariableNode value;

	private int incrementType;

	private int incrementOperation;

	@Override
	public void compile() throws ExecuteException {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		String varFullname = value.getFullname();
		Variable var = getVariable(varFullname);

		if (var != null) {
			switch (incrementType) {
				case INCREMENT_PREFIX:
					Operations.doIncrementOperation(var.getValue(), incrementOperation);

					ctx.value.type = var.getValue().type;
					var.getValue().copy(ctx.value);
					break;

				case INCREMENT_POSTFIX:
					ctx.value.type = var.getValue().type;
					var.getValue().copy(ctx.value);

					Operations.doIncrementOperation(var.getValue(), incrementOperation);
					break;
			}
		} else {
			throw new ExecuteException("can not find variable '" + varFullname + "'");
		}
	}
}
