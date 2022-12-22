package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.util.HashMap;
import java.util.Map;

public abstract class HiFieldPrimitive<T> extends HiField<T> implements PrimitiveTypes {
	protected static Map<Type, Integer> types_hash_type = new HashMap<>();

	protected static Map<HiClass, Integer> types_hash_class = new HashMap<>();

	static {
		types_hash_type.put(Type.getPrimitiveType("char"), CHAR);
		types_hash_type.put(Type.getPrimitiveType("byte"), BYTE);
		types_hash_type.put(Type.getPrimitiveType("short"), SHORT);
		types_hash_type.put(Type.getPrimitiveType("int"), INT);
		types_hash_type.put(Type.getPrimitiveType("float"), FLOAT);
		types_hash_type.put(Type.getPrimitiveType("long"), LONG);
		types_hash_type.put(Type.getPrimitiveType("double"), DOUBLE);
		types_hash_type.put(Type.getPrimitiveType("boolean"), BOOLEAN);
		types_hash_type.put(Type.getPrimitiveType("void"), VOID);

		types_hash_class.put(HiClass.getPrimitiveClass("char"), CHAR);
		types_hash_class.put(HiClass.getPrimitiveClass("byte"), BYTE);
		types_hash_class.put(HiClass.getPrimitiveClass("short"), SHORT);
		types_hash_class.put(HiClass.getPrimitiveClass("int"), INT);
		types_hash_class.put(HiClass.getPrimitiveClass("float"), FLOAT);
		types_hash_class.put(HiClass.getPrimitiveClass("long"), LONG);
		types_hash_class.put(HiClass.getPrimitiveClass("double"), DOUBLE);
		types_hash_class.put(HiClass.getPrimitiveClass("boolean"), BOOLEAN);
		types_hash_class.put(HiClass.getPrimitiveClass("void"), VOID);
	}

	public HiFieldPrimitive(Type type, String name) {
		super(type, name);
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.getPrimitiveClass(type.fullName);
	}

	public static int getType(HiClass type) {
		Integer t = types_hash_class.get(type);
		if (t != null) {
			return t.intValue();
		} else {
			throw new RuntimeException("unknown type: " + type);
		}
	}

	public static int getType(Type type) {
		Integer t = types_hash_class.get(type);
		if (t != null) {
			return t.intValue();
		} else {
			throw new RuntimeException("unknown type: " + type);
		}
	}

	/**
	 * src is value
	 */
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
				switch (dstType) {
					case FLOAT:
					case DOUBLE:
						return true;
				}
				break;

			case DOUBLE:
				return true;

			case BOOLEAN:
				return true;
		}
		return false;
	}

	public static boolean autoCast(HiClass src, HiClass dst) {
		int srcType = getType(src);
		int dstType = getType(dst);
		switch (srcType) {
			case CHAR:
				switch (dstType) {
					case CHAR:
					case SHORT:
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
