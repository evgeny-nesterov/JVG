package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeType extends HiNode {
	public NodeType(Type type) {
		super("type", TYPE_TYPE);
		this.type = type;
	}

	private final Type type;

	public Type getType() {
		return type;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return type.getClass(ctx);
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
