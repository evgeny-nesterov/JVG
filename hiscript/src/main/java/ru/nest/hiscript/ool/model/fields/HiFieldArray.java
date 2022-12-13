package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

import java.lang.reflect.Array;

public class HiFieldArray extends HiField<Object> {
	public HiFieldArray(Type type, String name) {
		super(type, name);
	}

	public Object array;

	public HiClass arrayType;

	@Override
	public void get(RuntimeContext ctx, Value value) {
		// check value on array and on object
		if (!value.type.isArray() && !(value.type.isObject() && value.type.superClass == null)) {
			ctx.throwRuntimeException("array is expected");
			return;
		}

		HiClass type = getClass(ctx);
		if (!autoCast(value.type, type)) {
			ctx.throwRuntimeException("incompatible types; found " + type.getClassName() + ", required " + value.type.getClassName());
			return;
		}

		value.valueType = Value.VALUE;
		value.type = arrayType != null ? arrayType : getClass(ctx);
		value.array = array;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		declared = true;
		if (value.type == HiClass.getNullClass()) {
			array = null;
		} else if (!value.type.isArray()) {
			ctx.throwRuntimeException("array is expected");
			return;
		} else {
			// check cast
			HiClass type = getClass(ctx);
			if (!autoCast(value.type, type)) {
				ctx.throwRuntimeException("incompatible types; found " + value.type.getClassName() + ", required " + type.getClassName());
				return;
			}

			array = value.array;
			arrayType = value.type;
		}
		initialized = true;
	}

	@Override
	public Object get() {
		return array;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		Class javaClass = arrayType.getJavaClass();
		if (javaClass != null) {
			Class roolCellClass = javaClass;
			while (roolCellClass.isArray()) {
				roolCellClass = javaClass.getComponentType();
			}
			if (roolCellClass.isPrimitive()) {
				return array;
			} else {
				return getJavaArray(ctx, array, javaClass.getComponentType());
			}
		}
		return null;
	}

	private Object getJavaArray(RuntimeContext ctx, Object array, Class cellClass) {
		int length = Array.getLength(array);
		Object javaArray = Array.newInstance(cellClass, length);
		for (int i = 0; i < length; i++) {
			Object cellValue = Array.get(array, i);
			if (cellValue == null) {
				continue;
			}
			Object javaCellValue = null;
			if (cellClass.isArray()) {
				javaCellValue = getJavaArray(ctx, cellValue, cellClass.getComponentType());
			} else {
				HiObject cellObject = (HiObject) cellValue;
				javaCellValue = cellObject.getJavaValue(ctx);
			}
			Array.set(array, i, javaCellValue);
		}
		return javaArray;
	}
}
