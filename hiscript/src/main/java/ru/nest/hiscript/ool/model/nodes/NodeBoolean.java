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

public class NodeBoolean extends HiNode {
	private final static String name = "boolean";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	private final static NodeBoolean TRUE = new NodeBoolean(true);

	private final static NodeBoolean FALSE = new NodeBoolean(false);

	public static NodeBoolean getInstance(boolean value, Token token) {
		if (token != null) {
			NodeBoolean node = new NodeBoolean(value);
			node.token = token;
			return node;
		} else {
			return value ? TRUE : FALSE;
		}
	}

	private NodeBoolean(boolean value) {
		super(name, TYPE_BOOLEAN);
		this.value = value;
	}

	private final boolean value;

	public boolean getValue() {
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
		return HiClassPrimitive.BOOLEAN;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.bool = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeBoolean(value);
	}

	public static NodeBoolean decode(DecodeContext os, Token token) throws IOException {
		return getInstance(os.readBoolean(), token);
	}
}
