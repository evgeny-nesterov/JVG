package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class NodeValueType implements PrimitiveTypes {
	public HiNode node;

	public HiClass type;

	public boolean isValue;

	public boolean valid;

	public HiClass valueType;

	public byte byteValue;

	public short shortValue;

	public int intValue;

	public long longValue;

	public float floatValue;

	public double doubleValue;

	public char charValue;

	public boolean booleanValue;

	public void init(HiNode node) {
		this.node = node;
		this.isValue = node.isValue();
		this.type = null;
		this.valid = false;
	}

	public NodeValueType apply(NodeValueType node) {
		this.valid &= node.valid;
		this.isValue &= this.valid && node.isValue && node.node.isValue();
		return this;
	}

	public void copyTo(NodeValueType nodeValueType) {
		nodeValueType.node = node;
		nodeValueType.isValue = isValue;
		nodeValueType.type = type;
		nodeValueType.valid = valid;

		if (isValue) {
			nodeValueType.valueType = valueType;
			// TODO optimize
			nodeValueType.intValue = intValue;
			nodeValueType.booleanValue = booleanValue;
			nodeValueType.longValue = longValue;
			nodeValueType.doubleValue = doubleValue;
			nodeValueType.charValue = charValue;
			nodeValueType.byteValue = byteValue;
			nodeValueType.floatValue = floatValue;
			nodeValueType.shortValue = shortValue;
		}
	}

	public void invalid() {
		this.isValue = false;
		this.type = null;
		this.valid = false;
	}

	public void get(HiNode node, HiClass type, boolean valid, boolean isValue) {
		if (type == null) {
			type = HiClassPrimitive.VOID;
			isValue = false;
		}
		this.node = node;
		this.type = type;
		this.valid = valid;
		this.isValue = isValue;
		getValue();
	}

	public NodeValueType get(ValidationInfo validationInfo, CompileClassContext ctx, HiNode node) {
		this.node = node;
		return get(validationInfo, ctx);
	}

	public NodeValueType get(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiNode node = this.node;
		node.getValueType(validationInfo, ctx);
		this.type = ctx.nodeValueType.type;
		this.valid = node.validate(validationInfo, ctx);
		this.isValue = ctx.nodeValueType.isValue;
		this.node = node;
		getValue();
		return this;
	}

	private void getValue() {
		if (valid && node.isValue()) {
			valueType = type;
			if (node instanceof NodeInt) {
				intValue = ((NodeInt) node).getValue();
			} else if (node instanceof NodeBoolean) {
				booleanValue = ((NodeBoolean) node).getValue();
			} else if (node instanceof NodeLong) {
				longValue = ((NodeLong) node).getValue();
			} else if (node instanceof NodeDouble) {
				doubleValue = ((NodeDouble) node).getValue();
			} else if (node instanceof NodeChar) {
				charValue = ((NodeChar) node).getValue();
			} else if (node instanceof NodeByte) {
				byteValue = ((NodeByte) node).getValue();
			} else if (node instanceof NodeFloat) {
				floatValue = ((NodeFloat) node).getValue();
			} else if (node instanceof NodeShort) {
				shortValue = ((NodeShort) node).getValue();
			}
		}
	}

	/**
	 * assumed type is primitive
	 */
	public boolean autoCastValue(HiClass type) {
		int t1 = HiFieldPrimitive.getType(valueType);
		int t2 = HiFieldPrimitive.getType(type);
		switch (t1) {
			case BYTE:
				switch (t2) {
					case CHAR:
						return byteValue >= Character.MIN_VALUE;
					case BYTE:
					case SHORT:
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
			case SHORT:
				switch (t2) {
					case CHAR:
						return shortValue >= Character.MIN_VALUE && shortValue <= Character.MAX_VALUE;
					case BYTE:
						return shortValue >= Byte.MIN_VALUE && shortValue <= Byte.MAX_VALUE;
					case SHORT:
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
			case INT:
				switch (t2) {
					case CHAR:
						return intValue >= Character.MIN_VALUE && intValue <= Character.MAX_VALUE;
					case BYTE:
						return intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE;
					case SHORT:
						return intValue >= Short.MIN_VALUE && intValue <= Short.MAX_VALUE;
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
			case LONG:
				switch (t2) {
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
			case FLOAT:
				switch (t2) {
					case FLOAT:
					case DOUBLE:
						return true;
				}
			case DOUBLE:
				return t2 == DOUBLE;
			case CHAR:
				switch (t2) {
					case CHAR:
						return true;
					case BYTE:
						return charValue >= Byte.MIN_VALUE && charValue <= Byte.MAX_VALUE;
					case SHORT:
						return charValue >= Short.MIN_VALUE && charValue <= Short.MAX_VALUE;
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
			case BOOLEAN:
				return t2 == BOOLEAN;
		}
		return false;
	}
}
