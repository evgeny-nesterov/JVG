package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.*;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationPrefixDecrement extends UnaryOperation {
	private static final HiOperation instance = new OperationPrefixDecrement();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPrefixDecrement() {
		super(PREFIX_DECREMENT);
	}

	@Override
	public boolean isStatement() {
		return true;
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
        if (!node.isVariable()) {
            validationInfo.error("variable expected", node.token);
        }
		HiClass type = node.clazz.getAutoboxedPrimitiveClass() == null ? node.clazz : node.clazz.getAutoboxedPrimitiveClass();
		if (!type.isPrimitive() || type.getPrimitiveType() == BOOLEAN) {
			validationInfo.error("operation '" + name + "' cannot be applied to '" + node.clazz.getNameDescr() + "'", node.node.getToken());
		}
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		return node.clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value value) {
		HiClass c = value.getOperationClass();
		if (!c.isPrimitive()) {
			errorInvalidOperator(ctx, value.valueClass);
			return;
		}

		int t = c.getPrimitiveType();
		if (t == BOOLEAN) {
			errorInvalidOperator(ctx, value.valueClass);
			return;
		}

		switch (t) {
			case CHAR:
				value.character--;
				break;
			case BYTE:
				value.byteNumber--;
				break;
			case SHORT:
				value.shortNumber--;
				break;
			case INT:
				value.intNumber--;
				break;
			case LONG:
				value.longNumber--;
				break;
			case FLOAT:
				value.floatNumber--;
				break;
			case DOUBLE:
				value.doubleNumber--;
				break;
		}

		// autobox
		if (value.valueClass.isObject()) {
			value.valueClass = c;
			value.object = ((HiClassPrimitive) c).autobox(ctx, value);
		}

		HiField<?> var = value.variable;
		if (value.valueType == Value.ARRAY_INDEX) {
			HiArrays.setArrayIndex(value.valueClass, value.parentArray, value.arrayIndex, value, ctx.value);
		} else {
			var.set(ctx, value);
		}
	}
}
