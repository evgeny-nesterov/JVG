package ru.nest.hiscript.ool.model.lib;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.nest.hiscript.ool.compiler.CompileContext;
import ru.nest.hiscript.ool.compiler.RootParseRule;
import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Native;
import ru.nest.hiscript.ool.model.Obj;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.RuntimeContext.StackLevel;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.tokenizer.Tokenizer;

public class SystemImpl extends ImplUtil {
	// System
	public static void System_void_loadLib_String(RuntimeContext ctx, Obj path) {
		try {
			String p = ImplUtil.getString(path);

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

			if (url == null) {
				ctx.throwException("can't load library: " + p);
			} else {
				final Exception[] error = new Exception[1];
				ClassLoader cl = new ClassLoader() {
					@Override
					protected Class<?> findClass(String name) throws ClassNotFoundException {
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
					Native.register(clazz);
				} else {
					ctx.throwException("can't load library: " + error[0].toString());
				}
			}
		} catch (Exception exc) {
			ctx.throwException("can't load library: " + exc.toString());
		}
	}

	public static void System_void_print_String(RuntimeContext ctx, Obj string) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
		char[] chars = ImplUtil.getChars(string);
		if (chars != null) {
			System.out.print(chars);
		}
	}

	public static void System_void_println_String(RuntimeContext ctx, Obj string) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
		char[] chars = ImplUtil.getChars(string);
		if (chars != null) {
			System.out.println(chars);
		}
	}

	public static void System_void_sleep_long(RuntimeContext ctx, long time) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
		try {
			Thread.sleep(time);
		} catch (InterruptedException exc) {
			exc.printStackTrace();
		}
	}

	public static void System_long_time(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("long");
		ctx.value.longNumber = System.currentTimeMillis();
	}

	public static void System_void_exit(RuntimeContext ctx) {
		ctx.isExit = true;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	public static void System_String_formatDate_long_String(RuntimeContext ctx, long time, Obj formatPatternObj) {
		char[] chars = ImplUtil.getChars(formatPatternObj);
		SimpleDateFormat format = new SimpleDateFormat(new String(chars));
		chars = format.format(new Date(time)).toCharArray();
		NodeString.createString(ctx, chars);
	}

	public static void System_void_exec_String_boolean_boolean(RuntimeContext ctx, Obj code, final boolean newInstance, boolean separateThread) {
		try {
			String text = getString(code);
			Tokenizer tokenizer = Tokenizer.getDefaultTokenizer(text);

			CompileContext compileCtx;
			if (newInstance) {
				compileCtx = new CompileContext(tokenizer, null, null, Clazz.CLASS_TYPE_TOP);
			} else {
				compileCtx = new CompileContext(tokenizer, null, ctx.level.clazz, Clazz.CLASS_TYPE_TOP);
			}

			final NodeBlock node = (NodeBlock) RootParseRule.getInstance().visit(tokenizer, compileCtx);
			node.setEnterType(RuntimeContext.SAME);

			if (node != null) {
				if (!separateThread) {
					RuntimeContext new_ctx;
					StackLevel level = null;
					if (newInstance) {
						new_ctx = new RuntimeContext(true);
					} else {
						new_ctx = ctx;

						// go to upper level to get access to context from which exec method was invoked
						level = ctx.exit(true);
					}

					node.execute(new_ctx);

					if (!newInstance) {
						// enter to method as OperationInvocation after method invocation perform ctx.exit()
						ctx.enter(level);
					}
				} else {
					final RuntimeContext new_ctx;
					if (newInstance) {
						new_ctx = new RuntimeContext(false);
					} else {
						new_ctx = new RuntimeContext(ctx);
					}

					new Thread() {
						@Override
						public void run() {
							try {
								node.execute(new_ctx);
							} catch (Exception exc) {
								exc.printStackTrace();
							}
						}
					}.start();
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			ctx.throwException("script execution error");
			return;
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	public static void System_void_arraycopy_Object_int_Object_int_int(RuntimeContext ctx, Object src, int srcOffset, Object dst, int dstOffset, int length) {
		System.arraycopy(src, srcOffset, dst, dstOffset, length);
	}
}
