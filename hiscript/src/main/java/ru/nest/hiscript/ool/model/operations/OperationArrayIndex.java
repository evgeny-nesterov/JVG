package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import java.lang.reflect.Array;

import static ru.nest.hiscript.ool.model.OperationType.*;

public class OperationArrayIndex extends BinaryOperation {
	private static final HiOperation instance = new OperationArrayIndex();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationArrayIndex() {
		super(ARRAY_INDEX);
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass clazz = node1.clazz;
		Type type = node1.type;
		boolean validIndex = false;
		if (node2.clazz.isPrimitive()) {
			switch (node2.clazz.getPrimitiveType()) {
				case VAR_TYPE:
				case CHAR_TYPE:
				case BYTE_TYPE:
				case SHORT_TYPE:
				case INT_TYPE:
					validIndex = true;
			}
		}
		if (!validIndex) {
			errorCast(validationInfo, node2.token, node2.clazz, HiClassPrimitive.INT);
		}
		if (node1.clazz.isArray()) {
			clazz = ((HiClassArray) clazz).cellClass;
			type = type.cellType;
		} else {
			errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
		}
		if (validIndex && node2.isCompileValue() && node2.getIntValue() < 0) {
			validationInfo.warning("negative array index", node2);
		}
		ctx.nodeValueType.type = type;
		ctx.nodeValueType.resolvedValueVariable = node1.resolvedValueVariable;
		ctx.nodeValueType.enclosingClass = clazz;
		ctx.nodeValueType.enclosingType = Type.getType(clazz);
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.isConstant = false;
		return clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		// Expected v1.valueType == ValueType.VARIABLE || v1.valueType == ValueType.ARRAY_INDEX || (v1.valueType == ValueType.VALUE && v1.valueClass.isArray())
		HiClassArray type = (HiClassArray) v1.valueClass;
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

		HiArrays.getArrayCellValue(v1, array, index);

		v1.valueType = ValueType.ARRAY_INDEX;
		v1.valueClass = type.cellClass;
		v1.parentArray = array;
		v1.arrayIndex = index;
		v1.variable = null; // for cases (new int[1])[0]
	}
}
