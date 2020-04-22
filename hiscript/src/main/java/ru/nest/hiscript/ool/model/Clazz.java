package ru.nest.hiscript.ool.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import ru.nest.hiscript.ool.compiler.ClassFileParseRule;
import ru.nest.hiscript.ool.model.Constructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.classes.ClazzArray;
import ru.nest.hiscript.ool.model.classes.ClazzNull;
import ru.nest.hiscript.ool.model.classes.ClazzPrimitive;
import ru.nest.hiscript.ool.model.lib.ObjectImpl;
import ru.nest.hiscript.ool.model.lib.SystemImpl;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Tokenizer;

public class Clazz implements Codable {
	public final static int CLASS_OBJECT = 0;

	public final static int CLASS_PRIMITIVE = 1;

	public final static int CLASS_ARRAY = 2;

	public final static int CLASS_NULL = 3;

	public final static int CLASS_TYPE_NONE = 0; // used for enclosing classes

	public final static int CLASS_TYPE_TOP = 1; // No outbound class

	public final static int CLASS_TYPE_INNER = 2; // static (NESTED) and not

	// static (INNER)
	public final static int CLASS_TYPE_LOCAL = 3; // in method, constructor

	public final static int CLASS_TYPE_ANONYMOUS = 4; // like new Object() {...}

	private static HashMap<String, Clazz> loadedClasses;

	public static Clazz OBJECT_CLASS;

