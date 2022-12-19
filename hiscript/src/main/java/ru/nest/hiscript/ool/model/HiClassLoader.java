package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compiler.ClassFileParseRule;
import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.compiler.ParserUtil;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Tokenizer;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
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
		if (clazz.classLoader != null) {
			throw new RuntimeException("can't add loaded class to class loader '" + name + "': " + clazz.fullName);
		}
		if (classes.containsKey(clazz.fullName)) {
			throw new RuntimeException("can't add class to class loader '" + name + "': another class with name " + clazz.fullName + " already loaded");
		}
		clazz.classLoader = this;
		classes.put(clazz.fullName, clazz);
	}

	public synchronized boolean removeClass(HiClass clazz) {
		if (classes.get(clazz.name) == clazz) {
			classes.remove(clazz.name);
			clazz.classLoader = null;
			return true;
		}
		return false;
	}

	public synchronized void addClassLoader(HiClassLoader classLoader) {
		if (classLoader.parent != null) {
			throw new RuntimeException("can't add class loader");
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
		HiCompiler compiler = new HiCompiler(tokenizer);
		List<HiClass> classes = ClassFileParseRule.getInstance().visit(tokenizer, compiler);
		if (validate) {
			ValidationInfo validationInfo = new ValidationInfo(compiler);
			for (HiClass clazz : classes) {
				CompileClassContext ctx = new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP);
				ctx.isRegisterClass = true;
				clazz.validate(validationInfo, ctx);
				addClass(clazz);
			}
			validationInfo.throwExceptionIf();
		}
		return classes;
	}

	@Override
	public String toString() {
		return getName();
	}
}
