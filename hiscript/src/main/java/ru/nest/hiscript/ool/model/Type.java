package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassVar;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Words;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 */
public class Type implements TypeArgumentIF, PrimitiveTypes, Codeable, Comparable<Type> {
	public final static int PRIMITIVE = 0;

	public final static int OBJECT = 1;

	public final static int ARRAY = 2;

	public final static Type byteType = new Type(BYTE, "byte", true);
	public final static Type byteBoxType = new Type(-1, "Byte", false);

	public final static Type charType = new Type(CHAR, "char", true);
	public final static Type charBoxType = new Type(-1, "Character", false);

	public final static Type shortType = new Type(SHORT, "short", true);
	public final static Type shortBoxType = new Type(-1, "Short", false);

	public final static Type intType = new Type(INT, "int", true);
	public final static Type intBoxType = new Type(-1, "Integer", false);

	public final static Type longType = new Type(LONG, "long", true);
	public final static Type longBoxType = new Type(-1, "Long", false);

	public final static Type floatType = new Type(FLOAT, "float", true);
	public final static Type floatBoxType = new Type(-1, "Float", false);

	public final static Type doubleType = new Type(DOUBLE, "double", true);
	public final static Type doubleBoxType = new Type(-1, "Double", false);

	public final static Type booleanType = new Type(BOOLEAN, "boolean", true);
	public final static Type booleanBoxType = new Type(-1, "Boolean", false);

	public final static Type voidType = new Type(VOID, "void", true);

	public final static Type nullType = new Type(-1, "null", false);

	public final static Type varType = new Type(VAR, "var", false);

	public final static Type invalidType = new Type(INVALID, "", false);

	private final static Map<String, Type> primitiveTypes = new HashMap<>();

	static {
		primitiveTypes.put("byte", byteType);
		primitiveTypes.put("char", charType);
		primitiveTypes.put("short", shortType);
		primitiveTypes.put("int", intType);
		primitiveTypes.put("long", longType);
		primitiveTypes.put("float", floatType);
		primitiveTypes.put("double", doubleType);
		primitiveTypes.put("boolean", booleanType);
		primitiveTypes.put("void", voidType);
		primitiveTypes.put("null", nullType);
		primitiveTypes.put("var", varType);

		primitiveTypes.put("Byte", byteBoxType);
		primitiveTypes.put("Character", charBoxType);
		primitiveTypes.put("Short", shortBoxType);
		primitiveTypes.put("Integer", intBoxType);
		primitiveTypes.put("Long", longBoxType);
		primitiveTypes.put("Float", floatBoxType);
		primitiveTypes.put("Double", doubleBoxType);
		primitiveTypes.put("Boolean", booleanBoxType);
	}

	public final int id;

