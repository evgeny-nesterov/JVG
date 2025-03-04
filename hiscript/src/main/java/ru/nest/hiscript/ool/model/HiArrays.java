package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.runtime.Value;

import java.lang.reflect.Array;

public class HiArrays implements PrimitiveTypes {
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
					int typeIndex = clazz.getPrimitiveType();
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
					c = Class.forName(prefix + "L" + Object.class.getName() + ";");
				}
			} catch (Exception e) {
			}
		} else {
			if (clazz.isPrimitive()) {
				int typeIndex = clazz.getPrimitiveType();
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
			int typeIndex = cellClass.getPrimitiveType();
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
			int typeIndex = clazz.getPrimitiveType();
			switch (typeIndex) {
				case BOOLEAN:
					dst.bool = value.getBoolean();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setBoolean(parentArray, index, dst.bool);
					break;
				case CHAR:
					dst.character = value.getChar();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setChar(parentArray, index, dst.character);
					break;
				case BYTE:
					dst.byteNumber = value.getByte();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setByte(parentArray, index, dst.byteNumber);
					break;
				case SHORT:
					dst.shortNumber = value.getShort();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setShort(parentArray, index, dst.shortNumber);
					break;
				case INT:
					dst.intNumber = value.getInt();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setInt(parentArray, index, dst.intNumber);
					break;
				case LONG:
					dst.longNumber = value.getLong();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setLong(parentArray, index, dst.longNumber);
					break;
				case FLOAT:
					dst.floatNumber = value.getFloat();
					if (value.ctx.exitFromBlock()) {
						return;
					}
					Array.setFloat(parentArray, index, dst.floatNumber);
					break;
				case DOUBLE:
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
			int typeIndex = HiFieldPrimitive.getAutoType(clazz);
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
			Array.set(array, index, value.getObject(clazz));
		}
	}
}
