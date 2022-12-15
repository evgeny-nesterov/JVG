package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeThrow extends Node {
	public NodeThrow(Node exception) {
		super("throw", TYPE_THROW);
		this.exception = exception;
	}

	private Node exception;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		return exception.validate(validationInfo, ctx);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		exception.execute(ctx);
		ctx.exception = ctx.value.getObject();
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		exception.code(os);
	}

	public static NodeThrow decode(DecodeContext os) throws IOException {
		return new NodeThrow(os.read(Node.class));
	}
}
