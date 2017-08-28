package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Field;
import script.ool.model.Modifiers;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Type;

public class NodeDeclaration extends Node implements NodeVariable {
	public NodeDeclaration(Type type, String name, Node initialization, Modifiers modifiers) {
		super("declaration", TYPE_DECLARATION);
		this.type = type;
		this.name = name.intern();
		this.initialization = initialization;
		this.modifiers = modifiers;
	}

	public Type type;

	public String name;

	public Node initialization;

	public Modifiers modifiers;

	public void execute(RuntimeContext ctx) {
		Field<?> field = Field.getField(type, name, initialization);
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

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeUTF(name);
		os.writeNullable(initialization);
		modifiers.code(os);
	}

	public static NodeDeclaration decode(DecodeContext os) throws IOException {
		return new NodeDeclaration(os.readType(), os.readUTF(), os.readNullable(Node.class), Modifiers.decode(os));
	}

	public String getVariableName() {
		return name;
	}

	public String getVariableType() {
		return type.name;
	}
}
