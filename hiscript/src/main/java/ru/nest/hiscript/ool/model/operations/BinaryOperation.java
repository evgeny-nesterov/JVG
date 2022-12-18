package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

public abstract class BinaryOperation extends Operation {
	BinaryOperation(String name, int operation) {
		super(name, 2, operation);
	}

	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node1, NodeExpressionNoLS.NodeOperandType node2) {
		return null;
	}

	@Override
	public void getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType... nodes) {
		NodeExpressionNoLS.NodeOperandType node1 = nodes[0];
		NodeExpressionNoLS.NodeOperandType node2 = nodes[1];
		if (prepareOperationResultType(validationInfo, ctx, node1, node2)) {
			node1.type = getOperationResultType(validationInfo, ctx, node1, node2);
			node1.isValue = node1.isValue && node2.isValue;
		}
	}

	protected boolean prepareOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node1, NodeExpressionNoLS.NodeOperandType node2) {
		boolean valid = true;
		if (node1.type == null) {
			node1.type = node1.getType(validationInfo, ctx);
			if (node1.type != null) {
				node1.isValue = node1.node.isValue();
			} else {
				validationInfo.error("cannot resolve expression type", node1.node.getToken());
				valid = false;
			}
		}
		if (node2.type == null && this != OperationInvocation.getInstance()) {
			node2.type = node2.getType(validationInfo, ctx);
			if (node2.type != null) {
				node2.isValue = node2.node.isValue();
			} else {
				validationInfo.error("cannot resolve expression type", node2.node.getToken());
				valid = false;
			}
		}
		return valid;
	}

	@Override
	public final void doOperation(RuntimeContext ctx, Value... values) {
		Value v1 = values[0];
		Value v2 = values[1];

		if (v1.valueType == Value.NAME) {
			boolean checkInitialization = operation != EQUATE;
			if (!NodeIdentifier.resolve(ctx, v1, checkInitialization)) {
				ctx.throwRuntimeException("can't resolve identifier " + v1.name);
				return;
			}

			if (ctx.exitFromBlock()) {
				return;
			}
		}

		if (operation != INVOCATION) {
			if (v2.valueType == Value.NAME) {
				if (!NodeIdentifier.resolve(ctx, v2, true)) {
					ctx.throwRuntimeException("can't resolve identifier " + v2.name);
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

	protected void autoCastInt(Value v1, int value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			v1.type = TYPE_BYTE;
			v1.byteNumber = (byte) value;
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			v1.type = TYPE_SHORT;
			v1.shortNumber = (short) value;
		} else {
			v1.type = TYPE_INT;
			v1.intNumber = value;
		}
	}

	protected HiClassPrimitive autoCastInt(int value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			return HiClassPrimitive.BYTE;
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			return HiClassPrimitive.SHORT;
		} else {
			return HiClassPrimitive.INT;
		}
	}

	protected void autoCastLong(Value v1, long value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			v1.type = TYPE_BYTE;
			v1.byteNumber = (byte) value;
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			v1.type = TYPE_SHORT;
			v1.shortNumber = (short) value;
		} else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
			v1.type = TYPE_INT;
			v1.intNumber = (int) value;
		} else {
			v1.type = TYPE_LONG;
			v1.longNumber = value;
		}
	}

	protected HiClassPrimitive autoCastLong(long value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			return HiClassPrimitive.BYTE;
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			return HiClassPrimitive.SHORT;
		} else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
			return HiClassPrimitive.INT;
		} else {
			return HiClassPrimitive.LONG;
		}
	}

	public void errorInvalidOperator(RuntimeContext ctx, HiClass type1, HiClass type2) {
		String text = "operator '" + name + "' can not be applied to " + type1.fullName + ", " + type2.fullName;
		ctx.throwRuntimeException(text);
	}

	public void errorInvalidOperator(ValidationInfo validationInfo, Token token, HiClass type1, HiClass type2) {
		String text = "operator '" + name + "' can not be applied to " + type1.fullName + ", " + type2.fullName;
		validationInfo.error(text, token);
	}

	public void errorInvalidOperator(ValidationInfo validationInfo, Token token, HiClass type) {
		String text = "operator '" + name + "' can not be applied to " + type.fullName;
		validationInfo.error(text, token);
	}

	public void errorUnexpectedType(RuntimeContext ctx) {
		String text = "unexpected type";
		ctx.throwRuntimeException(text);
	}

	public void errorDivideByZero(RuntimeContext ctx) {
		String text = "divide by zero";
		ctx.throwRuntimeException(text);
	}

	public void errorArrayIndexOutOfBound(RuntimeContext ctx, int arrayLength, int index) {
		String text = "array index out of bound: array length = " + arrayLength + ", index = " + index;
		ctx.throwRuntimeException(text);
	}

	public void errorCast(RuntimeContext ctx, HiClass typeFrom, HiClass typeTo) {
		String text = "can't cast " + typeFrom.getClassName() + " to " + typeTo.getClassName();
		ctx.throwRuntimeException(text);
	}

	public void errorCast(ValidationInfo validationInfo, Token token, HiClass typeFrom, HiClass typeTo) {
		String text = "can't cast " + typeFrom.getClassName() + " to " + typeTo.getClassName();
		validationInfo.error(text, token);
	}
}
