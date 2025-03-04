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
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeString extends HiNode {
	public NodeString(String text, Token token) {
		super("string", TYPE_STRING, false);
		setText(text);
		setToken(token);
	}

	private String text;

	private JavaString chars;

	private static HiClass clazz;

	private static HiConstructor constructor;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		this.chars = new JavaString(text);
	}

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
		createString(ctx, chars, true);
	}

	public static HiObject createString(RuntimeContext ctx, String text, boolean intern) {
		return createString(ctx, new JavaString(text), intern);
	}

	public static HiObject createString(RuntimeContext ctx, char[] chars, boolean intern) {
		return createString(ctx, new JavaString(chars), intern);
	}

	public static HiObject createString(RuntimeContext ctx, JavaString text, boolean intern) {
		if (clazz == null) {
			clazz = HiClass.forName(ctx, HiClass.STRING_CLASS_NAME);
			constructor = clazz.getConstructor(ctx);
		}

		HiObject object = ctx.getEnv() != null ? ctx.getEnv().strings.get(text) : null;
		if (object == null || !intern) {
			HiObject newObject = constructor.newInstance(ctx, null, null, null);
			HiFieldArray chars = (HiFieldArray) newObject.getField(ctx, "chars");
			chars.array = text.getChars();
			if (object == null && ctx.getEnv() != null) {
				ctx.getEnv().strings.put(text, newObject);
			}
			return newObject;
		} else {
			ctx.value.setObjectValue(clazz, object);
			return object;
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(text);
	}

	public static NodeString decode(DecodeContext os) throws IOException {
		return new NodeString(os.readUTF(), null);
	}
}
