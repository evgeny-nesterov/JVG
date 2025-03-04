package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

public class OperationPrefixMinus extends UnaryOperation {
	private static final HiOperation instance = new OperationPrefixMinus();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPrefixMinus() {
		super(PREFIX_MINUS);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
		HiClass clazz = node.clazz.getAutoboxedPrimitiveClass() == null ? node.clazz : node.clazz.getAutoboxedPrimitiveClass();
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		if (clazz.isPrimitive() && clazz.getPrimitiveType() != BOOLEAN) {
			if (node.isCompileValue()) {
				switch (clazz.getPrimitiveType()) {
					case CHAR:
						node.intValue = -node.charValue;
						return node.valueClass = TYPE_INT;
					case BYTE:
						node.intValue = -node.byteValue;
						return node.valueClass = TYPE_INT;
					case SHORT:
						node.intValue = -node.shortValue;
						return node.valueClass = TYPE_INT;
					case INT:
						node.intValue = -node.intValue;
						return node.valueClass = TYPE_INT;
					case LONG:
						node.longValue = -node.longValue;
						return node.valueClass = TYPE_LONG;
					case FLOAT:
						node.floatValue = -node.floatValue;
						return node.valueClass = TYPE_FLOAT;
					case DOUBLE:
						node.doubleValue = -node.doubleValue;
						return node.valueClass = TYPE_DOUBLE;
				}
			} else {
				switch (clazz.getPrimitiveType()) {
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
		boolean isP = c.isPrimitive();
		switch (c.getPrimitiveType()) {
			case CHAR:
				v.valueClass = TYPE_INT;
				v.intNumber = -v.character;
				break;
			case BYTE:
				v.valueClass = TYPE_INT;
				v.intNumber = -v.byteNumber;
				break;
			case SHORT:
				v.valueClass = TYPE_INT;
				v.intNumber = -v.shortNumber;
				break;
			case INT:
				v.valueClass = TYPE_INT; // switch to primitive from boxed type
				v.intNumber = -v.intNumber;
				break;
			case LONG:
				v.valueClass = TYPE_LONG; // switch to primitive from boxed type
				v.longNumber = -v.longNumber;
				break;
			case FLOAT:
				v.valueClass = TYPE_FLOAT; // switch to primitive from boxed type
				v.floatNumber = -v.floatNumber;
				break;
			case DOUBLE:
				v.valueClass = TYPE_DOUBLE; // switch to primitive from boxed type
				v.doubleNumber = -v.doubleNumber;
				break;
		}
	}
}
