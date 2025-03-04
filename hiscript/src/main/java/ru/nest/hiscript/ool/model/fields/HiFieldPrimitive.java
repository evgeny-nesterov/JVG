package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public abstract class HiFieldPrimitive<T> extends HiField<T> {
	public HiFieldPrimitive(Type type, String name) {
		super(type, name);
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.type = type;
		return HiClassPrimitive.getPrimitiveClass(type.fullName);
	}

	public static PrimitiveType getAutoType(HiClass clazz) {
		if (clazz.getAutoboxedPrimitiveClass() != null) {
			clazz = clazz.getAutoboxedPrimitiveClass();
		}
		return clazz.getPrimitiveType();
	}

	/**
	 * src is value
	 */
	// TODO check value!!!
	public static boolean autoCastValue(HiClass src, HiClass dst) {
		PrimitiveType srcType = src.getPrimitiveType();
		PrimitiveType dstType = dst.getPrimitiveType();
		switch (srcType) {
			case BYTE_TYPE:
			case CHAR_TYPE:
			case SHORT_TYPE:
			case INT_TYPE:
				switch (dstType) {
					case BYTE_TYPE:
					case CHAR_TYPE:
					case SHORT_TYPE:
					case INT_TYPE:
					case LONG_TYPE:
					case FLOAT_TYPE:
					case DOUBLE_TYPE:
						return true;
				}
				break;
			case LONG_TYPE:
				switch (dstType) {
					case LONG_TYPE:
					case FLOAT_TYPE:
					case DOUBLE_TYPE:
						return true;
				}
				break;
			case FLOAT_TYPE:
				return dstType == FLOAT_TYPE || dstType == DOUBLE_TYPE;
			case DOUBLE_TYPE:
				return dstType == DOUBLE_TYPE;
			case BOOLEAN_TYPE:
				return dstType == BOOLEAN_TYPE;
		}
		return false;
	}

	public static boolean autoCast(HiClass src, HiClass dst) {
		PrimitiveType srcType = src.getPrimitiveType();
		PrimitiveType dstType = dst.getPrimitiveType();
		switch (srcType) {
			case CHAR_TYPE:
				switch (dstType) {
					case CHAR_TYPE:
					case INT_TYPE:
					case LONG_TYPE:
					case FLOAT_TYPE:
					case DOUBLE_TYPE:
						return true;
				}
				break;
			case BYTE_TYPE:
				switch (dstType) {
					case BYTE_TYPE:
					case SHORT_TYPE:
					case INT_TYPE:
					case LONG_TYPE:
					case FLOAT_TYPE:
					case DOUBLE_TYPE:
						return true;
				}
				break;
			case SHORT_TYPE:
				switch (dstType) {
					case SHORT_TYPE:
					case INT_TYPE:
					case LONG_TYPE:
					case FLOAT_TYPE:
					case DOUBLE_TYPE:
						return true;
				}
				break;
			case INT_TYPE:
				switch (dstType) {
					case INT_TYPE:
					case LONG_TYPE:
					case FLOAT_TYPE:
					case DOUBLE_TYPE:
						return true;
				}
				break;
			case LONG_TYPE:
				switch (dstType) {
					case LONG_TYPE:
					case FLOAT_TYPE:
					case DOUBLE_TYPE:
						return true;
				}
				break;
			case FLOAT_TYPE:
				switch (dstType) {
					case FLOAT_TYPE:
					case DOUBLE_TYPE:
						return true;
				}
				break;
			case DOUBLE_TYPE:
				return dstType == DOUBLE_TYPE;
			case BOOLEAN_TYPE:
				return dstType == BOOLEAN_TYPE;
		}
		return false;
	}
}
