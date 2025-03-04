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

import static ru.nest.hiscript.ool.model.OperationType.*;
import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class OperationBitwiseShiftRightCyclic extends BinaryOperation {
	private static final HiOperation instance = new OperationBitwiseShiftRightCyclic();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationBitwiseShiftRightCyclic() {
		super(BITWISE_SHIFT_RIGHT_CYCLIC);
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.clazz.getAutoboxedPrimitiveClass() == null ? node1.clazz : node1.clazz.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.clazz.getAutoboxedPrimitiveClass() == null ? node2.clazz : node2.clazz.getAutoboxedPrimitiveClass();
		if (c1.isPrimitive() && c2.isPrimitive()) {
			PrimitiveType t1 = c1.getPrimitiveType();
			PrimitiveType t2 = c2.getPrimitiveType();
			if (node1.isCompileValue() && node2.isCompileValue()) {
				switch (t1) {
					case CHAR_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.intValue = node1.charValue >>> node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE_TYPE:
								node1.intValue = node1.charValue >>> node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT_TYPE:
								node1.intValue = node1.charValue >>> node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT_TYPE:
								node1.intValue = node1.charValue >>> node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG_TYPE:
								node1.longValue = node1.charValue >>> node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
						}
					case BYTE_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.intValue = node1.byteValue >>> node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE_TYPE:
								node1.intValue = node1.byteValue >>> node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT_TYPE:
								node1.intValue = node1.byteValue >>> node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT_TYPE:
								node1.intValue = node1.byteValue >>> node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG_TYPE:
								node1.longValue = node1.byteValue >>> node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
						}
					case SHORT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.intValue = node1.shortValue >>> node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE_TYPE:
								node1.intValue = node1.shortValue >>> node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT_TYPE:
								node1.intValue = node1.shortValue >>> node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT_TYPE:
								node1.intValue = node1.shortValue >>> node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG_TYPE:
								node1.longValue = node1.shortValue >>> node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
						}
					case INT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.intValue = node1.intValue >>> node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE_TYPE:
								node1.intValue = node1.intValue >>> node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT_TYPE:
								node1.intValue = node1.intValue >>> node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT_TYPE:
								node1.intValue = node1.intValue >>> node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG_TYPE:
								node1.longValue = node1.intValue >>> node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
						}
					case LONG_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.longValue = node1.longValue >>> node2.charValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case BYTE_TYPE:
								node1.longValue = node1.longValue >>> node2.byteValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case SHORT_TYPE:
								node1.longValue = node1.longValue >>> node2.shortValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case INT_TYPE:
								node1.longValue = node1.longValue >>> node2.intValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case LONG_TYPE:
								node1.longValue = node1.longValue >>> node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
						}
				}
			} else {
				switch (t1) {
					case VAR_TYPE:
					case CHAR_TYPE:
					case BYTE_TYPE:
					case SHORT_TYPE:
					case INT_TYPE:
					case LONG_TYPE:
						switch (t2) {
							case VAR_TYPE:
							case CHAR_TYPE:
							case BYTE_TYPE:
							case SHORT_TYPE:
							case INT_TYPE:
							case LONG_TYPE:
								return t1 == LONG_TYPE ? c1 : HiClassPrimitive.INT;
						}
				}
			}
		}
		errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
		return c1;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();
		PrimitiveType t1 = c1.getPrimitiveType();
		PrimitiveType t2 = c2.getPrimitiveType();
		switch (t1) {
			case CHAR_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.character >>> v2.character;
						return;
					case BYTE_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.character >>> v2.byteNumber;
						return;
					case SHORT_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.character >>> v2.shortNumber;
						return;
					case INT_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.character >>> v2.intNumber;
						return;
					case LONG_TYPE:
						v1.valueClass = HiClassPrimitive.LONG;
						v1.longNumber = v1.character >>> v2.longNumber;
						return;
				}
			case BYTE_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.intNumber = v1.byteNumber >>> v2.character;
						v1.valueClass = HiClassPrimitive.INT;
						return;
					case BYTE_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.byteNumber >>> v2.byteNumber;
						return;
					case SHORT_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.byteNumber >>> v2.shortNumber;
						return;
					case INT_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.byteNumber >>> v2.intNumber;
						return;
					case LONG_TYPE:
						v1.valueClass = HiClassPrimitive.LONG;
						v1.longNumber = v1.byteNumber >>> v2.longNumber;
						return;
				}
			case SHORT_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.shortNumber >>> v2.character;
						return;
					case BYTE_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.shortNumber >>> v2.byteNumber;
						return;
					case SHORT_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.shortNumber >>> v2.shortNumber;
						return;
					case INT_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.shortNumber >>> v2.intNumber;
						return;
					case LONG_TYPE:
						v1.valueClass = HiClassPrimitive.LONG;
						v1.longNumber = v1.shortNumber >>> v2.longNumber;
						return;
				}
			case INT_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.intNumber >>> v2.character;
						return;
					case BYTE_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.intNumber >>> v2.byteNumber;
						return;
					case SHORT_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.intNumber >>> v2.shortNumber;
						return;
					case INT_TYPE:
						v1.valueClass = HiClassPrimitive.INT;
						v1.intNumber = v1.intNumber >>> v2.intNumber;
						return;
					case LONG_TYPE:
						v1.valueClass = HiClassPrimitive.LONG;
						v1.longNumber = v1.intNumber >>> v2.longNumber;
						return;
				}
			case LONG_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.valueClass = HiClassPrimitive.LONG;
						v1.longNumber = v1.longNumber >>> v2.character;
						return;
					case BYTE_TYPE:
						v1.valueClass = HiClassPrimitive.LONG;
						v1.longNumber = v1.longNumber >>> v2.byteNumber;
						return;
					case SHORT_TYPE:
						v1.valueClass = HiClassPrimitive.LONG;
						v1.longNumber = v1.longNumber >>> v2.shortNumber;
						return;
					case INT_TYPE:
						v1.valueClass = HiClassPrimitive.LONG;
						v1.longNumber = v1.longNumber >>> v2.intNumber;
						return;
					case LONG_TYPE:
						v1.valueClass = HiClassPrimitive.LONG;
						v1.longNumber = v1.longNumber >>> v2.longNumber;
						return;
				}
		}
	}
}
