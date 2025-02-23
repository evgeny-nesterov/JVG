package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

/**
 * Field to wrap object field by get and set methods
 */
public class HiPojoField extends HiField<Object> {
	public HiPojoField(Type fieldType, HiClass fieldClass, String name, HiObject object, HiField objectField) {
		super(fieldType, name);
		this.object = object;
		this.objectField = objectField;
		this.fieldClass = fieldClass;

		String originalFieldName = objectField.getVariableName();
		char firstNameChar = Character.toUpperCase(originalFieldName.charAt(0));
		StringBuilder getMethodName = new StringBuilder("get").append(firstNameChar);
		StringBuilder setMethodName = new StringBuilder("set").append(firstNameChar);
		if (originalFieldName.length() > 1) {
			getMethodName.append(originalFieldName, 1, originalFieldName.length());
			setMethodName.append(originalFieldName, 1, originalFieldName.length());
		}
		this.getMethodName = getMethodName.toString();
		this.setMethodName = setMethodName.toString();
	}

	private HiClass fieldClass;

	private HiObject object;

	private String getMethodName;

	private String setMethodName;

	private HiField objectField;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		return true;
	}

	private HiMethod getMethod;

	@Override
	public void get(RuntimeContext ctx, Value value) {
		if (getMethod == null) {
			getMethod = object.clazz.getMethod(ctx, getMethodName);
		}
		ctx.enterMethod(getMethod, object);
		try {
			getMethod.invoke(ctx, object.clazz, object, null);

			// @generic
			if (ctx.value.valueClass.isGeneric()) {
				ctx.value.valueClass = fieldClass;
			}
		} finally {
			ctx.exit();
			ctx.isReturn = false;
		}
	}

	private HiMethod setMethod;

	private HiField setArgField;

	@Override
	public void set(RuntimeContext ctx, Value value) {
		if (setMethod == null) {
			HiClass originalClass = objectField.getValueClass(ctx);
			setMethod = object.clazz.searchMethod(ctx, setMethodName, originalClass);
			NodeArgument arg = setMethod.arguments[0];
			setArgField = HiField.getField(arg.clazz, arg.name, arg.getToken());
		}
		ctx.enterMethod(setMethod, object);
		try {
			setArgField.set(ctx, value);
			ctx.addVariable(setArgField);
			setMethod.invoke(ctx, object.clazz, object, new HiField[] {setArgField});
		} finally {
			ctx.removeVariable(setArgField.name);
			ctx.exit();
			ctx.isReturn = false;
		}
	}

	@Override
	public Object get() {
		throw new RuntimeException("not supported");
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		throw new RuntimeException("not supported");
	}
}
