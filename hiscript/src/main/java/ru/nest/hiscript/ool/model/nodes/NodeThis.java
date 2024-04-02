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
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = ctx.clazz;
		return ctx.clazz;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		HiObject currentObject = ctx.getCurrentObject();
		if (currentObject == null) {
			ctx.throwRuntimeException("cannot access this class");
			return;
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = currentObject.clazz;
		ctx.value.lambdaClass = null;
		ctx.value.object = currentObject;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeThis decode(DecodeContext os) {
		return new NodeThis();
	}
}
