package ru.nest.hiscript.ool.model.classes;

import java.io.IOException;
import java.util.HashMap;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiConstructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;

public class HiClassArray extends HiClass {
	public HiClass cellClass;

	public int dimension;

	private HiClassArray(HiClass cellClass) {
		super(OBJECT_CLASS, null, "0" + cellClass.fullName, CLASS_TYPE_TOP);
		init(cellClass);
	}

	private void init(HiClass cellClass) {
		this.cellClass = cellClass;

		if (cellClass.isArray()) {
			className = "[" + ((HiClassArray) cellClass).className;
		} else {
			className = "[" + cellClass.fullName;
		}

		if (cellClass instanceof HiClassArray) {
			dimension = ((HiClassArray) cellClass).dimension + 1;
		} else {
			dimension = 1;
		}

		constructors = new HiConstructor[1];
		Modifiers constructorModifiers = new Modifiers();
		constructorModifiers.setAccess(ModifiersIF.ACCESS_PUBLIC);
		constructors[0] = new HiConstructor(this, constructorModifiers, (NodeArgument[]) null, null, null, BodyConstructorType.NONE);

		fields = new HiField[1];
		fields[0] = HiField.getField(Type.getPrimitiveType("int"), "length");
		fields[0].getModifiers().setAccess(ModifiersIF.ACCESS_PUBLIC);
		fields[0].getModifiers().setFinal(true);
	}

	@Override
	public boolean isArray() {
		return dimension > 0;
	}

	@Override
	public HiClass getArrayType() {
		return cellClass;
	}

	private static HashMap<HiClass, HiClassArray> arrayClasses = new HashMap<HiClass, HiClassArray>();

	public static HiClassArray getArrayClass(HiClass cellClass) {
		HiClassArray c = arrayClasses.get(cellClass);
		if (c == null) {
			c = new HiClassArray(cellClass);
			arrayClasses.put(cellClass, c);
		}
		return c;
	}

	public static HiClassArray getArrayClass(HiClass cellClass, int dimensions) {
		HiClassArray c = null;
		for (int i = 0; i < dimensions; i++) {
			c = getArrayClass(cellClass);
			cellClass = c;
		}
		return c;
	}

	// name for array generation from java
	public String className;

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeByte(HiClass.CLASS_ARRAY);
		os.writeClass(cellClass);
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		// assumed cell class is already read
		HiClass cellClass = os.readClass();
		return getArrayClass(cellClass);
	}
}
