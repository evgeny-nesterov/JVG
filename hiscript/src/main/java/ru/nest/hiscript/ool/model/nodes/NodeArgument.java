package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.TypeArgumentIF;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeArgument extends Node implements NodeVariable {
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

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		// TODO check
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		// TODO check type, name, modifiers, annotations
		boolean valid = ctx.addLocalVariable(this, validationInfo);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// TODO keep in field only runtime annotations
		HiField<?> field = HiField.getField(typeArgument.getType(), name, null);
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
		os.writeShort(annotations != null ? annotations.length : 0);
		os.write(annotations);
	}

	public static NodeArgument decode(DecodeContext os) throws IOException {
		return new NodeArgument(os.readTypeArgument(), os.readUTF(), Modifiers.decode(os), os.readNodeArray(NodeAnnotation.class, os.readShort()));
	}

	@Override
	public String getVariableName() {
		return name;
	}

	@Override
	public String getVariableType() {
		return typeArgument.getName();
	}
}
