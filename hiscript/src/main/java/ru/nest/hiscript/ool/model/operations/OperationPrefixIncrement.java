package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class OperationPrefixIncrement extends UnaryOperation {
	private static final HiOperation instance = new OperationPrefixIncrement();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPrefixIncrement() {
		super(PREFIX_INCREMENT);
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
		switch (c.getPrimitiveType()) {
			case CHAR_TYPE:
				value.character++;
				break;
			case BYTE_TYPE:
				value.byteNumber++;
				break;
			case SHORT_TYPE:
				value.shortNumber++;
				break;
			case INT_TYPE:
				value.intNumber++;
				break;
			case LONG_TYPE:
				value.longNumber++;
				break;
			case FLOAT_TYPE:
				value.floatNumber++;
				break;
			case DOUBLE_TYPE:
				value.doubleNumber++;
				break;
		}

		// @autoboxing
		if (value.valueClass.isObject()) {
			value.valueClass = c;
			value.object = ((HiClassPrimitive) c).box(ctx, value);
		}

		HiField<?> var = value.variable;
		if (value.valueType == ValueType.ARRAY_INDEX) {
			HiArrays.setArrayIndex(value.valueClass, value.parentArray, value.arrayIndex, value, ctx.value);
		} else {
			var.set(ctx, value);
		}
	}
}
