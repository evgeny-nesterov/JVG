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
	public NodeCastedIdentifier(Type type, String castedVariableName, NodeCastedIdentifier[] castedRecordArguments) {
		super("identifier", TYPE_CASTED_IDENTIFIER, false);
		this.type = type;
		this.castedVariableName = castedVariableName;
		this.castedRecordArguments = castedRecordArguments;
	}

	public Type type;

	public HiClass clazz;

	public String castedVariableName;

	public NodeCastedIdentifier[] castedRecordArguments; // NodeVariable (NodeArgument or NodeCastedIdentifier)

	public HiNode castedCondition;

	public NodeDeclaration declarationNode; // only for validation

	public HiConstructor constructor; // computed on validation

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		clazz = type.getClass(ctx);
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.castedIdentifier;
		ctx.nodeValueType.type = type;
		return clazz;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = true;
		HiClass clazz = getValueClass(validationInfo, ctx);
		if (castedRecordArguments != null) {
			for (int i = 0; i < castedRecordArguments.length; i++) {
				HiNode castedRecordArgument = castedRecordArguments[i];
				valid &= castedRecordArgument.validate(validationInfo, ctx) && castedRecordArgument.expectValue(validationInfo, ctx);
			}
			if (clazz.isRecord()) {
				HiClass[] castedRecordArgumentsClasses = new HiClass[castedRecordArguments.length];
				for (int i = 0; i < castedRecordArguments.length; i++) {
					NodeCastedIdentifier castedRecordArgument = castedRecordArguments[i];
					castedRecordArgumentsClasses[i] = castedRecordArgument.getValueClass(validationInfo, ctx);
				}
				constructor = clazz.getConstructor(ctx, castedRecordArgumentsClasses);
				if (constructor == null) {
					String argsNames = "";
					for (int i = 0; i < castedRecordArgumentsClasses.length; i++) {
						if (i > 0) {
							argsNames += ", ";
						}
						argsNames += castedRecordArgumentsClasses[i].getNameDescr();
					}
					validationInfo.error("record constructor not found: " + clazz.getNameDescr() + "(" + argsNames + ")", getToken());
					valid = false;
				} else {
					for (int i = 0; i < castedRecordArguments.length; i++) {
						if (castedRecordArgumentsClasses[i].isVar()) {
							castedRecordArguments[i].setValueClass(constructor.argsClasses[i]);
						}
					}
				}
			} else {
				validationInfo.error("inconvertible types; cannot cast " + clazz.getNameDescr() + " to Record", getToken());
				valid = false;
			}
		}

		// @unnamed
		if (castedVariableName != null && !UNNAMED.equals(castedVariableName)) {
			declarationNode = new NodeDeclaration(type, castedVariableName);
			declarationNode.setToken(token);
			valid &= ctx.addLocalVariable(declarationNode, true);
			ctx.initializedNodes.add(declarationNode);
		}

		if (castedCondition != null) {
			valid &= castedCondition.validate(validationInfo, ctx) && castedCondition.expectBooleanValue(validationInfo, ctx);
		}
		return valid;
	}

	@Override
	public void setValueClass(HiClass clazz) {
		super.setValueClass(clazz);
		type = Type.getType(clazz);
		if (declarationNode != null) {
			declarationNode.setValueClass(clazz);
		} else {
			declarationNode = new NodeDeclaration(type, castedVariableName);
			declarationNode.setToken(token);
		}
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
		return castedVariableName;
	}

	@Override
	public String getVariableType() {
		return declarationNode.getVariableType();
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.NAME;
		ctx.value.valueClass = clazz;
		ctx.value.name = type.cellTypeRoot != null ? type.cellTypeRoot.name : type.name;
		ctx.value.nameDimensions = type.getDimension();
		ctx.value.castedVariableName = castedVariableName;
		ctx.value.castedRecordArguments = castedRecordArguments;
		if (castedRecordArguments != null) { // Type<G,...>(A,...) name | Type(A,...) name | Type<G,...>(A,...) | Type(A,...)
			ctx.value.variableType = type;
		} else if (castedVariableName != null) { // Type<G,...> name | Type name | Type<G,...>[] name | Type[] name | var name
			ctx.value.variableType = type;
		} else if (type.parameters != null || type.getDimension() > 1) { // Type<G,...>, Type<G,...>[]
			ctx.value.variableType = type;
		} else {
			ctx.value.variableType = null;
		}
		ctx.value.castedCondition = castedCondition;
		ctx.value.castedRecordArgumentsConstructor = constructor;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeNullableUTF(castedVariableName);
		os.writeByte(castedRecordArguments != null ? castedRecordArguments.length : 0);
		os.writeNullable(castedRecordArguments);
		os.writeNullable(castedCondition);
		os.writeConstructor(constructor);
		os.writeClass(clazz);
	}

	public static NodeCastedIdentifier decode(DecodeContext os) throws IOException {
		NodeCastedIdentifier node = new NodeCastedIdentifier(os.readType(), os.readNullableUTF(), os.readNullableNodeArray(NodeCastedIdentifier.class, os.readByte()));
		node.castedCondition = os.readNullable(HiNode.class);
		os.readConstructor(constructor -> node.constructor = constructor);
		os.readClass(clazz -> node.clazz = clazz);
		return node;
	}
}
