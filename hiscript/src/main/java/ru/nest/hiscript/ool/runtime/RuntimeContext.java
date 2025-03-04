package ru.nest.hiscript.ool.runtime;

import ru.nest.hiscript.ool.compile.HiCompiler;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.fields.HiPojoField;
import ru.nest.hiscript.ool.model.lib.ImplUtil;
import ru.nest.hiscript.ool.model.lib.ThreadImpl;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeCastedIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeInt;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.tokenizer.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.nest.hiscript.ool.model.ContextType.*;
import static ru.nest.hiscript.ool.model.nodes.NodeVariable.*;

public class RuntimeContext implements AutoCloseable, ClassResolver {
	public static int MAX_STACK_SIZE = 500;

	public boolean isExit;

	public boolean isReturn;

	public String label;

	public boolean isBreak;

	public boolean isContinue;

	public boolean main = false;

	public RuntimeContext root;

	private final List<Value[]> cacheValues = new ArrayList<>(1);

	public HiEnumValue initializingEnumValue;

	public final HiCompiler compiler;

	public final HiClassLoader classLoader;

	public final HiRuntimeEnvironment env;

	public HiObject currentThread;

	public boolean validating;

	public RuntimeContext(HiCompiler compiler, HiClassLoader classLoader, boolean main) {
		this.main = main;
		this.compiler = compiler;
		this.classLoader = classLoader != null ? classLoader : (compiler != null ? compiler.getClassLoader() : null);
		this.env = compiler.getClassLoader().getEnv();
		if (main) {
			ThreadImpl.createThread(this);
		}
	}

	// TODO: Used for a new thread to access to context of the parent thread
	public RuntimeContext(HiRuntimeEnvironment env, HiClassLoader classLoader) {
		this(null, env, classLoader);
	}

	public RuntimeContext(RuntimeContext root, HiClassLoader classLoader) {
		this(root, null, classLoader);
	}

	public RuntimeContext(RuntimeContext root, HiRuntimeEnvironment env, HiClassLoader classLoader) {
		this.root = root;
		if (root != null) {
			this.compiler = root.compiler;
			this.classLoader = classLoader != null ? classLoader : root.classLoader;
			this.env = root.env;

			// copy local context
			if (root.localClasses != null) {
				localClasses = new HashMap<>(root.localClasses.size());
				localClasses.putAll(root.localClasses);
			}

			if (root.localVariables != null) {
				localVariables = new HashMap<>(root.localVariables.size());
				localVariables.putAll(root.localVariables);
			}
		} else {
			this.compiler = null;
			this.env = env;
			this.classLoader = classLoader != null ? classLoader : (env != null ? env.getUserClassLoader() : null);
		}
	}

	@Override
	public HiCompiler getCompiler() {
		return compiler;
	}

	@Override
	public HiRuntimeEnvironment getEnv() {
		return env;
	}

	@Override
	public HiClassLoader getClassLoader() {
		return classLoader;
	}

	public boolean isCurrentLabel() {
		if (label == null && level.label == null) {
			return true;
		}
		if (label != null && label.equals(level.label)) {
			return true;
		}
		return false;
	}

	public Value value = new Value(this);

	public HiObject exception;

	public void setValue(Object value) {
		this.value.set(value);
	}

	@Override
	public void processResolverException(String message) {
		throwRuntimeException(message);
	}

	public void throwRuntimeException(String message) {
		throwException("RuntimeException", message, null);
	}

	public void throwRuntimeException(String message, HiObject cause) {
		throwException("RuntimeException", message, cause);
	}

	boolean hasException = false;

