package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.MethodSignature;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HiClassRecord extends HiClass {
	public HiConstructor defaultConstructor;

	public HiClassRecord(HiClassLoader classLoader, String name, NodeGenerics generics, int type, ClassResolver classResolver) {
		super(classLoader, Type.recordType, null, null, name, generics, type, classResolver);
	}

	// for decode
	public HiClassRecord(String name, NodeGenerics generics, int type) {
		super(Type.recordType, name, generics, type);
		// init(...) is in decode
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
	protected boolean _validateNext(ValidationInfo validationInfo, CompileClassContext ctx) {
		return defaultConstructor.validate(validationInfo, ctx);
	}

	@Override
	protected List<HiConstructor> _searchConstructors(ClassResolver classResolver, MethodSignature signature) {
		List<HiConstructor> constructors = super._searchConstructors(classResolver, signature);
		if (constructors == null || constructors.size() == 0) {
			if (isMatchConstructorArguments(classResolver, defaultConstructor, signature.argClasses)) {
				constructors = Arrays.asList(defaultConstructor);
			}
		}
		return constructors;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		code(os, CLASS_RECORD);
		os.write(defaultConstructor);
	}

	public static HiClass decode(DecodeContext os, int classIndex) throws IOException {
		HiClassRecord recordClass = (HiClassRecord) HiClass.decodeObject(os, CLASS_RECORD, classIndex);
		recordClass.defaultConstructor = os.read(HiConstructor.class);
		recordClass.defaultConstructor.clazz = recordClass;
		return recordClass;
	}

	@Override
	public Class getJavaClass(HiRuntimeEnvironment env) {
		return null;
	}
}
