package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class HiClassRecord extends HiClass {
	public HiConstructor defaultConstructor;

	public HiClassRecord(HiClassLoader classLoader, String name, NodeGenerics generics, int type, ClassResolver classResolver) {
		super(classLoader, Type.recordType, null, null, null, name, generics, type, classResolver);
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
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = super.validate(validationInfo, ctx);
		return valid;
	}

	@Override
	protected HiConstructor _searchConstructor(ClassResolver classResolver, HiClass[] argTypes) {
		HiConstructor constructor = super._searchConstructor(classResolver, argTypes);
		if (constructor == null) {
			if (matchConstructor(classResolver, defaultConstructor, argTypes, MatchMethodArgumentsType.soft)) {
				constructor = defaultConstructor;
			}
		}
		return constructor;
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

	@Override
	public Class getJavaClass() {
		return null;
	}
}
