package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.NodeInitializer;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileClassContext {
	public CompileClassContext(HiCompiler compiler, HiClass enclosingClass, int classType) {
		this.compiler = compiler;
		this.tokenizer = compiler.getTokenizer();
		this.parent = null;
		this.enclosingClass = enclosingClass;
		this.classType = classType;
	}

	public CompileClassContext(CompileClassContext parent, HiClass enclosingClass, int classType) {
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

	private CompileClassLevel level = new CompileClassLevel(RuntimeContext.BLOCK, null);

	public List<HiField<?>> fields = null;

	public Map<String, HiField<?>> fieldsMap = null;

	public List<HiClass> classes = null;

	public Map<String, HiClass> classesMap = null;

	public List<NodeInitializer> initializers = null;

	public List<HiMethod> methods = null;

	public List<HiConstructor> constructors = null;

	public List<HiEnumValue> enumValues = null;

	public CompileClassContext parent;

	public HiCompiler getCompiler() {
		return compiler;
	}

	public void addEnum(HiEnumValue enumValue) {
		if (enumValues == null) {
			enumValues = new ArrayList<>(1);
		}
		enumValues.add(enumValue);
	}

	public void addMethod(HiMethod method) {
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

	public void enter(int type) {
		level = new CompileClassLevel(type, level);
	}

	public void exit() {
		level = level.parent;
	}

	public boolean addLocalClass(HiClass clazz, ValidationInfo validationInfo) {
		boolean valid = true;
		if (getLocalClass(clazz.name) != null) {
			validationInfo.error("Duplicated nested type " + clazz.fullName, clazz.token);
			valid = false;
		}
		level.addClass(clazz);
		return valid;
	}

	public HiClass getLocalClass(String name) {
		CompileClassLevel level = this.level;
		while (level != null) {
			HiClass clazz = level.getClass(name);
			if (clazz != null) {
				return clazz;
			}
			if (level.type == RuntimeContext.STATIC_CLASS || level.type == RuntimeContext.METHOD || level.type == RuntimeContext.CONSTRUCTOR) {
				break;
			}
			level = level.parent;
		}
		return null;
	}

	public HiClass getClass(String name) {
		int dimension = 0;
		while (name.charAt(dimension) == '0') {
			dimension++;
		}
		String baseName = dimension == 0 ? name : name.substring(dimension);
		HiClass baseClass = getBaseClass(baseName);
		return dimension == 0 ? baseClass : HiClassArray.getArrayClass(baseClass, dimension);
	}

	private HiClass getBaseClass(String name) {
		CompileClassLevel level = this.level;
		while (level != null) {
			HiClass clazz = level.getClass(name);
			if (clazz != null) {
				return clazz;
			}
			level = level.parent;
		}
		return HiClass.forName(null, name);
	}

	public boolean addLocalVariable(NodeVariable localVariable, ValidationInfo validationInfo) {
		boolean valid = true;
		if (getLocalVariable(localVariable.getVariableName()) != null) {
			validationInfo.error("Duplicated local variable " + localVariable.getVariableName(), ((Node) localVariable).getToken());
			valid = false;
		}
		level.addField(localVariable);
		return valid;
	}

	public NodeVariable getLocalVariable(String name) {
		CompileClassLevel level = this.level;
		while (level != null) {
			NodeVariable localVariable = level.getField(name);
			if (localVariable != null) {
				return localVariable;
			}
			if (level.type == RuntimeContext.STATIC_CLASS || level.type == RuntimeContext.METHOD || level.type == RuntimeContext.CONSTRUCTOR) {
				break;
			}
			level = level.parent;
		}
		return null;
	}

	public NodeVariable getVariable(String name) {
		CompileClassLevel level = this.level;
		while (level != null) {
			NodeVariable localVariable = level.getField(name);
			if (localVariable != null) {
				return localVariable;
			}
			level = level.parent;
		}
		return null;
	}

	public Object resolveIdentifier(String name) {
		CompileClassLevel level = this.level;
		while (level != null) {
			NodeVariable localVariable = level.getField(name);
			if (localVariable != null) {
				return localVariable;
			}
			HiClass clazz = level.getClass(name);
			if (clazz != null) {
				return clazz;
			}
			level = level.parent;
		}
		return HiClass.forName(null, name);
	}

	static class CompileClassLevel {
		int type;

		Map<String, HiClass> classes = null;

		Map<String, NodeVariable> localVariables = null;

		int deep;

		CompileClassLevel parent;

		CompileClassLevel child;

		public CompileClassLevel(int type, CompileClassLevel parent) {
			this.type = type;
			setParent(parent);
		}

		public void setParent(CompileClassLevel parent) {
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
			if (classes != null) {
				if (name.indexOf('.') != -1) {
					String[] path = name.split("\\.");
					HiClass clazz = classes.get(path[0]);
					for (int i = 1; i < path.length && clazz != null; i++) {
						clazz = clazz.getChild(null, path[i]);
					}
					return clazz;
				} else {
					return classes.get(name);
				}
			}
			return null;
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