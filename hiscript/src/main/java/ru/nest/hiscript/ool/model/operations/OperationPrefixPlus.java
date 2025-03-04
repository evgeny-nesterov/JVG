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
		HiClass clazz = node.clazz.getAutoboxedPrimitiveClass() == null ? node.clazz : node.clazz.getAutoboxedPrimitiveClass();
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		if (clazz.isPrimitive() && clazz.getPrimitiveType() != BOOLEAN_TYPE) {
			if (node.isCompileValue()) {
				switch (clazz.getPrimitiveType()) {
					case CHAR_TYPE:
						node.intValue = node.charValue;
						return node.valueClass = HiClassPrimitive.INT;
					case BYTE_TYPE:
						node.intValue = node.byteValue;
						return node.valueClass = HiClassPrimitive.INT;
					case SHORT_TYPE:
						node.intValue = node.shortValue;
						return node.valueClass = HiClassPrimitive.INT;
					case INT_TYPE:
						return node.valueClass = HiClassPrimitive.INT;
					case LONG_TYPE:
						return node.valueClass = HiClassPrimitive.LONG;
					case FLOAT_TYPE:
						return node.valueClass = HiClassPrimitive.FLOAT;
					case DOUBLE_TYPE:
						return node.valueClass = HiClassPrimitive.DOUBLE;
				}
			} else {
				switch (clazz.getPrimitiveType()) {
					case CHAR_TYPE:
					case BYTE_TYPE:
					case SHORT_TYPE:
					case INT_TYPE:
						return HiClassPrimitive.INT;
					case LONG_TYPE:
						return HiClassPrimitive.LONG;
					case FLOAT_TYPE:
						return HiClassPrimitive.FLOAT;
					case DOUBLE_TYPE:
						return HiClassPrimitive.DOUBLE;
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
			case CHAR_TYPE:
				v.valueClass = HiClassPrimitive.INT;
				v.intNumber = v.character;
				break;
			case BYTE_TYPE:
				v.valueClass = HiClassPrimitive.INT;
				v.intNumber = v.byteNumber;
				break;
			case SHORT_TYPE:
				v.valueClass = HiClassPrimitive.INT;
				v.intNumber = v.shortNumber;
				break;
		}
	}
}
