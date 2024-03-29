package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeChar extends HiNode {
	private final static String name = "char";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	private static NodeChar[] cache = new NodeChar[256];

	public static NodeChar getInstance(char value, Token token) {
		if (token != null) {
			NodeChar node = new NodeChar(value);
			node.token = token;
			return node;
		}

		int index = value & 0xFF;
		if (cache[index] == null) {
			cache[index] = new NodeChar(value);
		}
		return cache[index];
	}

	private NodeChar(char value) {
		super(name, TYPE_CHAR);
		this.value = value;
	}

	private char value;

	public char getValue() {
		return value;
	}

	@Override
	public Object getConstantValue() {
		return value;
	}

	@Override
	public boolean isValue() {
		return true;
	}

	@Override
	public boolean isConstant(CompileClassContext ctx) {
		return true;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.CHAR;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.character = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeChar(value);
	}

	public static NodeChar decode(DecodeContext os, Token token) throws IOException {
		return getInstance(os.readChar(), token);
	}
}
