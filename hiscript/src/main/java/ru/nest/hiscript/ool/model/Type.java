package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 */
public class Type implements TypeArgumentIF, PrimitiveTypes, Codeable, Comparable<Type> {
	public final static int PRIMITIVE = 0;

	public final static int OBJECT = 1;

	public final static int ARRAY = 2;

	private static Map<String, Type> predefinedTypes = new HashMap<>();

	static {
		predefinedTypes.put("char", new Type("char"));
		predefinedTypes.put("boolean", new Type("boolean"));
		predefinedTypes.put("byte", new Type("byte"));
		predefinedTypes.put("short", new Type("short"));
		predefinedTypes.put("int", new Type("int"));
		predefinedTypes.put("float", new Type("float"));
		predefinedTypes.put("long", new Type("long"));
		predefinedTypes.put("double", new Type("double"));
		predefinedTypes.put("void", voidType = new Type("void"));
		predefinedTypes.put("null", new Type("null"));
	}

	/**
	 * Object type
	 */
	private Type(Type parent, String name) {
		this.parent = parent;
		this.cellType = null;
		this.name = name.intern();
		this.dimension = 0;
		this.primitive = false;

		if (parent != null) {
			if (parent.path != null) {
				path = new Type[parent.path.length + 1];
				System.arraycopy(parent.path, 0, path, 0, parent.path.length);
				path[path.length - 1] = parent;
			} else {
				path = new Type[] {parent};
			}
			this.fullName = parent.fullName + "." + name;
		} else {
			this.fullName = name;
		}
	}

	/**
	 * Primitive type
	 */
	private Type(String name) {
		this.parent = null;
		this.cellType = null;
		this.name = name;
		this.dimension = 0;
		this.primitive = true;
		this.fullName = name;
	}

	/**
	 * Array type
	 */
	private Type(Type cellType) {
		this.parent = null;
		this.cellType = cellType;
		this.name = "0" + cellType.name;
		this.dimension = cellType.dimension + 1;
		this.primitive = false;
		this.fullName = "0" + cellType.fullName;

		if (dimension == 1) {
			cellTypeRoot = cellType;
		} else {
			cellTypeRoot = cellType.cellTypeRoot;
		}
	}

	public Type parent;

	public Type[] path;

	public Type cellType;

	public Type cellTypeRoot;

	public String name;

	public String fullName;

	private int dimension;

	public int getDimension() {
		return dimension;
	}

	public Type getCellType() {
		return cellType;
	}

	private boolean primitive;

	public boolean isPrimitive() {
		return primitive;
	}

	public boolean isNull() {
		return this == getNullType();
	}

	@Override
	public int hashCode() {
		return Objects.hash(fullName, dimension);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (o != null && o.getClass() == Type.class) {
			Type t = (Type) o;

			if (!t.fullName.equals(fullName)) {
				return false;
			}

			if (t.primitive != primitive) {
				return false;
			}

			if (t.dimension != dimension) {
				return false;
			} else if (dimension > 0) {
				return t.cellType.equals(cellType);
			} else {
				if (t.parent == null || parent == null) {
					return parent == t.parent;
				} else {
					return t.parent.equals(parent);
				}
			}
		}
		return false;
	}

	public HiClass getClass(RuntimeContext ctx) {
		// Нельзя кэшировать класс, т.к. имена разных классов могут совпадать в разных контекстах
		HiClass clazz = resolveClass(ctx);
		return clazz;
	}

	public HiClass resolveClass(RuntimeContext ctx) {
		if (isPrimitive()) {
			return HiClass.getPrimitiveClass(name);
		}

		if (isArray()) {
			HiClass cellClass = cellType.getClass(ctx);
			if (cellClass == null) {
				if (!ctx.exitFromBlock()) {
					ctx.throwRuntimeException("Class '" + fullName + "' can not be resolved");
				}
				return null;
			} else {
				return HiClassArray.getArrayClass(cellClass);
			}
		}

		if (isNull()) {
			return HiClass.getNullClass();
		}

		HiClass clazz;
		if (ctx != null) {
			if (path != null) {
				clazz = path[0].getClass(ctx);
				for (int i = 1; i < path.length; i++) {
					clazz = clazz.getClass(ctx, path[i].name);
				}
				clazz = clazz.getClass(ctx, name);
			} else {
				clazz = ctx.getClass(name);
			}
		} else {
			clazz = HiClass.forName(null, fullName);
		}

		if (clazz == null && ctx != null) {
			ctx.throwRuntimeException("Class '" + fullName + "' can not be resolved");
		}
		return clazz;
	}

