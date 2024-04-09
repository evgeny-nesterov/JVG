package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.classes.HiClassVar;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeReturn extends HiNode {
	public NodeReturn(HiNode value) {
		super("return", TYPE_RETURN);
		this.value = value;
	}

	private final HiNode value;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (value != null) {
			valid = value.validate(validationInfo, ctx) && value.expectValue(validationInfo, ctx);
		}

		CompileClassContext.CompileClassLevel level = ctx.level;
		HiMethod method = null;
		while (level != null) {
			if (level.type == RuntimeContext.METHOD) {
				method = (HiMethod) level.node;
				break;
			}
			level = level.parent;
		}
		valid &= validateReturn(validationInfo, ctx, method, value, token);
		return valid;
	}

	public static boolean validateLambdaReturn(ValidationInfo validationInfo, CompileClassContext ctx, HiNode value, Token token) {
		if (ctx.level.type == RuntimeContext.METHOD) {
			HiMethod method = (HiMethod) ctx.level.node;
			if (method.isLambda()) {
				if (value instanceof NodeBlock) {
					NodeBlock block = (NodeBlock) value;
					if (block.statements.size() > 0) {
						value = block.statements.get(block.statements.size() - 1);
						if (value instanceof NodeReturn) {
							value = ((NodeReturn) value).value;
						} else {
							value = EmptyNode.getInstance();
						}
					} else {
						value = EmptyNode.getInstance();
					}
				}
				return NodeReturn.validateReturn(validationInfo, ctx, method, value, token);
			}
		}
		return true;
	}

	public static boolean validateReturn(ValidationInfo validationInfo, CompileClassContext ctx, HiMethod method, HiNode value, Token token) {
		HiClass expectedType = HiClassPrimitive.VOID;
		if (method != null) {
			method.resolve(ctx);
			expectedType = method.returnClass == null ? HiClassVar.VAR : method.returnClass;
		}
		if (value != null) {
			NodeValueType returnValueType = value.getValueType(validationInfo, ctx);
			if (returnValueType.valid && !HiClass.autoCast(ctx, returnValueType.type, expectedType, returnValueType.isCompileValue(), true)) {
				validationInfo.error("incompatible types; found " + returnValueType.type + ", required " + expectedType, value.getToken());
				return false;
			}
		} else if (expectedType != HiClassPrimitive.VOID) {
			validationInfo.error("incompatible types; found " + HiClassPrimitive.VOID + ", required " + expectedType, token);
			return false;
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
						if (ctx.value.nameDimensions == 0) {
							ctx.throwRuntimeException("cannot resolve variable " + ctx.value.name);
						} else {
							ctx.throwRuntimeException("cannot resolve class " + ctx.value.name);
						}
						return;
					}
				}

				// TODO: check on void return value
			} else {
				ctx.value.valueType = Value.VALUE;
				ctx.value.type = HiClassPrimitive.VOID;
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
