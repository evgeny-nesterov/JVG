package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

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
			int t = type.getPrimitiveType();
			switch (t) {
				case VAR:
				case CHAR:
				case BYTE:
				case SHORT:
				case INT:
				case LONG:
					return t == LONG ? type : HiClassPrimitive.INT;
			}
		}
		validationInfo.error("operation '" + name + "' cannot be applied to '" + node.clazz.getNameDescr() + "'", node.node.getToken());
		return node.clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		HiClass c = v.getOperationClass();

		boolean isP = c.isPrimitive();
		if (!isP) {
			errorInvalidOperator(ctx, c);
			return;
		}

		int t = c.getPrimitiveType();
		switch (t) {
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

		errorInvalidOperator(ctx, c);
	}
}
