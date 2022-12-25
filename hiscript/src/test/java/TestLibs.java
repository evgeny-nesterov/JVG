import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestLibs extends HiTest {
	@Test
	public void testCollections() throws IOException {
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); assert l.size() == 1; assert l.get(0).equals(\"a\"); l.remove(\"a\"); assert l.size() == 0;");
		assertSuccessSerialize("HashMap m = new HashMap(); m.put(\"1\", \"a\"); assert m.size() == 1; assert m.containsKey(\"1\"); assert m.get(\"1\").equals(\"a\"); m.remove(\"1\"); assert m.size() == 0;");
	}
}