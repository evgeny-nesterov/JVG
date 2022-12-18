package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationBitwiseShiftRight extends BinaryOperation {
	private static Operation instance = new OperationBitwiseShiftRight();

	public static Operation getInstance() {
		return instance;
	}

	private OperationBitwiseShiftRight() {
		super(">>", BITWISE_SHIFT_RIGHT);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node1, NodeExpressionNoLS.NodeOperandType node2) {
		if (node1.type.isPrimitive()) {
			int t1 = HiFieldPrimitive.getType(node1.type);
			switch (t1) {
				case CHAR:
				case BYTE:
				case SHORT:
				case INT:
				case LONG:
					int t2 = HiFieldPrimitive.getType(node1.type);
					switch (t2) {
						case CHAR:
						case BYTE:
						case SHORT:
						case INT:
						case LONG:
							return t1 == LONG ? node1.type : HiClassPrimitive.INT;
					}
			}
		}
		errorInvalidOperator(validationInfo, node1.node.getToken(), node1.type, node2.type);
		return node1.type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.type;
		HiClass c2 = v2.type;

		boolean isP1 = c1.isPrimitive();
		boolean isP2 = c2.isPrimitive();
		if (!isP1 || !isP2) {
			errorInvalidOperator(ctx, c1, c2);
			return;
		}

		int t1 = HiFieldPrimitive.getType(c1);
		int t2 = HiFieldPrimitive.getType(c2);
		switch (t1) {
			case CHAR:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character >> v2.character;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character >> v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character >> v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character >> v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.character >> v2.longNumber;
						return;
				}
				break;

			case BYTE:
				switch (t2) {
					case CHAR:
						v1.intNumber = v1.byteNumber >> v2.character;
						v1.type = TYPE_INT;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.byteNumber >> v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.byteNumber >> v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.byteNumber >> v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.byteNumber >> v2.longNumber;
						return;
				}
				break;

			case SHORT:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber >> v2.character;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber >> v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber >> v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber >> v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.shortNumber >> v2.longNumber;
						return;
				}
				break;

			case INT:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber >> v2.character;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber >> v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber >> v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber >> v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.intNumber >> v2.longNumber;
						return;
				}
				break;

			case LONG:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber >> v2.character;
						return;

					case BYTE:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber >> v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber >> v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber >> v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber >> v2.longNumber;
						return;
				}
				break;
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
