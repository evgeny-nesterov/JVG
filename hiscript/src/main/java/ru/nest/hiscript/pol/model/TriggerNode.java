package ru.nest.hiscript.pol.model;

public class TriggerNode extends Node {
	public TriggerNode(Node condition, Node trueValue, Node falseValue) {
		super("trigger");
		this.condition = condition;
		this.trueValue = trueValue;
		this.falseValue = falseValue;

		condition.setParent(this);
		trueValue.setParent(this);
		falseValue.setParent(this);
	}

	private final Node condition;

	public Node getCondition() {
		return condition;
	}

	private final Node trueValue;

	public Node getTrueValue() {
		return trueValue;
	}

	private final Node falseValue;

	public Node getFalseValue() {
		return falseValue;
	}

	@Override
	public void compile() throws ExecuteException {
		condition.compile();
		trueValue.compile();
		falseValue.compile();
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		condition.execute(ctx);
		if (ctx.value.getBoolean()) {
			trueValue.execute(ctx);
		} else {
			falseValue.execute(ctx);
		}
	}
}
