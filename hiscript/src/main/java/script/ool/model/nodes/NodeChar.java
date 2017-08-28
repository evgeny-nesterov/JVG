package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeChar extends Node {
	private final static String name = "char";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

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

	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.character = value;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeChar(value);
	}

	public static NodeChar decode(DecodeContext os) throws IOException {
		return getInstance(os.readChar());
	}
}
