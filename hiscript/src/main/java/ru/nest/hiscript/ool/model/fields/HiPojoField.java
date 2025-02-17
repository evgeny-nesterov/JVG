package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
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
	public HiPojoField(HiObject object, HiField objectField, String name) {
		super(objectField.type, name);
		this.object = object;

		String originalFieldName = objectField.name;
		char firstNameChar = Character.toUpperCase(originalFieldName.charAt(0));
		getMethodName = new StringBuilder("get").append(firstNameChar);
		setMethodName = new StringBuilder("set").append(firstNameChar);
		if (originalFieldName.length() > 1) {
			getMethodName.append(originalFieldName, 1, originalFieldName.length());
			setMethodName.append(originalFieldName, 1, originalFieldName.length());
		}
	}

	private HiObject object;

	private StringBuilder getMethodName;

	private StringBuilder setMethodName;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		return true;
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		HiMethod getMethod = object.clazz.getMethod(ctx, getMethodName.toString());
		ctx.enterMethod(getMethod, object);
		try {
			getMethod.invoke(ctx, object.clazz, object, null);
		} finally {
			ctx.exit();
			ctx.isReturn = false;
		}
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		HiMethod setMethod = object.clazz.getMethod(ctx, setMethodName.toString(), getClass(ctx));
		NodeArgument arg = setMethod.arguments[0];
		ctx.enterMethod(setMethod, object);
		try {
			HiField argField = HiField.getField(arg.clazz, arg.name, arg.getToken());
			argField.set(ctx, value);
			ctx.addVariable(argField);
			setMethod.invoke(ctx, object.clazz, object, new HiField[] {argField});
		} finally {
			ctx.removeVariable(arg.name);
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