	public void throwException(String exceptionClass, String message, HiObject cause) {
		if (hasException && exception != cause) {
			return;
		}
		exception = null;
		hasException = true;

		if (message == null) {
			message = "";
		}

		int mainLevel = level.mainLevel;
		level.mainLevel = 0;

		HiClass excClass = HiClass.forName(this, exceptionClass);

		HiConstructor excConstructor;
		HiField<?>[] args;
		if (cause == null) {
			excConstructor = excClass.getConstructor(this, HiClass.STRING_CLASS);
			args = new HiField<?>[1];
		} else {
			excConstructor = excClass.getConstructor(this, HiClass.STRING_CLASS, HiClass.EXCEPTION_CLASS);
			if (excConstructor != null) {
				args = new HiField<?>[2];

				args[1] = HiField.getField(HiClass.EXCEPTION_CLASS, "cause", null);
				value.setObjectValue(HiClass.EXCEPTION_CLASS, cause);
				args[1].set(this, value);
				args[1].initialized = true;
			} else {
				excConstructor = excClass.getConstructor(this, HiClass.STRING_CLASS);
				args = new HiField<?>[1];
			}
		}

		args[0] = HiField.getField(HiClass.STRING_CLASS, "msg", null);
		NodeString.createString(this, message, false);
		args[0].set(this, value);
		args[0].initialized = true;

		exception = excConstructor.newInstance(this, null, args, null);
		level.mainLevel = mainLevel;
	}

	public boolean exitFromBlock() {
		return isReturn || isExit || exception != null;
	}

	public StackLevel level;

	// inside method, constructor and initializers
	public void enter(ContextType type, Token token) {
		if (level != null && level.mainLevel == MAX_STACK_SIZE) {
			level.mainLevel = 0;
			throwRuntimeException("stack overflow");
			throw new StackOverflowError("stack overflow");
		}
		if (level != null) {
			level = getStack(type, level, level.clazz, level.constructor, level.method, level.object, null, token);
		} else {
			level = getStack(type, null, null, null, null, null, null, token);
		}
	}

	public void enterLabel(String label, Token token) {
		level = getStack(LABEL, level, level.clazz, level.constructor, level.method, level.object, label, token);
	}

	// for operations type of a.new B(), enter in object a
	public void enterObject(HiObject object, Token token) {
		level = getStack(OBJECT, level, object.clazz, null, null, object, null, token);
	}

	public void enterMethod(HiMethod method, HiObject object) {
		if (level != null && level.mainLevel == MAX_STACK_SIZE) {
			level.mainLevel = 0;
			throwRuntimeException("stack overflow");
			throw new StackOverflowError("stack overflow");
		}
		while (object != null && !object.clazz.isInstanceof(method.clazz)) {
			// method called from local class
			object = object.outboundObject;
		}
		level = getStack(METHOD, level, method.clazz, null, method, object, null, method.getToken());
		level.mainLevel++;
	}

	public void enterConstructor(HiConstructor constructor, HiObject object, Token token) {
		if (level != null && level.mainLevel == MAX_STACK_SIZE) {
			level.mainLevel = 0;
			throwRuntimeException("stack overflow");
			throw new StackOverflowError("stack overflow");
		}
		level = getStack(CONSTRUCTOR, level, constructor.clazz, constructor, null, object, null, token);
		level.mainLevel++;
	}

	public void enterInitialization(HiClass clazz, HiObject object, Token token) {
		if (level != null && level.mainLevel == MAX_STACK_SIZE) {
			level.mainLevel = 0;
			throwRuntimeException("stack overflow");
			throw new StackOverflowError("stack overflow");
		}
		level = getStack(INITIALIZATION, level, clazz, null, null, object, null, token);
	}

	public void enterStart(HiObject object) {
		level = getStack(START, level, object != null ? object.clazz : null, null, null, object, null, null);
	}

	public void enter(StackLevel level) {
		if (this.level != null && this.level.mainLevel == MAX_STACK_SIZE) {
			level.mainLevel = 0;
			throwRuntimeException("stack overflow");
			throw new StackOverflowError("stack overflow");
		}
		level.parent = this.level;
		level.level = this.level != null ? this.level.level + 1 : 0;
		level.mainLevel = this.level != null ? this.level.mainLevel : 0;
		this.level = level;
	}

