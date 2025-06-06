package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.ValueType;
import ru.nest.hiscript.tokenizer.Token;

public interface HiNodeIF extends TokenAccessible, Codeable {
	boolean validate(ValidationInfo validationInfo, CompileClassContext ctx);

	void setToken(Token token);

	default void execute(RuntimeContext ctx) {
		execute(ctx, null);
	}

	default void execute(RuntimeContext ctx, HiClass clazz) {
		execute(ctx);
	}

	default NodeValueType getNodeValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.node = this;
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.clazz = getValueClass(validationInfo, ctx);
		ctx.nodeValueType.valid = validate(validationInfo, ctx);
		ctx.nodeValueType.resolvedValueVariable = null;
		ctx.nodeValueType.enclosingClass = null;
		ctx.nodeValueType.enclosingType = null;
		return ctx.nodeValueType;
	}

	default HiClass getValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return getNodeValueType(validationInfo, ctx).clazz;
	}

	default Type getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		return getNodeValueType(validationInfo, ctx).type;
	}

	default NodeValueType.NodeValueReturnType getValueReturnType() {
		return null;
	}

	default boolean isCompileValue() {
		return getValueReturnType() == NodeValueType.NodeValueReturnType.compileValue;
	}

	default boolean isRuntimeValue() {
		return getValueReturnType() == NodeValueType.NodeValueReturnType.runtimeValue;
	}

	default boolean isStatement() {
		return false;
	}

	default boolean isVariable() {
		return false;
	}

	default ValueType getInvocationValueType() {
		return ValueType.UNDEFINED;
	}

	default int getArrayDimension() {
		return 0;
	}

	default <N extends HiNodeIF> N getSingleNode(Class<N> clazz) {
		if (getClass() == clazz) {
			return (N) this;
		} else if (this instanceof NodeExpressionNoLS) {
			NodeExpressionNoLS expressionNode = (NodeExpressionNoLS) this;
			HiNodeIF expressionSingleNode = expressionNode.getSingleNode();
			if (expressionSingleNode != null && clazz.isAssignableFrom(expressionSingleNode.getClass())) {
				return (N) expressionSingleNode;
			}
		}
		return null;
	}

	default <N extends HiNodeIF> N getExpressionSingleNode() {
		if (this instanceof NodeExpressionNoLS) {
			NodeExpressionNoLS expressionNode = (NodeExpressionNoLS) this;
			return (N) expressionNode.getSingleNode();
		}
		return (N) this;
	}
}
