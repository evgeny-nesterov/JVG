package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

/**
 * used only in precompile after initialization type is defined
 */
public class HiFieldVar extends HiField<Object> {
	public HiFieldVar(Type type, String name) {
		super(type, name);
	}

	private HiField typedField;

	public HiField getTypedField(ClassResolver classResolver) {
		if (typedField == null) {
			typedField = HiFieldVar.getField(getClass(classResolver), name, initializer, token);
		}
		return typedField;
	}

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		return true;
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		getTypedField(ctx).get(ctx, value);
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		getTypedField(ctx).set(ctx, value);
	}

	@Override
	public Object get() {
		return null;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return null;
	}
}
