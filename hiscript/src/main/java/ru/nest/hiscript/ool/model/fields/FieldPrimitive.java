package ru.nest.hiscript.ool.model.fields;

import java.util.HashMap;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Field;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.Type;

public abstract class FieldPrimitive<T> extends Field<T> implements PrimitiveTypes {
	protected static HashMap<Type, Integer> types_hash_type = new HashMap<Type, Integer>();

	protected static HashMap<Clazz, Integer> types_hash_class = new HashMap<Clazz, Integer>();
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

		types_hash_class.put(Clazz.getPrimitiveClass("char"), CHAR);
		types_hash_class.put(Clazz.getPrimitiveClass("byte"), BYTE);
		types_hash_class.put(Clazz.getPrimitiveClass("short"), SHORT);
		types_hash_class.put(Clazz.getPrimitiveClass("int"), INT);
		types_hash_class.put(Clazz.getPrimitiveClass("float"), FLOAT);
		types_hash_class.put(Clazz.getPrimitiveClass("long"), LONG);
		types_hash_class.put(Clazz.getPrimitiveClass("double"), DOUBLE);
		types_hash_class.put(Clazz.getPrimitiveClass("boolean"), BOOLEAN);
		types_hash_class.put(Clazz.getPrimitiveClass("void"), VOID);
	}

	public FieldPrimitive(Type type, String name) {
		super(type, name);
	}

	public static int getType(Clazz type) {
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

	public static boolean autoCast(Clazz src, Clazz dst) {
		int srcType = getType(src);
		int dstType = getType(dst);

		switch (srcType) {
			case CHAR:
				switch (dstType) {
					case CHAR:
					case SHORT:
					case INT:
					case FLOAT:
					case LONG:
					case DOUBLE:
						return true;
				}
				break;

			case BOOLEAN:
			case DOUBLE:
				return srcType == dstType;

			case BYTE:
				switch (dstType) {
					case BYTE:
					case SHORT:
					case INT:
					case FLOAT:
					case LONG:
					case DOUBLE:
						return true;
				}
				break;

			case SHORT:
				switch (dstType) {
					case SHORT:
					case INT:
					case FLOAT:
					case LONG:
					case DOUBLE:
						return true;
				}
				break;

			case INT:
				switch (dstType) {
					case INT:
					case FLOAT:
					case LONG:
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

			case LONG:
				switch (dstType) {
					case FLOAT:
					case LONG:
					case DOUBLE:
						return true;
				}
				break;
		}

		return false;
	}
}
