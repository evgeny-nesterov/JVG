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

public class NodeSuper extends HiNode {
	public NodeSuper() {
		super("super", TYPE_SUPER, false);
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass invocationClass = ctx.invocationNode != null ? ctx.invocationNode.clazz : (ctx.clazz != null ? ctx.clazz : null);
		Type invocationType = ctx.invocationNode != null ? ctx.invocationNode.type : Type.getType(ctx.clazz);
		HiClass superClass = invocationClass != null && !invocationClass.isInterface ? invocationClass.superClass : invocationClass;
		Type superType = invocationClass != null && !invocationClass.isInterface ? invocationClass.superClassType : invocationType;
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = superClass;
		ctx.nodeValueType.enclosingType = superType;
		return superClass;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		if (ctx.clazz == null || ctx.clazz.superClass == null) {
			validationInfo.error("cannot resolve super class", token);
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
		HiObject object = ctx.getCurrentObject();
		HiClass objectClass;
		if (clazz == null || !clazz.isInterface) {
			object = object.getSuperObject();
			objectClass = object.clazz;
		} else {
			objectClass = clazz;
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = objectClass;
		ctx.value.originalValueClass = null;
		ctx.value.object = object;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeSuper decode(DecodeContext os) {
		return new NodeSuper();
	}
}
