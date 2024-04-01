package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationEquateXOR extends BinaryOperation {
	private static HiOperation instance = new OperationEquateXOR();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationEquateXOR() {
		super("^=", EQUATE_XOR);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		if (node1.type == HiClassPrimitive.BYTE.getAutoboxClass() || node1.type == HiClassPrimitive.SHORT.getAutoboxClass()) {
			errorInvalidOperator(validationInfo, node1.token, node1.type, node2.type);
			return null;
		}

		HiClass c2 = node2.type.getAutoboxedPrimitiveClass() == null ? node2.type : node2.type.getAutoboxedPrimitiveClass();
		// TODO check

		HiNodeIF node = node1.node != null ? node1.node : node1.resolvedValueVariable;
		checkFinal(validationInfo, ctx, node, true);
		return node1.type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.valueType != Value.VARIABLE && v1.valueType != Value.ARRAY_INDEX) {
			errorUnexpectedType(ctx);
			return;
		}

		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();
		if (c1.isPrimitive() && c2.isPrimitive()) {
			int t1 = HiFieldPrimitive.getType(c1);
			int t2 = HiFieldPrimitive.getType(c2);
			switch (t1) {
				case FLOAT:
				case DOUBLE:
					errorInvalidOperator(ctx, v1.type, v2.type);
					return;
			}

			switch (t2) {
				case FLOAT:
				case DOUBLE:
					errorInvalidOperator(ctx, v1.type, v2.type);
					return;
			}

			switch (t1) {
				case BOOLEAN:
					if (t2 != BOOLEAN) {
						errorInvalidOperator(ctx, v1.type, v2.type);
						return;
					}
					v1.bool ^= v2.bool;
					break;

				case CHAR:
					switch (t2) {
						case CHAR:
							v1.character ^= v2.character;
							break;
						case BYTE:
							v1.character ^= v2.byteNumber;
							break;
						case SHORT:
							v1.character ^= v2.shortNumber;
							break;
						case INT:
							v1.character ^= v2.intNumber;
							break;
						case LONG:
							v1.character ^= v2.longNumber;
							break;
					}
					break;

				case BYTE:
					switch (t2) {
						case CHAR:
							v1.byteNumber ^= v2.character;
							break;
						case BYTE:
							v1.byteNumber ^= v2.byteNumber;
							break;
						case SHORT:
							v1.byteNumber ^= v2.shortNumber;
							break;
						case INT:
							v1.byteNumber ^= v2.intNumber;
							break;
						case LONG:
							v1.byteNumber ^= v2.longNumber;
							break;
					}
					break;

				case SHORT:
					switch (t2) {
						case CHAR:
							v1.shortNumber ^= v2.character;
							break;
						case BYTE:
							v1.shortNumber ^= v2.byteNumber;
							break;
						case SHORT:
							v1.shortNumber ^= v2.shortNumber;
							break;
						case INT:
							v1.shortNumber ^= v2.intNumber;
							break;
						case LONG:
							v1.shortNumber ^= v2.longNumber;
							break;
					}
					break;

				case INT:
					switch (t2) {
						case CHAR:
							v1.intNumber ^= v2.character;
							break;
						case BYTE:
							v1.intNumber ^= v2.byteNumber;
							break;
						case SHORT:
							v1.intNumber ^= v2.shortNumber;
							break;
						case INT:
							v1.intNumber ^= v2.intNumber;
							break;
						case LONG:
							v1.intNumber ^= v2.longNumber;
							break;
					}
					break;

				case LONG:
					switch (t2) {
						case CHAR:
							v1.longNumber ^= v2.character;
							break;
						case BYTE:
							v1.longNumber ^= v2.byteNumber;
							break;
						case SHORT:
							v1.longNumber ^= v2.shortNumber;
							break;
						case INT:
							v1.longNumber ^= v2.intNumber;
							break;
						case LONG:
							v1.longNumber ^= v2.longNumber;
							break;
					}
					break;
			}

			// autobox
			if (v1.type.isObject()) {
				v1.type = c1;
				v1.object = ((HiClassPrimitive) c1).autobox(ctx, v1);
			}

			if (v1.valueType == Value.VARIABLE) {
				v1.variable.set(ctx, v1);
			} else if (v1.valueType == Value.ARRAY_INDEX) {
				v1.copyToArray(v1);
			}
			return;
		}

		errorInvalidOperator(ctx, v1.type, v2.type);
	}
}
