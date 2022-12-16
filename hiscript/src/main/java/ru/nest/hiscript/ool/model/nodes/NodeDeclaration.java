package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeDeclaration extends Node implements NodeVariable {
	public NodeDeclaration(Type type, String name, Node initialization, Modifiers modifiers, NodeAnnotation[] annotations) {
		super("declaration", TYPE_DECLARATION);
		this.type = type;
		this.name = name.intern();
		this.initialization = initialization;
		this.modifiers = modifiers;
		this.annotations = annotations;
	}

	public NodeDeclaration(String typeName, String name) {
		super("declaration", TYPE_DECLARATION);
		this.type = Type.getTypeByFullName(typeName);
		this.name = name.intern();
	}

	public Type type;

	public String name;

	public Node initialization;

	public Modifiers modifiers;

	public NodeAnnotation[] annotations;

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass clazz = ctx.getClass(type.fullName);
		if (clazz == null) {
			clazz = type.getClass(null);
		}
		return clazz;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (initialization != null) {
			valid = initialization.validate(validationInfo, ctx);
		}
		// TODO check type, name, modifiers, annotations
		valid &= ctx.addLocalVariable(this, validationInfo);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// TODO keep in field only runtime annotations
		HiField<?> field = HiField.getField(type, name, initialization);
		field.setModifiers(modifiers);

		ctx.addVariable(field);

		try {
			field.execute(ctx);
		} finally {
			if (ctx.exitFromBlock()) {
				return;
			}
			field.initialized = initialization != null;
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeUTF(name);
		os.writeNullable(initialization);
		modifiers.code(os);
		os.writeShort(annotations != null ? annotations.length : 0);
		os.write(annotations);
	}

	public static NodeDeclaration decode(DecodeContext os) throws IOException {
		return new NodeDeclaration(os.readType(), os.readUTF(), os.readNullable(Node.class), Modifiers.decode(os), os.readNodeArray(NodeAnnotation.class, os.readShort()));
	}

	@Override
	public String getVariableName() {
		return name;
	}

	@Override
	public String getVariableType() {
		return type.name;
	}
}
