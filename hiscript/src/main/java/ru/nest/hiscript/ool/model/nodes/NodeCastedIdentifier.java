package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import java.io.IOException;

public class NodeCastedIdentifier extends HiNode implements NodeVariable {
	public NodeCastedIdentifier(String name, int dimension) {
		super("identifier", TYPE_CASTED_IDENTIFIER, false);
		this.name = name.intern();
		this.dimension = dimension;
	}

	public String name;

	public int dimension;

	public HiNode[] castedRecordArguments; // NodeVariable (NodeArgument or NodeCastedIdentifier)

	public String castedVariableName;

	public HiNode castedCondition;

	public NodeDeclaration declarationNode; // only for validation

	public HiConstructor constructor;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass clazz = ctx.getClass(name);
		if (dimension > 0) {
			clazz = clazz.getArrayClass(dimension);
		}
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.castedIdentifier;
		ctx.nodeValueType.type = Type.getType(clazz);
		return clazz;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = true;
		if (castedRecordArguments != null) {
			for (int i = 0; i < castedRecordArguments.length; i++) {
				HiNode castedRecordArgument = castedRecordArguments[i];
				valid &= castedRecordArgument.validate(validationInfo, ctx) && castedRecordArgument.expectValue(validationInfo, ctx);
			}

			HiClass recordClass = ctx.getLocalClass(name);
			if (recordClass != null) {
				if (recordClass.isRecord()) {
					HiClass[] castedRecordArgumentsClasses = new HiClass[castedRecordArguments.length];
					for (int i = 0; i < castedRecordArguments.length; i++) {
						HiNode castedRecordArgument = castedRecordArguments[i];
						castedRecordArgumentsClasses[i] = castedRecordArgument.getValueClass(validationInfo, ctx);
					}
					constructor = recordClass.getConstructor(ctx, castedRecordArgumentsClasses);
					if (constructor == null) {
						String argsNames = "";
						for (int i = 0; i < castedRecordArgumentsClasses.length; i++) {
							if (i > 0) {
								argsNames += ", ";
							}
							argsNames += castedRecordArgumentsClasses[i].getNameDescr();
						}
						validationInfo.error("record constructor not found: " + recordClass.getNameDescr() + "(" + argsNames + ")", getToken());
						valid = false;
					}
				} else {
					validationInfo.error("inconvertible types; cannot cast " + name + " to Record", getToken());
					valid = false;
				}
			}
		}
		if (castedVariableName != null) {
			Type type = Type.getTypeByFullName(name, ctx.getEnv());
			declarationNode = new NodeDeclaration(name, type, castedVariableName);
			declarationNode.setToken(token);
			valid &= ctx.addLocalVariable(declarationNode, true);
			ctx.initializedNodes.add(declarationNode);
		}
		if (castedCondition != null) {
			valid &= castedCondition.validate(validationInfo, ctx) && castedCondition.expectBooleanValue(validationInfo, ctx);
		}
		return valid;
	}

	public void removeLocalVariables(CompileClassContext ctx) {
		if (declarationNode != null) {
			ctx.removeLocalVariable(declarationNode);
		}
		if (castedRecordArguments != null) {
			for (int i = 0; i < castedRecordArguments.length; i++) {
				HiNode arg = castedRecordArguments[i];
				ctx.removeLocalVariable((NodeVariable) arg);
				if (arg instanceof NodeCastedIdentifier) {
					((NodeCastedIdentifier) arg).removeLocalVariables(ctx);
				}
			}
		}
	}

	public void removeLocalVariables(RuntimeContext ctx) {
		if (castedVariableName != null) {
			ctx.removeVariable(castedVariableName);
		}
		if (castedRecordArguments != null) {
			for (int i = 0; i < castedRecordArguments.length; i++) {
				HiNode arg = castedRecordArguments[i];
				ctx.removeVariable(((NodeVariable) arg).getVariableName());
				if (arg instanceof NodeCastedIdentifier) {
					((NodeCastedIdentifier) arg).removeLocalVariables(ctx);
				}
			}
		}
	}

	@Override
	public String getVariableName() {
		return castedVariableName != null ? castedVariableName : name;
	}

	@Override
	public String getVariableType() {
		return declarationNode.getVariableType();
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
		os.writeConstructor(constructor);
	}

	public static NodeCastedIdentifier decode(DecodeContext os) throws IOException {
		NodeCastedIdentifier node = new NodeCastedIdentifier(os.readUTF(), os.readByte());
		node.castedRecordArguments = os.readNullableNodeArray(HiNode.class, os.readByte());
		node.castedVariableName = os.readNullableUTF();
		node.castedCondition = os.readNullable(HiNode.class);
		os.readConstructor(constructor -> node.constructor = constructor);
		return node;
	}
}
