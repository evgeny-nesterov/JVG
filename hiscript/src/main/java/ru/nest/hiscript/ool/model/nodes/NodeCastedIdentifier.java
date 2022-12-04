package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

import java.io.IOException;

public class NodeCastedIdentifier extends Node {
	public NodeCastedIdentifier(String name) {
		super("identifier", TYPE_CASTED_IDENTIFIER);
		this.name = name.intern();
	}

	public String name;

	public String getName() {
		return name;
	}

	public NodeArgument[] castedRecordArguments;

	public String castedVariableName;

	public Node castedCondition;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.NAME;
		ctx.value.name = name;
		ctx.value.castedRecordArguments = castedRecordArguments;
		ctx.value.castedVariableName = castedVariableName;
		ctx.value.castedCondition = castedCondition;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(name);
		os.writeByte(castedRecordArguments != null ? castedRecordArguments.length : 0);
		os.writeNullable(castedRecordArguments);
		os.writeNullableUTF(castedVariableName);
		os.writeNullable(castedCondition);
	}

	public static NodeCastedIdentifier decode(DecodeContext os) throws IOException {
		NodeCastedIdentifier node = new NodeCastedIdentifier(os.readUTF());
		node.castedRecordArguments = os.readNullableNodeArray(NodeArgument.class, os.readByte());
		node.castedVariableName = os.readNullableUTF();
		node.castedCondition = os.readNullable(Node.class);
		return node;
	}
}
