package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationEquateMultiply extends BinaryOperation {
	private static HiOperation instance = new OperationEquateMultiply();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationEquateMultiply() {
		super("*=", EQUATE_MULTIPLY);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		// TODO check
		return node1.type;
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

		if (v1.valueType != Value.VARIABLE && v1.valueType != Value.ARRAY_INDEX) {
			errorUnexpectedType(ctx);
			return;
		}

		int t1 = HiFieldPrimitive.getType(c1);
		int t2 = HiFieldPrimitive.getType(c2);
		switch (t1) {
			case CHAR:
				switch (t2) {
					case CHAR:
						v1.character *= v2.character;
						break;

					case BYTE:
						v1.character *= v2.byteNumber;
						break;

					case SHORT:
						v1.character *= v2.shortNumber;
						break;

					case INT:
						v1.character *= v2.intNumber;
						break;

					case LONG:
						v1.character *= v2.longNumber;
						break;

					case FLOAT:
						v1.character *= v2.floatNumber;
						break;

					case DOUBLE:
						v1.character *= v2.doubleNumber;
						break;
				}
				break;

			case BYTE:
				switch (t2) {
					case CHAR:
						v1.byteNumber *= v2.character;
						break;

					case BYTE:
						v1.byteNumber *= v2.byteNumber;
						break;

					case SHORT:
						v1.byteNumber *= v2.shortNumber;
						break;

					case INT:
						v1.byteNumber *= v2.intNumber;
						break;

					case LONG:
						v1.byteNumber *= v2.longNumber;
						break;

					case FLOAT:
						v1.byteNumber *= v2.floatNumber;
						break;

					case DOUBLE:
						v1.byteNumber *= v2.doubleNumber;
						break;
				}
				break;

			case SHORT:
				switch (t2) {
					case CHAR:
						v1.shortNumber *= v2.character;
						break;

					case BYTE:
						v1.shortNumber *= v2.byteNumber;
						break;

					case SHORT:
						v1.shortNumber *= v2.shortNumber;
						break;

					case INT:
						v1.shortNumber *= v2.intNumber;
						break;

					case LONG:
						v1.shortNumber *= v2.longNumber;
						break;

					case FLOAT:
						v1.shortNumber *= v2.floatNumber;
						break;

					case DOUBLE:
						v1.shortNumber *= v2.doubleNumber;
						break;
				}
				break;

			case INT:
				switch (t2) {
					case CHAR:
						v1.intNumber *= v2.character;
						break;

					case BYTE:
						v1.intNumber *= v2.byteNumber;
						break;

					case SHORT:
						v1.intNumber *= v2.shortNumber;
						break;

					case INT:
						v1.intNumber *= v2.intNumber;
						break;

					case LONG:
						v1.intNumber *= v2.longNumber;
						break;

					case FLOAT:
						v1.intNumber *= v2.floatNumber;
						break;

					case DOUBLE:
						v1.intNumber *= v2.doubleNumber;
						break;
				}
				break;

			case LONG:
				switch (t2) {
					case CHAR:
						v1.longNumber *= v2.character;
						break;

					case BYTE:
						v1.longNumber *= v2.byteNumber;
						break;

					case SHORT:
						v1.longNumber *= v2.shortNumber;
						break;

					case INT:
						v1.longNumber *= v2.intNumber;
						break;

					case LONG:
						v1.longNumber *= v2.longNumber;
						break;

					case FLOAT:
						v1.longNumber *= v2.floatNumber;
						break;

					case DOUBLE:
						v1.longNumber *= v2.doubleNumber;
						break;
				}
				break;

			case FLOAT:
				switch (t2) {
					case CHAR:
						v1.floatNumber *= v2.character;
						break;

					case BYTE:
						v1.floatNumber *= v2.byteNumber;
						break;

					case SHORT:
						v1.floatNumber *= v2.shortNumber;
						break;

					case INT:
						v1.floatNumber *= v2.intNumber;
						break;

					case LONG:
						v1.floatNumber *= v2.longNumber;
						break;

					case FLOAT:
						v1.floatNumber *= v2.floatNumber;
						break;

					case DOUBLE:
						v1.floatNumber *= v2.doubleNumber;
						break;
				}
				break;

			case DOUBLE:
				switch (t2) {
					case CHAR:
						v1.doubleNumber *= v2.character;
						break;

					case BYTE:
						v1.doubleNumber *= v2.byteNumber;
						break;

					case SHORT:
						v1.doubleNumber *= v2.shortNumber;
						break;

					case INT:
						v1.doubleNumber *= v2.intNumber;
						break;

					case LONG:
						v1.doubleNumber *= v2.longNumber;
						break;

					case FLOAT:
						v1.doubleNumber *= v2.floatNumber;
						break;

					case DOUBLE:
						v1.doubleNumber *= v2.doubleNumber;
						break;
				}
				break;
		}

		if (v1.valueType == Value.VARIABLE) {
			v1.variable.set(ctx, v1);
		} else if (v1.valueType == Value.ARRAY_INDEX) {
			v1.copyToArray(v1);
		}
		return;
	}
}
