package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.lang.reflect.Array;

public class NodeForIterator extends Node {
	public NodeForIterator(NodeDeclaration declaration, Node iterable, Node body) {
		super("for", TYPE_FOR_ITERATOR);
		this.declaration = declaration;
		this.iterable = iterable;
		this.body = body;
	}

	private NodeDeclaration declaration;

	private Node iterable;

	private Node body;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(RuntimeContext.FOR);
		boolean valid = declaration.validate(validationInfo, ctx);
		valid &= iterable.validate(validationInfo, ctx);
		if (body != null) {
			valid &= body.validateBlock(validationInfo, ctx);
		}
		ctx.exit();
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.enter(RuntimeContext.FOR, token);
		try {
			declaration.execute(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}

			iterable.execute(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}

			if (!ctx.value.type.isArray()) {
				ctx.throwRuntimeException("not iterable argument");
			}

			if (body != null) {
				HiField<?> forVariable = ctx.getVariable(declaration.name);
				int size = Array.getLength(ctx.value.array);
				for (int i = 0; i < size; i++) {
					Object value = Array.get(ctx.value.array, i); // TODO primitives
					ctx.value.set(value);
					forVariable.set(ctx, ctx.value);
					forVariable.initialized = true;

					body.execute(ctx);
					if (ctx.exitFromBlock()) {
						return;
					}

					if (ctx.isBreak || (ctx.isContinue && !ctx.isCurrentLabel())) {
						break;
					}
				}
			}
		} finally {
			ctx.exit();
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(declaration);
		os.writeNullable(iterable);
		os.writeNullable(body);
	}

	public static NodeForIterator decode(DecodeContext os) throws IOException {
		return new NodeForIterator((NodeDeclaration) os.readNullable(Node.class), os.readNullable(Node.class), os.readNullable(Node.class));
	}
}
