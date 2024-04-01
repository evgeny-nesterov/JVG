package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
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
		HiClass type = node.type.getAutoboxedPrimitiveClass() == null ? node.type : node.type.getAutoboxedPrimitiveClass();
		if (!type.isPrimitive() || HiFieldPrimitive.getType(type) == BOOLEAN) {
			validationInfo.error("operation '" + name + "' cannot be applied to '" + node.type.fullName + "'", node.node.getToken());
		}
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		return node.type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value value) {
		HiClass c = value.getOperationClass();
		if (!c.isPrimitive()) {
			errorInvalidOperator(ctx, value.type);
			return;
		}

		int t = HiFieldPrimitive.getType(c);
		if (t == BOOLEAN) {
			errorInvalidOperator(ctx, value.type);
			return;
		}

		switch (t) {
			case CHAR:
				value.character++;
				break;
			case BYTE:
				value.byteNumber++;
				break;
			case SHORT:
				value.shortNumber++;
				break;
			case INT:
				value.intNumber++;
				break;
			case LONG:
				value.longNumber++;
				break;
			case FLOAT:
				value.floatNumber++;
				break;
			case DOUBLE:
				value.doubleNumber++;
				break;
		}
		if (value.type.isObject()) {
			value.type = c;
			value.object = ((HiClassPrimitive) c).autobox(ctx, value);
		}

		HiField<?> var = value.variable;
		if (value.valueType == Value.ARRAY_INDEX) {
			HiArrays.setArrayIndex(value.type, value.parentArray, value.arrayIndex, value, ctx.value);
		} else {
			var.set(ctx, value);
		}
	}
}
