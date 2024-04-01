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

public class NodeArgument extends HiNode implements NodeVariable {
	public NodeArgument(TypeArgumentIF typeArgument, String name, Modifiers modifiers, NodeAnnotation[] annotations) {
		super("argument", TYPE_ARGUMENT);
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

	public Modifiers modifiers;

	public NodeAnnotation[] annotations;

	private HiClass clazz;

	public HiClass getArgClass() {
		return clazz;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return getType().getClass(ctx);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		// TODO check modifiers (access)
		boolean valid = HiNode.validateAnnotations(validationInfo, ctx, annotations);
		clazz = typeArgument.getType().getClass(ctx);
		valid &= clazz != null;
		valid &= ctx.addLocalVariable(this);
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
