package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.*;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationEquate extends BinaryOperation {
	private static final HiOperation instance = new OperationEquate();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationEquate() {
		super(EQUATE);
	}

	@Override
	public boolean isStatement() {
		return true;
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
        if (!node1.isVariable()) {
            validationInfo.error("variable expected", node1.token);
        }
		return node1.clazz;
	}

	@Override
	public void getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType... nodes) {
		NodeValueType node1 = nodes[0];
		HiNodeIF node = node1.node != null ? node1.node : node1.resolvedValueVariable;
		checkFinal(validationInfo, ctx, node, true);
		super.getOperationResultType(validationInfo, ctx, nodes);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v2.valueType == Value.VARIABLE && !v2.variable.isInitialized(ctx)) {
			ctx.throwRuntimeException("variable not initialized: " + v2.variable.name);
			return;
		}

		if (v1.valueType == Value.VARIABLE) {
			// 1. copy variable from v1
			HiField<?> variable = v1.variable;
			if (variable.initialized && variable.getModifiers().isFinal()) {
				ctx.throwRuntimeException("cannot assign a value to final variable '" + variable.name + "'");
				return;
			}

			// 2. copy v2 to v1
			v2.copyTo(v1);

			// 3. set v1 variable again
			v1.valueType = Value.VARIABLE;
			v1.variable = variable;

			// 4. set value of variable from v2
			variable.set(ctx, v2);
			variable.initialized = true;

			// DEBUG
			// System.out.println(v1.variable.name + " (" + v1.variable.getClazz(ctx)+ ") = " + v1.variable.get() + ", " + v1.get() + ", " + v1.type);
		} else if (v1.valueType == Value.ARRAY_INDEX) {
			if (!HiClass.autoCast(ctx, v2.valueClass, v1.valueClass, v2.valueType == Value.VALUE, true)) {
				ctx.throwRuntimeException("incompatible types; found " + v2.valueClass + ", required " + v1.valueClass);
				return;
			}
			HiArrays.setArrayIndex(v1.valueClass, v1.parentArray, v1.arrayIndex, v2, v1);
		} else {
			errorUnexpectedType(ctx);
		}
	}
}
