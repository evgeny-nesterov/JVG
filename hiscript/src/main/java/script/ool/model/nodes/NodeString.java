package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Constructor;
import script.ool.model.Node;
import script.ool.model.Obj;
import script.ool.model.RuntimeContext;
import script.ool.model.fields.FieldArray;

public class NodeString extends Node {
	public NodeString(String text) {
		super("string", TYPE_STRING);
		this.text = text.toCharArray();
	}

	public char[] text;

	private static Clazz clazz;

	private static Constructor constructor;

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

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(new String(text));
	}

	public static NodeString decode(DecodeContext os) throws IOException {
		return new NodeString(os.readUTF());
	}
}
