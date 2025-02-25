package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

public class OperationLogicalOrCheck extends UnaryOperation {
	private static final HiOperation instance = new OperationLogicalOrCheck();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationLogicalOrCheck() {
		super(LOGICAL_OR_CHECK);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
		return node.clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
	}
}
