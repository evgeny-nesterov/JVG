package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationPostfixIncrement extends UnaryOperation {
	private static HiOperation instance = new OperationPostfixIncrement();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPostfixIncrement() {
		super("++", POST_INCREMENT);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node) {
		if (!node.type.isPrimitive() || HiFieldPrimitive.getType(node.type) == BOOLEAN) {
			validationInfo.error("operation '" + name + "' cannot be applied to '" + node.type.fullName + "'", node.node.getToken());
		}
		return node.type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		HiClass c = v.type;

		boolean isPrimitive = c.isPrimitive();
		if (!isPrimitive) {
			errorInvalidOperator(ctx, c);
			return;
		}

		int type = HiFieldPrimitive.getType(c);
		if (type == BOOLEAN) {
			errorInvalidOperator(ctx, c);
			return;
		}

		HiField<?> var = v.variable;
		Value[] vs = ctx.getValues(1);
		try {
			Value tmp = vs[0];
			v.copyTo(tmp);

			switch (type) {
				case CHAR:
					tmp.character++;
					break;

				case BYTE:
					tmp.byteNumber++;
					break;

				case SHORT:
					tmp.shortNumber++;
					break;

				case INT:
					tmp.intNumber++;
					break;

				case LONG:
					tmp.longNumber++;
					break;

				case FLOAT:
					tmp.floatNumber++;
					break;

				case DOUBLE:
					tmp.doubleNumber++;
					break;
			}

			if (v.valueType == Value.ARRAY_INDEX) {
				HiArrays.setArrayIndex(v.type, v.parentArray, v.arrayIndex, tmp, ctx.value);
			} else {
				var.set(ctx, tmp);
			}
		} finally {
			ctx.putValues(vs);
		}
	}
}
