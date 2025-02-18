package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import java.io.IOException;

public class NodeThis extends HiNode {
	public NodeThis() {
		super("this", TYPE_THIS, false);
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass invocationClass = ctx.invocationNode != null ? ctx.invocationNode.clazz : ctx.clazz;
		Type invocationType = ctx.invocationNode != null ? ctx.invocationNode.type : Type.getType(ctx.clazz);
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = invocationClass;
		ctx.nodeValueType.enclosingType = invocationType;
		return invocationClass;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		if (ctx.clazz == null) {
			validationInfo.error("cannot resolve this", token);
			valid = false;
		}
		return valid;
	}

	@Override
	public int getInvocationValueType() {
		return Value.TYPE_INVOCATION;
	}

	@Override
	public void execute(RuntimeContext ctx, HiClass clazz) {
		HiObject currentObject = ctx.getCurrentObject();
		if (clazz != null && clazz != currentObject.clazz) {
			while (currentObject != null) {
				currentObject = currentObject.outboundObject;
				if (currentObject == null || currentObject.clazz == clazz) {
					break;
				}
			}
		}

		assert currentObject != null;
		ctx.value.setObjectValue(currentObject.clazz, currentObject);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeThis decode(DecodeContext os) {
		return new NodeThis();
	}
}
