package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.TypeArgumentIF;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;

public class NodeArgument extends HiNode implements NodeVariable, HasModifiers {
	public NodeArgument(TypeArgumentIF typeArgument, String name, Modifiers modifiers, NodeAnnotation[] annotations) {
		super("argument", TYPE_ARGUMENT, false);
		this.typeArgument = typeArgument;
		this.name = name.intern();
		this.modifiers = modifiers != null ? modifiers : new Modifiers();
		this.annotations = annotations;
	}

	public TypeArgumentIF typeArgument;

	public String name;

	private Modifiers modifiers;

	public NodeAnnotation[] annotations;

	public HiClass clazz;

	public Type getType() {
		return typeArgument.getType();
	}

	public String getTypeName() {
		return typeArgument.getName();
	}

	public boolean isVarargs() {
		return typeArgument.isVarargs();
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	public HiClass getArgClass(ClassResolver classResolver) {
		if (clazz == null) {
			clazz = getType().getClass(classResolver);
		}
		return clazz;
	}

	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		if (clazz == null) {
			ctx.nodeValueType.type = getType();
			clazz = getType().getClass(ctx);
		} else {
			ctx.nodeValueType.type = Type.getType(clazz);
		}
		return clazz;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		// TODO check modifiers (access)
		boolean valid = HiNode.validateAnnotations(validationInfo, ctx, annotations);
		Type type = typeArgument.getType();
		clazz = type.getClass(ctx);
		if (type.isWildcard()) {
			validationInfo.error("invalid argument type", getToken());
			valid = false;
		}

		// @generics
		if (type.parameters != null) {
			if (type.parameters.length > 0) {
				valid &= type.validateClass(clazz, validationInfo, ctx, getToken());
			} else {
				validationInfo.error("type parameter expected", getToken());
				valid = false;
			}
		}

		valid &= clazz != null;
		valid &= ctx.addLocalVariable(this, ctx.level.isInsideBlock());
		ctx.initializedNodes.add(this);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// do nothing
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		// TODO keep in field only runtime annotations
		os.writeTypeArgument(typeArgument);
		os.writeUTF(name);
		modifiers.code(os);
		os.writeShortArray(annotations);
		os.writeClass(clazz);
	}

	public static NodeArgument decode(DecodeContext os) throws IOException {
		NodeArgument node = new NodeArgument(os.readTypeArgument(), os.readUTF(), Modifiers.decode(os), os.readShortNodeArray(NodeAnnotation.class));
		os.readClass(clazz -> node.clazz = clazz);
		return node;
	}

	@Override
	public String getVariableName() {
		return name;
	}

	@Override
	public Type getVariableType() {
		return typeArgument.getType();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof NodeArgument && ((NodeArgument) o).name.equals(name);
	}
}
