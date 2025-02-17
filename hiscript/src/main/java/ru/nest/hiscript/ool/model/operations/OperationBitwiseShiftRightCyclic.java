package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

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
			int t1 = c1.getPrimitiveType();
			int t2 = c2.getPrimitiveType();
			switch (t1) {
				case VAR:
				case CHAR:
				case BYTE:
				case SHORT:
				case INT:
				case LONG:
					switch (t2) {
						case VAR:
						case CHAR:
						case BYTE:
						case SHORT:
						case INT:
						case LONG:
							return t1 == LONG ? c1 : HiClassPrimitive.INT;
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
		int t1 = c1.getPrimitiveType();
		int t2 = c2.getPrimitiveType();
		switch (t1) {
			case CHAR:
				switch (t2) {
					case CHAR:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.character >>> v2.character;
						return;
					case BYTE:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.character >>> v2.byteNumber;
						return;
					case SHORT:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.character >>> v2.shortNumber;
						return;
					case INT:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.character >>> v2.intNumber;
						return;
					case LONG:
						v1.valueClass = TYPE_LONG;
						v1.longNumber = v1.character >>> v2.longNumber;
						return;
				}
			case BYTE:
				switch (t2) {
					case CHAR:
						v1.intNumber = v1.byteNumber >>> v2.character;
						v1.valueClass = TYPE_INT;
						return;
					case BYTE:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.byteNumber >>> v2.byteNumber;
						return;
					case SHORT:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.byteNumber >>> v2.shortNumber;
						return;
					case INT:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.byteNumber >>> v2.intNumber;
						return;
					case LONG:
						v1.valueClass = TYPE_LONG;
						v1.longNumber = v1.byteNumber >>> v2.longNumber;
						return;
				}
			case SHORT:
				switch (t2) {
					case CHAR:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.shortNumber >>> v2.character;
						return;
					case BYTE:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.shortNumber >>> v2.byteNumber;
						return;
					case SHORT:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.shortNumber >>> v2.shortNumber;
						return;
					case INT:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.shortNumber >>> v2.intNumber;
						return;
					case LONG:
						v1.valueClass = TYPE_LONG;
						v1.longNumber = v1.shortNumber >>> v2.longNumber;
						return;
				}
			case INT:
				switch (t2) {
					case CHAR:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.intNumber >>> v2.character;
						return;
					case BYTE:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.intNumber >>> v2.byteNumber;
						return;
					case SHORT:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.intNumber >>> v2.shortNumber;
						return;
					case INT:
						v1.valueClass = TYPE_INT;
						v1.intNumber = v1.intNumber >>> v2.intNumber;
						return;
					case LONG:
						v1.valueClass = TYPE_LONG;
						v1.longNumber = v1.intNumber >>> v2.longNumber;
						return;
				}
			case LONG:
				switch (t2) {
					case CHAR:
						v1.valueClass = TYPE_LONG;
						v1.longNumber = v1.longNumber >>> v2.character;
						return;
					case BYTE:
						v1.valueClass = TYPE_LONG;
						v1.longNumber = v1.longNumber >>> v2.byteNumber;
						return;
					case SHORT:
						v1.valueClass = TYPE_LONG;
						v1.longNumber = v1.longNumber >>> v2.shortNumber;
						return;
					case INT:
						v1.valueClass = TYPE_LONG;
						v1.longNumber = v1.longNumber >>> v2.intNumber;
						return;
					case LONG:
						v1.valueClass = TYPE_LONG;
						v1.longNumber = v1.longNumber >>> v2.longNumber;
						return;
				}
		}
	}
}
