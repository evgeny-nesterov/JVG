package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeLong extends NodeNumber {
	private final static String name = "long";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeLong(long value, boolean hasSign, Token token) {
		super(name, TYPE_LONG, hasSign, token);
		this.value = value;
	}

	private long value;

	public long getValue() {
		return value;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.LONG;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.longNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeLong(value);
		os.writeBoolean(hasSign);
	}

	public static NodeLong decode(DecodeContext os) throws IOException {
		return new NodeLong(os.readLong(), os.readBoolean(), null);
	}
}
