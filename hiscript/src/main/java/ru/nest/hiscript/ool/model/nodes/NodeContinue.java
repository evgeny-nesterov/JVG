package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class NodeContinue extends Node {
	public NodeContinue(String label) {
		super("continue", TYPE_CONTINUE);
		this.label = label != null ? label.intern() : null;
	}

	private String label;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		// TODO label
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.isContinue = true;
		ctx.label = label;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullableUTF(label);
	}

	public static NodeContinue decode(DecodeContext os) throws IOException {
		return new NodeContinue(os.readNullableUTF());
	}
}
