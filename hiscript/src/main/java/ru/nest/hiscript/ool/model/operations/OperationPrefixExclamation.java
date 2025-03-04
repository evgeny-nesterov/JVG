package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import static ru.nest.hiscript.ool.model.OperationType.*;
import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class OperationPrefixExclamation extends UnaryOperation {
	private static final HiOperation instance = new OperationPrefixExclamation();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPrefixExclamation() {
		super(PREFIX_EXCLAMATION);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
		HiClass clazz = node.clazz.getAutoboxedPrimitiveClass() == null ? node.clazz : node.clazz.getAutoboxedPrimitiveClass();
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		if (clazz.getPrimitiveType() == BOOLEAN_TYPE) {
			if (node.isCompileValue()) {
				node.booleanValue = !node.booleanValue;
			}
			return HiClassPrimitive.BOOLEAN;
		}
		validationInfo.error("operation '" + name + "' cannot be applied to '" + node.clazz.getNameDescr() + "'", node.node);
		return HiClassPrimitive.BOOLEAN;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		HiClass c = v.getOperationClass();
		v.bool = !v.bool;
	}
}
