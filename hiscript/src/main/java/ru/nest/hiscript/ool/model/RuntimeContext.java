package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.lib.ImplUtil;
import ru.nest.hiscript.ool.model.lib.ThreadImpl;
import ru.nest.hiscript.ool.model.nodes.NodeInt;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.tokenizer.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuntimeContext implements AutoCloseable {
	public final static int SAME = -1;

	public final static int METHOD = 0;

	public final static int CONSTRUCTOR = 1;

	public final static int INITIALIZATION = 2;

	public final static int BLOCK = 3;

	public final static int FOR = 4;

	public final static int WHILE = 5;

	public final static int IF = 6;

	public final static int DO_WHILE = 7;

	public final static int SWITCH = 8;

	public final static int TRY = 9;

	public final static int CATCH = 10;

	public final static int FINALLY = 11;

	public final static int LABEL = 12;

	public final static int START = 13;

	public final static int OBJECT = 14;

	public final static int SYNCHRONIZED = 15;

	public final static int STATIC_CLASS = 16;

	public boolean isExit;

	public boolean isReturn;

	public String label;

	public boolean isBreak;

	public boolean isContinue;

	public boolean main = false;

	public RuntimeContext root;

	private List<Value[]> cacheValues = new ArrayList<>();

	public HiEnumValue initializingEnumValue;

	public HiCompiler compiler;

	public RuntimeContext(HiCompiler compiler, boolean main) {
		this.main = main;
		this.compiler = compiler;
		if (main) {
			ThreadImpl.createThread(this);
		}
	}

	// TODO: Used for a new thread to access to context of the parent thread
	public RuntimeContext(RuntimeContext root) {
		this.root = root;
		if (root != null) {
			this.compiler = root.compiler;

			// copy local context
			if (root.localClasses != null) {
				localClasses = new HashMap<>(root.localClasses.size());
				localClasses.putAll(root.localClasses);
			}

			if (root.localVariables != null) {
				localVariables = new HashMap<>(root.localVariables.size());
				localVariables.putAll(root.localVariables);
			}
		}
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

	private static HiClass excClass;

	private static HiConstructor excConstructor;

	public void throwRuntimeException(String message) {
		throwException("RuntimeException", message);
	}

	// TODO cause exception
	public void throwException(String exceptionClass, String message) {
		if (exception != null) {
			return;
		}

		if (message == null) {
			message = "";
		}

		// DEBUG
		// System.out.println(message);
		// new Exception().printStackTrace();

		if (excClass == null) {
			excClass = HiClass.forName(this, exceptionClass);
			excConstructor = excClass.getConstructor(this, HiClass.forName(this, HiClass.STRING_CLASS_NAME));
		}

		HiField<?>[] args = new HiField<?>[1];
		args[0] = HiField.getField(Type.stringType, "msg");
		NodeString.createString(this, message.toCharArray());
		args[0].set(this, value);
		args[0].initialized = true;

		exception = excConstructor.newInstance(this, args, null);

		// DEBUG
		// new Exception("DEBUG").printStackTrace();
	}

	public boolean exitFromBlock() {
		return isReturn || isExit || exception != null;
	}

	public StackLevel level = null;

	// inside method, constructor and initializers
	public void enter(int type, Token token) {
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
		level = getStack(METHOD, level, method.clazz, null, method, object, null, method.token);
	}

	public void enterConstructor(HiConstructor constructor, HiObject object, Token token) {
		level = getStack(CONSTRUCTOR, level, constructor.clazz, constructor, null, object, null, token);
	}

	public void enterInitialization(HiClass clazz, HiObject object, Token token) {
		level = getStack(INITIALIZATION, level, clazz, null, null, object, null, token);
	}

	public void enterStart(HiObject object) {
		level = getStack(START, level, object != null ? object.clazz : null, null, null, object, null, null);
	}

	public void enter(StackLevel level) {
		level.parent = this.level;
		level.level = this.level != null ? this.level.level + 1 : 0;
		this.level = level;

		// String s = "";
		// while(s.length() != 2 * level.level)
		// s += "  ";
		// System.out.println(s + "ENTER: " + level);
	}

	public void exit() {
		exit(false);
	}

	public StackLevel exit(boolean lockLevel) {
		boolean isBroken = (isBreak || isContinue) && isCurrentLabel();

		// String s = "";
		// while(s.length() != 2 * level.level)
		// s += "  ";
		// System.out.println(s + "EXIT: " + level);

		if (level.type == START) {
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
			switch (level.type) {
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

			switch (level.type) {
				case METHOD:
				case CONSTRUCTOR:
				case INITIALIZATION:
					break WHILE;
			}

			level = level.parent;
		}

		// check object fields
		if (var == null) {
			HiObject object = getCurrentObject();
			if (object != null) {
				object = object.getMainObject();
				var = object.getField(this, name);
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
		HiClass clazz = this.level.clazz;
		while (clazz != null) {
			HiField<?> field = getLocalVariable(clazz, name);
			if (field != null) {
				var = field;
				break;
			}
			clazz = clazz.enclosingClass;
		}
		return var;
	}

	public HiClass getClass(String name) {
		// search in blocks up to method or constructor
		StackLevel level = this.level;
		HiClass clazz = null;
		HiObject levelObject = null;
		HiClass levelClass = null;
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
				clazz.init(this);
				break;
			}

			switch (level.type) {
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
		if (clazz == null && level != null && level.clazz != null && !level.clazz.isTopLevel()) {
			clazz = level.clazz.enclosingClass.getClass(this, name);
		}

		// search by class full name
		if (clazz == null) {
			clazz = HiClass.forName(this, name);
		}

		if (clazz != null) {
			clazz.init(this);
		}
		return clazz;
	}

	public HiObject getCurrentObject() {
		return level != null ? level.object : null;
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

	public void addClass(HiClass clazz) {
		level.putClass(clazz);
	}

	private List<StackLevel> stacks_cache = new ArrayList<>();

	private StackLevel getStack(int type, StackLevel parent, HiClass clazz, HiConstructor constructor, HiMethod method, HiObject object, String name, Token token) {
		StackLevel stack;
		int cache_size = stacks_cache.size();
		if (cache_size > 0) {
			stack = stacks_cache.remove(cache_size - 1);
		} else {
			stack = new StackLevel();
		}
		stack.set(type, parent, clazz, constructor, method, object, name, token);

		// String s = "";
		// while(s.length() != 2 * stack.level)
		// s += "  ";
		// System.out.println(s + "ENTER: " + stack);
		return stack;
	}

	public void putStack(StackLevel stack) {
		level.clear();
		stacks_cache.add(stack);
	}

	@Override
	public void close() {
		ImplUtil.removeThread(this);
	}

	public class StackLevel {
		public StackLevel parent;

		public int type;

		public String label;

		public HiClass clazz;

		public HiMethod method;

		public HiConstructor constructor;

		public HiObject object;

		public int level;

		public Token token;

		private Map<String, HiField<?>> variables;

		public StackLevel() {
		}

		public StackLevel set(int type, StackLevel parent, HiClass clazz, HiConstructor constructor, HiMethod method, HiObject object, String label, Token token) {
			this.parent = parent;
			this.clazz = clazz;
			this.constructor = constructor;
			this.method = method;
			this.object = object;
			this.level = parent != null ? parent.level + 1 : 0;
			this.type = type;
			this.label = label;
			this.token = token;

			if (variables != null) {
				variables.clear();
			}

			if (classes != null) {
				classes.clear();
			}
			return this;
		}

		public int getLine() {
			return token != null ? token.getLine() : -1;
		}

		public void putVariable(HiField<?> variable) {
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
				if (variable != null) {
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

		private Map<String, HiClass> classes;

		public void putClass(HiClass clazz) {
			if (classes == null) {
				classes = new HashMap<>();
			}
			classes.put(clazz.fullName, clazz);
			classes.put(clazz.name, clazz);

			// store final variables and classes
			StackLevel level = this;
			WHILE:
			while (level != null) {
				if (level.classes != null) {
					for (HiClass c : level.classes.values()) {
						addLocalClass(clazz, c);
					}
				}

				if (level.variables != null) {
					for (HiField<?> f : level.variables.values()) {
						if (f.getModifiers().isFinal()) {
							addLocalField(clazz, f);
						}
					}
				}

				switch (level.type) {
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
		 * @param name
		 *            имя или полное имя класса
		 * @return найденный класс
		 */
		public HiClass getClass(String name) {
			if (classes != null) {
				// поиск в контексте по имени
				return classes.get(name);
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
			if (classes != null) {
				for (HiClass c : classes.values()) {
					localClasses.remove(c);
					if (localVariables != null) {
						localVariables.remove(c);
					}
				}
				classes.clear();
			}
		}

		@Override
		public String toString() {
			return "[" + level + "] " + type + ", class=" + clazz + ", method=" + method + ", object=" + object;
		}
	}

	private Map<HiClass, Map<String, HiClass>> localClasses;

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
			localClasses = new HashMap<>();
		}

		Map<String, HiClass> classes = localClasses.get(clazz);
		if (classes == null) {
			classes = new HashMap<>();
			localClasses.put(clazz, classes);
		}
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
		if (localVariables == null) {
			localVariables = new HashMap<>();
		}

		Map<String, HiField<?>> fields = localVariables.get(clazz);
		if (fields == null) {
			fields = new HashMap<>(1);
			localVariables.put(clazz, fields);
		}
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

	public void clear() {
		isExit = false;
		isReturn = false;

		label = null;
		isBreak = false;
		isContinue = false;

		value.clear();
		exception = null;
	}

	public List<StackLevel> getStack() {
		List<StackLevel> list = new ArrayList<>();

		StackLevel level = this.level;

		// remove stack trace for class Exception
		HiObject o = level.object; // instance of Exception
		while (level != null && level.object == o) {
			level = level.parent;
		}

		while (level != null) {
			switch (level.type) {
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

			array[i] = steConstructor.newInstance(this, null, null);

			NodeString.createString(this, level.clazz.fullName.toCharArray());
			array[i].getField(this, "className").set(this, value);

			if (level.method != null) {
				NodeString.createString(this, level.method.toString().toCharArray());
				array[i].getField(this, "methodName").set(this, value);
			} else {
				NodeString.createString(this, "<init>".toCharArray());
				array[i].getField(this, "methodName").set(this, value);
			}

			RuntimeContext.StackLevel lineLevel = level;
			while (lineLevel != null && lineLevel.getLine() == -1) {
				lineLevel = lineLevel.parent;
			}
			new NodeInt(lineLevel != null ? lineLevel.getLine() : -1, false, null).execute(this);
			array[i].getField(this, "line").set(this, value);

			// TODO: set codeLine for StackTraceElement in RuntimeContext, at the current moment codeLine=-1
		}
		return array;
	}

	@Override
	public String toString() {
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
		if (exception != null) {
			if (!exception.clazz.isInstanceof("Exception")) {
				throw new RuntimeException("Bad exception value");
			}
			HiField<?> messageField = exception.getMainObject().getField(this, "message");
			String message = messageField.getStringValue(this);
			if (printStackTrace) {
				HiField<?> stackTraceField = exception.getMainObject().getField(this, "stackTrace");
				HiObject[] stackTraceElements = ((HiObject[]) stackTraceField.get());
				System.out.println("hiscript error: " + message);
				for (HiObject stackTraceElement : stackTraceElements) {
					String className = stackTraceElement.getField(this, "className").getStringValue(this);
					String methodName = stackTraceElement.getField(this, "methodName").getStringValue(this);
					Integer line = (Integer) stackTraceElement.getField(this, "line").get();
					System.out.print("\t" + className + "." + methodName);
					if (line >= 0) {
						System.out.print(":" + line);
					}
					System.out.println();
				}
			}
			throw new RuntimeException("HiScript error: " + message);
		}
	}
}
