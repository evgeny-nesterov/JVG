package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeAnnotationArgument extends Node {
	public NodeAnnotationArgument(String name, Node value) {
		super("annotationArgument", TYPE_ANNOTATION_ARGUMENT);
		this.name = name;
		this.value = value;
	}

	private String name;

	private Node value;

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
		os.write(value);
	}

	public static NodeAnnotationArgument decode(DecodeContext os) throws IOException {
		return new NodeAnnotationArgument(os.readNullableUTF(), os.read(Node.class));
	}
}
