package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.classes.HiClassVar;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.util.Set;

public class NodeReturn extends HiNode {
	public NodeReturn(HiNode value) {
		super("return", TYPE_RETURN, true);
		this.value = value;
	}

	private final HiNode value;

	private HiClass returnClass;

	private Type returnType;

	@Override
	public NodeReturn getReturnNode() {
		return this;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		returnClass = value != null ? value.getValueClass(validationInfo, ctx) : HiClassPrimitive.VOID;
		ctx.nodeValueType.clazz = returnClass;
		ctx.nodeValueType.type = Type.getType(returnClass);
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		return returnClass;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		if (value != null) {
			valid &= value.validate(validationInfo, ctx) && value.expectValue(validationInfo, ctx);
		}

		CompileClassContext.CompileClassLevel localContextLevel = ctx.level.getLocalContextLevel();
		HiMethod method = null;
		if (localContextLevel != null) {
			if (localContextLevel.node instanceof HiMethod) {
				method = (HiMethod) localContextLevel.node;
			}
			valid &= validateReturn(validationInfo, ctx, method, value, token);

			ctx.level.terminate(localContextLevel);
		}
		return valid;
	}

	public static boolean validateLambdaReturn(ValidationInfo validationInfo, CompileClassContext ctx, HiNode value, Token token) {
		if (ctx.level.type == RuntimeContext.METHOD) {
			HiMethod method = (HiMethod) ctx.level.node;
			if (method.isLambda()) {
				if (value instanceof NodeBlock) {
					NodeReturn returnNode = value.getReturnNode();
					if (returnNode != null) {
						value = returnNode.value;
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
		HiClass expectedClass = HiClassPrimitive.VOID;
		if (method != null) {
			method.resolve(ctx);
			expectedClass = method.returnClass == null ? HiClassVar.VAR : method.returnClass;
		}
		if (value != null) {
			NodeValueType returnValueType = value.getNodeValueType(validationInfo, ctx);
			if (returnValueType.valid) {
				boolean match = false;
				if (expectedClass.isGeneric()) {
					// @generics
					HiClassGeneric dstGenericClass = (HiClassGeneric) expectedClass;
					if (returnValueType.clazz == dstGenericClass) {
						match = true;
					} else if (returnValueType.clazz.isGeneric()) {
						// TODO check
						HiClassGeneric srcGenericClass = (HiClassGeneric) returnValueType.clazz;
						if (srcGenericClass.sourceClass != dstGenericClass.sourceClass) {
							if (dstGenericClass.clazz.isInstanceof(srcGenericClass.clazz)) {
								match = true;
							}
						}
					}
				} else {
					match = HiClass.autoCast(ctx, returnValueType.clazz, expectedClass, returnValueType.isCompileValue(), true);
				}
				if (!match) {
					validationInfo.error("incompatible types; found " + returnValueType.clazz.getNameDescr() + ", required " + expectedClass.getNameDescr(), value);
					return false;
				}
			}
		} else if (expectedClass != HiClassPrimitive.VOID) {
			validationInfo.error("incompatible types; found " + HiClassPrimitive.VOID + ", required " + expectedClass.getNameDescr(), token);
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
					assert NodeIdentifier.resolveVariable(ctx, ctx.value);
				}
			} else {
				ctx.value.setVoidValue();
			}
		} finally {
			ctx.isReturn = true;
			ctx.exception = null;
		}
	}

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		return label == null;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(value);
		os.writeType(returnType);
		os.writeClass(returnClass);
	}

	public static NodeReturn decode(DecodeContext os) throws IOException {
		NodeReturn node = new NodeReturn(os.readNullable(HiNode.class));
		node.returnType = os.readType();
		os.readClass(clazz -> node.returnClass = clazz);
		return node;
	}
}
