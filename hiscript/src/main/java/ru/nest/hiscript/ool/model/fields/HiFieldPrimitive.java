package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public abstract class HiFieldPrimitive<T> extends HiField<T> implements PrimitiveTypes {
	public HiFieldPrimitive(Type type, String name) {
		super(type, name);
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.type = type;
		return HiClassPrimitive.getPrimitiveClass(type.fullName);
	}

	public static int getAutoType(HiClass clazz) {
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
		int srcType = src.getPrimitiveType();
		int dstType = dst.getPrimitiveType();
		switch (srcType) {
			case BYTE:
			case CHAR:
			case SHORT:
			case INT:
				switch (dstType) {
					case BYTE:
					case CHAR:
					case SHORT:
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
				break;
			case LONG:
				switch (dstType) {
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
				break;
			case FLOAT:
				return dstType == FLOAT || dstType == DOUBLE;
			case DOUBLE:
				return dstType == DOUBLE;
			case BOOLEAN:
				return dstType == BOOLEAN;
		}
		return false;
	}

	public static boolean autoCast(HiClass src, HiClass dst) {
		int srcType = src.getPrimitiveType();
		int dstType = dst.getPrimitiveType();
		switch (srcType) {
			case CHAR:
				switch (dstType) {
					case CHAR:
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
				break;
			case BYTE:
				switch (dstType) {
					case BYTE:
					case SHORT:
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
				break;
			case SHORT:
				switch (dstType) {
					case SHORT:
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
				break;
			case INT:
				switch (dstType) {
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
				break;
			case LONG:
				switch (dstType) {
					case LONG:
					case FLOAT:
					case DOUBLE:
						return true;
				}
				break;
			case FLOAT:
				switch (dstType) {
					case FLOAT:
					case DOUBLE:
						return true;
				}
				break;
			case DOUBLE:
				return dstType == DOUBLE;
			case BOOLEAN:
				return dstType == BOOLEAN;
		}
		return false;
	}
}
