package script.pol.model;

import java.util.HashMap;

import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class Types implements Words {
	public final static int OBJECT = 28;

	public static boolean isType(int type) {
		switch (type) {
			case VOID:
			case CHAR:
			case STRING:
			case BOOLEAN:
			case BYTE:
			case SHORT:
			case INT:
			case FLOAT:
			case LONG:
			case DOUBLE:
				return true;
		}
		return false;
	}

	public static boolean isAutoCast(int srcType, int dstType) {
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

			case STRING:
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

	private final static HashMap<Class<?>, Integer> types = new HashMap<Class<?>, Integer>();

	public static int getType(Class<?> c) {
		Class<?> clazz;
		while ((clazz = c.getComponentType()) != null) {
			c = clazz;
		}

		// Class superClass = c.getSuperclass();
		// if (superClass != null)
		// {
		// c = superClass;
		// }

		Integer type = types.get(c);
		if (type != null) {
			return type;
		}

		type = VOID;
		if (c.isAssignableFrom(String.class)) {
			type = STRING;
		} else if (c.isAssignableFrom(boolean.class) || c.isAssignableFrom(Boolean.class)) {
			type = BOOLEAN;
		} else if (c.isAssignableFrom(byte.class) || c.isAssignableFrom(Byte.class)) {
			type = BYTE;
		} else if (c.isAssignableFrom(short.class) || c.isAssignableFrom(Short.class)) {
			type = SHORT;
		} else if (c.isAssignableFrom(int.class) || c.isAssignableFrom(Integer.class)) {
			type = INT;
		} else if (c.isAssignableFrom(float.class) || c.isAssignableFrom(Float.class)) {
			type = FLOAT;
		} else if (c.isAssignableFrom(long.class) || c.isAssignableFrom(Long.class)) {
			type = LONG;
		} else if (c.isAssignableFrom(double.class) || c.isAssignableFrom(Double.class)) {
			type = DOUBLE;
		} else if (c.isAssignableFrom(char.class) || c.isAssignableFrom(Character.class)) {
			type = CHAR;
		}

		types.put(c, type);
		return type;
	}

	public static Class<?> getType(int type) {
		switch (type) {
			case CHAR:
				return char.class;

			case STRING:
				return String.class;

			case BOOLEAN:
				return boolean.class;

			case BYTE:
				return byte.class;

			case SHORT:
				return short.class;

			case INT:
				return int.class;

			case FLOAT:
				return float.class;

			case LONG:
				return long.class;

			case DOUBLE:
				return double.class;
		}

		return null;
	}

	public static Class<?> getArrayType(int type, int dimension) {
		String prefix = "";
		for (int i = 0; i < dimension; i++) {
			prefix += "[";
		}

		try {
			switch (type) {
				case CHAR:
					return Class.forName(prefix + "C");

				case STRING:
					return Class.forName(prefix + "L" + String.class.getName() + ";");

				case BOOLEAN:
					return Class.forName(prefix + "Z");

				case BYTE:
					return Class.forName(prefix + "B");

				case SHORT:
					return Class.forName(prefix + "S");

				case INT:
					return Class.forName(prefix + "I");

				case FLOAT:
					return Class.forName(prefix + "F");

				case LONG:
					return Class.forName(prefix + "J");

				case DOUBLE:
					return Class.forName(prefix + "D");
			}
		} catch (ClassNotFoundException exc) {
			exc.printStackTrace();
		}

		return null;
	}

	public static boolean isArray(Object o) {
		return o != null && o.getClass().isArray();
	}

	public static Object getArrayValue(Object array, int type, int index) throws ExecuteException {
		if (array instanceof Object[]) {
			return ((Object[]) array)[index];
		} else {
			switch (type) {
				case CHAR:
					return ((char[]) array)[index];

				case STRING:
					return ((String[]) array)[index];

				case BOOLEAN:
					return ((boolean[]) array)[index];

				case BYTE:
					return ((byte[]) array)[index];

				case SHORT:
					return ((short[]) array)[index];

				case INT:
					return ((int[]) array)[index];

				case FLOAT:
					return ((float[]) array)[index];

				case LONG:
					return ((long[]) array)[index];

				case DOUBLE:
					return ((double[]) array)[index];
			}
		}

		throw new ExecuteException("array required");
	}

	public static int getDimension(Class<?> c) {
		int dimension = 0;
		if (c != null) {
			c = c.getComponentType();
			while (c != null) {
				dimension++;
				c = c.getComponentType();
			}
		}
		return dimension;
	}

	public static String getArrayPostfix(int dimension) {
		String s = "";
		for (int i = 0; i < dimension; i++) {
			s += "[]";
		}
		return s;
	}

	public static String getTypeDescr(int type, int dimension) {
		return WordToken.getWord(type) + getArrayPostfix(dimension);
	}

	// public static boolean isSameType(ValueContainer v1, ValueContainer v2)
	// {
	// if (v1.type == v2.type && v1.isArray == v2.isArray)
	// {
	// if (v1.isArray)
	// {
	// return v1.
	// }
	// else
	// {
	// return true;
	// }
	// }
	//
	// return false;
	// }
	//
}
