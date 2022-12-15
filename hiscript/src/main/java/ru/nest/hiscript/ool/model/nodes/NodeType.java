package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeType extends Node {
	public NodeType(Type type) {
		super("type", TYPE_TYPE);
		this.type = type;
	}

	private Type type;

	public Type getType() {
		return type;
	}

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		return ctx.getClass(type.fullName);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.TYPE;
		ctx.value.variableType = type;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
	}

	public static NodeType decode(DecodeContext os) throws IOException {
		return new NodeType(os.readType());
	}
}
