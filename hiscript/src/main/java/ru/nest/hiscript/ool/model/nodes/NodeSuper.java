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
		super("super", SUPER);
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (ctx.clazz != null) {
			return ctx.clazz.superClass;
		}
		return null;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		HiObject currentObject = ctx.getCurrentObject();
		if (currentObject == null || currentObject.getSuperObject() == null) {
			ctx.throwRuntimeException("can not access super");
			return;
		}

		HiObject superObject = currentObject.getSuperObject();
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = superObject.clazz;
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
