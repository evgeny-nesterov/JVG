package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;

import java.io.IOException;

public class NodeTry extends Node {
	public NodeTry(Node body, NodeCatch[] catches, Node finallyBody, NodeDeclaration[] resources) {
		super("try", TYPE_TRY);
		this.body = body;
		this.catches = catches;
		this.finallyBody = finallyBody;
		this.resources = resources;
	}

	private Node body;

	private NodeCatch[] catches;

	private Node finallyBody;

	private NodeDeclaration[] resources;

	@Override
	public void execute(RuntimeContext ctx) {
		if (resources != null) {
			// TODO init resources
		}

		boolean closeException = false;
		try {
			if (body != null || resources != null) {
				ctx.enter(RuntimeContext.TRY, line);
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
							ctx.throwException("RuntimeException", "Null pointer");
							return;
						}

						HiMethod closeMethod = resourceObject.clazz.searchMethod(ctx, "close");
						if (closeMethod == null) {
							ctx.throwException("RuntimeException", "'" + resource.name + "' is not auto closeable");
							return;
						}

						ctx.enterMethod(closeMethod, resourceObject, -1);
						try {
							closeMethod.invoke(ctx, HiClass.getPrimitiveClass("void"), resourceObject, null);
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
				int index = catchNode.getMatchedExceptionClass(ctx);
				if (index == -2) {
					// error occurred while catch exception class resolving
					return;
				}

				if (closeException) {
					ctx.exception = null;
					ctx.throwRuntimeException("can't catch close resource exception");
					break;
				}

				if (index >= 0) {
					catchNode.execute(ctx);
					break;
				}
			}
		}

		if (finallyBody != null) {
			ctx.enter(RuntimeContext.FINALLY, line);
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
		return new NodeTry(os.readNullable(Node.class), os.readNullableNodeArray(NodeCatch.class, os.readShort()), os.readNullable(Node.class), os.readNullableNodeArray(NodeDeclaration.class, os.readShort()));
	}
}
