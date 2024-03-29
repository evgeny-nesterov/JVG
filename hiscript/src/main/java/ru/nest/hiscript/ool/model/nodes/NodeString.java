package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldArray;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeString extends HiNode {
	public NodeString(String text) {
		super("string", TYPE_STRING);
		this.text = text;
		this.chars = text.toCharArray();
	}

	private String text;

	private char[] chars;

	private static HiClass clazz;

	private static HiConstructor constructor;

	@Override
	public boolean isConstant(CompileClassContext ctx) {
		return true;
	}

	@Override
	public Object getConstantValue() {
		return text;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass =  ctx.getClassLoader().getClass(HiClass.STRING_CLASS_NAME);
		return ctx.nodeValueType.enclosingClass;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		createString(ctx, chars);
	}

	public static HiObject createString(RuntimeContext ctx, String text) {
		return createString(ctx, text.toCharArray());
	}

	// TODO cache string objects
	public static HiObject createString(RuntimeContext ctx, char[] text) {
		if (clazz == null) {
			clazz = HiClass.forName(ctx, HiClass.STRING_CLASS_NAME);
			constructor = clazz.getConstructor(ctx);
		}

		HiObject obj = constructor.newInstance(ctx, null, null);
		if (obj != null) {
			HiFieldArray chars = (HiFieldArray) obj.getField(ctx, "chars");
			chars.array = text;
		}
		return obj;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(text);
	}

	public static NodeString decode(DecodeContext os) throws IOException {
		return new NodeString(os.readUTF());
	}
}
