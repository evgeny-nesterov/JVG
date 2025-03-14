package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.HiCompiler;
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
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.ool.runtime.HiScriptRuntimeException;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import static ru.nest.hiscript.ool.model.PrimitiveType.CHAR_TYPE;
import static ru.nest.hiscript.ool.model.nodes.NodeVariable.UNNAMED;

public class HiClass implements HiNodeIF, HiType, HasModifiers {
	public static HiClass OBJECT_CLASS;

	public static HiClass NUMBER_CLASS;

	public static HiClass STRING_CLASS;

	public static HiClass EXCEPTION_CLASS;

	public static HiClass MOCK_CLASS = new HiClass(); // used in validations for invalid class names

	public static String ROOT_CLASS_NAME = "@root";

	public static String ROOT_CLASS_NAME_PREFIX = ROOT_CLASS_NAME + "$";

	public static String OBJECT_CLASS_NAME = "Object";

	public static String STRING_CLASS_NAME = "String";

	public static String NUMBER_CLASS_NAME = "Number";

	public static String ENUM_CLASS_NAME = "Enum";

	public static String RECORD_CLASS_NAME = "Record";

	public static String ITERATOR_CLASS_NAME = "Iterator";

	public static String HASHMAP_CLASS_NAME = "HashMap";

	public static String ARRAYLIST_CLASS_NAME = "ArrayList";

	public static String EXCEPTION_CLASS_NAME = "Exception";

	public static String AUTOCLOSEABLE_CLASS_NAME = "AutoCloseable";

	public static String RUNTIME_EXCEPTION_CLASS_NAME = "RuntimeException";