	/**
	 * Object type
	 */
	private Type(Type parent, String name) {
		this.id = -1;
		this.parent = parent;
		this.cellType = null;
		this.name = (name != null ? name : "").intern();
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
			this.fullName = (parent.fullName + "." + name).intern();
		} else {
			this.fullName = name.intern();
		}
	}

	private Type(Type type, Type[] parameters) {
		this.id = type.id;
		this.parent = type.parent;
		this.name = type.name;
		this.dimension = 0;
		this.primitive = false;
		this.path = type.path;
		this.fullName = type.fullName;
		this.parameters = parameters;
	}

	/**
	 * Primitive type
	 */
	private Type(int id, String name, boolean primitive) {
		this.id = id;
		this.parent = null;
		this.cellType = null;
		this.name = name.intern();
		this.dimension = 0;
		this.primitive = primitive;
		this.fullName = name.intern();
	}

	/**
	 * Array type
	 */
	private Type(Type cellType) {
		this.id = -1;
		this.parent = null;
		this.cellType = cellType;
		this.name = ("0" + cellType.name).intern();
		this.dimension = cellType.dimension + 1;
		this.primitive = false;
		this.fullName = ("0" + cellType.fullName).intern();

		if (dimension == 1) {
			cellTypeRoot = cellType;
		} else {
			cellTypeRoot = cellType.cellTypeRoot;
		}
	}

	private Type(Type extendedType, boolean isSuper) {
		this.id = -1;
		this.primitive = false;
		this.parent = extendedType.parent;
		this.name = extendedType.name;
		this.dimension = extendedType.dimension;
		this.path = extendedType.path;
		this.fullName = extendedType.fullName;
		this.isExtends = !isSuper;
		this.isSuper = isSuper;
	}

	public boolean validateClass(HiClass clazz, ValidationInfo validationInfo, CompileClassContext ctx, Token token) {
		if (parameters != null && parameters.length > 0) {
			if (clazz.generics == null) {
				validationInfo.error("type '" + clazz.getNameDescr() + "' does not have type parameters", token);
				return false;
			}
			if (parameters.length != clazz.generics.generics.length) {
				validationInfo.error("wrong number of type arguments: " + parameters.length + "; required: " + clazz.generics.generics.length, token);
				return false;
			}
			for (int i = 0; i < parameters.length; i++) {
				Type parameterType = parameters[i];
				if (parameterType.isExtends) {
					HiClass parameterClass = parameterType.getClass(ctx);
					if (parameterClass != null) {
						HiClassGeneric definedClass = clazz.generics.generics[i].clazz;
						if (!definedClass.isGeneric() && !definedClass.isInstanceof(parameterClass)) {
							validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + definedClass.clazz.getNameDescr() + "'", token);
						} else if (definedClass.isGeneric()) {
							if (parameterType.isExtends) {
								if (!definedClass.clazz.isInstanceof(parameterClass) && !parameterClass.isInstanceof(definedClass.clazz)) {
									validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + definedClass.clazz.getNameDescr() + "'", token);
								}
							} else {
								if (!parameterClass.isInstanceof(definedClass.clazz)) {
									validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + definedClass.clazz.getNameDescr() + "'", token);
								}
							}
						}
					}
				} else if (parameterType.isSuper) {
					HiClass parameterClass = parameterType.getClass(ctx);
					if (parameterClass != null) {
						HiClassGeneric definedClass = clazz.generics.generics[i].clazz;
						if (!parameterClass.isInstanceof(definedClass.clazz)) {
							validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + definedClass.clazz.getNameDescr() + "'", token);
						}
					}
				} else {
					HiClass parameterClass = parameterType.getClass(ctx);
					if (parameterClass != null) {
						HiClassGeneric definedClass = clazz.generics.generics[i].clazz;
						if (!definedClass.isGeneric() && !parameterClass.isInstanceof(definedClass)) {
							validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + definedClass.clazz.getNameDescr() + "'", token);
						} else if (definedClass.isGeneric() && !parameterClass.isInstanceof(definedClass.clazz)) {
							validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + definedClass.clazz.getNameDescr() + "'", token);
						}
					}
				}
			}
		}
		return true;
	}

	public boolean validateMatch(Type type, ValidationInfo validationInfo, CompileClassContext ctx, Token token) {
		if (parameters != null && parameters.length == 0) {
			if (type.parameters == null) { // T<> => T
				validationInfo.error("diamond operator is not applicable for non-parameterized types", token);
				return false;
			} else { // T<> => T<T1,...>
				return true;
			}
		}
		if (type.parameters != null && parameters != null) {
			if (type.parameters.length > 0) {
				for (int i = 0; i < parameters.length; i++) {
					Type fromType = parameters[i];
					HiClass fromClass = fromType.getClass(ctx);
					Type toType = type.parameters[i];
					HiClass toClass = toType.getClass(ctx);
					if (fromClass != null && toClass != null) { // type is invalid
						if (toType.isExtends) {
							if (!fromClass.isInstanceof(toClass)) {
								validationInfo.error("type parameter '" + fromClass.getNameDescr() + "' is not within its bound; should extend '" + toClass.getNameDescr() + "'", token);
								return false;
							}
						} else if (toType.isSuper) {
							if (!toClass.isInstanceof(fromClass)) {
								validationInfo.error("type parameter '" + fromClass.getNameDescr() + "' is not within its bound; should extend '" + toClass.getNameDescr() + "'", token);
								return false;
							}
						} else {
							if (fromClass != toClass) {
								// validationInfo.error("type parameter '" + fromClass.fullName + "' is not within its bound; should extend '" + toClass.fullName + "'", token);
								validationInfo.error("incompatible types. Found: '" + fromClass.getNameDescr() + "' required: '" + toClass.getNameDescr() + "'", token);
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	public HiClass getArrayClass(ClassResolver classResolver, int dimensions) {
		HiClass rootCellClass = getClass(classResolver);
		return rootCellClass.getArrayClassIf(dimensions);
	}

	public Type parent;

	public Type[] path;

	public Type cellType;

	public Type cellTypeRoot;

	public String name;

	public String fullName;

	private final int dimension;

	public int getDimension() {
		return dimension;
	}

	public Type getCellType() {
		return cellType;
	}

	private final boolean primitive;

	public Type[] parameters;

	public boolean isExtends;

	public boolean isSuper;

	public boolean isPrimitive() {
		return primitive;
	}

	public boolean isNull() {
		return this == nullType;
	}

	public boolean isVar() {
		return this == varType;
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

	public HiClass getClass(ClassResolver classResolver) {
		// Нельзя кэшировать класс, т.к. имена разных классов могут совпадать в разных контекстах
		if (isPrimitive()) {
			return HiClass.getPrimitiveClass(name);
		}

		if (isSuper) {
			return HiClass.OBJECT_CLASS;
		}

		if (isArray()) {
			HiClass cellClass = cellType.getClass(classResolver);
			if (cellClass == null) {
				classResolver.processResolverException("class '" + fullName + "' can not be resolved");
				return null;
			} else {
				return cellClass.getArrayClass();
			}
		}

		if (isNull()) {
			return HiClassNull.NULL;
		}

		if (isVar()) {
			return HiClassVar.VAR;
		}

		HiClass clazz = null;
		if (classResolver instanceof CompileClassContext) {
			clazz = ((CompileClassContext) classResolver).level.resolveGeneric(name);
		}
		if (clazz == null) {
			if (path != null) {
				clazz = path[0].getClass(classResolver);
				for (int i = 1; i < path.length; i++) {
					clazz = clazz.getClass(classResolver, path[i].name);
				}
				clazz = clazz.getClass(classResolver, name);
			} else {
				clazz = classResolver.getClass(name);
			}
		}

		if (clazz == null) {
			classResolver.processResolverException("class '" + fullName + "' can not be resolved");
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
	private final static Map<String, Type> typesWithoutParent = new HashMap<>();

	private final static Map<Type, Type> arrayTypes = new HashMap<>();

	public static Type getType(Type parent, String name) {
		if (parent != null) {
			return parent.getInnerType(name, 0);
		} else {
			return getTopType(name);
		}
	}

	public static Type getParameterizedType(Type type, Type[] parameters) {
		return new Type(type, parameters);
	}

	public static Type getExtendedType(Type extendedType, boolean isSuper) {
		if (extendedType == null || extendedType == objectType) {
			return anyType;
		} else {
			return new Type(extendedType, isSuper);
		}
	}

	public static Type getTopType(String name) {
		Type type = primitiveTypes.get(name);
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
		return primitiveTypes.get(name);
	}

	public static Type getAutoboxType(String name) {
		return primitiveTypes.get(name);
	}

	public static Type getTypeByWord(int wordType) {
		switch (wordType) {
			case Words.BYTE:
				return byteType;
			case Words.CHAR:
				return charType;
			case Words.SHORT:
				return shortType;
			case Words.INT:
				return intType;
			case Words.LONG:
				return longType;
			case Words.FLOAT:
				return floatType;
			case Words.DOUBLE:
				return doubleType;
			case Words.BOOLEAN:
				return booleanType;
			case Words.VOID:
				return voidType;
			case Words.NULL:
				return nullType;
			case Words.VAR:
				return varType;
		}
		return null;
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

	public static Type getType(HiClass clazz) {
		if (clazz == null) {
			return null;
		}
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
		String name = fullName;
		if (parameters != null) {
			name += "<" + Arrays.stream(parameters).map(Object::toString).collect(Collectors.joining(", ")) + ">";
		} else if (isExtends) {
			name = "? extends " + name;
		} else if (isSuper) {
			name = "? super " + name;
		}
		return name;
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
		throw new HiScriptRuntimeException("unknown type " + typeType);
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

	public Type getParameterType(HiClassGeneric genericClass) {
		if (parameters != null && genericClass.index < parameters.length) {
			return parameters[genericClass.index];
		}
		return null;
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
	public final static Type objectType = getTopType(HiClass.OBJECT_CLASS_NAME);

	public final static Type anyType = new Type(objectType, false); // used for case <? extends Type>

	public final static Type enumType = getTopType(HiClass.ENUM_CLASS_NAME);

	public final static Type recordType = getTopType(HiClass.RECORD_CLASS_NAME);

	public final static Type stringType = getTopType(HiClass.STRING_CLASS_NAME);
}
