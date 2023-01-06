package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public interface HiNodeIF extends TokenAccessible, Codeable {
	boolean validate(ValidationInfo validationInfo, CompileClassContext ctx);

	void execute(RuntimeContext ctx);

	default NodeValueType getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.node = this;
		ctx.nodeValueType.isValue = false;
		ctx.nodeValueType.type = getValueClass(validationInfo, ctx);
		ctx.nodeValueType.valid = validate(validationInfo, ctx);
		ctx.nodeValueType.resolvedValueVariable = null;
		ctx.nodeValueType.enclosingClass = null;
		return ctx.nodeValueType;
	}

	default HiClass getValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return getValueType(validationInfo, ctx).type;
	}

	default boolean isValue() {
		return false;
	}
}
