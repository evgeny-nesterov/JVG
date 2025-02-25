package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
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
		HiClass type = node.clazz.getAutoboxedPrimitiveClass() == null ? node.clazz : node.clazz.getAutoboxedPrimitiveClass();
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		if (type.isPrimitive()) {
			if (node.isCompileValue()) {
				switch (type.getPrimitiveType()) {
					case CHAR:
						node.intValue = ~node.charValue;
						return node.valueClass = TYPE_INT;
					case BYTE:
						node.intValue = ~node.byteValue;
						return node.valueClass = TYPE_INT;
					case SHORT:
						node.intValue = ~node.shortValue;
						return node.valueClass = TYPE_INT;
					case INT:
						node.intValue = ~node.intValue;
						return node.valueClass = TYPE_INT; // switch to primitive from boxed type
					case LONG:
						node.longValue = ~node.longValue;
						return node.valueClass = TYPE_LONG; // switch to primitive from boxed type
				}
			} else {
				int t = type.getPrimitiveType();
				switch (t) {
					case VAR:
					case CHAR:
					case BYTE:
					case SHORT:
					case INT:
						return TYPE_INT;
					case LONG:
						return TYPE_LONG;
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
				v.intNumber = ~v.character;
				return;
			case BYTE:
				v.valueClass = TYPE_INT;
				v.intNumber = ~v.byteNumber;
				return;
			case SHORT:
				v.valueClass = TYPE_INT;
				v.intNumber = ~v.shortNumber;
				return;
			case INT:
				v.valueClass = TYPE_INT; // switch to primitive from boxed type
				v.intNumber = ~v.intNumber;
				return;
			case LONG:
				v.valueClass = TYPE_LONG; // switch to primitive from boxed type
				v.longNumber = ~v.longNumber;
				return;
		}
	}
}
