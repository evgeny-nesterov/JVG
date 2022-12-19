package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeBreak extends Node {
	public NodeBreak(String label) {
		super("break", TYPE_BREAK);
		this.label = label != null ? label.intern() : null;
	}

	private String label;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (label != null) {
			CompileClassContext.CompileClassLevel level = ctx.level;
			boolean found = false;
			while (level != null) {
				if (level.isLabel(label)) {
					found = true;
					break;
				}
				level = level.parent;
			}
			if (!found) {
				validationInfo.error("undefined label '" + label + "'", token);
				return false;
			}
		}
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.isBreak = true;
		ctx.label = label;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullableUTF(label);
	}

	public static NodeBreak decode(DecodeContext os) throws IOException {
		return new NodeBreak(os.readNullableUTF());
	}
}
