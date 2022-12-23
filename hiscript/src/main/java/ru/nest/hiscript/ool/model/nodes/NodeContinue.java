package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeContinue extends HiNode {
	public NodeContinue(String label) {
		super("continue", TYPE_CONTINUE);
		this.label = label; // != null ? label.intern() : null;
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

	@Override
	public boolean isTerminal() {
		return true;
	}
}
