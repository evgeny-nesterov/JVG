package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.ClassFileParseRule;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Tokenizer;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HiClassLoader {
	private String name;

	private HiClassLoader parent;

	private List<HiClassLoader> classLoaders;

	private Map<String, HiClass> classes = new ConcurrentHashMap<>();

	public HiClassLoader(String name) {
		this.name = name;
	}

	public HiClassLoader(String name, HiClassLoader parent) {
		this(name);
		if (parent != null) {
			parent.addClassLoader(this);
		}
	}

	public synchronized void addClass(HiClass clazz) {
		HiClass currentClass = classes.get(clazz.fullName);
		if (currentClass != null) {
			if (currentClass != clazz) {
				throw new RuntimeException("can't add class to class loader: another class with the same name '" + clazz.fullName + "' already loaded to '" + name + "'");
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
		if (superClass != null) {
			if (superClass != origClazz) {
				valid &= checkCyclicDependencies(origClazz, superClass, validationInfo);
			} else {
				validationInfo.error("cyclic inheritance involving " + superClass, superClass.getToken());
				valid = false;
			}
		}

		if (clazz.interfaces != null) {
			for (HiClass i : clazz.interfaces) {
				if (i != origClazz) {
					valid &= checkCyclicDependencies(origClazz, i, validationInfo);
				} else {
					validationInfo.error("cyclic inheritance involving " + i.fullName, i.getToken());
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
					validationInfo.error("cyclic inheritance involving " + i.fullName, null);
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
					throw new RuntimeException("can't add class to class loader: another class with the same name '" + clazz.fullName + "' already loaded to '" + name + "'");
				} else {
					return;
				}
			}
		}
		for (HiClass clazz : classes) {
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
		if (classLoader.parent != null) {
			throw new RuntimeException("can't add class loader");
		}
		if (classLoader == HiClass.systemClassLoader) {
			throw new RuntimeException("can't add system class loader");
		}

		HiClassLoader parent = this;
		while (parent != null) {
			if (parent == classLoader) {
				throw new RuntimeException("can't add class loader: cyclic dependency");
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
		boolean hasSystem = false;
		while (parent != null) {
			if (parent == HiClass.systemClassLoader) {
				hasSystem = true;
			}
			HiClass clazz = parent.classes.get(name);
			if (clazz != null) {
				return clazz;
			}
			parent = parent.parent;
		}

		if (!hasSystem && this != HiClass.systemClassLoader) {
			HiClass clazz = HiClass.systemClassLoader.classes.get(name);
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
		return clazz;
	}

	public String getName() {
		return name;
	}

	public List<HiClass> load(URL url) throws Exception {
		return load(url.openStream());
	}

	public List<HiClass> load(InputStream is) throws Exception {
		return load(ParserUtil.readString(is));
	}

	public List<HiClass> load(Reader r) throws Exception {
		return load(ParserUtil.readString(r));
	}

	public List<HiClass> load(String classCode) throws Exception {
		return load(classCode, true);
	}

	public List<HiClass> load(URL url, boolean validate) throws Exception {
		return load(url.openStream(), validate);
	}

	public List<HiClass> load(InputStream is, boolean validate) throws Exception {
		return load(ParserUtil.readString(is), validate);
	}

	public List<HiClass> load(String classCode, boolean validate) throws Exception {
		Tokenizer tokenizer = Tokenizer.getDefaultTokenizer(classCode);
		HiCompiler compiler = new HiCompiler(this, tokenizer);
		List<HiClass> classes = ClassFileParseRule.getInstance().visit(tokenizer, compiler);
		addClasses(classes);
		if (validate) {
			ValidationInfo validationInfo = new ValidationInfo(compiler);
			for (HiClass clazz : classes) {
				CompileClassContext ctx = new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP);
				clazz.validate(validationInfo, ctx);
			}
			validationInfo.throwExceptionIf();
		}
		return classes;
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
	}

	@Override
	public String toString() {
		return getName();
	}
}
