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
		HiClass type = node.clazz.getAutoboxedPrimitiveClass() == null ? node.clazz : node.clazz.getAutoboxedPrimitiveClass();
		if (type.isPrimitive() && type.getPrimitiveType() != BOOLEAN) {
			if (node.isCompileValue()) {
				switch (type.getPrimitiveType()) {
					case CHAR:
						node.valueClass = TYPE_INT;
						node.intValue = -node.charValue;
						break;
					case BYTE:
						node.valueClass = TYPE_INT;
						node.intValue = -node.byteValue;
						break;
					case SHORT:
						node.valueClass = TYPE_INT;
						node.intValue = -node.shortValue;
						break;
					case INT:
						node.valueClass = TYPE_INT;
						node.intValue = -node.intValue;
						break;
					case LONG:
						node.valueClass = TYPE_LONG;
						node.longValue = -node.longValue;
						break;
					case FLOAT:
						node.valueClass = TYPE_FLOAT;
						node.floatValue = -node.floatValue;
						break;
					case DOUBLE:
						node.valueClass = TYPE_DOUBLE;
						node.doubleValue = -node.doubleValue;
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
