package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiScriptRuntimeException;
import ru.nest.hiscript.tokenizer.Token;

public class NodeValueType implements PrimitiveTypes {
	public enum NodeValueReturnType {
		noValue, compileValue, runtimeValue, classValue, castedIdentifier
	}

	public HiNodeIF node;

	public HiClass clazz;

	public Type type;

	public NodeValueReturnType returnType = NodeValueReturnType.runtimeValue;

	public boolean isCompileValue() {
		return returnType == NodeValueReturnType.compileValue;
	}

	public boolean isRuntimeValue() {
		return returnType == NodeValueReturnType.runtimeValue;
	}

	public boolean isConstant;

	public boolean valid;

	public HiClass valueClass;

	public byte byteValue;

	public short shortValue;

	public int intValue;

	public long longValue;

	public float floatValue;

	public double doubleValue;

	public char charValue;

	public boolean booleanValue;

	public String stringValue;

	public HiNodeIF resolvedValueVariable;

	public boolean isVariable() {
		return resolvedValueVariable != null && resolvedValueVariable.isVariable();
	}

	public HiClass enclosingClass;

	public Type enclosingType;

	public Token token;

	public void init(HiNodeIF node) {
		this.node = node;
		this.returnType = null;
		this.clazz = null;
		this.type = null;
		this.valid = false;
		this.valueClass = null;
		this.token = node.getToken() != null ? new Token(node.getToken()) : null;
		this.resolvedValueVariable = null;
		this.enclosingClass = null;
		this.enclosingType = null;
	}

	public NodeValueType apply(NodeValueType node) {
		this.valid &= node.valid;
		if (this.valid) {
			if (this.returnType == NodeValueReturnType.compileValue) {
				if (node.returnType == null || node.returnType == NodeValueReturnType.runtimeValue) {
					this.returnType = NodeValueReturnType.runtimeValue;
				}
			} else if (this.returnType != NodeValueReturnType.runtimeValue) {
				this.returnType = node.returnType;
			}
		} else {
			this.returnType = null;
		}
		this.isConstant &= this.valid && node.isConstant;
		if (this.token != null) {
			this.token.extend(node.token);
		} else if (node.token != null) {
			this.token = new Token(node.token);
		}
		return this;
	}

	public void copyTo(NodeValueType nodeValueType) {
		nodeValueType.node = node;
		nodeValueType.returnType = returnType;
		nodeValueType.clazz = clazz;
		nodeValueType.type = type;
		nodeValueType.valid = valid;
		nodeValueType.resolvedValueVariable = resolvedValueVariable;
		nodeValueType.enclosingClass = enclosingClass;
		nodeValueType.enclosingType = enclosingType;
		// do not copy isConstant!

		if (isCompileValue() && valueClass != null) { // not void
			nodeValueType.valueClass = valueClass;
			if (valueClass.isPrimitive()) {
				switch (valueClass.getPrimitiveType()) {
					case CHAR:
						nodeValueType.charValue = charValue;
						break;
					case BYTE:
						nodeValueType.byteValue = byteValue;
						break;
					case SHORT:
						nodeValueType.shortValue = shortValue;
						break;
					case INT:
						nodeValueType.intValue = intValue;
						break;
					case LONG:
						nodeValueType.longValue = longValue;
						break;
					case FLOAT:
						nodeValueType.floatValue = floatValue;
						break;
					case DOUBLE:
						nodeValueType.doubleValue = doubleValue;
						break;
					case BOOLEAN:
						nodeValueType.booleanValue = booleanValue;
						break;
				}
			} else if (valueClass == HiClass.STRING_CLASS) {
				nodeValueType.stringValue = stringValue;
			}
		}
	}

	public void invalid() {
		this.returnType = null;
		this.valid = false;
	}

	public void get(HiNodeIF node, HiClass clazz, Type type, boolean valid, NodeValueReturnType returnType, HiClass valueClass, boolean isConstant, HiNodeIF resolvedValueVariable, HiClass enclosingClass, Type enclosingType) {
		if (clazz == null) {
			clazz = HiClassPrimitive.VOID;
			returnType = NodeValueReturnType.noValue;
		}
		this.node = node;
		this.clazz = clazz;
		this.type = type;
		this.valid = valid;
		this.returnType = returnType;
		this.isConstant = isConstant;
		this.resolvedValueVariable = resolvedValueVariable;
		this.enclosingClass = enclosingClass;
		this.enclosingType = enclosingType;
		if (valueClass != null) {
			this.valueClass = valueClass;
		} else {
			getCompileValueFromNode();
		}
	}

