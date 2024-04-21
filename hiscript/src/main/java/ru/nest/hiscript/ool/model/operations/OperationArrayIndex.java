package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassVar;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.lang.reflect.Array;

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
		boolean validIndex = false;
		if (node2.clazz.isPrimitive()) {
			switch (node2.clazz.getPrimitiveType()) {
				case VAR:
				case CHAR:
				case BYTE:
				case SHORT:
				case INT:
				case LONG:
					validIndex = true;
			}
		}
		boolean validArray = false;
		if (node1.clazz.isVar()) {
			clazz = HiClassVar.VAR;
			validArray = true;
		} else if (node1.clazz.isArray()) {
			clazz = ((HiClassArray) clazz).cellClass;
			validArray = true;
		}
		if (!validIndex || !validArray) {
			errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
		}
		if (validIndex && node2.isCompileValue() && node2.getIntValue() < 0) {
			validationInfo.error("negative array index", node2.token);
		}
		ctx.nodeValueType.resolvedValueVariable = node1.resolvedValueVariable;
		ctx.nodeValueType.enclosingClass = clazz;
		ctx.nodeValueType.enclosingType = Type.getType(clazz);
		ctx.nodeValueType.returnType = clazz.isPrimitive() ? NodeValueType.NodeValueReturnType.compileValue : NodeValueType.NodeValueReturnType.runtimeValue;
		return clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.valueType == Value.VARIABLE || v1.valueType == Value.ARRAY_INDEX || (v1.valueType == Value.VALUE && v1.valueClass.isArray())) {
			// OK
		} else {
			errorUnexpectedType(ctx);
			return;
		}

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

		HiArrays.getArrayIndex(v1, array, index);

		v1.valueType = Value.ARRAY_INDEX;
		v1.valueClass = type.cellClass;
		v1.parentArray = array;
		v1.arrayIndex = index;
		v1.variable = null; // for cases (new int[1])[0]
	}
}
