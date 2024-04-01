package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.classes.HiClassVar;

public interface PrimitiveTypes {
	int CHAR = 0;

	int BYTE = 1;

	int SHORT = 2;

	int INT = 3;

	int LONG = 4;

	int FLOAT = 5;

	int DOUBLE = 6;

	int BOOLEAN = 7;

	int VOID = 8;

	int VAR = 9;

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
