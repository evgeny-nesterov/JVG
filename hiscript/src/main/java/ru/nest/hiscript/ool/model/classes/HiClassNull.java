package ru.nest.hiscript.ool.model.classes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

public class HiClassNull extends HiClass {
	public final static HiClassNull NULL = new HiClassNull();

	private HiClassNull() {
		super((HiClass) null, null, "null", CLASS_TYPE_TOP);
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

	public static HiClass decode(DecodeContext os) throws IOException {
		return NULL;
	}

	@Override
	public Class getJavaClass() {
		return null;
	}
}
