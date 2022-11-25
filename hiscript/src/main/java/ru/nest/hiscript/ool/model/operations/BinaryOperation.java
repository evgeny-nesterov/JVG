package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeIdentificator;

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

	public void errorInvalidOperator(RuntimeContext ctx, HiClass type1, HiClass type2) {
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

	public void errorCast(RuntimeContext ctx, HiClass typeFrom, HiClass typeTo) {
		String text = "can't cast " + typeFrom.getClassName() + " to " + typeTo.getClassName();
		ctx.throwException(text);
	}
}
