package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;

public class NodeArgument extends Node implements NodeVariable {
	public NodeArgument(Type type, String name, Modifiers modifiers) {
		super("argument", TYPE_ARGUMENT);
		this.type = type;
		this.name = name.intern();
		this.modifiers = modifiers;
	}

	public Type type;

	public String name;

	public Modifiers modifiers;

	@Override
	public void execute(RuntimeContext ctx) {
		HiField<?> field = HiField.getField(type, name, null);
		field.setModifiers(modifiers);
		field.declared = true;
		field.initialized = true;

		ctx.addVariable(field);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeUTF(name);
		modifiers.code(os);
	}

	public static NodeArgument decode(DecodeContext os) throws IOException {
		return new NodeArgument(os.readType(), os.readUTF(), Modifiers.decode(os));
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
