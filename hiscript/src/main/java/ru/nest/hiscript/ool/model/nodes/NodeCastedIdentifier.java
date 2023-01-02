package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassRecord;
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

	public NodeDeclaration declarationNode;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
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
			for (int i = 0; i < castedRecordArguments.length; i++) {
				NodeArgument castedRecordArgument = castedRecordArguments[i];
				valid &= castedRecordArgument.validate(validationInfo, ctx) && castedRecordArgument.expectValue(validationInfo, ctx);
			}

			HiClass recordClass = ctx.getLocalClass(name);
			if (recordClass != null) {
				if (!recordClass.isRecord()) {
					validationInfo.error("Inconvertible types; cannot cast " + name + " to Record", getToken());
					valid = false;
				}
				for (int i = 0; i < castedRecordArguments.length; i++) {
					NodeArgument castedRecordArgument = castedRecordArguments[i];
					NodeValueType castedRecordArgumentValueType = castedRecordArgument.getValueType(validationInfo, ctx);
					HiClass castedRecordArgumentClass = castedRecordArgumentValueType.type;
					boolean isCastedRecordArgumentValue = castedRecordArgumentValueType.isValue;

					NodeArgument recordArgument = null;
					for (NodeArgument argument : ((HiClassRecord) recordClass).defaultConstructor.arguments) {
						if (argument.getVariableName().equals(castedRecordArgument.getVariableName())) {
							recordArgument = argument;
							break;
						}
					}
					if (recordArgument == null && recordClass.constructors != null) {
						for (HiConstructor constructor : recordClass.constructors) {
							if (constructor.arguments != null) {
								for (NodeArgument argument : constructor.arguments) {
									if (argument.getVariableName().equals(castedRecordArgument.getVariableName())) {
										recordArgument = argument;
										break;
									}
								}
							}
						}
					}
					if (recordArgument != null) {
						HiClass recordArgumentClass = recordArgument.getValueClass(validationInfo, ctx);
						if (recordArgumentClass != null && !HiClass.autoCast(recordArgumentClass, castedRecordArgumentClass, isCastedRecordArgumentValue)) {
							validationInfo.error("Record argument '" + castedRecordArgument.getVariableType() + " " + castedRecordArgument.getVariableName() + "' is not found", castedRecordArgument.getToken());
							valid = false;
						}
					} else {
						validationInfo.error("Record argument '" + castedRecordArgument.getVariableType() + " " + castedRecordArgument.getVariableName() + "' is not found", castedRecordArgument.getToken());
						valid = false;
					}
				}
			} else {
				valid = false;
			}
		}
		if (castedVariableName != null) {
			declarationNode = new NodeDeclaration(name, castedVariableName);
			declarationNode.setToken(token);
			valid &= ctx.addLocalVariable(declarationNode);
			ctx.initializedNodes.add(declarationNode);
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
