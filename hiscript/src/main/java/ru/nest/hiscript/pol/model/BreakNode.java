package ru.nest.hiscript.pol.model;

import java.util.ArrayList;

import ru.nest.hiscript.Breakable;

public class BreakNode extends Node {
	public BreakNode(String mark) {
		super("break");
		this.mark = mark;
	}

	@Override
	public void compile() throws ExecuteException {
		Node parent = getParent();
		while (parent != null) {
			if (parent instanceof Breakable) {
				breakableNodes.add((Breakable) parent);
			}

			if (mark == null) {
				if (parent instanceof WhileNode || parent instanceof ForNode || parent instanceof SwitchNode) {
					break;
				}
			} else if (parent instanceof MarkNode) {
				MarkNode node = (MarkNode) parent;
				if (mark.equals(node.getMarkName())) {
					break;
				}
			}

			parent = parent.getParent();
		}
	}

	private String mark;

	public String getMark() {
		return mark;
	}

	private ArrayList<Breakable> breakableNodes = new ArrayList<Breakable>();

	public ArrayList<Breakable> getBreakableNodes() {
		return breakableNodes;
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		int size = breakableNodes.size();
		for (int i = 0; i < size; i++) {
			Breakable breakable = breakableNodes.get(i);
			breakable.Break();
		}
	}
}
