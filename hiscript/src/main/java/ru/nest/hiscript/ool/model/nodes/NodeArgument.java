package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.TypeArgumentIF;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeArgument extends HiNode implements NodeVariable, HasModifiers {
	public NodeArgument(TypeArgumentIF typeArgument, String name, Modifiers modifiers, NodeAnnotation[] annotations) {
		super("argument", TYPE_ARGUMENT, false);
		this.typeArgument = typeArgument;
		this.name = name.intern();
		this.modifiers = modifiers;
		this.annotations = annotations;
	}

	public TypeArgumentIF typeArgument;

	public Type getType() {
		return typeArgument.getType();
	}

	public String getTypeName() {
		return typeArgument.getName();
	}

	public boolean isVarargs() {
		return typeArgument.isVarargs();
	}

	public String name;

	private Modifiers modifiers;

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	public NodeAnnotation[] annotations;

	public HiClass clazz;

	public HiClass getArgClass() {
		return clazz;
	}

	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.type = getType();
		return getType().getClass(ctx);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		// TODO check modifiers (access)
		boolean valid = HiNode.validateAnnotations(validationInfo, ctx, annotations);
		Type type = typeArgument.getType();
		clazz = type.getClass(ctx);
		if (type.isWildcard()) {
			validationInfo.error("invalid argument type", getToken());
			valid = false;
		}

		// generics
		if (type.parameters != null) {
			if (type.parameters.length > 0) {
				valid &= type.validateClass(clazz, validationInfo, ctx, getToken());
			} else {
				validationInfo.error("type parameter expected", getToken());
				valid = false;
			}
		}

		valid &= clazz != null;
		valid &= ctx.addLocalVariable(this, false);
		ctx.initializedNodes.add(this);
		return valid;
	}

	// TODO unused!
	@Override
	public void execute(RuntimeContext ctx) {
		// TODO keep in field only runtime annotations
		HiField<?> field = HiField.getField(clazz, name, null);
		field.setModifiers(modifiers);
		field.declared = true;
		field.initialized = true;

		ctx.addVariable(field);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeTypeArgument(typeArgument);
		os.writeUTF(name);
		modifiers.code(os);
		os.writeShortArray(annotations);
	}

	public static NodeArgument decode(DecodeContext os) throws IOException {
		return new NodeArgument(os.readTypeArgument(), os.readUTF(), Modifiers.decode(os), os.readShortNodeArray(NodeAnnotation.class));
	}

	@Override
	public String getVariableName() {
		return name;
	}

	@Override
	public String getVariableType() {
		return typeArgument.getName();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof NodeArgument && ((NodeArgument) o).name.equals(name);
	}
}
