package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.OperationSymbols;

import java.util.ArrayList;
import java.util.List;

public class ExpressionNode extends Node implements Value {
	public ExpressionNode(PrefixNode prefix, Node value, ArrayIndexesNode index) {
		super("expression");
		addValue(prefix, value, index);
	}

	public void doOperation(int operation, PrefixNode prefix, Node value, ArrayIndexesNode index) {
		operations.add(operation);
		addValue(prefix, value, index);
	}

	private void addValue(PrefixNode prefix, Node value, ArrayIndexesNode index) {
		prefixes.add(prefix);
		if (prefix != null) {
			prefix.setParent(this);
		}

		values.add(value);
		value.setParent(this);

		indexes.add(index);
		if (index != null) {
			index.setParent(this);
		}
	}

	private List<Integer> operations = new ArrayList<>();

	public List<Integer> getOperations() {
		return operations;
	}

	private List<PrefixNode> prefixes = new ArrayList<>();

	public List<PrefixNode> getPrefixes() {
		return prefixes;
	}

	private List<Node> values = new ArrayList<>();

	public List<Node> getValues() {
		return values;
	}

	private List<ArrayIndexesNode> indexes = new ArrayList<>();

	public List<ArrayIndexesNode> getIndexes() {
		return indexes;
	}

	@Override
	public void compile() throws ExecuteException {
		int valuesCount = values.size();
		int operationsCount = operations.size();
		list = new int[valuesCount + operationsCount];

		for (int i = 0; i < valuesCount; i++) {
			Node value = values.get(i);
			value.compile();

			ArrayIndexesNode index = indexes.get(i);
			if (index != null) {
				index.compile();
			}

			PrefixNode prefix = prefixes.get(i);
			if (prefix != null) {
				prefix.compile();
			}
		}

		if (valuesCount == 1) {
			list[0] = 0;
			return;
		} else if (valuesCount == 2) {
			list[0] = 0;
			list[1] = 0;
			list[2] = operations.get(0);
			buffer = new ValueContainer[2];
			return;
		}

		buffer = new ValueContainer[valuesCount];
		list[0] = 0;
		list[1] = 0;

		int[] buf = new int[operationsCount];
		buf[0] = operations.get(0);
		int bufSize = 1;

		int pos = 2;
		for (int i = 1; i < operationsCount; i++) {
			int o = operations.get(i);
			int p = OperationSymbols.getPriority(o);

			int lo = buf[bufSize - 1];
			int lp = OperationSymbols.getPriority(lo);
			while (lp >= p) {
				list[pos++] = lo; // operation
				bufSize--;

				if (bufSize == 0) {
					break;
				}

				lo = buf[bufSize - 1];
				lp = OperationSymbols.getPriority(lo);
			}

			list[pos++] = 0; // value
			buf[bufSize++] = operations.get(i);
		}

		while (bufSize != 0) {
			list[pos++] = buf[--bufSize];
		}
	}

	private int[] list = null;

	private ValueContainer[] buffer = null;

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		if (values.size() == 1) {
			Node value = values.get(0);
			value.execute(ctx);

			ArrayIndexesNode index = indexes.get(0);
			if (index != null) {
				index.execute(ctx);
			}

			PrefixNode prefix = prefixes.get(0);
			if (prefix != null) {
				prefix.execute(ctx);
			}
			return;
		} else {
			int bufSize = 0;
			int valuePos = 0;
			for (int i = 0; i < list.length; i++) {
				if (list[i] == 0) // value
				{
					Node valueNode = values.get(valuePos);
					valueNode.execute(ctx);

					ArrayIndexesNode index = indexes.get(valuePos);
					if (index != null) {
						index.execute(ctx);
					}

					PrefixNode prefix = prefixes.get(valuePos);
					if (prefix != null) {
						prefix.execute(ctx);
					}

					if (buffer[bufSize] == null) {
						buffer[bufSize] = new ValueContainer();
					}
					buffer[bufSize].type = ctx.value.type;
					buffer[bufSize].dimension = ctx.value.dimension;
					ctx.value.copy(buffer[bufSize]);

					valuePos++;
					bufSize++;
				} else // operation
				{
					if (OperationSymbols.isEquate(list[i])) {
						processEquateOperation(list[i], valuePos - 2, buffer[bufSize - 2], buffer[bufSize - 1]);
					} else {
						Operations.doOperation(buffer[bufSize - 2], buffer[bufSize - 1], list[i]);
					}
					bufSize--;
				}
			}

			ctx.value.type = buffer[0].type;
			ctx.value.dimension = buffer[0].dimension;
			buffer[0].copy(ctx.value);
		}
	}

	private void processEquateOperation(int equateType, int valueIndex, ValueContainer left, ValueContainer right) throws ExecuteException {
		Variable var = getVariable(valueIndex);
		if (valueIndex != 0) {
			while (--valueIndex >= 0) {
				if (values.get(valueIndex) instanceof VariableNode && OperationSymbols.isEquate(operations.get(valueIndex))) {
					continue;
				} else {
					break;
				}
			}

			if (valueIndex != 0) {
				throw new ExecuteException("incompatible types");
			}
		}

		Operations.doOperation(var.getValue(), right, equateType);
		var.getValue().copy(left);
	}

	private Variable getVariable(int valueIndex) throws ExecuteException {
		Node node = values.get(valueIndex);
		if (node instanceof VariableNode) {
			VariableNode varNode = (VariableNode) node;
			Variable var = getVariable(varNode.getFullname());
			return var;
		} else {
			throw new ExecuteException("variable is expected");
		}
	}
}
