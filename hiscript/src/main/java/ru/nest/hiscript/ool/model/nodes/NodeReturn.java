package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeReturn extends Node {
	public NodeReturn(Node value) {
		super("return", TYPE_RETURN);
		this.value = value;
	}

	private Node value;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (value != null) {
			return value.validate(validationInfo, ctx);
		}
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		try {
			if (value != null) {
				value.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}

				if (ctx.value.valueType == Value.NAME) {
					if (!NodeIdentifier.resolveVariable(ctx, ctx.value, true)) {
						ctx.throwRuntimeException("can't resolve variable " + ctx.value.name);
						return;
					}
				}

				// TODO: check on void return value
			} else {
				ctx.value.valueType = Value.VALUE;
				ctx.value.type = HiClass.getPrimitiveClass("void");
			}
		} finally {
			ctx.isReturn = true;
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(value);
	}

	public static NodeReturn decode(DecodeContext os) throws IOException {
		return new NodeReturn(os.readNullable(Node.class));
	}
}
