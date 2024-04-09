package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeThis extends HiNode {
	public NodeThis() {
		super("this", TYPE_THIS);
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass invocationClass = ctx.invocationNode != null ? ctx.invocationNode.type : ctx.clazz;
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = invocationClass;
		return invocationClass;
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
			ctx.value.type = currentObject.clazz;
			ctx.value.lambdaClass = null;
			ctx.value.object = currentObject;
		} else {
			ctx.throwRuntimeException("can not find this for class '" + clazz.fullName + "'");
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
