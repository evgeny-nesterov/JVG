package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;

public class HiClassNull extends HiClass {
	public final static HiClassNull NULL = new HiClassNull();

	private HiClassNull() {
		super(HiClass.systemClassLoader, null, null, "null", CLASS_TYPE_TOP, null);
	}

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeByte(HiClass.CLASS_NULL);
	}

	public static HiClass decode(DecodeContext os) {
		return NULL;
	}

	@Override
	public Class getJavaClass() {
		return null;
	}

	@Override
	public boolean isInstanceof(HiClass clazz) {
		return false;
	}
}
