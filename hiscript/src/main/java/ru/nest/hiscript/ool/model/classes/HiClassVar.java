package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.ClassLocationType;
import ru.nest.hiscript.ool.model.ClassType;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;

import java.io.IOException;

public class HiClassVar extends HiClass {
	public final static HiClassVar VAR = new HiClassVar();

	private HiClassVar() {
		super(null, null, null, "var", ClassLocationType.top, null);
	}

	@Override
	public boolean isVar() {
		return true;
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public boolean isNumber() {
		return true;
	}

	@Override
	public boolean isIntNumber() {
		return true;
	}

	@Override
	public boolean isLongNumber() {
		return true;
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public boolean isAnnotation() {
		return true;
	}

	@Override
	public boolean isNull() {
		return true;
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
	public boolean isRecord() {
		return true;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeEnum(ClassType.CLASS_VAR);
	}

	public static HiClass decode(DecodeContext os) {
		return VAR;
	}

	@Override
	public Class getJavaClass(HiRuntimeEnvironment env) {
		return null;
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.VAR_TYPE;
	}

	@Override
	public String getNameDescr() {
		return fullName;
	}
}
