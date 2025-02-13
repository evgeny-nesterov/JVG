package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

/**
 * Field to wrap record field by get and set methods
 */
public class HiRecordField extends HiField<Object> {
	public HiRecordField(RuntimeContext ctx, HiObject recordObject, int recordArgumentIndex, HiField field, String name) {
		super(field.type, name);
		this.recordObject = recordObject;
		this.recordArgumentIndex = recordArgumentIndex;
		this.field = field;

		char firstNameChar = Character.toUpperCase(field.name.charAt(0));
		StringBuilder getMethodName = new StringBuilder("get").append(firstNameChar);
		StringBuilder setMethodName = new StringBuilder("set").append(firstNameChar);
		if (field.name.length() > 1) {
			getMethodName.append(field.name, 1, field.name.length());
			setMethodName.append(field.name, 1, field.name.length());
		}
		getMethod = recordObject.clazz.getMethod(ctx, getMethodName.toString());
		setMethod = recordObject.clazz.getMethod(ctx, setMethodName.toString(), field.getClass(ctx));
	}

	private HiObject recordObject;

	private int recordArgumentIndex;

	private HiField field;

	private HiMethod getMethod;

	private HiMethod setMethod;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		return true;
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		ctx.enterMethod(getMethod, recordObject);
		try {
			getMethod.invoke(ctx, recordObject.clazz, recordObject, null);
		} finally {
			ctx.exit();
			ctx.isReturn = false;
		}
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		NodeArgument arg = setMethod.arguments[0];
		ctx.enterMethod(getMethod, recordObject);
		try {
			HiField argField = HiField.getField(arg.clazz, arg.name, arg.getToken());
			argField.set(ctx, value);
			ctx.addVariable(argField);
			setMethod.invoke(ctx, recordObject.clazz, recordObject, new HiField[] {argField});
		} finally {
			ctx.removeVariable(arg.name);
			ctx.exit();
			ctx.isReturn = false;
		}
	}

	@Override
	public Object get() {
		return field.get();
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return field.getJava(ctx);
	}
}
