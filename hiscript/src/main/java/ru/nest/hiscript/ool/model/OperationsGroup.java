package ru.nest.hiscript.ool.model;

import java.util.ArrayList;
import java.util.List;

/**
 *  (operand) [postfix operations...] operation [prefix operations...] (operand)
 */
public class OperationsGroup {
	private HiOperation operation;

	private List<HiOperation> prefix;

	public List<HiOperation> postfix;

	public OperationsGroup() {
	}

	public OperationsGroup(int operation) {
		this.operation = Operations.getOperation(operation);
	}

	public int appendPrefix(HiOperation[] stack, int index) {
		if (prefix != null) {
			for (int i = 0; i < prefix.size(); i++) {
				stack[index++] = prefix.get(i);
			}
		}
		return index;
	}

	public int append(HiOperation[] stack, int index) {
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

	public HiOperation getOperation() {
		return operation;
	}

	public void addPrefixOperation(int o) {
		if (prefix == null) {
			prefix = new ArrayList<>(1);
		}
		prefix.add(Operations.getOperation(o));
	}

	public void addPostfixOperation(int o) {
		if (postfix == null) {
			postfix = new ArrayList<>(1);
		}
		postfix.add(Operations.getOperation(o));
	}

	public boolean hasOperations() {
		return operation != null || prefix != null || postfix != null;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (postfix != null) {
			for (HiOperation o : postfix) {
				buf.append(o.getName()).append(' ');
			}
		}
		if (operation != null) {
			buf.append("[").append(operation.getName()).append("] ");
		}
		if (prefix != null) {
			for (HiOperation o : prefix) {
				buf.append(o.getName()).append(' ');
			}
		}
		return buf.toString().trim();
	}
}