	public void exit() {
		exit(false);
	}

	public StackLevel exit(boolean lockLevel) {
		boolean isBroken = (isBreak || isContinue) && isCurrentLabel();

		if (level.levelType == START) {
			if (exception != null) {
				HiMethod method = exception.clazz.getMethod(this, "printStackTrace");
				enterMethod(method, exception);
				try {
					exception = null;
					method.invoke(this, level.object != null ? level.object.clazz : null, level.object, null);
				} finally {
					exit();
				}
			}
			close();
		}

		if (isBroken) {
			switch (level.levelType) {
				case WHILE:
				case DO_WHILE:
				case FOR:
					label = null;
					isBreak = false;
					isContinue = false;
					break;

				case SWITCH:
				case LABEL:
					label = null;
					isBreak = false;
					break;
			}
		}

		StackLevel lockedLevel = null;
		if (!lockLevel) {
			putStack(level);
		} else {
			lockedLevel = level;
		}

		level = level.parent;
		return lockedLevel;
	}

	public HiField<?> getVariable(String name) {
		HiField<?> var = null;

		// search in blocks up to method or constructor
		StackLevel level = this.level;
		WHILE:
		while (level != null) {
			var = level.getVariable(name);
			if (var != null) {
				break;
			}
			switch (level.levelType) {
				case METHOD:
				case CONSTRUCTOR:
				case INITIALIZATION:
					level = level.parent;
					break WHILE;
			}
			level = level.parent;
		}

		// check object fields (except outbound classes)
		HiObject object = null;
		if (var == null) {
			object = getCurrentObject();
			if (object != null) {
				object = object.getMainObject();
				var = object.getField(this, name, false);
			}
		}

		// check class static fields
		if (var == null) {
			HiClass enclosingClass = getCurrentClass();
			if (enclosingClass != null) {
				HiField<?> field = enclosingClass.getField(this, name);
				if (field != null && field.isStatic()) {
					var = field;
				}
			}
		}

		// check local enclosing classes
		if (var == null) {
			WHILE:
			while (level != null) {
				var = level.getVariable(name);
				if (var != null) {
					break;
				}
				switch (level.levelType) {
					case METHOD:
					case CONSTRUCTOR:
					case INITIALIZATION:
						if (level.object != null) {
							var = level.object.getMainObject().getField(this, name, false);
							if (var != null) {
								break WHILE;
							}
						}
				}
				level = level.parent;
			}
		}

		if (var == null && object != null) {
			var = object.getOutboundField(this, name);
		}

		// TODO remove?
		if (var == null) {
			HiClass clazz = this.level.clazz;
			while (clazz != null) {
				HiField<?> field = getLocalVariable(clazz, name);
				if (field != null) {
					var = field;
					break;
				}
				clazz = clazz.enclosingClass;
			}
		}
		return var;
	}

	@Override
	public HiClass getClass(String name) {
		// search in blocks up to method or constructor
		HiClass clazz = null;
		HiObject levelObject = null;
		HiClass levelClass = null;
		StackLevel level = this.level;
		WHILE:
		while (level != null) {
			clazz = level.getClass(name);

			if (level.object != null) {
				levelObject = level.object;
			}

			if (level.clazz != null) {
				levelClass = level.clazz;
			}

			if (clazz != null) {
				break;
			}

			switch (level.levelType) {
				case METHOD:
				case CONSTRUCTOR:
				case INITIALIZATION:
					break WHILE;
			}

			level = level.parent;
		}

		// поиск в классе текущего объекта
		if (clazz == null && levelObject != null) {
			clazz = levelObject.clazz.getClass(this, name);
		}

		// поиск в текущем классе (например, в случае статического доступа)
		if (clazz == null && levelClass != null) {
			clazz = levelClass.getClass(this, name);
		}

		// search in enclosing class
		if (clazz == null && levelClass != null && !levelClass.isTopLevel()) {
			clazz = levelClass.enclosingClass.getClass(this, name);
		}

		// search by class full name
		if (clazz == null) {
			// try restore class full name
			// occurred only while validating
			if (levelClass != null && name.indexOf('$') == -1) {
				int index = levelClass.fullName.lastIndexOf('$');
				if (index != -1) {
					String outboundClassName = levelClass.fullName.substring(0, index + 1);
					String extendedName = outboundClassName + '0' + name;
					clazz = HiClass.forName(this, extendedName);
					if (clazz != null) {
						return clazz;
					}
				}
			}
			clazz = HiClass.forName(this, name);
		}

		if (clazz != null) {
			clazz.init(this);
		}
		return clazz;
	}

