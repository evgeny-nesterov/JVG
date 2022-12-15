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

public class OperationLower extends BinaryOperation {
	private static Operation instance = new OperationLower();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLower() {
		super("<", LOWER);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node1, NodeExpressionNoLS.NodeOperandType node2) {
		HiClass c1 = node1.type;
		HiClass c2 = node2.type;
		if (c1.isNumber() && c2.isNumber()) {
			return HiClassPrimitive.BOOLEAN;
		} else {
			errorInvalidOperator(validationInfo, node1.node.getToken(), c1, c2);
			return null;
		}
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
		v1.type = TYPE_BOOLEAN;
		switch (t1) {
			case CHAR:
				switch (t2) {
					case CHAR:
						v1.bool = v1.character < v2.character;
						return;

					case BYTE:
						v1.bool = v1.character < v2.byteNumber;
						return;

					case SHORT:
						v1.bool = v1.character < v2.shortNumber;
						return;

					case INT:
						v1.bool = v1.character < v2.intNumber;
						return;

					case LONG:
						v1.bool = v1.character < v2.longNumber;
						return;

					case FLOAT:
						v1.bool = v1.character < v2.floatNumber;
						return;

					case DOUBLE:
						v1.bool = v1.character < v2.doubleNumber;
						return;
				}
				break;

			case BYTE:
				switch (t2) {
					case CHAR:
						v1.bool = v1.byteNumber < v2.character;
						return;

					case BYTE:
						v1.bool = v1.byteNumber < v2.byteNumber;
						return;

					case SHORT:
						v1.bool = v1.byteNumber < v2.shortNumber;
						return;

					case INT:
						v1.bool = v1.byteNumber < v2.intNumber;
						return;

					case LONG:
						v1.bool = v1.byteNumber < v2.longNumber;
						return;

					case FLOAT:
						v1.bool = v1.byteNumber < v2.floatNumber;
						return;

					case DOUBLE:
						v1.bool = v1.byteNumber < v2.doubleNumber;
						return;
				}
				break;

			case SHORT:
				switch (t2) {
					case CHAR:
						v1.bool = v1.shortNumber < v2.character;
						return;

					case BYTE:
						v1.bool = v1.shortNumber < v2.byteNumber;
						return;

					case SHORT:
						v1.bool = v1.shortNumber < v2.shortNumber;
						return;

					case INT:
						v1.bool = v1.shortNumber < v2.intNumber;
						return;

					case LONG:
						v1.bool = v1.shortNumber < v2.longNumber;
						return;

					case FLOAT:
						v1.bool = v1.shortNumber < v2.floatNumber;
						return;

					case DOUBLE:
						v1.bool = v1.shortNumber < v2.doubleNumber;
						return;
				}
				break;

			case INT:
				switch (t2) {
					case CHAR:
						v1.bool = v1.intNumber < v2.character;
						return;

					case BYTE:
						v1.bool = v1.intNumber < v2.byteNumber;
						return;

					case SHORT:
						v1.bool = v1.intNumber < v2.shortNumber;
						return;

					case INT:
						v1.bool = v1.intNumber < v2.intNumber;
						return;

					case LONG:
						v1.bool = v1.intNumber < v2.longNumber;
						return;

					case FLOAT:
						v1.bool = v1.intNumber < v2.floatNumber;
						return;

					case DOUBLE:
						v1.bool = v1.intNumber < v2.doubleNumber;
						return;
				}
				break;

			case LONG:
				switch (t2) {
					case CHAR:
						v1.bool = v1.longNumber < v2.character;
						return;

					case BYTE:
						v1.bool = v1.longNumber < v2.byteNumber;
						return;

					case SHORT:
						v1.bool = v1.longNumber < v2.shortNumber;
						return;

					case INT:
						v1.bool = v1.longNumber < v2.intNumber;
						return;

					case LONG:
						v1.bool = v1.longNumber < v2.longNumber;
						return;

					case FLOAT:
						v1.bool = v1.longNumber < v2.floatNumber;
						return;

					case DOUBLE:
						v1.bool = v1.longNumber < v2.doubleNumber;
						return;
				}
				break;

			case FLOAT:
				switch (t2) {
					case CHAR:
						v1.bool = v1.floatNumber < v2.character;
						return;

					case BYTE:
						v1.bool = v1.floatNumber < v2.byteNumber;
						return;

					case SHORT:
						v1.bool = v1.floatNumber < v2.shortNumber;
						return;

					case INT:
						v1.bool = v1.floatNumber < v2.intNumber;
						return;

					case LONG:
						v1.bool = v1.floatNumber < v2.longNumber;
						return;

					case FLOAT:
						v1.bool = v1.floatNumber < v2.floatNumber;
						return;

					case DOUBLE:
						v1.bool = v1.floatNumber < v2.doubleNumber;
						return;
				}
				break;

			case DOUBLE:
				switch (t2) {
					case CHAR:
						v1.bool = v1.doubleNumber < v2.character;
						return;

					case BYTE:
						v1.bool = v1.doubleNumber < v2.byteNumber;
						return;

					case SHORT:
						v1.bool = v1.doubleNumber < v2.shortNumber;
						return;

					case INT:
						v1.bool = v1.doubleNumber < v2.intNumber;
						return;

					case LONG:
						v1.bool = v1.doubleNumber < v2.longNumber;
						return;

					case FLOAT:
						v1.bool = v1.doubleNumber < v2.floatNumber;
						return;

					case DOUBLE:
						v1.bool = v1.doubleNumber < v2.doubleNumber;
						return;
				}
				break;
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
