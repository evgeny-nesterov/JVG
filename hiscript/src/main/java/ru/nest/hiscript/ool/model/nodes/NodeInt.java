package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeInt extends NodeNumber {
	public NodeInt(int value, Token token) {
		super("int", TYPE_INT, token);
		this.value = value;
	}

	private final int value;

	public int getValue() {
		return value;
	}

	@Override
	public Object getConstantValue() {
		return value;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.compileValue;
		ctx.nodeValueType.type = Type.intType;
		return HiClassPrimitive.INT;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.setIntValue(value);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeInt(value);
	}

	public static NodeInt decode(DecodeContext os) throws IOException {
		return new NodeInt(os.readInt(), null);
	}
}