	public HiObject getCurrentObject() {
		return level != null ? (level.object != null ? level.object.getMainObject() : null) : null;
	}

	public HiObject getOutboundObject(HiClass clazz) {
		if (clazz.isTopLevel()) {
			return null;
		}

		StackLevel level = this.level;
		while (level != null) {
			if (level.clazz == clazz.enclosingClass) {
				return level.object;
			}
			level = level.parent;
		}
		return null;
	}

	@Override
	public HiClass getCurrentClass() {
		return level.clazz;
	}

	public void addVariable(HiField<?> var) {
		level.putVariable(var);
	}

	public void addVariables(HiField<?>[] vars) {
		if (vars != null) {
			level.putVariables(vars);
		}
	}

	public void removeVariable(String name) {
		level.removeVariable(name);
	}

	public void addClass(HiClass clazz) {
		level.putClass(clazz);
	}

	private final List<StackLevel> stacksCache = new ArrayList<>(1);

	private StackLevel getStack(ContextType type, StackLevel parent, HiClass clazz, HiConstructor constructor, HiMethod method, HiObject object, String name, Token token) {
		StackLevel stack;
		int cache_size = stacksCache.size();
		if (cache_size > 0) {
			stack = stacksCache.remove(cache_size - 1);
		} else {
			stack = new StackLevel();
		}
		stack.set(type, parent, clazz, constructor, method, object, name, token);
		return stack;
	}

	public void putStack(StackLevel stack) {
		level.clear();
		stacksCache.add(stack);
	}

	@Override
	public void close() {
		ImplUtil.removeThread(this);
	}

	public class StackLevel {
		public StackLevel parent;

		public ContextType levelType;

		public String label;

		public HiClass clazz;

		public Type type;

		public HiMethod method;

		public HiConstructor constructor;

		public HiObject object;

		public int level;

		public int mainLevel;

		public Token token;

		// TODO use fast map
		private Map<String, HiField<?>> variables;

		public StackLevel() {
		}

		public StackLevel set(ContextType levelType, StackLevel parent, HiClass clazz, HiConstructor constructor, HiMethod method, HiObject object, String label, Token token) {
			this.parent = parent;
			this.clazz = clazz;
			this.constructor = constructor;
			this.method = method;
			this.object = object;
			this.level = parent != null ? parent.level + 1 : 0;
			this.mainLevel = parent != null ? parent.mainLevel : 0;
			this.levelType = levelType;
			this.label = label;
			this.token = token;

			if (variables != null) {
				variables.clear();
			}

			if (levelClasses != null) {
				levelClasses.clear();
			}
			return this;
		}

		public int getLine() {
			return token != null ? token.getLine() : -1;
		}

		public void putVariable(HiField<?> variable) {
			if (variable == null) {
				return;
			}

			// @unnamed
			if (UNNAMED.equals(variable.name)) {
				return;
			}

			if (variables == null) {
				variables = new HashMap<>(1);
			}
			variables.put(variable.name, variable);
		}

