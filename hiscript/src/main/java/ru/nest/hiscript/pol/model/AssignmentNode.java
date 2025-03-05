package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.OperationSymbols;
import ru.nest.hiscript.tokenizer.SymbolType;

import java.util.List;

public class AssignmentNode extends Node {
	public AssignmentNode(String namespace, String varName, List<ExpressionNode> indexes, Node value, SymbolType equateType) {
		super("assignment");
		if (namespace != null && namespace.length() > 0) {
			fullname = namespace + "." + varName;
		} else {
			fullname = varName;
			namespace = "";
		}

		this.namespace = namespace.intern();
		this.varName = varName.intern();
		this.indexes = indexes;
		this.value = value;
		this.equateType = equateType;

		for (ExpressionNode index : indexes) {
			index.setParent(this);
		}
		value.setParent(this);
	}

	private final String fullname;

	public String getFullname() {
		return fullname;
	}

	private final String namespace;

	public String getNamespace() {
		return namespace;
	}

	private final String varName;

	public String getVarName() {
		return varName;
	}

	private final List<ExpressionNode> indexes;

	private final Node value;

	public Node getValue() {
		return value;
	}

	private final SymbolType equateType;

	public SymbolType getEquateType() {
		return equateType;
	}

	private int[] dimensions;

	@Override
	public void compile() throws ExecuteException {
		value.compile();
		for (ExpressionNode index : indexes) {
			index.compile();
		}

		if (indexes.size() > 0) {
			dimensions = new int[indexes.size()];
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		Variable var = getVariable(fullname);
		if (var != null) {
			if (indexes.size() == 0) {
				value.execute(ctx);
				if (equateType == SymbolType.EQUATE) {
					var.define();
				} else if (OperationSymbols.isEquate(equateType) && !var.isDefined()) {
					throw new ExecuteException("variable '" + var.getFullname() + "' is not initialized");
				}
				Operations.doOperation(var.getValue(), ctx.value, equateType);
			} else {
				for (int i = 0; i < dimensions.length; i++) {
					ExpressionNode index = indexes.get(i);
					index.execute(ctx);
					dimensions[i] = ctx.value.getInt();
				}

				value.execute(ctx);
				if (equateType == SymbolType.EQUATE) {
					var.define();
				} else if (OperationSymbols.isEquate(equateType) && !var.isDefined()) {
					throw new ExecuteException("variable '" + var.getFullname() + "' is not initialized");
				}

				var.getValue().setArrayValue(dimensions, ctx.value, equateType);
			}
		} else {
			throw new ExecuteException("can not find variable " + varName);
		}
	}
}
