package script.pol.model;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import script.tokenizer.Words;

//	IO: 
//	void print(string text)
//	void println(string text)
//	
//	Math:
//	double random()
//	double sqrt(double value)
//	double sin(double value)
//	double cos(double value)
//	double tan(double value)
//	double log(double value)
//	double log10(double value)
//	double abs(double value)
//	double floor(double value)
//	double ceil(double value)
//	double exp(double value)
//	double asin(double value)
//	double acos(double value)
//	double atan(double value)
//	double sinh(double value)
//	double cosh(double value)
//	double tanh(double value)
//	double cbrt(double value)
//	double min(double a, double b)
//	double max(double a, double b)
//	double pow(double value, double pow)
//	
//	Arrays:
//	int length(long[] array)
//	
//	System:
//	long time()
//	void setSystemProperty(string key, string value)
//	string getSystemProperty(string key)
//	boolean hasSystemProperty(string key)
//	void removeSystemProperty(string key)
//	void exit(int status)
//	
//	Toolkit:
//	void beep()
//	int getScreenResolution()
//	int getScreenWidth()
//	int getScreenHeight()
//	
//	Dialog:
//	showMessage(string message, string title)
//	
//	Thread:
//	long getCurrentThread()
//	void join(long tid)
//	void join(long tid, long timeout)
//	void interrupt(long tid)
//	boolean interrupted(long tid)
//	void yeld(long tid)
//	void setPriority(long tid, int priority)
//	int getPriority(long tid)
//	void setThreadName(long tid, string name)
//	string getThreadName(long tid)
//	void sleep(long time)
//	long getLock()
//	lock(long lockID, long timeout)
//	unlock(long lockID)
//	
//	String:
//	int length(string str)
//	string trim(string str)
//	char charAt(string str, int index)
//	string substring(string str, int offset, int length)
//	string substring(string str, int offset)
//	int indexOf(string str, string text, int startOffset)
//	int lastIndexOf(string str, string text, int endOffset)
//	string replace(string str, char src, char dst)
//	void replaceAll(string str, string pattern, string replacement)
//	string getString(byte[] bytes)
//	string getString(char[] chars)
//	
//	Character:
//	boolean isDigit(char c)
//	boolean isLetter(char c)
//	boolean isLowerCase(char c)
//	boolean isUpperCase(char c)
//	boolean isSpaceChar(char c)
//	boolean isWhitespace(char c)
//	
//	Number:
//	byte parseByte(string str)
//	short parseShort(string str)
//	int parseInt(string str)
//	float parseFloat(string str)
//	long parseLong(string str)
//	double parseDouble(string str)
//	
//	Script:
//	long execute(string script, boolean createNewThread)
//	
//	Reflection:
//	boolean isVariableExists(string varName)
//	boolean isVariableExists(string namespace, string varName)
//	boolean isVariableDefined(string varName)
//	boolean isVariableDefined(string namespace, string varName)

public class RuntimeContext {
	private volatile static long current_id = 0;

	public synchronized static long nextID() {
		return current_id++;
	}

	private static Set<Long> locks = new HashSet<Long>();

	private volatile static long current_lock_id = 0;

	public static long nextLockID() {
		synchronized (locks) {
			return current_lock_id++;
		}
	}

	public RuntimeContext() throws ExecuteException {
		addMethods(nativeMethods);
	}

	public void addMethods(List<Method> methods) throws ExecuteException {
		for (Method m : methods) {
			addMethod(m);
		}
	}

	private RuntimeContext parentContext;

	public RuntimeContext(RuntimeContext parentContext) throws ExecuteException {
		this();
		this.parentContext = parentContext;
	}

	private final static List<Method> nativeMethods = new ArrayList<Method>();