	public NodeValueType get(ValidationInfo validationInfo, CompileClassContext ctx, HiNodeIF node) {
		this.node = node;
		return get(validationInfo, ctx);
	}

	public NodeValueType get(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiNodeIF node = this.node;
		boolean valid = node.validate(validationInfo, ctx);
		node.getNodeValueType(validationInfo, ctx); // after validation
		ctx.nodeValueType.copyTo(this);
		this.valid = valid;
		if (valueClass == null) {
			getCompileValueFromNode();
		}
		return this;
	}

	public void getCompileValueFromNode() {
		if (valid && node != null && node.isCompileValue()) {
			valueClass = clazz;
			if (node instanceof NodeInt) {
				intValue = ((NodeInt) node).getValue();
			} else if (node instanceof NodeString) {
				stringValue = ((NodeString) node).text;
			} else if (node instanceof NodeBoolean) {
				booleanValue = ((NodeBoolean) node).getValue();
			} else if (node instanceof NodeLong) {
				longValue = ((NodeLong) node).getValue();
			} else if (node instanceof NodeDouble) {
				doubleValue = ((NodeDouble) node).getValue();
			} else if (node instanceof NodeChar) {
				charValue = ((NodeChar) node).getValue();
			} else if (node instanceof NodeFloat) {
				floatValue = ((NodeFloat) node).getValue();
			}
		}
	}

	public int getIntValue() {
		if (valid && isCompileValue()) {
			valueClass = clazz;
			if (clazz == HiClassPrimitive.INT) {
				return intValue;
			} else if (clazz == HiClassPrimitive.CHAR) {
				return charValue;
			} else if (clazz == HiClassPrimitive.BYTE) {
				return byteValue;
			} else if (clazz == HiClassPrimitive.SHORT) {
				return shortValue;
			}
		}
		throw new HiScriptRuntimeException("integer value expected");
	}

	public Object getCompileValue() {
		if (valid && isCompileValue()) {
			if (valueClass == HiClassPrimitive.INT) {
				return intValue;
			} else if (valueClass == HiClass.STRING_CLASS) {
				return stringValue;
			} else if (valueClass == HiClassPrimitive.BOOLEAN) {
				return booleanValue;
			} else if (valueClass == HiClassPrimitive.LONG) {
				return longValue;
			} else if (valueClass == HiClassPrimitive.DOUBLE) {
				return doubleValue;
			} else if (valueClass == HiClassPrimitive.CHAR) {
				return charValue;
			} else if (valueClass == HiClassPrimitive.BYTE) {
				return byteValue;
			} else if (valueClass == HiClassPrimitive.FLOAT) {
				return floatValue;
			} else if (valueClass == HiClassPrimitive.SHORT) {
				return shortValue;
			}
		}
		return null;
	}

	public boolean autoCastValue(HiClass clazz) {
		// @autoboxing
		// @generics
		if (!clazz.isPrimitive()) {
			if (clazz == HiClass.OBJECT_CLASS) {
				return true;
			} else if (clazz == HiClass.NUMBER_CLASS) {
				return valueClass.isNumber();
			} else if (clazz == HiClass.STRING_CLASS) {
				return valueClass == HiClass.STRING_CLASS;
			}

			HiClass autoboxedPrimitiveClass = clazz.getAutoboxedPrimitiveClass();
			if (autoboxedPrimitiveClass != null) {
				clazz = autoboxedPrimitiveClass;
			} else if (clazz.isGeneric()) {
				HiClassGeneric genericType = (HiClassGeneric) clazz;
				if (genericType.isSuper) {
					return false;
				} else if (genericType.clazz == HiClass.NUMBER_CLASS) {
					return valueClass.isNumber();
				} else if (genericType.clazz == HiClass.OBJECT_CLASS) {
					return true;
				}
			} else {
				return false;
			}
		}

		if (!valueClass.isPrimitive()) {
			if (valueClass == HiClass.OBJECT_CLASS) {
				return clazz == HiClass.OBJECT_CLASS;
			}
			return false;
		}

		int t1 = valueClass.getPrimitiveType();
		int t2 = clazz.getPrimitiveType();
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
