package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationNotEquals extends BinaryOperation {
	private static final HiOperation instance = new OperationNotEquals();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationNotEquals() {
		super("!=", NOT_EQUALS);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.type;
		HiClass c2 = node2.type;
		if (c1.isVar() || c2.isVar()) {
			return HiClassPrimitive.BOOLEAN;
		}
		if (c1.isPrimitive() && !c2.isPrimitive()) {
			if (c2.getAutoboxedPrimitiveClass() != null) {
				c2 = c2.getAutoboxedPrimitiveClass();
			}
		} else if (!c1.isPrimitive() && c2.isPrimitive()) {
			if (c1.getAutoboxedPrimitiveClass() != null) {
				c1 = c1.getAutoboxedPrimitiveClass();
			}
		}
		if (c1.isPrimitive() == c2.isPrimitive()) {
			if (c1.isNumber() == c2.isNumber()) {
				return HiClassPrimitive.BOOLEAN;
			} else if (c1 == HiClassPrimitive.BOOLEAN && c2 == HiClassPrimitive.BOOLEAN) {
				return HiClassPrimitive.BOOLEAN;
			}
		} else if (!c1.isPrimitive() && !c2.isPrimitive()) {
			return HiClassPrimitive.BOOLEAN;
		}
		if (node1.valid && node2.valid) {
			errorInvalidOperator(validationInfo, node1.token, node1.type, node2.type);
		}
		return null;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.type;
		HiClass c2 = v2.type;
		boolean isP1 = c1.isPrimitive();
		boolean isP2 = c2.isPrimitive();

		// autobox
		if (isP1 && !isP2) {
			if (c2.getAutoboxedPrimitiveClass() != null) {
				c2 = c2.getAutoboxedPrimitiveClass();
				v2.substitutePrimitiveValueFromAutoboxValue();
				isP2 = true;
			}
		} else if (!isP1 && isP2) {
			if (c1.getAutoboxedPrimitiveClass() != null) {
				c1 = c1.getAutoboxedPrimitiveClass();
				v1.substitutePrimitiveValueFromAutoboxValue();
				isP1 = true;
			}
		}

		v1.type = TYPE_BOOLEAN;
		if (isP1 && isP2) {
			int t1 = c1.getPrimitiveType();
			int t2 = c2.getPrimitiveType();
			switch (t1) {
				case BOOLEAN:
					if (t2 == BOOLEAN) {
						v1.bool = v1.bool != v2.bool;
						return;
					}
					break;

				case CHAR:
					switch (t2) {
						case CHAR:
							v1.bool = v1.character != v2.character;
							return;
						case BYTE:
							v1.bool = v1.character != v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.character != v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.character != v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.character != v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.character != v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.character != v2.doubleNumber;
							return;
					}
					break;

				case BYTE:
					switch (t2) {
						case CHAR:
							v1.bool = v1.byteNumber != v2.character;
							return;
						case BYTE:
							v1.bool = v1.byteNumber != v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.byteNumber != v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.byteNumber != v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.byteNumber != v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.byteNumber != v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.byteNumber != v2.doubleNumber;
							return;
					}
					break;

				case SHORT:
					switch (t2) {
						case CHAR:
							v1.bool = v1.shortNumber != v2.character;
							return;
						case BYTE:
							v1.bool = v1.shortNumber != v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.shortNumber != v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.shortNumber != v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.shortNumber != v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.shortNumber != v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.shortNumber != v2.doubleNumber;
							return;
					}
					break;

				case INT:
					switch (t2) {
						case CHAR:
							v1.bool = v1.intNumber != v2.character;
							return;
						case BYTE:
							v1.bool = v1.intNumber != v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.intNumber != v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.intNumber != v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.intNumber != v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.intNumber != v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.intNumber != v2.doubleNumber;
							return;
					}
					break;

				case LONG:
					switch (t2) {
						case CHAR:
							v1.bool = v1.longNumber != v2.character;
							return;
						case BYTE:
							v1.bool = v1.longNumber != v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.longNumber != v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.longNumber != v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.longNumber != v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.longNumber != v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.longNumber != v2.doubleNumber;
							return;
					}
					break;

				case FLOAT:
					switch (t2) {
						case CHAR:
							v1.bool = v1.floatNumber != v2.character;
							return;
						case BYTE:
							v1.bool = v1.floatNumber != v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.floatNumber != v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.floatNumber != v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.floatNumber != v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.floatNumber != v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.floatNumber != v2.doubleNumber;
							return;
					}
					break;

				case DOUBLE:
					switch (t2) {
						case CHAR:
							v1.bool = v1.doubleNumber != v2.character;
							return;
						case BYTE:
							v1.bool = v1.doubleNumber != v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.doubleNumber != v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.doubleNumber != v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.doubleNumber != v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.doubleNumber != v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.doubleNumber != v2.doubleNumber;
							return;
					}
					break;
			}
		} else if (!isP1 && !isP2) {
			Object o1;
			if (c1.isArray()) {
				o1 = v1.array;
			} else {
				o1 = v1.object;
			}

			Object o2;
			if (c2.isArray()) {
				o2 = v2.array;
			} else {
				o2 = v2.object;
			}

			v1.bool = o1 != o2;
			return;
		}

		errorInvalidOperator(ctx, v1.type, v2.type);
	}
}
