package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiConstructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HiClassArray extends HiClass {
	public HiClass cellClass;

	public int dimension;

	public HiClassArray(HiClassLoader classLoader, HiClass cellClass) {
		super(classLoader, OBJECT_CLASS, null, "0" + cellClass.fullName, CLASS_TYPE_TOP, null);
		init(cellClass);
	}

	public HiClass getRootCellClass() {
		HiClass cellClass = this.cellClass;
		while (cellClass.isArray()) {
			cellClass = ((HiClassArray) cellClass).cellClass;
		}
		return cellClass;
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
		constructors[0] = new HiConstructor(this, null, constructorModifiers, (NodeArgument[]) null, null, null, null, BodyConstructorType.NONE);

		fields = new HiField[1];
		fields[0] = HiField.getField(HiClassPrimitive.INT, "length", null);
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
		return cellClass.getArrayClass();
	}

	private static Map<HiClassArray, Class> javaClassesMap = new ConcurrentHashMap<>();

	@Override
	public Class getJavaClass() {
		Class javaClass = javaClassesMap.get(this);
		if (javaClass != null) {
			return javaClass;
		}

		HiClass rootCellClass = cellClass;
		while (rootCellClass.isArray()) {
			rootCellClass = ((HiClassArray) rootCellClass).cellClass;
		}
		Class javaRootCellClass = cellClass.getJavaClass();
		if (javaRootCellClass != null) {
			String name = "";
			for (int i = 0; i < dimension; i++) {
				name += '[';
			}
			if (javaRootCellClass.isPrimitive()) {
				if (javaRootCellClass == boolean.class) {
					name += "Z";
				} else if (javaRootCellClass == byte.class) {
					name += "B";
				} else if (javaRootCellClass == char.class) {
					name += "C";
				} else if (javaRootCellClass == double.class) {
					name += "D";
				} else if (javaRootCellClass == float.class) {
					name += "F";
				} else if (javaRootCellClass == int.class) {
					name += "I";
				} else if (javaRootCellClass == long.class) {
					name += "J";
				} else if (javaRootCellClass == short.class) {
					name += "S";
				}
			} else {
				name += "L" + javaRootCellClass.getName() + ";";
			}
			try {
				javaClass = Class.forName(name);
				javaClassesMap.put(this, javaClass);
				return javaClass;
			} catch (ClassNotFoundException e) {
			}
		}
		return null;
	}

	public static Class<?> getArrayClass(Class<?> componentType) throws ClassNotFoundException {
		ClassLoader classLoader = componentType.getClassLoader();
		String name;
		if (componentType.isArray()) {
			// just add a leading "["
			name = "[" + componentType.getName();
		} else if (componentType == boolean.class) {
			name = "[Z";
		} else if (componentType == byte.class) {
			name = "[B";
		} else if (componentType == char.class) {
			name = "[C";
		} else if (componentType == double.class) {
			name = "[D";
		} else if (componentType == float.class) {
			name = "[F";
		} else if (componentType == int.class) {
			name = "[I";
		} else if (componentType == long.class) {
			name = "[J";
		} else if (componentType == short.class) {
			name = "[S";
		} else {
			// must be an object non-array class
			name = "[L" + componentType.getName() + ";";
		}
		return classLoader != null ? classLoader.loadClass(name) : Class.forName(name);
	}
}
