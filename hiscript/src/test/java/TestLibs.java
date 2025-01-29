import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class TestLibs extends HiTest {
	@Test
	public void testArrayList() {
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); assert l.size() == 1; assert l.get(0).equals(\"a\");");
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); assert l.remove(\"a\"); assert l.size() == 0;");
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); assert l.remove(0).equals(\"a\"); assert l.size() == 0;");
		assertSuccessSerialize("ArrayList l = new ArrayList(2); l.add(\"a\"); l.add(\"a\"); assert l.indexOf(\"a\") == 0;");
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); assert l.indexOf(\"b\") == -1;");
		assertSuccessSerialize("ArrayList l = new ArrayList(2); l.add(\"a\"); l.add(\"a\"); assert l.lastIndexOf(\"a\") == 1;");
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); l.set(0, \"b\"); assert l.size() == 1; assert l.get(0).equals(\"b\");");
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); assert l.contains(\"a\");");
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); Object[] arr = l.toArray(); assert arr.length == 1; assert arr[0].equals(\"a\");");
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); l.clear(); assert l.size() == 0;");
		assertSuccessSerialize("ArrayList l1 = new ArrayList(); l1.add(\"a\"); ArrayList l2 = new ArrayList(); l2.addAll(l1); assert l2.size() == 1; assert l2.get(0).equals(\"a\");");
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); l.add(1); assert l.size() == 2; assert l.indexOf(1) == 1; assert l.get(0).equals(\"a\"); assert l.get(1).equals(1); assert l.remove(\"a\"); assert l.size() == 1;");
		assertSuccessSerialize("ArrayList<String> l = new ArrayList(); l.add(\"a\"); l.add(\"b\"); assert l.size() == 2; assert l.indexOf(\"b\") == 1; assert l.get(0).equals(\"a\"); assert l.get(1).equals(\"b\"); assert l.remove(\"a\"); assert l.size() == 1;");
		assertSuccessSerialize("ArrayList<Integer> l = new ArrayList<>(); l.add(1); l.add(2); Iterator<Integer> i = l.iterator(); String s = \"\"; while(i.hasNext()) {s += i.next();} assert s.equals(\"12\");");
		assertFailCompile("ArrayList<String> l = new ArrayList(); l.add(1L);");
	}

	@Test
	public void testHashMap() {
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