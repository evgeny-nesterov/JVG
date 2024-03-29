package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
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
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiClassEnum extends HiClass {
	public List<HiEnumValue> enumValues;

	public HiClassEnum(HiClassLoader classLoader, String name, int type) {
		super(classLoader, Type.enumType, null, null, name, type, null);
	}

	private Map<String, HiField> enumsMap;

	public void initEnumValues(RuntimeContext ctx) {
		enumsMap = new HashMap<>(enumValues.size());
		HiObject outboundObject = ctx.getOutboundObject(this);
		for (HiEnumValue enumValue : enumValues) {
			ctx.initializingEnumValue = enumValue;
			NodeConstructor.invokeConstructor(ctx, this, enumValue.getArguments(), null, outboundObject);
			if (ctx.exitFromBlock()) {
				return;
			}
			enumsMap.put(enumValue.getName(), createField(enumValue.getName(), ctx.value.getObject()));
		}
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = super.validate(validationInfo, ctx);
		if (enumValues != null) {
			for (int i1 = 0; i1 < enumValues.size() - 1; i1++) {
				HiEnumValue enumValue1 = enumValues.get(i1);
				for (int i2 = i1 + 1; i2 < enumValues.size(); i2++) {
					HiEnumValue enumValue2 = enumValues.get(i2);
					if (enumValue1.getName().equals(enumValue2.getName())) {
						validationInfo.error("Variable '" + enumValue1.getName() + "' is already defined in the scope", enumValue2.getToken());
						valid = false;
					}
				}
			}
		}
		return valid;
	}

	private HiFieldObject createField(String name, HiObject value) {
		HiFieldObject enumField = new HiFieldObject(Type.getType(this), name, value);
		enumField.getModifiers().setFinal(true);
		enumField.getModifiers().setStatic(true);
		enumField.getModifiers().setAccess(ModifiersIF.ACCESS_PUBLIC);
		return enumField;
	}

	public HiField getEnumValue(String name) {
		if (enumsMap != null) {
			HiField enumField = enumsMap.get(name);
			if (enumField != null) {
				return enumField;
			}
		}

		// emulate for validation
		for (HiEnumValue enumValue : enumValues) {
			if (enumValue.getName().equals(name)) {
				HiField enumField = createField(enumValue.getName(), null);
				return enumField;
			}
		}
		return null;
	}

	public int getEnumOrdinal(String name) {
		for (HiEnumValue enumValue : enumValues) {
			if (enumValue.getName().equals(name)) {
				return enumValue.getOrdinal();
			}
		}
		return -1;
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

	@Override
	public Class getJavaClass() {
		return null;
	}
}
