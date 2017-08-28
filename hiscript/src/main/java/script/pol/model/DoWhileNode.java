package script.pol.model;

import script.Breakable;

public class DoWhileNode extends Node implements Breakable {
	public DoWhileNode(Node condition, Node body) {
		super("do-while");
		this.condition = condition;
		this.body = body;
		isBlock = true;

		condition.setParent(this);
		body.setParent(this);
	}

	private Node condition;

	public Node getCondition() {
		return condition;
	}

	private Node body;

	public Node getBody() {
		return body;
	}

	private boolean isBreaked = false;

	public void Break() {
		isBreaked = true;
	}

	public void compile() throws ExecuteException {
		condition.compile();

		if (body != null) {
			body.compile();
		}
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		isBreaked = false;
		do {
			if (body != null) {
				body.execute(ctx);
			}

			if (ctx.isExit) {
				return;
			}

			if (isBreaked) {
				break;
			}

			condition.execute(ctx);
		} while (ctx.value.getBoolean());
	}
}
