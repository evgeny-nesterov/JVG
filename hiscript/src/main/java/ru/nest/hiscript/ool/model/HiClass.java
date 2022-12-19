package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compiler.ClassFileParseRule;
import ru.nest.hiscript.ool.compiler.CompileClassContext;
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
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiClass implements Codeable, TokenAccessible {
	public final static int CLASS_OBJECT = 0;

	public final static int CLASS_PRIMITIVE = 1;

	public final static int CLASS_ARRAY = 2;

	public final static int CLASS_ENUM = 3;

	public final static int CLASS_RECORD = 4;

	public final static int CLASS_ANNOTATION = 5;

	public final static int CLASS_NULL = 6;

	public final static int CLASS_TYPE_TOP = 0; // No outbound class

	public final static int CLASS_TYPE_INNER = 1; // static (NESTED) and not

	// static (INNER)
	public final static int CLASS_TYPE_LOCAL = 2; // in method, constructor

	public final static int CLASS_TYPE_ANONYMOUS = 3; // like new Object() {...}

	public static HiClass OBJECT_CLASS;

	public static String ROOT_CLASS_NAME = "@root";

	public static String OBJECT_CLASS_NAME = "Object";

	public static String STRING_CLASS_NAME = "String";

	public static String HASHMAP_CLASS_NAME = "HashMap";

	public static String ARRAYLIST_CLASS_NAME = "ArrayList";

	public Token token;

	public final static HiClassLoader systemClassLoader = new HiClassLoader("system");

	public static HiClassLoader userClassLoader = new HiClassLoader("user", systemClassLoader);

	public static void setUserClassLoader(HiClassLoader classLoader) {
		systemClassLoader.removeClassLoader(userClassLoader);
		systemClassLoader.addClassLoader(classLoader);
		userClassLoader = classLoader;
	}

	protected HiClassLoader classLoader;

	public HiClassLoader getClassLoader() {
		return classLoader;
	}

	static {
		loadSystemClasses();
	}

	private static void loadSystemClasses() {
		Native.register(SystemImpl.class);
		Native.register(ObjectImpl.class);

		try {
			// object
			OBJECT_CLASS = systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Object.hi")).get(0);
			OBJECT_CLASS.superClassType = null;
			HiConstructor emptyConstructor = new HiConstructor(OBJECT_CLASS, null, new Modifiers(), (List<NodeArgument>) null, null, null, BodyConstructorType.NONE);
			OBJECT_CLASS.constructors = new HiConstructor[] {emptyConstructor};

			// TODO define classes initialization order automatically
			List<HiClass> classes = new ArrayList<>();
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/String.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Class.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Enum.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Record.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/AutoCloseable.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/System.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Math.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Exception.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/RuntimeException.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/AssertException.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/ArrayList.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/HashMap.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Thread.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Java.hi"), false));

			HiCompiler compiler = new HiCompiler(null);
			ValidationInfo validationInfo = new ValidationInfo(compiler);
			for (HiClass clazz : classes) {
				CompileClassContext ctx = new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP);
				clazz.validate(validationInfo, ctx);
			}
			validationInfo.throwExceptionIf();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	// for ClassPrimitive, ClassArray and ClassNull
	public HiClass(HiClassLoader classLoader, HiClass superClass, HiClass enclosingClass, String name, int type, ClassResolver classResolver) {
		this.superClass = superClass;
		if (superClass != null) {
			this.superClassType = Type.getType(superClass);
		}
		init(classLoader, classResolver, enclosingClass, name, type);
	}

	// for ClassParseRule, InterfaceParseRule and NewParseRule
	public HiClass(HiClassLoader classLoader, Type superClassType, HiClass enclosingClass, Type[] interfaceTypes, String name, int type, ClassResolver classResolver) {
		this.superClassType = superClassType;
		this.interfaceTypes = interfaceTypes;
		init(classLoader, classResolver, enclosingClass, name, type);
	}

	// for decode
	private HiClass(Type superClassType, String name, int type) {
		this.superClassType = superClassType;
		this.name = name.intern();
		this.type = type;
		// init(...) is in decode
	}

	private void init(HiClassLoader classLoader, ClassResolver classResolver, HiClass enclosingClass, String name, int type) {
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
		if (superClass == null && superClassType != null && classResolver instanceof RuntimeContext) {
			superClass = superClassType.getClass(classResolver);
		}

		// intern name to optimize via a == b
		this.name = name.intern();
		this.fullName = getFullName(classLoader);
		this.classLoader = classLoader;
	}

	public String getFullName(HiClassLoader classLoader) {
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
						} while (classLoader.getClass(fullName) != null);
						break;

					case CLASS_TYPE_ANONYMOUS:
						number = 0;
						do {
							name = Integer.toString(number);
							fullName = enclosingClass.fullName + "$" + number + name;
							number++;
						} while (classLoader.getClass(fullName) != null);
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

	public void init(ClassResolver classResolver) {
		if (!isInitialized) {
			isInitialized = true;

			if (enclosingClass != null) {
				enclosingClass.init(classResolver);
			}

			// resolve super class if needed
			if (superClass == null && superClassType != null) {
				superClass = superClassType.getClass(classResolver);
				if (superClass == null) {
					classResolver.processResolverException("Can't resolve class '" + superClassType.fullName + "'");
					return;
				}
			}

			if (superClass != null) {
				// init super class
				superClass.init(classResolver);

				// TODO: move this logic to parser ???

				// check super class on static
				if (!superClass.isStatic() && !superClass.isTopLevel() && isStatic()) {
					classResolver.processResolverException("Static class " + fullName + " can not extends not static and not top level class");
					return;
				}

				// check super class on final
				if (superClass.isFinal()) {
					classResolver.processResolverException("The type " + fullName + " cannot subclass the final class " + superClass.fullName);
					return;
				}
			}

			// resolve interfaces
			if (interfaces == null && interfaceTypes != null) {
				interfaces = new HiClass[interfaceTypes.length];
				for (int i = 0; i < interfaceTypes.length; i++) {
					interfaces[i] = interfaceTypes[i].getClass(classResolver);
				}
			}

			if (interfaces != null) {
				for (HiClass classInterface : interfaces) {
					// init interface
					classInterface.init(classResolver);

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
					clazz.init(classResolver);
				}
			}

			RuntimeContext ctx = classResolver instanceof RuntimeContext ? (RuntimeContext) classResolver : new RuntimeContext(null, false);
			ctx.enterInitialization(this, null, null);
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
				if (ctx != classResolver) {
					ctx.close();
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
	public NodeAnnotation[] annotations;

	public HiField<?>[] fields;

	public NodeInitializer[] initializers;

	public HiConstructor[] constructors;

	public HiMethod[] methods;

	public HiClass[] classes;

	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(RuntimeContext.STATIC_CLASS, this);
		HiClass outboundClass = ctx.clazz;
		ctx.clazz = this;
		boolean valid = true;

		if (superClassType != null && superClass == null) {
			superClass = superClassType.getClass(ctx);
		}

		// check modifiers
		if (ctx.enclosingClass != null && isStatic()) {
			validationInfo.error("Illegal modifier for the local class " + fullName + "; only abstract or final is permitted", token);
			valid = false;
		}

		if (initializers != null) {
			for (NodeInitializer initializer : initializers) {
				valid &= ((Node) initializer).validate(validationInfo, ctx);
			}
		}

		if (classes != null) {
			for (HiClass innerClass : classes) {
				if (innerClass.enclosingClass != null && !innerClass.enclosingClass.isTopLevel() && !innerClass.enclosingClass.isStatic()) {
					if (innerClass.isInterface) {
						validationInfo.error("The member interface " + innerClass.fullName + " can only be defined inside a top-level class or interface", innerClass.token);
						valid = false;
						continue;
					}

					// check on valid static modifier (includes annotations)
					if (innerClass.isStatic() && !innerClass.isDeclaredInRootClass()) {
						validationInfo.error("The member type " + innerClass.fullName + " cannot be declared static; static types can only be declared in static or top level types", innerClass.token);
						valid = false;
						continue;
					}
				}
				valid &= ctx.addLocalClass(innerClass);
			}
		}

		if (constructors != null) {
			for (HiConstructor constructor : constructors) {
				valid &= constructor.validate(validationInfo, ctx);
			}
		}

		if (methods != null) {
			for (HiMethod method : methods) {
				valid &= method.validate(validationInfo, ctx);
			}
		}
		ctx.exit();

		ctx.addLocalClass(this);
		ctx.clazz = outboundClass;
		return valid;
	}

	public HiClass getChild(ClassResolver classResolver, String name) {
		HiClass child = null;
		if (classes != null) {
			for (HiClass c : classes) {
				if (c.name.equals(name) || c.fullName.equals(name)) {
					child = c;
					child.init(classResolver);
					break;
				}
			}
		}
		return child;
	}

	private HiClassArray arrayClass;

	public HiClassArray getArrayClass() {
		if (arrayClass == null) {
			arrayClass = new HiClassArray(classLoader, this);
		}
		return arrayClass;
	}

	public HiClassArray getArrayClass(int dimensions) {
		HiClassArray c = getArrayClass();
		for (int i = 1; i < dimensions; i++) {
			c = c.getArrayClass();
		}
		return c;
	}

	private Map<String, HiClass> classesMap;

	public HiClass getClass(ClassResolver classResolver, String name) {
		if (classesMap != null && classesMap.containsKey(name)) {
			return classesMap.get(name);
		}

		HiClass clazz = _getClass(classResolver, name);
		if (clazz != null) {
			if (classesMap == null) {
				classesMap = new HashMap<>(1);
			}
			classesMap.put(name, clazz);
		}
		return clazz;
	}

	/*
	 * name - is String.intern value to optimize via s1 == s2
	 */
	private HiClass _getClass(ClassResolver classResolver, String name) {
		// inner classes
		if (classes != null) {
			for (HiClass c : classes) {
				if (c.name.equals(name) || c.fullName.equals(name)) {
					c.init(classResolver);
					return c;
				}
			}
		}

		// parent class
		if (superClass != null) {
			if (superClass == this) {
				throw new RuntimeException("cyclic");
			}
			HiClass c = superClass.getClass(classResolver, name);
			if (c != null) {
				return c;
			}
		}

		if (classResolver != null) {
			HiClass clazz = this;
			while (clazz != null) {
				// check local enclosing classes
				HiClass localClass = classResolver.getLocalClass(clazz, name);
				if (localClass != null) {
					return localClass;
				}

				// check enclosing classes
				localClass = clazz.getChild(classResolver, name);
				if (localClass != null) {
					return localClass;
				}

				clazz = clazz.enclosingClass;
			}
		}

		// registered classes
		return forName(classResolver, name);
	}

	public boolean isInstanceof(HiClass clazz) {
		if (this == clazz) {
			return true;
		}
		if (clazz == null) {
			return false;
		}
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

	public HiField<?> getField(ClassResolver classResolver, String name) {
		if (fieldsMap != null && fieldsMap.containsKey(name)) {
			return fieldsMap.get(name);
		}

		HiField<?> field = _searchField(classResolver, name);
		if (field != null) {
			if (fieldsMap == null) {
				fieldsMap = new HashMap<>();
			}
			fieldsMap.put(name, field);
		}
		return field;
	}

	protected HiField<?> _searchField(ClassResolver classResolver, String name) {
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
				field = i.getField(classResolver, name);
				if (field != null) {
					break;
				}
			}
		}

		// super fields
		if (field == null && superClass != null) {
			field = superClass.getField(classResolver, name);
		}
		return field;
	}

	private final Map<MethodSignature, HiMethod> methodsHash = new HashMap<>();

	public HiMethod searchMethod(ClassResolver classResolver, String name, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set(name, argTypes);
		HiMethod method = methodsHash.get(signature);
		if (method == null) {
			method = _searchMethod(classResolver, name, argTypes);
			if (method != null) {
				methodsHash.put(new MethodSignature(signature), method);
			}
		}
		return method;
	}

	protected HiMethod _searchMethod(ClassResolver classResolver, String name, HiClass... argTypes) {
		// this methods
		if (methods != null && methods.length > 0) {
			for (HiMethod m : methods)
				FOR:{
					if (m.name.equals(name)) {
						if (m.hasVarargs()) {
							int mainArgCount = m.argCount - 1;
							if (mainArgCount > argTypes.length) {
								continue;
							}

							m.resolve(classResolver);

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

							m.resolve(classResolver);

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
			HiMethod m = superClass.searchMethod(classResolver, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		if (interfaces != null) {
			HiClass id = null;
			HiMethod md = null;
			HiMethod m = null;
			for (HiClass i : interfaces) {
				HiMethod _m = i.searchMethod(classResolver, name, argTypes);
				if (_m != null) {
					if (_m.modifiers.isDefault()) {
						if (md != null) {
							classResolver.processResolverException("ambiguous method " + name + " for interfaces " + id.fullName + " and " + i.fullName + ".");
							return null;
						}
						id = i;
						md = _m;
					} else {
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
			HiMethod m = enclosingClass.searchMethod(classResolver, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// not found
		return null;
	}

	public HiMethod getMethod(ClassResolver classResolver, String name, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set(name, argTypes);
		HiMethod method = methodsHash.get(signature);
		if (method == null) {
			method = _getMethod(classResolver, name, argTypes);
			if (method != null) {
				methodsHash.put(new MethodSignature(signature), method);
			}
		}
		return method;
	}

	private HiMethod _getMethod(ClassResolver classResolver, String name, HiClass... argTypes) {
		// this methods
		if (methods != null) {
			for (HiMethod m : methods)
				FOR:{
					if (m.name == name) {
						int argCount = m.argCount;
						if (argCount != argTypes.length) {
							continue;
						}

						m.resolve(classResolver);

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
			HiMethod m = superClass.getMethod(classResolver, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// enclosing methods
		if (!isTopLevel()) {
			HiMethod m = enclosingClass.getMethod(classResolver, name, argTypes);
			if (m != null) {
				return m;
			}
		}

		// not found
		return null;
	}

	private Map<MethodSignature, HiConstructor> constructorsHash = new HashMap<>();

	public HiConstructor searchConstructor(ClassResolver classResolver, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set("", argTypes);
		HiConstructor constructor = constructorsHash.get(signature);
		if (constructor == null) {
			constructor = _searchConstructor(classResolver, argTypes);
			if (constructor != null) {
				constructorsHash.put(new MethodSignature(signature), constructor);
			}
		}
		return constructor;
	}

	protected HiConstructor _searchConstructor(ClassResolver classResolver, HiClass[] argTypes) {
		if (constructors != null) {
			for (HiConstructor constructor : constructors) {
				if (matchConstructor(classResolver, constructor, argTypes)) {
					return constructor;
				}
			}
		}
		return null;
	}

	protected boolean matchConstructor(ClassResolver classResolver, HiConstructor constructor, HiClass[] argTypes) {
		int argCount = constructor.arguments != null ? constructor.arguments.length : 0;
		if (argCount != (argTypes != null ? argTypes.length : 0)) {
			return false;
		}

		constructor.resolve(classResolver);
		for (int i = 0; i < argCount; i++) {
			if (!HiField.autoCast(argTypes[i], constructor.argClasses[i])) {
				return false;
			}
		}
		return true;
	}

	public HiConstructor getConstructor(ClassResolver classResolver, HiClass... argTypes) {
		if (constructors != null) {
			for (HiConstructor c : constructors)
				FOR:{
					int argCount = c.arguments != null ? c.arguments.length : 0;
					if (argCount != argTypes.length) {
						continue;
					}

					c.resolve(classResolver);
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

	public boolean isNumber() {
		return false;
	}

	public boolean isArray() {
		return false;
	}

	public boolean isAnnotation() {
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

	public static HiClass forName(ClassResolver classResolver, String name) {
		HiClass clazz = systemClassLoader.getClass(name);
		if (clazz != null && classResolver != null) {
			clazz.init(classResolver);
		}
		return clazz;
	}

	public static HiClass getNullClass() {
		return HiClassNull.NULL;
	}

	public static HiClass getPrimitiveClass(String name) {
		return HiClassPrimitive.getPrimitiveClass(name);
	}

	@Override
	public String toString() {
		return fullName != null ? fullName : name;
	}

	public static HiClass getCellType(Class<?> clazz) {
		HiClass cellType = null;
		if (clazz == HiObject.class || clazz == Object.class) {
			cellType = HiClass.OBJECT_CLASS;
		} else if (clazz == Boolean.class || clazz == boolean.class) {
			cellType = HiClassPrimitive.BOOLEAN;
		} else if (clazz == Character.class || clazz == char.class) {
			cellType = HiClassPrimitive.CHAR;
		} else if (clazz == Byte.class || clazz == byte.class) {
			cellType = HiClassPrimitive.BYTE;
		} else if (clazz == Short.class || clazz == short.class) {
			cellType = HiClassPrimitive.SHORT;
		} else if (clazz == Integer.class || clazz == int.class) {
			cellType = HiClassPrimitive.INT;
		} else if (clazz == Float.class || clazz == float.class) {
			cellType = HiClassPrimitive.FLOAT;
		} else if (clazz == Long.class || clazz == long.class) {
			cellType = HiClassPrimitive.LONG;
		} else if (clazz == Double.class || clazz == double.class) {
			cellType = HiClassPrimitive.DOUBLE;
		}
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
		return cellType.getArrayClass(dimension);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		code(os, CLASS_OBJECT);
	}

	public void code(CodeContext os, int classType) throws IOException {
		// write class type
		os.writeByte(classType);
		os.writeToken(token);

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
		os.writeShortArray(annotations);
		os.writeNullable(modifiers);
		os.writeTypes(interfaceTypes);

		os.writeShort(fields != null ? fields.length : 0);
		os.writeShortArray(initializers); // contains ordered fields and blocks
		os.writeShortArray(constructors);
		os.writeShortArray(methods);

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
		Token token = os.readToken();

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
						classAccess[0].init(userClassLoader, null, clazz, classAccess[0].name, classAccess[0].type);
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
			clazz = new HiClassRecord(name, type, null);
		} else {
			clazz = new HiClass(superClassType, name, type);
		}
		clazz.token = token;
		classAccess[0] = clazz;
		if (initClass) {
			clazz.init(userClassLoader, null, outerClass, name, type);
		}

		HiClass oldClass = os.getHiClass();
		os.setHiClass(clazz);

		// content
		clazz.isInterface = os.readBoolean();
		clazz.annotations = os.readShortNodeArray(NodeAnnotation.class);
		clazz.modifiers = os.readNullable(Modifiers.class);
		clazz.interfaceTypes = os.readTypes();
		int fieldsCount = os.readShort();
		clazz.initializers = os.readShortNodeArray(NodeInitializer.class); // contains ordered fields and blocks
		clazz.constructors = os.readShortArray(HiConstructor.class);
		clazz.methods = os.readShortArray(HiMethod.class);

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
			clazz.superClass = superClassType.getClass(/*no context*/ null);
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
		return enclosingClass == null || enclosingClass.name.equals(HiClass.ROOT_CLASS_NAME);
	}

	public boolean isDeclaredInRootClass() {
		return topClass != null && topClass.fullName.equals(HiClass.ROOT_CLASS_NAME);
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

	public HiClass getCommonClass(HiClass c) {
		if (c.isInstanceof(this)) {
			return this;
		}
		while (c != null) {
			if (isInstanceof(c)) {
				return c;
			}
			c = c.superClass;
		}
		return OBJECT_CLASS;
	}

	@Override
	public Token getToken() {
		return token;
	}
}