	static {
		loadedClasses = new HashMap<String, Clazz>();

		Native.register(SystemImpl.class);
		Native.register(ObjectImpl.class);

		try {
			// object
			load(Compiler.class.getResource("lib/Object.txt"));

			OBJECT_CLASS = forName(null, "Object");
			OBJECT_CLASS.superClassType = null;
			Constructor emptyConstructor = new Constructor(OBJECT_CLASS, new Modifiers(), (List<NodeArgument>) null, null, null, BodyConstructorType.NONE);
			OBJECT_CLASS.constructors = new Constructor[] { emptyConstructor };

			load(Compiler.class.getResource("lib/String.txt"));
			load(Compiler.class.getResource("lib/Class.txt"));
			load(Compiler.class.getResource("lib/System.txt"));
			load(Compiler.class.getResource("lib/Math.txt"));
			load(Compiler.class.getResource("lib/Exception.txt"));
			load(Compiler.class.getResource("lib/ArrayList.txt"));
			load(Compiler.class.getResource("lib/HashMap.txt"));
			load(Compiler.class.getResource("lib/Thread.txt"));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
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
	public Clazz(Clazz superClass, Clazz enclosingClass, String name, int type) {
		this.superClass = superClass;
		if (superClass != null) {
			this.superClassType = Type.getType(superClass);
		}
		init(enclosingClass, name, type);
	}

	// for ClassParseRule, InterfaceParseRule and NewParseRule
	public Clazz(Type superClassType, Clazz enclosingClass, Type[] interfaceTypes, String name, int type) {
		this.superClassType = superClassType;
		this.interfaceTypes = interfaceTypes;
		init(enclosingClass, name, type);
	}

	// for decode
	private Clazz(Type superClassType, String name, int type) {
		this.superClassType = superClassType;
		this.name = name.intern();
		this.type = type;
		// init(...) is in decode
	}

	private void init(Clazz enclosingClass, String name, int type) {
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
		if (loadedClasses.containsKey(fullName)) {
			throw new ClassLoadException("class '" + fullName + "' already loaded");
		}
		loadedClasses.put(fullName, this);
	}

	private boolean isInitialized = false;

	public void init(RuntimeContext current_ctx) {
		if (!isInitialized) {
			isInitialized = true;

			// resolve super class if needed
			if (superClass == null && superClassType != null) {
				superClass = superClassType.getClazz(current_ctx);
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
				interfaces = new Clazz[interfaceTypes.length];
				for (int i = 0; i < interfaceTypes.length; i++) {
					interfaces[i] = interfaceTypes[i].getClazz(current_ctx);
				}
			}

			if (interfaces != null) {
				for (Clazz interfac : interfaces) {
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
				superClassType = Type.ObjectType;
			}

			// init children classes
			if (classes != null) {
				for (Clazz clazz : classes) {
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
			} catch (Throwable exc) {
				exc.printStackTrace();
				ctx.throwException("can not initialize class " + fullName + ": " + exc.getMessage());
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

	public Clazz superClass;

	public Clazz[] interfaces;

	public String name;

	public String fullName;

	public int type;

	public Clazz enclosingClass;

	public Clazz topClass;

	public boolean isInterface = false;

	// content
	public Field<?>[] fields;

	public NodeInitializer[] initializers;

	public Constructor[] constructors;

	public Method[] methods;

	public Clazz[] classes;

	public Clazz getChildren(RuntimeContext ctx, String name) {
		Clazz children = null;
		if (classes != null) {
			for (Clazz c : classes) {
				if (c.name == name || c.fullName == name) {
					children = c;
					children.init(ctx);
					break;
				}
			}
		}
		return children;
	}

	private HashMap<String, Clazz> classes_map;

	public Clazz getClass(RuntimeContext ctx, String name) {
		if (classes_map != null && classes_map.containsKey(name)) {
			return classes_map.get(name);
		}

		Clazz clazz = _getClass(ctx, name);
		if (clazz != null) {
			if (classes_map == null) {
				classes_map = new HashMap<String, Clazz>(1);
			}
			classes_map.put(name, clazz);
		}
		return clazz;
	}

	/*
	 * name - is String.intern value to optimize via s1 == s2
	 */
	private Clazz _getClass(RuntimeContext ctx, String name) {
		// inner classes
		if (classes != null) {
			for (Clazz c : classes) {
				if (c.name == name || c.fullName == name) {
					c.init(ctx);
					return c;
				}
			}
		}

		// parent class
		if (superClass != null) {
			Clazz c = superClass.getClass(ctx, name);
			if (c != null) {
				return c;
			}
		}

		Clazz clazz = this;
		while (clazz != null) {
			// check local enclosing classes
			Clazz c = ctx.getLocalClass(clazz, name);
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

	public boolean isInstanceof(Clazz clazz) {
		Clazz c = this;
		while (c != null) {
			if (c == clazz) {
				return true;
			}
			if (c.hasInterfaceInstanceof(clazz)) {
				return true;
			}
			c = c.superClass;
		}

		return false;
	}

	public boolean hasInterfaceInstanceof(Clazz superInterface) {
		if (interfaces != null && superInterface.isInterface) {
			for (Clazz in : interfaces) {
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

	private HashMap<String, Field<?>> fields_map;

	public Field<?> getField(String name) {
		if (fields_map != null && fields_map.containsKey(name)) {
			return fields_map.get(name);
		}

		Field<?> field = null;

		// this fields
		if (fields != null && fields.length > 0) {
			for (Field<?> f : fields) {
				if (f.name == name) {
					field = f;
					break;
				}
			}
		}

		// interfaces static fields
		if (field == null && interfaces != null) {
			for (Clazz in : interfaces) {
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
				fields_map = new HashMap<String, Field<?>>();
			}
			fields_map.put(name, field);
		}

		return field;
	}

	private HashMap<MethodSignature, Method> methods_hash = new HashMap<MethodSignature, Method>();

	private MethodSignature signature = new MethodSignature();

	// name - interned
	public Method searchMethod(RuntimeContext ctx, String name, Clazz... argTypes) {
		signature.set(name, argTypes);
		Method method = methods_hash.get(signature);
		if (method == null) {
			method = _searchMethod(ctx, name, argTypes);
			if (method != null) {
				methods_hash.put(new MethodSignature(signature), method);
			}
		}
		return method;
	}

	// name - interned
	private Method _searchMethod(RuntimeContext ctx, String name, Clazz... argTypes) {
		// this methods
		if (methods != null && methods.length > 0) {
			for (Method m : methods)
				FOR: {
					if (m.name == name) {
						int argCount = m.argCount;
						if (argCount != argTypes.length) {
							continue;
						}

						m.resolve(ctx);

						for (int i = 0; i < argCount; i++) {
							if (!Field.autoCast(argTypes[i], m.argClasses[i])) {
								break FOR;
							}
						}

						return m;
					}
				}
		}

		// super methods
		if (superClass != null) {
			Method m = superClass.searchMethod(ctx, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// enclosing methods
		if (!isTopLevel()) {
			Method m = enclosingClass.searchMethod(ctx, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// not found
		return null;
	}

	public Method getMethod(RuntimeContext ctx, String name, Clazz... argTypes) {
		signature.set(name, argTypes);
		Method method = methods_hash.get(signature);
		if (method == null) {
			method = _getMethod(ctx, name, argTypes);
			if (method != null) {
				methods_hash.put(new MethodSignature(signature), method);
			}
		}
		return method;
	}

	private Method _getMethod(RuntimeContext ctx, String name, Clazz... argTypes) {
		// this methods
		if (methods != null) {
			for (Method m : methods)
				FOR: {
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
			Method m = superClass.getMethod(ctx, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// enclosing methods
		if (!isTopLevel()) {
			Method m = enclosingClass.getMethod(ctx, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// not found
		return null;
	}

	public Constructor searchConstructor(RuntimeContext ctx, Clazz... argTypes) {
		if (constructors != null) {
			for (Constructor c : constructors)
				FOR: {
					int argCount = c.arguments != null ? c.arguments.length : 0;
					if (argCount != argTypes.length) {
						continue;
					}

					c.resolve(ctx);
					for (int i = 0; i < argCount; i++) {
						if (!Field.autoCast(argTypes[i], c.argClasses[i])) {
							break FOR;
						}
					}

					return c;
				}
		}

		return null;
	}

	public Constructor getConstructor(RuntimeContext ctx, Clazz... argTypes) {
		if (constructors != null) {
			for (Constructor c : constructors)
				FOR: {
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

	public Clazz getArrayType() {
		return null;
	}

	public static Clazz forName(RuntimeContext ctx, String name) {
		Clazz clazz = loadedClasses.get(name);
		if (clazz != null) {
			clazz.init(ctx);
		}
		return clazz;
	}

	public static Clazz getNullClass() {
		return ClazzNull.NULL;
	}

	public static Clazz getPrimitiveClass(String name) {
		return ClazzPrimitive.getPrimitiveClass(name);
	}

	public static Clazz getArrayClass(Clazz cellClass) {
		return ClazzArray.getArrayClass(cellClass);
	}

	public static ClazzArray getArrayClass(Clazz cellClass, int dimensions) {
		return ClazzArray.getArrayClass(cellClass, dimensions);
	}

	@Override
	public String toString() {
		return fullName;
	}

	private static HashMap<Class<?>, Clazz> cellTypes = new HashMap<Class<?>, Clazz>(9);

	public static Clazz getCellType(Class<?> clazz) {
		if (cellTypes.containsKey(clazz)) {
			return cellTypes.get(clazz);
		}

		Clazz cellType = null;
		if (clazz == Obj.class || clazz == Object.class) {
			cellType = Clazz.OBJECT_CLASS;
		} else if (clazz == Boolean.class) {
			cellType = Clazz.getPrimitiveClass("boolean");
		} else if (clazz == Character.class) {
			cellType = Clazz.getPrimitiveClass("char");
		} else if (clazz == Byte.class) {
			cellType = Clazz.getPrimitiveClass("byte");
		} else if (clazz == Short.class) {
			cellType = Clazz.getPrimitiveClass("short");
		} else if (clazz == Integer.class) {
			cellType = Clazz.getPrimitiveClass("int");
		} else if (clazz == Float.class) {
			cellType = Clazz.getPrimitiveClass("float");
		} else if (clazz == Long.class) {
			cellType = Clazz.getPrimitiveClass("long");
		} else if (clazz == Double.class) {
			cellType = Clazz.getPrimitiveClass("double");
		}
		cellTypes.put(clazz, cellType);
		return cellType;
	}

	public static ClazzArray getArrayType(Class<?> clazz) {
		int dimension = 0;
		Class<?> cellClass = clazz;
		while (cellClass.isArray()) {
			cellClass = cellClass.getComponentType();
			dimension++;
		}

		Clazz cellType = getCellType(cellClass);
		return getArrayClass(cellType, dimension);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeByte(CLASS_OBJECT);

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

	public static Clazz decode(DecodeContext os) throws IOException {
		int classType = os.readByte();
		switch (classType) {
			case CLASS_OBJECT:
				return decodeObject(os);

			case CLASS_PRIMITIVE:
				return ClazzPrimitive.decode(os);

			case CLASS_ARRAY:
				return ClazzArray.decode(os);

			case CLASS_NULL:
				return ClazzNull.decode(os);
		}
		throw new RuntimeException("unknown class type: " + classType);
	}

	public static Clazz decodeObject(DecodeContext os) throws IOException {
		final Clazz[] classAccess = new Clazz[1];

		// constructor parameters
		Type superClassType = null;
		if (os.readBoolean()) {
			superClassType = os.readType();
		}

		Clazz outerClass = null;
		boolean initClass = true;
		if (os.readBoolean()) {
			try {
				outerClass = os.readClass();
			} catch (NoClassException exc) {
				initClass = false;
				os.addClassLoadListener(new ClassLoadListener() {
					@Override
					public void classLoaded(Clazz clazz) {
						classAccess[0].init(clazz, classAccess[0].name, classAccess[0].type);
					}
				}, exc.getIndex());
			}
		}

		String name = os.readUTF();
		int type = os.readByte();

		Clazz clazz = new Clazz(superClassType, name, type);
		classAccess[0] = clazz;
		if (initClass) {
			clazz.init(outerClass, name, type);
		}

		Clazz oldClazz = os.getClazz();
		os.setClazz(clazz);

		// content
		clazz.modifiers = os.readNullable(Modifiers.class);
		clazz.fields = os.readNodeArray(Field.class, os.readShort());
		clazz.initializers = os.readNodeArray(NodeInitializer.class, os.readShort());
		clazz.constructors = os.readArray(Constructor.class, os.readShort());
		clazz.methods = os.readArray(Method.class, os.readShort());

		clazz.classes = new Clazz[os.readShort()];
		for (int i = 0; i < clazz.classes.length; i++) {
			final int index = i;
			try {
				clazz.classes[i] = os.readClass();
			} catch (NoClassException exc) {
				os.addClassLoadListener(new ClassLoadListener() {
					@Override
					public void classLoaded(Clazz clazz) {
						classAccess[0].classes[index] = clazz;
					}
				}, exc.getIndex());
			}
		}

		os.setClazz(oldClazz);

		// resolve super class
		if (superClassType != null) {
			clazz.superClass = superClassType.getClazz(null);
		}

		return clazz;
	}

	@Override
	public int hashCode() {
		return fullName.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Clazz) {
			Clazz c = (Clazz) o;
			return fullName.equals(c.fullName);
		}
		return false;
	}

	public String getClassName() {
		if (this instanceof ClazzArray) {
			ClazzArray arrayClazz = (ClazzArray) this;
			return arrayClazz.className;
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
