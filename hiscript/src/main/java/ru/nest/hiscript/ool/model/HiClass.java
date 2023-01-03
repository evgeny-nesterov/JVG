package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiConstructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.classes.HiClassRecord;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldVar;
import ru.nest.hiscript.ool.model.lib.ObjectImpl;
import ru.nest.hiscript.ool.model.lib.SystemImpl;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

	public static String ENUM_CLASS_NAME = "Enum";

	public static String RECORD_CLASS_NAME = "Record";

	public static String HASHMAP_CLASS_NAME = "HashMap";

	public static String ARRAYLIST_CLASS_NAME = "ArrayList";

	public static String EXCEPTION_CLASS_NAME = "Exception";

	public static String RUNTIME_EXCEPTION_CLASS_NAME = "RuntimeException";

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
		systemClassLoader.clear();

		HiNative.register(SystemImpl.class);
		HiNative.register(ObjectImpl.class);

		try {
			List<HiClass> classes = new ArrayList<>();

			// object
			OBJECT_CLASS = systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Object.hi"), false).get(0);
			OBJECT_CLASS.superClassType = null;
			HiConstructor emptyConstructor = new HiConstructor(OBJECT_CLASS, null, new Modifiers(), (NodeArgument[]) null, null, null, null, BodyConstructorType.NONE);
			OBJECT_CLASS.constructors = new HiConstructor[] {emptyConstructor};
			classes.add(OBJECT_CLASS);

			// TODO define classes initialization order automatically
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

			HiCompiler compiler = new HiCompiler(systemClassLoader, null);
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

		// try resolve super class if needed
		if (superClass == null && superClassType != null && classResolver instanceof RuntimeContext) {
			superClass = superClassType.getClass(classResolver);
		}

		// intern name to optimize via a == b
		this.name = name.intern();
		this.fullName = getFullName(classLoader);

		if (classLoader == null) {
			classLoader = userClassLoader;
		}
		classLoader.addClass(this);
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
					classResolver.processResolverException("cannot resolve class '" + superClassType.fullName + "'");
					return;
				}
			}

			if (superClass != null) {
				// init super class
				superClass.init(classResolver);

				// TODO: move this logic to validation ???

				// check super class on static
				if (!superClass.isStatic() && !superClass.isTopLevel() && isStatic()) {
					classResolver.processResolverException("static class " + fullName + " can not extends not static and not top level class");
					return;
				}

				// check super class on final
				if (superClass.isFinal()) {
					classResolver.processResolverException("the type " + fullName + " cannot subclass the final class " + superClass.fullName);
					return;
				}
			}

			// resolve interfaces
			if (interfaceTypes != null) {
				if (interfaces == null) {
					interfaces = new HiClass[interfaceTypes.length];
				}
				for (int i = 0; i < interfaceTypes.length; i++) {
					if (interfaces[i] == null) {
						interfaces[i] = interfaceTypes[i].getClass(classResolver);
					}
				}
			}

			if (interfaces != null) {
				for (HiClass classInterface : interfaces) {
					if (classInterface == null) {
						// not found
						continue;
					}

					// init interface
					classInterface.init(classResolver);

					// TODO remove (interface is always static)?
					// check interface on static
					if (!classInterface.isStatic() && !classInterface.isTopLevel() && isStatic()) {
						classResolver.processResolverException("static class " + fullName + " can not extends not static and not top level class");
					}
				}
			}

			// set super class to Object by default
			if (superClass == null && this != OBJECT_CLASS && !isPrimitive() && !isNull() && !isInterface) {
				superClass = OBJECT_CLASS;
				superClassType = Type.objectType;
			}

			// init children classes
			if (innerClasses != null) {
				for (HiClass clazz : innerClasses) {
					clazz.init(classResolver);
				}
			}
		}

		initStaticInitializers(classResolver);
	}

	private boolean staticInitialized = false;

	/**
	 * Init static initializers only on runtime on first class reference
	 */
	private void initStaticInitializers(ClassResolver classResolver) {
		if (isInitialized && !staticInitialized && classResolver instanceof RuntimeContext) {
			staticInitialized = true;

			if (superClass != null) {
				superClass.initStaticInitializers(classResolver);
			}
			if (interfaces != null) {
				for (HiClass classInterface : interfaces) {
					classInterface.initStaticInitializers(classResolver);
				}
			}

			RuntimeContext ctx = classResolver instanceof RuntimeContext ? (RuntimeContext) classResolver : new RuntimeContext(classResolver.getCompiler(), false);
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
				ctx.throwRuntimeException("cannot initialize class " + fullName + ": " + exc.getMessage());
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

	public boolean isInterface = false;

	// content
	public NodeAnnotation[] annotations;

	public HiField<?>[] fields;

	public NodeInitializer[] initializers;

	public HiConstructor[] constructors;

	public HiMethod[] methods;

	public HiClass[] innerClasses;

	private Boolean valid;

	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (valid == null) {
			valid = true;
			valid = _validate(validationInfo, ctx);
		}
		return valid;
	}

	private boolean _validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass outboundClass = ctx.clazz;
		boolean valid = true;

		// resolve interfaces before set ctx.clazz, as interfaces has to be initialized outsize of this class context
		if (interfaces == null && interfaceTypes != null) {
			interfaces = new HiClass[interfaceTypes.length];
			for (int i = 0; i < interfaceTypes.length; i++) {
				HiClass intf = interfaceTypes[i].getClass(ctx);
				if (intf != null) {
					valid &= intf.validate(validationInfo, ctx);
					interfaces[i] = intf;
				}
			}
		}

		// init before enter
		ctx.clazz = this;
		init(ctx);

		ctx.enter(RuntimeContext.STATIC_CLASS, this);
		valid &= HiNode.validateAnnotations(validationInfo, ctx, annotations);

		if (superClassType != null && superClass == null && !name.equals(OBJECT_CLASS_NAME)) {
			superClass = superClassType.getClass(ctx);
			if (superClass != null) {
				valid &= superClass.validate(validationInfo, ctx);
			} else {
				valid = false;
			}
		}
		if (superClass != null) {
			if (!isInterface && superClass.isInterface && !isAnonymous()) {
				validationInfo.error("cannot extends interface", token);
				valid = false;
			} else if (superClass.modifiers.isFinal()) {
				validationInfo.error("cannot extends final class", token);
				valid = false;
			}
		}
		if ((isAbstract() || isInterface) && modifiers.isFinal()) {
			validationInfo.error("abstract class cannot be final", token);
			valid = false;
		}

		// check modifiers
		if (ctx.enclosingClass != null && isStatic()) {
			validationInfo.error("illegal modifier for the local class " + fullName + "; only abstract or final is permitted", token);
			valid = false;
		}

		if (initializers != null) {
			for (int i = 0; i < initializers.length; i++) {
				HiNode initializer = (HiNode) initializers[i];
				boolean isField = initializer instanceof HiField;
				if (!isField) {
					ctx.enter(RuntimeContext.INITIALIZATION, initializer);
				}
				valid &= initializer.validate(validationInfo, ctx);
				if (!isField) {
					ctx.exit();
				}

				if (isField) {
					if (initializer instanceof HiFieldVar) {
						HiFieldVar varField = (HiFieldVar) initializer;
						if (varField.type != Type.varType) {
							HiField typedField = HiFieldVar.getField(varField.getClass(ctx), varField.name, varField.initializer, varField.token);
							initializer = typedField;
							initializers[i] = typedField;
							ctx.level.addField(typedField);
						}
					}

					HiField field = (HiField) initializer;
					if (field.getModifiers().isFinal() && field.initializer == null) {
						// TODO check initialization in all constructors
						validationInfo.error("variable '" + field.name + "' might not have been initialized", field.getToken());
						valid = false;
					}
					ctx.initializedNodes.add(field);
				}
			}
		}

		if (innerClasses != null) {
			boolean isStaticRootClassTop = isStaticRootClassTop();
			for (HiClass innerClass : innerClasses) {
				if (!isStaticRootClassTop) {
					if (innerClass.isInterface) {
						validationInfo.error("the member interface " + innerClass.fullName + " can only be defined inside a top-level class or interface", innerClass.token);
						isStaticRootClassTop();
						valid = false;
					}

					// check on valid static modifier (includes annotations)
					if (innerClass.isStatic()) {
						validationInfo.error("the member type " + innerClass.fullName + " cannot be declared static; static types can only be declared in static or top level types", innerClass.token);
						valid = false;
					}
				}
				valid &= innerClass.validate(validationInfo, ctx);
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

		if (!isInterface && !isAbstract() && !isEnum() && !isRecord()) {
			Map<MethodSignature, HiMethod> abstractMethods = new HashMap<>();
			getAbstractMethods(validationInfo, ctx, abstractMethods, new HashMap<>(), new HashSet<>());
			if (abstractMethods.size() > 0) {
				for (HiMethod abstractMethod : abstractMethods.values()) {
					validationInfo.error("abstract method " + abstractMethod.signature + " is implemented in non-abstract class", token);
				}
				valid = false;
			}
		}
		ctx.exit();

		ctx.addLocalClass(this);
		ctx.clazz = outboundClass;
		return valid;
	}

	private void getAbstractMethods(ValidationInfo validationInfo, CompileClassContext ctx, Map<MethodSignature, HiMethod> abstractMethods, Map<MethodSignature, HiMethod> implementedMethods, Set<HiClass> processedClasses) {
		if (processedClasses.contains(this)) {
			return;
		}
		processedClasses.add(this);

		if (methods != null) {
			for (HiMethod method : methods) {
				if (!method.modifiers.isStatic()) {
					method.resolve(ctx);
					if (method.modifiers.isAbstract()) {
						if (!implementedMethods.containsKey(method.signature)) {
							abstractMethods.put(method.signature, method);
						}
					} else {
						implementedMethods.put(method.signature, method);
						abstractMethods.remove(method.signature);
					}
				}
			}
		}
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				HiClass intf = interfaces[i];
				if (intf != null) {
					intf.getAbstractMethods(validationInfo, ctx, abstractMethods, implementedMethods, processedClasses);
				}
			}
		}
		if (superClass != null) {
			superClass.getAbstractMethods(validationInfo, ctx, abstractMethods, implementedMethods, processedClasses);
		}
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

	public HiClass getArrayClassIf(int dimensions) {
		HiClass c = this;
		for (int i = 0; i < dimensions; i++) {
			c = c.getArrayClass();
		}
		return c;
	}

	//	private Map<String, HiClass> classesMap;

	// TODO optimize
	public HiClass getClass(ClassResolver classResolver, String name) {
		//		if (classesMap != null && classesMap.containsKey(name)) {
		//			return classesMap.get(name);
		//		}

		HiClass clazz = _getClass(classResolver, name);
		if (clazz != null) {
			//			if (classesMap == null) {
			//				classesMap = new HashMap<>(1);
			//			}
			//			classesMap.put(name, clazz);
		}
		return clazz;
	}

	private HiClass _getClass(ClassResolver classResolver, String name) {
		// inner classes
		HiClass innerClass = getInnerClass(classResolver, name);
		if (innerClass != null) {
			return innerClass;
		}

		HiClass clazz = this;
		while (clazz != null) {
			// check local enclosing classes
			HiClass localClass = classResolver.getLocalClass(clazz, name);
			if (localClass != null) {
				return localClass;
			}

			// check enclosing classes
			localClass = clazz.getInnerClass(classResolver, name);
			if (localClass != null) {
				return localClass;
			}

			clazz = clazz.enclosingClass;
		}

		// registered classes
		return forName(classResolver, name);
	}

	public HiClass getInnerClass(ClassResolver classResolver, String name) {
		if (innerClasses != null) {
			for (HiClass c : innerClasses) {
				if (c.name.equals(name) || c.fullName.equals(name)) {
					c.init(classResolver);
					return c;
				}
			}
		}
		if (superClass != null) {
			HiClass innerClass = superClass.getInnerClass(classResolver, name);
			if (innerClass != null) {
				return innerClass;
			}
		}
		if (interfaces != null) {
			for (HiClass i : interfaces) {
				HiClass innerClass = i.getInnerClass(classResolver, name);
				if (innerClass != null) {
					return innerClass;
				}
			}
		}
		return null;
	}

	public boolean isInstanceofAny(HiClass[] classes) {
		if (classes != null) {
			for (HiClass clazz : classes) {
				if (isInstanceof(clazz)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isInstanceofAny(ClassResolver classResolver, Type[] types) {
		if (types != null) {
			for (Type type : types) {
				HiClass clazz = type.getClass(classResolver);
				if (isInstanceof(clazz)) {
					return true;
				}
			}
		}
		return false;
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

	protected Map<String, HiField<?>> fieldsMap;

	public HiField<?> getField(ClassResolver classResolver, String name) {
		if (fieldsMap != null && fieldsMap.containsKey(name)) {
			return fieldsMap.get(name);
		}

		HiField<?> field = _searchField(classResolver, name);
		if (field != null) {
			if (fieldsMap == null) {
				fieldsMap = new ConcurrentHashMap<>();
			}
			fieldsMap.put(name, field);
		}
		return field;
	}

	protected HiField<?> _searchField(ClassResolver classResolver, String name) {
		init(classResolver);
		HiField<?> field = null;

		// this fields
		if (fields != null && fields.length > 0) {
			for (HiField<?> f : fields) {
				if (f.name.equals(name)) {
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

		// enclosing fields
		if (field == null && enclosingClass != null) {
			field = enclosingClass.getField(classResolver, name);
		}
		return field;
	}

	private final Map<MethodSignature, HiMethod> methodsHash = new HashMap<>();

	public HiMethod searchMethod(ClassResolver classResolver, String name, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set(name, argTypes);
		return searchMethod(classResolver, signature);
	}

	public HiMethod searchMethod(ClassResolver classResolver, MethodSignature signature) {
		HiMethod method = methodsHash.get(signature);
		if (method == null) {
			method = _searchMethod(classResolver, signature);
			if (method != null) {
				methodsHash.put(new MethodSignature(signature), method);
			}
		}
		return method;
	}

	protected HiMethod _searchMethod(ClassResolver classResolver, MethodSignature signature) {
		String name = signature.name;
		HiClass[] argTypes = signature.argClasses;

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
								if (!HiClass.autoCast(argTypes[i], m.argClasses[i], false)) {
									break FOR;
								}
							}
							HiClass varargsType = m.argClasses[mainArgCount].getArrayType();
							for (int i = mainArgCount; i < argTypes.length; i++) {
								if (!HiClass.autoCast(argTypes[i], varargsType, false)) {
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
								if (!HiClass.autoCast(argTypes[i], m.argClasses[i], false)) {
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
			HiMethod m = superClass.searchMethod(classResolver, signature);
			if (m != null) {
				return m;
			}
		}

		if (interfaces != null) {
			HiClass id = null;
			HiMethod md = null;
			HiMethod m = null;
			for (HiClass i : interfaces) {
				HiMethod _m = i.searchMethod(classResolver, signature);
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
			HiMethod m = enclosingClass.searchMethod(classResolver, signature);
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
		return getMethod(classResolver, signature);
	}

	public HiMethod getMethod(ClassResolver classResolver, MethodSignature signature) {
		HiMethod method = methodsHash.get(signature);
		if (method == null) {
			method = _getMethod(classResolver, signature);
			if (method != null) {
				methodsHash.put(new MethodSignature(signature), method);
			}
		}
		return method;
	}

	private HiMethod _getMethod(ClassResolver classResolver, MethodSignature signature) {
		String name = signature.name;
		HiClass[] argTypes = signature.argClasses;

		// this methods
		if (methods != null) {
			for (HiMethod m : methods)
				FOR:{
					if (m.name.equals(name)) {
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
			HiMethod m = superClass.getMethod(classResolver, signature);
			if (m != null) {
				return m;
			}
		}

		// enclosing methods
		if (!isTopLevel()) {
			HiMethod m = enclosingClass.getMethod(classResolver, signature);
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
		signature.set(HiConstructor.METHOD_NAME, argTypes);
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
		if (constructor.hasVarargs()) {
			int mainArgCount = constructor.arguments.length - 1;
			if (mainArgCount > argTypes.length) {
				return false;
			}

			constructor.resolve(classResolver);

			for (int i = 0; i < mainArgCount; i++) {
				if (!HiClass.autoCast(argTypes[i], constructor.argClasses[i], false)) {
					return false;
				}
			}
			HiClass varargsType = constructor.argClasses[mainArgCount].getArrayType();
			for (int i = mainArgCount; i < argTypes.length; i++) {
				if (!HiClass.autoCast(argTypes[i], varargsType, false)) {
					return false;
				}
			}
		} else {
			int argCount = constructor.arguments != null ? constructor.arguments.length : 0;
			if (argCount != (argTypes != null ? argTypes.length : 0)) {
				return false;
			}

			constructor.resolve(classResolver);
			for (int i = 0; i < argCount; i++) {
				if (!HiClass.autoCast(argTypes[i], constructor.argClasses[i], false)) {
					return false;
				}
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

	public boolean isIntNumber() {
		return false;
	}

	public boolean isArray() {
		return false;
	}

	public boolean isAnnotation() {
		return false;
	}

	public boolean isAnonymous() {
		return type == CLASS_TYPE_ANONYMOUS; // or name.equals("")
	}

	public boolean isLocal() {
		return type == CLASS_TYPE_LOCAL;
	}

	public boolean isInner() {
		return type == CLASS_TYPE_INNER;
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

	public boolean isConstant() {
		return isPrimitive() || isEnum() || fullName.equals(STRING_CLASS_NAME) || (isArray() && ((HiClassArray) this).cellClass.isConstant());
	}

	public static HiClass forName(ClassResolver classResolver, String name) {
		HiClassLoader classLoader = classResolver.getClassLoader();
		HiClass clazz = classLoader.getClass(name);
		if (clazz != null) {
			clazz.init(classResolver);
		}
		return clazz;
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

		os.writeShort(innerClasses != null ? innerClasses.length : 0);
		if (innerClasses != null) {
			for (int i = 0; i < innerClasses.length; i++) {
				os.writeClass(innerClasses[i]);
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
		throw new HiScriptRuntimeException("unknown class type: " + classType);
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
			} catch (HiNoClassException exc) {
				initClass = false;
				os.addClassLoadListener(new ClassLoadListener() {
					@Override
					public void classLoaded(HiClass clazz) {
						classAccess[0].init(os.getClassLoader(), null, clazz, classAccess[0].name, classAccess[0].type);
					}
				}, exc.getIndex());
			}
		}

		String name = os.readUTF();
		int type = os.readByte();

		HiClass clazz;
		if (classType == CLASS_ENUM) {
			clazz = new HiClassEnum(os.getClassLoader(), name, type);
		} else if (classType == CLASS_RECORD) {
			clazz = new HiClassRecord(os.getClassLoader(), name, type, null);
		} else {
			clazz = new HiClass(superClassType, name, type);
		}
		clazz.token = token;
		classAccess[0] = clazz;
		if (initClass) {
			clazz.init(os.getClassLoader(), null, outerClass, name, type);
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

		clazz.innerClasses = new HiClass[os.readShort()];
		for (int i = 0; i < clazz.innerClasses.length; i++) {
			final int index = i;
			try {
				clazz.innerClasses[i] = os.readClass();
			} catch (HiNoClassException exc) {
				os.addClassLoadListener(new ClassLoadListener() {
					@Override
					public void classLoaded(HiClass clazz) {
						classAccess[0].innerClasses[index] = clazz;
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

	//	public boolean hasOutboundObject() {
	//		return !isStatic() && !isTopLevel();
	//	}

	public boolean isStatic() {
		if (isTopLevel()) {
			return true;
		} else {
			return (modifiers != null && modifiers.isStatic()) && isStaticRootClassTop();
		}
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
		if (c == this || c == null) {
			return this;
		}
		if (isPrimitive() || c.isPrimitive()) {
			if (isNumber() && c.isNumber()) {
				if (this == HiClassPrimitive.DOUBLE || c == HiClassPrimitive.DOUBLE) {
					return HiClassPrimitive.DOUBLE;
				} else if (this == HiClassPrimitive.FLOAT || c == HiClassPrimitive.FLOAT) {
					return HiClassPrimitive.DOUBLE;
				} else if (this == HiClassPrimitive.LONG || c == HiClassPrimitive.LONG) {
					return HiClassPrimitive.LONG;
				} else if (this == HiClassPrimitive.INT || c == HiClassPrimitive.INT) {
					return HiClassPrimitive.INT;
				} else if (this == HiClassPrimitive.CHAR || c == HiClassPrimitive.CHAR) {
					return HiClassPrimitive.INT;
				} else if (this == HiClassPrimitive.SHORT || c == HiClassPrimitive.SHORT) {
					return HiClassPrimitive.SHORT;
				}
			}
			return null;
		}
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

	public static boolean autoCast(HiClass src, HiClass dst, boolean isValue) {
		if (src == dst) {
			return true;
		}

		if (src == null || dst == null) {
			return false;
		}

		if (src.isPrimitive() || dst.isPrimitive()) {
			if (src.isPrimitive() && dst.isPrimitive()) {
				if (isValue) {
					return HiFieldPrimitive.autoCastValue(src, dst);
				} else {
					return HiFieldPrimitive.autoCast(src, dst);
				}
			} else {
				return false;
			}
		}

		if (src.isNull()) {
			return true;
		}

		if (dst == HiClass.OBJECT_CLASS) {
			return true;
		}

		if (src.isArray() || dst.isArray()) {
			if (!src.isArray() || !dst.isArray()) {
				return false;
			}

			HiClassArray arraySrc = (HiClassArray) src;
			HiClassArray arrayDst = (HiClassArray) dst;
			if (arraySrc.dimension != arrayDst.dimension) {
				return false;
			}

			if (arraySrc.cellClass.isPrimitive()) {
				return arraySrc.cellClass == arrayDst.cellClass;
			} else {
				return autoCast(arraySrc.cellClass, arrayDst.cellClass, false);
			}
		}
		return src.isInstanceof(dst);
	}

	public boolean hasInnerClass(HiClass clazz) {
		if (innerClasses != null) {
			for (HiClass innerClass : innerClasses) {
				if (innerClass == clazz) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isRootClassTop() {
		HiClass rootClass = getRootClass();
		return rootClass.isTopLevel() || rootClass.name.equals(ROOT_CLASS_NAME);
	}

	public HiClass getRootClass() {
		HiClass clazz = this;
		while (clazz.enclosingClass != null) {
			if (!clazz.enclosingClass.hasInnerClass(clazz)) {
				break;
			}
			clazz = clazz.enclosingClass;
		}
		return clazz;
	}

	public boolean isStaticRootClassTop() {
		HiClass rootClass = getStaticRootClass();
		return rootClass != null && rootClass.isTopLevel();
	}

	public HiClass getStaticRootClass() {
		HiClass clazz = this;
		while (clazz.enclosingClass != null) {
			if (!clazz.enclosingClass.hasInnerClass(clazz) || (!clazz.enclosingClass.isStatic() && !clazz.enclosingClass.isTopLevel())) {
				break;
			}
			clazz = clazz.enclosingClass;
		}
		return clazz.isStatic() || clazz.isTopLevel() ? clazz : null;
	}
}
