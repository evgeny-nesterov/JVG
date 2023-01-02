package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.RootParseRule;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.HiNative;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.RuntimeContext.StackLevel;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeString;
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

			Class<?> clazz = cl.loadClass(url.getFile());
			if (clazz != null) {
				HiNative.register(clazz);
			} else {
				ctx.throwRuntimeException("cannot load library: " + error[0].toString());
			}
		} catch (Exception exc) {
			ctx.throwRuntimeException("cannot load library: " + exc.toString());
		}
	}

	public static void System_void_print_String(RuntimeContext ctx, HiObject string) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.VOID;
		char[] chars = ImplUtil.getChars(ctx, string);
		if (chars != null) {
			System.out.print(chars);
		}
	}

	public static void System_void_println_String(RuntimeContext ctx, HiObject string) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.VOID;
		char[] chars = ImplUtil.getChars(ctx, string);
		if (chars != null) {
			System.out.println(chars);
		}
	}

	public static void System_void_sleep_long(RuntimeContext ctx, long time) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.VOID;
		try {
			Thread.sleep(time);
		} catch (InterruptedException exc) {
			exc.printStackTrace();
		}
	}

	public static void System_long_time(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.LONG;
		ctx.value.longNumber = System.currentTimeMillis();
	}

	public static void System_void_exit(RuntimeContext ctx) {
		ctx.isExit = true;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.VOID;
	}

	public static void System_String_formatDate_long_String(RuntimeContext ctx, long time, HiObject formatPatternObj) {
		String formatPattern = ImplUtil.getString(ctx, formatPatternObj);
		SimpleDateFormat format = new SimpleDateFormat(formatPattern);
		NodeString.createString(ctx, format.format(new Date(time)));
	}

	public static void System_void_exec_String_boolean_boolean(RuntimeContext ctx, HiObject code, final boolean newInstance, boolean separateThread) {
		try {
			String text = getString(ctx, code);
			Tokenizer tokenizer = Tokenizer.getDefaultTokenizer(text);

			CompileClassContext compileCtx;
			if (newInstance) {
				compileCtx = new CompileClassContext(ctx.compiler, null, HiClass.CLASS_TYPE_TOP);
			} else {
				compileCtx = new CompileClassContext(ctx.compiler, ctx.level.clazz, HiClass.CLASS_TYPE_TOP);
			}

			final NodeBlock node = (NodeBlock) new RootParseRule(ctx.compiler, false, false).visit(tokenizer, compileCtx);
			node.setEnterType(RuntimeContext.SAME);

			if (!separateThread) {
				RuntimeContext newCtx;
				StackLevel level = null;
				if (newInstance) {
					newCtx = new RuntimeContext(ctx.compiler, true);
				} else {
					newCtx = ctx;

					// go to upper level to get access to context from which exec method was invoked
					level = ctx.exit(true);
				}

				node.execute(newCtx);

				if (!newInstance) {
					// enter to method as OperationInvocation after method invocation perform ctx.exit()
					ctx.enter(level);
				} else {
					ctx.close();
				}
			} else {
				final RuntimeContext newCtx;
				if (newInstance) {
					newCtx = new RuntimeContext(ctx.compiler, false);
				} else {
					newCtx = new RuntimeContext(ctx);
				}

				new Thread() {
					@Override
					public void run() {
						try {
							node.execute(newCtx);
						} catch (Exception exc) {
							exc.printStackTrace();
						} finally {
							newCtx.close();
						}
					}
				}.start();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			ctx.throwRuntimeException("script execution error");
			return;
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.VOID;
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
