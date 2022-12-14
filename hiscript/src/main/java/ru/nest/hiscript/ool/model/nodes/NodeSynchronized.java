package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

public class NodeSynchronized extends Node {
	public NodeSynchronized(Node lock, Node body) {
		super("synchronized", TYPE_SYNCHRONIZED);
		this.lock = lock;
		this.body = body;
	}

	private Node lock;

	private Node body;

	@Override
	public void execute(RuntimeContext ctx) {
		lock.execute(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		HiClass clazz = ctx.value.type;
		if (clazz.isNull()) {
			ctx.throwRuntimeException("null pointer");
			return;
		}

		Object lockObject;
		if (clazz.isObject()) {
			lockObject = ctx.value.object;
		} else if (clazz.isArray()) {
			lockObject = ctx.value.array;
		} else {
			ctx.throwRuntimeException("object is expected");
			return;
		}

		if (lockObject == null) {
			ctx.throwRuntimeException("null pointer");
			return;
		}

		if (body != null) {
			ctx.enter(RuntimeContext.TRY, token);
			try {
				synchronized (lockObject) {
					body.execute(ctx);
				}
			} finally {
				ctx.exit();
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.write(lock);
		os.writeNullable(body);
	}

	public static NodeSynchronized decode(DecodeContext os) throws IOException {
		return new NodeSynchronized(os.read(Node.class), os.readNullable(Node.class));
	}
}
