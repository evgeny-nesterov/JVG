package script.ool.model.operations;

import java.lang.reflect.Array;

import script.ool.model.Arrays;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.classes.ClazzArray;

public class OperationArrayIndex extends BinaryOperation {
	private static Operation instance = new OperationArrayIndex();

	public static Operation getInstance() {
		return instance;
	}

	private OperationArrayIndex() {
		super("[]", ARRAY_INDEX);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.valueType == Value.VARIABLE || v1.valueType == Value.ARRAY_INDEX || (v1.valueType == Value.VALUE && v1.type.isArray())) {
			// OK
		} else {
			errorUnexpectedType(ctx);
			return;
		}

		ClazzArray type = (ClazzArray) v1.type;
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
