package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationEquate extends BinaryOperation {
	private static HiOperation instance = new OperationEquate();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationEquate() {
		super("=", EQUATE);
	}

	@Override
	public void getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType... nodes) {
		NodeExpressionNoLS.NodeOperandType node1 = nodes[0];
		if (node1.node instanceof NodeIdentifier) {
			NodeIdentifier identifierNode = (NodeIdentifier) node1.node;
			Object resolvedIdentifier = ctx.resolveIdentifier(identifierNode.getName());
			if (resolvedIdentifier instanceof HiNode) {
				ctx.initializedNodes.add((HiNode) resolvedIdentifier);
			}
		}
		super.getOperationResultType(validationInfo, ctx, nodes);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v2.valueType == Value.VARIABLE) {
			if (!v2.variable.initialized) {
				ctx.throwRuntimeException("variable not initialized: " + v2.variable.name);
				return;
			}
		}

		if (v1.valueType == Value.VARIABLE) {
			// 1. copy variable from v1
			HiField<?> variable = v1.variable;
			if (variable.initialized && variable.getModifiers().isFinal()) {
				ctx.throwException("RuntimeException", "Cannot assign a value to final variable '" + variable.name + "'");
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
			// System.out.println(v1.variable.name + " (" + v1.variable.getClazz(ctx)+ ") = " + v1.variable.get() + ", " + v1.get() + ", " +
			// v1.type);
		} else if (v1.valueType == Value.ARRAY_INDEX) {
			if (!HiClass.autoCast(v2.type, v1.type)) {
				ctx.throwRuntimeException("incompatible types; found " + v2.type + ", required " + v1.type);
				return;
			}
			HiArrays.setArrayIndex(v1.type, v1.parentArray, v1.arrayIndex, v2, v1);
		} else {
			errorUnexpectedType(ctx);
		}
	}
}
