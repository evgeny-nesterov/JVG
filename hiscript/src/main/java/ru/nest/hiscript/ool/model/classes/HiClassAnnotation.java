package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;

public class HiClassAnnotation extends HiClass {
	public HiClassAnnotation(String name, int type) {
		super((HiClass) null, null, name, type, null);
	}

	@Override
	public boolean isAnnotation() {
		return true;
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeByte(HiClass.CLASS_ANNOTATION);
		// TODO
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		// TODO
		return null;
	}

	@Override
	public Class getJavaClass() {
		return null;
	}
}
