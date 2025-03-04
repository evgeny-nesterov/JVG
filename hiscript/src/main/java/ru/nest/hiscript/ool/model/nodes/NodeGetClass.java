package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import java.io.IOException;

public class NodeGetClass extends HiNode {
	public NodeGetClass() {
		super("class", TYPE_GET_CLASS, false);
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return ctx.getClassLoader().getClassClass(ctx);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		return true;
	}

	@Override
	public ValueType getInvocationValueType() {
		return ValueType.TYPE_INVOCATION;
	}

	@Override
	public void execute(RuntimeContext ctx, HiClass clazz) {
		HiClass valueClass = ctx.getClassLoader().getClassClass(ctx);
		HiObject object = ctx.getClassLoader().getClassObject(ctx, clazz);
		ctx.value.setObjectValue(valueClass, object);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeGetClass decode(DecodeContext os) {
		return new NodeGetClass();
	}
}
