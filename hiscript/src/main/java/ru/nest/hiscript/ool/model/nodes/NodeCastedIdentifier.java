package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeCastedIdentifier extends HiNode {
	public NodeCastedIdentifier(String name, int dimension) {
		super("identifier", TYPE_CASTED_IDENTIFIER);
		this.name = name.intern();
		this.dimension = dimension;
	}

	public String name;

	public int dimension;

	public String getName() {
		return name;
	}

	public NodeArgument[] castedRecordArguments;

	public String castedVariableName;

	public HiNode castedCondition;

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass clazz = ctx.getClass(name);
		if (dimension > 0) {
			clazz = clazz.getArrayClass(dimension);
		}
		return clazz;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (castedRecordArguments != null) {
			for (NodeArgument castedRecordArgument : castedRecordArguments) {
				valid &= castedRecordArgument.validate(validationInfo, ctx);
			}
		}
		if (castedVariableName != null) {
			valid &= ctx.addLocalVariable(new NodeDeclaration(name, castedVariableName));
		}
		if (castedCondition != null) {
			valid &= castedCondition.validate(validationInfo, ctx) && castedCondition.expectBooleanValue(validationInfo, ctx);
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.NAME;
		ctx.value.name = name;
		ctx.value.nameDimensions = dimension;
		ctx.value.castedRecordArguments = castedRecordArguments;
		ctx.value.castedVariableName = castedVariableName;
		ctx.value.castedCondition = castedCondition;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(name);
		os.writeByte(dimension);
		os.writeByte(castedRecordArguments != null ? castedRecordArguments.length : 0);
		os.writeNullable(castedRecordArguments);
		os.writeNullableUTF(castedVariableName);
		os.writeNullable(castedCondition);
	}

	public static NodeCastedIdentifier decode(DecodeContext os) throws IOException {
		NodeCastedIdentifier node = new NodeCastedIdentifier(os.readUTF(), os.readByte());
		node.castedRecordArguments = os.readNullableNodeArray(NodeArgument.class, os.readByte());
		node.castedVariableName = os.readNullableUTF();
		node.castedCondition = os.readNullable(HiNode.class);
		return node;
	}
}
