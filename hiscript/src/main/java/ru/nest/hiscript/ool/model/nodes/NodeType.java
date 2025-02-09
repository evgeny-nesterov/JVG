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
		super("type", TYPE_TYPE, false);
		this.type = type;
	}

	private final Type type;

	private HiClass clazz;

	public Type getType() {
		return type;
	}

	public HiClass getTypeClass() {
		return clazz;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.classValue;
		ctx.nodeValueType.type = type;
		return clazz = type.getClass(ctx);
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
		os.writeClass(clazz);
	}

	public static NodeType decode(DecodeContext os) throws IOException {
		NodeType node = new NodeType(os.readType());
		os.readClass(clazz -> node.clazz = clazz);
		return node;
	}
}