	private Map<String, Type> innerTypes;

	public Type getInnerType(String name, int dimension) {
		if (innerTypes == null) {
			innerTypes = new HashMap<>(1);
		}

		Type type = innerTypes.get(name);
		if (type == null) {
			type = new Type(this, name);
			innerTypes.put(name, type);
		}

		type = getArrayType(type, dimension);
		return type;
	}

	// === Static Methods ===
	private static Map<String, Type> typesWithoutParent = new HashMap<>();

	private static Map<Type, Type> arrayTypes = new HashMap<>();

	public static Type getType(Type parent, String name) {
		if (parent != null) {
			return parent.getInnerType(name, 0);
		} else {
			return getTopType(name);
		}
	}

	public static Type getTopType(String name) {
		Type type = predefinedTypes.get(name);
		if (type != null) {
			return type;
		}

		type = typesWithoutParent.get(name);
		if (type == null) {
			type = new Type(null, name);
			typesWithoutParent.put(name, type);
		}
		return type;
	}

	public static Type getTypeByFullName(String fullName) {
		int index = fullName.indexOf('.');
		if (index != -1) {
			Type type = getType(null, fullName.substring(0, index));
			while (index != -1) {
				int nextIndex = fullName.indexOf('.', index + 1);
				if (index != -1) {
					type = getType(null, fullName.substring(index, nextIndex));
					index = nextIndex;
				} else {
					type = getType(null, fullName.substring(index));
				}
			}
			return type;
		} else {
			return getType(null, fullName);
		}
	}

	public static Type getPrimitiveType(String name) {
		return predefinedTypes.get(name);
	}

	public static Type getArrayType(Type type) {
		Type arrayType = arrayTypes.get(type);
		if (arrayType == null) {
			arrayType = new Type(type);
			arrayTypes.put(type, arrayType);
		}
		return arrayType;
	}

	public static Type getArrayType(Type cellType, int dimension) {
		for (int i = 0; i < dimension; i++) {
			cellType = getArrayType(cellType);
		}
		return cellType;
	}

	public static Type getNullType() {
		return predefinedTypes.get("null");
	}

	public static Type getType(HiClass clazz) {
		if (clazz.isPrimitive()) {
			return getPrimitiveType(clazz.fullName);
		}
		if (clazz.isArray()) {
			HiClassArray arrayClass = (HiClassArray) clazz;
			int index = 0;
			while (arrayClass.cellClass.fullName.charAt(index) == '0') {
				index++;
			}
			Type cellType = getTypeByFullName(arrayClass.cellClass.fullName.substring(index));
			return getArrayType(cellType, arrayClass.dimension);
		}
		if (clazz.isNull()) {
			return null;
		}
		return getTypeByFullName(clazz.fullName);
	}

	@Override
	public int compareTo(Type type) {
		if (dimension != type.dimension) {
			return dimension - type.dimension;
		}
		return name.compareTo(type.name);
	}

	@Override
	public String toString() {
		return fullName;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		byte typeClass = getTypeClass();
		os.writeByte(typeClass);

		switch (typeClass) {
			case PRIMITIVE:
			case OBJECT:
				os.writeUTF(fullName);
				break;

			case ARRAY:
				os.writeType(cellType);
				break;
		}
	}

	public static Type decode(DecodeContext os) throws IOException {
		int typeType = os.readByte();
		switch (typeType) {
			case PRIMITIVE:
				return getPrimitiveType(os.readUTF());

			case OBJECT:
				String fullName = os.readUTF();
				String[] path = fullName.split("\\.");
				Type type = null;
				for (String name : path) {
					type = getType(type, name);
				}
				return type;

			case ARRAY:
				return getArrayType(os.readType());
		}
		throw new RuntimeException("unknown type " + typeType);
	}

	public byte getTypeClass() {
		byte typeClass = OBJECT; // object
		if (isPrimitive()) {
			typeClass = PRIMITIVE; // primitive
		} else if (isArray()) {
			typeClass = ARRAY; // array
		}
		return typeClass;
	}

	@Override
	public Type getType() {
		return this;
	}

	@Override
	public boolean isArray() {
		return dimension > 0;
	}

	@Override
	public boolean isVarargs() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	// has to be set at the end of class init
	public final static Type objectType = getTopType("Object");

	public final static Type enumType = getTopType("Enum");

	public final static Type recordType = getTopType("Record");

	public final static Type stringType = getTopType("String");

	public final static Type voidType;

	public final static Type varType = new Type("var");
}