		public void putVariables(HiField<?>[] list) {
			int size = list.length;
			if (variables == null) {
				variables = new HashMap<>(size);
			}

			for (int i = 0; i < size; i++) {
				HiField<?> variable = list[i];
				// @unnamed
				if (variable != null && !UNNAMED.equals(variable.name)) {
					variables.put(variable.name, variable);
				}
			}
		}

		public HiField<?> getVariable(String name) {
			if (variables != null) {
				return variables.get(name);
			}
			return null;
		}

		public HiField<?> removeVariable(String name) {
			if (variables != null) {
				return variables.remove(name);
			}
			return null;
		}

		private Map<String, HiClass> levelClasses;

		public void putClass(HiClass putClass) {
			if (levelClasses == null) {
				levelClasses = new HashMap<>(1);
			}
			levelClasses.put(putClass.fullName, putClass);
			levelClasses.put(putClass.name, putClass);

			// store final variables and classes
			StackLevel level = this;
			WHILE:
			while (level != null) {
				if (level.levelClasses != null) {
					for (HiClass levelClass : level.levelClasses.values()) {
						addLocalClass(putClass, levelClass);
					}
				}

				if (level.variables != null) {
					for (HiField<?> f : level.variables.values()) {
						if (f.isFinal()) {
							addLocalField(putClass, f);
						}
					}
				}

				switch (level.levelType) {
					case METHOD:
					case CONSTRUCTOR:
					case INITIALIZATION:
						// TODO: don't work
						break WHILE;
				}

				level = level.parent;
			}
		}

		/**
		 * @param name имя или полное имя класса
		 * @return найденный класс
		 */
		public HiClass getClass(String name) {
			if (levelClasses != null) {
				// поиск в контексте по имени
				return levelClasses.get(name);
			}
			return null;
		}

		public void clear() {
			clazz = null;
			method = null;
			object = null;

			// clear from classes info about local final variables
			if (variables != null) {
				variables.clear();
			}

			// clear from classes info about local final classes
			if (levelClasses != null) {
				for (HiClass c : levelClasses.values()) {
					localClasses.remove(c);
					if (localVariables != null) {
						localVariables.remove(c);
					}
				}
				levelClasses.clear();
			}
		}

		@Override
		public String toString() {
			return "[" + level + "] " + levelType + ", class=" + clazz + ", method=" + method + ", object=" + object;
		}
	}

	private Map<HiClass, Map<String, HiClass>> localClasses;

	@Override
	public HiClass getLocalClass(HiClass clazz, String name) {
		if (localClasses != null) {
			Map<String, HiClass> classes = localClasses.get(clazz);
			if (classes != null) {
				return classes.get(name);
			}
		}
		return null;
	}

	public void addLocalClass(HiClass clazz, HiClass localClass) {
		if (localClasses == null) {
			localClasses = new HashMap<>(1);
		}

		Map<String, HiClass> classes = localClasses.computeIfAbsent(clazz, k -> new HashMap<>(2));
		classes.put(localClass.name, localClass);
		classes.put(localClass.fullName, localClass);
	}

	private Map<HiClass, Map<String, HiField<?>>> localVariables;

	public HiField<?> getLocalVariable(HiClass clazz, String name) {
		if (localVariables != null) {
			Map<String, HiField<?>> fields = localVariables.get(clazz);
			if (fields != null && fields.containsKey(name)) {
				return fields.get(name);
			}
		}
		return null;
	}

	public void addLocalField(HiClass clazz, HiField<?> field) {
		// @unnamed
		if (UNNAMED.equals(field.name)) {
			return;
		}

		if (localVariables == null) {
			localVariables = new HashMap<>(1);
		}

		Map<String, HiField<?>> fields = localVariables.computeIfAbsent(clazz, k -> new HashMap<>(1));
		fields.put(field.name, field);
	}

