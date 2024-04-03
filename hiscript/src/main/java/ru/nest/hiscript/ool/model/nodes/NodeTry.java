package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeTry extends HiNode {
	public NodeTry(HiNode body, NodeCatch[] catches, HiNode finallyBody, NodeDeclaration[] resources) {
		super("try", TYPE_TRY);
		this.resources = resources;
		this.body = body;
		this.catches = catches;
		this.finallyBody = finallyBody;
	}

	private NodeDeclaration[] resources;

	private HiNode body;

	public NodeCatch[] catches;

	private HiNode finallyBody;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		ctx.enter(RuntimeContext.TRY, this);
		if (resources != null) {
			for (NodeDeclaration resource : resources) {
				valid &= resource.validate(validationInfo, ctx);
				HiClass resourceClass = resource.getValueClass(validationInfo, ctx);
				if (!resourceClass.isInstanceof("AutoCloseable")) {
					validationInfo.error("incompatible types: try-with-resources not applicable to variable type", resource.getToken());
					valid = false;
				}
			}
		}
		if (body != null) {
			valid &= body.validateBlock(validationInfo, ctx);
		}
		if (catches != null) {
			for (NodeCatch catchNode : catches) {
				valid &= catchNode.validateBlock(validationInfo, ctx);
			}
		}
		if (finallyBody != null) {
			valid &= finallyBody.validateBlock(validationInfo, ctx);
		}
		ctx.exit();
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		if (resources != null) {
			// TODO init resources
		}

		boolean closeException = false;
		try {
			if (body != null || resources != null) {
				ctx.enter(RuntimeContext.TRY, token);
			}

			if (resources != null) {
				for (NodeDeclaration resource : resources) {
					resource.execute(ctx);
					if (ctx.exception != null) {
						break;
					}
				}
			}

			if (body != null && ctx.exception == null) {
				body.execute(ctx);
			}
		} finally {
			if (resources != null) {
				try {
					HiObject initialException = ctx.exception;
					ctx.exception = null;
					for (NodeDeclaration resource : resources) {
						HiFieldObject resourceField = (HiFieldObject) ctx.getVariable(resource.name);
						HiObject resourceObject = resourceField.get();
						if (resourceObject == null) {
							ctx.throwRuntimeException("null pointer");
							return;
						}

						HiMethod closeMethod = resourceObject.clazz.searchMethod(ctx, "close");
						if (closeMethod == null) {
							ctx.throwRuntimeException("'" + resource.name + "' is not auto closeable");
							return;
						}

						ctx.enterMethod(closeMethod, resourceObject);
						try {
							closeMethod.invoke(ctx, HiClassPrimitive.VOID, resourceObject, null);
						} finally {
							ctx.exit();
							ctx.isReturn = false;
						}

						if (ctx.exception != null) {
							closeException = true;
							break;
						}
					}
					if (!closeException) {
						ctx.exception = initialException;
					}
				} finally {
					ctx.exit();
				}
			} else if (body != null) {
				ctx.exit();
			}
		}

		if (ctx.exception != null && !ctx.exception.clazz.name.equals("AssertException") && catches != null) {
			for (NodeCatch catchNode : catches) {
				if (closeException) {
					ctx.exception = null;
					ctx.throwRuntimeException("cannot catch close resource exception");
					break;
				}

				if (ctx.exception.clazz.isInstanceof(catchNode.excClass)) {
					catchNode.execute(ctx);
					break;
				}
			}
		}

		if (finallyBody != null) {
			ctx.enter(RuntimeContext.FINALLY, token);
			try {
				finallyBody.execute(ctx);
			} finally {
				ctx.exit();
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(body);
		os.writeShort(catches != null ? catches.length : 0);
		os.writeNullable(catches);
		os.writeNullable(finallyBody);
		os.writeShort(resources != null ? resources.length : 0);
		os.writeNullable(resources);
	}

	public static NodeTry decode(DecodeContext os) throws IOException {
		return new NodeTry(os.readNullable(HiNode.class), os.readNullableNodeArray(NodeCatch.class, os.readShort()), os.readNullable(HiNode.class), os.readNullableNodeArray(NodeDeclaration.class, os.readShort()));
	}
}
