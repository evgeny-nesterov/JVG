import org.junit.jupiter.api.Test;
import ru.nest.hiscript.ool.HiScript;

public class TestLoad extends HiTest {
	@Test
	public void testCalcLoad() throws Exception {
		HiScript script = HiScript.create().compile("int x = 0;").execute().printError().compile("x = x + 1;");
		for (int i = 0; i < 1_000_000; i++) {
			script.execute().printError();
		}
		script.compile("System.println(\"x=\" + x); assert x == 1_000_000;").execute().printError().close();
		System.out.println("load test duration: " + script.duration() / 1000.0 + "sec");
	}

	@Test
	public void testStringLoad() throws Exception {
		HiScript script = HiScript.create().compile("String s = \"\";").execute().printError().compile("s = s + \"*\";");
		for (int i = 0; i < 50_000; i++) {
			script.execute().printError();
		}
		script.compile("System.println(\"s.length=\" + s.length()); assert s.length() == 50_000;").execute().printError().close();
		System.out.println("load test duration: " + script.duration() / 1000.0 + "sec");
	}

	@Test
	public void testClassLoad() throws Exception {
		long time = System.currentTimeMillis();
		for (int i = 0; i < 10_000_000; i++) {
			class A {
				class B {
					int x;
				}
			}
			A.B b = new A().new B();
			b.x = 1;
			assert b.x == 1;
		}
		System.out.println("t1: " + (System.currentTimeMillis() - time));

		time = System.currentTimeMillis();
		HiScript script = HiScript.create().compile("class A {class B {int x;}} A.B b = new A().new B(); b.x = 1; assert b.x == 1;");
		for (int i = 0; i < 1_000_000; i++) {
			script.execute();
		}
		System.out.println("t2: " + (System.currentTimeMillis() - time));
		// ~200 times slower
	}

	@Test
	public void testClassLoad2() throws Exception {
		boolean[] started = {false};
		boolean[] ready = {false};
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HiScript script = HiScript.create().compile("class A {class B {int x;}} A.B b = new A().new B(); b.x = 1; assert b.x == 1;");
					started[0] = true;
					for (int i = 0; i < 100_000; i++) {
						script.execute();
					}
				} catch (Exception e) {
				}
				ready[0] = true;
			}
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!ready[0]) {
					try {
						Thread.sleep(1);
						if (started[0]) {
							System.out.println("---------------------------");
							for (StackTraceElement e : t1.getStackTrace()) {
								System.out.println("\t" + e);
							}
						}
					} catch (Exception e) {
					}
				}
			}
		});
		t1.start();
		t2.start();
		t1.join();
		t2.join();
	}
}