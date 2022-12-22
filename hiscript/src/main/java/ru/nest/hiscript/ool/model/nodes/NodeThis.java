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
	public final static NodeThis instance = new NodeThis();

	private NodeThis() {
		super("this", THIS);
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return ctx.clazz;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		HiObject currentObject = ctx.getCurrentObject();
		if (currentObject == null) {
			ctx.throwRuntimeException("can not access to this");
			return;
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = currentObject.clazz;
		ctx.value.object = currentObject;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeThis decode(DecodeContext os) throws IOException {
		return instance;
	}
}
