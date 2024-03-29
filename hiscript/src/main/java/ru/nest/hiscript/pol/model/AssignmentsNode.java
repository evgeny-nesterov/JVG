package ru.nest.hiscript.pol.model;

import java.util.ArrayList;
import java.util.List;

public class AssignmentsNode extends Node {
	public AssignmentsNode() {
		super("assignments");
	}

	public List<Node> assignments = new ArrayList<>();

	public void addAssignment(Node assignment) {
		assignments.add(assignment);
		assignment.setParent(this);
	}

	@Override
	public void compile() throws ExecuteException {
		for (Node node : assignments) {
			node.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		int size = assignments.size();
		for (int i = 0; i < size; i++) {
			Node node = assignments.get(i);
			node.execute(ctx);
		}
	}
}
