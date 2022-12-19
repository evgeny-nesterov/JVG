package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeAnnotation extends HiNode {
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
		os.writeUTF(name);
		os.writeShortArray(args);
	}

	public static NodeAnnotation decode(DecodeContext os) throws IOException {
		return new NodeAnnotation(os.readUTF(), os.readShortNodeArray(NodeAnnotationArgument.class));
	}
}
