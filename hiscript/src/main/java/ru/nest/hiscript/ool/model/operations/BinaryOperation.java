package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.OperationType;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;
import ru.nest.hiscript.tokenizer.Token;

import static ru.nest.hiscript.ool.model.OperationType.*;

public abstract class BinaryOperation extends HiOperation {
	BinaryOperation(OperationType operation) {
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
		ctx.nodeValueType.valueClass = null;

		HiClass clazz = getOperationResultClass(validationInfo, ctx, node1, node2);
		NodeValueType.NodeValueReturnType returnType = null;
		Type type = ctx.nodeValueType.type;
		if (clazz != null) {
			returnType = ctx.nodeValueType.returnType != null ? ctx.nodeValueType.returnType : node1.returnType;
			if (returnType == null) {
				returnType = NodeValueType.NodeValueReturnType.runtimeValue;
			}
		}
		node1.get(node1.node, clazz, type, clazz != null, returnType, node1.valueClass, clazz != null && node1.isConstant, ctx.nodeValueType.resolvedValueVariable, ctx.nodeValueType.enclosingClass, ctx.nodeValueType.enclosingType);
		node1.apply(node2);
	}

	protected boolean prepareOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		boolean valid = true;
		if (node1.clazz == null) {
			node1.get(validationInfo, ctx);
			if (node1.clazz != null) {
				valid = node1.valid;
			} else {
				// TODO delete?
				validationInfo.error("cannot resolve expression type", node1.node);
				valid = false;
			}
		}
		if (node2.clazz == null && this != OperationInvocation.getInstance()) {
			node2.get(validationInfo, ctx);
			if (node2.clazz != null) {
				valid &= node2.valid;
			} else {
				// TODO delete?
				validationInfo.error("cannot resolve expression type", node2.node);
				valid = false;
			}
		}
		return valid;
	}

	@Override
	public final void doOperation(RuntimeContext ctx, Value... values) {
		Value v1 = values[0];
		Value v2 = values[1];
		if (v1.valueType == ValueType.NAME) {
			NodeIdentifier.resolve(ctx, v1);
		}
		if (operation != INVOCATION && v2.valueType == ValueType.NAME) {
			NodeIdentifier.resolve(ctx, v2);
		}
		doOperation(ctx, v1, v2);
	}

	public abstract void doOperation(RuntimeContext ctx, Value v1, Value v2);

	protected void autoCastInt(Value v1, int value) {
		v1.valueClass = HiClassPrimitive.INT;
		v1.intNumber = value;
	}

	protected void autoCastLong(Value v1, long value) {
		v1.valueClass = HiClassPrimitive.LONG;
		v1.longNumber = value;
	}

	public void errorInvalidOperator(ValidationInfo validationInfo, Token token, HiClass class1, HiClass class2) {
		String text = "operator '" + name + "' can not be applied to " + class1.getNameDescr() + ", " + class2.getNameDescr();
		validationInfo.error(text, token);
	}

	public void errorDivideByZero(RuntimeContext ctx) {
		String text = "divide by zero";
		ctx.throwRuntimeException(text);
	}

	public void errorArrayIndexOutOfBound(RuntimeContext ctx, int arrayLength, int index) {
		String text = "array index out of bound: array length = " + arrayLength + ", index = " + index;
		ctx.throwRuntimeException(text);
	}

	public void errorCast(ValidationInfo validationInfo, Token token, HiClass classFrom, HiClass classTo) {
		String text = "cannot cast " + classFrom.getNameDescr() + " to " + classTo.getNameDescr();
		validationInfo.error(text, token);
	}

	public void errorInconvertible(ValidationInfo validationInfo, Token token, HiClass classFrom, HiClass classTo) {
		String text = "inconvertible types; cannot cast '" + classFrom.getNameDescr() + "' to '" + classTo.getNameDescr() + "'";
		validationInfo.error(text, token);
	}

	public void errorCast(RuntimeContext ctx, HiClass classFrom, HiClass classTo) {
		String text = "cannot cast " + classFrom.getNameDescr() + " to " + classTo.getNameDescr();
		ctx.throwRuntimeException(text);
	}
}