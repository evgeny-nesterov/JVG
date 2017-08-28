package script.ool.model;

public interface PrimitiveTypes {
	public final static int CHAR = 0;

	public final static int BYTE = 1;

	public final static int SHORT = 2;

	public final static int INT = 3;

	public final static int FLOAT = 4;

	public final static int LONG = 5;

	public final static int DOUBLE = 6;

	public final static int BOOLEAN = 7;

	public final static int VOID = 8;

	public final static Clazz TYPE_BOOLEAN = Clazz.getPrimitiveClass("boolean");

	public final static Clazz TYPE_CHAR = Clazz.getPrimitiveClass("char");

	public final static Clazz TYPE_BYTE = Clazz.getPrimitiveClass("byte");

	public final static Clazz TYPE_SHORT = Clazz.getPrimitiveClass("short");

	public final static Clazz TYPE_INT = Clazz.getPrimitiveClass("int");

	public final static Clazz TYPE_LONG = Clazz.getPrimitiveClass("long");

	public final static Clazz TYPE_FLOAT = Clazz.getPrimitiveClass("float");

	public final static Clazz TYPE_DOUBLE = Clazz.getPrimitiveClass("double");
}
