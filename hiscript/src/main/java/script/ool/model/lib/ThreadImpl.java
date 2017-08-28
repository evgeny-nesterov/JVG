package script.ool.model.lib;

import script.ool.model.Clazz;
import script.ool.model.Constructor;
import script.ool.model.Obj;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.nodes.NodeInvocation;

public class ThreadImpl extends ImplUtil {
	public static void createThread(RuntimeContext ctx) {
		synchronized (threads) {
			if (threads.containsKey(ctx)) {
				return;
			}

			// TODO: cache thread class and constr
			Clazz clazz = Clazz.forName(ctx, "Thread");
			Constructor constr = clazz.getConstructor(ctx);
			Obj obj = constr.newInstance(ctx, null, null);
			obj.userObject = Thread.currentThread();
			threads.put(ctx, obj);
		}
	}

	public static class Run implements Runnable {
		public Run(RuntimeContext ctx, Obj o) {
			this.o = o;
			new_ctx = new RuntimeContext(ctx);
		}

		private RuntimeContext new_ctx;

		private Obj o;

		public void run() {
			synchronized (threads) {
				threads.put(new_ctx, o);
			}

			new_ctx.enterStart(o, -1);
			try {
				NodeInvocation.invoke(new_ctx, o, "run");
			} finally {
				new_ctx.exit();

				synchronized (threads) {
					threads.remove(new_ctx);
				}
			}
		}
	}

	public static void Thread_void_init(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		o.userObject = new Thread(new Run(ctx, o));

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	public static void Thread_void_start(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.start();

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = Clazz.getPrimitiveClass("void");
		} catch (Exception exc) {
			ctx.throwException(exc.getMessage());
		}
	}

	public static void Thread_void_interrupt(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.interrupt();

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = Clazz.getPrimitiveClass("void");
		} catch (Exception exc) {
			ctx.throwException(exc.getMessage());
		}
	}

	public static void Thread_void_join(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.join();

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = Clazz.getPrimitiveClass("void");
		} catch (InterruptedException exc) {
			ctx.throwException(exc.getMessage());
		}
	}

	public static void Thread_void_join_long(RuntimeContext ctx, long timeMillis) {
		Obj o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		try {
			thread.join(timeMillis);

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = Clazz.getPrimitiveClass("void");
		} catch (InterruptedException exc) {
			ctx.throwException(exc.getMessage());
		}
	}

	public static void Thread_void_sleep_long(RuntimeContext ctx, long timeMillis) {
		try {
			Thread.sleep(timeMillis);
			ctx.value.valueType = Value.VALUE;
			ctx.value.type = Clazz.getPrimitiveClass("void");
		} catch (InterruptedException exc) {
			ctx.throwException(exc.toString());
		}
	}

	public static void Thread_boolean_isInterrupted(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = thread.isInterrupted();
	}

	public static void Thread_boolean_interrupted(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = Thread.interrupted();
	}

	public static void Thread_boolean_isAlive(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = thread.isAlive();
	}

	public static void Thread_void_setDaemon_boolean(RuntimeContext ctx, boolean on) {
		Obj o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();
		thread.setDaemon(on);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	public static void Thread_boolean_isDaemon(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		Thread thread = (Thread) o.getUserObject();

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = thread.isDaemon();
	}

	public static void Thread_Thread_currentThread(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.forName(ctx, "Thread");
		synchronized (threads) {
			ctx.value.object = threads.get(ctx);
		}
	}

	public static void Thread_void_yield(RuntimeContext ctx) {
		try {
			Thread.yield();
			ctx.value.valueType = Value.VALUE;
			ctx.value.type = Clazz.getPrimitiveClass("void");
		} catch (Throwable exc) {
			ctx.throwException(exc.getMessage());
		}
	}

	public static void Thread_boolean_holdsLock_Object(RuntimeContext ctx, Object obj) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = Thread.holdsLock(obj);
	}
}
