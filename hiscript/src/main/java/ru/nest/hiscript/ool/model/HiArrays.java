package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.runtime.Value;

import java.lang.reflect.Array;

public class HiArrays {
	public static Class<?> getClass(HiClass clazz, int dimension) {
		Class<?> c = null;
		assert !clazz.isArray();
		if (dimension > 0) {
			StringBuilder prefix = new StringBuilder();
			for (int i = 0; i < dimension; i++) {
				prefix.append("[");
			}

			try {
				if (clazz.isPrimitive()) {
					switch (clazz.getPrimitiveType()) {
						case BOOLEAN_TYPE:
							c = Class.forName(prefix + "Z");
							break;
						case CHAR_TYPE:
							c = Class.forName(prefix + "C");
							break;
						case BYTE_TYPE:
							c = Class.forName(prefix + "B");
							break;
						case SHORT_TYPE:
							c = Class.forName(prefix + "S");
							break;
						case INT_TYPE:
							c = Class.forName(prefix + "I");
							break;
						case LONG_TYPE:
							c = Class.forName(prefix + "J");
							break;
						case FLOAT_TYPE:
							c = Class.forName(prefix + "F");
							break;
						case DOUBLE_TYPE:
							c = Class.forName(prefix + "D");
							break;
					}
				} else {
					c = Class.forName(prefix + "L" + Object.class.getName() + ";");
				}
			} catch (Exception e) {
			}
		} else {
			if (clazz.isPrimitive()) {
				switch (clazz.getPrimitiveType()) {
					case BOOLEAN_TYPE:
						c = boolean.class;
						break;
					case CHAR_TYPE:
						c = char.class;
						break;
					case BYTE_TYPE:
						c = byte.class;
						break;
					case SHORT_TYPE:
						c = short.class;
						break;
					case INT_TYPE:
						c = int.class;
						break;
					case LONG_TYPE:
						c = long.class;
						break;
					case FLOAT_TYPE:
						c = float.class;
						break;
					case DOUBLE_TYPE:
						c = double.class;
						break;
				}
			} else {
				c = Object.class;
			}
		}
		return c;
	}

	public static void getArrayCellValue(Value v, Object array, int index) {
		if (array instanceof Object[]) {
			Object value = Array.get(array, index);
			assert v.set(value);
		} else {
			HiClassArray arrayClass = (HiClassArray) v.valueClass;
			HiClass cellClass = arrayClass.cellClass;
			PrimitiveType type = cellClass.getPrimitiveType();
			switch (type) {
				case BOOLEAN_TYPE:
					v.bool = Array.getBoolean(array, index);
					break;
				case CHAR_TYPE:
					v.character = Array.getChar(array, index);
					break;
				case BYTE_TYPE:
					v.byteNumber = Array.getByte(array, index);
					break;
				case SHORT_TYPE:
					v.shortNumber = Array.getShort(array, index);
					break;
				case INT_TYPE:
					v.intNumber = Array.getInt(array, index);
					break;
				case LONG_TYPE:
					v.longNumber = Array.getLong(array, index);
					break;
				case FLOAT_TYPE:
					v.floatNumber = Array.getFloat(array, index);
					break;
				case DOUBLE_TYPE:
					v.doubleNumber = Array.getDouble(array, index);
					break;
			}
		}
	}

	public static void setArrayIndex(HiClass clazz, Object parentArray, int index, Value value, Value dst) {
		dst.valueClass = clazz;

		if (clazz.isArray()) {
			dst.object = value.getArray();
			if (value.ctx.exitFromBlock()) {
				return;
			}
			Array.set(parentArray, index, dst.object);
		} else if (clazz.isPrimitive()) {
			// @autoboxing
			switch (clazz.getPrimitiveType()) {
				case BOOLEAN_TYPE:
					dst.bool = value.getBoolean();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setBoolean(parentArray, index, dst.bool);
					break;
				case CHAR_TYPE:
					dst.character = value.getChar();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setChar(parentArray, index, dst.character);
					break;
				case BYTE_TYPE:
					dst.byteNumber = value.getByte();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setByte(parentArray, index, dst.byteNumber);
					break;
				case SHORT_TYPE:
					dst.shortNumber = value.getShort();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setShort(parentArray, index, dst.shortNumber);
					break;
				case INT_TYPE:
					dst.intNumber = value.getInt();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setInt(parentArray, index, dst.intNumber);
					break;
				case LONG_TYPE:
					dst.longNumber = value.getLong();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setLong(parentArray, index, dst.longNumber);
					break;
				case FLOAT_TYPE:
					dst.floatNumber = value.getFloat();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setFloat(parentArray, index, dst.floatNumber);
					break;
				case DOUBLE_TYPE:
					dst.doubleNumber = value.getDouble();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setDouble(parentArray, index, dst.doubleNumber);
					break;
			}
		} else {
			// @autoboxing
			if (value.valueClass.isPrimitive()) {
				value.object = ((HiClassPrimitive) value.valueClass).box(value.ctx, value);
				value.valueClass = value.valueClass.getAutoboxClass();
				if (value.ctx.exitFromBlock()) {
					return;
				}
			}
			dst.object = value.getObject();
			dst.originalValueClass = null;
			Array.set(parentArray, index, dst.object);
		}
	}

	public static void setArray(HiClass clazz, Object array, int index, Value value) {
		if (clazz.isArray()) {
			Array.set(array, index, value.getArray());
		} else if (clazz.isPrimitive()) {
			switch (HiFieldPrimitive.getAutoType(clazz)) {
				case BOOLEAN_TYPE:
					Array.setBoolean(array, index, value.getBoolean());
					break;
				case CHAR_TYPE:
					Array.setChar(array, index, value.getChar());
					break;
				case BYTE_TYPE:
					Array.setByte(array, index, value.getByte());
					break;
				case SHORT_TYPE:
					Array.setShort(array, index, value.getShort());
					break;
				case INT_TYPE:
					Array.setInt(array, index, value.getInt());
					break;
				case LONG_TYPE:
					Array.setLong(array, index, value.getLong());
					break;
				case FLOAT_TYPE:
					Array.setFloat(array, index, value.getFloat());
					break;
				case DOUBLE_TYPE:
					Array.setDouble(array, index, value.getDouble());
					break;
			}
		} else {
			Array.set(array, index, value.getObject(clazz));
		}
	}
}
