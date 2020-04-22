package ru.nest.hiscript.ool.model.classes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

public class ClazzNull extends Clazz {
	public final static ClazzNull NULL = new ClazzNull();

	private ClazzNull() {
		super((Clazz) null, null, "null", CLASS_TYPE_TOP);
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
		os.writeByte(Clazz.CLASS_NULL);
	}

	public static Clazz decode(DecodeContext os) throws IOException {
		return NULL;
	}
}
