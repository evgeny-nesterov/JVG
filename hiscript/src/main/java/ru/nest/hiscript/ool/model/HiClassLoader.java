package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.HiCompiler;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.HiScriptRuntimeException;
import ru.nest.hiscript.ool.compile.parse.ClassFileParseRule;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiNative;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HiClassLoader {
	public final static String ROOT_CLASS_LOADER_NAME = "root";

	public final static String PRIMITIVE_CLASS_LOADER_NAME = "primitive";

	public final static String SYSTEM_CLASS_LOADER_NAME = "system";

	public final static String USER_CLASS_LOADER_NAME = "user";

	public final static HiClassLoader primitiveClassLoader = new HiClassLoader(PRIMITIVE_CLASS_LOADER_NAME, null, null);

	public static HiClassLoader createRoot(HiRuntimeEnvironment env) {
		return new HiClassLoader(env);
	}

	private static HiClassLoader systemClassLoader;

	public static HiClassLoader createSystem() {
		if (systemClassLoader == null) {
			systemClassLoader = new HiClassLoader(SYSTEM_CLASS_LOADER_NAME, null, null);
		}
		return systemClassLoader;
	}

	public static HiClassLoader getSystemClassLoader() {
		return systemClassLoader;
	}

	// root
	private HiClassLoader(HiRuntimeEnvironment env) {
		this.name = ROOT_CLASS_LOADER_NAME;
		this.env = env;
		classLoaders = new ArrayList<>(1);
		HiClass.getSystemClassLoader(); // init HiClass
	}

	public HiClassLoader(String name, HiClassLoader parent, HiRuntimeEnvironment env) {
		this.name = name;
		if (SYSTEM_CLASS_LOADER_NAME.equals(name)) {
			this.parent = null;
			this.env = null;
			this.nativeObjects = new HiNative(this);
		} else {
			this.parent = parent;
			this.env = parent != null ? parent.getEnv() : env;
			if (parent != null) {
				this.nativeObjects = new HiNative(this);
				parent.addClassLoader(this);
			}
		}
	}

	public HiClassLoader getRoot() {
		HiClassLoader root = this;
		while (root.parent != null) {
			root = root.parent;
		}
		return root;
	}

	public HiClassLoader getParent() {
		return parent;
	}

	private final String name;

	private HiClassLoader parent;

	private List<HiClassLoader> classLoaders;

	private final Map<String, HiClass> classes = new ConcurrentHashMap<>();

	private final Map<HiClass, HiObject> classesObjects = new ConcurrentHashMap<>();

	private HiClass classClass;

	private HiConstructor classConstructor;

	private HiRuntimeEnvironment env;

	private HiNative nativeObjects;

	public boolean isSystem() {
		return SYSTEM_CLASS_LOADER_NAME.equals(name);
	}

	public boolean isRoot() {
		return ROOT_CLASS_LOADER_NAME.equals(name);
	}

	private HiClassLoader getByName(String name) {
		if (this.name.equals(name)) {
			return this;
		}
		if (classLoaders != null) {
			for (HiClassLoader classLoader : classLoaders) {
				HiClassLoader matched = classLoader.getByName(name);
				if (matched != null) {
					return matched;
				}
			}
		}
		return null;
	}

	public HiRuntimeEnvironment getEnv() {
		return env;
	}

	public HiClass getClassClass(ClassResolver ctx) {
		if (classClass == null) {
			classClass = HiClass.forName(ctx, "Class");
			classConstructor = classClass.getConstructor(ctx);
		}
		classClass.init(ctx);
		return classClass;
	}

	public HiConstructor getClassConstructor(RuntimeContext ctx) {
		if (classClass == null) {
			classClass = HiClass.forName(ctx, "Class");
			classConstructor = classClass.getConstructor(ctx);
		}
		return classConstructor;
	}

	public HiObject getClassObject(RuntimeContext ctx, HiClass clazz) {
		HiObject classObject = classesObjects.get(clazz);
		if (classObject == null) {
			classObject = getClassConstructor(ctx).newInstance(ctx, null, null, null);
			if (classObject != null) {
				classObject.userObject = clazz;
				classesObjects.put(clazz, classObject);
			}
		}
		return classObject;
	}

	public synchronized void addClass(HiClass clazz, boolean isRuntime) {
		if (clazz.isGeneric()) {
			return;
		}

		HiClass currentClass = classes.get(clazz.fullName);
		if (currentClass != null) {
			if (isRuntime && currentClass != clazz) {
				throw new HiDuplicateClassException("cannot add class to class loader: another class with the same name '" + clazz.getNameDescr() + "' already loaded to '" + name + "'");
			} else {
				return;
			}
		}

		classes.put(clazz.fullName, clazz);
		clazz.classLoader = this;
	}

	public boolean validate(ValidationInfo validationInfo) {
		boolean valid = true;
		for (HiClass clazz : classes.values()) {
			valid &= checkCyclicDependencies(clazz, clazz, validationInfo);
		}
		return valid;
	}

	private boolean checkCyclicDependencies(HiClass origClazz, HiClass clazz, ValidationInfo validationInfo) {
		boolean valid = true;
		HiClass superClass = clazz.superClass != null ? clazz.superClass : clazz.superClassType != null ? getClass(clazz.superClassType.fullName) : null;
		if (superClass != null && !superClass.fullName.equals(HiClass.OBJECT_CLASS_NAME)) {
			if (superClass != origClazz) {
				valid &= checkCyclicDependencies(origClazz, superClass, validationInfo);
			} else {
				validationInfo.error("cyclic inheritance involving " + superClass.getNameDescr(), superClass.getToken());
				valid = false;
			}
		}

		if (clazz.interfaces != null) {
			for (HiClass i : clazz.interfaces) {
				if (i == null) {
					continue;
				}
				if (i != origClazz) {
					valid &= checkCyclicDependencies(origClazz, i, validationInfo);
				} else {
					validationInfo.error("cyclic inheritance involving " + i.getNameDescr(), i.getToken());
					valid = false;
				}
			}
		} else if (clazz.interfaceTypes != null) {
			for (Type it : clazz.interfaceTypes) {
				HiClass i = getClass(it.fullName);
				if (i == null) {
					continue;
				}
				if (i != origClazz) {
					valid &= checkCyclicDependencies(origClazz, i, validationInfo);
				} else {
					validationInfo.error("cyclic inheritance involving " + i.getNameDescr(), null);
					valid = false;
				}
			}
		}
		return valid;
	}

	public synchronized void addClasses(Collection<HiClass> classes) {
		for (HiClass clazz : classes) {
			HiClass currentClass = this.classes.get(clazz.fullName);
			if (currentClass != null) {
				if (currentClass != clazz) {
					throw new HiScriptRuntimeException("cannot add class to class loader: another class with the same name '" + clazz.getNameDescr() + "' already loaded to '" + name + "'");
				} else {
					return;
				}
			}
		}
		for (HiClass clazz : classes) {
			if (clazz.isGeneric()) {
				continue;
			}
			this.classes.put(clazz.fullName, clazz);
			clazz.classLoader = this;
		}
	}

	public synchronized boolean removeClass(HiClass clazz) {
		if (classes.get(clazz.fullName) == clazz) {
			classes.remove(clazz.fullName);
			clazz.classLoader = null;
			return true;
		}
		return false;
	}

	public synchronized void addClassLoader(HiClassLoader classLoader) {
		if (classLoader.parent != null && classLoader.parent != this) {
			throw new HiScriptRuntimeException("cannot add class loader");
		}
		if (classLoader.isSystem()) {
			throw new HiScriptRuntimeException("cannot add system class loader");
		}

		HiClassLoader parent = this;
		while (parent != null) {
			if (parent == classLoader) {
				throw new HiScriptRuntimeException("cannot add class loader: cyclic dependency");
			}
			parent = parent.parent;
		}

		if (classLoaders == null) {
			classLoaders = new ArrayList<>();
		}
		classLoader.parent = this;
		classLoaders.add(classLoader);
	}

	public synchronized boolean removeClassLoader(HiClassLoader classLoader) {
		if (classLoaders != null && classLoaders.remove(classLoader)) {
			classLoader.parent = null;
			return true;
		}
		return false;
	}

	public synchronized HiClass getClass(String name) {
		HiClassLoader parent = this.parent;
		boolean hasRoot = false;
		while (parent != null) {
			if (parent.isRoot()) {
				hasRoot = true;
			}
			HiClass clazz = parent.classes.get(name);
			if (clazz != null) {
				return clazz;
			}
			parent = parent.parent;
		}

		if (!hasRoot && !isRoot()) {
			HiClass clazz = systemClassLoader.classes.get(name);
			if (clazz != null) {
				return clazz;
			}
		}

		HiClass clazz = classes.get(name);
		if (clazz == null && classLoaders != null) {
			for (HiClassLoader classLoader : classLoaders) {
				clazz = classLoader.getClass(name);
				if (clazz != null) {
					break;
				}
			}
		}
		if (clazz == null) {
			clazz = systemClassLoader.classes.get(name);
		}
		if (clazz == null) {
			clazz = primitiveClassLoader.classes.get(name);
		}
		return clazz;
	}

	public String getName() {
		return name;
	}

	public List<HiClass> load(URL url) throws IOException, TokenizerException, HiScriptParseException, HiScriptValidationException {
		return load(url.openStream());
	}

	public List<HiClass> load(InputStream is) throws IOException, TokenizerException, HiScriptParseException, HiScriptValidationException {
		return load(ParserUtil.readString(is));
	}

	public List<HiClass> load(Reader r) throws IOException, TokenizerException, HiScriptParseException, HiScriptValidationException {
		return load(ParserUtil.readString(r));
	}

	public List<HiClass> load(String classCode) throws IOException, TokenizerException, HiScriptParseException, HiScriptValidationException {
		return load(classCode, true);
	}

	public List<HiClass> load(URL url, boolean validate) throws IOException, TokenizerException, HiScriptParseException, HiScriptValidationException {
		return load(url.openStream(), validate);
	}

	public List<HiClass> load(InputStream is, boolean validate) throws IOException, TokenizerException, HiScriptParseException, HiScriptValidationException {
		return load(ParserUtil.readString(is), validate);
	}

	public List<HiClass> load(String classCode, boolean validate) throws TokenizerException, HiScriptParseException, HiScriptValidationException {
		Tokenizer tokenizer = Tokenizer.getDefaultTokenizer(classCode);
		HiCompiler compiler = new HiCompiler(this, tokenizer);
		List<HiClass> classes = ClassFileParseRule.getInstance().visit(tokenizer, compiler);
		addClasses(classes);
		if (validate) {
			ValidationInfo validationInfo = new ValidationInfo(compiler);
			for (HiClass clazz : classes) {
				CompileClassContext ctx = new CompileClassContext(compiler, null, null, HiClass.CLASS_TYPE_TOP);
				clazz.validate(validationInfo, ctx);
			}
			validationInfo.throwExceptionIf();
		}
		return classes;
	}

	public HiNative getNative() {
		return nativeObjects;
	}

	public void clear() {
		clearClassLoaders();
		clearClasses();
	}

	public void clearClassLoaders() {
		if (classLoaders != null) {
			classLoaders.clear();
		}
	}

	public void clearClasses() {
		classes.clear();
		classesObjects.clear();
	}

	@Override
	public String toString() {
		return getName();
	}
}
