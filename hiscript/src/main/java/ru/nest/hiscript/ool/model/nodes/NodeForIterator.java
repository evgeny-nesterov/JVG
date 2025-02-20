package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

public class NodeForIterator extends HiNode {
	public NodeForIterator(NodeDeclaration declaration, HiNode iterable, HiNode body) {
		super("for", TYPE_FOR_ITERATOR, true);
		this.declaration = declaration;
		this.iterable = iterable;
		this.body = body;
	}

	private final NodeDeclaration declaration;

	private final HiNode iterable;

	private final HiNode body;

	@Override
	public NodeReturn getReturnNode() {
		return body != null ? body.getReturnNode() : null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());

		ctx.enter(RuntimeContext.FOR, this);

		declaration.isInitialized = true;
		valid &= declaration.validate(validationInfo, ctx, false);

		if (declaration.hasModifiers()) {
			validationInfo.error("modifiers not allowed", declaration.getToken());
			valid = false;
		}

		ctx.initializedNodes.add(declaration);

		if (iterable.validate(validationInfo, ctx) && iterable.expectIterableValue(validationInfo, ctx)) {
			HiClass declarationClass = declaration.getValueClass(validationInfo, ctx);
			HiClass iterableClass = iterable.getValueClass(validationInfo, ctx);
			HiClass iterableElementClass;
			if (iterableClass.isArray()) {
				iterableElementClass = ((HiClassArray) iterableClass).cellClass;
			} else {
				Type iterableType = iterable.getReturnValueType();
				Type iterableElementType = null;
				if (iterableType != null && iterableType.parameters != null && iterableType.parameters.length > 0) {
					iterableElementType = iterableType.parameters[0];
				}
				iterableElementClass = iterableElementType != null ? iterableElementType.getClass(ctx) : HiClass.OBJECT_CLASS;
			}

			if (!HiClass.autoCast(ctx, iterableElementClass, declarationClass, false, true)) {
				validationInfo.error("incompatible types: " + iterableElementClass + " cannot be converted to " + declarationClass, token);
				valid = false;
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
			HiField<?> forVariable = declaration.executeAndGetVariable(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}

			iterable.execute(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}

			if (ctx.value.valueClass.isArray()) {
				Object array = ctx.value.object;
				int size = Array.getLength(array);
				for (int i = 0; i < size; i++) {
					Object value = Array.get(array, i); // TODO primitives
					if (!executeValue(ctx, forVariable, value)) {
						break;
					}
				}
			} else if (ctx.value.valueClass.isInstanceof(HiClass.ARRAYLIST_CLASS_NAME)) { // TODO isInstanceof Iterable
				List list = (List) ((HiObject) ctx.value.object).userObject;
				for (Object value : list) {
					if (!executeValue(ctx, forVariable, value)) {
						break;
					}
				}
			}
		} finally {
			ctx.exit();
		}
	}

	private boolean executeValue(RuntimeContext ctx, HiField<?> forVariable, Object value) {
		ctx.setValue(value);
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
