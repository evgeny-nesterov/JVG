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

public class NodeInt extends NodeNumber {
	private final static String name = "int";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeInt(int value, boolean hasSign, Token token) {
		super(name, TYPE_INT, hasSign, token);
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
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = type;
		ctx.value.intNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeInt(value);
		os.writeBoolean(hasSign);
	}

	public static NodeInt decode(DecodeContext os) throws IOException {
		return new NodeInt(os.readInt(), os.readBoolean(), null);
	}
}
