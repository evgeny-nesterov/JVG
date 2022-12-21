package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.lang.reflect.Array;

public class OperationArrayIndex extends BinaryOperation {
	private static HiOperation instance = new OperationArrayIndex();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationArrayIndex() {
		super("[]", ARRAY_INDEX);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass type = node1.type;
		boolean validIndex = false;
		if (node2.type.isPrimitive()) {
			switch (HiFieldPrimitive.getType(node2.type)) {
				case CHAR:
				case BYTE:
				case SHORT:
				case INT:
				case LONG:
					validIndex = true;
			}
		}
		boolean validArray = false;
		if (node1.type.isArray()) {
			type = ((HiClassArray) type).cellClass;
			validArray = true;
		}
		if (!validIndex || !validArray) {
			errorInvalidOperator(validationInfo, node1.node.getToken(), node1.type, node2.type);
		}
		return type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.valueType == Value.VARIABLE || v1.valueType == Value.ARRAY_INDEX || (v1.valueType == Value.VALUE && v1.type.isArray())) {
			// OK
		} else {
			errorUnexpectedType(ctx);
			return;
		}

		HiClassArray type = (HiClassArray) v1.type;
		Object array = v1.getArray();
		if (array == null) {
			ctx.throwRuntimeException("null pointer");
			return;
		}

		int index = v2.getInt();
		if (ctx.exitFromBlock()) {
			return;
		}

		int length = Array.getLength(array);
		if (index < 0 || index >= length) {
			errorArrayIndexOutOfBound(ctx, length, index);
			return;
		}

		HiArrays.getArrayIndex(v1, array, index);

		v1.valueType = Value.ARRAY_INDEX;
		v1.type = type.cellClass;
		v1.parentArray = array;
		v1.arrayIndex = index;
		v1.variable = null; // for cases (new int[1])[0]
	}
}
