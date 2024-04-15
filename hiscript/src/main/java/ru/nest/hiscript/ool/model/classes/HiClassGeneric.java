package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class HiClassGeneric extends HiClass {
	public HiClass clazz;

	public boolean isSuper;

	public HiClassGeneric(String name, HiClass clazz, boolean isSuper) {
		this.name = name;
		this.clazz = clazz;
		this.isSuper = isSuper;
		this.type = CLASS_GENERIC;
		this.fullName = name;
	}

	@Override
	public boolean isGeneric() {
		return true;
	}

	@Override
	public void init(ClassResolver classResolver) {
		clazz.init(classResolver);
	}

	@Override
	public boolean isInstanceof(HiClass clazz) {
		if (isSuper) {
			return clazz == HiClass.OBJECT_CLASS;
		} else {
			return this.clazz.isInstanceof(clazz);
		}
	}

	@Override
	protected boolean _validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		return clazz.validate(validationInfo, ctx);
	}

	@Override
	public boolean isStatic() {
		return false;
	}
}
