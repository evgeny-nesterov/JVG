package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ool.model.ClassLocationType;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.NodeInitializer;
import ru.nest.hiscript.ool.model.TokenAccessible;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.nest.hiscript.ool.model.ContextType.*;
import static ru.nest.hiscript.ool.model.nodes.NodeVariable.*;

public class CompileClassContext implements ClassResolver {
	public CompileClassContext(HiCompiler compiler, HiClass enclosingClass, Type enclosingType, ClassLocationType classLocationType) {
		this.compiler = compiler;
		this.tokenizer = compiler.getTokenizer();
		this.parent = null;
		this.enclosingClass = enclosingClass;
		this.enclosingType = enclosingType;
		this.classLocationType = classLocationType;
	}

	public CompileClassContext(CompileClassContext parent, HiClass enclosingClass, Type enclosingType, ClassLocationType classLocationType) {
		this.compiler = parent.getCompiler();
		this.tokenizer = compiler.getTokenizer();
		this.parent = parent;
		this.enclosingClass = enclosingClass;
		this.enclosingType = enclosingType;
		this.classLocationType = classLocationType;
	}

	// for script execution in runtime context
	public CompileClassContext(RuntimeContext ctx) {
		this.runtimeCtx = ctx;
		this.compiler = ctx.compiler;
		this.tokenizer = ctx.compiler.getTokenizer();
		this.parent = null;
		this.enclosingClass = ctx.level.clazz;
		this.enclosingType = ctx.level.type;
		this.classLocationType = ClassLocationType.top;
	}

	private final HiCompiler compiler;

	private final Tokenizer tokenizer;

	public HiClass clazz;

	public Type type;

	public HiClass enclosingClass;

	public Type enclosingType;

	public ClassLocationType classLocationType;

	public TokenAccessible currentNode;

	public CompileClassLevel level = new CompileClassLevel(BLOCK, null, null);

	public List<HiField<?>> fields = null;

	public List<HiClass> classes = null;

	public Map<String, HiClass> classesMap = null;

	public List<NodeInitializer> initializers = null;

	public List<HiMethod> methods = null;

	public List<HiConstructor> constructors = null;

	public List<HiEnumValue> enumValues = null;

	public CompileClassContext parent;

	public final Set<HiNodeIF> initializedNodes = new HashSet<>();

	public final NodeValueType nodeValueType = new NodeValueType();

	private final List<NodeValueType[]> nodesValueTypesCache = new ArrayList<>();

	public NodeValueType invocationNode;

	public final Set<String> breaksLabels = new HashSet<>();

	public final AtomicInteger lambdasCount = new AtomicInteger();

	public RuntimeContext runtimeCtx;

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
	public HiRuntimeEnvironment getEnv() {
		return compiler.getClassLoader().getEnv();
	}

	@Override
	public HiClassLoader getClassLoader() {
		return compiler.getClassLoader();
	}