	private static Map<Long, Thread> threads = new HashMap<Long, Thread>();
	static {
		try {
			// In, Out
			addNativeMethods(System.out, new Class[] { String.class }, "print", "println");

			// Math
			addNativeMethods(Math.class, new Class[] {}, "random");

			addNativeMethods(Math.class, new Class[] { double.class }, "sqrt", "sin", "cos", "tan", "log", "log10", "abs", "floor", "ceil", "exp", "asin", "acos", "atan", "sinh", "cosh", "tanh", "cbrt");

			addNativeMethods(Math.class, new Class[] { double.class, double.class }, "min", "max", "pow");

			// arrays
			nativeMethods.add(new Method(null, "length", new int[] { Words.BYTE }, new int[] { 1 }, Words.INT) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					ctx.value.intNumber = ((byte[]) arguments[0]).length;
				}
			});
			nativeMethods.add(new Method(null, "length", new int[] { Words.SHORT }, new int[] { 1 }, Words.INT) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					ctx.value.intNumber = ((short[]) arguments[0]).length;
				}
			});
			nativeMethods.add(new Method(null, "length", new int[] { Words.INT }, new int[] { 1 }, Words.INT) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					ctx.value.intNumber = ((int[]) arguments[0]).length;
				}
			});
			nativeMethods.add(new Method(null, "length", new int[] { Words.LONG }, new int[] { 1 }, Words.INT) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					ctx.value.intNumber = ((long[]) arguments[0]).length;
				}
			});
			nativeMethods.add(new Method(null, "length", new int[] { Words.FLOAT }, new int[] { 1 }, Words.INT) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					ctx.value.intNumber = ((float[]) arguments[0]).length;
				}
			});
			nativeMethods.add(new Method(null, "length", new int[] { Words.DOUBLE }, new int[] { 1 }, Words.INT) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					ctx.value.intNumber = ((double[]) arguments[0]).length;
				}
			});

			// System
			addNativeMethod(System.class, new Class[] {}, "currentTimeMillis", "time");

			addNativeMethod(System.class, new Class[] { String.class }, "getProperty", "getSystemProperty");

			addNativeMethod(System.class, new Class[] { String.class, String.class }, "setProperty", "setSystemProperty");

			addNativeMethod(System.class, new Class[] { int.class }, "exit", "exit");

			nativeMethods.add(new Method(null, "hasSystemProperty", new int[] { Words.STRING }, new int[] { 0 }, Words.BOOLEAN) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					String key = (String) arguments[0];
					ctx.value.bool = System.getProperty(key) != null;
				}
			});

			nativeMethods.add(new Method(null, "removeSystemProperty", new int[] { Words.STRING }, new int[] { 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					String key = (String) arguments[0];
					System.setProperty(key, null);
				}
			});

			// Toolkit
			addNativeMethods(Toolkit.getDefaultToolkit(), new Class[] {}, new String[] { "beep", "getScreenResolution" });

			nativeMethods.add(new Method(null, "getScreenWidth", new int[] {}, new int[] {}, Words.INT) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					ctx.value.intNumber = Toolkit.getDefaultToolkit().getScreenSize().width;
				}
			});

			nativeMethods.add(new Method(null, "getScreenHeight", new int[] {}, new int[] {}, Words.INT) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					ctx.value.intNumber = Toolkit.getDefaultToolkit().getScreenSize().height;
				}
			});

			// Dialog
			nativeMethods.add(new Method(null, "showMessage", new int[] { Words.STRING, Words.STRING }, new int[] { 0, 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					String message = (String) arguments[0];
					String title = (String) arguments[1];
					JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
				}
			});

			// Thread
			nativeMethods.add(new Method(null, "getCurrentThread", new int[] {}, new int[] {}, Words.LONG) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					ctx.value.longNumber = Thread.currentThread().getId();
				}
			});

			nativeMethods.add(new Method(null, "join", new int[] { Words.LONG }, new int[] { 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					Long threadID = (Long) arguments[0];
					Thread thread = threads.get(threadID);
					if (thread != null) {
						try {
							thread.join();
						} catch (InterruptedException exc) {
							throw new ExecuteException(exc.toString());
						}
					}
				}
			});

			nativeMethods.add(new Method(null, "join", new int[] { Words.LONG, Words.LONG }, new int[] { 0, 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					Long threadID = (Long) arguments[0];
					Thread thread = threads.get(threadID);
					if (thread != null) {
						try {
							long timeout = (Long) arguments[1];
							thread.join(timeout);
						} catch (InterruptedException exc) {
							throw new ExecuteException(exc.toString());
						}
					}
				}
			});

			nativeMethods.add(new Method(null, "interrupt", new int[] { Words.LONG }, new int[] { 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					Long threadID = (Long) arguments[0];
					Thread thread = threads.get(threadID);
					if (thread != null) {
						thread.interrupt();
					}
				}
			});

			nativeMethods.add(new Method(null, "interrupted", new int[] { Words.LONG }, new int[] { 0 }, Words.BOOLEAN) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					Long threadID = (Long) arguments[0];
					Thread thread = threads.get(threadID);
					ctx.value.bool = thread != null ? thread.isInterrupted() : true;
				}
			});

			nativeMethods.add(new Method(null, "yield", new int[] { Words.LONG }, new int[] { 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					Long threadID = (Long) arguments[0];
					Thread thread = threads.get(threadID);
					if (thread != null) {
						Thread.yield();
					}
				}
			});

			nativeMethods.add(new Method(null, "setPriority", new int[] { Words.LONG, Words.INT }, new int[] { 0, 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					Long threadID = (Long) arguments[0];
					Thread thread = threads.get(threadID);
					if (thread != null) {
						int priority = (Integer) arguments[1];
						thread.setPriority(priority);
					}
				}
			});

			nativeMethods.add(new Method(null, "getPriority", new int[] { Words.LONG }, new int[] { 0 }, Words.INT) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					Long threadID = (Long) arguments[0];
					Thread thread = threads.get(threadID);
					if (thread != null) {
						ctx.value.intNumber = thread.getPriority();
					}
				}
			});

			nativeMethods.add(new Method(null, "setThreadName", new int[] { Words.LONG, Words.STRING }, new int[] { 0, 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					Long threadID = (Long) arguments[0];
					Thread thread = threads.get(threadID);
					if (thread != null) {
						String name = (String) arguments[1];
						thread.setName(name);
					}
				}
			});

			nativeMethods.add(new Method(null, "getThreadName", new int[] { Words.LONG }, new int[] { 0 }, Words.STRING) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);
					Long threadID = (Long) arguments[0];
					Thread thread = threads.get(threadID);
					if (thread != null) {
						ctx.value.string = thread.getName();
					}
				}
			});

			nativeMethods.add(new Method(null, "sleep", new int[] { Words.LONG }, new int[] { 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					try {
						long time = (Long) arguments[0];
						Thread.sleep(time);
					} catch (InterruptedException exc) {
					}
				}
			});

			nativeMethods.add(new Method(null, "getLock", new int[] {}, new int[] {}, Words.LONG) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					ctx.value.longNumber = nextLockID();
				}
			});

			nativeMethods.add(new Method(null, "unlock", new int[] { Words.LONG }, new int[] { 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					Long lockID = (Long) arguments[0];
					synchronized (locks) {
						locks.remove(lockID);
					}
				}
			});

			nativeMethods.add(new Method(null, "lock", new int[] { Words.LONG, Words.LONG }, new int[] { 0, 0 }, Words.VOID) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					Long lockID = (Long) arguments[0];
					long timeout = (Long) arguments[1];
					long startTime = System.currentTimeMillis();
					synchronized (locks) {
						while (locks.contains(lockID)) {
							if (timeout > 0 && System.currentTimeMillis() - startTime >= timeout) {
								break;
							}

							try {
								locks.wait(1);
							} catch (InterruptedException exc) {
							}
						}
						locks.add(lockID);
					}
				}
			});

			// String
			addNativeMethod(String.class, new Class[] {}, "length");

			addNativeMethod(String.class, new Class[] {}, "trim");

			addNativeMethod(String.class, new Class[] { int.class }, "charAt");

			addNativeMethod(String.class, new Class[] { int.class, int.class }, "substring");

			addNativeMethod(String.class, new Class[] { int.class }, "substring");

			addNativeMethod(String.class, new Class[] { String.class, int.class }, "indexOf");

			addNativeMethod(String.class, new Class[] { String.class, int.class }, "lastIndexOf");

			addNativeMethod(String.class, new Class[] { char.class, char.class }, "replace");

			addNativeMethod(String.class, new Class[] { String.class, String.class }, "replaceAll");

			nativeMethods.add(new Method(null, "getString", new int[] { Words.BYTE }, new int[] { 1 }, Words.STRING) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					ctx.value.type = Words.STRING;
					ctx.value.dimension = 0;
					ctx.value.string = new String((byte[]) arguments[0]);
				}
			});

			nativeMethods.add(new Method(null, "getString", new int[] { Words.CHAR }, new int[] { 1 }, Words.STRING) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					ctx.value.type = Words.STRING;
					ctx.value.dimension = 0;
					ctx.value.string = new String((char[]) arguments[0]);
				}
			});

			// Character
			addNativeMethods(Character.class, new Class[] { char.class }, "isDigit", "isLetter", "isLowerCase", "isUpperCase", "isSpaceChar", "isWhitespace");

			// Number
			addNativeMethods(Byte.class, new Class[] { String.class }, "parseByte");

			addNativeMethods(Short.class, new Class[] { String.class }, "parseShort");

			addNativeMethods(Integer.class, new Class[] { String.class }, "parseInt");

			addNativeMethods(Float.class, new Class[] { String.class }, "parseFloat");

			addNativeMethods(Long.class, new Class[] { String.class }, "parseLong");

			addNativeMethods(Double.class, new Class[] { String.class }, "parseDouble");

			// execute script
			nativeMethods.add(new Method(null, "execute", new int[] { Words.STRING, Words.BOOLEAN }, new int[] { 0, 0 }, Words.LONG) {
				@Override
				public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
					super.invoke(ctx, parent, arguments);

					String s = (String) arguments[0];
					boolean newThread = (Boolean) arguments[1];

					long threadID = execute(ctx, parent, s, newThread);

					ctx.value.longNumber = threadID;
				}
			});

			// reflection
			nativeMethods.add(new Method(null, "isVariableExists", new int[] { Words.STRING }, new int[] { 0 }, Words.BOOLEAN) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					String variableName = (String) arguments[0];
					Variable variable = node.getVariable(variableName);
					ctx.value.bool = variable != null;
				}
			});

			nativeMethods.add(new Method(null, "isVariableExists", new int[] { Words.STRING, Words.STRING }, new int[] { 0, 0 }, Words.BOOLEAN) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					String namespace = (String) arguments[0];
					String variableName = (String) arguments[1];

					String fullname;
					if (namespace != null && namespace.length() > 0) {
						fullname = namespace + "." + variableName;
					} else {
						fullname = variableName;
					}

					Variable variable = node.getVariable(fullname);
					ctx.value.bool = variable != null;
				}
			});

			nativeMethods.add(new Method(null, "isVariableDefined", new int[] { Words.STRING }, new int[] { 0 }, Words.BOOLEAN) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					String variableName = (String) arguments[0];
					Variable variable = node.getVariable(variableName);
					ctx.value.bool = variable != null && variable.isDefined();
				}
			});
			nativeMethods.add(new Method(null, "isVariableDefined", new int[] { Words.STRING, Words.STRING }, new int[] { 0, 0 }, Words.BOOLEAN) {
				@Override
				public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
					super.invoke(ctx, node, arguments);
					String namespace = (String) arguments[0];
					String variableName = (String) arguments[1];

					String fullname;
					if (namespace != null && namespace.length() > 0) {
						fullname = namespace + "." + variableName;
					} else {
						fullname = variableName;
					}

					Variable variable = node.getVariable(fullname);
					ctx.value.bool = variable != null && variable.isDefined();
				}
			});
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static long execute(final RuntimeContext ctx, Node parent, final String s, boolean newThread) throws ExecuteException {
		Thread thread;
		if (newThread) {
			final Map<String, Variable> variables = parent.getAllVariables();
			thread = new Thread() {
				@Override
				public void run() {
					try {
						Node parent = new BlockNode();
						parent.addVariables(variables);

						// Create a new context to avoid breaking of data during
						// concurency.
						ScriptUtil.execute(new RuntimeContext(ctx), parent, s);
						threads.remove(Thread.currentThread().getId());
					} catch (ExecuteException exc) {
						exc.printStackTrace();
					}
				}
			};
			threads.put(thread.getId(), thread);
			thread.start();
		} else {
			thread = Thread.currentThread();
			ScriptUtil.execute(ctx, parent, s);
		}

		return thread.getId();
	}

	public static void addNativeMethods(Class<?> c, Class<?>[] argTypes, String... methodNames) {
		for (String methodName : methodNames) {
			try {
				java.lang.reflect.Method method = c.getMethod(methodName, argTypes);
				addNativeMethod(method, method.getName(), c);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public static void addNativeMethod(Class<System> c, Class<?>[] argTypes, String methodName, String newMethodName) {
		try {
			addNativeMethod(c.getMethod(methodName, argTypes), newMethodName, c);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static void addNativeMethods(Object o, Class<?>[] argTypes, String... methodNames) {
		for (String methodName : methodNames) {
			try {
				java.lang.reflect.Method method = o.getClass().getMethod(methodName, argTypes);
				addNativeMethod(method, method.getName(), o);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public static void addNativeMethod(Class<?> objectType, Class<?>[] argTypes, String methodName) {
		try {
			addNativeMethodByObject(objectType.getMethod(methodName, argTypes), objectType);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static void addNativeMethod(final java.lang.reflect.Method method, String methodName, final Object o) {
		int returnType = Types.getType(method.getReturnType());

		Class<?>[] t = method.getParameterTypes();
		int[] argTypes = new int[t.length];
		int[] argDimensions = new int[t.length];
		for (int i = 0; i < argTypes.length; i++) {
			argTypes[i] = Types.getType(t[i]);
			argDimensions[i] = Types.getDimension(t[i]);
		}

		nativeMethods.add(new Method(null, methodName, argTypes, argDimensions, returnType) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				try {
					Object returnedValue = method.invoke(o, arguments);
					ctx.value.setValue(returnedValue, getReturnType());
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});
	}

	public static void addNativeMethodByObject(final java.lang.reflect.Method method, Class<?> objectType) {
		int returnType = Types.getType(method.getReturnType());

		Class<?>[] t = method.getParameterTypes();
		int[] argTypes = new int[t.length + 1];
		int[] argDimensions = new int[t.length + 1];
		argTypes[0] = Types.getType(objectType);
		argDimensions[0] = Types.getDimension(objectType);
		for (int i = 1; i < argTypes.length; i++) {
			argTypes[i] = Types.getType(t[i - 1]);
			argDimensions[i] = Types.getDimension(t[i - 1]);
		}

		nativeMethods.add(new Method(null, method.getName(), argTypes, argDimensions, returnType) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				try {
					Object[] arg = new Object[arguments.length - 1];
					for (int i = 0; i < arg.length; i++) {
						arg[i] = arguments[i + 1];
					}

					Object returnedValue = method.invoke(arguments[0], arg);
					ctx.value.setValue(returnedValue, getReturnType());
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});
	}

	private Methods methods = new Methods();

	public Method getMethod(String namespace, String name, int[] argTypes, int[] argDimensions) {
		Method method = methods.get(namespace, name, argTypes, argDimensions);
		if (method != null) {
			return method;
		}

		if (parentContext != null) {
			return parentContext.getMethod(namespace, name, argTypes, argDimensions);
		}

		return null;
	}

	public void addMethod(Method method) throws ExecuteException {
		Method m = getMethod(method.getNamespace(), method.getName(), method.getArgTypes(), method.getArgDimensions());
		if (m != null) {
			throw new ExecuteException(method.toString() + " is already defined");
		}

		if (methods == null) {
			methods = new Methods();
		}

		methods.add(method);
	}

	// private HashMap<String, HashMap<Integer, ArrayList<Method>>> hash_methods
	// = new HashMap();
	// private Method getOwnMethod(String name, int[] argTypes, int[]
	// argDimensions)
	// {
	// HashMap<Integer, ArrayList<Method>> argcount_methods =
	// hash_methods.get(name);
	// if(argcount_methods == null)
	// {
	// return null;
	// }
	//
	// int argCount = argTypes.length;
	// ArrayList<Method> methods = argcount_methods.get(argCount);
	// if(methods == null)
	// {
	// return null;
	// }
	//
	// int size = methods.size();
	// for(int j = 0; j < size; j++)
	// {
	// Method m = methods.get(j);
	// for(int i = 0; i < argCount; i++)
	// {
	// if(m.getArgDimensions()[i] != argDimensions[i] ||
	// (argDimensions[i] == 0 && !Types.isAutoCast(argTypes[i],
	// m.getArgTypes()[i])) ||
	// (argDimensions[i] > 0 && argTypes[i] != m.getArgTypes()[i]))
	// {
	// return null;
	// }
	// }
	// return m;
	// }
	//
	// return null;
	// }
	//
	//
	// public void addMethod(Method method)
	// {
	// Method m = getMethod(method.getName(), method.getArgTypes(),
	// method.getArgDimensions());
	// if(m == null)
	// {
	// HashMap<Integer, ArrayList<Method>> argcount_methods =
	// hash_methods.get(method.getName());
	// if(argcount_methods == null)
	// {
	// argcount_methods = new HashMap();
	// hash_methods.put(method.getName(), argcount_methods);
	// }
	//
	// int argCount = method.getArgTypes().length;
	// ArrayList<Method> methods = argcount_methods.get(argCount);
	// if(methods == null)
	// {
	// methods = new ArrayList();
	// argcount_methods.put(argCount, methods);
	// }
	// methods.add(method);
	// }
	// }

	public boolean isExit;

	public ValueContainer value = new ValueContainer();

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		int i = 0;
		while (i < 1000000) {
			i++;
		}
		System.out.println("time=" + (System.currentTimeMillis() - time));
	}
}
