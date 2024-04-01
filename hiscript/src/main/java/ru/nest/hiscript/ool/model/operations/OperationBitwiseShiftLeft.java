package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationBitwiseShiftLeft extends BinaryOperation {
	private static HiOperation instance = new OperationBitwiseShiftLeft();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationBitwiseShiftLeft() {
		super("<<", BITWISE_SHIFT_LEFT);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.type.getAutoboxedPrimitiveClass() == null ? node1.type : node1.type.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.type.getAutoboxedPrimitiveClass() == null ? node2.type : node2.type.getAutoboxedPrimitiveClass();
		if (c1.isPrimitive()) {
			int t1 = HiFieldPrimitive.getType(c1);
			switch (t1) {
				case VAR:
				case CHAR:
				case BYTE:
				case SHORT:
				case INT:
				case LONG:
					int t2 = HiFieldPrimitive.getType(c1);
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

		errorInvalidOperator(validationInfo, node1.token, node1.type, node2.type);
		return c1;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();
		if (c1.isPrimitive() && c2.isPrimitive()) {
			int t1 = HiFieldPrimitive.getType(c1);
			int t2 = HiFieldPrimitive.getType(c2);
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_INT;
							v1.intNumber = v1.character << v2.character;
							return;
						case BYTE:
							v1.type = TYPE_INT;
							v1.intNumber = v1.character << v2.byteNumber;
							return;
						case SHORT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.character << v2.shortNumber;
							return;
						case INT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.character << v2.intNumber;
							return;
						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.character << v2.longNumber;
							return;
					}
					break;

				case BYTE:
					switch (t2) {
						case CHAR:
							v1.intNumber = v1.byteNumber << v2.character;
							v1.type = TYPE_INT;
							return;
						case BYTE:
							v1.type = TYPE_INT;
							v1.intNumber = v1.byteNumber << v2.byteNumber;
							return;
						case SHORT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.byteNumber << v2.shortNumber;
							return;
						case INT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.byteNumber << v2.intNumber;
							return;
						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.byteNumber << v2.longNumber;
							return;
					}
					break;

				case SHORT:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_INT;
							v1.intNumber = v1.shortNumber << v2.character;
							return;
						case BYTE:
							v1.type = TYPE_INT;
							v1.intNumber = v1.shortNumber << v2.byteNumber;
							return;
						case SHORT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.shortNumber << v2.shortNumber;
							return;
						case INT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.shortNumber << v2.intNumber;
							return;
						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.shortNumber << v2.longNumber;
							return;
					}
					break;

				case INT:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_INT;
							v1.intNumber = v1.intNumber << v2.character;
							return;
						case BYTE:
							v1.type = TYPE_INT;
							v1.intNumber = v1.intNumber << v2.byteNumber;
							return;
						case SHORT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.intNumber << v2.shortNumber;
							return;
						case INT:
							v1.type = TYPE_INT;
							v1.intNumber = v1.intNumber << v2.intNumber;
							return;
						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.intNumber << v2.longNumber;
							return;
					}
					break;

				case LONG:
					switch (t2) {
						case CHAR:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber << v2.character;
							return;
						case BYTE:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber << v2.byteNumber;
							return;
						case SHORT:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber << v2.shortNumber;
							return;
						case INT:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber << v2.intNumber;
							return;
						case LONG:
							v1.type = TYPE_LONG;
							v1.longNumber = v1.longNumber << v2.longNumber;
							return;
					}
					break;
			}
		}

		errorInvalidOperator(ctx, v1.type, v2.type);
	}
}
