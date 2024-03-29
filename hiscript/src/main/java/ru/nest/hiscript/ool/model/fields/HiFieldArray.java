package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.lang.reflect.Array;

public class HiFieldArray extends HiField<Object> {
	public HiFieldArray(Type type, String name) {
		super(type, name);
	}

	public HiFieldArray(HiClass clazz, String name) {
		super(clazz, name);
	}

	public Object array;

	public HiClass arrayType;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		return valueType.type.isNull() || (valueType.type.isArray() && HiClass.autoCast(ctx, valueType.type, fieldClass, false));
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		value.valueType = Value.VALUE;
		value.type = arrayType != null ? arrayType : getClass(ctx);
		value.array = array;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		declared = true;
		if (value.type == HiClassNull.NULL) {
			array = null;
			arrayType = getClass(ctx);
		} else {
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
			Class rootCellClass = javaClass;
			while (rootCellClass.isArray()) {
				rootCellClass = rootCellClass.getComponentType();
			}
			if (rootCellClass.isPrimitive()) {
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
			Object javaCellValue;
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
