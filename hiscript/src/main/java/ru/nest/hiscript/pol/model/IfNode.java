package ru.nest.hiscript.pol.model;

import java.util.ArrayList;
import java.util.List;

public class IfNode extends Node {
	public IfNode(Node condition, Node body) {
		super("if");
		this.condition = condition;
		this.body = body;
		isBlock = true;

		if (condition != null) {
			condition.setParent(this);
		}
		if (body != null) {
			body.setParent(this);
		}
	}

	private Node condition;

	public Node getCondition() {
		return condition;
	}

	private Node body;

	public Node getBody() {
		return body;
	}

	private List<IfNode> elseIf = new ArrayList<>();

	public void addElseIf(IfNode node) {
		elseIf.add(node);
		node.setParent(this);
	}

	@Override
	public void compile() throws ExecuteException {
		if (condition != null) {
			condition.compile();
		}

		if (body != null) {
			body.compile();
		}

		for (IfNode node : elseIf) {
			node.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		if (condition != null) {
			condition.execute(ctx);
		}

		if (ctx.value.getBoolean()) {
			if (body != null) {
				body.execute(ctx);
			}
			return;
		}

		int size = elseIf.size();
		for (int i = 0; i < size; i++) {
			IfNode node = elseIf.get(i);
			boolean condition;
			if (node.getCondition() == null) {
				condition = true;
			} else {
				node.getCondition().execute(ctx);
				condition = ctx.value.getBoolean();
			}

			if (condition) {
				if (node.getBody() != null) {
					node.getBody().execute(ctx);
				}
				return;
			}
		}
	}
}
