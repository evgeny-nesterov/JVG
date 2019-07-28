package script.ool.model.classes;

import java.io.IOException;
import java.util.HashMap;

import script.ool.model.Clazz;
import script.ool.model.Constructor;
import script.ool.model.Constructor.BodyConstructorType;
import script.ool.model.Field;
import script.ool.model.Modifiers;
import script.ool.model.ModifiersIF;
import script.ool.model.Type;
import script.ool.model.nodes.CodeContext;
import script.ool.model.nodes.DecodeContext;
import script.ool.model.nodes.NodeArgument;

public class ClazzArray extends Clazz {
	public Clazz cellClass;

	public int dimension;

	private ClazzArray(Clazz cellClass) {
		super(OBJECT_CLASS, null, "0" + cellClass.fullName, CLASS_TYPE_TOP);
		init(cellClass);
	}

	private void init(Clazz cellClass) {
		this.cellClass = cellClass;

		if (cellClass.isArray()) {
			className = "[" + ((ClazzArray) cellClass).className;
		} else {
			className = "[" + cellClass.fullName;
		}

		if (cellClass instanceof ClazzArray) {
			dimension = ((ClazzArray) cellClass).dimension + 1;
		} else {
			dimension = 1;
		}

		constructors = new Constructor[1];
		Modifiers constructorModifiers = new Modifiers();
		constructorModifiers.setAccess(ModifiersIF.ACCESS_PUBLIC);
		constructors[0] = new Constructor(this, constructorModifiers, (NodeArgument[]) null, null, null, BodyConstructorType.NONE);

		fields = new Field[1];
		fields[0] = Field.getField(Type.getPrimitiveType("int"), "length");
		fields[0].getModifiers().setAccess(ModifiersIF.ACCESS_PUBLIC);
		fields[0].getModifiers().setFinal(true);
	}

	@Override
	public boolean isArray() {
		return dimension > 0;
	}

	@Override
	public Clazz getArrayType() {
		return cellClass;
	}

	private static HashMap<Clazz, ClazzArray> arrayClasses = new HashMap<Clazz, ClazzArray>();

	public static ClazzArray getArrayClass(Clazz cellClass) {
		ClazzArray c = arrayClasses.get(cellClass);
		if (c == null) {
			c = new ClazzArray(cellClass);
			arrayClasses.put(cellClass, c);
		}
		return c;
	}

	public static ClazzArray getArrayClass(Clazz cellClass, int dimensions) {
		ClazzArray c = null;
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
		os.writeByte(Clazz.CLASS_ARRAY);
		os.writeClass(cellClass);
	}

	public static Clazz decode(DecodeContext os) throws IOException {
		// assumed cell class is already readed
		Clazz cellClass = os.readClass();
		return getArrayClass(cellClass);
	}
}
