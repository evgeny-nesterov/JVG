package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

public abstract class BinaryOperation extends HiOperation {
	BinaryOperation(int operation) {
		super(2, operation);
	}

	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		return null;
	}

	/**
	 * node1 <operation> node2 => node1
	 */
	@Override
	public void getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType... nodes) {
		NodeValueType node1 = nodes[0];
		NodeValueType node2 = nodes[1];
		if (prepareOperationResultType(validationInfo, ctx, node1, node2)) {
			ctx.nodeValueType.returnType = null;
		} else {
			ctx.nodeValueType.invalid();
		}
		ctx.nodeValueType.resolvedValueVariable = null;
		ctx.nodeValueType.enclosingClass = null;
		ctx.nodeValueType.enclosingType = null;

		HiClass clazz = getOperationResultClass(validationInfo, ctx, node1, node2);
		NodeValueType.NodeValueReturnType returnType = null;
		Type type = ctx.nodeValueType.type;
		if (clazz != null) {
			returnType = ctx.nodeValueType.returnType != null ? ctx.nodeValueType.returnType : node1.returnType;
			if (returnType == null) {
				returnType = NodeValueType.NodeValueReturnType.runtimeValue;
			}
		}
		node1.get(node1.node, clazz, type, clazz != null, returnType, clazz != null && node1.isConstant, ctx.nodeValueType.resolvedValueVariable, ctx.nodeValueType.enclosingClass, ctx.nodeValueType.enclosingType);
		node1.apply(node2);
	}

	protected boolean prepareOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		boolean valid = true;
		if (node1.clazz == null) {
			node1.get(validationInfo, ctx);
			if (node1.clazz != null) {
				valid = node1.valid;
			} else {
				validationInfo.error("cannot resolve expression type", node1.node.getToken());
				valid = false;
			}
		}
		if (node2.clazz == null && this != OperationInvocation.getInstance()) {
			node2.get(validationInfo, ctx);
			if (node2.clazz != null) {
				valid &= node2.valid;
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
				NodeIdentifier.resolve(ctx, v1, checkInitialization);
				ctx.throwRuntimeException("cannot resolve identifier " + v1.name);
				return;
			}

			if (ctx.exitFromBlock()) {
				return;
			}
		}

		if (operation != INVOCATION) {
			if (v2.valueType == Value.NAME) {
				if (!NodeIdentifier.resolve(ctx, v2, true)) {
					ctx.throwRuntimeException("cannot resolve identifier " + v2.name);
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
			v1.valueClass = TYPE_BYTE;
			v1.byteNumber = (byte) value;
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			v1.valueClass = TYPE_SHORT;
			v1.shortNumber = (short) value;
		} else {
			v1.valueClass = TYPE_INT;
			v1.intNumber = value;
		}
	}

	protected HiClassPrimitive autoCastInt(int value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			return TYPE_BYTE;
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			return TYPE_SHORT;
		} else {
			return TYPE_INT;
		}
	}

	protected void autoCastLong(Value v1, long value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			v1.valueClass = TYPE_BYTE;
			v1.byteNumber = (byte) value;
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			v1.valueClass = TYPE_SHORT;
			v1.shortNumber = (short) value;
		} else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
			v1.valueClass = TYPE_INT;
			v1.intNumber = (int) value;
		} else {
			v1.valueClass = TYPE_LONG;
			v1.longNumber = value;
		}
	}

	protected HiClassPrimitive autoCastLong(long value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			return TYPE_BYTE;
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			return TYPE_SHORT;
		} else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
			return TYPE_INT;
		} else {
			return TYPE_LONG;
		}
	}

	public void errorInvalidOperator(RuntimeContext ctx, HiClass type1, HiClass type2) {
		String text = "operator '" + name + "' can not be applied to " + type1.getNameDescr() + ", " + type2.getNameDescr();
		ctx.throwRuntimeException(text);
	}

	public void errorInvalidOperator(ValidationInfo validationInfo, Token token, HiClass type1, HiClass type2) {
		String text = "operator '" + name + "' can not be applied to " + type1.getNameDescr() + ", " + type2.getNameDescr();
		validationInfo.error(text, token);
	}

	public void errorInvalidOperator(ValidationInfo validationInfo, Token token, HiClass type) {
		String text = "operator '" + name + "' can not be applied to " + type.getNameDescr();
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
		String text = "cannot cast " + typeFrom.getClassName() + " to " + typeTo.getClassName();
		ctx.throwRuntimeException(text);
	}

	public void errorCast(ValidationInfo validationInfo, Token token, HiClass typeFrom, HiClass typeTo) {
		String text = "cannot cast " + typeFrom.getClassName() + " to " + typeTo.getClassName();
		validationInfo.error(text, token);
	}
}
