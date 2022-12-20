package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeThrow extends HiNode {
	public NodeThrow(HiNode exception) {
		super("throw", TYPE_THROW);
		this.exception = exception;
	}

	private HiNode exception;

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
		return new NodeThrow(os.read(HiNode.class));
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
