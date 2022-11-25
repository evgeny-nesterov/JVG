package ru.nest.hiscript.ool.model;

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

	public final static HiClass TYPE_BOOLEAN = HiClass.getPrimitiveClass("boolean");

	public final static HiClass TYPE_CHAR = HiClass.getPrimitiveClass("char");

	public final static HiClass TYPE_BYTE = HiClass.getPrimitiveClass("byte");

	public final static HiClass TYPE_SHORT = HiClass.getPrimitiveClass("short");

	public final static HiClass TYPE_INT = HiClass.getPrimitiveClass("int");

	public final static HiClass TYPE_LONG = HiClass.getPrimitiveClass("long");

	public final static HiClass TYPE_FLOAT = HiClass.getPrimitiveClass("float");

	public final static HiClass TYPE_DOUBLE = HiClass.getPrimitiveClass("double");
}
