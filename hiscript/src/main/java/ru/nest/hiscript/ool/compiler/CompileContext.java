package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.NodeInitializer;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileContext {
	public CompileContext(HiCompiler compiler, HiClass enclosingClass, int classType) {
		this.compiler = compiler;
		this.tokenizer = compiler.getTokenizer();
		this.parent = null;
		this.enclosingClass = enclosingClass;
		this.classType = classType;
	}

	public CompileContext(CompileContext parent, HiClass enclosingClass, int classType) {
		this.compiler = parent.getCompiler();
		this.tokenizer = compiler.getTokenizer();
		this.parent = parent;
		this.enclosingClass = enclosingClass;
		this.classType = classType;
	}

	private HiCompiler compiler;

	private Tokenizer tokenizer;

	public HiClass clazz;

	public HiClass enclosingClass;

	public int classType;

	private CompileLevel level = new CompileLevel(null);

	public List<HiField<?>> fields = null;

	public Map<String, HiField<?>> fieldsMap = null;

	public List<HiClass> classes = null;

	public Map<String, HiClass> classesMap = null;

	public List<NodeInitializer> initializers = null;

	public List<HiMethod> methods = null;

	public List<HiConstructor> constructors = null;

	public List<HiEnumValue> enumValues = null;

	public CompileContext parent = null;

	public HiCompiler getCompiler() {
		return compiler;
	}

	public void addEnum(HiEnumValue enumValue) throws ParseException {
		if (enumValues == null) {
			enumValues = new ArrayList<>(1);
		}
		enumValues.add(enumValue);
	}

	public void addMethod(HiMethod method) throws ParseException {
		if (methods == null) {
			methods = new ArrayList<>(1);
		}
		methods.add(method);
	}

	public HiMethod[] getMethods() {
		HiMethod[] methodsArray = null;
		int size = methods != null ? methods.size() : 0;
		if (size > 0) {
			methodsArray = new HiMethod[size];
			methods.toArray(methodsArray);
		}
		return methodsArray;
	}

	public void addConstructor(HiConstructor constructor) throws ParseException {
		if (constructors == null) {
			constructors = new ArrayList<>(1);
		}
		constructors.add(constructor);
	}

	public HiConstructor[] getConstructors() {
		HiConstructor[] constructorsArray = null;
		int size = constructors != null ? constructors.size() : 0;
		if (size > 0) {
			constructorsArray = new HiConstructor[size];
			constructors.toArray(constructorsArray);
		}
		return constructorsArray;
	}

	public void addField(HiField<?> field) throws ParseException {
		if (fields == null) {
			fields = new ArrayList<>(1);
			fieldsMap = new HashMap<>(1);
		}

		if (initializers == null) {
			initializers = new ArrayList<>(1);
		}

		if (fieldsMap.containsKey(field.name)) {
			throw new ParseException("Duplicate field " + clazz.fullName + "." + field.name, tokenizer.currentToken());
		}

		fields.add(field);
		fieldsMap.put(field.name, field);
		initializers.add(field);
	}

	public HiField<?>[] getFields() {
		HiField<?>[] fieldsArray = null;
		int size = fields != null ? fields.size() : 0;
		if (size > 0) {
			fieldsArray = new HiField<?>[size];
			fields.toArray(fieldsArray);
		}
		return fieldsArray;
	}

	public void addClass(HiClass clazz) throws ParseException {
		if (classes == null) {
			classes = new ArrayList<>(1);
			classesMap = new HashMap<>(1);
		}

		if (classesMap.containsKey(clazz.name)) {
			throw new ParseException("Duplicate nested type " + clazz.name, tokenizer.currentToken());
		}

		classes.add(clazz);
		classesMap.put(clazz.name, clazz);
	}

	public HiClass[] getClasses() {
		HiClass[] classesArray = null;
		int size = classes != null ? classes.size() : 0;
		if (size > 0) {
			classesArray = new HiClass[size];
			classes.toArray(classesArray);
		}
		return classesArray;
	}

	public void addBlockInitializer(NodeInitializer block) {
		if (initializers == null) {
			initializers = new ArrayList<>(1);
		}
		initializers.add(block);
	}

	public NodeInitializer[] getInitializers() {
		NodeInitializer[] initializersArray = null;
		int size = initializers != null ? initializers.size() : 0;
		if (size > 0) {
			initializersArray = new NodeInitializer[size];
			initializers.toArray(initializersArray);
		}
		return initializersArray;
	}

	public void initClass() {
		clazz.classes = getClasses();
		clazz.constructors = getConstructors();
		clazz.methods = getMethods();
		clazz.fields = getFields();
		clazz.initializers = getInitializers();
		if (clazz instanceof HiClassEnum) {
			((HiClassEnum) clazz).enumValues = enumValues;
		}
	}

	public void enter() {
		level = new CompileLevel(level);
	}

	public void exit() {
		level = level.parent;
	}

	public void addLocalClass(HiClass clazz) throws ParseException {
		if (getLocalClass(clazz.name) != null) {
			throw new ParseException("Duplicated nested type " + clazz.fullName, tokenizer.currentToken());
		}
		level.addClass(clazz);
	}

	public HiClass getLocalClass(String name) {
		CompileLevel level = this.level;
		while (level != null) {
			HiClass clazz = level.getClass(name);
			if (clazz != null) {
				return clazz;
			}
			level = level.parent;
		}
		return null;
	}

	public void addLocalVariable(NodeVariable localVariable) throws ParseException {
		if (getLocalVariable(localVariable.getVariableName()) != null) {
			throw new ParseException("Duplicated local variable " + localVariable.getVariableName(), tokenizer.currentToken());
		}
		level.addField(localVariable);
	}

	public NodeVariable getLocalVariable(String name) {
		CompileLevel level = this.level;
		while (level != null) {
			NodeVariable localVariable = level.getField(name);
			if (localVariable != null) {
				return localVariable;
			}
			level = level.parent;
		}
		return null;
	}

	class CompileLevel {
		Map<String, HiClass> classes = null;

		Map<String, NodeVariable> localVariables = null;

		int deep;

		CompileLevel parent;

		CompileLevel child;

		public CompileLevel(CompileLevel parent) {
			setParent(parent);
		}

		public void setParent(CompileLevel parent) {
			this.parent = parent;
			if (parent != null) {
				parent.child = this;
				deep = parent.deep + 1;
			} else {
				deep = 0;
			}
		}

		public void addClass(HiClass clazz) {
			if (classes == null) {
				classes = new HashMap<>(1);
			}
			classes.put(clazz.name, clazz);
		}

		public HiClass getClass(String name) {
			return classes != null ? classes.get(name) : null;
		}

		public void addField(NodeVariable localVariable) {
			if (localVariables == null) {
				localVariables = new HashMap<>(1);
			}
			localVariables.put(localVariable.getVariableName(), localVariable);
		}

		public NodeVariable getField(String name) {
			return localVariables != null ? localVariables.get(name) : null;
		}

		public void clear() {
			deep = 0;
			parent = null;
			child = null;
			if (classes != null) {
				classes.clear();
			}
			if (localVariables != null) {
				localVariables.clear();
			}
		}
	}
}
