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
	public boolean isValue() {
		return true;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
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
		valid &= trueValueNode.validate(validationInfo, ctx) && trueValueNode.expectValue(validationInfo, ctx);
		valid &= falseValueNode.validate(validationInfo, ctx) && falseValueNode.expectValue(validationInfo, ctx);
		if (valid) {
			HiClass type1 = trueValueNode.getValueClass(validationInfo, ctx);
			HiClass type2 = falseValueNode.getValueClass(validationInfo, ctx);
			if (type1 != null && type2 != null) {
				if (!type1.isInstanceof(type2) && !type2.isInstanceof(type1)) {
					validationInfo.error(type1.fullName + " expected", falseValueNode.getToken());
				}
			}
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
