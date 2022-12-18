package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.Arrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationPostfixDecrement extends UnaryOperation {
	private static Operation instance = new OperationPostfixDecrement();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPostfixDecrement() {
		super("--", POST_DECREMENT);
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

		HiField<?> var = v.variable;
		Value[] vs = ctx.getValues(1);
		try {
			Value tmp = vs[0];
			v.copyTo(tmp);

			switch (t) {
				case CHAR:
					tmp.character--;
					break;

				case BYTE:
					tmp.byteNumber--;
					break;

				case SHORT:
					tmp.shortNumber--;
					break;

				case INT:
					tmp.intNumber--;
					break;

				case LONG:
					tmp.longNumber--;
					break;

				case FLOAT:
					tmp.floatNumber--;
					break;

				case DOUBLE:
					tmp.doubleNumber--;
					break;
			}

			if (v.valueType == Value.ARRAY_INDEX) {
				Arrays.setArrayIndex(v.type, v.parentArray, v.arrayIndex, tmp, ctx.value);
			} else {
				var.set(ctx, tmp);
			}
		} finally {
			ctx.putValues(vs);
		}
	}
}
