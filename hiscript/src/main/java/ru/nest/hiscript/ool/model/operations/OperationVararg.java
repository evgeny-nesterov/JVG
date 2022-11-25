package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Arrays;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassArray;

import java.lang.reflect.Array;

public class OperationVararg extends BinaryOperation {
	private static Operation instance = new OperationVararg();

	public static Operation getInstance() {
		return instance;
	}

	private OperationVararg() {
		super("...", ARRAY_INDEX);
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
			ctx.throwException("null pointer");
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

		Arrays.getArrayIndex(v1, array, index);

		v1.valueType = Value.ARRAY_INDEX;
		v1.type = type.cellClass;
		v1.parentArray = array;
		v1.arrayIndex = index;
	}
}
