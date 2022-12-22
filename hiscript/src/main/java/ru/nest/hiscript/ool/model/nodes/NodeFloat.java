package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeFloat extends NodeNumber {
	private final static String name = "float";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	// TODO NaN, Infinite

	public NodeFloat(float value, boolean hasSign, Token token) {
		super(name, TYPE_FLOAT, hasSign, token);
		this.value = value;
	}

	private float value;

	public float getValue() {
		return value;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.FLOAT;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.floatNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeFloat(value);
		os.writeBoolean(hasSign);
	}

	public static NodeFloat decode(DecodeContext os) throws IOException {
		return new NodeFloat(os.readFloat(), os.readBoolean(), null);
	}
}
