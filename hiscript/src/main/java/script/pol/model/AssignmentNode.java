package script.pol.model;

import java.util.ArrayList;
import java.util.List;

import script.tokenizer.OperationSymbols;
import script.tokenizer.Symbols;

public class AssignmentNode extends Node {
	public AssignmentNode(String namespace, String varName, List<ExpressionNode> indexes, Node value, int equateType) {
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

	private String fullname;

	public String getFullname() {
		return fullname;
	}

	private String namespace;

	public String getNamespace() {
		return namespace;
	}

	private String varName;

	public String getVarName() {
		return varName;
	}

	private List<ExpressionNode> indexes = new ArrayList<ExpressionNode>();

	private Node value;

	public Node getValue() {
		return value;
	}

	private int equateType;

	public int getEquateType() {
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
				if (equateType == Symbols.EQUATE) {
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
				if (equateType == Symbols.EQUATE) {
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
