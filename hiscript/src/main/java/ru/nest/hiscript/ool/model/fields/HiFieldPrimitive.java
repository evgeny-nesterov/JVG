package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.classes.HiClassVar;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.util.HashMap;
import java.util.Map;

public abstract class HiFieldPrimitive<T> extends HiField<T> implements PrimitiveTypes {
	// TODO optimize
	protected static Map<Type, Integer> typesHashType = new HashMap<>();

	protected static Map<HiClass, Integer> typesHashClass = new HashMap<>();

	static {
		typesHashType.put(Type.charType, CHAR);
		typesHashType.put(Type.byteType, BYTE);
		typesHashType.put(Type.shortType, SHORT);
		typesHashType.put(Type.intType, INT);
		typesHashType.put(Type.floatType, FLOAT);
		typesHashType.put(Type.longType, LONG);
		typesHashType.put(Type.doubleType, DOUBLE);
		typesHashType.put(Type.booleanType, BOOLEAN);
		typesHashType.put(Type.voidType, VOID);
		typesHashType.put(Type.varType, VAR);

		typesHashClass.put(HiClassPrimitive.CHAR, CHAR);
		typesHashClass.put(HiClassPrimitive.BYTE, BYTE);
		typesHashClass.put(HiClassPrimitive.SHORT, SHORT);
		typesHashClass.put(HiClassPrimitive.INT, INT);
		typesHashClass.put(HiClassPrimitive.FLOAT, FLOAT);
		typesHashClass.put(HiClassPrimitive.LONG, LONG);
		typesHashClass.put(HiClassPrimitive.DOUBLE, DOUBLE);
		typesHashClass.put(HiClassPrimitive.BOOLEAN, BOOLEAN);
		typesHashClass.put(HiClassPrimitive.VOID, VOID);
		typesHashClass.put(HiClassVar.VAR, VAR);
	}

	public HiFieldPrimitive(Type type, String name) {
		super(type, name);
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.getPrimitiveClass(type.fullName);
	}

	public static int getAutoType(HiClass type) {
		if (type.getAutoboxedPrimitiveClass() != null) {
			type = type.getAutoboxedPrimitiveClass();
		}
		return getType(type);
	}

	public static int getType(HiClass type) {
		Integer t = typesHashClass.get(type);
		if (t != null) {
			return t.intValue();
		}
		throw new HiScriptRuntimeException("unknown type: " + type);
	}

	public static int getType(Type type) {
		Integer t = typesHashClass.get(type);
		if (t != null) {
			return t.intValue();
		}
		throw new HiScriptRuntimeException("unknown type: " + type);
	}

	/**
	 * src is value
	 */
	// TODO check value!!!
	public static boolean autoCastValue(HiClass src, HiClass dst) {
		int srcType = getType(src);
		int dstType = getType(dst);
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
		int srcType = getType(src);
		int dstType = getType(dst);
		if (srcType == VAR || dstType == VAR) {
			return true;
		}
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
