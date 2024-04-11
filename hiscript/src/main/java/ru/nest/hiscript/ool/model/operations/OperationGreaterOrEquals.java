package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationGreaterOrEquals extends BinaryOperation {
	private static final HiOperation instance = new OperationGreaterOrEquals();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationGreaterOrEquals() {
		super(GREATER_OR_EQUALS);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.type.getAutoboxedPrimitiveClass() == null ? node1.type : node1.type.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.type.getAutoboxedPrimitiveClass() == null ? node2.type : node2.type.getAutoboxedPrimitiveClass();
		if (c1.isNumber() && c2.isNumber()) {
			return HiClassPrimitive.BOOLEAN;
		} else {
			errorInvalidOperator(validationInfo, node1.token, node1.type, node2.type);
			return null;
		}
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();
		if (c1.isPrimitive() && c2.isPrimitive()) {
			int t1 = c1.getPrimitiveType();
			int t2 = c2.getPrimitiveType();
			v1.type = TYPE_BOOLEAN;
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							v1.bool = v1.character >= v2.character;
							return;
						case BYTE:
							v1.bool = v1.character >= v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.character >= v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.character >= v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.character >= v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.character >= v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.character >= v2.doubleNumber;
							return;
					}
					break;

				case BYTE:
					switch (t2) {
						case CHAR:
							v1.bool = v1.byteNumber >= v2.character;
							return;
						case BYTE:
							v1.bool = v1.byteNumber >= v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.byteNumber >= v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.byteNumber >= v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.byteNumber >= v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.byteNumber >= v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.byteNumber >= v2.doubleNumber;
							return;
					}
					break;

				case SHORT:
					switch (t2) {
						case CHAR:
							v1.bool = v1.shortNumber >= v2.character;
							return;
						case BYTE:
							v1.bool = v1.shortNumber >= v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.shortNumber >= v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.shortNumber >= v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.shortNumber >= v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.shortNumber >= v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.shortNumber >= v2.doubleNumber;
							return;
					}
					break;

				case INT:
					switch (t2) {
						case CHAR:
							v1.bool = v1.intNumber >= v2.character;
							return;
						case BYTE:
							v1.bool = v1.intNumber >= v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.intNumber >= v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.intNumber >= v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.intNumber >= v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.intNumber >= v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.intNumber >= v2.doubleNumber;
							return;
					}
					break;

				case LONG:
					switch (t2) {
						case CHAR:
							v1.bool = v1.longNumber >= v2.character;
							return;
						case BYTE:
							v1.bool = v1.longNumber >= v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.longNumber >= v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.longNumber >= v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.longNumber >= v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.longNumber >= v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.longNumber >= v2.doubleNumber;
							return;
					}
					break;

				case FLOAT:
					switch (t2) {
						case CHAR:
							v1.bool = v1.floatNumber >= v2.character;
							return;
						case BYTE:
							v1.bool = v1.floatNumber >= v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.floatNumber >= v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.floatNumber >= v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.floatNumber >= v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.floatNumber >= v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.floatNumber >= v2.doubleNumber;
							return;
					}
					break;

				case DOUBLE:
					switch (t2) {
						case CHAR:
							v1.bool = v1.doubleNumber >= v2.character;
							return;
						case BYTE:
							v1.bool = v1.doubleNumber >= v2.byteNumber;
							return;
						case SHORT:
							v1.bool = v1.doubleNumber >= v2.shortNumber;
							return;
						case INT:
							v1.bool = v1.doubleNumber >= v2.intNumber;
							return;
						case LONG:
							v1.bool = v1.doubleNumber >= v2.longNumber;
							return;
						case FLOAT:
							v1.bool = v1.doubleNumber >= v2.floatNumber;
							return;
						case DOUBLE:
							v1.bool = v1.doubleNumber >= v2.doubleNumber;
							return;
					}
					break;
			}
		}

		errorInvalidOperator(ctx, v1.type, v2.type);
	}
}
