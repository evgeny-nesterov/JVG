package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeAnnotationArgument extends HiNode {
	public NodeAnnotationArgument(String name, HiNode value, Token token) {
		super("annotationArgument", TYPE_ANNOTATION_ARGUMENT, token);
		this.name = name;
		this.value = value;
	}

	private String name;

	private HiNode value;

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
		return new NodeAnnotationArgument(os.readUTF(), os.read(HiNode.class), null);
	}
}
