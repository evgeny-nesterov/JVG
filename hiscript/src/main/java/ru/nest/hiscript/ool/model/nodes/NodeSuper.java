package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeSuper extends HiNode {
	public NodeSuper() {
		super("super", TYPE_SUPER);
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass invocationClass = ctx.invocationNode != null ? ctx.invocationNode.type.superClass : ctx.clazz.superClass;
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = invocationClass;
		return invocationClass;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (ctx.clazz == null || ctx.clazz.superClass == null) {
			validationInfo.error("cannot resolve super class", token);
			return false;
		}
		return true;
	}

	@Override
	public int getInvocationValueType() {
		return Value.TYPE_INVOCATION;
	}

	@Override
	public void execute(RuntimeContext ctx, HiClass clazz) {
		HiObject currentObject = ctx.getCurrentObject();
		if (currentObject == null || currentObject.getSuperObject() == null) {
			ctx.throwRuntimeException("cannot access super class");
			return;
		}

		if (clazz != null && clazz != currentObject.clazz) {
			while (currentObject != null) {
				currentObject = currentObject.getSuperObject();
				if (currentObject == null || currentObject.clazz == clazz) {
					break;
				}
			}
		}

		HiObject superObject = currentObject.getSuperObject();
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = superObject.clazz;
		ctx.value.lambdaClass = null;
		ctx.value.object = superObject;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeSuper decode(DecodeContext os) {
		return new NodeSuper();
	}
}
