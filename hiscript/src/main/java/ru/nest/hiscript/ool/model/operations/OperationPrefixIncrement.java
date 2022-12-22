package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationPrefixIncrement extends UnaryOperation {
	private static HiOperation instance = new OperationPrefixIncrement();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPrefixIncrement() {
		super("++", PREFIX_INCREMENT);
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
				v.character++;
				break;

			case BYTE:
				v.byteNumber++;
				break;

			case SHORT:
				v.shortNumber++;
				break;

			case INT:
				v.intNumber++;
				break;

			case LONG:
				v.longNumber++;
				break;

			case FLOAT:
				v.floatNumber++;
				break;

			case DOUBLE:
				v.doubleNumber++;
				break;
		}

		HiField<?> var = v.variable;
		if (v.valueType == Value.ARRAY_INDEX) {
			HiArrays.setArrayIndex(v.type, v.parentArray, v.arrayIndex, v, ctx.value);
		} else {
			var.set(ctx, v);
		}
	}
}
