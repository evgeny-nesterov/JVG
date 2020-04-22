package ru.nest.hiscript.ool.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Constructor;
import ru.nest.hiscript.ool.model.Field;
import ru.nest.hiscript.ool.model.Method;
import ru.nest.hiscript.ool.model.NodeInitializer;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.tokenizer.Tokenizer;

public class CompileContext {
	public CompileContext(Tokenizer tokenizer, CompileContext parent, Clazz enclosingClass, int classType) {
		this.tokenizer = tokenizer;
		this.parent = parent;
		this.enclosingClass = enclosingClass;
		this.classType = classType;
	}

	private Tokenizer tokenizer;

	public Clazz clazz;

	public Clazz enclosingClass;

	public int classType;

	private CompileLevel level = new CompileLevel(null);

	public List<Field<?>> fields = null;

	public Map<String, Field<?>> fieldsMap = null;

	public List<Clazz> classes = null;

	public Map<String, Clazz> classesMap = null;

	public List<NodeInitializer> initializers = null;

	public List<Method> methods = null;

	public List<Constructor> constructors = null;

	public CompileContext parent = null;

	public void addMethod(Method method) throws ParseException {
		if (methods == null) {
			methods = new ArrayList<Method>(1);
		}
		methods.add(method);
	}

	public Method[] getMethods() {
		Method[] methodsArray = null;
		int size = methods != null ? methods.size() : 0;
		if (size > 0) {
			methodsArray = new Method[size];
			methods.toArray(methodsArray);
		}
		return methodsArray;
	}

	public void addConstructor(Constructor constructor) throws ParseException {
		if (constructors == null) {
			constructors = new ArrayList<Constructor>(1);
		}
		constructors.add(constructor);
	}

	public Constructor[] getConstructors() {
		Constructor[] constructorsArray = null;
		int size = constructors != null ? constructors.size() : 0;
		if (size > 0) {
			constructorsArray = new Constructor[size];
			constructors.toArray(constructorsArray);
		}
		return constructorsArray;
	}

	public void addField(Field<?> field) throws ParseException {
		if (fields == null) {
			fields = new ArrayList<Field<?>>(1);
			fieldsMap = new HashMap<String, Field<?>>(1);
		}

		if (initializers == null) {
			initializers = new ArrayList<NodeInitializer>(1);
		}

		if (fieldsMap.containsKey(field.name)) {
			throw new ParseException("Dublicate field " + clazz.fullName + "." + field.name, tokenizer.currentToken());
		}

		fields.add(field);
		fieldsMap.put(field.name, field);
		initializers.add(field);
	}

	public Field<?>[] getFields() {
		Field<?>[] fieldsArray = null;
		int size = fields != null ? fields.size() : 0;
		if (size > 0) {
			fieldsArray = new Field<?>[size];
			fields.toArray(fieldsArray);
		}
		return fieldsArray;
	}

	public void addClass(Clazz clazz) throws ParseException {
		if (classes == null) {
			classes = new ArrayList<Clazz>(1);
			classesMap = new HashMap<String, Clazz>(1);
		}

		if (classesMap.containsKey(clazz.name)) {
			throw new ParseException("Dublicate nested type " + clazz.name, tokenizer.currentToken());
		}

		classes.add(clazz);
		classesMap.put(clazz.name, clazz);
	}

	public Clazz[] getClasses() {
		Clazz[] classesArray = null;
		int size = classes != null ? classes.size() : 0;
		if (size > 0) {
			classesArray = new Clazz[size];
			classes.toArray(classesArray);
		}
		return classesArray;
	}

	public void addBlockInitializer(NodeInitializer block) {
		if (initializers == null) {
			initializers = new ArrayList<NodeInitializer>(1);
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
	}

	public void enter() {
		level = new CompileLevel(level);
	}

	public void exit() {
		level = level.parent;
	}

	public void addLocalClass(Clazz clazz) throws ParseException {
		if (getLocalClass(clazz.name) != null) {
			throw new ParseException("Dublicate nested type " + clazz.fullName, tokenizer.currentToken());
		}
		level.addClass(clazz);
	}

	public Clazz getLocalClass(String name) {
		CompileLevel level = this.level;
		while (level != null) {
			Clazz clazz = level.getClass(name);
			if (clazz != null) {
				return clazz;
			}
			level = level.parent;
		}
		return null;
	}

	public void addLocalVariable(NodeVariable localVariable) throws ParseException {
		if (getLocalVariable(localVariable.getVariableName()) != null) {
			throw new ParseException("Dublicate local variable " + localVariable.getVariableName(), tokenizer.currentToken());
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
		Map<String, Clazz> classes = null;

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

		public void addClass(Clazz clazz) {
			if (classes == null) {
				classes = new HashMap<String, Clazz>(1);
			}
			classes.put(clazz.name, clazz);
		}

		public Clazz getClass(String name) {
			return classes != null ? classes.get(name) : null;
		}

		public void addField(NodeVariable localVariable) {
			if (localVariables == null) {
				localVariables = new HashMap<String, NodeVariable>(1);
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
