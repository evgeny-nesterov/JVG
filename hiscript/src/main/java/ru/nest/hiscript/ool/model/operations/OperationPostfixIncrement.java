package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

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
		int type = c.getPrimitiveType();
		HiField<?> var = value.variable;
		Value[] tmpValues = ctx.getValues(1);
		try {
			Value tmp = tmpValues[0];
			value.copyTo(tmp); // copy primitive value

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

			// @autobox
			if (value.valueClass.isObject()) {
				tmp.valueClass = c;
				tmp.object = ((HiClassPrimitive) c).autobox(ctx, tmp);
			}

			if (value.valueType == Value.ARRAY_INDEX) {
				HiArrays.setArrayIndex(value.valueClass, value.parentArray, value.arrayIndex, tmp, ctx.value);
			} else {
				var.set(ctx, tmp);
			}
		} finally {
			ctx.putValues(tmpValues);
		}
	}
}
