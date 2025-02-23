package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeFloat extends NodeNumber {
	// TODO NaN, Infinite

	public NodeFloat(float value, Token token) {
		super("float", TYPE_FLOAT, token);
		this.value = value;
	}

	private final float value;

	public float getValue() {
		return value;
	}

	@Override
	public Object getConstantValue() {
		return value;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.compileValue;
		ctx.nodeValueType.type = Type.floatType;
		return HiClassPrimitive.FLOAT;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.setFloatValue(value);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeFloat(value);
	}

	public static NodeFloat decode(DecodeContext os) throws IOException {
		return new NodeFloat(os.readFloat(), null);
	}
}
