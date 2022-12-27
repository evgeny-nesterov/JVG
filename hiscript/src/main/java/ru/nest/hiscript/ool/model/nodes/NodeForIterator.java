package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

public class NodeForIterator extends HiNode {
	public NodeForIterator(NodeDeclaration declaration, HiNode iterable, HiNode body) {
		super("for", TYPE_FOR_ITERATOR);
		this.declaration = declaration;
		this.iterable = iterable;
		this.body = body;
	}

	private NodeDeclaration declaration;

	private HiNode iterable;

	private HiNode body;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(RuntimeContext.FOR, this);
		boolean valid = declaration.validate(validationInfo, ctx);
		if (declaration.modifiers.hasModifiers()) {
			validationInfo.error("modifiers not allowed", declaration.getToken());
			valid = false;
		}
		ctx.initializedNodes.add(declaration);
		if (iterable.validate(validationInfo, ctx) && iterable.expectIterableValue(validationInfo, ctx)) {
			HiClass declarationClass = declaration.getValueClass(validationInfo, ctx);
			HiClass iterableClass = iterable.getValueClass(validationInfo, ctx);
			if (iterableClass.isArray()) {
				HiClass cellClass = ((HiClassArray) iterableClass).cellClass;
				if (!HiClass.autoCast(cellClass, declarationClass, false)) {
					validationInfo.error("incompatible types: " + cellClass + " cannot be converted to " + declarationClass, token);
					valid = false;
				}
			} else {
				// TODO ArrayList
			}
		} else {
			valid = false;
		}
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

			if (ctx.value.type.isArray()) {
				HiField<?> forVariable = ctx.getVariable(declaration.name);
				Object array = ctx.value.array;
				int size = Array.getLength(array);
				for (int i = 0; i < size; i++) {
					Object value = Array.get(array, i); // TODO primitives
					if (!executeValue(ctx, forVariable, value)) {
						break;
					}
				}
			} else if (ctx.value.type.isInstanceof(HiClass.ARRAYLIST_CLASS_NAME)) { // TODO isInstanceof Iterable
				HiField<?> forVariable = ctx.getVariable(declaration.name);
				List list = (List) ctx.value.object.userObject;
				for (Object value : list) {
					if (!executeValue(ctx, forVariable, value)) {
						break;
					}
				}
			} else {
				ctx.throwRuntimeException("not iterable argument");
			}
		} finally {
			ctx.exit();
		}
	}

	private boolean executeValue(RuntimeContext ctx, HiField<?> forVariable, Object value) {
		ctx.value.set(value);
		forVariable.set(ctx, ctx.value);
		forVariable.initialized = true;

		if (body != null) {
			body.execute(ctx);
			if (ctx.exitFromBlock()) {
				return false;
			}
		}

		if (ctx.isBreak || (ctx.isContinue && !ctx.isCurrentLabel())) {
			return false;
		}
		return true;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(declaration);
		os.writeNullable(iterable);
		os.writeNullable(body);
	}

	public static NodeForIterator decode(DecodeContext os) throws IOException {
		return new NodeForIterator((NodeDeclaration) os.readNullable(HiNode.class), os.readNullable(HiNode.class), os.readNullable(HiNode.class));
	}
}
