package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;

public class HiClassVar extends HiClass {
	public final static HiClassVar VAR = new HiClassVar();

	private HiClassVar() {
		super(HiClass.systemClassLoader, null, null, "var", CLASS_TYPE_TOP, null);
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
		os.writeByte(HiClass.CLASS_VAR);
	}

	public static HiClass decode(DecodeContext os) {
		return VAR;
	}

	@Override
	public Class getJavaClass() {
		return null;
	}

	public int getPrimitiveType() {
		return PrimitiveTypes.VAR;
	}
}
