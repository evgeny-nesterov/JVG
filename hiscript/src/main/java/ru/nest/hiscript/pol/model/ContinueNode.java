package ru.nest.hiscript.pol.model;

import java.util.ArrayList;
import java.util.List;

import ru.nest.hiscript.Breakable;

public class ContinueNode extends Node {
	public ContinueNode(String mark) {
		super("continue");
		this.mark = mark;
	}

	@Override
	public void compile() {
		Node parent = getParent();
		while (parent != null) {
			if (mark == null) {
				if (parent instanceof WhileNode || parent instanceof ForNode) {
					break;
				}
			}

			if (parent instanceof Breakable) {
				breakableNodes.add((Breakable) parent);
			}

			parent = parent.getParent();
		}
	}

	private final String mark;

	public String getMark() {
		return mark;
	}

	private final List<Breakable> breakableNodes = new ArrayList<>();

	public List<Breakable> getBreakableNodes() {
		return breakableNodes;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		int size = breakableNodes.size();
		for (int i = 0; i < size; i++) {
			breakableNodes.get(i).breakBlock();
		}
	}
}
