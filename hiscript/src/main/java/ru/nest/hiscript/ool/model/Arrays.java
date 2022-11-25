package ru.nest.hiscript.ool.model;

import java.lang.reflect.Array;
import java.util.HashSet;

import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;

public class Arrays implements PrimitiveTypes {
	private static HashSet<Class<?>> primitiveArrayClasses;

	private static boolean initialized = false;

	private static void init() {
		if (!initialized) {
			primitiveArrayClasses = new HashSet<Class<?>>(8);

			primitiveArrayClasses.add(boolean[].class);
			primitiveArrayClasses.add(char[].class);
			primitiveArrayClasses.add(byte[].class);
			primitiveArrayClasses.add(short[].class);
			primitiveArrayClasses.add(int[].class);
			primitiveArrayClasses.add(float[].class);
			primitiveArrayClasses.add(long[].class);
			primitiveArrayClasses.add(double[].class);

			initialized = true;
		}
	}

	public static Class<?> getClass(HiClass clazz, int dimension) {
		Class<?> c = null;
		if (dimension > 0) {
			String prefix = "";
			for (int i = 0; i < dimension; i++) {
				prefix += "[";
			}

			try {
				if (clazz.isPrimitive()) {
					int typeIndex = HiFieldPrimitive.getType(clazz);
					switch (typeIndex) {
						case PrimitiveTypes.BOOLEAN:
							c = Class.forName(prefix + "Z");
							break;

						case PrimitiveTypes.CHAR:
							c = Class.forName(prefix + "C");
							break;

						case PrimitiveTypes.BYTE:
							c = Class.forName(prefix + "B");
							break;

						case PrimitiveTypes.SHORT:
							c = Class.forName(prefix + "S");
							break;

						case PrimitiveTypes.INT:
							c = Class.forName(prefix + "I");
							break;

						case PrimitiveTypes.LONG:
							c = Class.forName(prefix + "J");
							break;

						case PrimitiveTypes.FLOAT:
							c = Class.forName(prefix + "F");
							break;

						case PrimitiveTypes.DOUBLE:
							c = Class.forName(prefix + "D");
							break;
					}
				} else {
					c = Class.forName(prefix + "L" + HiObject.class.getName() + ";");
				}
			} catch (Exception exc) {
				// TODO: error
				exc.printStackTrace();
			}
		} else {
			if (clazz.isPrimitive()) {
				int typeIndex = HiFieldPrimitive.getType(clazz);
				switch (typeIndex) {
					case PrimitiveTypes.BOOLEAN:
						c = boolean.class;
						break;

					case PrimitiveTypes.CHAR:
						c = char.class;
						break;

					case PrimitiveTypes.BYTE:
						c = byte.class;
						break;

					case PrimitiveTypes.SHORT:
						c = short.class;
						break;

					case PrimitiveTypes.INT:
						c = int.class;
						break;

					case PrimitiveTypes.LONG:
						c = long.class;
						break;

					case PrimitiveTypes.FLOAT:
						c = float.class;
						break;

					case PrimitiveTypes.DOUBLE:
						c = double.class;
						break;
				}
			} else {
				c = HiObject.class;
			}
		}
		return c;
	}

	public static void getArrayIndex(Value v, Object array, int index) {
		if (array instanceof Object[]) {
			init();

			Object value = Array.get(array, index);
			if (value == null) {
				v.object = null;
				v.array = null;
			} else if (value instanceof Object[] || primitiveArrayClasses.contains(value.getClass())) {
				v.array = value;
			} else if (value instanceof HiObject) {
				v.object = (HiObject) value;
				v.set(value);
			} else {
				throw new IllegalArgumentException("array cell: " + value);
			}
		} else {
			HiClassArray arrayClass = (HiClassArray) v.type;
			HiClass cellType = arrayClass.cellClass;
			int typeIndex = HiFieldPrimitive.getType(cellType);
			switch (typeIndex) {
				case BOOLEAN:
					v.bool = Array.getBoolean(array, index);
					break;

				case CHAR:
					v.character = Array.getChar(array, index);
					break;

				case BYTE:
					v.byteNumber = Array.getByte(array, index);
					break;

				case SHORT:
					v.shortNumber = Array.getShort(array, index);
					break;

				case INT:
					v.intNumber = Array.getInt(array, index);
					break;

				case LONG:
					v.longNumber = Array.getLong(array, index);
					break;

				case FLOAT:
					v.floatNumber = Array.getFloat(array, index);
					break;

				case DOUBLE:
					v.doubleNumber = Array.getDouble(array, index);
					break;
			}
		}
	}

	public static void setArrayIndex(HiClass type, Object parentArray, int index, Value value, Value dst) {
		dst.type = type;

		if (type.isArray()) {
			dst.array = value.getArray();
			dst.object = value.object;
			Array.set(parentArray, index, dst.array);
		} else if (type.isPrimitive()) {
			int typeIndex = HiFieldPrimitive.getType(type);
			switch (typeIndex) {
				case BOOLEAN:
					dst.bool = value.getBoolean();
					Array.setBoolean(parentArray, index, dst.bool);
					break;

				case CHAR:
					dst.character = value.getChar();
					Array.setChar(parentArray, index, dst.character);
					break;

				case BYTE:
					dst.byteNumber = value.getByte();
					Array.setByte(parentArray, index, dst.byteNumber);
					break;

				case SHORT:
					dst.shortNumber = value.getShort();
					Array.setShort(parentArray, index, dst.shortNumber);
					break;

				case INT:
					dst.intNumber = value.getInt();
					Array.setInt(parentArray, index, dst.intNumber);
					break;

				case LONG:
					dst.longNumber = value.getLong();
					Array.setLong(parentArray, index, dst.longNumber);
					break;

				case FLOAT:
					dst.floatNumber = value.getFloat();
					Array.setFloat(parentArray, index, dst.floatNumber);
					break;

				case DOUBLE:
					dst.doubleNumber = value.getDouble();
					Array.setDouble(parentArray, index, dst.doubleNumber);
					break;
			}
		} else {
			dst.object = value.getObject();
			Array.set(parentArray, index, dst.object);
		}
	}

	public static void setArray(HiClass type, Object array, int index, Value value) {
		if (type.isArray()) {
			Array.set(array, index, value.getArray());
		} else if (type.isPrimitive()) {
			int typeIndex = HiFieldPrimitive.getType(type);
			switch (typeIndex) {
				case BOOLEAN:
					Array.setBoolean(array, index, value.getBoolean());
					break;

				case CHAR:
					Array.setChar(array, index, value.getChar());
					break;

				case BYTE:
					Array.setByte(array, index, value.getByte());
					break;

				case SHORT:
					Array.setShort(array, index, value.getShort());
					break;

				case INT:
					Array.setInt(array, index, value.getInt());
					break;

				case LONG:
					Array.setLong(array, index, value.getLong());
					break;

				case FLOAT:
					Array.setFloat(array, index, value.getFloat());
					break;

				case DOUBLE:
					Array.setDouble(array, index, value.getDouble());
					break;
			}
		} else {
			Array.set(array, index, value.getObject());
		}
	}
}
