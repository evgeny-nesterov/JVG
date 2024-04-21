package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.lib.ImplUtil;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeGetClass extends HiNode {
	public NodeGetClass() {
		super("class", TYPE_GET_CLASS, false);
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return ImplUtil.getClassClass(ctx);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		return true;
	}

	@Override
	public int getInvocationValueType() {
		return Value.TYPE_INVOCATION;
	}

	@Override
	public void execute(RuntimeContext ctx, HiClass clazz) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = ImplUtil.getClassClass(ctx);
		ctx.value.object = ImplUtil.getClassObject(ctx, clazz);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeGetClass decode(DecodeContext os) {
		return new NodeGetClass();
	}
}
