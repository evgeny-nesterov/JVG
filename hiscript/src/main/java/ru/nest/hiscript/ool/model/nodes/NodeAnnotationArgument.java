package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeAnnotationArgument extends HiNode {
	public NodeAnnotationArgument(String name, HiNode valueNode, Token token) {
		super("annotationArgument", TYPE_ANNOTATION_ARGUMENT, token, false);
		this.name = name;
		this.valueNode = valueNode;
	}

	private NodeAnnotationArgument(String name, Object value) {
		super("annotationArgument", TYPE_ANNOTATION_ARGUMENT, null, false);
		this.name = name;
		this.value = value;
	}

	public String name;

	public HiNode valueNode; // only for validation

	private Object value;

	@Override
	public NodeValueType getNodeValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		return valueNode.getNodeValueType(validationInfo, ctx);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = valueNode.validate(validationInfo, ctx) && valueNode.expectConstant(validationInfo, ctx);
		if (valid) {
			value = valueNode.getObjectValue(validationInfo, ctx, getToken());
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// do nothing
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(name);
		os.writeObject(value);
	}

	public static NodeAnnotationArgument decode(DecodeContext os) throws IOException {
		return new NodeAnnotationArgument(os.readUTF(), os.readObject());
	}
}
