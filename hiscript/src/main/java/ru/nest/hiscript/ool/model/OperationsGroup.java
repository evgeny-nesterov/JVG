package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.nodes.CodeContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OperationsGroup {
	public static ArrayList<Operation> empty = new ArrayList<Operation>(0);

	private Operation operation;

	private List<Operation> prefix;

	public List<Operation> postfix;

	public OperationsGroup() {
	}

	public OperationsGroup(int operation) {
		setOperation(operation);
	}

	public int appendPrefix(Operation[] stack, int index) {
		if (prefix != null) {
			for (int i = 0; i < prefix.size(); i++) {
				stack[index++] = prefix.get(i);
			}
		}
		return index;
	}

	public int appendPostfix(Operation[] stack, int index) {
		if (postfix != null) {
			for (int i = postfix.size() - 1; i >= 0; i--) {
				stack[index++] = postfix.get(i);
			}
		}
		return index;
	}

	public int append(Operation[] stack, int index) {
		if (operation != null) {
			stack[index++] = operation;
		}
		return index;
	}

	public int getPrefixOperandsCount() {
		int count = 0;
		if (prefix != null) {
			int size = prefix.size();
			for (int i = 0; i < size; i++) {
				count += prefix.get(i).getIncrement();
			}
		}
		return count;
	}

	public int getPostfixOperandsCount() {
		int count = 0;
		if (postfix != null) {
			int size = postfix.size();
			for (int i = 0; i < size; i++) {
				count += postfix.get(i).getIncrement();
			}
		}
		return count;
	}

	public int getOperandsCount() {
		if (operation != null) {
			return operation.getIncrement();
		} else {
			return 0;
		}
	}

	public int getCount() {
		int count = (prefix != null ? prefix.size() : 0) + (postfix != null ? postfix.size() : 0);
		if (operation != null) {
			count++;
		}
		return count;
	}

	public void setOperation(int o) {
		operation = Operations.getOperation(o);
	}

	public Operation getOperation() {
		return operation;
	}

	public void addPrefixOperation(int o) {
		if (prefix == null) {
			prefix = new ArrayList<Operation>(1);
		}
		prefix.add(Operations.getOperation(o));
	}

	public List<Operation> getPrefixOperations() {
		return prefix != null ? prefix : empty;
	}

	public boolean hasPrefixOperations() {
		return prefix != null;
	}

	public void addPostfixOperation(int o) {
		if (postfix == null) {
			postfix = new ArrayList<Operation>(1);
		}
		postfix.add(Operations.getOperation(o));
	}

	public List<Operation> getPostfixOperations() {
		return postfix != null ? postfix : empty;
	}

	public boolean hasPostfixOperations() {
		return postfix != null;
	}

	public boolean hasOperations() {
		return operation != null || prefix != null || postfix != null;
	}

	public int getMinPriority() {
		int priority = Integer.MAX_VALUE;
		if (operation != null) {
			priority = operation.getPriority();
		}

		if (prefix != null) {
			int size = prefix.size();
			for (int i = 0; i < size; i++) {
				Operation o = prefix.get(i);
				priority = Math.min(priority, o.getPriority());
			}
		}

		if (postfix != null) {
			int size = postfix.size();
			for (int i = 0; i < size; i++) {
				Operation o = postfix.get(i);
				priority = Math.min(priority, o.getPriority());
			}
		}

		// set -1 if there are no one operation
		if (priority == Integer.MAX_VALUE) {
			priority = -1;
		}
		return priority;
	}

	public int getMaxPriority() {
		int priority = 0;
		if (operation != null) {
			priority = operation.getPriority();
		}

		if (prefix != null) {
			for (int i = 0; i < prefix.size(); i++) {
				Operation o = prefix.get(i);
				priority = Math.max(priority, o.getPriority());
			}
		}

		if (postfix != null) {
			for (int i = 0; i < postfix.size(); i++) {
				Operation o = postfix.get(i);
				priority = Math.max(priority, o.getPriority());
			}
		}
		return priority;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (prefix != null) {
			for (Operation o : prefix) {
				buf.append(o.getName());
				buf.append(' ');
			}
		}

		if (operation != null) {
			buf.append("[");
			buf.append(operation.getName());
			buf.append("] ");
		}

		if (postfix != null) {
			for (Operation o : postfix) {
				buf.append(o.getName());
				buf.append(' ');
			}
		}
		return buf.toString().trim();
	}

	public void code(CodeContext os) throws IOException {
		// TODO: write start operation group data block
		operation.code(os);

		os.writeByte(prefix != null ? prefix.size() : 0);
		if (prefix != null) {
			for (int i = 0; i < prefix.size(); i++) {
				prefix.get(i).code(os);
			}
		}

		os.writeByte(postfix != null ? postfix.size() : 0);
		if (postfix != null) {
			for (int i = 0; i < postfix.size(); i++) {
				postfix.get(i).code(os);
			}
		}
	}
}
