package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
		super(HiClass.systemClassLoader, null, null, name, CLASS_TYPE_TOP, null);
	}

	private static Map<String, HiClassPrimitive> primitiveClasses = new HashMap<>();

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
	public boolean isNumber() {
		return this != BOOLEAN && this != VOID;
	}

	@Override
	public boolean isIntNumber() {
		return this == INT || this == LONG || this == BYTE || this == SHORT || this == CHAR;
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

	@Override
	public Class getJavaClass() {
		switch (name) {
			case "short":
				return short.class;
			case "byte":
				return byte.class;
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "double":
				return double.class;
			case "float":
				return float.class;
			case "char":
				return char.class;
			case "boolean":
				return boolean.class;
		}
		return null;
	}
}
