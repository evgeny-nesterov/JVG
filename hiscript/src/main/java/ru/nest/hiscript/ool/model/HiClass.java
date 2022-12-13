package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compiler.ClassFileParseRule;
import ru.nest.hiscript.ool.compiler.ParserUtil;
import ru.nest.hiscript.ool.model.HiConstructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.classes.HiClassRecord;
import ru.nest.hiscript.ool.model.lib.ObjectImpl;
import ru.nest.hiscript.ool.model.lib.SystemImpl;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HiClass implements Codeable {
	public final static int CLASS_OBJECT = 0;

	public final static int CLASS_PRIMITIVE = 1;

	public final static int CLASS_ARRAY = 2;

	public final static int CLASS_ENUM = 3;

	public final static int CLASS_RECORD = 4;

	public final static int CLASS_NULL = 5;

	public final static int CLASS_JAVA = 6;

	public final static int CLASS_TYPE_NONE = 0; // used for enclosing classes

	public final static int CLASS_TYPE_TOP = 1; // No outbound class

	public final static int CLASS_TYPE_INNER = 2; // static (NESTED) and not

	// static (INNER)
	public final static int CLASS_TYPE_LOCAL = 3; // in method, constructor

	public final static int CLASS_TYPE_ANONYMOUS = 4; // like new Object() {...}

	public static Map<String, HiClass> loadedClasses;

	public static HiClass OBJECT_CLASS;

	public static String ROOT_CLASS_NAME = "@root";

	static {
		loadSystemClasses();
	}

	private static void loadSystemClasses() {
		loadedClasses = new ConcurrentHashMap<>();

		Native.register(SystemImpl.class);
		Native.register(ObjectImpl.class);

		try {
			// object
			load(HiCompiler.class.getResource("/hilibs/Object.hi"));

			OBJECT_CLASS = forName(null, "Object");
			OBJECT_CLASS.superClassType = null;
			HiConstructor emptyConstructor = new HiConstructor(OBJECT_CLASS, new Modifiers(), (List<NodeArgument>) null, null, null, BodyConstructorType.NONE);
			OBJECT_CLASS.constructors = new HiConstructor[] {emptyConstructor};

			// TODO define classes initialization order automatically
			load(HiCompiler.class.getResource("/hilibs/String.hi"));
			load(HiCompiler.class.getResource("/hilibs/Class.hi"));
			load(HiCompiler.class.getResource("/hilibs/Enum.hi"));
			load(HiCompiler.class.getResource("/hilibs/Record.hi"));
			load(HiCompiler.class.getResource("/hilibs/AutoCloseable.hi"));
			load(HiCompiler.class.getResource("/hilibs/System.hi"));
			load(HiCompiler.class.getResource("/hilibs/Math.hi"));
			load(HiCompiler.class.getResource("/hilibs/Exception.hi"));
			load(HiCompiler.class.getResource("/hilibs/RuntimeException.hi"));
			load(HiCompiler.class.getResource("/hilibs/AssertException.hi"));
			load(HiCompiler.class.getResource("/hilibs/ArrayList.hi"));
			load(HiCompiler.class.getResource("/hilibs/HashMap.hi"));
			load(HiCompiler.class.getResource("/hilibs/Thread.hi"));
			load(HiCompiler.class.getResource("/hilibs/Java.hi"));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static void clearClassLoader() {
		cellTypes.clear();
		HiClassArray.clear();
		Native.clear();
		loadSystemClasses();
	}

	// TODO: ClassLoader
	public static void load(URL url) throws Exception {
		load(url.openStream());
	}

	public static void load(InputStream is) throws Exception {
		load(ParserUtil.readString(is));
	}

	public static void load(Reader r) throws Exception {
		load(ParserUtil.readString(r));
	}

	public static void load(String classCode) throws Exception {
		Tokenizer tokenizer = Tokenizer.getDefaultTokenizer(classCode);
		ClassFileParseRule.getInstance().visit(tokenizer, null);
	}

	// for ClassPrimitive, ClassArray and ClassNull
	public HiClass(HiClass superClass, HiClass enclosingClass, String name, int type) {
		this.superClass = superClass;
		if (superClass != null) {
			this.superClassType = Type.getType(superClass);
		}
		init(enclosingClass, name, type);
	}

	// for ClassParseRule, InterfaceParseRule and NewParseRule
	public HiClass(Type superClassType, HiClass enclosingClass, Type[] interfaceTypes, String name, int type) {
		this.superClassType = superClassType;
		this.interfaceTypes = interfaceTypes;
		init(enclosingClass, name, type);
	}

	// for decode
	private HiClass(Type superClassType, String name, int type) {
		this.superClassType = superClassType;
		this.name = name.intern();
		this.type = type;
		// init(...) is in decode
	}

	private void init(HiClass enclosingClass, String name, int type) {
		this.enclosingClass = enclosingClass;
		this.type = type;

		// define top class
		if (enclosingClass != null) {
			if (enclosingClass.topClass != null) {
				topClass = enclosingClass.topClass;
			} else {
				topClass = enclosingClass;
			}
		} else {
			topClass = this;
		}

		// try resolve super class if needed
		if (superClass == null && superClassType != null) {
			superClass = HiClass.forName(null, superClassType.fullName);
		}

		// intern name to optimize via a == b
		this.name = name.intern();
		this.fullName = getFullName();

		// register class by fullName
		if (!ROOT_CLASS_NAME.equals(fullName)) {
			if (loadedClasses.containsKey(fullName)) {
				//throw new ClassLoadException("class '" + fullName + "' already loaded");
			}
			loadedClasses.put(fullName, this);
		}
	}

	public String getFullName() {
		if (this.fullName == null) {
			if (enclosingClass != null) {
				switch (type) {
					case CLASS_TYPE_TOP:
					case CLASS_TYPE_INNER:
						fullName = enclosingClass.fullName + "$" + name;
						break;

					case CLASS_TYPE_LOCAL:
						int number = 0;
						do {
							fullName = enclosingClass.fullName + "$" + number + name;
							number++;
						} while (loadedClasses.containsKey(fullName));
						break;

					case CLASS_TYPE_ANONYMOUS:
						number = 0;
						do {
							name = Integer.toString(number);
							fullName = enclosingClass.fullName + "$" + number + name;
							number++;
						} while (loadedClasses.containsKey(fullName));
						break;

					default:
						fullName = name;
						break;
				}
			} else {
				fullName = name;
			}
			// intern name to optimize via a == b
			this.fullName = fullName.intern();
		}
		return this.fullName;
	}

	private boolean isInitialized = false;

	public void init(RuntimeContext currentCtx) {
		if (!isInitialized) {
			isInitialized = true;

			// resolve super class if needed
			if (superClass == null && superClassType != null) {
				superClass = superClassType.getClass(currentCtx);
				if (superClass == null) {
					currentCtx.throwRuntimeException("Can't resolve class '" + superClassType.fullName + "'");
					return;
				}
			}

			if (superClass != null) {
				// init super class
				superClass.init(currentCtx);

				// TODO: move this logic to parser ???

				// check super class on static
				if (!superClass.isStatic() && !superClass.isTopLevel() && isStatic()) {
					throw new IllegalStateException("Static class " + fullName + " can not extends not static and not top level class");
				}

				// check super class on final
				if (superClass.isFinal()) {
					throw new IllegalStateException("The type " + fullName + " cannot subclass the final class " + superClass.fullName);
				}
			}

			// resolve interfaces
			if (interfaces == null && interfaceTypes != null) {
				interfaces = new HiClass[interfaceTypes.length];
				for (int i = 0; i < interfaceTypes.length; i++) {
					interfaces[i] = interfaceTypes[i].getClass(currentCtx);
				}
			}

			if (interfaces != null) {
				for (HiClass classInterface : interfaces) {
					// init interface
					classInterface.init(currentCtx);

					// check interface on static
					if (!classInterface.isStatic() && !classInterface.isTopLevel() && isStatic()) {
						throw new IllegalStateException("Static class " + fullName + " can not extends not static and not top level class");
					}
				}
			}

			// set super class to Object by default
			if (superClass == null && this != OBJECT_CLASS && !isPrimitive() && !isNull()) {
				superClass = OBJECT_CLASS;
				superClassType = Type.objectType;
			}

			// init children classes
			if (classes != null) {
				for (HiClass clazz : classes) {
					clazz.init(currentCtx);
				}
			}

			RuntimeContext ctx = currentCtx != null ? currentCtx : RuntimeContext.get(null);
			ctx.enterInitialization(this, null, -1);
			try {
				if (initializers != null) {
					int size = initializers.length;
					for (int i = 0; i < size; i++) {
						NodeInitializer initializer = initializers[i];
						if (initializer.isStatic()) {
							initializer.execute(ctx);
							if (ctx.exitFromBlock()) {
								return;
							}
						}
					}
				}

				if (isEnum()) {
					((HiClassEnum) this).initEnumValues(ctx);
				}
			} catch (Throwable exc) {
				exc.printStackTrace();
				ctx.throwRuntimeException("Can not initialize class " + fullName + ": " + exc.getMessage());
			} finally {
				ctx.exit();
				if (ctx != currentCtx) {
					RuntimeContext.utilize(ctx);
				}
			}
		}
	}

	// used to resolve super class in runtime after all classes will be loaded
	public Type superClassType;

	// used to resolve interfaces in runtime after all classes will be loaded
	public Type[] interfaceTypes;

	// main class properties
	public Modifiers modifiers;

	public HiClass superClass;

	public HiClass[] interfaces;

	public String name;

	public String fullName;

	public int type;

	public HiClass enclosingClass;

	public HiClass topClass;

	public boolean isInterface = false;

	// content
	public HiField<?>[] fields;

	public NodeInitializer[] initializers;

	public HiConstructor[] constructors;

	public HiMethod[] methods;

	public HiClass[] classes;

	public HiClass getChildren(RuntimeContext ctx, String name) {
		HiClass children = null;
		if (classes != null) {
			for (HiClass c : classes) {
				if (c.name == name || c.fullName == name) {
					children = c;
					children.init(ctx);
					break;
				}
			}
		}
		return children;
	}

	private HashMap<String, HiClass> classes_map;

	public HiClass getClass(RuntimeContext ctx, String name) {
		if (classes_map != null && classes_map.containsKey(name)) {
			return classes_map.get(name);
		}

		HiClass clazz = _getClass(ctx, name);
		if (clazz != null) {
			if (classes_map == null) {
				classes_map = new HashMap<>(1);
			}
			classes_map.put(name, clazz);
		}
		return clazz;
	}

	/*
	 * name - is String.intern value to optimize via s1 == s2
	 */
	private HiClass _getClass(RuntimeContext ctx, String name) {
		// inner classes
		if (classes != null) {
			for (HiClass c : classes) {
				if (c.name == name || c.fullName == name) {
					c.init(ctx);
					return c;
				}
			}
		}

		// parent class
		if (superClass != null) {
			if (superClass == this) {
				throw new RuntimeException("cyclic");
			}
			HiClass c = superClass.getClass(ctx, name);
			if (c != null) {
				return c;
			}
		}

		HiClass clazz = this;
		while (clazz != null) {
			// check local enclosing classes
			HiClass c = ctx.getLocalClass(clazz, name);
			if (c != null) {
				return c;
			}

			// check enclosing classes
			c = clazz.getChildren(ctx, name);
			if (c != null) {
				return c;
			}

			clazz = clazz.enclosingClass;
		}

		// registered classes
		return forName(ctx, name);
	}

	public boolean isInstanceof(HiClass clazz) {
		HiClass c = this;
		while (c != null) {
			if (c == clazz || c.fullName.equals(clazz.fullName)) {
				return true;
			}
			if (c.hasInterfaceInstanceof(clazz)) {
				return true;
			}
			c = c.superClass;
		}
		return false;
	}

	public boolean isInstanceof(String className) {
		HiClass c = this;
		while (c != null) {
			if (c.fullName.equals(className)) {
				return true;
			}
			if (c.hasInterfaceInstanceof(className)) {
				return true;
			}
			c = c.superClass;
		}
		return false;
	}

	public boolean hasInterfaceInstanceof(HiClass superInterface) {
		if (interfaces != null && superInterface.isInterface) {
			for (HiClass in : interfaces) {
				if (in == superInterface) {
					return true;
				}
				if (in.hasInterfaceInstanceof(superInterface)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasInterfaceInstanceof(String superInterfaceName) {
		if (interfaces != null) {
			for (HiClass in : interfaces) {
				if (in.fullName.equals(superInterfaceName)) {
					return true;
				}
				if (in.hasInterfaceInstanceof(superInterfaceName)) {
					return true;
				}
			}
		}
		return false;
	}

	protected HashMap<String, HiField<?>> fieldsMap;

	public HiField<?> getField(RuntimeContext ctx, String name) {
		if (fieldsMap != null && fieldsMap.containsKey(name)) {
			return fieldsMap.get(name);
		}

		HiField<?> field = _searchField(ctx, name);
		if (field != null) {
			if (fieldsMap == null) {
				fieldsMap = new HashMap<>();
			}
			fieldsMap.put(name, field);
		}
		return field;
	}

	protected HiField<?> _searchField(RuntimeContext ctx, String name) {
		HiField<?> field = null;

		// this fields
		if (fields != null && fields.length > 0) {
			for (HiField<?> f : fields) {
				if (f.name == name) {
					field = f;
					break;
				}
			}
		}

		// interfaces static fields
		if (field == null && interfaces != null) {
			for (HiClass i : interfaces) {
				field = i.getField(ctx, name);
				if (field != null) {
					break;
				}
			}
		}

		// super fields
		if (field == null && superClass != null) {
			field = superClass.getField(ctx, name);
		}
		return field;
	}

	private HashMap<MethodSignature, HiMethod> methodsHash = new HashMap<>();

	// name - interned
	public HiMethod searchMethod(RuntimeContext ctx, String name, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set(name, argTypes);
		HiMethod method = methodsHash.get(signature);
		if (method == null) {
			method = _searchMethod(ctx, name, argTypes);
			if (method != null) {
				methodsHash.put(new MethodSignature(signature), method);
			}
		}
		return method;
	}

	// name - interned
	protected HiMethod _searchMethod(RuntimeContext ctx, String name, HiClass... argTypes) {
		// this methods
		if (methods != null && methods.length > 0) {
			for (HiMethod m : methods)
				FOR:{
					if (m.name == name) {
						if (m.hasVarargs()) {
							int mainArgCount = m.argCount - 1;
							if (mainArgCount > argTypes.length) {
								continue;
							}

							m.resolve(ctx);

							for (int i = 0; i < mainArgCount; i++) {
								if (!HiField.autoCast(argTypes[i], m.argClasses[i])) {
									break FOR;
								}
							}
							HiClass varargsType = m.argClasses[mainArgCount].getArrayType();
							for (int i = mainArgCount; i < argTypes.length; i++) {
								if (!HiField.autoCast(argTypes[i], varargsType)) {
									break FOR;
								}
							}
						} else {
							int argCount = m.argCount;
							if (argCount != argTypes.length) {
								continue;
							}

							m.resolve(ctx);

							for (int i = 0; i < argCount; i++) {
								if (!HiField.autoCast(argTypes[i], m.argClasses[i])) {
									break FOR;
								}
							}
						}
						return m;
					}
				}
		}

		// super methods
		if (superClass != null) {
			HiMethod m = superClass.searchMethod(ctx, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		if (interfaces != null) {
			HiClass id = null;
			HiMethod md = null;
			HiClass i = null;
			HiMethod m = null;
			for (HiClass _i : interfaces) {
				HiMethod _m = _i.searchMethod(ctx, name, argTypes);
				if (_m != null) {
					if (_m.modifiers.isDefault()) {
						if (md != null) {
							ctx.throwRuntimeException("ambiguous method " + name + " for interfaces " + id.fullName + " and " + _i.fullName + ".");
							return null;
						}
						id = _i;
						md = _m;
					} else {
						i = _i;
						m = _m;
					}
				}
			}
			if (md != null) {
				return md;
			} else if (m != null) {
				return m;
			}
		}

		// enclosing methods
		if (!isTopLevel()) {
			HiMethod m = enclosingClass.searchMethod(ctx, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// not found
		return null;
	}

	public HiMethod getMethod(RuntimeContext ctx, String name, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set(name, argTypes);
		HiMethod method = methodsHash.get(signature);
		if (method == null) {
			method = _getMethod(ctx, name, argTypes);
			if (method != null) {
				methodsHash.put(new MethodSignature(signature), method);
			}
		}
		return method;
	}

	private HiMethod _getMethod(RuntimeContext ctx, String name, HiClass... argTypes) {
		// this methods
		if (methods != null) {
			for (HiMethod m : methods)
				FOR:{
					if (m.name == name) {
						int argCount = m.argCount;
						if (argCount != argTypes.length) {
							continue;
						}

						m.resolve(ctx);

						for (int i = 0; i < argCount; i++) {
							if (argTypes[i] != m.argClasses[i]) {
								break FOR;
							}
						}
						return m;
					}
				}
		}

		// super methods
		if (superClass != null) {
			HiMethod m = superClass.getMethod(ctx, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// enclosing methods
		if (!isTopLevel()) {
			HiMethod m = enclosingClass.getMethod(ctx, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// not found
		return null;
	}

	private Map<MethodSignature, HiConstructor> constructorsHash = new HashMap<>();

	public HiConstructor searchConstructor(RuntimeContext ctx, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set("", argTypes);
		HiConstructor constructor = constructorsHash.get(signature);
		if (constructor == null) {
			constructor = _searchConstructor(ctx, argTypes);
			if (constructor != null) {
				constructorsHash.put(new MethodSignature(signature), constructor);
			}
		}
		return constructor;
	}

	protected HiConstructor _searchConstructor(RuntimeContext ctx, HiClass[] argTypes) {
		if (constructors != null) {
			for (HiConstructor constructor : constructors) {
				if (matchConstructor(ctx, constructor, argTypes)) {
					return constructor;
				}
			}
		}
		return null;
	}

	protected boolean matchConstructor(RuntimeContext ctx, HiConstructor constructor, HiClass[] argTypes) {
		int argCount = constructor.arguments != null ? constructor.arguments.length : 0;
		if (argCount != (argTypes != null ? argTypes.length : 0)) {
			return false;
		}

		constructor.resolve(ctx);
		for (int i = 0; i < argCount; i++) {
			if (!HiField.autoCast(argTypes[i], constructor.argClasses[i])) {
				return false;
			}
		}
		return true;
	}

	public HiConstructor getConstructor(RuntimeContext ctx, HiClass... argTypes) {
		if (constructors != null) {
			for (HiConstructor c : constructors)
				FOR:{
					int argCount = c.arguments != null ? c.arguments.length : 0;
					if (argCount != argTypes.length) {
						continue;
					}

					c.resolve(ctx);
					for (int i = 0; i < argCount; i++) {
						if (argTypes[i] != c.argClasses[i]) {
							break FOR;
						}
					}
					return c;
				}
		}
		return null;
	}

	public boolean isPrimitive() {
		return false;
	}

	public boolean isArray() {
		return false;
	}

	public boolean isNull() {
		return false;
	}

	public boolean isObject() {
		return true;
	}

	public boolean isJava() {
		return false;
	}

	public boolean isEnum() {
		return false;
	}

	public boolean isRecord() {
		return false;
	}

	public HiClass getArrayType() {
		return null;
	}

	public static HiClass forName(RuntimeContext ctx, String name) {
		HiClass clazz = loadedClasses.get(name);
		if (clazz != null && ctx != null) {
			clazz.init(ctx);
		}
		return clazz;
	}

	public static HiClass getNullClass() {
		return HiClassNull.NULL;
	}

	public static HiClass getPrimitiveClass(String name) {
		return HiClassPrimitive.getPrimitiveClass(name);
	}

	public static HiClass getArrayClass(HiClass cellClass) {
		return HiClassArray.getArrayClass(cellClass);
	}

	public static HiClassArray getArrayClass(HiClass cellClass, int dimensions) {
		return HiClassArray.getArrayClass(cellClass, dimensions);
	}

	@Override
	public String toString() {
		return fullName != null ? fullName : name;
	}

	private static HashMap<Class<?>, HiClass> cellTypes = new HashMap<>(9);

	public static HiClass getCellType(Class<?> clazz) {
		if (cellTypes.containsKey(clazz)) {
			return cellTypes.get(clazz);
		}

		HiClass cellType = null;
		if (clazz == HiObject.class || clazz == Object.class) {
			cellType = HiClass.OBJECT_CLASS;
		} else if (clazz == Boolean.class || clazz == boolean.class) {
			cellType = HiClass.getPrimitiveClass("boolean");
		} else if (clazz == Character.class || clazz == char.class) {
			cellType = HiClass.getPrimitiveClass("char");
		} else if (clazz == Byte.class || clazz == byte.class) {
			cellType = HiClass.getPrimitiveClass("byte");
		} else if (clazz == Short.class || clazz == short.class) {
			cellType = HiClass.getPrimitiveClass("short");
		} else if (clazz == Integer.class || clazz == int.class) {
			cellType = HiClass.getPrimitiveClass("int");
		} else if (clazz == Float.class || clazz == float.class) {
			cellType = HiClass.getPrimitiveClass("float");
		} else if (clazz == Long.class || clazz == long.class) {
			cellType = HiClass.getPrimitiveClass("long");
		} else if (clazz == Double.class || clazz == double.class) {
			cellType = HiClass.getPrimitiveClass("double");
		}
		cellTypes.put(clazz, cellType);
		return cellType;
	}

	public static HiClassArray getArrayType(Class<?> clazz) {
		int dimension = 0;
		Class<?> cellClass = clazz;
		while (cellClass.isArray()) {
			cellClass = cellClass.getComponentType();
			dimension++;
		}

		HiClass cellType = getCellType(cellClass);
		return getArrayClass(cellType, dimension);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		code(os, CLASS_OBJECT);
	}

	public void code(CodeContext os, int classType) throws IOException {
		// write class type
		os.writeByte(classType);

		// constructor parameters
		if (superClass != null) {
			os.writeBoolean(true);
			os.writeType(Type.getType(superClass));
			// Type.getType(superClass).code(os);
		} else if (superClassType != null) {
			os.writeBoolean(true);
			os.writeType(superClassType);
			// superClassType.code(os);
		} else {
			os.writeBoolean(false);
		}

		os.writeBoolean(enclosingClass != null);
		if (enclosingClass != null) {
			os.writeClass(enclosingClass);
		}

		os.writeUTF(name);
		os.writeByte(type);

		// content
		os.writeBoolean(isInterface);
		os.writeNullable(modifiers);
		os.writeTypes(interfaceTypes);

		os.writeShort(fields != null ? fields.length : 0);
		os.writeShort(initializers != null ? initializers.length : 0);
		os.write(initializers); // contains ordered fields and blocks

		os.writeShort(constructors != null ? constructors.length : 0);
		os.write(constructors);

		os.writeShort(methods != null ? methods.length : 0);
		os.write(methods);

		os.writeShort(classes != null ? classes.length : 0);
		if (classes != null) {
			for (int i = 0; i < classes.length; i++) {
				os.writeClass(classes[i]);
			}
		}
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		int classType = os.readByte();
		switch (classType) {
			case CLASS_OBJECT:
				return decodeObject(os, classType);

			case CLASS_PRIMITIVE:
				return HiClassPrimitive.decode(os);

			case CLASS_ARRAY:
				return HiClassArray.decode(os);

			case CLASS_ENUM:
				return HiClassEnum.decode(os);

			case CLASS_RECORD:
				return HiClassRecord.decode(os);

			case CLASS_NULL:
				return HiClassNull.decode(os);
		}
		throw new RuntimeException("unknown class type: " + classType);
	}

	public static HiClass decodeObject(DecodeContext os, int classType) throws IOException {
		final HiClass[] classAccess = new HiClass[1];

		// constructor parameters
		Type superClassType = null;
		if (os.readBoolean()) {
			superClassType = os.readType();
		}

		HiClass outerClass = null;
		boolean initClass = true;
		if (os.readBoolean()) {
			try {
				outerClass = os.readClass();
			} catch (NoClassException exc) {
				initClass = false;
				os.addClassLoadListener(new ClassLoadListener() {
					@Override
					public void classLoaded(HiClass clazz) {
						classAccess[0].init(clazz, classAccess[0].name, classAccess[0].type);
					}
				}, exc.getIndex());
			}
		}

		String name = os.readUTF();
		int type = os.readByte();

		HiClass clazz;
		if (classType == CLASS_ENUM) {
			clazz = new HiClassEnum(name, type);
		} else if (classType == CLASS_RECORD) {
			clazz = new HiClassRecord(name, type);
		} else {
			clazz = new HiClass(superClassType, name, type);
		}
		classAccess[0] = clazz;
		if (initClass) {
			clazz.init(outerClass, name, type);
		}

		HiClass oldClass = os.getHiClass();
		os.setHiClass(clazz);

		// content
		clazz.isInterface = os.readBoolean();
		clazz.modifiers = os.readNullable(Modifiers.class);
		clazz.interfaceTypes = os.readTypes();
		int fieldsCount = os.readShort();
		clazz.initializers = os.readNodeArray(NodeInitializer.class, os.readShort()); // contains ordered fields and blocks
		clazz.constructors = os.readArray(HiConstructor.class, os.readShort());
		clazz.methods = os.readArray(HiMethod.class, os.readShort());

		if (fieldsCount > 0) {
			clazz.fields = new HiField[fieldsCount];
			int index = 0;
			for (NodeInitializer initializer : clazz.initializers) {
				if (initializer instanceof HiField) {
					clazz.fields[index++] = (HiField) initializer;
				}
			}
		}

		clazz.classes = new HiClass[os.readShort()];
		for (int i = 0; i < clazz.classes.length; i++) {
			final int index = i;
			try {
				clazz.classes[i] = os.readClass();
			} catch (NoClassException exc) {
				os.addClassLoadListener(new ClassLoadListener() {
					@Override
					public void classLoaded(HiClass clazz) {
						classAccess[0].classes[index] = clazz;
					}
				}, exc.getIndex());
			}
		}

		os.setHiClass(oldClass);

		// try resolve super class
		if (superClassType != null) {
			clazz.superClass = superClassType.resolveClass(null);
		}
		return clazz;
	}

	@Override
	public int hashCode() {
		return fullName.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HiClass) {
			HiClass c = (HiClass) o;
			return fullName.equals(c.fullName);
		}
		return false;
	}

	public String getClassName() {
		if (this instanceof HiClassArray) {
			HiClassArray arrayClass = (HiClassArray) this;
			return arrayClass.className;
		} else {
			return fullName;
		}
	}

	public boolean hasOutboundObject() {
		return !isStatic() && !isTopLevel();
	}

	public boolean isStatic() {
		return modifiers != null && modifiers.isStatic();
	}

	public boolean isFinal() {
		return modifiers != null && modifiers.isFinal();
	}

	public boolean isAbstract() {
		return modifiers != null && modifiers.isAbstract();
	}

	public boolean isTopLevel() {
		return enclosingClass == null || enclosingClass.name == "@root";
	}

	public Class getJavaClass() {
		switch (fullName) {
			case "String":
				return String.class;
			case "HashMap":
				return HashMap.class;
			case "ArrayList":
				return ArrayList.class;
		}
		return null;
	}
}