	public Value[] getValues(int size) {
		Value[] values;
		int bufSize = cacheValues.size();
		if (bufSize == 0) {
			values = new Value[size];
			for (int i = 0; i < size; i++) {
				values[i] = new Value(this);
			}
		} else {
			values = cacheValues.remove(bufSize - 1);
			if (values.length < size) {
				Value[] newValues = new Value[size];
				System.arraycopy(values, 0, newValues, 0, values.length);
				for (int i = values.length; i < size; i++) {
					newValues[i] = new Value(this);
				}
				values = newValues;
			}
		}
		return values;
	}

	public void putValues(Value[] values) {
		if (values != null) {
			cacheValues.add(values);
		}
	}

	public List<StackLevel> getStack() {
		StackLevel level = this.level;

		// remove stack trace for class Exception
		HiObject o = level.object; // instance of Exception
		while (level != null && level.object == o) {
			level = level.parent;
		}

		List<StackLevel> list = new ArrayList<>();
		while (level != null) {
			switch (level.levelType) {
				case INITIALIZATION:
				case CONSTRUCTOR:
				case METHOD:
					list.add(level);
			}
			level = level.parent;
		}
		return list;
	}

	private static HiClass steClass;

	private static HiConstructor steConstructor;

	public HiObject[] getNativeStack() {
		List<RuntimeContext.StackLevel> list = getStack();
		int size = list.size();

		if (steClass == null) {
			steClass = HiClass.forName(this, "StackTraceElement");
			steConstructor = steClass.getConstructor(this);
		}

		HiObject[] array = new HiObject[size];
		for (int i = 0; i < size; i++) {
			RuntimeContext.StackLevel level = list.get(i);

			array[i] = steConstructor.newInstance(this, null, null, null);

			NodeString.createString(this, level.clazz.getNameDescr(), true);
			array[i].getField(this, "className").set(this, value);

			if (level.method != null) {
				NodeString.createString(this, level.method.toString(), true); // TODO use getSignatureText
				array[i].getField(this, "methodName").set(this, value);
			} else {
				NodeString.createString(this, "<init>", true);
				array[i].getField(this, "methodName").set(this, value);
			}

			RuntimeContext.StackLevel lineLevel = level;
			while (lineLevel != null && lineLevel.getLine() == -1) {
				lineLevel = lineLevel.parent;
			}
			new NodeInt(lineLevel != null ? lineLevel.getLine() : -1, null).execute(this);
			array[i].getField(this, "line").set(this, value);

			// TODO: set codeLine for StackTraceElement in RuntimeContext, at the current moment codeLine=-1
		}
		return array;
	}

	@Override
	public String toString() {
		StackLevel level = this.level;
		StringBuilder buf = new StringBuilder();
		buf.append(value);
		buf.append('\n');
		StackLevel l = level;
		while (l != null) {
			buf.append('\t');
			buf.append(l);
			buf.append('\n');
			l = l.parent;
		}
		return buf.toString();
	}

	public void throwExceptionIf(boolean printStackTrace) {
		HiObject exception = this.exception;
		if (exception != null) {
			if (!exception.clazz.isInstanceof(HiClass.EXCEPTION_CLASS_NAME)) {
				throw new HiScriptRuntimeException("Bad exception value");
			}
			String message;
			if (printStackTrace) {
				message = printException();
			} else {
				message = getExceptionMessage();
			}
			throw new HiScriptRuntimeException(message);
		}
	}

