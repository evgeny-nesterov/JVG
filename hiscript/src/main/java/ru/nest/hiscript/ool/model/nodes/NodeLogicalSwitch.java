package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;

public class NodeLogicalSwitch extends NodeExpression {
	public NodeLogicalSwitch(NodeExpression condition, NodeExpression trueValueNode, NodeExpression falseValueNode) {
		super("?:", TYPE_LOGICAL_SWITCH);
		this.condition = condition;
		this.trueValueNode = trueValueNode;
		this.falseValueNode = falseValueNode;
	}

	private final NodeExpression condition;

	private final NodeExpression trueValueNode;

	private final NodeExpression falseValueNode;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = this;
		HiClass type1 = trueValueNode != null ? trueValueNode.getValueClass(validationInfo, ctx) : null;
		HiClass type2 = falseValueNode != null ? falseValueNode.getValueClass(validationInfo, ctx) : null;
		if (type1 != null && type2 != null) {
			NodeValueType compiledValueType = null;
			NodeValueType conditionValueType = condition.getNodeValueType(validationInfo, ctx);
			if (conditionValueType.isCompileValue()) {
				NodeExpression valueNode = conditionValueType.booleanValue ? trueValueNode : falseValueNode;
				NodeValueType valueType = valueNode.getNodeValueType(validationInfo, ctx);
				if (valueType.isCompileValue()) {
					compiledValueType = valueType;
				}
			}

			if (compiledValueType != null) {
				compiledValueType.copyTo(ctx.nodeValueType);
				return compiledValueType.clazz;
			} else {
				HiClass commonClass = type1.getCommonClass(type2);
				ctx.nodeValueType.enclosingClass = commonClass;
				ctx.nodeValueType.enclosingType = commonClass != null ? commonClass.superClassType : null;
				ctx.nodeValueType.returnType = commonClass != null && commonClass.isPrimitive() ? NodeValueType.NodeValueReturnType.compileValue : NodeValueType.NodeValueReturnType.runtimeValue;
				ctx.nodeValueType.type = Type.getType(commonClass);
				return commonClass;
			}
		}
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = condition.validate(validationInfo, ctx) && condition.expectBooleanValue(validationInfo, ctx);
		boolean trueValid = trueValueNode != null ? trueValueNode.validate(validationInfo, ctx) && trueValueNode.expectValue(validationInfo, ctx) : false;
		boolean falseValid = falseValueNode != null ? falseValueNode.validate(validationInfo, ctx) && falseValueNode.expectValue(validationInfo, ctx) : false;
		if (trueValid && falseValid) {
			HiClass trueClass = trueValueNode.getValueClass(validationInfo, ctx);
			HiClass falseClass = falseValueNode.getValueClass(validationInfo, ctx);
			if (trueClass.getCommonClass(falseClass) == null) {
				validationInfo.error("incompatible switch values types: '" + trueClass + "' and '" + falseClass + "'", trueClass.getToken());
				valid = false;
			}
		} else {
			valid = false;
		}
		valid &= super.validate(validationInfo, ctx);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		condition.execute(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		boolean value = ctx.value.getBoolean();
		if (ctx.exitFromBlock()) {
			return;
		}

		if (value) {
			trueValueNode.execute(ctx);
		} else {
			falseValueNode.execute(ctx);
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.write(condition);
		os.write(trueValueNode);
		os.write(falseValueNode);
	}

	public static NodeLogicalSwitch decode(DecodeContext os) throws IOException {
		return new NodeLogicalSwitch(os.read(NodeExpression.class), os.read(NodeExpression.class), os.read(NodeExpression.class));
	}
}
