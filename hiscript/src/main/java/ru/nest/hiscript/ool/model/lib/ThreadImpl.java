package ru.nest.hiscript.ool.model.lib;

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
				throw new RuntimeException("can't find class Thread");
			}
		}

		HiObject object = new HiObject(threadClass, null);
		object.userObject = Thread.currentThread();

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = threadClass;
		ctx.value.object = object;
		ctx.currentThread = object;
	}

	public static class Run implements Runnable {
		public Run(RuntimeContext ctx, HiObject object) {
			this.object = object;
			newCtx = new RuntimeContext(ctx);
		}

		private RuntimeContext newCtx;

		private HiObject object;

		@Override
		public void run() {
			newCtx.currentThread = object;
			newCtx.enterStart(object);
			try {
				NodeInvocation.invoke(newCtx, object, "run");
			} finally {
				newCtx.exit();
				newCtx.close();
			}
		}
	}

	public static void Thread_void_init(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		o.userObject = new Thread(new Run(ctx, o));

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("void");
	}

	public static void Thread_void_start(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.start();

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = HiClass.getPrimitiveClass("void");
		} catch (Exception exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_void_interrupt(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.interrupt();

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = HiClass.getPrimitiveClass("void");
		} catch (Exception exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_void_join(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.join();

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = HiClass.getPrimitiveClass("void");
		} catch (InterruptedException exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_void_join_long(RuntimeContext ctx, long timeMillis) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.join(timeMillis);

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = HiClass.getPrimitiveClass("void");
		} catch (InterruptedException exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_void_sleep_long(RuntimeContext ctx, long timeMillis) {
		try {
			Thread.sleep(timeMillis);
			ctx.value.valueType = Value.VALUE;
			ctx.value.type = HiClass.getPrimitiveClass("void");
		} catch (InterruptedException exc) {
			ctx.throwRuntimeException(exc.toString());
		}
	}

	public static void Thread_boolean_isInterrupted(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = thread.isInterrupted();
	}

	public static void Thread_boolean_interrupted(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = Thread.interrupted();
	}

	public static void Thread_boolean_isAlive(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = thread.isAlive();
	}

	public static void Thread_void_setDaemon_boolean(RuntimeContext ctx, boolean on) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		thread.setDaemon(on);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("void");
	}

	public static void Thread_boolean_isDaemon(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = thread.isDaemon();
	}

	public static void Thread_Thread_currentThread(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.forName(ctx, "Thread");
		ctx.value.object = ctx.currentThread;
	}

	public static void Thread_void_yield(RuntimeContext ctx) {
		try {
			Thread.yield();
			ctx.value.valueType = Value.VALUE;
			ctx.value.type = HiClass.getPrimitiveClass("void");
		} catch (Throwable exc) {
			ctx.throwRuntimeException(exc.getMessage());
		}
	}

	public static void Thread_boolean_holdsLock_Object(RuntimeContext ctx, Object obj) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = Thread.holdsLock(obj);
	}
}
