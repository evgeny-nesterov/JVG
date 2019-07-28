package script.ool.model.classes;

import java.io.IOException;
import java.util.HashMap;

import script.ool.model.Clazz;
import script.ool.model.nodes.CodeContext;
import script.ool.model.nodes.DecodeContext;

public class ClazzPrimitive extends Clazz {
	public final static ClazzPrimitive CHAR = new ClazzPrimitive("char");

	public final static ClazzPrimitive BOOLEAN = new ClazzPrimitive("boolean");

	public final static ClazzPrimitive BYTE = new ClazzPrimitive("byte");

	public final static ClazzPrimitive SHORT = new ClazzPrimitive("short");

	public final static ClazzPrimitive INT = new ClazzPrimitive("int");

	public final static ClazzPrimitive FLOAT = new ClazzPrimitive("float");

	public final static ClazzPrimitive LONG = new ClazzPrimitive("long");

	public final static ClazzPrimitive DOUBLE = new ClazzPrimitive("double");

	public final static ClazzPrimitive VOID = new ClazzPrimitive("void");

	private ClazzPrimitive(String name) {
		super((Clazz) null, null, name, CLASS_TYPE_TOP);
	}

	private static HashMap<String, ClazzPrimitive> primitiveClasses = new HashMap<String, ClazzPrimitive>();
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

	public static Clazz getPrimitiveClass(String name) {
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
		os.writeByte(Clazz.CLASS_PRIMITIVE);
		os.writeUTF(name);
	}

	public static Clazz decode(DecodeContext os) throws IOException {
		return getPrimitiveClass(os.readUTF());
	}
}