	static {
		// system class loader is common and has to have null parent
		HiClassLoader systemClassLoader = null;
		boolean loadSystem = true;
		if (!HiCompiler.compilingSystem) {
			InputStream is = HiClass.class.getResourceAsStream("/hilibs/bin/system.hilib");
			if (is != null) {
				try (DataInputStream dis = new DataInputStream(new GZIPInputStream(is))) {
					systemClassLoader = HiClassLoader.decodeSystem(dis);
					loadSystem = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (systemClassLoader == null) {
			systemClassLoader = HiClassLoader.createSystem();
		}
		systemClassLoader.getNative().register(SystemImpl.class);
		systemClassLoader.getNative().register(ObjectImpl.class);
		if (loadSystem) {
			loadSystemClasses(systemClassLoader);
		} else {
			initSystemClasses(systemClassLoader);
		}
	}

	public static HiClassLoader getSystemClassLoader() {
		return HiClassLoader.getSystemClassLoader();
	}

	private static void initSystemClasses(HiClassLoader systemClassLoader) {
		OBJECT_CLASS = systemClassLoader.getClass(OBJECT_CLASS_NAME);
		STRING_CLASS = systemClassLoader.getClass(STRING_CLASS_NAME);
		NUMBER_CLASS = systemClassLoader.getClass(NUMBER_CLASS_NAME);
		EXCEPTION_CLASS = systemClassLoader.getClass(EXCEPTION_CLASS_NAME);
		HiClassPrimitive.BYTE.setAutoboxingClass(systemClassLoader.getClass("Byte"));
		HiClassPrimitive.SHORT.setAutoboxingClass(systemClassLoader.getClass("Short"));
		HiClassPrimitive.INT.setAutoboxingClass(systemClassLoader.getClass("Integer"));
		HiClassPrimitive.LONG.setAutoboxingClass(systemClassLoader.getClass("Long"));
		HiClassPrimitive.FLOAT.setAutoboxingClass(systemClassLoader.getClass("Float"));
		HiClassPrimitive.DOUBLE.setAutoboxingClass(systemClassLoader.getClass("Double"));
		HiClassPrimitive.BOOLEAN.setAutoboxingClass(systemClassLoader.getClass("Boolean"));
		HiClassPrimitive.CHAR.setAutoboxingClass(systemClassLoader.getClass("Character"));
		HiClassPrimitive.VOID.setAutoboxingClass(HiClassPrimitive.VOID); // to avoid NPE
		for (HiClassPrimitive primitiveClass : HiClassPrimitive.primitiveClasses.values()) {
			if (primitiveClass != HiClassPrimitive.VOID) {
				primitiveClass.getAutoboxClass().autoboxedPrimitiveClass = primitiveClass;
			}
		}
	}

	private static void loadSystemClasses(HiClassLoader systemClassLoader) {
		systemClassLoader.clear();

		try {
			List<HiClass> classes = new ArrayList<>();

			// TODO define classes initialization order automatically

			// object
			OBJECT_CLASS = systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Object.hi"), false).get(0);
			OBJECT_CLASS.superClassType = null;
			OBJECT_CLASS.constructors = new HiConstructor[] {HiConstructor.createDefaultConstructor(OBJECT_CLASS, Type.objectType)};
			classes.add(OBJECT_CLASS);

			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/System.hi"), false));
			classes.add(STRING_CLASS = systemClassLoader.load(HiCompiler.class.getResource("/hilibs/String.hi"), false).get(0));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Class.hi"), false));
			classes.add(NUMBER_CLASS = systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Number.hi"), false).get(0));

			classes.add(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Byte.hi"), false).get(0));
			classes.add(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Short.hi"), false).get(0));
			classes.add(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Integer.hi"), false).get(0));
			classes.add(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Long.hi"), false).get(0));
			classes.add(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Float.hi"), false).get(0));
			classes.add(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Double.hi"), false).get(0));
			classes.add(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Boolean.hi"), false).get(0));
			classes.add(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Character.hi"), false).get(0));
			HiClassPrimitive.BYTE.setAutoboxingClass(systemClassLoader.getClass("Byte"));
			HiClassPrimitive.SHORT.setAutoboxingClass(systemClassLoader.getClass("Short"));
			HiClassPrimitive.INT.setAutoboxingClass(systemClassLoader.getClass("Integer"));
			HiClassPrimitive.LONG.setAutoboxingClass(systemClassLoader.getClass("Long"));
			HiClassPrimitive.FLOAT.setAutoboxingClass(systemClassLoader.getClass("Float"));
			HiClassPrimitive.DOUBLE.setAutoboxingClass(systemClassLoader.getClass("Double"));
			HiClassPrimitive.BOOLEAN.setAutoboxingClass(systemClassLoader.getClass("Boolean"));
			HiClassPrimitive.CHAR.setAutoboxingClass(systemClassLoader.getClass("Character"));
			HiClassPrimitive.VOID.setAutoboxingClass(HiClassPrimitive.VOID); // to avoid NPE
			for (HiClassPrimitive primitiveClass : HiClassPrimitive.primitiveClasses.values()) {
				if (primitiveClass != HiClassPrimitive.VOID) {
					primitiveClass.getAutoboxClass().autoboxedPrimitiveClass = primitiveClass;
				}
			}

			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Enum.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/Record.hi"), false));
			classes.addAll(systemClassLoader.load(HiCompiler.class.getResource("/hilibs/AutoCloseable.hi"), false));
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
				CompileClassContext ctx = new CompileClassContext(compiler, null, null, ClassLocationType.top);
				clazz.validate(validationInfo, ctx);
			}
			validationInfo.throwExceptionIf();
		} catch (IOException | TokenizerException | HiScriptParseException | HiScriptValidationException exc) {
			exc.printStackTrace();
		}
	}

	public String[] packagePath;

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

	public ClassLocationType locationType;

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
	public HiClass(HiClassLoader classLoader, HiClass superClass, HiClass enclosingClass, String name, ClassLocationType locationType, ClassResolver classResolver) {
		this.superClass = superClass;
		if (superClass != null) {
			this.superClassType = Type.getType(superClass);
		}
		init(classLoader, classResolver, enclosingClass, name, null, locationType);
	}

	// for ClassParseRule and NewParseRule
	public HiClass(HiClassLoader classLoader, Type superClassType, HiClass enclosingClass, Type[] interfaceTypes, String name, NodeGenerics generics, ClassLocationType locationType, ClassResolver classResolver) {
		this.superClassType = superClassType;
		this.interfaceTypes = interfaceTypes;
		init(classLoader, classResolver, enclosingClass, name, generics, locationType);
	}

	// for decode
	protected HiClass(Type superClassType, String name, NodeGenerics generics, ClassLocationType locationType) {
		this.superClassType = superClassType;
		this.name = name.intern();
		this.locationType = locationType;
		this.generics = generics;
		// init(...) is in decode
	}

	private void init(HiClassLoader classLoader, ClassResolver classResolver, HiClass enclosingClass, String name, NodeGenerics generics, ClassLocationType locationType) {
		this.enclosingClass = enclosingClass;
		this.locationType = locationType;
		this.classLoader = classLoader;
		// intern name to optimize via a == b
		this.name = (name != null ? name : "").intern();
		this.fullName = getFullName(classLoader);
		this.hashCode = fullName.hashCode();
		this.generics = generics;
		if (classLoader != null) { // generated classes may have null classLoader
			classLoader.addClass(this, classResolver instanceof RuntimeContext);
		}
	}

	public String getFullName(HiClassLoader classLoader) {
		if (this.fullName == null) {
			if (enclosingClass != null && !isEnum() && classLoader != null) {
				switch (locationType) {
					case top:
					case inner:
						fullName = enclosingClass.fullName + "$" + name;
						break;

					case local:
						int number = 0;
						do {
							fullName = enclosingClass.fullName + "$" + number + name;
							number++;
						} while (classLoader.getClass(fullName) != null);
						break;

					case anonymous:
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
					classResolver.processResolverException("cannot resolve class '" + superClassType + "'");
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

			if (constructors == null && !isInterface) {
				if (isAnonymous() && !superClass.isInterface) {
					// delegate to super constructors
					constructors = new HiConstructor[superClass.constructors.length];
					for (int i = 0; i < superClass.constructors.length; i++) {
						HiConstructor superConstructor = superClass.constructors[i];
						constructors[i] = new HiConstructor(this, superConstructor, classResolver);
					}
				} else {
					HiConstructor defaultConstructor = HiConstructor.createDefaultConstructor(this, null);
					constructors = new HiConstructor[] {defaultConstructor};
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
				String message = exc.getMessage() != null ? exc.getMessage() : exc.getClass().getName();
				ctx.throwRuntimeException("cannot initialize class " + getNameDescr() + ": " + message, ctx.exception);
			} finally {
				ctx.exit();
			}
		}
	}

	public HiClassLoader getClassLoader() {
		return classLoader;
	}

	public HiRuntimeEnvironment getEnv() {
		return classLoader.getEnv();
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

		// @unnamed
		if (UNNAMED.equals(name)) {
			validationInfo.error("keyword '_' cannot be used as an identifier", token);
			valid = false;
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
				validationInfo.error("type parameter expected", generics);
				valid = false;
			} else {
				valid &= generics.validate(validationInfo, ctx);
			}
			for (int i = 0; i < generics.generics.length; i++) {
				NodeGeneric generic = generics.generics[i];
				if (generic.isWildcard()) {
					validationInfo.error("unexpected wildcard", generic);
					valid = false;
				} else if (generic.isSuper) {
					validationInfo.error("super is unsupported", generic);
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
				assert parameterClass != null;
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

		ctx.enter(ContextType.STATIC_CLASS, this);
		valid &= HiNode.validateAnnotations(validationInfo, ctx, annotations);

		if (superClassType != null && superClass == null && !name.equals(OBJECT_CLASS_NAME)) {
			superClass = OBJECT_CLASS;
			valid = false;
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
			if (isTopLevel() && modifiers.isPrivate()) {
				validationInfo.error("modifier 'private' not allowed here", token);
				valid = false;
			}
			if (modifiers.isProtected()) {
				validationInfo.error("modifier 'protected' not allowed here", token);
				valid = false;
			}
		}

		if (initializers != null) {
			// validate fields
			for (int i = 0; i < initializers.length; i++) {
				HiNode initializer = (HiNode) initializers[i];
				if (!(initializer instanceof HiField)) {
					continue;
				}
				HiField field = (HiField) initializer;

				valid &= field.validate(validationInfo, ctx);

				if (field instanceof HiFieldVar) {
					HiFieldVar varField = (HiFieldVar) field;
					if (varField.type != Type.varType) {
						int fieldIndex = -1;
						for (int j = 0; j < fields.length; j++) {
							if (field == fields[j]) {
								fieldIndex = j;
								break;
							}
						}
						ctx.level.removeField(field);
						field = HiFieldVar.getField(varField.getClass(ctx), varField.name, varField.initializer, varField.token);
						initializers[i] = field;
						ctx.level.addField(field);
						fields[fieldIndex] = field;
					}
				}

				// @unnamed
				if (UNNAMED.equals(field.name)) {
					validationInfo.error("keyword '_' cannot be used as an identifier", token);
					valid = false;
				}
				if (isInterface) {
					Modifiers fieldModifiers = field.getModifiers();
					if (fieldModifiers.isProtected()) {
						validationInfo.error("modifier 'protected' not allowed here", token);
						valid = false;
					}
					if (!fieldModifiers.isPublic() || !fieldModifiers.isFinal() || !fieldModifiers.isStatic()) {
						field.setModifiers(fieldModifiers.change().setPublic().setFinal(true).setStatic(true));
					}
				}
				if (field.isFinal() && field.initializer == null) {
					// TODO check initialization in all constructors
					validationInfo.error("variable '" + field.name + "' might not have been initialized", field);
					valid = false;
				}
				ctx.initializedNodes.add(field);
			}

			// validate initializers after fields
			for (int i = 0; i < initializers.length; i++) {
				HiNode initializer = (HiNode) initializers[i];
				if (initializer instanceof HiField) {
					continue;
				}

				ctx.enter(ContextType.INITIALIZATION, initializer);
				valid &= initializer.validate(validationInfo, ctx);
				ctx.exit();

				if (isInterface) {
					validationInfo.error("interface cannot have initializers", initializer);
					valid = false;
				}
			}
		}

		if (innerClasses != null) {
			for (int i = 0; i < innerClasses.length; i++) {
				HiClass innerClass = innerClasses[i];
				valid &= innerClass.validate(validationInfo, ctx);
			}
		}

		if (constructors != null) {
			for (int i = 0; i < constructors.length; i++) {
				HiConstructor constructor = constructors[i];
				valid &= constructor.validate(validationInfo, ctx);
				if (isInterface) {
					validationInfo.error("interface cannot have constructors", constructor);
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
		_validateNext(validationInfo, ctx);
		ctx.exit();

		ctx.addLocalClass(this);
		ctx.clazz = outboundClass;
		return valid;
	}

	// after ctx enter and before exit in context of current class
	protected boolean _validateNext(ValidationInfo validationInfo, CompileClassContext ctx) {
		return true;
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

	public HiClassArray arrayClass;

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

		// TODO delete?
//		HiClassGeneric genericClass = getGenericClass(classResolver, name);
//		if (genericClass != null) {
//			return genericClass;
//		}

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
		if (this == clazz || clazz == HiClass.OBJECT_CLASS || this == HiClassVar.VAR) {
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
			// @generics
			HiClassGeneric genericClass = (HiClassGeneric) clazz;
			return genericClass.isInstanceof(this);
		} else if (isArray()) {
			if (!clazz.isArray()) {
				return false;
			}
			HiClassArray c1 = (HiClassArray) this;
			HiClassArray c2 = (HiClassArray) clazz;
			if (c1.cellClass.isPrimitive() || c2.cellClass.isPrimitive()) {
				return false; // condition this == clazz is already checked
			}
			if (c2.cellClass == HiClass.OBJECT_CLASS) {
				return true;
			}
			if (c1.dimension != c2.dimension) {
				return false;
			}
			return c1.getRootCellClass().isInstanceof(c2.getRootCellClass());
		} else if (clazz.isArray()) {
			return false;
		} else {
			HiClass c = this;
			HiClass first = this;
			while (c != null) {
				if (c == clazz || c.fullName.equals(clazz.fullName)) {
					return true;
				}
				if (c.hasInterfaceInstanceof(clazz)) {
					return true;
				}
				c = c.superClass;
				if (c == first) {
					// isInstanceof may be called before checking on 'cyclic inheritance involving'
					break;
				}
			}
		}
		return false;
	}

	public boolean isInstanceof(String className) {
		HiClass c = this;
		HiClass first = this;
		while (c != null) {
			if (c.fullName.equals(className)) {
				return true;
			}
			if (c.hasInterfaceInstanceof(className)) {
				return true;
			}
			c = c.superClass;
			if (c == first) {
				// isInstanceof may be called before checking on 'cyclic inheritance involving'
				break;
			}
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
				if (field.isPrivate() && classResolver instanceof CompileClassContext) {
					classResolver.processResolverException("'" + field.name + "' has private access in '" + superClass.getNameDescr() + "'");
				}
				return field;
			}
		}

		// enclosing fields
		if (!local && enclosingClass != null) {
			HiField<?> field = enclosingClass.getField(classResolver, name, false);
			if (field != null) {
				return field;
			}
		}
		return null;
	}

	private final Map<MethodSignature, List<HiMethod>> methodsHash = new HashMap<>();

	public HiMethod searchMethod(ClassResolver classResolver, String name, HiClass... argsClasses) {
		return searchMethod(classResolver, new MethodSignature(name, argsClasses, false));
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
					ArgClassPriorityType argPriority = foundMethod.signature.getPriority(method.signature, searchSignature);
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
		HiClass[] argsClasses = signature.argsClasses;
		List<HiMethod> matchedMethods = null;

		// functional method
		if (functionalMethod != null) {
			if (isMatchMethodArguments(classResolver, functionalMethod, argsClasses)) {
				matchedMethods = addFoundMethod(functionalMethod, signature, matchedMethods, false);
			}
		}

		// current class methods
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				HiMethod method = methods[i];
				if (method.name.equals(name) || method.isLambda() || signature.isLambda()) {
					if (isMatchMethodArguments(classResolver, method, argsClasses)) {
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

	public HiMethod getInterfaceAbstractMethod(ClassResolver classResolver, HiMethod method) {
		if (methods != null && methods.length > 0) {
			for (MatchMethodArgumentsType matchType : MatchMethodArgumentsType.values()) {
				for (int i = 0; i < methods.length; i++) {
					HiMethod m = methods[i];
					if (m.isAbstract() && isMatchMethodsInvocation(classResolver, m, method, matchType)) {
						return m;
					}
				}
			}
		}
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				HiMethod m = interfaces[i].getInterfaceAbstractMethod(classResolver, method);
				if (m != null) {
					return m;
				}
			}
		}
		if (superClass != null) {
			return superClass.getInterfaceAbstractMethod(classResolver, method);
		}
		return null;
	}

	public enum MatchMethodArgumentsType {
		strict(false, false, false), autoboxing(false, false, true), noCast(false, true, true), noVarargs(true, false, true), soft(true, true, true);

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

	private boolean isMatchMethodArguments(ClassResolver classResolver, HiMethod method, HiClass[] argsTypes) {
		if (method.hasVarargs()) {
			int mainArgsCount = method.argsCount - 1;
			if (!isMatchMethodArgumentsPartially(classResolver, method, argsTypes, mainArgsCount)) {
				return false;
			}

			HiClass varargsClass = method.argsClasses[mainArgsCount].getArrayElementClass();
			NodeArgument vararg = method.arguments[mainArgsCount];
			if (argsTypes.length == method.argsCount && argsTypes[mainArgsCount].getArrayDimension() == varargsClass.getArrayDimension() + 1) {
				HiClass argClass = argsTypes[mainArgsCount].getArrayElementClass();
				if (!HiClass.autoCast(classResolver, argClass, varargsClass, false, true)) {
					return false;
				}
				argClass.applyLambdaImplementedMethod(classResolver, varargsClass, vararg);
			} else {
				for (int i = mainArgsCount; i < argsTypes.length; i++) {
					if (!HiClass.autoCast(classResolver, argsTypes[i], varargsClass, false, true)) {
						return false;
					}
				}
				for (int i = mainArgsCount; i < argsTypes.length; i++) {
					argsTypes[i].applyLambdaImplementedMethod(classResolver, varargsClass, vararg);
				}
			}
		} else {
			int argsCount = method.argsCount;
			if (argsCount != argsTypes.length) {
				return false;
			}
			if (!isMatchMethodArgumentsPartially(classResolver, method, argsTypes, argsCount)) {
				return false;
			}
		}
		return true;
	}

	private boolean isMatchMethodArgumentsPartially(ClassResolver classResolver, HiMethod method, HiClass[] argsTypes, int argsCount) {
		if (argsCount > argsTypes.length) {
			return false;
		}

		method.resolve(classResolver);

		for (int i = 0; i < argsCount; i++) {
			if (!HiClass.autoCast(classResolver, argsTypes[i], method.argsClasses[i], false, true)) {
				return false;
			}
		}
		for (int i = 0; i < argsCount; i++) {
			argsTypes[i].applyLambdaImplementedMethod(classResolver, method.argsClasses[i], method.arguments[i]);
		}
		return true;
	}

	/**
	 * @param method1 method for invocation with matching arguments
	 * @param method2 check whether this method may be invoked with arguments from method1
	 */
	private boolean isMatchMethodsInvocation(ClassResolver classResolver, HiMethod method1, HiMethod method2, MatchMethodArgumentsType matchType) {
		if (matchType.isVarargs() && method1.hasVarargs() && !method2.hasVarargs()) {
			return false;
		} else if (matchType.isVarargs() && !method1.hasVarargs() && method2.hasVarargs()) {
			int mainArgsCount = method2.argsCount - 1;
			if (mainArgsCount > method1.argsCount) {
				return false;
			}

			HiClass[] argsClasses1 = method1.argsClasses;
			HiClass[] argsClasses2 = method2.argsClasses;
			for (int i = 0; i < mainArgsCount; i++) {
				if (!HiClass.autoCast(classResolver, argsClasses1[i], argsClasses2[i], false, matchType.isAutobox())) {
					return false;
				}
			}
			for (int i = 0; i < mainArgsCount; i++) {
				argsClasses2[i].applyLambdaImplementedMethod(classResolver, argsClasses1[i], method1.arguments[i]);
			}

			HiClass varargsClass = method2.argsClasses[mainArgsCount].getArrayElementClass();
			NodeArgument varargs = method2.arguments[mainArgsCount];
			if (method1.argsCount == method2.argsCount && method1.argsClasses[mainArgsCount].getArrayDimension() == varargsClass.getArrayDimension() + 1) {
				HiClass argClass = method1.argsClasses[mainArgsCount].getArrayElementClass();
				if (!HiClass.autoCast(classResolver, varargsClass, argClass, false, matchType.isAutobox())) {
					return false;
				}
				argClass.applyLambdaImplementedMethod(classResolver, varargsClass, varargs);
			} else {
				for (int i = mainArgsCount; i < method1.argsCount; i++) {
					if (!HiClass.autoCast(classResolver, method1.argsClasses[i], varargsClass, false, matchType.isAutobox())) {
						return false;
					}
				}
				for (int i = mainArgsCount; i < method2.argsCount; i++) {
					argsClasses2[i].applyLambdaImplementedMethod(classResolver, varargsClass, varargs);
				}
			}
		} else {
			int argCount = method1.argsCount;
			if (argCount != method2.argsCount) {
				return false;
			}
			HiClass[] argsClasses1 = method1.argsClasses;
			HiClass[] argsClasses2 = method2.argsClasses;
			for (int i = 0; i < argCount; i++) {
				if (!HiClass.autoCast(classResolver, argsClasses1[i], argsClasses2[i], false, matchType.isAutobox())) {
					return false;
				}
			}
			for (int i = 0; i < argCount; i++) {
				argsClasses2[i].applyLambdaImplementedMethod(classResolver, argsClasses1[i], method1.arguments[i]);
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

	public HiMethod getMethod(ClassResolver classResolver, String name, HiClass... argsClasses) {
		return getMethod(classResolver, new MethodSignature(name, argsClasses, false));
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
		HiClass[] argsTypes = signature.argsClasses;

		if (functionalMethod != null) {
			int argCount = functionalMethod.argsCount;
			if (argCount == argsTypes.length) {
				functionalMethod.resolve(classResolver);
				boolean match = true;
				for (int i = 0; i < argCount; i++) {
					if (argsTypes[i] != functionalMethod.argsClasses[i]) {
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
						int argCount = method.argsCount;
						if (argCount != argsTypes.length) {
							continue;
						}

						method.resolve(classResolver);

						for (int j = 0; j < argCount; j++) {
							if (argsTypes[j] != method.argsClasses[j]) {
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

	private final Map<ArgumentsSignature, List<HiConstructor>> constructorsHash = new HashMap<>();

	public HiConstructor searchConstructor(ClassResolver classResolver, HiClass... argsClasses) {
		ArgumentsSignature signature = new ArgumentsSignature(argsClasses, false);
		List<HiConstructor> constructors = constructorsHash.get(signature);
		if (constructors == null) {
			constructors = _searchConstructors(classResolver, signature);
			if (constructors != null) {
				if (constructors.size() > 1) {
					HiConstructor c1 = constructors.get(0);
					HiConstructor c2 = constructors.get(1);
					classResolver.processResolverException("Ambiguous constructor call. Both " + c1 + " in " + c1.clazz.getNameDescr() + " and " + c2 + " in " + c2.clazz.getNameDescr() + " match.");
				}
				constructorsHash.put(new ArgumentsSignature(signature), constructors);
			}
		}
		return constructors != null && constructors.size() > 0 ? constructors.get(0) : null;
	}

	private List<HiConstructor> addFoundConstructor(HiConstructor constructor, ArgumentsSignature searchSignature, List<HiConstructor> foundConstructors, boolean mayRewite) {
		if (foundConstructors != null) {
			for (int i = foundConstructors.size() - 1; i >= 0; i--) {
				HiConstructor foundConstructor = foundConstructors.get(i);
				ArgClassPriorityType argPriority = foundConstructor.signature.getPriority(constructor.signature, searchSignature);
				if (argPriority == ArgClassPriorityType.higher) {
					return foundConstructors;
				} else if (argPriority == ArgClassPriorityType.lower) {
					foundConstructors.remove(i);
				} else if (argPriority == ArgClassPriorityType.equals && mayRewite) {
					return foundConstructors;
				}
			}
		} else {
			foundConstructors = new ArrayList<>(1);
		}
		foundConstructors.add(constructor);
		return foundConstructors;
	}

	protected List<HiConstructor> _searchConstructors(ClassResolver classResolver, ArgumentsSignature signature) {
		List<HiConstructor> matchedConstructors = null;
		if (constructors != null) {
			for (int i = 0; i < constructors.length; i++) {
				HiConstructor constructor = constructors[i];
				if (isMatchConstructorArguments(classResolver, constructor, signature.argsClasses)) {
					matchedConstructors = addFoundConstructor(constructor, signature, matchedConstructors, false);
				}
			}
		}
		return matchedConstructors;
	}

	protected boolean isMatchConstructorArguments(ClassResolver classResolver, HiConstructor constructor, HiClass[] argsClasses) {
		int constructorArgsCount = constructor.arguments != null ? constructor.arguments.length : 0;
		int matchArgsCount = argsClasses != null ? argsClasses.length : 0;
		if (constructor.hasVarargs()) {
			int mainArgCount = constructorArgsCount - 1;
			if (!isMatchConstructorArgumentsPartially(classResolver, constructor, argsClasses, mainArgCount)) {
				return false;
			}

			HiClass varargsClass = constructor.argsClasses[mainArgCount].getArrayElementClass();
			NodeArgument vararg = constructor.arguments[mainArgCount];
			if (matchArgsCount == constructorArgsCount && argsClasses[mainArgCount].getArrayDimension() == varargsClass.getArrayDimension() + 1) {
				HiClass argClass = argsClasses[mainArgCount].getArrayElementClass();
				if (!HiClass.autoCast(classResolver, argClass, varargsClass, false, true)) {
					return false;
				}
				argClass.applyLambdaImplementedMethod(classResolver, varargsClass, vararg);
			} else {
				for (int i = mainArgCount; i < matchArgsCount; i++) {
					if (!HiClass.autoCast(classResolver, argsClasses[i], varargsClass, false, true)) {
						return false;
					}
				}
				for (int i = mainArgCount; i < matchArgsCount; i++) {
					argsClasses[i].applyLambdaImplementedMethod(classResolver, varargsClass, vararg);
				}
			}
		} else {
			if (constructorArgsCount != matchArgsCount) {
				return false;
			}
			if (!isMatchConstructorArgumentsPartially(classResolver, constructor, argsClasses, constructorArgsCount)) {
				return false;
			}
		}
		return true;
	}

	private boolean isMatchConstructorArgumentsPartially(ClassResolver classResolver, HiConstructor constructor, HiClass[] argsClasses, int constructorArgsCount) {
		int matchArgsCount = argsClasses != null ? argsClasses.length : 0;
		if (constructorArgsCount > matchArgsCount) {
			return false;
		}

		constructor.resolve(classResolver);

		for (int i = 0; i < constructorArgsCount; i++) {
			if (!HiClass.autoCast(classResolver, argsClasses[i], constructor.argsClasses[i], false, true)) {
				return false;
			}
		}
		for (int i = 0; i < constructorArgsCount; i++) {
			argsClasses[i].applyLambdaImplementedMethod(classResolver, constructor.argsClasses[i], constructor.arguments[i]);
		}
		return true;
	}

	public HiConstructor getConstructor(ClassResolver classResolver, HiClass... argsClasses) {
		if (constructors != null) {
			int size = constructors.length;
			for (int i = 0; i < size; i++)
				FOR:{
					HiConstructor c = constructors[i];
					int argCount = c.arguments != null ? c.arguments.length : 0;
					if (argCount != argsClasses.length) {
						continue;
					}

					c.resolve(classResolver);
					for (int j = 0; j < argCount; j++) {
						if (argsClasses[j] != c.argsClasses[j]) {
							break FOR;
						}
					}
					return c;
				}
		}
		return null;
	}

	// @generics
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

	public PrimitiveType getPrimitiveType() {
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

	public boolean isLongNumber() {
		return false;
	}

	public boolean isArray() {
		return false;
	}

	public boolean isAnnotation() {
		return false;
	}

	public boolean isAnonymous() {
		return locationType == ClassLocationType.anonymous; // or name.equals("")
	}

	public boolean isLocal() {
		return locationType == ClassLocationType.local;
	}

	public boolean isInner() {
		return locationType == ClassLocationType.inner;
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

	public HiClass getArrayElementClass() {
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

	public static HiClass getCellClass(Class<?> javaClass) {
		HiClass cellClass = null;
		if (javaClass == HiObject.class || javaClass == Object.class) {
			cellClass = HiClass.OBJECT_CLASS;

		} else if (javaClass == boolean.class) {
			cellClass = HiClassPrimitive.BOOLEAN;
		} else if (javaClass == Boolean.class) {
			cellClass = HiClassPrimitive.BOOLEAN.getAutoboxClass();

		} else if (javaClass == char.class) {
			cellClass = HiClassPrimitive.CHAR;
		} else if (javaClass == Character.class) {
			cellClass = HiClassPrimitive.CHAR.getAutoboxClass();

		} else if (javaClass == byte.class) {
			cellClass = HiClassPrimitive.BYTE;
		} else if (javaClass == Byte.class) {
			cellClass = HiClassPrimitive.BYTE.getAutoboxClass();

		} else if (javaClass == short.class) {
			cellClass = HiClassPrimitive.SHORT;
		} else if (javaClass == Short.class) {
			cellClass = HiClassPrimitive.SHORT.getAutoboxClass();

		} else if (javaClass == int.class) {
			cellClass = HiClassPrimitive.INT;
		} else if (javaClass == Integer.class) {
			cellClass = HiClassPrimitive.INT.getAutoboxClass();

		} else if (javaClass == float.class) {
			cellClass = HiClassPrimitive.FLOAT;
		} else if (javaClass == Float.class) {
			cellClass = HiClassPrimitive.FLOAT.getAutoboxClass();

		} else if (javaClass == long.class) {
			cellClass = HiClassPrimitive.LONG;
		} else if (javaClass == Long.class) {
			cellClass = HiClassPrimitive.LONG.getAutoboxClass();

		} else if (javaClass == double.class) {
			cellClass = HiClassPrimitive.DOUBLE;
		} else if (javaClass == Double.class) {
			cellClass = HiClassPrimitive.DOUBLE.getAutoboxClass();

		} else if (javaClass == String.class) {
			cellClass = HiClass.STRING_CLASS;
		} else if (Map.class.isAssignableFrom(javaClass)) {
			cellClass = HiClass.getSystemClassLoader().getClass(HiClass.HASHMAP_CLASS_NAME);
		} else if (Collection.class.isAssignableFrom(javaClass)) {
			cellClass = HiClass.getSystemClassLoader().getClass(HiClass.ARRAYLIST_CLASS_NAME);
		}
		return cellClass;
	}

	public static HiClassArray getArrayElementClass(Class<?> clazz) {
		int dimension = 0;
		Class<?> cellJavaClass = clazz;
		while (cellJavaClass.isArray()) {
			cellJavaClass = cellJavaClass.getComponentType();
			dimension++;
		}

		HiClass cellClass = getCellClass(cellJavaClass);
		return cellClass.getArrayClass(dimension);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		if (classLoader.isSystem() && !classLoader.isLoading()) {
			os.writeEnum(ClassType.CLASS_SYSTEM);
			os.writeUTF(fullName);
		} else {
			code(os, ClassType.CLASS_OBJECT);
		}
	}

	public void code(CodeContext os, ClassType classType) throws IOException {
		// write class type
		os.writeEnum(classType);
		os.writeToken(token);
		os.writeUTF(name);
		os.writeUTF(classLoader.getName());

		// constructor parameters
		os.writeBoolean(superClassType != null || superClass != null);
		if (superClassType != null) {
			os.writeType(superClassType);
		} else if (superClass != null) {
			os.writeType(Type.getType(superClass));
		}

		os.writeBoolean(enclosingClass != null);
		if (enclosingClass != null) {
			os.writeClass(enclosingClass);
		}

		os.writeNullable(generics);
		os.writeEnum(locationType);
		os.writeUTF(fullName);

		// content
		os.writeBoolean(superClass != null);
		if (superClass != null) {
			os.writeClass(superClass);
		}
		os.writeNullable(modifiers);
		os.writeTypes(interfaceTypes);
		os.writeClasses(interfaces);
		os.writeShortArray(constructors);
		if (isLambda()) {
			os.write(functionalMethod);
		}
		os.writeBoolean(isInterface);
		os.writeShortArray(annotations);

		os.writeShort(fields != null ? fields.length : 0);
		os.writeShortArray(initializers); // contains ordered fields and blocks
		os.writeShortArray(methods);
		os.writeClasses(innerClasses);
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		int classIndex = os.readShort(); // written in CodeContext.getClassContext(int index)
		ClassType classType = ClassType.values()[os.readByte()];
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
			case CLASS_MIX:
				return HiClassMix.decode(os, classIndex);
		}
		throw new HiScriptRuntimeException("unknown class type: " + classType);
	}

	public static HiClass decodeSystem(DecodeContext os) throws IOException {
		return os.getClassLoader().getClass(os.readUTF());
	}

	public static HiClass decodeObject(DecodeContext os, ClassType classType, int classIndex) throws IOException {
		final HiClass[] classAccess = new HiClass[1];
		Token token = os.readToken();
		String name = os.readUTF();
		String classLoaderName = os.readUTF();

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
				os.addClassLoadListener(clazz -> classAccess[0].init(os, classLoaderName, clazz, classAccess[0].name, classAccess[0].generics, classAccess[0].locationType), exc.getIndex());
			}
		}

		NodeGenerics generics = os.readNullable(NodeGenerics.class);
		ClassLocationType locationType = ClassLocationType.values()[os.readByte()];

		HiClass clazz;
		if (classType == ClassType.CLASS_ENUM) {
			clazz = new HiClassEnum(name, locationType);
		} else if (classType == ClassType.CLASS_RECORD) {
			clazz = new HiClassRecord(name, generics, locationType);
		} else if (classType == ClassType.CLASS_ANNOTATION) {
			clazz = new HiClassAnnotation(name, locationType);
		} else if (classType == ClassType.CLASS_GENERIC) {
			clazz = new HiClassGeneric(name, locationType);
		} else if (classType == ClassType.CLASS_MIX) {
			clazz = new HiClassMix(os.getClassLoader());
		} else {
			clazz = new HiClass(superClassType, name, generics, locationType);
		}
		clazz.fullName = os.readUTF();
		clazz.token = token;
		classAccess[0] = clazz;
		if (initClass) {
			clazz.init(os, classLoaderName, outerClass, name, generics, locationType);
		}

		HiClass oldClass = os.getHiClass();
		os.setHiClass(clazz);

		// content
		if (os.readBoolean()) {
			os.readClass(c -> clazz.superClass = c);
		}
		clazz.modifiers = os.readNullable(Modifiers.class);
		clazz.interfaceTypes = os.readTypes();
		clazz.interfaces = os.readClasses();
		clazz.constructors = os.readShortArray(HiConstructor.class);
		if (clazz.isLambda()) {
			clazz.functionalMethod = os.read(HiMethod.class);
		}
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

		os.setHiClass(oldClass);

		// try resolve super class
		if (superClassType != null) {
			clazz.superClass = os.getClassLoader().getClass(superClassType.fullName);
		}
		return clazz;
	}

	private void init(DecodeContext os, String classLoaderName, HiClass enclosingClass, String name, NodeGenerics generics, ClassLocationType locationType) {
		HiClassLoader classLoader;
		if (isPrimitive()) {
			// TODO delete?
			classLoader = HiClassLoader.primitiveClassLoader;
		} else if (HiClassLoader.SYSTEM_CLASS_LOADER_NAME.equals(classLoaderName)) {
			classLoader = os.getClassLoader().getSystemClassLoader();
		} else {
			classLoader = os.getClassLoader();
		}
		init(classLoader, null, enclosingClass, name, generics, locationType);
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

	public Class getJavaClass(HiRuntimeEnvironment env) {
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
			if (c == this) {
				return c;
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
				validationInfo.error("incompatible types: " + src.getNameDescr() + " cannot be converted to " + dst.getNameDescr(), node);
				return false;
			}
			return true;
		}
		if (dst.getRootCellClass() == HiClass.OBJECT_CLASS && dst.dimension <= src.dimension) {
			return true;
		}
		if (!autoCast(ctx, src.getRootCellClass(), dst.getRootCellClass(), false, true)) {
			validationInfo.error("incompatible types: " + src.getNameDescr() + " cannot be converted to " + dst.getNameDescr(), node);
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
						validationInfo.error("incompatible types: " + cellValue.getValueClass(validationInfo, ctx).getNameDescr() + " cannot be converted to " + dst.cellClass.getNameDescr(), cellValue);
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

		// @autoboxing
		if (isAutobox && src != HiClassPrimitive.VOID && dst == HiClass.OBJECT_CLASS) {
			return true;
		}

		if (src.isPrimitive() || dst.isPrimitive()) {
			// @autoboxing
			// @generics
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

		// @generics
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
		String fullName = isArray() ? getArrayElementClass().fullName : this.fullName;
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
				boolean isNumberArg = argClass.isNumber();
				boolean isNumber1 = c1.isNumber();
				boolean isNumber2 = c2.isNumber();
				if (isNumber1 != isNumber2) {
					if (isNumber1 == isNumberArg) {
						return ArgClassPriorityType.higher;
					} else if (isNumber2 == isNumberArg) {
						return ArgClassPriorityType.lower;
					}
				} else if (isNumber1 && isNumber2) {
					// matched next closed type (except char and short)
					PrimitiveType t1 = c1.getPrimitiveType();
					if (t1 == CHAR_TYPE) {
						return ArgClassPriorityType.nonComparable;
					}
					PrimitiveType t2 = c2.getPrimitiveType();
					if (t2 == CHAR_TYPE) {
						return ArgClassPriorityType.nonComparable;
					}
					PrimitiveType t = argClass.getPrimitiveType();
					if (t.ordinal() <= t1.ordinal() && (t1.ordinal() < t2.ordinal() || t2.ordinal() < t.ordinal())) {
						return ArgClassPriorityType.higher;
					} else if (t.ordinal() <= t2.ordinal() && (t2.ordinal() < t1.ordinal() || t1.ordinal() < t.ordinal())) {
						return ArgClassPriorityType.lower;
					} else {
						return ArgClassPriorityType.nonComparable;
					}
				}
			} else if (c1.getAutoboxedPrimitiveClass() == argClass) {
				return ArgClassPriorityType.higher;
			} else if (c2.getAutoboxedPrimitiveClass() == argClass) {
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
		} else if (c2.isInstanceof(c1)) {
			return ArgClassPriorityType.lower;
		}
		return ArgClassPriorityType.nonComparable;
	}

	public boolean isStringClass() {
		return STRING_CLASS_NAME.equals(fullName);
	}
}
