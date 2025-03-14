package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassVar;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.ool.runtime.HiScriptRuntimeException;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.WordType;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class Type implements HiType, TypeArgumentIF, Codeable, Comparable<Type> {
	public final static int PRIMITIVE = 0;

	public final static int OBJECT = 1;

	public final static int ARRAY = 2;

	public final static Type byteType = new Type(BYTE_TYPE, "byte", true);
	public final static Type byteBoxType = new Type(UNDEFINED_TYPE, "Byte", false);

	public final static Type charType = new Type(CHAR_TYPE, "char", true);
	public final static Type charBoxType = new Type(UNDEFINED_TYPE, "Character", false);

	public final static Type shortType = new Type(SHORT_TYPE, "short", true);
	public final static Type shortBoxType = new Type(UNDEFINED_TYPE, "Short", false);

	public final static Type intType = new Type(INT_TYPE, "int", true);
	public final static Type intBoxType = new Type(UNDEFINED_TYPE, "Integer", false);

	public final static Type longType = new Type(LONG_TYPE, "long", true);
	public final static Type longBoxType = new Type(UNDEFINED_TYPE, "Long", false);

	public final static Type floatType = new Type(FLOAT_TYPE, "float", true);
	public final static Type floatBoxType = new Type(UNDEFINED_TYPE, "Float", false);

	public final static Type doubleType = new Type(DOUBLE_TYPE, "double", true);
	public final static Type doubleBoxType = new Type(UNDEFINED_TYPE, "Double", false);

	public final static Type booleanType = new Type(BOOLEAN_TYPE, "boolean", true);
	public final static Type booleanBoxType = new Type(UNDEFINED_TYPE, "Boolean", false);

	public final static Type voidType = new Type(VOID_TYPE, "void", true);

	public final static Type nullType = new Type(UNDEFINED_TYPE, "null", false);

	public final static Type varType = new Type(VAR_TYPE, "var", false);

	public final static Type invalidType = new Type(INVALID_TYPE, "", false);

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

	/**
	 * Object type
	 */
	public Type(String name) {
		this(null, name);
	}

	public Type(Type parent, String name) {
		this.id = UNDEFINED_TYPE;
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
		this.hashCode = Objects.hash(fullName, dimension, Objects.hash((Object[]) parameters), isExtends, isSuper);
	}

	/**
	 * Parameterized type for generic class
	 */
	private Type(Type type, Type[] parameters) {
		this.id = type.id;
		this.parent = type.parent;
		this.name = type.name;
		this.dimension = 0;
		this.primitive = false;
		this.path = type.path;
		this.fullName = type.fullName;
		this.hashCode = type.hashCode;
		this.parameters = parameters;
	}

	/**
	 * Primitive type
	 */
	private Type(PrimitiveType id, String name, boolean primitive) {
		this.id = id;
		this.parent = null;
		this.cellType = null;
		this.name = name.intern();
		this.dimension = 0;
		this.primitive = primitive;
		this.fullName = name.intern();
		this.hashCode = Objects.hash(fullName, dimension);
	}

	/**
	 * Array type
	 */
	public Type(Type cellType) {
		this.id = UNDEFINED_TYPE;
		this.parent = null;
		this.cellType = cellType;
		this.name = ("0" + cellType.name).intern();
		this.dimension = cellType.dimension + 1;
		this.primitive = false;
		this.fullName = ("0" + cellType.fullName).intern();
		this.hashCode = Objects.hash(fullName, dimension);
		this.parameters = cellType.parameters;
		this.isSuper = cellType.isSuper;
		this.isExtends = cellType.isExtends;

		if (dimension == 1) {
			cellTypeRoot = cellType;
		} else {
			cellTypeRoot = cellType.cellTypeRoot;
		}
	}

	public Type(Type extendedType, boolean isSuper) {
		this.id = UNDEFINED_TYPE;
		this.primitive = false;
		this.parent = extendedType.parent;
		this.name = extendedType.name;
		this.dimension = extendedType.dimension;
		this.path = extendedType.path;
		this.fullName = extendedType.fullName;
		this.hashCode = extendedType.hashCode;
		this.isExtends = !isSuper;
		this.isSuper = isSuper;
	}

	// @generics
	public boolean validateClass(HiClass clazz, ValidationInfo validationInfo, CompileClassContext ctx, Token token) {
		if (parameters != null && parameters.length > 0) {
			if (clazz.generics == null) {
				if (!clazz.isArray()) {
					validationInfo.error("type '" + clazz.getNameDescr() + "' does not have type parameters", token);
					return false;
				} else {
					return true;
				}
			}
			if (parameters.length != clazz.generics.generics.length) {
				validationInfo.error("wrong number of type arguments: " + parameters.length + "; required: " + clazz.generics.generics.length, token);
				return false;
			}
			for (int i = 0; i < parameters.length; i++) {
				Type parameterType = parameters[i];
				HiClassGeneric definedClass = clazz.generics.generics[i].clazz;
				HiClass extendsClass = definedClass.clazz;
				if (extendsClass.isGeneric()) {
					HiClassGeneric extendsGenericClass = (HiClassGeneric) extendsClass;
					if (extendsGenericClass != definedClass) {
						extendsClass = parameters[extendsGenericClass.index].getClass(ctx);
					}
				}
				if (parameterType.isExtends) {
					HiClass parameterClass = parameterType.getClass(ctx);
					if (parameterClass != null) {
						if (!definedClass.isGeneric() && !definedClass.isInstanceof(parameterClass)) {
							validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + extendsClass.getNameDescr() + "'", token);
						} else if (definedClass.isGeneric()) {
							if (parameterType.isExtends) {
								if (!extendsClass.isInstanceof(parameterClass) && !parameterClass.isInstanceof(extendsClass)) {
									validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + extendsClass.getNameDescr() + "'", token);
								}
							} else if (!parameterClass.isInstanceof(extendsClass)) {
								validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + extendsClass.getNameDescr() + "'", token);
							}
						}
					}
				} else {
					HiClass parameterClass = parameterType.getClass(ctx);
					if (parameterClass != null) {
						if (!definedClass.isGeneric() && !parameterClass.isInstanceof(definedClass)) {
							validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + extendsClass.getNameDescr() + "'", token);
						} else if (definedClass.isGeneric() && !parameterClass.isInstanceof(extendsClass)) {
							validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + extendsClass.getNameDescr() + "'", token);
						}
						parameterType.validateClass(parameterClass, validationInfo, ctx, token);
					}
				}
			}
		}
		return true;
	}

	// @generics
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

	private PrimitiveType id;

	public Type parent;

	public Type[] path;

	public Type cellType;

	public Type cellTypeRoot;

	public String name;

	public String fullName;

	public int hashCode;

	private final int dimension;

	private final boolean primitive;

	// @generics
	public Type[] parameters;

	public boolean isExtends;

	public boolean isSuper;

	public int getDimension() {
		return dimension;
	}

	public Type getCellType() {
		return cellType;
	}

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
		return hashCode;
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
			if (t.isExtends != isExtends) {
				return false;
			}
			if (t.isSuper != isSuper) {
				return false;
			}
			if (t.parameters != null && parameters == null) {
				return false;
			} else if (t.parameters == null && parameters != null) {
				return false;
			} else if (t.parameters != null && parameters != null) {
				if (t.parameters.length != parameters.length) {
					return false;
				}
				for (int i = 0; i < parameters.length; i++) {
					if (!t.parameters[i].equals(parameters[i])) {
						return false;
					}
				}
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

	@Override
	public HiClass getClass(ClassResolver classResolver) {
		// Нельзя кэшировать класс, т.к. имена разных классов могут совпадать в разных контекстах
		if (isPrimitive()) {
			return HiClass.getPrimitiveClass(name);
		}

		if (isArray()) {
			HiClass cellClass = cellType.getClass(classResolver);
			if (cellClass == null) {
				if (classResolver != null) {
					classResolver.processResolverException("class '" + this + "' can not be resolved");
				}
				return null;
			}
			return cellClass.getArrayClass();
		}

		if (isNull()) {
			return HiClassNull.NULL;
		}

		if (isVar()) {
			return HiClassVar.VAR;
		}

		HiClass clazz = null;

		// @generics
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

		if (clazz == null && classResolver != null) {
			classResolver.processResolverException("class '" + this + "' can not be resolved");
		}
		return clazz;
	}

	private Map<String, Type> innerTypes;

	public Type getInnerType(String name, int dimension, HiRuntimeEnvironment env) {
		if (innerTypes == null) {
			innerTypes = new HashMap<>(1);
		}

		Type type = innerTypes.get(name);
		if (type == null) {
			type = new Type(this, name);
			innerTypes.put(name, type);
		}

		type = getArrayType(type, dimension, env);
		return type;
	}

	// === Static Methods ===
	public static Type getType(Type parent, String name, HiRuntimeEnvironment env) {
		if (parent != null) {
			return parent.getInnerType(name, 0, env);
		} else {
			return getTopType(name, env);
		}
	}

	// @generics
	public static Type getParameterizedType(Type type, Type[] parameters) {
		return new Type(type, parameters);
	}

	public static Type getExtendedType(Type extendedType, boolean isSuper) {
		if (extendedType == null || extendedType == Type.objectType) {
			return Type.anyType;
		} else {
			return new Type(extendedType, isSuper);
		}
	}

	private final static Map<String, Type> typesWithoutParent = new HashMap<>();

	public static Type getTopType(String name, HiRuntimeEnvironment env) {
		Type type = primitiveTypes.get(name);
		if (type != null) {
			return type;
		}
		if (env != null) {
			return env.getTopType(name);
		} else {
			// system types
			type = typesWithoutParent.get(name);
			if (type == null) {
				type = new Type(null, name);
				typesWithoutParent.put(name, type);
			}
			return type;
		}
	}

	public static Type getTypeByFullName(String fullName, HiRuntimeEnvironment env) {
		int index = fullName.indexOf('.');
		if (index != -1) {
			Type type = getType(null, fullName.substring(0, index), env);
			while (index != -1) {
				int nextIndex = fullName.indexOf('.', index + 1);
				if (index != -1) {
					type = getType(null, fullName.substring(index, nextIndex), env);
					index = nextIndex;
				} else {
					type = getType(null, fullName.substring(index), env);
				}
			}
			return type;
		} else {
			return getType(null, fullName, env);
		}
	}

	public static Type getPrimitiveType(String name) {
		return primitiveTypes.get(name);
	}

	public static Type getAutoboxType(String name) {
		return primitiveTypes.get(name);
	}

	public static Type getTypeByWord(WordType wordType) {
		if (wordType != null) {
			switch (wordType) {
				case BYTE:
					return byteType;
				case CHAR:
					return charType;
				case SHORT:
					return shortType;
				case INT:
					return intType;
				case LONG:
					return longType;
				case FLOAT:
					return floatType;
				case DOUBLE:
					return doubleType;
				case BOOLEAN:
					return booleanType;
				case VOID:
					return voidType;
				case NULL:
					return nullType;
				case VAR:
					return varType;
			}
		}
		return null;
	}

	private final static Map<Type, Type> arrayTypes = new HashMap<>();

	public static Type getArrayType(Type type, HiRuntimeEnvironment env) {
		if (env != null) {
			return env.getArrayType(type);
		} else {
			// system types
			Type arrayType = arrayTypes.get(type);
			if (arrayType == null) {
				arrayType = new Type(type);
				arrayTypes.put(type, arrayType);
			}
			return arrayType;
		}
	}

	public static Type getArrayType(Type cellType, int dimension, HiRuntimeEnvironment env) {
		for (int i = 0; i < dimension; i++) {
			cellType = getArrayType(cellType, env);
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
			HiRuntimeEnvironment env = clazz.getClassLoader().getEnv();
			Type cellType = getTypeByFullName(arrayClass.cellClass.fullName.substring(index), env);
			return getArrayType(cellType, arrayClass.dimension, env);
		}
		if (clazz.isNull()) {
			return nullType;
		}
		return getTypeByFullName(clazz.fullName, clazz.getClassLoader().getEnv());
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
		if (parameters != null) {
			return fullName + getParametersDescr();
		} else if (isExtends) {
			if (!fullName.equals(HiClass.OBJECT_CLASS_NAME)) {
				return "? extends " + fullName;
			} else {
				return "?";
			}
		} else if (isSuper) {
			return "? super " + fullName;
		}
		return fullName;
	}

	public String getParametersDescr() {
		if (parameters != null) {
			return "<" + Arrays.stream(parameters).map(Object::toString).collect(Collectors.joining(", ")) + ">";
		} else {
			return "";
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		byte typeClass = getTypeClass();
		os.writeByte(typeClass);

		switch (typeClass) {
			case PRIMITIVE:
				os.writeUTF(fullName);
				return;

			case OBJECT:
				os.writeUTF(fullName);
				os.writeByte(parameters != null ? parameters.length : 0);
				os.writeNullable(parameters);
				os.writeBoolean(isExtends);
				os.writeBoolean(isSuper);
				break;

			case ARRAY:
				os.writeType(cellType);
				os.writeShort(dimension);
				break;
		}
	}

	public static Type decode(DecodeContext os) throws IOException {
		int typeType = os.readByte();
		Type type = null;
		switch (typeType) {
			case PRIMITIVE:
				return getPrimitiveType(os.readUTF());

			case OBJECT:
				String fullName = os.readUTF();
				String[] path = fullName.split("\\.");
				for (String name : path) {
					type = getType(type, name, os.getEnv());
				}
				Type[] parameters = os.readNullableArray(Type.class, os.readByte());
				if (parameters != null) {
					type = getParameterizedType(type, parameters);
				}
				type.isExtends = os.readBoolean();
				type.isSuper = os.readBoolean();
				return type;

			case ARRAY:
				return getArrayType(os.readType(), os.readShort(), os.getEnv());
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

	// @generics
	public Type getParameterType(HiClassGeneric genericClass) {
		if (parameters != null && genericClass.index < parameters.length) {
			return parameters[genericClass.index];
		}
		return null;
	}

	// @generics
	public boolean isWildcard() {
		return isExtends || isSuper;
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

	@Override
	public String getTypeName() {
		return name;
	}

	// has to be set at the end of class init
	public final static Type objectType = new Type(HiClass.OBJECT_CLASS_NAME);

	public final static Type anyType = new Type(objectType, false); // used for case <? extends Type>

	public final static Type enumType = new Type(HiClass.ENUM_CLASS_NAME);

	public final static Type recordType = new Type(HiClass.RECORD_CLASS_NAME);
}
