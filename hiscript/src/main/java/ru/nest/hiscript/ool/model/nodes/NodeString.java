package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Constructor;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Obj;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.FieldArray;

public class NodeString extends Node {
	public NodeString(String text) {
		super("string", TYPE_STRING);
		this.text = text.toCharArray();
	}

	public char[] text;

	private static Clazz clazz;

	private static Constructor constructor;

	@Override
	public void execute(RuntimeContext ctx) {
		createString(ctx, text);
	}

	public static Obj createString(RuntimeContext ctx, char[] text) {
		if (clazz == null) {
			clazz = Clazz.forName(ctx, "String");
			constructor = clazz.getConstructor(ctx);
		}

		Obj obj = constructor.newInstance(ctx, null, null);
		if (obj != null) {
			FieldArray chars = (FieldArray) obj.getField("chars");
			chars.array = text;
		}

		return obj;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(new String(text));
	}

	public static NodeString decode(DecodeContext os) throws IOException {
		return new NodeString(os.readUTF());
	}
}