	@Override
	public HiClass getCurrentClass() {
		return clazz;
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
		// rewrite record methods
		if (clazz != null && clazz.isRecord()) {
			for (int i = 0; i < methods.size(); i++) {
				if (method.name.equals(methods.get(i).name) && ((method.name.startsWith("get") && method.hasArguments(0)) || (method.name.startsWith("set") && method.hasArguments(1)))) {
					boolean match;
					if (method.name.startsWith("set")) {
						StringBuilder fieldNameBuf = new StringBuilder(method.name.length() - 3).append(Character.toLowerCase(method.name.charAt(3)));
						if (method.name.length() > 4) {
							fieldNameBuf.append(method.name.substring(4));
						}
						String fieldName = fieldNameBuf.toString();
						HiField<?> field = null;
						for (HiField<?> f : fields) {
							if (f.name.equals(fieldName)) {
								field = f;
								break;
							}
						}
						Type fieldType = field.type;
						match = method.arguments[0].typeArgument.equals(fieldType);
					} else {
						match = true;
					}
					if (match) {
						methods.set(i, method);
						return;
					}
				}
			}
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
			tokenizer.error("duplicate nested type " + clazz.name);
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

	public void enter(ContextType type, TokenAccessible node) {
		level = new CompileClassLevel(type, node, level);
	}

	public void enterLabel(String label, TokenAccessible node) {
		level = new CompileClassLevel(LABEL, node, level);
		level.label = label;
	}

	public void enterDeclaration(HiClass declarationClass, Type declarationType) {
		level.declarationClass = declarationClass;
		level.declarationType = declarationType;
	}

	public void exitDeclaration() {
		level.declarationClass = null;
		level.declarationType = null;
	}

	public void enterObject(HiClass enclosingClass, Type enclosingType, boolean isEnclosingObject) {
		level = new CompileClassLevel(OBJECT, enclosingClass, level);
		setObjectContext(enclosingClass, enclosingType, isEnclosingObject);
	}

	public void setObjectContext(HiClass enclosingClass, Type enclosingType, boolean isEnclosingObject) {
		level.enclosingClass = enclosingClass;
		level.enclosingType = enclosingType;
		level.isEnclosingObject = isEnclosingObject;
	}

	public void clearObjectContext() {
		level.enclosingClass = null;
		level.enclosingType = null;
		level.isEnclosingObject = false;
	}

	public void exit() {
		level = level.parent;
		if (level != null) {
			currentNode = level.node;
		}
	}

	public HiClass consumeInvocationClass() {
		HiClass clazz = this.level.enclosingClass;
		this.level.enclosingClass = null;
		this.level.enclosingType = null;
		return clazz;
	}

	public boolean addLocalClass(HiClass clazz) {
		boolean valid = true;
		if (getLocalClass(clazz.name) != null) {
			compiler.getValidationInfo().error("duplicated nested type " + clazz.getNameDescr(), clazz);
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
			if (level.type == METHOD || level.type == CONSTRUCTOR || level.type == INITIALIZATION || level.type == STATIC_CLASS) {
				break;
			}
			level = level.parent;
		}
		return null;
	}

	public HiClass getUniqueClass(String name, HiClass currentClass) {
		CompileClassLevel level = this.level;
		while (level != null) {
			if (level.classes != null) {
				HiClass clazz = level.classes.get(name);
				if (clazz != null) {
					return clazz;
				}
			}
			if (level.type == METHOD || level.type == CONSTRUCTOR || level.type == INITIALIZATION || level.type == STATIC_CLASS) {
				break;
			}
			level = level.parent;
		}
		HiClass outerClass = this.clazz;
		while (outerClass != null) {
			if ((outerClass.name.equals(name) || outerClass.fullName.equals(name))) {
				return this.clazz;
			}
			outerClass = outerClass.enclosingClass;
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
		if (name.length() == 0) {
			return HiClass.MOCK_CLASS;
		}
		int dimension = 0;
		while (name.charAt(dimension) == '0') {
			dimension++;
		}
		HiClass baseClass = (HiClass) resolveIdentifier(name, true, false, false);
		return dimension == 0 ? baseClass : baseClass.getArrayClass(dimension);
	}

	@Override
	public void processResolverException(String message) {
		compiler.getValidationInfo().error(message, getCurrentToken());
	}

	public Token getCurrentToken() {
		if (currentNode != null && currentNode.getToken() != null) {
			return currentNode.getToken();
		}
		CompileClassLevel l = level;
		while (l != null) {
			if (l.node != null && l.node.getToken() != null) {
				return l.node.getToken();
			}
			l = l.parent;
		}
		return null;
	}

	public boolean addLocalVariable(NodeVariable localVariable, boolean checkDuplicate) {
		boolean valid = true;
		if (checkDuplicate && hasLocalVariable(localVariable.getVariableName())) {
			compiler.getValidationInfo().error("variable '" + localVariable.getVariableName() + "' is already defined in the scope", localVariable);
			valid = false;
		}
		level.addField(localVariable);
		return valid;
	}

	public void removeLocalVariable(NodeVariable localVariable) {
		level.removeField(localVariable);
		initializedNodes.remove(localVariable);
	}

	public boolean hasLocalVariable(String name) {
		return resolveIdentifier(name, false, true, true) != null;
	}

	public HiNodeIF resolveIdentifier(String name) {
		return resolveIdentifier(name, true, true, false);
	}

	public HiNodeIF resolveClassIdentifier(String name, HiClass outerClass, boolean resolveClass, boolean resolveVariable, boolean onlyLocal) {
		if (outerClass == null) {
			return null;
		}
		if (resolveVariable) {
			HiField field = outerClass.getField(this, name, onlyLocal);
			if (field != null) {
				return field;
			}
		}
		if (resolveClass) {
			if ((outerClass.name.equals(name) || outerClass.fullName.equals(name))) {
				return outerClass;
			}

			HiClass innerClass = outerClass.getInnerClass(this, name, true);
			if (innerClass != null) {
				return innerClass;
			}

			HiClass genericClass = outerClass.getGenericClass(this, name);
			if (genericClass != null) {
				return genericClass;
			}
		}
		return null;
	}

	public HiNodeIF resolveIdentifier(String name, boolean resolveClass, boolean resolveVariable, boolean onlyLocal) {
		CompileClassLevel level = this.level;
		if (onlyLocal && level.type == STATIC_CLASS) {
			HiNodeIF resolvedIdentifier = level.resolveLevelIdentifier(name, resolveClass, resolveVariable);
			if (resolvedIdentifier != null) {
				return resolvedIdentifier;
			}

			resolvedIdentifier = resolveClassIdentifier(name, (HiClass) level.node, resolveClass, false, true);
			if (resolvedIdentifier != null) {
				return resolvedIdentifier;
			}
		} else {
			// methods, constructors, initializers
			while (level != null) {
				if (level.type == STATIC_CLASS) {
					if (onlyLocal) {
						break;
					}
					if (resolveClass) {
						HiNodeIF resolvedClass = resolveClassIdentifier(name, (HiClass) level.node, resolveClass, resolveVariable, onlyLocal);
						if (resolvedClass != null) {
							return resolvedClass;
						}
					}
				}
				HiNodeIF resolvedIdentifier = level.resolveLevelIdentifier(name, resolveClass, resolveVariable);
				if (resolvedIdentifier != null) {
					return resolvedIdentifier;
				}
				level = level.parent;
			}
		}
		if (!onlyLocal) {
			HiClass outerClass = this.clazz;
			HiNodeIF resolvedClass = resolveClassIdentifier(name, outerClass, resolveClass, resolveVariable, false);
			if (resolvedClass != null) {
				return resolvedClass;
			}
			if (parent != null) {
				HiNodeIF resolvedIdentifier = parent.resolveIdentifier(name, resolveClass, resolveVariable, false);
				if (resolvedIdentifier != null) {
					if (resolvedIdentifier instanceof HiClass) {
						checkClassAccess((HiClass) resolvedIdentifier);
					}
					return resolvedIdentifier;
				}
			}
			if (resolveClass) {
				if (outerClass != null && name.indexOf('$') == -1) {
					int index = outerClass.fullName.lastIndexOf('$');
					if (index != -1) {
						String outboundClassName = outerClass.fullName.substring(0, index + 1);
						String extendedName = outboundClassName + '0' + name;
						HiClass clazz = HiClass.forName(this, extendedName);
						if (clazz != null) {
							checkClassAccess(clazz);
							return clazz;
						}
					}
				}
				HiClass clazz = HiClass.forName(this, name);
				if (clazz != null) {
					return clazz;
				}
			}
			if (runtimeCtx != null) {
				HiField<?> variable = runtimeCtx.getVariable(name);
				if (variable != null) {
					if (variable.initialized) {
						initializedNodes.add(variable);
					}
					return variable;
				}
				HiClass clazz = runtimeCtx.getClass(name);
				if (clazz != null) {
					return clazz;
				}
			}
		}
		return null;
	}

	private void checkClassAccess(HiClass clazz) {
		if (clazz.isPrivate() && clazz.enclosingClass != null) {
			boolean hasCommonEnclosingClass = false;
			HiClass c = this.clazz;
			while (c != null) {
				if (c == clazz.enclosingClass) {
					hasCommonEnclosingClass = true;
					break;
				}
				c = c.enclosingClass;
			}
			if (!hasCommonEnclosingClass) {
				processResolverException("class '" + clazz.getNameDescr() + "' has private access");
			}
		}
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

	// @generic
	public HiClass getDeclaredGenericClass(HiClassGeneric genericClass) {
		Type declarationType = level.getDeclarationType(genericClass.sourceClass);
		if (declarationType.parameters != null && declarationType.parameters.length > 0) {
			Type requiredArgumentType = declarationType.parameters[genericClass.index];
			return requiredArgumentType.getClass(this);
		}
		return genericClass;
	}

	public class CompileClassLevel {
		public ContextType type;

		Map<String, HiClass> classes = null;

		Map<String, NodeVariable> localVariables = null;

		int deep;

		public CompileClassLevel parent;

		CompileClassLevel child;

		public HiClass enclosingClass;

		public Type enclosingType;

		public boolean isEnclosingObject;

		public TokenAccessible node;

		public HiClass variableClass;

		public HiNodeIF variableNode;

		String label;

		public HiClass declarationClass;

		public Type declarationType;

		boolean isTerminated;

		boolean unreachableError;

		public CompileClassLevel(ContextType type, TokenAccessible node, CompileClassLevel parent) {
			this.type = type;
			this.node = node;
			setParent(parent);
			currentNode = node;
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

		public HiNodeIF resolveLevelIdentifier(String name, boolean resolveClass, boolean resolveVariable) {
			if (resolveVariable) {
				NodeVariable variable = getField(name, true);
				if (variable != null) {
					return variable;
				}
			}
			if (resolveClass) {
				HiClass clazz = getClass(name);
				if (clazz != null) {
					checkClassAccess(clazz);
					return clazz;
				}
			}
			return null;
		}

		public void addClass(HiClass clazz) {
			if (classes == null) {
				classes = new HashMap<>(1);
			}
			classes.put(clazz.name, clazz);
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
						clazz = clazz.getInnerClass(CompileClassContext.this, path[i], true);
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
						clazz = clazz.getInnerClass(CompileClassContext.this, path[i], true);
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
			String name = localVariable.getVariableName();

			// @unnamed
			if (UNNAMED.equals(name)) {
				return;
			}

			if (localVariables == null) {
				localVariables = new HashMap<>(1);
			}
			localVariables.put(localVariable.getVariableName(), localVariable);
		}

		public void removeField(NodeVariable localVariable) {
			if (localVariables != null) {
				localVariables.remove(localVariable.getVariableName());
			}
		}

		public NodeVariable getField(String name, boolean onlyLocal) {
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
			if (localVariables != null) {
				NodeVariable variable = localVariables.get(name);
				if (variable != null) {
					return variable;
				}
			}
			if (!onlyLocal && node instanceof HiClass) {
				HiClass clazz = (HiClass) node;
				return clazz.getField(CompileClassContext.this, name, true);
			}
			return null;
		}

		public CompileClassLevel getLabelLevel(String label) {
			CompileClassLevel level = this;
			while (level != null) {
				if (level.type == LABEL && level.label.equals(label)) {
					return level;
				} else if (level.type == METHOD || level.type == CONSTRUCTOR || level.type == INITIALIZATION) {
					break;
				}
				level = level.parent;
			}
			return null;
		}

		public boolean isInsideBlock() {
			return type != METHOD && type != CONSTRUCTOR && type != STATIC_CLASS;
		}

		public boolean isBreakable(String label) {
			if (label != null && label.length() > 0) {
				return type == LABEL && label.equals(this.label);
			} else {
				return type == FOR || type == WHILE || type == DO_WHILE || type == SWITCH;
			}
		}

		public CompileClassLevel getBreakLevel(String label) {
			CompileClassLevel level = this;
			while (level != null) {
				if (level.isBreakable(label)) {
					return level;
				} else if (level.type == METHOD || level.type == CONSTRUCTOR || level.type == INITIALIZATION) {
					break;
				}
				level = level.parent;
			}
			return null;
		}

		public boolean isContinuable(String label) {
			if (label != null) {
				return type == LABEL && label.equals(this.label) && child != null && (child.type == FOR || child.type == WHILE || child.type == DO_WHILE);
			} else {
				return type == FOR || type == WHILE || type == DO_WHILE;
			}
		}

		public CompileClassLevel getContinueLevel(String label) {
			CompileClassLevel level = this;
			while (level != null) {
				if (level.isContinuable(label)) {
					return level;
				} else if (level.type == METHOD || level.type == CONSTRUCTOR || level.type == INITIALIZATION) {
					break;
				}
				level = level.parent;
			}
			return null;
		}

		public CompileClassLevel getLocalContextLevel() {
			CompileClassLevel terminateLevel = this;
			while (terminateLevel != null) {
				if (terminateLevel.type == METHOD || terminateLevel.type == CONSTRUCTOR || terminateLevel.type == INITIALIZATION) {
					return terminateLevel;
				}
				terminateLevel = terminateLevel.parent;
			}
			return null;
		}

		public HiClass getCurrentStaticClass() {
			CompileClassLevel level = this;
			while (level != null) {
				if (level.type == STATIC_CLASS) {
					return (HiClass) level.node;
				}
				level = level.parent;
			}
			return null;
		}

		// @generics
		public HiClassGeneric resolveGeneric(String name) {
			CompileClassLevel level = this;
			NodeGeneric generic = null;
			while (level != null && generic == null) {
				if (level.type == METHOD) {
					HiMethod method = (HiMethod) level.node;
					if (method.generics != null) {
						generic = method.generics.getGeneric(name);
					}
				} else if (level.type == CONSTRUCTOR) {
					HiConstructor constructor = (HiConstructor) level.node;
					if (constructor.generics != null) {
						generic = constructor.generics.getGeneric(name);
					}
				} else if (level.type == STATIC_CLASS) {
					HiClass clazz = (HiClass) level.node;
					if (clazz.generics != null) {
						generic = clazz.generics.getGeneric(name);
					}
				}
				level = level.parent;
			}
			if (generic != null) {
				generic.clazz.init(CompileClassContext.this);
				return generic.clazz;
			}
			if (parent != null) {
				return parent.resolveGeneric(name);
			}
			return null;
		}

		public void terminate(boolean isReturn) {
			CompileClassLevel toLevel = isReturn ? getLocalContextLevel() : null;
			if (breaksLabels.size() > 0) {
				for (String label : breaksLabels) {
					CompileClassLevel labelLevel = getBreakLevel(label);
					if (toLevel == null) {
						toLevel = labelLevel;
					} else if (labelLevel.deep > toLevel.deep) {
						toLevel = labelLevel;
					}
				}
			}
			terminate(toLevel);
		}

		public void terminate(CompileClassLevel toLevel) {
			CompileClassLevel terminateLevel = this;
			while (terminateLevel != null) {
				if (terminateLevel.type == BLOCK || terminateLevel.type == LABEL || terminateLevel.type == DO_WHILE || terminateLevel.type == SYNCHRONIZED) {
					terminateLevel.isTerminated = true;
					if (terminateLevel == toLevel) {
						break;
					}
					terminateLevel = terminateLevel.parent;
				} else {
					break;
				}
			}
		}

		public boolean checkUnreachable(ValidationInfo validationInfo, Token token) {
			if (isTerminated && !unreachableError) {
				validationInfo.error("unreachable statement", token);
				unreachableError = true;
				return false;
			}
			return true;
		}

		public Type getDeclarationType(HiClass declarationClass) {
			CompileClassLevel level = this;
			while (level != null) {
				if (level.declarationClass == declarationClass) {
					return level.declarationType;
				}
				level = level.parent;
			}
			return null;
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
			isTerminated = false;
			unreachableError = false;
		}
	}

	public boolean isStaticContext() {
		if (clazz != null && !clazz.isStatic()) {
			return false;
		}
		CompileClassLevel level = this.level;
		while (level != null) {
			if (level.type == METHOD) {
				return ((HiMethod) level.node).isStatic();
			} else if (level.type == INITIALIZATION) {
				return ((NodeBlock) level.node).isStatic();
			} else if (level.enclosingClass != null && !level.isEnclosingObject) {
				return level.enclosingClass.isStatic();
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
