package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeLogicalSwitch extends NodeExpression {
	public NodeLogicalSwitch(NodeExpression condition, NodeExpression trueValueNode, NodeExpression falseValueNode) {
		super("?:", TYPE_LOGICAL_SWITCH);
		this.condition = condition;
		this.trueValueNode = trueValueNode;
		this.falseValueNode = falseValueNode;
	}

	private NodeExpression condition;

	private NodeExpression trueValueNode;

	private NodeExpression falseValueNode;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = this;
		HiClass type1 = trueValueNode.getValueClass(validationInfo, ctx);
		HiClass type2 = falseValueNode.getValueClass(validationInfo, ctx);
		if (type1 != null && type2 != null) {
			return type1.getCommonClass(type2);
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = condition.validate(validationInfo, ctx) && condition.expectBooleanValue(validationInfo, ctx);
		boolean trueValid = trueValueNode.validate(validationInfo, ctx) && trueValueNode.expectValue(validationInfo, ctx);
		boolean falseValid = falseValueNode.validate(validationInfo, ctx) && falseValueNode.expectValue(validationInfo, ctx);
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
