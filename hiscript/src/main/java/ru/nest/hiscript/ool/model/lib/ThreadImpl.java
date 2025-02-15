package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;

public class ThreadImpl extends ImplUtil {
	private static HiClass threadClass;

	public synchronized static void createThread(RuntimeContext ctx) {
		if (threadClass == null) {
			threadClass = HiClass.forName(ctx, "Thread");
			if (threadClass == null) {
				throw new HiScriptRuntimeException("cannot find class Thread");
			}
		}

		HiObject object = new HiObject(ctx, threadClass, null, null);
		object.userObject = Thread.currentThread();

		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = threadClass;
		ctx.value.originalValueClass = null;
		ctx.value.object = object;
		ctx.currentThread = object;
	}

	public static class Run implements Runnable {
		public Run(RuntimeContext ctx, HiObject object) {
			this.object = object; // object fot thread
			newCtx = new RuntimeContext(ctx);
		}

		private final RuntimeContext newCtx;

		private HiObject object;

		@Override
		public void run() {
			object = object.getMainObject(); // if class is anonymous
			newCtx.currentThread = object;
			newCtx.enterStart(object);
			try {
				NodeInvocation.invoke(newCtx, object, "run");
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				newCtx.exit();
				newCtx.close();
			}
		}
	}

	public static void Thread_void_init_String(RuntimeContext ctx, HiObject name) {
		String n = getString(ctx, name);
		HiObject o = ctx.getCurrentObject();
		o.userObject = n != null ? new Thread(new Run(ctx, o), n) : new Thread(new Run(ctx, o));
		returnVoid(ctx);
	}

	public static void Thread_void_start(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.start();
			returnVoid(ctx);
		} catch (Exception exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_void_interrupt(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.interrupt();
			returnVoid(ctx);
		} catch (Exception exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_void_join(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.join();
			returnVoid(ctx);
		} catch (InterruptedException exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_void_join_long(RuntimeContext ctx, long timeMillis) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.join(timeMillis);
			returnVoid(ctx);
		} catch (InterruptedException exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_void_sleep_long(RuntimeContext ctx, long timeMillis) {
		try {
			Thread.sleep(timeMillis);
			returnVoid(ctx);
		} catch (InterruptedException exc) {
			ctx.throwRuntimeException(exc.toString());
		}
	}

	public static void Thread_boolean_isInterrupted(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		returnBoolean(ctx, thread.isInterrupted());
	}

	public static void Thread_boolean_interrupted(RuntimeContext ctx) {
		returnBoolean(ctx, Thread.interrupted());
	}

	public static void Thread_boolean_isAlive(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		returnBoolean(ctx, thread.isAlive());
	}

	public static void Thread_void_setDaemon_boolean(RuntimeContext ctx, boolean on) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		thread.setDaemon(on);
		returnVoid(ctx);
	}

	public static void Thread_boolean_isDaemon(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		returnBoolean(ctx, thread.isDaemon());
	}

	public static void Thread_Thread_currentThread(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClass.forName(ctx, "Thread");
		ctx.value.originalValueClass = null;
		ctx.value.object = ctx.currentThread;
	}

	public static void Thread_void_yield(RuntimeContext ctx) {
		try {
			Thread.yield();
			returnVoid(ctx);
		} catch (Throwable exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_boolean_holdsLock_Object(RuntimeContext ctx, Object obj) {
		returnBoolean(ctx, Thread.holdsLock(obj));
	}

	public static void Thread_String_toString(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		returnString(ctx, thread.toString());
	}
}
