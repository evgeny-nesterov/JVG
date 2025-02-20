import org.junit.jupiter.api.Test;

public class TestThread extends HiTest {
	@Test
	public void test() {
		assertSuccess("new Thread(\"test thread\").start();");
		assertSuccess("new Thread(\"\"){};");
		assertSuccess("new Thread(){public void run(){}}.start();");
		assertSuccess("class A{int x;}; A a=new A(); var t=new Thread(){public void run(){sleep(100); a.x++; assert a.x==1;}}; t.start(); assert a.x==0; t.join(); assert a.x==1;");
		assertSuccess("assert Thread.currentThread() != null;");
		assertSuccess("Thread.currentThread().sleep(100);");
		assertSuccess("int[] i = {0}; Thread t = new Thread(\"test thread\"){public void run(){Thread.currentThread().sleep(100); i[0]++;}}; t.start(); assert i[0] == 0; t.join(); assert i[0] == 1;");
	}
}