package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Field;
import script.ool.model.Modifiers;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Type;

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
		Field<?> field = Field.getField(type, name, null);
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
