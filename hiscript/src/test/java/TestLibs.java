import org.junit.jupiter.api.Test;

public class TestLibs extends HiTest {
	@Test
	public void testCollections() {
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); assert l.size() == 1; assert l.get(0).equals(\"a\"); l.remove(\"a\"); assert l.size() == 0;");
		assertSuccessSerialize("HashMap m = new HashMap(); m.put(\"1\", \"a\"); assert m.size() == 1; assert m.containsKey(\"1\"); assert m.get(\"1\").equals(\"a\"); m.remove(\"1\"); assert m.size() == 0;");
		assertSuccessSerialize("HashMap<String, String> m = new HashMap<>(); m.put(\"1\", \"a\"); for (String key : m.keys()) {assert key.equals(\"1\");}  for (String v : m.values()) {assert v.equals(\"a\");}");
		assertSuccessSerialize("HashMap m1 = new HashMap(); m1.put(\"1\", \"a\"); HashMap m2 = new HashMap(); m2.putAll(m1); assert m2.size() == 1; assert m2.get(\"1\").equals(\"a\");");
	}

	@Test
	public void testMath() {
		assertSuccessSerialize("double pi = Math.PI;");
		assertSuccessSerialize("assert Math.sqrt(256) == 16;");
	}

	@Test
	public void testSystemExec() {
		assertSuccessSerialize("Integer v = System.exec(\"1 + 2\"); assert v == 3;");
		assertSuccessSerialize("Boolean v = System.exec(\"int x = 1; int y = 2; return x < y;\"); assert v;");
		assertFailMessage("Integer v = System.exec(\"1 < 2\");", "cannot convert 'Boolean' to 'Integer'");
		assertFailMessage("Float v = System.exec(\"1.0/2.0\");", "cannot convert 'Double' to 'Float'");
	}
}