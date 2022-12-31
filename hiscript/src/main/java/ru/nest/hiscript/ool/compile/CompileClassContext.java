package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.NodeInitializer;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.TokenAccessible;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompileClassContext implements ClassResolver {
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

	public CompileClassLevel level = new CompileClassLevel(RuntimeContext.BLOCK, null, null);

	public List<HiField<?>> fields = null;

	public List<HiClass> classes = null;

	public Map<String, HiClass> classesMap = null;

	public List<NodeInitializer> initializers = null;

	public List<HiMethod> methods = null;

	public List<HiConstructor> constructors = null;

	public List<HiEnumValue> enumValues = null;

	public CompileClassContext parent;

	public Set<HiNode> initializedNodes = new HashSet<>();

	public NodeValueType nodeValueType = new NodeValueType();

	private List<NodeValueType[]> nodesValueTypesCache = new ArrayList<>();

	public NodeValueType[] getNodesValueTypesCache(int size) {
		if (this.nodesValueTypesCache.size() > 0) {
			NodeValueType[] nodesValueTypesCache = this.nodesValueTypesCache.remove(this.nodesValueTypesCache.size() - 1);
			if (size > nodesValueTypesCache.length) {
				NodeValueType[] newNodesValueTypesCache = new NodeValueType[size];
				int currentSize = nodesValueTypesCache.length;
				System.arraycopy(nodesValueTypesCache, 0, newNodesValueTypesCache, 0, currentSize);
				for (int i = currentSize; i < size; i++) {
					newNodesValueTypesCache[i] = new NodeValueType();
				}
				return newNodesValueTypesCache;
			} else {
				return nodesValueTypesCache;
			}
		}
		NodeValueType[] nodesValueTypesCache = new NodeValueType[size];
		for (int i = 0; i < size; i++) {
			nodesValueTypesCache[i] = new NodeValueType();
		}
		return nodesValueTypesCache;
	}

	public void putNodesValueTypesCache(NodeValueType[] nodesValueTypesCache) {
		this.nodesValueTypesCache.add(nodesValueTypesCache);
	}

	@Override
	public HiCompiler getCompiler() {
		return compiler;
	}

	@Override
	public HiClassLoader getClassLoader() {
		return compiler.getClassLoader();
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

	public void addConstructor(HiConstructor constructor) {
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

	public void addField(HiField<?> field) {
		if (fields == null) {
			fields = new ArrayList<>(1);
		}

		if (initializers == null) {
			initializers = new ArrayList<>(1);
		}

		fields.add(field);
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

	public void addClass(HiClass clazz) throws TokenizerException {
		if (classes == null) {
			classes = new ArrayList<>(1);
			classesMap = new HashMap<>(1);
		}

		if (classesMap.containsKey(clazz.name)) {
			tokenizer.error("Duplicate nested type " + clazz.name);
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
		clazz.innerClasses = getClasses();
		clazz.constructors = getConstructors();
		clazz.methods = getMethods();
		clazz.fields = getFields();
		clazz.initializers = getInitializers();
		if (clazz instanceof HiClassEnum) {
			((HiClassEnum) clazz).enumValues = enumValues;
		}
	}

	public void enter(int type, TokenAccessible node) {
		level = new CompileClassLevel(type, node, level);
	}

	public void enterLabel(String label, TokenAccessible node) {
		level = new CompileClassLevel(RuntimeContext.LABEL, node, level);
		level.label = label;
	}

	public void enterObject(HiClass enclosingClass, boolean isEnclosingObject) {
		level = new CompileClassLevel(RuntimeContext.OBJECT, enclosingClass, level);
		level.enclosingClass = enclosingClass;
		level.isEnclosingObject = isEnclosingObject;
	}

	public void exit() {
		level = level.parent;
	}

	public HiClass consumeInvocationClass() {
		HiClass clazz = this.level.enclosingClass;
		this.level.enclosingClass = null;
		return clazz;
	}

	public boolean addLocalClass(HiClass clazz) {
		boolean valid = true;
		if (getLocalClass(clazz.name) != null) {
			compiler.getValidationInfo().error("Duplicated nested type " + clazz.fullName, clazz.token);
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
			if (level.type == RuntimeContext.METHOD || level.type == RuntimeContext.CONSTRUCTOR || level.type == RuntimeContext.STATIC_CLASS) {
				break;
			}
			level = level.parent;
		}
		return null;
	}

	@Override
	public HiClass getLocalClass(HiClass enclosingClass, String name) {
		CompileClassLevel level = this.level;
		while (level != null) {
			HiClass localClass = level.getLocalClass(enclosingClass, name);
			if (localClass != null) {
				return localClass;
			}
			level = level.parent;
		}
		return null;
	}

	@Override
	public HiClass getClass(String name) {
		int dimension = 0;
		while (name.charAt(dimension) == '0') {
			dimension++;
		}
		String baseName = dimension == 0 ? name : name.substring(dimension);
		HiClass baseClass = (HiClass) resolveIdentifier(name, true, false, false);
		return dimension == 0 ? baseClass : baseClass.getArrayClass(dimension);
	}

	@Override
	public void processResolverException(String message) {
		compiler.getValidationInfo().error(message, getCurrentToken());
	}

	public Token getCurrentToken() {
		CompileClassLevel l = level;
		while (l != null) {
			if (l.node != null && l.node.getToken() != null) {
				return l.node.getToken();
			}
			l = l.parent;
		}
		return null;
	}

	public boolean addLocalVariable(NodeVariable localVariable) {
		boolean valid = true;
		if (hasLocalVariable(localVariable.getVariableName())) {
			compiler.getValidationInfo().error("Duplicated local variable " + localVariable.getVariableName(), ((HiNode) localVariable).getToken());
			valid = false;
		}
		level.addField(localVariable);
		return valid;
	}

	public boolean hasLocalVariable(String name) {
		return resolveIdentifier(name, false, true, true) != null;
	}

	public Object resolveIdentifier(String name) {
		return resolveIdentifier(name, true, true, false);
	}

	public Object resolveIdentifier(String name, boolean resolveClass, boolean resolveVariable, boolean onlyLocal) {
		CompileClassLevel level = this.level;
		while (level != null) {
			if (resolveVariable) {
				NodeVariable variable = level.getField(name);
				if (variable != null) {
					return variable;
				}
			}
			if (resolveClass) {
				HiClass clazz = level.getClass(name);
				if (clazz != null) {
					return clazz;
				}
			}
			if (level.type == RuntimeContext.METHOD || level.type == RuntimeContext.CONSTRUCTOR || level.type == RuntimeContext.STATIC_CLASS) {
				break;
			}
			level = level.parent;
		}
		if (onlyLocal) {
			return null;
		}

		if (resolveClass) {
			if (this.clazz != null) {
				if ((this.clazz.name.equals(name) || this.clazz.fullName.equals(name))) {
					return this.clazz;
				}

				HiClass outerClass = this.clazz;
				while (outerClass != null) {
					HiClass innerClass = outerClass.getInnerClass(this, name);
					if (innerClass != null) {
						return innerClass;
					}
					outerClass = outerClass.enclosingClass;
				}
			}
		}

		if (parent != null) {
			Object resolvedIdentifier = parent.resolveIdentifier(name, resolveClass, resolveVariable, false);
			if (resolvedIdentifier != null) {
				return resolvedIdentifier;
			}
		}

		while (level != null) {
			if (resolveVariable) {
				NodeVariable variable = level.getField(name);
				if (variable != null) {
					return variable;
				}
			}
			if (resolveClass) {
				HiClass clazz = level.getClass(name);
				if (clazz != null) {
					return clazz;
				}
			}
			level = level.parent;
		}

		if (resolveClass) {
			if (this.clazz != null && name.indexOf('$') == -1) {
				int index = this.clazz.fullName.lastIndexOf('$');
				if (index != -1) {
					String outboundClassName = this.clazz.fullName.substring(0, index + 1);
					String extendedName = outboundClassName + '0' + name;
					HiClass clazz = HiClass.forName(this, extendedName);
					if (clazz != null) {
						return clazz;
					}
				}
			}
			return HiClass.forName(this, name);
		}
		return null;
	}

	public HiMethod resolveMethod(String name, HiClass... argsClasses) {
		if (level.enclosingClass != null) {
			return level.enclosingClass.searchMethod(this, name, argsClasses);
		}
		if (this.clazz != null) {
			HiClass outerClass = this.clazz;
			while (outerClass != null) {
				HiMethod method = outerClass.searchMethod(this, name, argsClasses);
				if (method != null) {
					return method;
				}
				outerClass = outerClass.enclosingClass;
			}
		}
		if (parent != null) {
			return parent.resolveMethod(name, argsClasses);
		}
		return null;
	}

	public class CompileClassLevel {
		public int type;

		Map<String, HiClass> classes = null;

		Map<String, NodeVariable> localVariables = null;

		int deep;

		public CompileClassLevel parent;

		CompileClassLevel child;

		public HiClass enclosingClass;

		public boolean isEnclosingObject;

		public TokenAccessible node;

		String label;

		public CompileClassLevel(int type, TokenAccessible node, CompileClassLevel parent) {
			this.type = type;
			this.node = node;
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

		public boolean isLabel(String label) {
			return type == RuntimeContext.LABEL && label.equals(this.label);
		}

		public HiClass getClass(String name) {
			if (enclosingClass != null) {
				HiClass localClass = enclosingClass.getClass(CompileClassContext.this, name);
				if (localClass != null) {
					return localClass;
				}
			}
			if (classes != null) {
				if (name.indexOf('.') != -1) {
					String[] path = name.split("\\.");
					HiClass clazz = classes.get(path[0]);
					for (int i = 1; i < path.length && clazz != null; i++) {
						clazz = clazz.getInnerClass(CompileClassContext.this, path[i]);
					}
					return clazz;
				} else {
					return classes.get(name);
				}
			}
			return null;
		}

		public HiClass getLocalClass(HiClass enclosingClass, String name) {
			if (classes != null) {
				HiClass clazz;
				if (name.indexOf('.') != -1) {
					String[] path = name.split("\\.");
					clazz = classes.get(path[0]);
					for (int i = 1; i < path.length && clazz != null; i++) {
						clazz = clazz.getInnerClass(CompileClassContext.this, path[i]);
					}
				} else {
					clazz = classes.get(name);
				}
				if (clazz != null && clazz.enclosingClass == enclosingClass) {
					return clazz;
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
			if (enclosingClass != null) {
				if (enclosingClass instanceof HiClassEnum) {
					HiClassEnum classEnum = (HiClassEnum) enclosingClass;
					classEnum.init(CompileClassContext.this);
					HiField enumField = classEnum.getEnumValue(name);
					if (enumField != null) {
						return enumField;
					}
				}

				HiField field = enclosingClass.getField(CompileClassContext.this, name);
				if (field != null) {
					return field;
				}
			}
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

	public boolean isStaticContext() {
		if (clazz != null && !clazz.isStatic()) {
			return false;
		}
		CompileClassLevel level = this.level;
		while (level != null) {
			if (level.type == RuntimeContext.METHOD) {
				return ((HiMethod) level.node).modifiers.isStatic();
			} else if (level.type == RuntimeContext.INITIALIZATION) {
				return ((NodeBlock) level.node).isStatic();
			}
			level = level.parent;
		}
		if (parent != null) {
			return parent.isStaticContext();
		}
		return false;
	}

	public String getTokenText(Token token) {
		return token != null ? tokenizer.getText(token) : null;
	}
}
