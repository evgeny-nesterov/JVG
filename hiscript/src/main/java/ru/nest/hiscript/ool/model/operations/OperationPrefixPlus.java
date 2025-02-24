package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

public class OperationPrefixPlus extends UnaryOperation {
	private static final HiOperation instance = new OperationPrefixPlus();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPrefixPlus() {
		super(PREFIX_PLUS);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
		HiClass type = node.clazz.getAutoboxedPrimitiveClass() == null ? node.clazz : node.clazz.getAutoboxedPrimitiveClass();
		if (type.isPrimitive() && type.getPrimitiveType() != BOOLEAN) {
			if (node.isCompileValue()) {
				switch (type.getPrimitiveType()) {
					case CHAR:
						node.valueClass = TYPE_INT;
						node.intValue = node.charValue;
						break;
					case BYTE:
						node.valueClass = TYPE_INT;
						node.intValue = node.byteValue;
						break;
					case SHORT:
						node.valueClass = TYPE_INT;
						node.intValue = node.shortValue;
						break;
				}
			}
		} else {
			validationInfo.error("operation '" + name + "' cannot be applied to '" + node.clazz.getNameDescr() + "'", node.node.getToken());
		}
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		return node.clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		HiClass c = v.getOperationClass();
		switch (c.getPrimitiveType()) {
			case CHAR:
				v.valueClass = TYPE_INT;
				v.intNumber = v.character;
				break;
			case BYTE:
				v.valueClass = TYPE_INT;
				v.intNumber = v.byteNumber;
				break;
			case SHORT:
				v.valueClass = TYPE_INT;
				v.intNumber = v.shortNumber;
				break;
		}
	}
}
