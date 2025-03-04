package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

public class OperationPrefixBitwiseReverse extends UnaryOperation {
	private static final HiOperation instance = new OperationPrefixBitwiseReverse();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPrefixBitwiseReverse() {
		super(PREFIX_BITWISE_REVERSE);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
		HiClass clazz = node.clazz.getAutoboxedPrimitiveClass() == null ? node.clazz : node.clazz.getAutoboxedPrimitiveClass();
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		if (clazz.isPrimitive()) {
			if (node.isCompileValue()) {
				switch (clazz.getPrimitiveType()) {
					case CHAR_TYPE:
						node.intValue = ~node.charValue;
						return node.valueClass = HiClassPrimitive.INT;
					case BYTE_TYPE:
						node.intValue = ~node.byteValue;
						return node.valueClass = HiClassPrimitive.INT;
					case SHORT_TYPE:
						node.intValue = ~node.shortValue;
						return node.valueClass = HiClassPrimitive.INT;
					case INT_TYPE:
						node.intValue = ~node.intValue;
						return node.valueClass = HiClassPrimitive.INT; // switch to primitive from boxed type
					case LONG_TYPE:
						node.longValue = ~node.longValue;
						return node.valueClass = HiClassPrimitive.LONG; // switch to primitive from boxed type
				}
			} else {
				PrimitiveType t = clazz.getPrimitiveType();
				switch (t) {
					case VAR_TYPE:
					case CHAR_TYPE:
					case BYTE_TYPE:
					case SHORT_TYPE:
					case INT_TYPE:
						return HiClassPrimitive.INT;
					case LONG_TYPE:
						return HiClassPrimitive.LONG;
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
				v.intNumber = ~v.character;
				return;
			case BYTE_TYPE:
				v.valueClass = HiClassPrimitive.INT;
				v.intNumber = ~v.byteNumber;
				return;
			case SHORT_TYPE:
				v.valueClass = HiClassPrimitive.INT;
				v.intNumber = ~v.shortNumber;
				return;
			case INT_TYPE:
				v.valueClass = HiClassPrimitive.INT; // switch to primitive from boxed type
				v.intNumber = ~v.intNumber;
				return;
			case LONG_TYPE:
				v.valueClass = HiClassPrimitive.LONG; // switch to primitive from boxed type
				v.longNumber = ~v.longNumber;
				return;
		}
	}
}
