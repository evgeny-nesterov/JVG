package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeAnnotation extends Node {
	public NodeAnnotation(String name, NodeAnnotationArgument[] args) {
		super("annotation", TYPE_ANNOTATION);
		this.name = name;
		this.args = args;
	}

	private String name;

	private NodeAnnotationArgument[] args;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		// TODO
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// TODO
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(args);
		os.writeShort(args != null ? args.length : 0);
		os.write(args);
	}

	public static NodeAnnotation decode(DecodeContext os) throws IOException {
		return new NodeAnnotation(os.readNullableUTF(), os.readNullableNodeArray(NodeAnnotationArgument.class, os.readShort()));
	}
}
