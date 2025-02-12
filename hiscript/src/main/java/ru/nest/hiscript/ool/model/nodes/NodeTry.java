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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NodeTry extends HiNode {
	public NodeTry(HiNode body, NodeCatch[] catches, HiNode finallyBody, NodeDeclaration[] resources) {
		super("try", TYPE_TRY, true);
		this.resources = resources;
		this.body = body;
		this.catches = catches;
		this.finallyBody = finallyBody;
	}

	private final NodeDeclaration[] resources;

	private final HiNode body;

	public NodeCatch[] catches;

	private final HiNode finallyBody;

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		if (finallyBody != null && finallyBody.isReturnStatement(label, labels)) {
			return true;
		}
		if (body == null || !body.isReturnStatement(label, labels)) {
			return false;
		}
		if (catches != null) {
			for (NodeCatch catchNode : catches) {
				if (!catchNode.isReturnStatement(label, labels)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public NodeReturn getReturnNode() {
		NodeReturn returnNode = null;
		if (finallyBody != null) {
			returnNode = finallyBody.getReturnNode();
		}
		if (returnNode == null && body != null) {
			returnNode = body.getReturnNode();
		}
		if (returnNode == null && catches != null) {
			for (NodeCatch catchNode : catches) {
				returnNode = catchNode.getReturnNode();
				if (returnNode != null) {
					break;
				}
			}
		}
		return returnNode;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		ctx.enter(RuntimeContext.TRY, this);
		if (resources != null) {
			for (NodeDeclaration resource : resources) {
				valid &= resource.validate(validationInfo, ctx);
				HiClass resourceClass = resource.getValueClass(validationInfo, ctx);
				if (!resourceClass.isInstanceof("AutoCloseable")) {
					validationInfo.error("incompatible types: found: '" + resourceClass.getNameDescr() + "', required: 'AutoCloseable'", resource.getToken());
					valid = false;
				}
			}
		}
		if (body != null) {
			valid &= body.validateBlock(validationInfo, ctx);
		}
		if (catches != null) {
			List<HiClass> excClasses = new ArrayList<>(1);
			for (NodeCatch catchNode : catches) {
				valid &= catchNode.validateBlock(validationInfo, ctx);
				if (catchNode.excClass != null) {
					for (HiClass prevExcClass : excClasses) {
						if (catchNode.excClass.isInstanceof(prevExcClass)) {
							validationInfo.error("exception '" + catchNode.excClass.getNameDescr() + "' has already been caught", catchNode.getToken());
							valid = false;
						}
					}
					excClasses.add(catchNode.excClass);
				}
			}
		}
		if (finallyBody != null) {
			valid &= finallyBody.validateBlock(validationInfo, ctx);
		}
		ctx.exit();
		checkStatementTermination(ctx);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		int initializedResources = -1;
		try {
			if (body != null || resources != null) {
				ctx.enter(RuntimeContext.TRY, token);
			}

			if (resources != null) {
				for (int i = 0; i < resources.length; i++) {
					resources[i].execute(ctx);
					if (ctx.exception != null) {
						break;
					}
					initializedResources = i;
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
					boolean closeException = false;
					for (int i = 0; i <= initializedResources; i++) {
						NodeDeclaration resource = resources[i];
						HiFieldObject resourceField = (HiFieldObject) ctx.getVariable(resource.name);
						HiObject resourceObject = (HiObject) resourceField.get();
						if (resourceObject == null) {
							ctx.throwRuntimeException("null pointer");
							return;
						}

						// supposed resourceObject.clazz.isInstanceof(HiClass.AUTOCLOSEABLE_CLASS_NAME)
						HiMethod closeMethod = resourceObject.clazz.searchMethod(ctx, "close");
						assert closeMethod != null;

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