	public String printException() {
		HiObject exception = this.exception;
		if (exception != null) {
			if (!exception.clazz.isInstanceof(HiClass.EXCEPTION_CLASS_NAME)) {
				throw new HiScriptRuntimeException("Bad exception value");
			}
			String message = getExceptionMessage();
			System.out.println(message);
			HiField<?> stackTraceField = exception.getMainObject().getField(this, "stackTrace");
			HiObject[] stackTraceElements = ((HiObject[]) stackTraceField.get());
			int printLinesCount = Math.min(stackTraceElements.length, 50);
			for (int i = 0; i < printLinesCount; i++) {
				HiObject stackTraceElement = stackTraceElements[i];
				String className = stackTraceElement.getField(this, "className").getStringValue(this);
				if (className.startsWith(HiClass.ROOT_CLASS_NAME_PREFIX)) {
					className = className.substring(HiClass.ROOT_CLASS_NAME_PREFIX.length());
					while (Character.isDigit(className.charAt(0))) {
						className = className.substring(1);
					}
				}
				String methodName = stackTraceElement.getField(this, "methodName").getStringValue(this);
				Integer line = (Integer) stackTraceElement.getField(this, "line").get();
				System.out.print("\t" + className + "." + methodName);
				if (line >= 0) {
					System.out.print(":" + line);
				}
				System.out.println();
			}
			if (printLinesCount < stackTraceElements.length) {
				System.out.println("\t... (" + (stackTraceElements.length - printLinesCount) + ") lines");
			}
			return message;
		}
		return null;
	}

	public String getExceptionMessage() {
		HiObject exception = this.exception;
		if (exception != null) {
			HiField<?> messageField = exception.getMainObject().getField(this, "message");
			return messageField.getStringValue(this);
		}
		return null;
	}

	public boolean addCastedVariables(String castedVariableName, HiClass fieldClass, HiNode[] castedRecordArguments, HiConstructor castedRecordArgumentsConstructor, Object object, HiClass objectClass) {
		if (object == null) {
			throwRuntimeException("null pointer");
			return false;
		}
		if (castedVariableName != null) {
			HiField castedField = HiField.getField(fieldClass, castedVariableName, null);
			castedField.set(object, objectClass);
			addVariable(castedField);
		}
		return addCastedRecordArguments(castedRecordArguments, castedRecordArgumentsConstructor, object);
	}

	private boolean addCastedRecordArguments(HiNode[] castedRecordArguments, HiConstructor castedRecordArgumentsConstructor, Object object) {
		if (castedRecordArguments != null && object instanceof HiObject) {
			HiObject hiObject = (HiObject) object;
			for (int recordArgumentIndex = 0; recordArgumentIndex < castedRecordArguments.length; recordArgumentIndex++) {
				NodeVariable castedRecordArgument = (NodeVariable) castedRecordArguments[recordArgumentIndex];
				NodeArgument originalArgument = castedRecordArgumentsConstructor.arguments[recordArgumentIndex];

				HiField objectField = null;
				for (int objectFieldIndex = 0; objectFieldIndex < hiObject.fields.length; objectFieldIndex++) {
					if (hiObject.fields[objectFieldIndex].name.equals(originalArgument.name)) {
						objectField = hiObject.fields[objectFieldIndex];
						break;
					}
				}

				HiClass realClass = objectField.getValueClass(this);
				HiClass castedClass = castedRecordArgument.getVariableClass(this);
				if (!castedClass.isInstanceof(realClass)) {
					return false;
				}

				HiPojoField castedField = new HiPojoField(castedRecordArgument.getVariableType(), castedClass, castedRecordArgument.getVariableName(), hiObject, objectField);
				addVariable(castedField);

				if (castedRecordArgument instanceof NodeCastedIdentifier) {
					castedField.get(this, value);
					HiClass argFieldClass = castedField.getClass(this);
					if (argFieldClass != null && argFieldClass.isObject()) {
						HiObject castedArgumentObject = (HiObject) value.object;
						if (object != null) {
							NodeCastedIdentifier castedIdentifier = (NodeCastedIdentifier) castedRecordArgument;
							if (!addCastedRecordArguments(castedIdentifier.castedRecordArguments, castedIdentifier.constructor, castedArgumentObject)) {
								return false;
							}
						} else {
							throwRuntimeException("null pointer");
						}
					}
				}
			}
		}
		return true;
	}
}
