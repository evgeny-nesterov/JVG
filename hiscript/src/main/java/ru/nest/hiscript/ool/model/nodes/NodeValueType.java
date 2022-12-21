package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class NodeValueType {
	public HiNode node;

	public HiClass type;

	public boolean isValue;

	public boolean valid;

	public void init(HiNode node) {
		this.node = node;
		this.isValue = node.isValue();
		this.type = null;
		this.valid = false;
	}

	public NodeValueType apply(NodeValueType node) {
		this.valid &= node.valid;
		this.isValue &= this.valid && node.isValue;
		return this;
	}

	public void copyTo(NodeValueType node) {
		this.node = node.node;
		this.isValue = node.isValue;
		this.type = node.type;
		this.valid = node.valid;
	}

	public void invalid() {
		this.node = null;
		this.isValue = false;
		this.type = null;
		this.valid = false;
	}

	public void get(HiClass type, boolean valid, boolean isValue) {
		if (type == null) {
			type = HiClassPrimitive.VOID;
			isValue = false;
		}
		this.node = null;
		this.type = type;
		this.valid = valid;
		this.isValue = isValue;
	}


	public void get(HiClass type) {
		if (type == null) {
			type = HiClassPrimitive.VOID;
			isValue = false;
		}
		this.node = null;
		this.type = type;
		this.valid = valid;
		this.isValue = isValue;
	}

	public NodeValueType get(ValidationInfo validationInfo, CompileClassContext ctx, HiNode node) {
		this.node = node;
		get(validationInfo, ctx);
		return this;
	}

	public void get(ValidationInfo validationInfo, CompileClassContext ctx) {
		node.getValueType(validationInfo, ctx);
		type = ctx.nodeValueType.type;
		valid = node.validate(validationInfo, ctx);
		isValue = ctx.nodeValueType.isValue;
	}
}
