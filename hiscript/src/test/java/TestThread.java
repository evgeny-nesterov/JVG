import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestThread extends HiTest {
	@Test
	public void test() throws IOException {
		assertSuccessSerialize("new Thread(\"test thread\").start();");
		assertSuccessSerialize("new Thread(){void run(){}}.start();");
		assertSuccessSerialize("class A{int x;}; A a=new A(); var t=new Thread(){void run(){sleep(100); a.x++;}}; t.start(); assert a.x==0; t.join(); assert a.x==1;");
		assertSuccessSerialize("assert Thread.currentThread() != null;");
		assertSuccessSerialize("Thread.currentThread().sleep(100);");
	}
}