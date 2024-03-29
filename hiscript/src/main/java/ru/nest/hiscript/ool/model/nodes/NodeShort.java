package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeShort extends NodeNumber {
	private final static String name = "short";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeShort(short value, boolean hasSign, Token token) {
		super(name, TYPE_SHORT, hasSign, token);
		this.value = value;
	}

	private short value;

	public short getValue() {
		return value;
	}

	@Override
	public Object getConstantValue() {
		return value;
	}

		@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.SHORT;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.shortNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeShort(value);
		os.writeBoolean(hasSign);
	}

	public static NodeShort decode(DecodeContext os) throws IOException {
		return new NodeShort(os.readShort(), os.readBoolean(), null);
	}
}
