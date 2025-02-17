package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;
import java.util.Set;

public class NodeSynchronized extends HiNode {
	public NodeSynchronized(HiNode lock, HiNode body) {
		super("synchronized", TYPE_SYNCHRONIZED, true);
		this.lock = lock;
		this.body = body;
	}

	private final HiNode lock;

	private final HiNode body;

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		return body != null && body.isReturnStatement(label, labels);
	}

	@Override
	public NodeReturn getReturnNode() {
		return body != null ? body.getReturnNode() : null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		ctx.enter(RuntimeContext.SYNCHRONIZED, this);
		valid &= lock.validate(validationInfo, ctx) && lock.expectObjectValue(validationInfo, ctx);
		if (body != null) {
			valid &= body.validateBlock(validationInfo, ctx);
		}
		ctx.exit();
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		lock.execute(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		HiClass clazz = ctx.value.valueClass;

		Object lockObject = null;
		if (clazz.isObject() || clazz.isArray()) {
			lockObject = ctx.value.object;
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
		return new NodeSynchronized(os.read(HiNode.class), os.readNullable(HiNode.class));
	}
}
