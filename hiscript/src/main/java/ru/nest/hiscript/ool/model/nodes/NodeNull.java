package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeNull extends HiNode {
	public final static NodeNull instance = new NodeNull();

	private NodeNull() {
		super("null", TYPE_NULL);
	}

	@Override
	protected NodeValueType computeValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassNull.NULL;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassNull.NULL;
		ctx.value.object = null;
		ctx.value.array = null;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeNull decode(DecodeContext os) throws IOException {
		return instance;
	}
}
