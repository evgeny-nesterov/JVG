package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.nodes.NodeIdentificator;

public abstract class BinaryOperation extends Operation {
	BinaryOperation(String name, int operation) {
		super(name, 2, operation);
	}

	@Override
	public final void doOperation(RuntimeContext ctx, Value... values) {
		Value v1 = values[0];
		Value v2 = values[1];

		if (v1.valueType == Value.NAME) {
			boolean checkInitialization = operation != EQUATE;
			if (!NodeIdentificator.resolve(ctx, v1, checkInitialization)) {
				ctx.throwException("can't resolve identifier " + v1.name);
				return;
			}

			if (ctx.exitFromBlock()) {
				return;
			}
		}

		if (operation != INVOCATION) {
			if (v2.valueType == Value.NAME) {
				if (!NodeIdentificator.resolve(ctx, v2, true)) {
					ctx.throwException("can't resolve identifier " + v2.name);
					return;
				}

				if (ctx.exitFromBlock()) {
					return;
				}
			}
		}

		// TODO: check on null
		// if((v1.type.isNull()) || (v2.type.isNull() && operation != EQUATE))
		// {
		// errorInvalidOperator(ctx, v2.type, v1.type);
		// }

		doOperation(ctx, v1, v2);
	}

	public abstract void doOperation(RuntimeContext ctx, Value v1, Value v2);

	public void errorInvalidOperator(RuntimeContext ctx, Clazz type1, Clazz type2) {
		String text = "operator '" + name + "' can not be applyed to " + type1.fullName + ", " + type2.fullName;
		ctx.throwException(text);
	}

	public void errorUnexpectedType(RuntimeContext ctx) {
		String text = "unexpected type";
		ctx.throwException(text);
	}

	public void errorDevideByZero(RuntimeContext ctx) {
		String text = "devide by zero";
		ctx.throwException(text);
	}

	public void errorArrayRequired(RuntimeContext ctx) {
		String text = "array required";
		ctx.throwException(text);
	}

	public void errorArrayIndexOutOfBound(RuntimeContext ctx, int arrayLenth, int index) {
		String text = "array index out of bound: array length = " + arrayLenth + ", index = " + index;
		ctx.throwException(text);
	}

	public void errorCast(RuntimeContext ctx, Clazz typeFrom, Clazz typeTo) {
		String text = "can't cast " + typeFrom.getClassName() + " to " + typeTo.getClassName();
		ctx.throwException(text);
	}
}
