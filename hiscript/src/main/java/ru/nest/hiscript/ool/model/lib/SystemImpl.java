package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.parse.RootParseRule;
import ru.nest.hiscript.ool.model.ClassLocationType;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeReturn;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.RuntimeContext.StackLevel;
import ru.nest.hiscript.tokenizer.Tokenizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemImpl extends ImplUtil {
	// System
	public static void System_void_loadLib_String(RuntimeContext ctx, HiObject path) {
		try {
			String p = ImplUtil.getString(ctx, path);
			String classFileSuffix = "Impl.class";
			assert p.endsWith(classFileSuffix) : "invalid class path: " + p;

			String className = p.substring(0, p.length() - classFileSuffix.length());
			HiClass clazz = HiClass.forName(ctx, className);
			assert clazz != null : "class not found: " + className;

			URL url = SystemImpl.class.getResource(p);
			if (url == null) {
				File file = new File(p);
				if (file.exists()) {
					url = file.toURI().toURL();
				}
			}

			if (url == null) {
				url = new URL(p);
			}

			final Exception[] error = new Exception[1];
			ClassLoader cl = new ClassLoader() {
				@Override
				protected Class<?> findClass(String name) {
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						URL url = new URL("file", "localhost", name);
						InputStream is = url.openStream();
						int c;
						while ((c = is.read()) != -1) {
							bos.write(c);
						}
						return defineClass(null, bos.toByteArray(), 0, bos.size());
					} catch (Exception exc) {
						error[0] = exc;
						exc.printStackTrace();
					}
					return null;
				}
			};

			Class<?> javaClass = cl.loadClass(url.getFile());
			if (javaClass != null) {
				clazz.getClassLoader().getNative().register(javaClass);
			} else {
				ctx.throwRuntimeException("cannot load library: " + error[0].toString());
			}
		} catch (Exception exc) {
			ctx.throwRuntimeException("cannot load library: " + exc);
		}
		returnVoid(ctx);
	}

	public static void System_void_print_String(RuntimeContext ctx, HiObject string) {
		char[] chars = ImplUtil.getChars(ctx, string);
		if (chars != null) {
			System.out.print(chars);
		}
		returnVoid(ctx);
	}

	public static void System_void_println_String(RuntimeContext ctx, HiObject string) {
		char[] chars = ImplUtil.getChars(ctx, string);
		if (chars != null) {
			System.out.println(chars);
		}
		returnVoid(ctx);
	}

	public static void System_void_sleep_long(RuntimeContext ctx, long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException exc) {
			exc.printStackTrace();
		}
		returnVoid(ctx);
	}

	public static void System_long_time(RuntimeContext ctx) {
		returnLong(ctx, System.currentTimeMillis());
	}

	public static void System_void_exit(RuntimeContext ctx) {
		ctx.isExit = true;
		returnVoid(ctx);
	}

	public static void System_String_formatDate_long_String(RuntimeContext ctx, long time, HiObject formatPatternObj) {
		String formatPattern = ImplUtil.getString(ctx, formatPatternObj);
		SimpleDateFormat format = new SimpleDateFormat(formatPattern);
		NodeString.createString(ctx, format.format(new Date(time)), false);
	}

	public static void System_V_exec_String_boolean_boolean(RuntimeContext ctx, HiObject code, final boolean newInstance, boolean separateThread) {
		Object returnValue = null;
		HiClass originalValueClass = null;
		try {
			String text = getString(ctx, code);
			if (!text.endsWith(";")) {
				text += ";";
			}
			Tokenizer tokenizer = Tokenizer.getDefaultTokenizer(text);

			CompileClassContext compileCtx;
			if (newInstance) {
				compileCtx = new CompileClassContext(ctx.compiler, null, null, ClassLocationType.top);
			} else {
				compileCtx = new CompileClassContext(ctx);
			}

			final NodeBlock node = (NodeBlock) new RootParseRule(ctx.compiler, false, false).visit(tokenizer, compileCtx);

			ValidationInfo validationInfo = new ValidationInfo(ctx.compiler);
			boolean valid = node != null;
			if (node != null) {
				NodeExpressionNoLS expressionNode = node.getSingleStatement(NodeExpressionNoLS.class);
				if (expressionNode != null) {
					expressionNode.setStatement(false);
					node.statements.set(0, new NodeReturn(expressionNode));
					valid &= expressionNode.validate(validationInfo, compileCtx);
				} else {
					valid &= node.validate(validationInfo, compileCtx);
				}
				valid &= ctx.getClassLoader().validate(validationInfo);
			} else {
				return;
			}

			if (validationInfo.messages.size() > 0) {
				validationInfo.throwExceptionIf();
			} else if (!valid && !ctx.compiler.isVerbose()) {
				throw new HiScriptValidationException("Validation error", null);
			}

			node.setEnterType(ContextType.SAME);

			if (!separateThread) {
				RuntimeContext newCtx;
				StackLevel level = null;
				if (newInstance) {
					newCtx = new RuntimeContext(ctx.compiler, ctx.getClassLoader(), true);
				} else {
					newCtx = ctx;

					// go to upper level to get access to context from which exec method was invoked
					level = ctx.exit(true);
				}

				node.execute(newCtx);
				if (newCtx.exception == null) {
					if (newCtx.value.valueClass == HiClassPrimitive.VOID) {
						returnValue = null;
					} else if (newCtx.value.valueClass.isPrimitive()) {
						returnValue = ((HiClassPrimitive) newCtx.value.valueClass).box(ctx, ctx.value);
					} else if (!newCtx.value.valueClass.isNull()) {
						returnValue = newCtx.value.object;
					}
					originalValueClass = newCtx.value.originalValueClass;
				}

				if (!newInstance) {
					// enter to method as OperationInvocation after method invocation perform ctx.exit()
					ctx.enter(level);
				} else {
					ctx.close();
				}
			} else {
				final RuntimeContext newCtx;
				if (newInstance) {
					newCtx = new RuntimeContext(ctx.compiler, ctx.getClassLoader(), false);
				} else {
					newCtx = new RuntimeContext(ctx, ctx.getClassLoader());
				}

				new Thread(() -> {
					try {
						node.execute(newCtx);
					} catch (Exception exc) {
						exc.printStackTrace();
					} finally {
						newCtx.close();
					}
				}).start();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			String message = exc.getMessage() != null ? exc.getMessage() : exc.getClass().getName();
			ctx.throwRuntimeException("script execution error: " + message);
			return;
		}
		returnObjectOrArray(ctx, HiClass.OBJECT_CLASS, originalValueClass, returnValue);
	}

	public static void System_Object_getVariable_String(RuntimeContext ctx, HiObject name) {
		StackLevel level = ctx.exit(true);
		HiField field = ctx.getVariable(name.getStringValue(ctx));
		field.execute(ctx);
		ctx.enter(level);
	}

	public static void System_void_arraycopy_Object_int_Object_int_int(RuntimeContext ctx, Object src, int srcOffset, Object dst, int dstOffset, int length) {
		System.arraycopy(src, srcOffset, dst, dstOffset, length);
	}
}
