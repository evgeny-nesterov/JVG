package ru.nest.hiscript.ool.model.classes;

import java.io.IOException;
import java.util.HashMap;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

public class HiClassPrimitive extends HiClass {
	public final static HiClassPrimitive CHAR = new HiClassPrimitive("char");

	public final static HiClassPrimitive BOOLEAN = new HiClassPrimitive("boolean");

	public final static HiClassPrimitive BYTE = new HiClassPrimitive("byte");

	public final static HiClassPrimitive SHORT = new HiClassPrimitive("short");

	public final static HiClassPrimitive INT = new HiClassPrimitive("int");

	public final static HiClassPrimitive FLOAT = new HiClassPrimitive("float");

	public final static HiClassPrimitive LONG = new HiClassPrimitive("long");

	public final static HiClassPrimitive DOUBLE = new HiClassPrimitive("double");

	public final static HiClassPrimitive VOID = new HiClassPrimitive("void");

	private HiClassPrimitive(String name) {
		super((HiClass) null, null, name, CLASS_TYPE_TOP);
	}

	private static HashMap<String, HiClassPrimitive> primitiveClasses = new HashMap<String, HiClassPrimitive>();
	static {
		primitiveClasses.put("char", CHAR);
		primitiveClasses.put("boolean", BOOLEAN);
		primitiveClasses.put("byte", BYTE);
		primitiveClasses.put("short", SHORT);
		primitiveClasses.put("int", INT);
		primitiveClasses.put("float", FLOAT);
		primitiveClasses.put("long", LONG);
		primitiveClasses.put("double", DOUBLE);
		primitiveClasses.put("void", VOID);
	}

	public static HiClass getPrimitiveClass(String name) {
		return primitiveClasses.get(name);
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeByte(HiClass.CLASS_PRIMITIVE);
		os.writeUTF(name);
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		return getPrimitiveClass(os.readUTF());
	}
}
