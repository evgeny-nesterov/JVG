package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

/**
 * used only in precompile after initialization type is defined
 */
public class HiFieldVar extends HiField<Object> {
	public HiFieldVar(Type type, String name) {
		super(type, name);
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		throw new RuntimeException("not supported");
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		throw new RuntimeException("not supported");
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
