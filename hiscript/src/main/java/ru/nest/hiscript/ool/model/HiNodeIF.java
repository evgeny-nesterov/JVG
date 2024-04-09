package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public interface HiNodeIF extends TokenAccessible, Codeable {
	boolean validate(ValidationInfo validationInfo, CompileClassContext ctx);

	default void execute(RuntimeContext ctx) {
		execute(ctx, null);
	}

	default void execute(RuntimeContext ctx, HiClass clazz) {
		execute(ctx);
	}

	default NodeValueType getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.node = this;
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.type = getValueClass(validationInfo, ctx);
		ctx.nodeValueType.valid = validate(validationInfo, ctx);
		ctx.nodeValueType.resolvedValueVariable = null;
		ctx.nodeValueType.enclosingClass = null;
		return ctx.nodeValueType;
	}

	default HiClass getValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return getValueType(validationInfo, ctx).type;
	}

	default NodeValueType.NodeValueReturnType getReturnValueType() {
		return null;
	}

	default boolean isCompileValue() {
		return getReturnValueType() == NodeValueType.NodeValueReturnType.compileValue;
	}

	default boolean isRuntimeValue() {
		return getReturnValueType() == NodeValueType.NodeValueReturnType.runtimeValue;
	}

	default int getInvocationValueType() {
		return -1;
	}
}
