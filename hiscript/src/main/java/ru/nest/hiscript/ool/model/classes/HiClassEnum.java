package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;
import java.util.List;

public class HiClassEnum extends HiClass {
	public List<HiEnumValue> enumValues;

	public HiClassEnum(String name, int type) {
		super(Type.enumType, null, null, name, type);
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public boolean isEnum() {
		return true;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		code(os, CLASS_ENUM);
		os.writeShort(enumValues.size());
		os.write(enumValues);
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		HiClassEnum enumClass = (HiClassEnum) HiClass.decodeObject(os, CLASS_ENUM);
		enumClass.enumValues = os.readList(HiEnumValue.class, os.readShort());
		return enumClass;
	}
}
