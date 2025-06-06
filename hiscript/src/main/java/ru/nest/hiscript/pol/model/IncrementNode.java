package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.SymbolType;

public class IncrementNode extends Node implements Value {
	public final static int INCREMENT_PREFIX = 0;

	public final static int INCREMENT_POSTFIX = 1;

	public IncrementNode(VariableNode value, int incrementType, SymbolType incrementOperation) {
		super("increment");
		this.value = value;
		this.incrementType = incrementType;
		this.incrementOperation = incrementOperation;
	}

	private final VariableNode value;

	private final int incrementType;

	private final SymbolType incrementOperation;

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		String varFullName = value.getFullname();
		Variable var = getVariable(varFullName);

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
			throw new ExecuteException("can not find variable '" + varFullName + "'");
		}
	}
}
