package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;

public class OperationPrefixBitwiseReverse extends UnaryOperation {
	private static Operation instance = new OperationPrefixBitwiseReverse();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPrefixBitwiseReverse() {
		super("~", PREFIX_BITWISE_REVERSE);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		HiClass c = v.type;

		boolean isP = c.isPrimitive();
		if (!isP) {
			errorInvalidOperator(ctx, c);
			return;
		}

		int t = HiFieldPrimitive.getType(c);
		switch (t) {
			case CHAR:
				v.type = TYPE_INT;
				v.intNumber = ~v.character;
				return;

			case BYTE:
				v.type = TYPE_INT;
				v.intNumber = ~v.byteNumber;
				return;

			case SHORT:
				v.type = TYPE_INT;
				v.intNumber = ~v.shortNumber;
				return;

			case INT:
				v.intNumber = ~v.intNumber;
				return;

			case LONG:
				v.longNumber = ~v.longNumber;
				return;
		}

		errorInvalidOperator(ctx, c);
	}
}
