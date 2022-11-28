package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;
import java.util.HashMap;

/**
 * Используется для описания типа до того как будет создана иерархия классов. После создана иерархии классов возможен поиск класса в данном
 * контексте по типу. Для разных классов из разных контекстов, у которых имена совпадают, тип также может совпадать и, наоборот, для разных типов
 * (например, С и A.B.C) класс может совпадать. Все зависит от того, в каком контексте производится поиск класса по типу. Если в коде указан
 * класс вместе с суперклассами (например, A.B.C), то для данного класса создается тип с соответствующей иерархией (Тип С, С.parent = B, B.parent
 * = A, A.parent = null). В данном случае вначале производится поиск последнего суперкласса (A) по контексту, а потом в найденном классе
 * производится поиск по дочернего элемента (в A ищется B, а потом в B ищется C).
 */
public class Type implements PrimitiveTypes, Codeable, Comparable<Type> {
	public final static int PRIMITIVE = 0;

	public final static int OBJECT = 1;

	public final static int ARRAY = 2;

	private static HashMap<String, Type> predefinedTypes = new HashMap<>();

	static {
		predefinedTypes.put("char", new Type("char"));
		predefinedTypes.put("boolean", new Type("boolean"));
		predefinedTypes.put("byte", new Type("byte"));
		predefinedTypes.put("short", new Type("short"));
		predefinedTypes.put("int", new Type("int"));
		predefinedTypes.put("float", new Type("float"));
		predefinedTypes.put("long", new Type("long"));
		predefinedTypes.put("double", new Type("double"));
		predefinedTypes.put("void", new Type("void"));
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
	private Type(Type cellType, boolean varargs) {
		this.parent = null;
		this.cellType = cellType;
		this.name = "0" + cellType.name;
		this.dimension = cellType.dimension + 1;
		this.primitive = false;
		this.fullName = "0" + cellType.fullName;
		this.varargs = varargs;

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

	private boolean varargs;

	public int getDimension() {
		return dimension;
	}

	public boolean isArray() {
		return dimension > 0;
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

	public boolean isVarargs() {
		return varargs;
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

		HiClass clazz = null;
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

		if (clazz == null) {
			ctx.throwRuntimeException("Class '" + fullName + "' can not be resolved");
		}
		return clazz;
	}

	private HashMap<String, Type> innerTypes;

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
	private static HashMap<String, Type> types = new HashMap<>();

	private static HashMap<Type, Type> arrayTypes = new HashMap<>();

	public static Type getType(Type parent, String name) {
		if (parent != null) {
			return parent.getInnerType(name, 0);
		} else {
			return getType(name);
		}
	}

	public static Type getType(String name) {
		Type type = types.get(name);
		if (type == null) {
			type = new Type(null, name);
			types.put(name, type);
		}
		return type;
	}

	public static Type getType(String name, int dimension) {
		Type type = null;
		if (predefinedTypes.containsKey(name)) {
			type = getPrimitiveType(name);
		} else {
			type = getType(name);
		}

		if (dimension > 0) {
			type = getArrayType(type, dimension);
		}
		return type;
	}

	public static Type getPrimitiveType(String name) {
		return predefinedTypes.get(name);
	}

	public static Type getArrayType(Type type) {
		Type arrayType = arrayTypes.get(type);
		if (arrayType == null) {
			arrayType = new Type(type, false);
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

	public static Type getVarargsType(Type cellType) {
		return new Type(cellType, true);
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
			return getType(arrayClass.cellClass.fullName, arrayClass.dimension);
		}
		return getType(clazz.fullName);
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
		byte type = getType();
		os.writeByte(type);

		switch (type) {
			case PRIMITIVE:
				os.writeUTF(name);
				break;

			case OBJECT:
				os.writeUTF(name);
				break;

			case ARRAY:
				os.writeType(cellType);
				break;
		}
	}

	public static Type decode(DecodeContext os) throws IOException {
		int type = os.readByte();
		switch (type) {
			case PRIMITIVE:
				return getPrimitiveType(os.readUTF());

			case OBJECT:
				return getType(os.readUTF());

			case ARRAY:
				return getArrayType(os.readType());
		}
		throw new RuntimeException("unknown type " + type);
	}

	public byte getType() {
		byte type = OBJECT; // object
		if (isPrimitive()) {
			type = PRIMITIVE; // primitive
		} else if (isArray()) {
			type = ARRAY; // array
		}
		return type;
	}

	// has to be set at the end of class init
	public final static Type ObjectType = getType("Object");
}
