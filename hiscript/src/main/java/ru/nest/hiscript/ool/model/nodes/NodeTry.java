package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;

import java.io.IOException;

public class NodeTry extends Node {
	public NodeTry(Node body, Node catchBody, Type excType, String excName, Node finallyBody) {
		super("try", TYPE_TRY);
		this.body = body;
		this.catchBody = catchBody;
		this.excType = excType;
		this.excName = excName.intern();
		this.finallyBody = finallyBody;
	}

	private Node body;

	private Node catchBody;

	private Type excType;

	private HiClass excClass;

	private String excName;

	private Node finallyBody;

	@Override
	public void execute(RuntimeContext ctx) {
		if (body != null) {
			ctx.enter(RuntimeContext.TRY, line);
			try {
				body.execute(ctx);
			} finally {
				ctx.exit();
			}
		}

		if (ctx.exception != null && !ctx.exception.clazz.name.equals("AssertException")) {
			HiObject exception = ctx.exception;
			if (excClass == null) {
				excClass = excType.getClass(ctx);
				if (exception != ctx.exception) {
					return;
				}
			}

			if (exception.clazz.isInstanceof(excClass)) {
				ctx.exception = null;

				if (catchBody != null) {
					ctx.enter(RuntimeContext.CATCH, line);

					HiFieldObject exc = (HiFieldObject) HiField.getField(excType, excName);
					exc.set(exception);
					exc.initialized = true;

					ctx.addVariable(exc);

					try {
						catchBody.execute(ctx);
					} finally {
						ctx.exit();
					}
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
		os.writeNullable(catchBody);
		os.writeType(excType);
		os.writeUTF(excName);
		os.writeNullable(finallyBody);
	}

	public static NodeTry decode(DecodeContext os) throws IOException {
		return new NodeTry(os.readNullable(Node.class), os.readNullable(Node.class), os.readType(), os.readUTF(), os.readNullable(Node.class));
	}
}
