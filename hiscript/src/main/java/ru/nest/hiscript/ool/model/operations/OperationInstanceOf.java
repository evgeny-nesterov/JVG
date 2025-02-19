package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.nodes.NodeCastedIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

public class OperationInstanceOf extends BinaryOperation {
	private static final HiOperation instance = new OperationInstanceOf();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationInstanceOf() {
		super(INSTANCE_OF);
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.clazz;
		HiClass c2 = node2.clazz;
		if (node2.returnType != NodeValueType.NodeValueReturnType.classValue && node2.returnType != NodeValueType.NodeValueReturnType.castedIdentifier) {
			validationInfo.error("type expected", node2.node.getToken());
		}
		if (!c1.isVar()) {
			// @generic
			if (c1.isGeneric()) {
				c1 = ((HiClassGeneric) c1).clazz;
			}
			if (!c1.isInstanceof(c2) && !c2.isInstanceof(c1) && !c1.isNull()) {
				validationInfo.error("inconvertible types; cannot cast " + c1.getNameDescr() + " to " + c2.getNameDescr(), node2.node.getToken());
			}
			if (node2.node instanceof NodeCastedIdentifier) {
				NodeCastedIdentifier identifier = (NodeCastedIdentifier) node2.node;
				if (identifier.declarationNode != null) {
					ctx.initializedNodes.add(identifier.declarationNode);
				}
			}
		}
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		return HiClassPrimitive.BOOLEAN;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c2 = v2.valueClass;
		HiClass c1;
		if (v1.valueClass.isArray()) {
			c1 = v1.object != null ? v1.valueClass : HiClassNull.NULL;
		} else if (v1.object instanceof HiObject) {
			c1 = ((HiObject) v1.object).clazz;
		} else if (v1.object == null) {
			c1 = HiClassNull.NULL;
		} else { // array
			c1 = v1.originalValueClass;
		}

		boolean isInstanceof = c1.isInstanceof(c2);
		if (isInstanceof) {
			if (v2.castedVariableName != null) {
				HiFieldObject castedField = (HiFieldObject) HiField.getField(c2, v2.castedVariableName, null);
				castedField.set(v1.object, v1.valueClass);
				ctx.addVariable(castedField);
			}
			if (v2.castedRecordArguments != null) {
				for (HiNode castedRecordArgument : v2.castedRecordArguments) {
					HiField castedField = ((HiObject) v1.object).getField(ctx, ((NodeVariable) castedRecordArgument).getVariableName(), c2);
					ctx.addVariable(castedField);
				}
			}
		}

		v1.valueClass = TYPE_BOOLEAN;
		v1.bool = isInstanceof;
	}
}
