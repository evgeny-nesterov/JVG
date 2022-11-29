package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.Breakable;

public class ForNode extends Node implements Breakable {
	public ForNode(Node condition, Node assignments, Node body) {
		super("for");
		this.initialization = null;
		this.condition = condition;
		this.assignments = assignments;
		this.body = body;
		isBlock = true;

		condition.setParent(this);
		assignments.setParent(this);
		body.setParent(this);
	}

	public ForNode(AssignmentsNode initialization, Node condition, Node assignments, Node body) {
		this(condition, assignments, body);
		this.initialization = initialization;
		initialization.setParent(this);
	}

	public ForNode(DeclarationsNode initialization, Node condition, Node assignments, Node body) {
		this(condition, assignments, body);
		this.initialization = initialization;
		initialization.setParent(this);
	}

	private Node initialization;

	public Node getInitialization() {
		return initialization;
	}

	private Node condition;

	public Node getCondition() {
		return condition;
	}

	private Node assignments;

	public Node getAssignments() {
		return assignments;
	}

	private Node body;

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
		if (initialization != null) {
			initialization.compile();
		}

		if (condition != null) {
			condition.compile();
		}

		if (assignments != null) {
			assignments.compile();
		}

		if (body != null) {
			body.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		isBroken = false;

		if (initialization != null) {
			initialization.execute(ctx);
		}

		if (condition != null) {
			condition.execute(ctx);
		}

		while (condition == null || ctx.value.getBoolean()) {
			if (body != null) {
				body.execute(ctx);
			}

			if (ctx.isExit) {
				return;
			}

			if (isBroken) {
				break;
			}

			if (assignments != null) {
				assignments.execute(ctx);
			}

			if (condition != null) {
				condition.execute(ctx);
			}
		}
	}
}
