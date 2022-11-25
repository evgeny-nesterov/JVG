package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;

public class OperationPrefixMinus extends UnaryOperation {
	private static Operation instance = new OperationPrefixMinus();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPrefixMinus() {
		super("-", PREFIX_MINUS);
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
		if (t == BOOLEAN) {
			errorInvalidOperator(ctx, c);
			return;
		}

		switch (t) {
			case CHAR:
				v.type = TYPE_INT;
				v.intNumber = -v.character;
				break;

			case BYTE:
				v.type = TYPE_INT;
				v.intNumber = -v.byteNumber;
				break;

			case SHORT:
				v.type = TYPE_INT;
				v.intNumber = -v.shortNumber;
				break;

			case INT:
				v.intNumber = -v.intNumber;
				break;

			case LONG:
				v.longNumber = -v.longNumber;
				break;

			case FLOAT:
				v.floatNumber = -v.floatNumber;
				break;

			case DOUBLE:
				v.doubleNumber = -v.doubleNumber;
				break;
		}
	}
}
