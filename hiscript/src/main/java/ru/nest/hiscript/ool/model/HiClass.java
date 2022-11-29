package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compiler.ClassFileParseRule;
import ru.nest.hiscript.ool.model.HiConstructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.lib.ObjectImpl;
import ru.nest.hiscript.ool.model.lib.SystemImpl;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class HiClass implements Codeable {
	public final static int CLASS_OBJECT = 0;

	public final static int CLASS_PRIMITIVE = 1;

	public final static int CLASS_ARRAY = 2;

	public final static int CLASS_ENUM = 3;

	public final static int CLASS_NULL = 4;

	public final static int CLASS_TYPE_NONE = 0; // used for enclosing classes

	public final static int CLASS_TYPE_TOP = 1; // No outbound class

	public final static int CLASS_TYPE_INNER = 2; // static (NESTED) and not

	// static (INNER)
	public final static int CLASS_TYPE_LOCAL = 3; // in method, constructor

	public final static int CLASS_TYPE_ANONYMOUS = 4; // like new Object() {...}

	private static HashMap<String, HiClass> loadedClasses;

	public static HiClass OBJECT_CLASS;

	public static String ROOT_CLASS_NAME = "@root";

	static {
		loadSystemClasses();
	}

	private static void loadSystemClasses() {
		loadedClasses = new HashMap<>();

		Native.register(SystemImpl.class);
		Native.register(ObjectImpl.class);

		try {
			// object
			load(Compiler.class.getResource("/hilibs/Object.hi"));

			OBJECT_CLASS = forName(null, "Object");
			OBJECT_CLASS.superClassType = null;
			HiConstructor emptyConstructor = new HiConstructor(OBJECT_CLASS, new Modifiers(), (List<NodeArgument>) null, null, null, BodyConstructorType.NONE);
			OBJECT_CLASS.constructors = new HiConstructor[] {emptyConstructor};

			// TODO define classes initialization order automatically
			load(Compiler.class.getResource("/hilibs/String.hi"));
			load(Compiler.class.getResource("/hilibs/Class.hi"));
			load(Compiler.class.getResource("/hilibs/Enum.hi"));
			load(Compiler.class.getResource("/hilibs/System.hi"));
			load(Compiler.class.getResource("/hilibs/Math.hi"));
			load(Compiler.class.getResource("/hilibs/Exception.hi"));
			load(Compiler.class.getResource("/hilibs/RuntimeException.hi"));
			load(Compiler.class.getResource("/hilibs/AssertException.hi"));
			load(Compiler.class.getResource("/hilibs/ArrayList.hi"));
			load(Compiler.class.getResource("/hilibs/HashMap.hi"));
			load(Compiler.class.getResource("/hilibs/Thread.hi"));
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
		StringBuilder buf = new StringBuilder();

		int c;
		while ((c = is.read()) != -1) {
			buf.append((char) c);
		}

		load(buf.toString());
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
		this.name = name.intern();
		this.fullName = fullName.intern();

		// register class by fullName
		if (!ROOT_CLASS_NAME.equals(fullName)) {
			if (loadedClasses.containsKey(fullName)) {
				throw new ClassLoadException("class '" + fullName + "' already loaded");
			}
			loadedClasses.put(fullName, this);
		}
	}

	private boolean isInitialized = false;

	public void init(RuntimeContext current_ctx) {
		if (!isInitialized) {
			isInitialized = true;

			// resolve super class if needed
			if (superClass == null && superClassType != null) {
				superClass = superClassType.getClass(current_ctx);
			}

			if (superClass != null) {
				// init super class
				superClass.init(current_ctx);

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
					interfaces[i] = interfaceTypes[i].getClass(current_ctx);
				}
			}

			if (interfaces != null) {
				for (HiClass interfac : interfaces) {
					// init interface
					interfac.init(current_ctx);

					// check interface on static
					if (!interfac.isStatic() && !interfac.isTopLevel() && isStatic()) {
						throw new IllegalStateException("Static class " + fullName + " can not extends not static and not top level class");
					}
				}
			}

			// set super class to Object by default
			if (superClass == null && this != OBJECT_CLASS) {
				superClass = OBJECT_CLASS;
				superClassType = Type.objectType;
			}

			// init children classes
			if (classes != null) {
				for (HiClass clazz : classes) {
					clazz.init(current_ctx);
				}
			}

			RuntimeContext ctx = current_ctx != null ? current_ctx : RuntimeContext.get();
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
				if (ctx != current_ctx) {
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

	protected HashMap<String, HiField<?>> fields_map;

	public HiField<?> getField(String name) {
		if (fields_map != null && fields_map.containsKey(name)) {
			return fields_map.get(name);
		}

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
			for (HiClass in : interfaces) {
				field = in.getField(name);
				if (field != null) {
					break;
				}
			}
		}

		// super fields
		if (field == null && superClass != null) {
			field = superClass.getField(name);
		}

		if (field != null) {
			if (fields_map == null) {
				fields_map = new HashMap<>();
			}
			fields_map.put(name, field);
		}
		return field;
	}

	private HashMap<MethodSignature, HiMethod> methods_hash = new HashMap<>();

	private MethodSignature signature = new MethodSignature();

	// name - interned
	public HiMethod searchMethod(RuntimeContext ctx, String name, HiClass... argTypes) {
		signature.set(name, argTypes);
		HiMethod method = methods_hash.get(signature);
		if (method == null) {
			method = _searchMethod(ctx, name, argTypes);
			if (method != null) {
				methods_hash.put(new MethodSignature(signature), method);
			}
		}
		return method;
	}

	// name - interned
	private HiMethod _searchMethod(RuntimeContext ctx, String name, HiClass... argTypes) {
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
		signature.set(name, argTypes);
		HiMethod method = methods_hash.get(signature);
		if (method == null) {
			method = _getMethod(ctx, name, argTypes);
			if (method != null) {
				methods_hash.put(new MethodSignature(signature), method);
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

	public HiConstructor searchConstructor(RuntimeContext ctx, HiClass[] argTypes) {
		if (constructors != null) {
			for (HiConstructor c : constructors)
				FOR:{
					int argCount = c.arguments != null ? c.arguments.length : 0;
					if (argCount != (argTypes != null ? argTypes.length : 0)) {
						continue;
					}

					c.resolve(ctx);
					for (int i = 0; i < argCount; i++) {
						if (!HiField.autoCast(argTypes[i], c.argClasses[i])) {
							break FOR;
						}
					}

					return c;
				}
		}
		return null;
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

	public boolean isEnum() {
		return false;
	}

	public HiClass getArrayType() {
		return null;
	}

	public static HiClass forName(RuntimeContext ctx, String name) {
		HiClass clazz = loadedClasses.get(name);
		if (clazz != null) {
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
		return fullName;
	}

	private static HashMap<Class<?>, HiClass> cellTypes = new HashMap<>(9);

	public static HiClass getCellType(Class<?> clazz) {
		if (cellTypes.containsKey(clazz)) {
			return cellTypes.get(clazz);
		}

		HiClass cellType = null;
		if (clazz == HiObject.class || clazz == Object.class) {
			cellType = HiClass.OBJECT_CLASS;
		} else if (clazz == Boolean.class) {
			cellType = HiClass.getPrimitiveClass("boolean");
		} else if (clazz == Character.class) {
			cellType = HiClass.getPrimitiveClass("char");
		} else if (clazz == Byte.class) {
			cellType = HiClass.getPrimitiveClass("byte");
		} else if (clazz == Short.class) {
			cellType = HiClass.getPrimitiveClass("short");
		} else if (clazz == Integer.class) {
			cellType = HiClass.getPrimitiveClass("int");
		} else if (clazz == Float.class) {
			cellType = HiClass.getPrimitiveClass("float");
		} else if (clazz == Long.class) {
			cellType = HiClass.getPrimitiveClass("long");
		} else if (clazz == Double.class) {
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
			Type.getType(superClass).code(os);
		} else if (superClassType != null) {
			os.writeBoolean(true);
			superClassType.code(os);
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
		os.writeNullable(modifiers);

		os.writeShort(fields != null ? fields.length : 0);
		os.write(fields);

		os.writeShort(initializers != null ? initializers.length : 0);
		os.write(initializers);

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

		HiClass clazz = classType == CLASS_ENUM ? new HiClassEnum(name, type) : new HiClass(superClassType, name, type);
		classAccess[0] = clazz;
		if (initClass) {
			clazz.init(outerClass, name, type);
		}

		HiClass oldClass = os.getHiClass();
		os.setHiClass(clazz);

		// content
		clazz.modifiers = os.readNullable(Modifiers.class);
		clazz.fields = os.readNodeArray(HiField.class, os.readShort());
		clazz.initializers = os.readNodeArray(NodeInitializer.class, os.readShort());
		clazz.constructors = os.readArray(HiConstructor.class, os.readShort());
		clazz.methods = os.readArray(HiMethod.class, os.readShort());

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

		// resolve super class
		if (superClassType != null) {
			clazz.superClass = superClassType.getClass(null);
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
}
