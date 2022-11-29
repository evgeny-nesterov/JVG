package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class HiClassEnum extends HiClass {
	public List<HiEnumValue> enumValues;

	public HiClassEnum(String name, int type) {
		super(Type.enumType, null, null, name, type);
	}

	private HashMap<String, HiField> enumsMap;

	public void initEnumValues(RuntimeContext ctx) {
		enumsMap = new HashMap<>(enumValues.size());
		HiObject outboundObject = ctx.getOutboundObject(this);
		for (HiEnumValue enumValue : enumValues) {
			ctx.initializingEnumValue = enumValue;
			NodeConstructor.invokeConstructor(ctx, this, enumValue.getArguments(), null, outboundObject);
			if (ctx.exitFromBlock()) {
				return;
			}

			HiFieldObject enumField = new HiFieldObject(Type.getType(this), enumValue.getName(), ctx.value.getObject());
			enumField.getModifiers().setFinal(true);
			enumField.getModifiers().setStatic(true);
			enumField.getModifiers().setAccess(ModifiersIF.ACCESS_PUBLIC);
			enumsMap.put(enumValue.getName(), enumField);
		}
	}

	public HiField getEnumValue(String name) {
		return enumsMap.get(name);
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
