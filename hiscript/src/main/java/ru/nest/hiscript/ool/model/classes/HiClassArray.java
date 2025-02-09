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
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;

import java.io.IOException;

public class HiClassArray extends HiClass {
	public HiClass cellClass;

	private HiClass rootCellClass;

	public int dimension;

	// name for array generation from java
	public String className;

	public HiClassArray(HiClassLoader classLoader, HiClass cellClass) {
		super(classLoader, OBJECT_CLASS, null, "0" + cellClass.fullName, CLASS_TYPE_TOP, null);
		init(cellClass);
	}

	public HiClass getRootCellClass() {
		return rootCellClass;
	}

	private void init(HiClass cellClass) {
		this.cellClass = cellClass;
		this.rootCellClass = cellClass.isArray() ? ((HiClassArray) cellClass).rootCellClass : cellClass;

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
		// do not set type Type.getType(this)
		constructors[0] = new HiConstructor(this, null, null, constructorModifiers, null, (NodeArgument[]) null, null, null, null, BodyConstructorType.NONE);

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

	@Override
	public int getArrayDimension() {
		return dimension;
	}

	@Override
	public Class getJavaClass(HiRuntimeEnvironment env) {
		Class javaClass = env.javaClassesMap.get(this);
		if (javaClass != null) {
			return javaClass;
		}

		HiClass rootCellClass = cellClass;
		while (rootCellClass.isArray()) {
			rootCellClass = ((HiClassArray) rootCellClass).cellClass;
		}
		Class javaRootCellClass = cellClass.getJavaClass(env);
		if (javaRootCellClass != null) {
			String name = getJavaClassName(javaRootCellClass);
			try {
				javaClass = Class.forName(name);
				env.javaClassesMap.put(this, javaClass);
				return javaClass;
			} catch (ClassNotFoundException e) {
			}
		}
		return null;
	}

	private String getJavaClassName(Class javaRootCellClass) {
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
		return name;
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

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeByte(HiClass.CLASS_ARRAY);
		os.writeClass(cellClass);
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		// assumed cell class is already loaded
		HiClass cellClass = os.readClass();
		return cellClass.getArrayClass();
	}
}
