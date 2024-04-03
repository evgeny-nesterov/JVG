package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.Breakable;

public class DoWhileNode extends Node implements Breakable {
	public DoWhileNode(Node condition, Node body) {
		super("do-while");
		this.condition = condition;
		this.body = body;
		isBlock = true;

		condition.setParent(this);
		body.setParent(this);
	}

	private final Node condition;

	public Node getCondition() {
		return condition;
	}

	private final Node body;

	public Node getBody() {
		return body;
	}

	private boolean isBroken = false;

	@Override
	public void breakBlock() {
		isBroken = true;
	}

	@Override
	public void compile() throws ExecuteException {
		condition.compile();

		if (body != null) {
			body.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		isBroken = false;
		do {
			if (body != null) {
				body.execute(ctx);
			}

			if (ctx.isExit) {
				return;
			}

			if (isBroken) {
				break;
			}

			condition.execute(ctx);
		} while (ctx.value.getBoolean());
	}
}
