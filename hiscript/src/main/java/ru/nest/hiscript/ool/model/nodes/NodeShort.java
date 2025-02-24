package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeShort extends NodeNumber {
	public NodeShort(short value, Token token) {
		super("short", TYPE_SHORT, token);
		this.value = value;
	}

	private final short value;

	public short getValue() {
		return value;
	}

	@Override
	public Object getConstantValue() {
		return value;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.compileValue;
		ctx.nodeValueType.type = Type.shortType;
		return HiClassPrimitive.SHORT;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.setShortValue(value);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeShort(value);
	}

	public static NodeShort decode(DecodeContext os) throws IOException {
		return new NodeShort(os.readShort(), null);
	}
}
