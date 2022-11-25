package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldArray;

public class NodeString extends Node {
	public NodeString(String text) {
		super("string", TYPE_STRING);
		this.text = text.toCharArray();
	}

	public char[] text;

	private static HiClass clazz;

	private static HiConstructor constructor;

	@Override
	public void execute(RuntimeContext ctx) {
		createString(ctx, text);
	}

	public static HiObject createString(RuntimeContext ctx, char[] text) {
		if (clazz == null) {
			clazz = HiClass.forName(ctx, "String");
			constructor = clazz.getConstructor(ctx);
		}

		HiObject obj = constructor.newInstance(ctx, null, null);
		if (obj != null) {
			HiFieldArray chars = (HiFieldArray) obj.getField("chars");
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
