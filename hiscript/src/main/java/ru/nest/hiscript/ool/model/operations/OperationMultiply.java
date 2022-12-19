package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeByte;
import ru.nest.hiscript.ool.model.nodes.NodeChar;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeInt;
import ru.nest.hiscript.ool.model.nodes.NodeLong;
import ru.nest.hiscript.ool.model.nodes.NodeShort;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationMultiply extends BinaryOperation {
	private static HiOperation instance = new OperationMultiply();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationMultiply() {
		super("*", MULTIPLY);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node1, NodeExpressionNoLS.NodeOperandType node2) {
		HiClass c1 = node1.type;
		HiClass c2 = node2.type;
		if (!c1.isNumber() || !c2.isNumber()) {
			errorInvalidOperator(validationInfo, node1.node.getToken(), c1, c2);
			return null;
		}

		int t1 = HiFieldPrimitive.getType(c1);
		int t2 = HiFieldPrimitive.getType(c2);
		if (node1.isValue && node2.isValue) {
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							return autoCastInt(((NodeChar) node1.node).getValue() * ((NodeChar) node2.node).getValue());
						case BYTE:
							return autoCastInt(((NodeChar) node1.node).getValue() * ((NodeByte) node2.node).getValue());
						case SHORT:
							return autoCastInt(((NodeChar) node1.node).getValue() * ((NodeShort) node2.node).getValue());
						case INT:
							return autoCastInt(((NodeChar) node1.node).getValue() * ((NodeInt) node2.node).getValue());
						case LONG:
							return autoCastLong(((NodeChar) node1.node).getValue() * ((NodeLong) node2.node).getValue());
						case FLOAT:
							return HiClassPrimitive.FLOAT;
						case DOUBLE:
							return HiClassPrimitive.DOUBLE;
					}
					break;

				case BYTE:
					switch (t2) {
						case CHAR:
							return autoCastInt(((NodeByte) node1.node).getValue() * ((NodeChar) node2.node).getValue());
						case BYTE:
							return autoCastInt(((NodeByte) node1.node).getValue() * ((NodeByte) node2.node).getValue());
						case SHORT:
							return autoCastInt(((NodeByte) node1.node).getValue() * ((NodeShort) node2.node).getValue());
						case INT:
							return autoCastInt(((NodeByte) node1.node).getValue() * ((NodeInt) node2.node).getValue());
						case LONG:
							return autoCastLong(((NodeByte) node1.node).getValue() * ((NodeLong) node2.node).getValue());
						case FLOAT:
							return HiClassPrimitive.FLOAT;
						case DOUBLE:
							return HiClassPrimitive.DOUBLE;
					}
					break;

				case SHORT:
					switch (t2) {
						case CHAR:
							return autoCastInt(((NodeShort) node1.node).getValue() * ((NodeChar) node2.node).getValue());
						case BYTE:
							return autoCastInt(((NodeShort) node1.node).getValue() * ((NodeByte) node2.node).getValue());
						case SHORT:
							return autoCastInt(((NodeShort) node1.node).getValue() * ((NodeShort) node2.node).getValue());
						case INT:
							return autoCastInt(((NodeShort) node1.node).getValue() * ((NodeInt) node2.node).getValue());
						case LONG:
							return autoCastLong(((NodeShort) node1.node).getValue() * ((NodeLong) node2.node).getValue());
						case FLOAT:
							return HiClassPrimitive.FLOAT;
						case DOUBLE:
							return HiClassPrimitive.DOUBLE;
					}
					break;

				case INT:
					switch (t2) {
						case CHAR:
							return autoCastInt(((NodeInt) node1.node).getValue() * ((NodeChar) node2.node).getValue());
						case BYTE:
							return autoCastInt(((NodeInt) node1.node).getValue() * ((NodeByte) node2.node).getValue());
						case SHORT:
							return autoCastInt(((NodeInt) node1.node).getValue() * ((NodeShort) node2.node).getValue());
						case INT:
							return autoCastInt(((NodeInt) node1.node).getValue() * ((NodeInt) node2.node).getValue());
						case LONG:
							return autoCastLong(((NodeInt) node1.node).getValue() * ((NodeLong) node2.node).getValue());
						case FLOAT:
							return HiClassPrimitive.FLOAT;
						case DOUBLE:
							return HiClassPrimitive.DOUBLE;
					}
					break;

				case LONG:
					switch (t2) {
						case CHAR:
							return autoCastLong(((NodeLong) node1.node).getValue() * ((NodeChar) node2.node).getValue());
						case BYTE:
							return autoCastLong(((NodeLong) node1.node).getValue() * ((NodeByte) node2.node).getValue());
						case SHORT:
							return autoCastLong(((NodeLong) node1.node).getValue() * ((NodeShort) node2.node).getValue());
						case INT:
							return autoCastLong(((NodeLong) node1.node).getValue() * ((NodeInt) node2.node).getValue());
						case LONG:
							return autoCastLong(((NodeLong) node1.node).getValue() * ((NodeLong) node2.node).getValue());
						case FLOAT:
							return HiClassPrimitive.FLOAT;
						case DOUBLE:
							return HiClassPrimitive.DOUBLE;
					}
					break;

				case FLOAT:
					switch (t2) {
						default:
							return HiClassPrimitive.FLOAT;
						case DOUBLE:
							return HiClassPrimitive.DOUBLE;
					}

				case DOUBLE:
					return HiClassPrimitive.DOUBLE;
			}
		} else {
			switch (t1) {
				case CHAR:
				case BYTE:
				case SHORT:
				case INT:
					switch (t2) {
						case CHAR:
						case BYTE:
						case SHORT:
						case INT:
							return HiClassPrimitive.INT;
						case LONG:
							return HiClassPrimitive.LONG;
						case FLOAT:
							return HiClassPrimitive.FLOAT;
						case DOUBLE:
							return HiClassPrimitive.DOUBLE;
					}

				case LONG:
					switch (t2) {
						case CHAR:
						case BYTE:
						case SHORT:
						case INT:
						case LONG:
							return HiClassPrimitive.LONG;
						case FLOAT:
							return HiClassPrimitive.FLOAT;
						case DOUBLE:
							return HiClassPrimitive.DOUBLE;
					}
					break;

				case FLOAT:
					switch (t2) {
						case CHAR:
						case BYTE:
						case SHORT:
						case INT:
						case LONG:
						case FLOAT:
							return HiClassPrimitive.FLOAT;
						case DOUBLE:
							return HiClassPrimitive.DOUBLE;
					}
					break;

				case DOUBLE:
					return HiClassPrimitive.DOUBLE;
			}
		}

		errorInvalidOperator(validationInfo, node1.node.getToken(), c1, c2);
		return null;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.type;
		HiClass c2 = v2.type;
		if (v1.object != null && c1.isObject()) {
			c1 = v1.object.clazz;
		}
		if (v2.object != null && c2.isObject()) {
			c2 = v2.object.clazz;
		}

		if (!c1.isNumber() || !c2.isNumber()) {
			errorInvalidOperator(ctx, c1, c2);
			return;
		}

		int t1 = HiFieldPrimitive.getType(c1);
		int t2 = HiFieldPrimitive.getType(c2);
		if (v1.valueType == Value.VALUE && v2.valueType == Value.VALUE) {
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							autoCastInt(v1, v1.character * v2.character);
							return;

						case BYTE:
							autoCastInt(v1, v1.character * v2.byteNumber);
							return;

						case SHORT:
							autoCastInt(v1, v1.character * v2.shortNumber);
							return;

						case INT:
							autoCastInt(v1, v1.character * v2.intNumber);
							return;

						case LONG:
							autoCastLong(v1, v1.character * v2.longNumber);
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.character * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.character * v2.doubleNumber;
							return;
					}
					break;

				case BYTE:
					switch (t2) {
						case CHAR:
							autoCastInt(v1, v1.byteNumber * v2.character);
							return;

						case BYTE:
							autoCastInt(v1, v1.byteNumber * v2.byteNumber);
							return;

						case SHORT:
							autoCastInt(v1, v1.byteNumber * v2.shortNumber);
							return;

						case INT:
							v1.type = TYPE_INT;
							autoCastInt(v1, v1.byteNumber * v2.intNumber);
							return;

						case LONG:
							autoCastLong(v1, v1.byteNumber * v2.longNumber);
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.byteNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.byteNumber * v2.doubleNumber;
							return;
					}
					break;

				case SHORT:
					switch (t2) {
						case CHAR:
							autoCastInt(v1, v1.shortNumber * v2.character);
							return;

						case BYTE:
							autoCastInt(v1, v1.shortNumber * v2.byteNumber);
							return;

						case SHORT:
							autoCastInt(v1, v1.shortNumber * v2.shortNumber);
							return;

						case INT:
							autoCastInt(v1, v1.shortNumber * v2.intNumber);
							return;

						case LONG:
							autoCastLong(v1, v1.shortNumber * v2.longNumber);
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.shortNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.shortNumber * v2.doubleNumber;
							return;
					}
					break;

				case INT:
					switch (t2) {
						case CHAR:
							autoCastInt(v1, v1.intNumber * v2.character);
							return;

						case BYTE:
							autoCastInt(v1, v1.intNumber * v2.byteNumber);
							return;

						case SHORT:
							autoCastInt(v1, v1.intNumber * v2.shortNumber);
							return;

						case INT:
							autoCastInt(v1, v1.intNumber * v2.intNumber);
							return;

						case LONG:
							autoCastLong(v1, v1.intNumber * v2.longNumber);
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.intNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.intNumber * v2.doubleNumber;
							return;
					}
					break;

				case LONG:
					switch (t2) {
						case CHAR:
							autoCastLong(v1, v1.longNumber * v2.character);
							return;

						case BYTE:
							autoCastLong(v1, v1.longNumber * v2.byteNumber);
							return;

						case SHORT:
							autoCastLong(v1, v1.longNumber * v2.shortNumber);
							return;

						case INT:
							autoCastLong(v1, v1.longNumber * v2.intNumber);
							return;

						case LONG:
							autoCastLong(v1, v1.longNumber * v2.longNumber);
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.longNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.longNumber * v2.doubleNumber;
							return;
					}
					break;

				case FLOAT:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.character;
							return;

						case BYTE:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.byteNumber;
							return;

						case SHORT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.shortNumber;
							return;

						case INT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.intNumber;
							return;

						case LONG:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.longNumber;
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.floatNumber * v2.doubleNumber;
							return;
					}
					break;

				case DOUBLE:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.character;
							return;

						case BYTE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.byteNumber;
							return;

						case SHORT:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.shortNumber;
							return;

						case INT:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.intNumber;
							return;

						case LONG:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.longNumber;
							return;

						case FLOAT:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.doubleNumber;
							return;
					}
					break;
			}
		} else {
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_INT;
							v1.intNumber = v1.character * v2.character;
							return;

						case BYTE:
							v1.type = TYPE_INT;
							v1.intNumber = v1.character * v2.byteNumber;
							return;

						case SHORT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.character * v2.shortNumber;
							return;

						case INT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.character * v2.intNumber;
							return;

						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.character * v2.longNumber;
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.character * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.character * v2.doubleNumber;
							return;
					}
					break;

				case BYTE:
					switch (t2) {
						case CHAR:
							v1.intNumber = v1.byteNumber * v2.character;
							v1.type = TYPE_INT;
							return;

						case BYTE:
							v1.type = TYPE_INT;
							v1.intNumber = v1.byteNumber * v2.byteNumber;
							return;

						case SHORT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.byteNumber * v2.shortNumber;
							return;

						case INT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.byteNumber * v2.intNumber;
							return;

						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.byteNumber * v2.longNumber;
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.byteNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.byteNumber * v2.doubleNumber;
							return;
					}
					break;

				case SHORT:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_INT;
							v1.intNumber = v1.shortNumber * v2.character;
							return;

						case BYTE:
							v1.type = TYPE_INT;
							v1.intNumber = v1.shortNumber * v2.byteNumber;
							return;

						case SHORT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.shortNumber * v2.shortNumber;
							return;

						case INT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.shortNumber * v2.intNumber;
							return;

						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.shortNumber * v2.longNumber;
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.shortNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.shortNumber * v2.doubleNumber;
							return;
					}
					break;

				case INT:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_INT;
							v1.intNumber = v1.intNumber * v2.character;
							return;

						case BYTE:
							v1.type = TYPE_INT;
							v1.intNumber = v1.intNumber * v2.byteNumber;
							return;

						case SHORT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.intNumber * v2.shortNumber;
							return;

						case INT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.intNumber * v2.intNumber;
							return;

						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.intNumber * v2.longNumber;
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.intNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.intNumber * v2.doubleNumber;
							return;
					}
					break;

				case LONG:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber * v2.character;
							return;

						case BYTE:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber * v2.byteNumber;
							return;

						case SHORT:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber * v2.shortNumber;
							return;

						case INT:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber * v2.intNumber;
							return;

						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber * v2.longNumber;
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.longNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.longNumber * v2.doubleNumber;
							return;
					}
					break;

				case FLOAT:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.character;
							return;

						case BYTE:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.byteNumber;
							return;

						case SHORT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.shortNumber;
							return;

						case INT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.intNumber;
							return;

						case LONG:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.longNumber;
							return;

						case FLOAT:
							v1.type = TYPE_FLOAT;
							v1.floatNumber = v1.floatNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.floatNumber * v2.doubleNumber;
							return;
					}
					break;

				case DOUBLE:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.character;
							return;

						case BYTE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.byteNumber;
							return;

						case SHORT:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.shortNumber;
							return;

						case INT:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.intNumber;
							return;

						case LONG:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.longNumber;
							return;

						case FLOAT:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.floatNumber;
							return;

						case DOUBLE:
							v1.type = TYPE_DOUBLE;
							v1.doubleNumber = v1.doubleNumber * v2.doubleNumber;
							return;
					}
					break;
			}
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
