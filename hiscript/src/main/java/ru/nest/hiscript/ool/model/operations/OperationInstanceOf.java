package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeCastedIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationInstanceOf extends BinaryOperation {
	private static final HiOperation instance = new OperationInstanceOf();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationInstanceOf() {
		super("instanceof", INSTANCE_OF);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.type;
		HiClass c2 = node2.type;
		if (!c1.isVar()) {
			if (c1.isPrimitive()) {
				validationInfo.error("inconvertible types; cannot cast " + c1.fullName + " to " + c2.fullName, node2.node.getToken());
			}
			if (node2.node instanceof NodeCastedIdentifier) {
				NodeCastedIdentifier castedIdentifier = (NodeCastedIdentifier) node2.node;
				if (castedIdentifier.declarationNode != null) {
					ctx.initializedNodes.add(castedIdentifier.declarationNode);
				}
			}
		}
		return HiClassPrimitive.BOOLEAN;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c2 = v2.type;
		if (!v1.type.isPrimitive()) {
			HiClass c1;
			if (v1.type.isArray()) {
				c1 = v1.array != null ? v1.type : HiClassNull.NULL;
			} else {
				c1 = v1.object != null ? v1.object.clazz : HiClassNull.NULL;
			}

			boolean isInstanceof = c1.isInstanceof(c2);
			if (isInstanceof) {
				if (v2.castedVariableName != null) {
					if (ctx.getVariable(v2.castedVariableName) != null) {
						ctx.throwRuntimeException("variable '" + v2.castedVariableName + "' is already defined in the scope");
						return;
					}

					HiFieldObject castedField = (HiFieldObject) HiField.getField(c2, v2.castedVariableName, null);
					castedField.set(v1.object);
					ctx.addVariable(castedField);
				}
				if (v2.castedRecordArguments != null) {
					if (!c2.isRecord()) {
						ctx.throwRuntimeException("inconvertible types; cannot cast " + c2.fullName + " to Record");
						return;
					}
					for (NodeArgument castedRecordArgument : v2.castedRecordArguments) {
						HiField castedField = v1.object.getField(ctx, castedRecordArgument.name, c2);
						ctx.addVariable(castedField);
					}
				}
			}

			v1.type = TYPE_BOOLEAN;
			v1.bool = isInstanceof;
			return;
		}
		ctx.throwRuntimeException("inconvertible types; cannot cast " + v1.type.fullName + " to " + c2.fullName);
	}
}
