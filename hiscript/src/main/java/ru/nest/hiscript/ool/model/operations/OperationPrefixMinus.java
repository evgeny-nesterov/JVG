package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationPrefixMinus extends UnaryOperation {
	private static HiOperation instance = new OperationPrefixMinus();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPrefixMinus() {
		super("-", PREFIX_MINUS);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
		if (!node.type.isPrimitive() || HiFieldPrimitive.getType(node.type) == BOOLEAN) {
			validationInfo.error("operation '" + name + "' cannot be applied to '" + node.type.fullName + "'", node.node.getToken());
		}
		return node.type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		HiClass c = v.type;

		boolean isP = c.isPrimitive();
		if (!isP) {
			errorInvalidOperator(ctx, c);
			return;
		}

		int t = HiFieldPrimitive.getType(c);
		if (t == BOOLEAN) {
			errorInvalidOperator(ctx, c);
			return;
		}

		switch (t) {
			case CHAR:
				v.type = TYPE_INT;
				v.intNumber = -v.character;
				break;

			case BYTE:
				v.type = TYPE_INT;
				v.intNumber = -v.byteNumber;
				break;

			case SHORT:
				v.type = TYPE_INT;
				v.intNumber = -v.shortNumber;
				break;

			case INT:
				v.intNumber = -v.intNumber;
				break;

			case LONG:
				v.longNumber = -v.longNumber;
				break;

			case FLOAT:
				v.floatNumber = -v.floatNumber;
				break;

			case DOUBLE:
				v.doubleNumber = -v.doubleNumber;
				break;
		}
	}
}
