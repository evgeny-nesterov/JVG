package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeReturn extends HiNode {
	public NodeReturn(HiNode value) {
		super("return", TYPE_RETURN);
		this.value = value;
	}

	private HiNode value;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = value != null ? value.validate(validationInfo, ctx) && value.expectValue(validationInfo, ctx) : true;

		CompileClassContext.CompileClassLevel level = ctx.level;
		HiClass expectedType = HiClassPrimitive.VOID;
		while (level != null) {
			if (level.type == RuntimeContext.METHOD) {
				HiMethod method = (HiMethod) level.node;
				method.resolve(ctx);
				expectedType = method.returnClass;
				break;
			}
			level = level.parent;
		}

		if (value != null) {
			NodeValueType returnValueType = value.getValueType(validationInfo, ctx);
			if (returnValueType.valid && !HiClass.autoCast(returnValueType.type, expectedType, returnValueType.isValue)) {
				validationInfo.error("incompatible types; found " + returnValueType.type + ", required " + expectedType, value != null ? value.getToken() : getToken());
				valid = false;
			}
		}
		return valid;
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
						if (ctx.value.nameDimensions == 0) {
							ctx.throwRuntimeException("Can't resolve variable " + ctx.value.name);
						} else {
							ctx.throwRuntimeException("Can't resolve class " + ctx.value.name);
						}
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
		return new NodeReturn(os.readNullable(HiNode.class));
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
