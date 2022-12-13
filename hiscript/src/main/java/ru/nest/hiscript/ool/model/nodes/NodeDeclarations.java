package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeDeclarations extends Node {
	public NodeDeclarations() {
		super("declarations", TYPE_DECLARATIONS);
	}

	public NodeDeclaration add(Type type, String name, Node initializer, Modifiers modifiers) {
		NodeDeclaration field = new NodeDeclaration(type, name, initializer, modifiers);
		declarations.add(field);
		return field;
	}

	private List<NodeDeclaration> declarations = new ArrayList<>();

	@Override
	public void execute(RuntimeContext ctx) {
		int size = declarations.size();
		for (int i = 0; i < size; i++) {
			NodeDeclaration declaration = declarations.get(i);
			declaration.execute(ctx);

			if (ctx.exitFromBlock()) {
				return;
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeShort(declarations.size());
		os.write(declarations);
	}

	public static NodeDeclarations decode(DecodeContext os) throws IOException {
		NodeDeclarations node = new NodeDeclarations();
		node.declarations = os.readNodesList(NodeDeclaration.class, os.readShort());
		return node;
	}
}
