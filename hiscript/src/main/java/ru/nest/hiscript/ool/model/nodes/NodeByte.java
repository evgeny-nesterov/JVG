package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeByte extends NodeNumber {
	public NodeByte(byte value, Token token) {
		super("byte", TYPE_BYTE, token);
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
		ctx.value.setByteValue(value);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeByte(value);
	}

	public static NodeByte decode(DecodeContext os) throws IOException {
		return new NodeByte(os.readByte(), null);
	}
}
