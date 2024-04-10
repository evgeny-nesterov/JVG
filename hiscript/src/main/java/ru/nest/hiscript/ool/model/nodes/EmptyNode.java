package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

/**
 * This type of node presents any empty statement, for example, statement ';'.
 */
public class EmptyNode extends HiNode {
	private final static EmptyNode instance = new EmptyNode();

	public static EmptyNode getInstance() {
		return instance;
	}

	private EmptyNode() {
		super("empty", TYPE_EMPTY, false);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		return NodeReturn.validateLambdaReturn(validationInfo, ctx, this, token);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// do nothing
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static EmptyNode decode(DecodeContext os) {
		return instance;
	}
}
