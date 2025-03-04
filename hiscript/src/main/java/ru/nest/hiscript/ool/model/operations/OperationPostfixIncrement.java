package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import static ru.nest.hiscript.ool.model.OperationType.*;
import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class OperationPostfixIncrement extends UnaryOperation {
	private static final HiOperation instance = new OperationPostfixIncrement();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPostfixIncrement() {
		super(POST_INCREMENT);
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
		HiClass clazz = node.clazz.getAutoboxedPrimitiveClass() == null ? node.clazz : node.clazz.getAutoboxedPrimitiveClass();
		if (!clazz.isPrimitive() || clazz.getPrimitiveType() == BOOLEAN_TYPE) {
			validationInfo.error("operation '" + name + "' cannot be applied to '" + node.clazz.getNameDescr() + "'", node.node);
		}
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		return node.clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value value) {
		HiClass c = value.getOperationClass();
		PrimitiveType type = c.getPrimitiveType();
		HiField<?> var = value.variable;
		Value[] tmpValues = ctx.getValues(1);
		try {
			Value tmp = tmpValues[0];
			value.copyTo(tmp); // copy primitive value

			switch (type) {
				case CHAR_TYPE:
					tmp.character++;
					break;
				case BYTE_TYPE:
					tmp.byteNumber++;
					break;
				case SHORT_TYPE:
					tmp.shortNumber++;
					break;
				case INT_TYPE:
					tmp.intNumber++;
					break;
				case LONG_TYPE:
					tmp.longNumber++;
					break;
				case FLOAT_TYPE:
					tmp.floatNumber++;
					break;
				case DOUBLE_TYPE:
					tmp.doubleNumber++;
					break;
			}

			// @autoboxing
			if (value.valueClass.isObject()) {
				tmp.valueClass = c;
				tmp.object = ((HiClassPrimitive) c).box(ctx, tmp);
			}

			if (value.valueType == ValueType.ARRAY_INDEX) {
				HiArrays.setArrayIndex(value.valueClass, value.parentArray, value.arrayIndex, tmp, ctx.value);
			} else {
				var.set(ctx, tmp);
			}
		} finally {
			ctx.putValues(tmpValues);
		}
	}
}
