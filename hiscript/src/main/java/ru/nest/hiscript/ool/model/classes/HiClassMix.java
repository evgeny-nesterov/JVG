package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class HiClassMix extends HiClass {
	public HiClass[] classes;

	public HiClassMix(HiClassLoader classLoader, HiClass[] classes, HiClass enclosingClass) {
		this.classLoader = classLoader;
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

	// for decode
	public HiClassMix(HiClassLoader classLoader) {
		this.classLoader = classLoader;
		this.type = CLASS_MIX;
		this.name = "mix";
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
				validationInfo.error("invalid class '" + clazz.getNameDescr() + "' in mix class: object class expected", getToken());
				valid = false;
			}
		}
		return valid;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		code(os, CLASS_MIX);
		os.writeUTF(fullName);
		os.writeClasses(classes);
		os.writeClass(enclosingClass);
	}

	public static HiClassMix decode(DecodeContext os, int classIndex) throws IOException {
		HiClassMix clazz = (HiClassMix) HiClass.decodeObject(os, CLASS_MIX, classIndex);
		clazz.fullName = os.readUTF();
		clazz.classes = os.readClasses();
		os.readClass(c -> clazz.enclosingClass = c);
		return clazz;
	}
}
