package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
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
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass type1 = trueValueNode.getValueType(validationInfo, ctx);
		HiClass type2 = falseValueNode.getValueType(validationInfo, ctx);
		if (type1 != null && type2 != null) {
			return type1.getCommonClass(type2);
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (condition.validate(validationInfo, ctx)) {
			HiClass conditionType = condition.getValueType(validationInfo, ctx);
			if (conditionType != HiClassPrimitive.BOOLEAN) {
				validationInfo.error("boolean expression expected", condition.getToken());
			}
		} else {
			valid = false;
		}

		valid &= trueValueNode.validate(validationInfo, ctx);
		valid &= falseValueNode.validate(validationInfo, ctx);

		if (valid) {
			HiClass type1 = trueValueNode.getValueType(validationInfo, ctx);
			HiClass type2 = falseValueNode.getValueType(validationInfo, ctx);
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
