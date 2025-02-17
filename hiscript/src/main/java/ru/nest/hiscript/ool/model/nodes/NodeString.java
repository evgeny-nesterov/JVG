package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.JavaString;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.fields.HiFieldArray;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import java.io.IOException;

public class NodeString extends HiNode {
	public NodeString(String text) {
		super("string", TYPE_STRING, false);
		this.text = text;
		this.chars = new JavaString(text);
	}

	public final String text;

	private final JavaString chars;

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
	public boolean isCompileValue() {
		return true;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = this;
		HiClass clazz = ctx.getClassLoader().getClass(HiClass.STRING_CLASS_NAME);
		ctx.nodeValueType.enclosingClass = clazz;
		ctx.nodeValueType.enclosingType = Type.getType(clazz);
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.compileValue;
		ctx.nodeValueType.type = ctx.nodeValueType.enclosingType;
		return clazz;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		createString(ctx, chars);
	}

	public static HiObject createString(RuntimeContext ctx, String text) {
		return createString(ctx, new JavaString(text));
	}

	public static HiObject createString(RuntimeContext ctx, char[] chars) {
		return createString(ctx, new JavaString(chars));
	}

	public static HiObject createString(RuntimeContext ctx, JavaString text) {
		if (clazz == null) {
			clazz = HiClass.forName(ctx, HiClass.STRING_CLASS_NAME);
			constructor = clazz.getConstructor(ctx);
		}

		HiObject object = ctx.strings.get(text);
		if (object == null) {
			object = constructor.newInstance(ctx, null, null, null);
			if (object != null) {
				HiFieldArray chars = (HiFieldArray) object.getField(ctx, "chars");
				chars.array = text.getChars();

				ctx.strings.put(text, object);
			}
		} else {
			ctx.value.valueType = Value.VALUE;
			ctx.value.valueClass = clazz;
			ctx.value.originalValueClass = null;
			ctx.value.object = object;
		}
		return object;
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
