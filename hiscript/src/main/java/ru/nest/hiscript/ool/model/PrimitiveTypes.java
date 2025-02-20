package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.classes.HiClassVar;

public interface PrimitiveTypes {
	int BYTE = 0;

	int SHORT = 1;

	int CHAR = 2;

	int INT = 3;

	int LONG = 4;

	int FLOAT = 5;

	int DOUBLE = 6;

	int BOOLEAN = 7;

	int VOID = 8;

	int VAR = 9;

	int INVALID = 10;

	int ANY = 11;

	Type[] primitiveTypes = {Type.charType, Type.byteType, Type.shortType, Type.intType, Type.longType, Type.floatType, Type.doubleType, Type.booleanType, Type.voidType, Type.varType};

	HiClassPrimitive TYPE_CHAR = HiClassPrimitive.CHAR;

	HiClassPrimitive TYPE_BYTE = HiClassPrimitive.BYTE;

	HiClassPrimitive TYPE_SHORT = HiClassPrimitive.SHORT;

	HiClassPrimitive TYPE_INT = HiClassPrimitive.INT;

	HiClassPrimitive TYPE_LONG = HiClassPrimitive.LONG;

	HiClassPrimitive TYPE_FLOAT = HiClassPrimitive.FLOAT;

	HiClassPrimitive TYPE_DOUBLE = HiClassPrimitive.DOUBLE;

	HiClassPrimitive TYPE_BOOLEAN = HiClassPrimitive.BOOLEAN;

	HiClassPrimitive TYPE_VOID = HiClassPrimitive.VOID;

	HiClassVar TYPE_VAR = HiClassVar.VAR;

	HiClass[] primitiveClasses = {TYPE_CHAR, TYPE_BYTE, TYPE_SHORT, TYPE_INT, TYPE_LONG, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOLEAN, TYPE_VOID, TYPE_VAR};
}
