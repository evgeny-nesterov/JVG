package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.Set;

public class NodeBreak extends HiNode {
	public NodeBreak(String label) {
		super("break", TYPE_BREAK, false);
		this.label = label; // != null ? label.intern() : null;
	}

	private final String label;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		CompileClassContext.CompileClassLevel breakLevel = ctx.level.getBreakLevel(label);
		if (breakLevel == null) {
			if (label != null) {
				validationInfo.error("undefined label '" + label + "'", token);
			} else {
				validationInfo.error("break outside switch or loop", token);
			}
			valid = false;
		} else {
			ctx.level.terminate(breakLevel);
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.isBreak = true;
		ctx.label = label;
	}

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		if (labels != null) {
			labels.add(this.label != null ? this.label : "");
		}
		if (label != null) {
			if (this.label == null) {
				return label.equals("");
			} else {
				return this.label.equals(label);
			}
		}
		return false;
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
