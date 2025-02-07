package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiConstructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.classes.HiClassAnnotation;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassMix;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.classes.HiClassRecord;
import ru.nest.hiscript.ool.model.classes.HiClassVar;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldVar;
import ru.nest.hiscript.ool.model.lib.ObjectImpl;
import ru.nest.hiscript.ool.model.lib.SystemImpl;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.HasModifiers;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeNull;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HiClass implements HiNodeIF, HiType, HasModifiers {
	public final static int CLASS_OBJECT = 0;

	public final static int CLASS_PRIMITIVE = 1;

	public final static int CLASS_ARRAY = 2;

	public final static int CLASS_ENUM = 3;

	public final static int CLASS_RECORD = 4;

	public final static int CLASS_ANNOTATION = 5;

	public final static int CLASS_NULL = 6;

	public final static int CLASS_VAR = 7;

	public final static int CLASS_MIX = 8;

	public final static int CLASS_GENERIC = 9;

	public final static int CLASS_SYSTEM = 10; // used only for serializations

	public final static int CLASS_TYPE_TOP = 0; // No outbound class

	public final static int CLASS_TYPE_INNER = 1; // static (NESTED) and not

	// static (INNER)
	public final static int CLASS_TYPE_LOCAL = 2; // in method, constructor

	public final static int CLASS_TYPE_ANONYMOUS = 3; // like new Object() {...}

	public static HiClass OBJECT_CLASS;

	public static HiClass NUMBER_CLASS;

	public static HiClass STRING_CLASS;

	public static HiClass MOCK_CLASS = new HiClass(); // used in validations for invalid class names

	public static String ROOT_CLASS_NAME = "@root";

	public static String ROOT_CLASS_NAME_PREFIX = ROOT_CLASS_NAME + "$";

	public static String OBJECT_CLASS_NAME = "Object";

	public static String STRING_CLASS_NAME = "String";

	public static String ENUM_CLASS_NAME = "Enum";

	public static String RECORD_CLASS_NAME = "Record";

	public static String ITERATOR_CLASS_NAME = "Iterator";

	public static String HASHMAP_CLASS_NAME = "HashMap";

	public static String ARRAYLIST_CLASS_NAME = "ArrayList";

	public static String EXCEPTION_CLASS_NAME = "Exception";

	public static String AUTOCLOSEABLE_CLASS_NAME = "AutoCloseable";

	public static String RUNTIME_EXCEPTION_CLASS_NAME = "RuntimeException";

	public final static HiClassLoader systemClassLoader = new HiClassLoader("system");

	public static HiClassLoader userClassLoader = new HiClassLoader("user", systemClassLoader);

	public static void setUserClassLoader(HiClassLoader classLoader) {
		systemClassLoader.removeClassLoader(userClassLoader);
		systemClassLoader.addClassLoader(classLoader);
		userClassLoader = classLoader;
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
			HiConstructor emptyConstructor = new HiConstructor(OBJECT_CLASS, Type.objectType, null, new Modifiers(), null, (NodeArgument[]) null, null, null, null, BodyConstructorType.NONE);
			OBJECT_CLASS.constructors = new HiConstructor[] {emptyConstructor};
			classes.add(OBJECT_CLASS);

			classes.add(STRING_CLASS = systemClassLoader.load(HiCompiler.class.getResource("/hilibs/String.hi"), false).get(0));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Class.hi"), false));

			// TODO define classes initialization order automatically
			classes.add(NUMBER_CLASS = systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Number.hi"), false).get(0));
			HiClassPrimitive.BYTE.setAutoboxClass(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Byte.hi"), false).get(0));
			HiClassPrimitive.SHORT.setAutoboxClass(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Short.hi"), false).get(0));
			HiClassPrimitive.INT.setAutoboxClass(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Integer.hi"), false).get(0));
			HiClassPrimitive.LONG.setAutoboxClass(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Long.hi"), false).get(0));
			HiClassPrimitive.FLOAT.setAutoboxClass(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Float.hi"), false).get(0));
			HiClassPrimitive.DOUBLE.setAutoboxClass(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Double.hi"), false).get(0));
			HiClassPrimitive.BOOLEAN.setAutoboxClass(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Boolean.hi"), false).get(0));
			HiClassPrimitive.CHAR.setAutoboxClass(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Character.hi"), false).get(0));
			HiClassPrimitive.VOID.setAutoboxClass(HiClassPrimitive.VOID); // to avoid NPE
			for (HiClassPrimitive primitiveClass : HiClassPrimitive.primitiveClasses.values()) {
				if (primitiveClass != HiClassPrimitive.VOID) {
					classes.add(primitiveClass.getAutoboxClass());
					primitiveClass.getAutoboxClass().autoboxedPrimitiveClass = primitiveClass;
				}
			}

			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Enum.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Record.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/AutoCloseable.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/System.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Math.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Exception.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/RuntimeException.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/AssertException.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Iterator.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/ArrayList.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/HashMap.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Thread.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Java.hi"), false));

			HiCompiler compiler = new HiCompiler(systemClassLoader, null);
			ValidationInfo validationInfo = new ValidationInfo(compiler);
			for (HiClass clazz : classes) {
				CompileClassContext ctx = new CompileClassContext(compiler, null, null, HiClass.CLASS_TYPE_TOP);
				clazz.validate(validationInfo, ctx);
			}
			validationInfo.throwExceptionIf();
		} catch (IOException | TokenizerException | HiScriptParseException | HiScriptValidationException exc) {
			exc.printStackTrace();
		}
	}

	// used to resolve super class in runtime after all classes will be loaded
	public Type superClassType;

	// used to resolve interfaces in runtime after all classes will be loaded
	public Type[] interfaceTypes;

	// main class properties
	public Modifiers modifiers;

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	public HiClass superClass;

	public HiClass[] interfaces;

	public String name;

	public NodeGenerics generics;

	public HiClass[] typeParameters;

	public String fullName;

	public int hashCode;

	public int type;

	public HiClass enclosingClass;

	public boolean isInterface = false;

	// content
	public NodeAnnotation[] annotations;

	public HiField<?>[] fields;

	public NodeInitializer[] initializers;

	public HiConstructor[] constructors;

	public HiMethod[] methods;

	public HiMethod functionalMethod;

	public HiClass[] innerClasses;

	public Boolean valid;

	public Token token;

	public HiClassLoader classLoader;

	// for mix class
	protected HiClass() {
	}

	// for ClassPrimitive, ClassArray and ClassNull
	public HiClass(HiClassLoader classLoader, HiClass superClass, HiClass enclosingClass, String name, int type, ClassResolver classResolver) {
		this.superClass = superClass;
		if (superClass != null) {
			this.superClassType = Type.getType(superClass);
		}
		init(classLoader, classResolver, enclosingClass, name, null, type);
	}

	// for ClassParseRule and NewParseRule
	public HiClass(HiClassLoader classLoader, Type superClassType, HiClass enclosingClass, Type[] interfaceTypes, String name, NodeGenerics generics, int type, ClassResolver classResolver) {
		this.superClassType = superClassType;
		this.interfaceTypes = interfaceTypes;
		init(classLoader, classResolver, enclosingClass, name, generics, type);
	}

	// for decode
	protected HiClass(Type superClassType, String name, NodeGenerics generics, int type) {
		this.superClassType = superClassType;
		this.name = name.intern();
		this.type = type;
		this.generics = generics;
		// init(...) is in decode
	}

	private void init(HiClassLoader classLoader, ClassResolver classResolver, HiClass enclosingClass, String name, NodeGenerics generics, int type) {
		this.enclosingClass = enclosingClass;
		this.type = type;

		// try resolve super class if needed
		boolean isRuntime = classResolver instanceof RuntimeContext;
		if (superClass == null && superClassType != null && isRuntime) {
			superClass = superClassType.getClass(classResolver);
		}

		// intern name to optimize via a == b
		this.name = (name != null ? name : "").intern();
		this.fullName = getFullName(classLoader);
		this.hashCode = fullName.hashCode();
		this.generics = generics;

		if (classLoader == null) {
			classLoader = userClassLoader;
		}
		classLoader.addClass(this, isRuntime);
	}

	public String getFullName(HiClassLoader classLoader) {
		if (this.fullName == null) {
			if (enclosingClass != null && !isEnum()) {
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
			this.hashCode = fullName.hashCode();
		}
		return this.fullName;
	}

	@Override
	public String getTypeName() {
		return name;
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
					classResolver.processResolverException("static class " + getNameDescr() + " can not extend not static and not top level class");
					return;
				}

				// check super class on final
				if (superClass.isFinal()) {
					classResolver.processResolverException("the type " + getNameDescr() + " cannot subclass the final class " + superClass.getNameDescr());
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
				for (int i = 0; i < interfaces.length; i++) {
					HiClass classInterface = interfaces[i];
					if (classInterface == null) {
						// not found
						continue;
					}

					// init interface
					classInterface.init(classResolver);
				}
			}

			// set super class to Object by default
			if (superClass == null && this != OBJECT_CLASS && !isPrimitive() && !isNull() && !isInterface) {
				superClass = OBJECT_CLASS;
				superClassType = Type.objectType;
			}

			// init children classes
			if (innerClasses != null) {
				for (int i = 0; i < innerClasses.length; i++) {
					innerClasses[i].init(classResolver);
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
				for (int i = 0; i < interfaces.length; i++) {
					interfaces[i].initStaticInitializers(classResolver);
				}
			}

			RuntimeContext ctx = (RuntimeContext) classResolver;
			ctx.enterInitialization(this, null, null);
			try {
				if (initializers != null) {
					int size = initializers.length;
					for (int i = 0; i < size; i++) {
						NodeInitializer initializer = initializers[i];
						if (initializer.isStatic() && initializer instanceof HiField) {
							HiField<?> field = (HiField<?>) initializer;
							field.declared = true;
							field.initialized = true;
						}
					}
					for (int i = 0; i < size; i++) {
						NodeInitializer initializer = initializers[i];
						if (initializer.isStatic()) {
							if (initializer instanceof HiField) {
								((HiField) initializer).initialized = false;
							}
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
				ctx.throwRuntimeException("cannot initialize class " + getNameDescr() + ": " + exc.getMessage());
			} finally {
				ctx.exit();
			}
		}
	}

	public HiClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (valid == null) {
			valid = true;
			valid = _validate(validationInfo, ctx);
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// not supported
	}

	protected boolean _validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass outboundClass = ctx.clazz;
		boolean valid = true;

		if (isInner() || isLocal()) {
			HiClass classSameName = ctx.getUniqueClass(name, this);
			if (classSameName != null && classSameName != this) {
				validationInfo.error("duplicate class: " + name, token);
				valid = false;
			}
		}

		// resolve interfaces before set ctx.clazz, as interfaces has to be initialized outsize of this class context
		if (interfaces == null && interfaceTypes != null) {
			interfaces = new HiClass[interfaceTypes.length];

			Map<MethodSignature, HiMethod> defaultMethods = null;
			for (int i = 0; i < interfaceTypes.length; i++) {
				HiClass intf = interfaceTypes[i].getClass(ctx);
				if (intf != null) {
					if (!intf.isInterface) {
						validationInfo.error("interface expected", token);
						valid = false;
					}
					valid &= intf.validate(validationInfo, ctx);
					interfaces[i] = intf;
					if (interfaceTypes.length > 1 && intf.methods != null) {
						for (int j = 0; j < intf.methods.length; j++) {
							HiMethod method = intf.methods[j];
							if (method.isDefault()) {
								if (defaultMethods == null) {
									defaultMethods = new HashMap<>();
								} else {
									HiMethod existingMethod = defaultMethods.get(method.signature);
									if (existingMethod != null) {
										HiMethod executeMethod = searchMethod(ctx, method.signature);
										if (executeMethod == null || defaultMethods.containsValue(executeMethod)) {
											validationInfo.error(getNameDescr() + " inherits unrelated defaults for " + method + " from types " + existingMethod.clazz.getNameDescr() + " and " + intf.getNameDescr(), token);
											valid = false;
										} else {
											continue;
										}
									}
								}
								defaultMethods.put(method.signature, method);
							}
						}
					}
				}
			}
		}

		// init before enter
		ctx.clazz = this;
		init(ctx);

		// @generics (after init)
		if (generics != null) {
			if (generics.generics.length == 0) {
				validationInfo.error("type parameter expected", generics.getToken());
				valid = false;
			} else {
				valid &= generics.validate(validationInfo, ctx);
			}
			for (int i = 0; i < generics.generics.length; i++) {
				NodeGeneric generic = generics.generics[i];
				if (generic.isWildcard()) {
					validationInfo.error("unexpected wildcard", generic.getToken());
					valid = false;
				} else if (generic.isSuper) {
					validationInfo.error("super is unsupported", generic.getToken());
					valid = false;
				}
			}
		}

		// type generics (after generics)
		if (superClassType != null && superClassType.parameters != null) {
			typeParameters = new HiClass[superClassType.parameters.length];
			for (int i = 0; i < typeParameters.length; i++) {
				Type parameterType = superClassType.parameters[i];
				HiClass parameterClass = parameterType.getClass(ctx);
				if (parameterClass == null) {
					parameterClass = OBJECT_CLASS;
				}
				typeParameters[i] = parameterClass;
			}

			// @generics
			// TODO use type (superClassType.parameters[i]) token
			if (superClass.generics != null) {
				if (superClass.generics.generics.length == typeParameters.length) {
					for (int i = 0; i < typeParameters.length; i++) {
						HiClass parameterClass = typeParameters[i];
						HiClassGeneric definedClass = superClass.generics.generics[i].clazz;
						if (!parameterClass.isInstanceof(definedClass.clazz)) {
							validationInfo.error("type parameter '" + parameterClass.getNameDescr() + "' is not within its bound; should extend '" + definedClass.clazz.getNameDescr() + "'", getToken());
							valid = false;
						}
					}
				} else {
					validationInfo.error("wrong number of type arguments: " + typeParameters.length + "; required: " + superClass.generics.generics.length, getToken());
					valid = false;
				}
			} else {
				validationInfo.error("type '" + superClass.getNameDescr() + "' does not have type parameters", getToken());
				valid = false;
			}
		}

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

		// check modifiers
		if (superClass != null) {
			if (!isInterface && superClass.isInterface && !isAnonymous()) {
				validationInfo.error("cannot extends interface", token);
				valid = false;
			} else if (superClass.isPrimitive()) {
				validationInfo.error("can not extends primitive class", token);
				valid = false;
			} else if (superClass.modifiers.isFinal()) {
				validationInfo.error("cannot extends final class", token);
				valid = false;
			}
		}
		if (modifiers != null) {
			if ((isAbstract() || isInterface) && modifiers.isFinal()) {
				validationInfo.error("abstract class cannot be final", token);
				valid = false;
			}
			if (enclosingClass != null && isLocal() && !isTopLevel() && modifiers.isStatic()) {
				validationInfo.error("modifier 'static' not allowed in local classes", token);
				valid = false;
			}
			if (isInner() && isObject() && !isEnum() && !isRecord() && !isInterface && modifiers.isStatic() && !isStaticRootClassTop()) {
				validationInfo.error("static declarations in inner classes are not supported", token);
				valid = false;
			}
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
					if (field.isFinal() && field.initializer == null) {
						// TODO check initialization in all constructors
						validationInfo.error("variable '" + field.name + "' might not have been initialized", field.getToken());
						valid = false;
					}
					ctx.initializedNodes.add(field);
				} else if (isInterface) {
					validationInfo.error("interface cannot have initializers", initializer.getToken());
					valid = false;
				}
			}
		}

		if (innerClasses != null) {
			boolean isStaticRootClassTop = isStaticRootClassTop();
			for (int i = 0; i < innerClasses.length; i++) {
				HiClass innerClass = innerClasses[i];
				valid &= innerClass.validate(validationInfo, ctx);
				if (!isStaticRootClassTop) {
					if (innerClass.isInterface) {
						validationInfo.error("the member interface " + innerClass.getNameDescr() + " can only be defined inside a top-level class or interface", innerClass.token);
						isStaticRootClassTop();
						valid = false;
					}

					// check on valid static modifier (includes annotations)
					if (innerClass.isStatic()) {
						validationInfo.error("the member type " + innerClass.getNameDescr() + " cannot be declared static; static types can only be declared in static or top level types", innerClass.token);
						valid = false;
					}
				}
			}
		}

		if (constructors != null) {
			for (int i = 0; i < constructors.length; i++) {
				HiConstructor constructor = constructors[i];
				valid &= constructor.validate(validationInfo, ctx);
				if (isInterface) {
					validationInfo.error("interface cannot have constructors", constructor.getToken());
					valid = false;
				}
			}
		}

		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				valid &= methods[i].validate(validationInfo, ctx);
			}
		}

		if (!isInterface && !isAbstract() && !isEnum() && !isRecord()) {
			Map<MethodSignature, HiMethod> abstractMethods = new HashMap<>();
			getAbstractMethods(ctx, abstractMethods, new HashMap<>(), new HashSet<>());
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

	public int getAbstractMethodsCount(ClassResolver classResolver) {
		Map<MethodSignature, HiMethod> abstractMethods = new HashMap<>();
		getAbstractMethods(classResolver, abstractMethods, new HashMap<>(), new HashSet<>());
		return abstractMethods.size();
	}

	public List<HiMethod> getAbstractMethods(ClassResolver classResolver) {
		Map<MethodSignature, HiMethod> abstractMethods = new HashMap<>();
		getAbstractMethods(classResolver, abstractMethods, new HashMap<>(), new HashSet<>());
		return new ArrayList<>(abstractMethods.values());
	}

	protected void getAbstractMethods(ClassResolver classResolver, Map<MethodSignature, HiMethod> abstractMethods, Map<MethodSignature, HiMethod> implementedMethods, Set<HiClass> processedClasses) {
		if (processedClasses.contains(this)) {
			return;
		}
		processedClasses.add(this);

		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				HiMethod method = methods[i];
				if (!method.isStatic()) {
					method.resolve(classResolver);
					if (method.isAbstract()) {
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
					intf.getAbstractMethods(classResolver, abstractMethods, implementedMethods, processedClasses);
				}
			}
		}
		if (superClass != null) {
			superClass.getAbstractMethods(classResolver, abstractMethods, implementedMethods, processedClasses);
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
		HiClass innerClass = getInnerClass(classResolver, name, true);
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
			localClass = clazz.getInnerClass(classResolver, name, true);
			if (localClass != null) {
				return localClass;
			}

			clazz = clazz.enclosingClass;
		}

		HiClassGeneric genericClass = getGenericClass(classResolver, name);
		if (genericClass != null) {
			return genericClass;
		}

		// registered classes
		return forName(classResolver, name);
	}

	// @generics
	public HiClassGeneric getGenericClass(ClassResolver classResolver, String name) {
		if (generics != null) {
			return generics.getGenericClass(classResolver, name);
		}
		return null;
	}

	public HiClass getInnerClass(ClassResolver classResolver, String name, boolean checkInheritance) {
		if (innerClasses != null) {
			for (int i = 0; i < innerClasses.length; i++) {
				HiClass innerClass = innerClasses[i];
				if (innerClass.name.equals(name) || innerClass.fullName.equals(name)) {
					innerClass.init(classResolver);
					return innerClass;
				}
			}
		}
		if (checkInheritance) {
			if (superClass != null) {
				HiClass innerClass = superClass.getInnerClass(classResolver, name, true);
				if (innerClass != null) {
					return innerClass;
				}
			}
			if (interfaces != null) {
				for (int i = 0; i < interfaces.length; i++) {
					HiClass classInterface = interfaces[i];
					if (classInterface == null) {
						continue;
					}
					HiClass innerClass = interfaces[i].getInnerClass(classResolver, name, true);
					if (innerClass != null) {
						return innerClass;
					}
				}
			}
		}
		return null;
	}

	public boolean isInstanceofAny(HiClass[] classes) {
		if (classes != null) {
			for (int i = 0; i < classes.length; i++) {
				if (isInstanceof(classes[i])) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isInstanceofAny(ClassResolver classResolver, Type[] types) {
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				HiClass clazz = types[i].getClass(classResolver);
				if (isInstanceof(clazz)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isInstanceof(HiClass clazz) {
		if (this == clazz || clazz == HiClass.OBJECT_CLASS) {
			return true;
		}
		if (clazz.isMix()) {
			HiClassMix mixClass = (HiClassMix) clazz;
			HiClass[] classes = mixClass.classes;
			for (int i = 0; i < classes.length; i++) {
				if (isInstanceof(classes[i])) {
					return true;
				}
			}
		} else if (clazz.isGeneric()) {
			// generic
			HiClassGeneric genericClass = (HiClassGeneric) clazz;
			return genericClass.isInstanceof(this);
		} else {
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
			for (int i = 0; i < interfaces.length; i++) {
				HiClass classInterface = interfaces[i];
				if (classInterface == superInterface) {
					return true;
				}
				if (classInterface.hasInterfaceInstanceof(superInterface)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasInterfaceInstanceof(String superInterfaceName) {
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				HiClass classInterface = interfaces[i];
				if (classInterface.fullName.equals(superInterfaceName)) {
					return true;
				}
				if (classInterface.hasInterfaceInstanceof(superInterfaceName)) {
					return true;
				}
			}
		}
		return false;
	}

	protected Map<String, HiField<?>> localFieldsMap;

	protected Map<String, HiField<?>> fieldsMap;

	public HiField<?> getField(ClassResolver classResolver, String name) {
		return getField(classResolver, name, false);
	}

	public HiField<?> getField(ClassResolver classResolver, String name, boolean local) {
		Map<String, HiField<?>> fieldsMap = local ? this.localFieldsMap : this.fieldsMap;
		if (fieldsMap != null && fieldsMap.containsKey(name)) {
			return fieldsMap.get(name);
		}

		HiField<?> field = _searchField(classResolver, name, local);
		if (field != null) {
			if (local) {
				if (this.localFieldsMap == null) {
					this.localFieldsMap = new ConcurrentHashMap<>();
					this.localFieldsMap.put(name, field);
				}
			} else {
				if (this.fieldsMap == null) {
					this.fieldsMap = new ConcurrentHashMap<>();
					this.fieldsMap.put(name, field);
				}
			}
		}
		return field;
	}

	protected HiField<?> _searchField(ClassResolver classResolver, String name, boolean local) {
		init(classResolver);

		// this fields
		if (fields != null && fields.length > 0) {
			for (int i = 0; i < fields.length; i++) {
				HiField<?> field = fields[i];
				if (field.name.equals(name)) {
					return field;
				}
			}
		}

		// interfaces static fields
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				HiField<?> field = interfaces[i].getField(classResolver, name, local);
				if (field != null) {
					return field;
				}
			}
		}

		// super fields
		if (superClass != null) {
			HiField<?> field = superClass.getField(classResolver, name, local);
			if (field != null) {
				return field;
			}
		}

		// enclosing fields
		if (!local && enclosingClass != null) {
			HiField<?> field = enclosingClass.getField(classResolver, name, local);
			if (field != null) {
				return field;
			}
		}
		return null;
	}

	private final Map<MethodSignature, List<HiMethod>> methodsHash = new HashMap<>();

	public HiMethod searchMethod(ClassResolver classResolver, String name, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set(name, argTypes, false);
		return searchMethod(classResolver, signature);
	}

	public HiMethod searchMethod(ClassResolver classResolver, MethodSignature signature) {
		List<HiMethod> methods = methodsHash.get(signature);
		if (methods == null) {
			methods = _searchMethods(classResolver, signature);
			if (methods != null) {
				if (methods.size() > 1) {
					HiMethod m1 = methods.get(0);
					HiMethod m2 = methods.get(1);
					classResolver.processResolverException("Ambiguous method call. Both " + m1 + " in " + m1.clazz.getNameDescr() + " and " + m2 + " in " + m2.clazz.getNameDescr() + " match.");
				}
				methodsHash.put(new MethodSignature(signature), methods);
			}
		}
		return methods != null && methods.size() > 0 ? methods.get(0) : null;
	}

	protected List<HiMethod> searchMethods(ClassResolver classResolver, MethodSignature signature) {
		List<HiMethod> methods = methodsHash.get(signature);
		if (methods == null) {
			methods = _searchMethods(classResolver, signature);
			if (methods != null) {
				if (methods.size() > 1) {
					HiMethod m1 = methods.get(0);
					HiMethod m2 = methods.get(1);
					classResolver.processResolverException("Ambiguous method call. Both " + m1 + " in " + m1.clazz.getNameDescr() + " and " + m2 + " in " + m2.clazz.getNameDescr() + " match.");
				}
				methodsHash.put(new MethodSignature(signature), methods);
			}
		}
		return methods;
	}

	private List<HiMethod> addFoundMethod(HiMethod method, MethodSignature searchSignature, List<HiMethod> foundMethods, boolean mayRewite) {
		if (foundMethods != null) {
			for (int i = foundMethods.size() - 1; i >= 0; i--) {
				HiMethod foundMethod = foundMethods.get(i);
				if (foundMethod.isAbstract() != method.isAbstract()) {
					if (foundMethod.isAbstract()) {
						foundMethods.remove(i);
					} else {
						return foundMethods;
					}
				} else {
					ArgClassPriorityType argPriority = foundMethod.signature.getArgsPriority(method.signature, searchSignature);
					if (argPriority == ArgClassPriorityType.higher) {
						return foundMethods;
					} else if (argPriority == ArgClassPriorityType.lower) {
						foundMethods.remove(i);
					} else if (argPriority == ArgClassPriorityType.equals && mayRewite) {
						return foundMethods;
					}
				}
			}
		} else {
			foundMethods = new ArrayList<>(1);
		}
		foundMethods.add(method);
		return foundMethods;
	}

	protected List<HiMethod> _searchMethods(ClassResolver classResolver, MethodSignature signature) {
		String name = signature.name;
		HiClass[] argTypes = signature.argClasses;
		List<HiMethod> matchedMethods = null;

		// functional method
		if (functionalMethod != null) {
			if (isMatchMethodArguments(classResolver, functionalMethod, argTypes)) {
				matchedMethods = addFoundMethod(functionalMethod, signature, matchedMethods, false);
			}
		}

		// current class methods
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				HiMethod method = methods[i];
				if (method.name.equals(name) || method.isLambda() || signature.isLambda()) {
					if (isMatchMethodArguments(classResolver, method, argTypes)) {
						matchedMethods = addFoundMethod(method, signature, matchedMethods, false);
					}
				}
			}
		}

		// super methods
		if (superClass != null) {
			List<HiMethod> superMethods = superClass.searchMethods(classResolver, signature);
			if (superMethods != null) {
				for (int i = 0; i < superMethods.size(); i++) {
					matchedMethods = addFoundMethod(superMethods.get(i), signature, matchedMethods, true);
				}
			}
		}

		if (matchedMethods != null) {
			if (!matchedMethods.get(0).isAbstract()) {
				return matchedMethods;
			}
		}

		// interfaces methods
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				List<HiMethod> interfaceMethods = interfaces[i].searchMethods(classResolver, signature);
				if (interfaceMethods != null) {
					for (int j = 0; j < interfaceMethods.size(); j++) {
						matchedMethods = addFoundMethod(interfaceMethods.get(j), signature, matchedMethods, true);
					}
				}
			}
		}
		if (matchedMethods != null) {
			return matchedMethods;
		}

		// enclosing methods
		if (!isTopLevel()) {
			List<HiMethod> enclosingMethods = enclosingClass.searchMethods(classResolver, signature);
			if (enclosingMethods != null) {
				for (int i = 0; i < enclosingMethods.size(); i++) {
					matchedMethods = addFoundMethod(enclosingMethods.get(i), signature, matchedMethods, false);
				}
			}
		}
		return matchedMethods;
	}

	public HiMethod getInterfaceAbstractMethod(ClassResolver classResolver, HiClass[] argTypes) {
		if (methods != null && methods.length > 0) {
			for (MatchMethodArgumentsType matchType : MatchMethodArgumentsType.values()) {
				for (int i = 0; i < methods.length; i++) {
					HiMethod m = methods[i];
					if (m.isAbstract() && isMatchMethodDefinition(classResolver, m, argTypes, matchType)) {
						return m;
					}
				}
			}
		}
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				HiMethod m = interfaces[i].getInterfaceAbstractMethod(classResolver, argTypes);
				if (m != null) {
					return m;
				}
			}
		}
		return null;
	}

	public enum MatchMethodArgumentsType {
		strict(false, false, false), autobox(false, false, true), noCast(false, true, true), noVarargs(true, false, true), soft(true, true, true);

		private final boolean isCast;

		private final boolean isVarargs;

		private final boolean isAutobox;

		MatchMethodArgumentsType(boolean isCast, boolean isVarargs, boolean isAutobox) {
			this.isCast = isCast;
			this.isVarargs = isVarargs;
			this.isAutobox = isAutobox;
		}

		public boolean isCast() {
			return isCast;
		}

		public boolean isVarargs() {
			return isVarargs;
		}

		public boolean isAutobox() {
			return isAutobox;
		}
	}

	private boolean isMatchMethodArguments(ClassResolver classResolver, HiMethod method, HiClass[] argTypes) {
		if (method.hasVarargs()) {
			int mainArgCount = method.argCount - 1;
			if (!isMatchMethodArgumentsPartially(classResolver, method, argTypes, mainArgCount)) {
				return false;
			}

			HiClass varargsType = method.argClasses[mainArgCount].getArrayType();
			NodeArgument vararg = method.arguments[mainArgCount];
			if (argTypes.length == method.argCount && argTypes[mainArgCount].getArrayDimension() == varargsType.getArrayDimension() + 1) {
				HiClass argType = argTypes[mainArgCount].getArrayType();
				if (!HiClass.autoCast(classResolver, argType, varargsType, false, true)) {
					return false;
				}
				argType.applyLambdaImplementedMethod(classResolver, varargsType, vararg);
			} else {
				for (int i = mainArgCount; i < argTypes.length; i++) {
					if (!HiClass.autoCast(classResolver, argTypes[i], varargsType, false, true)) {
						return false;
					}
				}
				for (int i = mainArgCount; i < argTypes.length; i++) {
					argTypes[i].applyLambdaImplementedMethod(classResolver, varargsType, vararg);
				}
			}
		} else {
			int argCount = method.argCount;
			if (argCount != argTypes.length) {
				return false;
			}
			if (!isMatchMethodArgumentsPartially(classResolver, method, argTypes, argCount)) {
				return false;
			}
		}
		return true;
	}

	private boolean isMatchMethodArgumentsPartially(ClassResolver classResolver, HiMethod method, HiClass[] argTypes, int argCount) {
		if (argCount > argTypes.length) {
			return false;
		}

		method.resolve(classResolver);

		for (int i = 0; i < argCount; i++) {
			if (!HiClass.autoCast(classResolver, argTypes[i], method.argClasses[i], false, true)) {
				return false;
			}
		}
		for (int i = 0; i < argCount; i++) {
			argTypes[i].applyLambdaImplementedMethod(classResolver, method.argClasses[i], method.arguments[i]);
		}
		return true;
	}

	private boolean isMatchMethodDefinition(ClassResolver classResolver, HiMethod method, HiClass[] argTypes, MatchMethodArgumentsType matchType) {
		if (matchType.isVarargs() && method.hasVarargs()) {
			int mainArgCount = method.argCount - 1;
			if (mainArgCount > argTypes.length) {
				return false;
			}

			method.resolve(classResolver);

			for (int i = 0; i < mainArgCount; i++) {
				if (!HiClass.autoCast(classResolver, argTypes[i], method.argClasses[i], false, matchType.isAutobox())) {
					return false;
				}
			}
			for (int i = 0; i < mainArgCount; i++) {
				argTypes[i].applyLambdaImplementedMethod(classResolver, method.argClasses[i], method.arguments[i]);
			}

			HiClass varargsType = method.argClasses[mainArgCount].getArrayType();
			NodeArgument vararg = method.arguments[mainArgCount];
			if (argTypes.length == method.argCount && argTypes[mainArgCount].getArrayDimension() == varargsType.getArrayDimension() + 1) {
				HiClass argType = argTypes[mainArgCount].getArrayType();
				if (!HiClass.autoCast(classResolver, varargsType, argType, false, matchType.isAutobox())) {
					return false;
				}
				argType.applyLambdaImplementedMethod(classResolver, varargsType, vararg);
			} else {
				for (int i = mainArgCount; i < argTypes.length; i++) {
					if (!HiClass.autoCast(classResolver, varargsType, argTypes[i], false, matchType.isAutobox())) {
						return false;
					}
				}
				for (int i = mainArgCount; i < argTypes.length; i++) {
					argTypes[i].applyLambdaImplementedMethod(classResolver, varargsType, vararg);
				}
			}
		} else {
			int argCount = method.argCount;
			if (argCount != argTypes.length) {
				return false;
			}

			method.resolve(classResolver);

			for (int i = 0; i < argCount; i++) {
				if (!HiClass.autoCast(classResolver, method.argClasses[i], argTypes[i], false, matchType.isAutobox())) {
					return false;
				}
			}
			for (int i = 0; i < argCount; i++) {
				argTypes[i].applyLambdaImplementedMethod(classResolver, method.argClasses[i], method.arguments[i]);
			}
		}
		return true;
	}

	public List<HiMethod> searchMethodsByName(ClassResolver classResolver, String name) {
		Map<MethodSignature, HiMethod> foundMethods = new HashMap<>(1);
		searchMethodsByName(classResolver, name, foundMethods);
		return new ArrayList<>(foundMethods.values());
	}

	private void searchMethodsByName(ClassResolver classResolver, String name, Map<MethodSignature, HiMethod> foundMethods) {
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				HiMethod method = methods[i];
				if (method.name.equals(name)) {
					method.resolve(classResolver);
					foundMethods.putIfAbsent(method.signature, method);
				}
			}
		}
		if (superClass != null) {
			superClass.searchMethodsByName(classResolver, name, foundMethods);
		}
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				interfaces[i].searchMethodsByName(classResolver, name, foundMethods);
			}
		}
	}

	public HiMethod getMethod(ClassResolver classResolver, String name, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set(name, argTypes, false);
		return getMethod(classResolver, signature);
	}

	private HiMethod getMethod(ClassResolver classResolver, MethodSignature signature) {
		List<HiMethod> methods = methodsHash.get(signature);
		if (methods == null) {
			HiMethod method = _getMethod(classResolver, signature);
			if (method != null) {
				methodsHash.put(new MethodSignature(signature), Collections.singletonList(method));
			}
			return method;
		}
		return methods.get(0);
	}

	private HiMethod _getMethod(ClassResolver classResolver, MethodSignature signature) {
		String name = signature.name;
		HiClass[] argTypes = signature.argClasses;

		if (functionalMethod != null) {
			int argCount = functionalMethod.argCount;
			if (argCount == argTypes.length) {
				functionalMethod.resolve(classResolver);
				boolean match = true;
				for (int i = 0; i < argCount; i++) {
					if (argTypes[i] != functionalMethod.argClasses[i]) {
						match = false;
						break;
					}
				}
				if (match) {
					return functionalMethod;
				}
			}
		}

		// this methods
		if (methods != null) {
			for (int i = 0; i < methods.length; i++)
				FOR:{
					HiMethod method = methods[i];
					if (method.name.equals(name)) {
						int argCount = method.argCount;
						if (argCount != argTypes.length) {
							continue;
						}

						method.resolve(classResolver);

						for (int j = 0; j < argCount; j++) {
							if (argTypes[j] != method.argClasses[j]) {
								break FOR;
							}
						}
						return method;
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

	private final Map<MethodSignature, HiConstructor> constructorsHash = new HashMap<>();

	public HiConstructor searchConstructor(ClassResolver classResolver, HiClass... argTypes) {
		MethodSignature signature = new MethodSignature();
		signature.set(HiConstructor.METHOD_NAME, argTypes, false);
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
			for (MatchMethodArgumentsType matchType : MatchMethodArgumentsType.values()) {
				for (int i = 0; i < constructors.length; i++) {
					HiConstructor constructor = constructors[i];
					if (matchConstructor(classResolver, constructor, argTypes, matchType)) {
						return constructor;
					}
				}
			}
		}
		return null;
	}

	protected boolean matchConstructor(ClassResolver classResolver, HiConstructor constructor, HiClass[] argTypes, MatchMethodArgumentsType matchType) {
		if (matchType.isVarargs && constructor.hasVarargs()) {
			int mainArgCount = constructor.arguments.length - 1;
			if (mainArgCount > argTypes.length) {
				return false;
			}

			constructor.resolve(classResolver);

			for (int i = 0; i < mainArgCount; i++) {
				if (!HiClass.autoCast(classResolver, argTypes[i], constructor.argClasses[i], false, matchType.isAutobox())) {
					return false;
				}
			}
			HiClass varargsType = constructor.argClasses[mainArgCount].getArrayType();
			NodeArgument vararg = constructor.arguments[mainArgCount];
			for (int i = mainArgCount; i < argTypes.length; i++) {
				if (!HiClass.autoCast(classResolver, argTypes[i], varargsType, false, matchType.isAutobox())) {
					return false;
				}
			}

			for (int i = 0; i < mainArgCount; i++) {
				argTypes[i].applyLambdaImplementedMethod(classResolver, constructor.argClasses[i], constructor.arguments[i]);
			}
			for (int i = mainArgCount; i < argTypes.length; i++) {
				argTypes[i].applyLambdaImplementedMethod(classResolver, varargsType, vararg);
			}
		} else {
			int argCount = constructor.arguments != null ? constructor.arguments.length : 0;
			if (argCount != (argTypes != null ? argTypes.length : 0)) {
				return false;
			}

			constructor.resolve(classResolver);
			for (int i = 0; i < argCount; i++) {
				if (!HiClass.autoCast(classResolver, argTypes[i], constructor.argClasses[i], false, matchType.isAutobox())) {
					return false;
				}
			}
			for (int i = 0; i < argCount; i++) {
				argTypes[i].applyLambdaImplementedMethod(classResolver, constructor.argClasses[i], constructor.arguments[i]);
			}
		}
		return true;
	}

	public HiConstructor getConstructor(ClassResolver classResolver, HiClass... argTypes) {
		if (constructors != null) {
			int size = constructors.length;
			for (int i = 0; i < size; i++)
				FOR:{
					HiConstructor c = constructors[i];
					int argCount = c.arguments != null ? c.arguments.length : 0;
					if (argCount != argTypes.length) {
						continue;
					}

					c.resolve(classResolver);
					for (int j = 0; j < argCount; j++) {
						if (argTypes[j] != c.argClasses[j]) {
							break FOR;
						}
					}
					return c;
				}
		}
		return null;
	}

	// generic
	public HiClass resolveGenericClass(ClassResolver classResolver, Type classType, HiClassGeneric genericClass) {
		HiClass srcClass = genericClass.sourceClass;
		HiClass enclosingClass = this;
		if (enclosingClass.isGeneric()) {
			HiClassGeneric genericEnclosingClass = (HiClassGeneric) enclosingClass;
			if (genericClass.sourceClass == genericEnclosingClass.clazz) {
				return genericEnclosingClass.parametersClasses[genericClass.index];
			} else {
				enclosingClass = ((HiClassGeneric) enclosingClass).clazz;
			}
		}
		if (enclosingClass == srcClass || genericClass.sourceType != NodeGeneric.GenericSourceType.classSource) {
			return genericClass;
		}
		if (classType != null && classType.parameters != null) {
			Type resolvedType = classType.getParameterType(genericClass);
			return resolvedType.getClass(classResolver);
		}

		HiClass upperClass = enclosingClass;
		if (upperClass.superClass != null && upperClass.superClass != srcClass) {
			upperClass = upperClass.superClass;
		}
		if (upperClass != null && upperClass.superClassType != null && upperClass.superClassType.parameters != null) {
			Type definedGenericType = upperClass.superClassType.parameters[genericClass.index];
			HiClassGeneric rewrittenGenericClass = upperClass.getGenericClass(classResolver, definedGenericType.name);
			if (rewrittenGenericClass != null) {
				if (rewrittenGenericClass.parametersClasses.length == 0) {
					return rewrittenGenericClass.clazz;
				} else {
					return rewrittenGenericClass;
				}
			} else {
				return definedGenericType.getClass(classResolver);
			}
		}
		return genericClass;
	}

	public boolean isPrimitive() {
		return false;
	}

	public int getPrimitiveType() {
		throw new HiScriptRuntimeException("unknown type: " + name);
	}

	public HiClass getAutoboxClass() {
		return null;
	}

	public HiClassPrimitive autoboxedPrimitiveClass;

	public HiClassPrimitive getAutoboxedPrimitiveClass() {
		return autoboxedPrimitiveClass;
	}

	public HiClass boxed() {
		return getAutoboxClass() != null ? getAutoboxClass() : this;
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

	public boolean isVar() {
		return false;
	}

	public boolean isMix() {
		return false;
	}

	public boolean isGeneric() {
		return false;
	}

	public HiClass getArrayType() {
		return null;
	}

	public int getArrayDimension() {
		return 0;
	}

	public boolean isConstant() {
		return isPrimitive() || isEnum() || isStringClass() || (isArray() && ((HiClassArray) this).cellClass.isConstant());
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
		} else if (clazz == String.class) {
			cellType = HiClass.STRING_CLASS;
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
		if (systemClassLoader.getClass(fullName) != null) {
			os.writeByte(CLASS_SYSTEM);
			os.writeUTF(fullName);
		} else {
			code(os, CLASS_OBJECT);
		}
	}

	public void code(CodeContext os, int classType) throws IOException {
		// write class type
		os.writeByte(classType);
		os.writeToken(token);
		os.writeUTF(name);

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

		os.writeNullable(generics);
		os.writeByte(type);
		os.writeUTF(fullName);

		os.writeNullable(modifiers);
		os.writeTypes(interfaceTypes);
		os.writeShortArray(constructors);
		if (isLambda()) {
			os.write(functionalMethod);
		} else {
			os.writeBoolean(isInterface);
			os.writeShortArray(annotations);

			os.writeShort(fields != null ? fields.length : 0);
			os.writeShortArray(initializers); // contains ordered fields and blocks
			os.writeShortArray(methods);
			os.writeClasses(innerClasses);
		}
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		int classIndex = os.readShort(); // written in CodeContext.getClassContext(int index)
		int classType = os.readByte();
		switch (classType) {
			case CLASS_SYSTEM:
				return decodeSystem(os);
			case CLASS_OBJECT:
				return decodeObject(os, classType, classIndex);
			case CLASS_PRIMITIVE:
				return HiClassPrimitive.decode(os);
			case CLASS_ARRAY:
				return HiClassArray.decode(os);
			case CLASS_ENUM:
				return HiClassEnum.decode(os, classIndex);
			case CLASS_RECORD:
				return HiClassRecord.decode(os, classIndex);
			case CLASS_ANNOTATION:
				return HiClassAnnotation.decode(os, classIndex);
			case CLASS_NULL:
				return HiClassNull.decode(os);
			case CLASS_VAR:
				return HiClassVar.decode(os);
			case CLASS_GENERIC:
				return HiClassGeneric.decode(os, classIndex);
		}
		throw new HiScriptRuntimeException("unknown class type: " + classType);
	}

	public static HiClass decodeSystem(DecodeContext os) throws IOException {
		return os.getClassLoader().getClass(os.readUTF());
	}

	public static HiClass decodeObject(DecodeContext os, int classType, int classIndex) throws IOException {
		final HiClass[] classAccess = new HiClass[1];
		Token token = os.readToken();
		String name = os.readUTF();

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
				os.addClassLoadListener(clazz -> classAccess[0].init(os.getClassLoader(), null, clazz, classAccess[0].name, classAccess[0].generics, classAccess[0].type), exc.getIndex());
			}
		}

		NodeGenerics generics = os.readNullable(NodeGenerics.class);
		int type = os.readByte();

		HiClass clazz;
		if (classType == CLASS_ENUM) {
			clazz = new HiClassEnum(name, type);
		} else if (classType == CLASS_RECORD) {
			clazz = new HiClassRecord(name, generics, type);
		} else if (classType == CLASS_ANNOTATION) {
			clazz = new HiClassAnnotation(name, type);
		} else if (classType == CLASS_GENERIC) {
			clazz = new HiClassGeneric(name, type);
		} else {
			clazz = new HiClass(superClassType, name, generics, type);
		}
		clazz.fullName = os.readUTF();
		clazz.token = token;
		classAccess[0] = clazz;
		if (initClass) {
			clazz.init(os.getClassLoader(), null, outerClass, name, generics, type);
		}

		HiClass oldClass = os.getHiClass();
		os.setHiClass(clazz);

		// content
		clazz.modifiers = os.readNullable(Modifiers.class);
		clazz.interfaceTypes = os.readTypes();
		clazz.constructors = os.readShortArray(HiConstructor.class);
		if (clazz.isLambda()) {
			clazz.isInterface = false;
			clazz.functionalMethod = os.read(HiMethod.class);
		} else {
			clazz.isInterface = os.readBoolean();
			clazz.annotations = os.readShortNodeArray(NodeAnnotation.class);

			int fieldsCount = os.readShort();
			clazz.initializers = os.readShortNodeArray(NodeInitializer.class); // contains ordered fields and blocks
			if (fieldsCount > 0) {
				clazz.fields = new HiField[fieldsCount];
				int fieldIndex = 0;
				for (int i = 0; i < clazz.initializers.length; i++) {
					NodeInitializer initializer = clazz.initializers[i];
					if (initializer instanceof HiField) {
						clazz.fields[fieldIndex++] = (HiField) initializer;
					}
				}
			}

			clazz.methods = os.readShortArray(HiMethod.class);
			clazz.innerClasses = os.readClasses();
		}
		os.setHiClass(oldClass);

		// try resolve super class
		if (superClassType != null) {
			clazz.superClass = os.getClassLoader().getClass(superClassType.fullName);
			// clazz.superClass = superClassType.getClass(/*no context*/ null);
		}
		os.fireClassLoaded(clazz, classIndex);
		return clazz;
	}

	@Override
	public int hashCode() {
		return hashCode;
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
		return enclosingClass == null || enclosingClass.name.equals(ROOT_CLASS_NAME);
	}

	public Class getJavaClass() {
		switch (fullName) {
			case "String":
				return String.class;
			case "HashMap":
				return HashMap.class;
			case "ArrayList":
				return ArrayList.class;
			case "Iterator":
				return Iterator.class;
		}
		return null;
	}

	public HiClass getCommonClass(HiClass c) {
		if (c == this || c == null) {
			return this;
		}
		if (isPrimitive() || c.isPrimitive()) {
			// autobox
			// TODO autobox int => Integer?
			HiClass c1 = this;
			HiClass c2 = c;
			if (!c1.isPrimitive()) {
				if (c1.getAutoboxedPrimitiveClass() != null) {
					c1 = c1.getAutoboxedPrimitiveClass();
				}
			} else if (!c2.isPrimitive()) {
				if (c2.getAutoboxedPrimitiveClass() != null) {
					c2 = c2.getAutoboxedPrimitiveClass();
				}
			}
			if (c1.isNumber() && c2.isNumber()) {
				if (c1 == HiClassPrimitive.DOUBLE || c2 == HiClassPrimitive.DOUBLE) {
					return HiClassPrimitive.DOUBLE;
				} else if (c1 == HiClassPrimitive.FLOAT || c2 == HiClassPrimitive.FLOAT) {
					return HiClassPrimitive.DOUBLE;
				} else if (c1 == HiClassPrimitive.LONG || c2 == HiClassPrimitive.LONG) {
					return HiClassPrimitive.LONG;
				} else if (c1 == HiClassPrimitive.INT || c2 == HiClassPrimitive.INT) {
					return HiClassPrimitive.INT;
				} else if (c1 == HiClassPrimitive.CHAR || c2 == HiClassPrimitive.CHAR) {
					return HiClassPrimitive.INT;
				} else if (c1 == HiClassPrimitive.SHORT || c2 == HiClassPrimitive.SHORT) {
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

	@Override
	public void setToken(Token token) {
		this.token = token;
	}

	public static boolean validateCastArray(ValidationInfo validationInfo, CompileClassContext ctx, HiNodeIF node, HiClassArray src, HiClassArray dst) {
		if (src == dst) {
			return true;
		}
		if (src.getRootCellClass().isPrimitive() || dst.getRootCellClass().isPrimitive()) {
			if (src.getRootCellClass() != dst.getRootCellClass() || src.dimension != dst.dimension) {
				validationInfo.error("incompatible types: " + src.getNameDescr() + " cannot be converted to " + dst.getNameDescr(), node.getToken());
				return false;
			}
			return true;
		}
		if (dst.getRootCellClass() == HiClass.OBJECT_CLASS && dst.dimension <= src.dimension) {
			return true;
		}
		if (!autoCast(ctx, src.getRootCellClass(), dst.getRootCellClass(), false, true)) {
			validationInfo.error("incompatible types: " + src.getNameDescr() + " cannot be converted to " + dst.getNameDescr(), node.getToken());
			return false;
		}

		boolean valid = true;
		node = node.getExpressionSingleNode();
		if (node instanceof NodeArrayValue) {
			NodeArrayValue nodeArrayValue = (NodeArrayValue) node;
			HiClass dstCellClass = dst.cellClass;
			for (int i = 0; i < nodeArrayValue.array.length; i++) {
				HiNode cellValue = nodeArrayValue.array[i];
				cellValue = cellValue.getExpressionSingleNode();
				HiClass srcCellClass = cellValue.getValueClass(validationInfo, ctx);
				if (cellValue instanceof NodeArrayValue) {
					if (srcCellClass.isArray() && dstCellClass.isArray()) {
						valid &= validateCastArray(validationInfo, ctx, cellValue, (HiClassArray) srcCellClass, (HiClassArray) dstCellClass);
					} else {
						valid &= autoCast(ctx, srcCellClass, dstCellClass, false, true);
					}
				} else if (cellValue instanceof NodeArray) {
					valid &= autoCast(ctx, srcCellClass, dstCellClass, false, true);
				} else if (cellValue != NodeNull.instance) {
					if (!autoCast(ctx, srcCellClass, dstCellClass, false, true)) {
						validationInfo.error("incompatible types: " + cellValue.getValueClass(validationInfo, ctx).getNameDescr() + " cannot be converted to " + dst.cellClass.getNameDescr(), cellValue.getToken());
						valid = false;
					}
				}
			}
		}
		return valid;
	}

	public static boolean autoCast(ClassResolver classResolver, HiClass src, HiClass dst, boolean isValue, boolean isAutobox) {
		if (src == dst) {
			return true;
		}

		if (src == null || dst == null) {
			return false;
		}

		if (src.isVar() || dst.isVar()) {
			return true;
		}

		// autobox
		if (isAutobox && src != HiClassPrimitive.VOID && dst == HiClass.OBJECT_CLASS) {
			return true;
		}

		if (src.isPrimitive() || dst.isPrimitive()) {
			// autobox
			// generic
			if (isAutobox) {
				if (!src.isPrimitive()) {
					if (src.getAutoboxedPrimitiveClass() != null) {
						src = src.getAutoboxedPrimitiveClass();
					} else if (src.isGeneric()) {
						HiClassGeneric genericSrc = (HiClassGeneric) src;
						return dst.getAutoboxClass() == genericSrc.clazz;
					}
				} else if (!dst.isPrimitive()) {
					if (dst.getAutoboxedPrimitiveClass() != null) {
						if (isValue) {
							dst = dst.getAutoboxedPrimitiveClass();
						} else if (src.isPrimitive()) {
							return src == dst.getAutoboxedPrimitiveClass();
						} else {
							return src == dst;
						}
					} else if (dst == HiClass.NUMBER_CLASS) {
						return src.isNumber();
					} else if (dst.isGeneric()) {
						HiClassGeneric genericDst = (HiClassGeneric) dst;
						if (genericDst.isSuper) {
							return genericDst.clazz.isInstanceof(src.getAutoboxClass());
						} else {
							return src.getAutoboxClass().isInstanceof(genericDst.clazz);
						}
					}
				}
			}

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
			if (!arraySrc.getRootCellClass().isPrimitive() && arrayDst.getRootCellClass() == HiClass.OBJECT_CLASS) {
				return true;
			}
			if (arraySrc.dimension != arrayDst.dimension) {
				return false;
			}

			if (arraySrc.cellClass.isPrimitive()) {
				return arraySrc.getRootCellClass() == arrayDst.getRootCellClass();
			} else {
				return autoCast(classResolver, arraySrc.cellClass, arrayDst.cellClass, false, isAutobox);
			}
		}
		if (src.getLambdaImplementedMethod(classResolver, dst) != null) {
			return true;
		}

		// generic
		if (dst.isGeneric() && !src.isGeneric()) {
			HiClassGeneric genericDst = (HiClassGeneric) dst;
			return src.isInstanceof(genericDst.clazz);
		} else {
			return src.isInstanceof(dst);
		}
	}

	public boolean hasInnerClass(HiClass clazz) {
		if (innerClasses != null) {
			for (int i = 0; i < innerClasses.length; i++) {
				if (clazz == innerClasses[i]) {
					return true;
				}
			}
		}
		return false;
	}

	private byte isStaticRootClassTop = -1;

	public boolean isStaticRootClassTop() {
		if (isStaticRootClassTop != -1) {
			return isStaticRootClassTop == 1 ? true : false;
		}
		HiClass rootClass = getStaticRootClass();
		if (rootClass != null && rootClass.isTopLevel()) {
			isStaticRootClassTop = 1;
			return true;
		} else {
			isStaticRootClassTop = 0;
			return false;
		}
	}

	public HiClass getStaticRootClass() {
		HiClass clazz = this;
		while (clazz.enclosingClass != null) {
			if (!clazz.enclosingClass.hasInnerClass(clazz) || (!clazz.enclosingClass.isStatic() && !clazz.enclosingClass.isTopLevel())) {
				break;
			}
			clazz = clazz.enclosingClass;
		}
		return clazz.modifiers != null && clazz.modifiers.isStatic() || clazz.isTopLevel() ? clazz : null;
	}

	public boolean isLambda() {
		return name.startsWith(HiMethod.LAMBDA_METHOD_NAME);
	}

	public HiMethod getLambdaMethod() {
		if (isLambda()) {
			if (functionalMethod != null) {
				return functionalMethod;
			} else if (methods != null && methods.length > 0) {
				// TODO remove
				return methods[0];
			}
		}
		return null;
	}

	public HiMethod getLambdaImplementedMethod(ClassResolver classResolver, HiClass dst) {
		if (isLambda() && dst.isInterface) {
			int methodsCount = dst.getAbstractMethodsCount(classResolver);
			if (methodsCount == 1) {
				HiMethod lambdaMethod = getLambdaMethod();
				if (lambdaMethod != null) {
					lambdaMethod.resolve(classResolver);
					return dst.searchMethod(classResolver, lambdaMethod.signature);
				}
			}
		}
		return null;
	}

	public void applyLambdaImplementedMethod(ClassResolver classResolver, HiClass variableClass, NodeArgument variableNode) {
		HiMethod lambdaMethod = getLambdaMethod();
		if (lambdaMethod != null) {
			lambdaMethod.applyLambdaImplementedMethod(classResolver, variableClass, variableNode);
		}
	}

	public String getNameDescr() {
		String fullName = isArray() ? getArrayType().fullName : this.fullName;
		String descr = fullName;
		int index = 0;
		if (fullName.startsWith(ROOT_CLASS_NAME_PREFIX)) {
			index = ROOT_CLASS_NAME_PREFIX.length();
		}
		while (index < descr.length() && Character.isDigit(descr.charAt(index))) {
			index++;
		}
		if (index > 0) {
			descr = descr.substring(index);
		}
		for (int i = 0; i < getArrayDimension(); i++) {
			descr += "[]";
		}
		return descr;
	}

	/**
	 * It is assumed this class and clazz are matched to argClass
	 */
	enum ArgClassPriorityType {
		higher, lower, equals, nonComparable
	}

	public ArgClassPriorityType getArgPriority(HiClass clazz, HiClass argClass) {
		HiClass c1 = this;
		HiClass c2 = clazz;
		if (c1 == c2) {
			return ArgClassPriorityType.equals;
		}
		if (argClass.isPrimitive() || argClass.getAutoboxedPrimitiveClass() != null) {
			HiClassPrimitive pc1 = c1.isPrimitive() ? (HiClassPrimitive) c1 : c1.getAutoboxedPrimitiveClass();
			HiClassPrimitive pc2 = c2.isPrimitive() ? (HiClassPrimitive) c2 : c2.getAutoboxedPrimitiveClass();
			if (pc1 == pc2) {
				return ArgClassPriorityType.equals;
			}
		}
		if (c1 == argClass) {
			return ArgClassPriorityType.higher;
		} else if (c2 == argClass) {
			return ArgClassPriorityType.lower;
		}

		if (argClass.isPrimitive()) {
			if (c1.isPrimitive() && c2.isPrimitive()) {
				boolean isIntArg = argClass.isIntNumber();
				boolean isInt1 = c1.isIntNumber();
				boolean isInt2 = c2.isIntNumber();
				if (isInt1 != isInt2) {
					if (isInt1 == isIntArg) {
						return ArgClassPriorityType.higher;
					} else {
						return ArgClassPriorityType.lower;
					}
				}
			} else if (c1.isPrimitive() || c1.getAutoboxedPrimitiveClass() == argClass) {
				return ArgClassPriorityType.higher;
			} else if (c2.isPrimitive() || c2.getAutoboxedPrimitiveClass() == argClass) {
				return ArgClassPriorityType.lower;
			}
		} else {
			if (c1.isPrimitive()) {
				c1 = c1.getAutoboxClass();
			}
			if (c2.isPrimitive()) {
				c2 = c2.getAutoboxClass();
			}
			if (c1 == argClass) {
				return ArgClassPriorityType.higher;
			} else if (c2 == argClass) {
				return ArgClassPriorityType.lower;
			}
		}

		// object - object
		if (c1.isInstanceof(c2)) {
			return ArgClassPriorityType.higher;
		}
		if (c2.isInstanceof(c1)) {
			return ArgClassPriorityType.lower;
		}
		return ArgClassPriorityType.nonComparable;
	}

	public boolean isStringClass() {
		return STRING_CLASS_NAME.equals(fullName);
	}
}
