import org.junit.jupiter.api.Test;

public class TestThread extends HiTest {
	@Test
	public void test() {
		assertSuccessSerialize("new Thread(\"test thread\").start();");
		assertSuccessSerialize("new Thread(\"\"){};");
		assertSuccessSerialize("new Thread(){public void run(){}}.start();");
		assertSuccessSerialize("class A{int x;}; A a=new A(); var t=new Thread(){public void run(){sleep(100); a.x++; assert a.x==1;}}; t.start(); assert a.x==0; t.join(); assert a.x==1;");
		assertSuccessSerialize("assert Thread.currentThread() != null;");
		assertSuccessSerialize("Thread.currentThread().sleep(100);");
		assertSuccessSerialize("int[] i = {0}; Thread t = new Thread(\"test thread\"){public void run(){Thread.currentThread().sleep(100); i[0]++;}}; t.start(); assert i[0] == 0; t.join(); assert i[0] == 1;");
	}
}