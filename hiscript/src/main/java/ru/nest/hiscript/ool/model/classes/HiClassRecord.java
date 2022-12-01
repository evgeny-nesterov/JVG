package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;

public class HiClassRecord extends HiClass {
	public HiConstructor defaultConstructor;

	public HiClassRecord(String name, int type) {
		super(Type.recordType, null, null, name, type);
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public boolean isObject() {
		return true;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public boolean isRecord() {
		return true;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		code(os, CLASS_RECORD);
		os.write(defaultConstructor);
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		HiClassRecord recordClass = (HiClassRecord) HiClass.decodeObject(os, CLASS_RECORD);
		recordClass.defaultConstructor = os.read(HiConstructor.class);
		recordClass.defaultConstructor.clazz = recordClass;
		return recordClass;
	}
}
