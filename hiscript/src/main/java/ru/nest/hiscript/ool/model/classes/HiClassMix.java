package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class HiClassMix extends HiClass {
	public HiClass[] classes;

	public HiClassMix(HiClass[] classes, HiClass enclosingClass) {
		this.classes = classes;
		this.type = CLASS_MIX;
		this.name = "mix";
		if (enclosingClass != null) {
			this.fullName = enclosingClass.fullName + "$$mix";
		} else {
			this.fullName = name;
		}
		this.hashCode = fullName.hashCode();
	}

	@Override
	public boolean isMix() {
		return true;
	}

	@Override
	public void init(ClassResolver classResolver) {
		for (HiClass c : classes) {
			c.init(classResolver);
		}
	}

	@Override
	public boolean isInstanceof(HiClass clazz) {
		for (HiClass c : classes) {
			if (c.isInstanceof(clazz)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean _validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		for (HiClass clazz : classes) {
			clazz.validate(validationInfo, ctx);
			valid &= clazz.valid;
			if (clazz.isObject()) {
				if (clazz.valid) {
					superClass = clazz.getCommonClass(superClass);
				}
			} else {
				validationInfo.error("Invalid class '" + clazz.getNameDescr() + "' in mix class: object class expected", getToken());
				valid = false;
			}
		}
		return valid;
	}

	@Override
	public boolean isStatic() {
		return false;
	}
}
