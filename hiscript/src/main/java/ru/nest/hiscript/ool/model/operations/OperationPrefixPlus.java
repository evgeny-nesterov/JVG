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
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		if (type.isPrimitive() && type.getPrimitiveType() != BOOLEAN) {
			if (node.isCompileValue()) {
				switch (type.getPrimitiveType()) {
					case CHAR:
						node.intValue = node.charValue;
						return node.valueClass = TYPE_INT;
					case BYTE:
						node.intValue = node.byteValue;
						return node.valueClass = TYPE_INT;
					case SHORT:
						node.intValue = node.shortValue;
						return node.valueClass = TYPE_INT;
					case INT:
						return node.valueClass = TYPE_INT;
					case LONG:
						return node.valueClass = TYPE_LONG;
					case FLOAT:
						return node.valueClass = TYPE_FLOAT;
					case DOUBLE:
						return node.valueClass = TYPE_DOUBLE;
				}
			} else {
				switch (type.getPrimitiveType()) {
					case CHAR:
					case BYTE:
					case SHORT:
					case INT:
						return TYPE_INT;
					case LONG:
						return TYPE_LONG;
					case FLOAT:
						return TYPE_FLOAT;
					case DOUBLE:
						return TYPE_DOUBLE;
				}
			}
		}
		validationInfo.error("operation '" + name + "' cannot be applied to '" + node.clazz.getNameDescr() + "'", node.node);
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
