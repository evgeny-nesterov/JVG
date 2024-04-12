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
		super("super", TYPE_SUPER, false);
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass invocationClass = ctx.invocationNode != null ? ctx.invocationNode.type : (ctx.clazz != null ? ctx.clazz : null);
		HiClass superClass = invocationClass != null && !invocationClass.isInterface ? invocationClass.superClass : invocationClass;
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = superClass;
		return superClass;
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
		HiObject object = ctx.getCurrentObject();
		HiClass objectClass;
		if (clazz == null || !clazz.isInterface) {
			if (object == null || object.getSuperObject() == null) {
				ctx.throwRuntimeException("cannot access super class");
				return;
			}

			if (clazz != null && clazz != object.clazz) {
				while (object != null) {
					object = object.getSuperObject();
					if (object == null || object.clazz == clazz) {
						break;
					}
				}
			}

			object = object.getSuperObject();
			objectClass = object.clazz;
		} else {
			objectClass = clazz;
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = objectClass;
		ctx.value.lambdaClass = null;
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
