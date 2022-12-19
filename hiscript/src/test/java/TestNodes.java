import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeByte;
import ru.nest.hiscript.ool.model.nodes.NodeInt;
import ru.nest.hiscript.ool.model.nodes.NodeLong;
import ru.nest.hiscript.ool.model.nodes.NodeShort;
import ru.nest.hiscript.ool.model.operations.OperationPlus;

import java.io.IOException;

public class TestNodes extends HiTest {
	@Test
	public void testPlus() throws IOException {
		assertByte(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), byteNode(2)), 3);
		assertByte(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), shortNode(2)), 3);
		assertByte(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), intNode(2)), 3);
		assertByte(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), longNode(2)), 3);

		assertByte(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), byteNode(Byte.MAX_VALUE - 1)), Byte.MAX_VALUE);
		assertByte(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), shortNode(Byte.MAX_VALUE - 1)), Byte.MAX_VALUE);
		assertByte(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), intNode(Byte.MAX_VALUE - 1)), Byte.MAX_VALUE);
		assertByte(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), longNode(Byte.MAX_VALUE - 1)), Byte.MAX_VALUE);

		assertShort(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), byteNode(Byte.MAX_VALUE)), Byte.MAX_VALUE + 1);
		assertShort(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), shortNode(Byte.MAX_VALUE)), Byte.MAX_VALUE + 1);
		assertShort(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), intNode(Byte.MAX_VALUE)), Byte.MAX_VALUE + 1);
		assertShort(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), longNode(Byte.MAX_VALUE)), Byte.MAX_VALUE + 1);

		assertInt(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), shortNode(Short.MAX_VALUE)), Short.MAX_VALUE + 1);
		assertInt(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), intNode(Short.MAX_VALUE)), Short.MAX_VALUE + 1);
		assertInt(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), longNode(Short.MAX_VALUE)), Short.MAX_VALUE + 1);

		assertInt(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), intNode(Integer.MAX_VALUE)), Integer.MAX_VALUE + 1);
		assertInt(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), longNode(Integer.MAX_VALUE - 1)), Integer.MAX_VALUE);

		assertLong(doBinaryOperation(OperationPlus.getInstance(), byteNode(1), longNode(Integer.MAX_VALUE)), Integer.MAX_VALUE + 1L);
		assertLong(doBinaryOperation(OperationPlus.getInstance(), shortNode(1), longNode(Integer.MAX_VALUE)), Integer.MAX_VALUE + 1L);
		assertLong(doBinaryOperation(OperationPlus.getInstance(), intNode(1), longNode(Integer.MAX_VALUE)), Integer.MAX_VALUE + 1L);
		assertLong(doBinaryOperation(OperationPlus.getInstance(), longNode(1), longNode(Integer.MAX_VALUE)), Integer.MAX_VALUE + 1L);
	}

	private Node byteNode(int value) {
		return NodeByte.getInstance((byte) value, false, null);
	}

	private Node shortNode(int value) {
		return new NodeShort((short) value, false, null);
	}

	private Node intNode(int value) {
		return new NodeInt(value, false, null);
	}

	private Node longNode(long value) {
		return new NodeLong(value, false, null);
	}

	private void assertByte(Value value, int n) {
		Assertions.assertTrue(value.valueType == Value.VALUE);
		Assertions.assertTrue(value.type == HiClassPrimitive.BYTE);
		Assertions.assertTrue(value.byteNumber == (byte) n);
	}

	private void assertShort(Value value, int n) {
		Assertions.assertTrue(value.valueType == Value.VALUE);
		Assertions.assertTrue(value.type == HiClassPrimitive.SHORT);
		Assertions.assertTrue(value.shortNumber == (short) n);
	}

	private void assertInt(Value value, int n) {
		Assertions.assertTrue(value.valueType == Value.VALUE);
		Assertions.assertTrue(value.type == HiClassPrimitive.INT);
		Assertions.assertTrue(value.intNumber == n);
	}

	private void assertLong(Value value, long n) {
		Assertions.assertTrue(value.valueType == Value.VALUE);
		Assertions.assertTrue(value.type == HiClassPrimitive.LONG);
		Assertions.assertTrue(value.longNumber == n);
	}

	private Value doBinaryOperation(Operation operation, Node n1, Node n2) {
		RuntimeContext ctx = new RuntimeContext(new HiCompiler(new HiClassLoader("test"), null), true);
		Value v1 = getValue(ctx, n1);
		Value v2 = getValue(ctx, n2);
		operation.doOperation(ctx, v1, v2);
		return v1;
	}

	private Value getValue(RuntimeContext ctx, Node valueNode) {
		valueNode.execute(ctx);
		Value value = new Value(ctx);
		ctx.value.copyTo(value);
		return value;
	}
}