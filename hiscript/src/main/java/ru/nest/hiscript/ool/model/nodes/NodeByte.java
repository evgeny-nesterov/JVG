package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeByte extends NodeNumber {
	private final static String name = "byte";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	private static NodeByte[] cache;

	public static NodeByte getInstance(byte value, boolean hasSign, Token token) {
		if (token != null) {
			return new NodeByte(value, hasSign, token);
		}

		if (cache == null) {
			cache = new NodeByte[256];
		}

		int index = value & 0xFF;
		if (cache[index] == null) {
			cache[index] = new NodeByte(value, hasSign, null);
		}
		return cache[index];
	}

	private NodeByte(byte value, boolean hasSign, Token token) {
		super(name, TYPE_BYTE, hasSign, token);
		this.value = value;
	}

	private final byte value;

	public byte getValue() {
		return value;
	}

	@Override
	public Object getConstantValue() {
		return value;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.compileValue;
		ctx.nodeValueType.type = Type.byteType;
		return HiClassPrimitive.BYTE;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = type;
		ctx.value.byteNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeByte(value);
		os.writeBoolean(hasSign);
	}

	public static NodeByte decode(DecodeContext os, Token token) throws IOException {
		return getInstance(os.readByte(), os.readBoolean(), token);
	}

	@Override
	public String toString() {
		return super.name + "=" + value;
	}
}
