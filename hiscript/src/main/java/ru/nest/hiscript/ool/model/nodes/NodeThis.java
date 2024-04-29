package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

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
		if (currentObject == null) {
			ctx.throwRuntimeException("cannot access this class");
			return;
		}

		if (clazz != null && clazz != currentObject.clazz) {
			while (currentObject != null) {
				currentObject = currentObject.outboundObject;
				if (currentObject == null || currentObject.clazz == clazz) {
					break;
				}
			}
		}

		if (currentObject != null) {
			ctx.value.valueType = Value.VALUE;
			ctx.value.valueClass = currentObject.clazz;
			ctx.value.lambdaClass = null;
			ctx.value.object = currentObject;
		} else {
			ctx.throwRuntimeException("can not find this for class '" + clazz.getNameDescr() + "'");
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeThis decode(DecodeContext os) {
		return new NodeThis();
	}
}
