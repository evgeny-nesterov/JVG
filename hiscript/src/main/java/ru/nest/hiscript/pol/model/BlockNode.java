package ru.nest.hiscript.pol.model;

import java.util.ArrayList;

import ru.nest.hiscript.Breakable;

public class BlockNode extends Node implements Breakable {
	public BlockNode() {
		super("block");
		isBlock = true;
	}

	private ArrayList<Node> statements = new ArrayList<Node>();

	public void addStatement(Node statement) {
		statements.add(statement);
		statement.setParent(this);
	}

	private boolean isBreaked = false;

	@Override
	public void Break() {
		isBreaked = true;
	}

	private int size;

	private Node[] nodes;

	public Node[] getStatements() {
		return nodes;
	}

	@Override
	public void compile() throws ExecuteException {
		size = statements.size();
		nodes = new Node[size];
		statements.toArray(nodes);

		for (int i = 0; i < size; i++) {
			Node statement = statements.get(i);
			statement.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		isBreaked = false;
		for (int i = 0; i < size; i++) {
			nodes[i].execute(ctx);
			nodes[i].removeVariables();

			if (ctx.isExit) {
				return;
			}

			if (isBreaked) {
				break;
			}
		}

		removeVariables();
	}
}
