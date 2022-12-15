package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeChar extends Node {
	private final static String name = "char";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	private static NodeChar[] cache = new NodeChar[256];

	public static NodeChar getInstance(char value) {
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
	public boolean isValue() {
		return true;
	}

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
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

	public static NodeChar decode(DecodeContext os) throws IOException {
		return getInstance(os.readChar());
	}
}
