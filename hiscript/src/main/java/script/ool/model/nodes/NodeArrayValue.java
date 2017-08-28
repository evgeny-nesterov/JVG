package script.ool.model.nodes;

import java.io.IOException;
import java.lang.reflect.Array;

import script.ool.model.Arrays;
import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Type;
import script.ool.model.Value;

public class NodeArrayValue extends Node {
	public NodeArrayValue(Type type, int dimensions, Node[] array) {
		super("array-value", TYPE_ARRAY_VALUE);

		this.type = type;
		this.dimensions = dimensions;
		this.array = array;
	}

	public Type type;

	private int dimensions;

	private Node[] array;

	public void execute(RuntimeContext ctx) {
		Clazz cellClazz = type.getClazz(ctx);
		Clazz currentCellClazz = dimensions > 1 ? Clazz.getArrayClass(cellClazz, dimensions - 1) : cellClazz;
		Clazz clazz = Clazz.getArrayClass(currentCellClazz, 1);
		Class<?> c = Arrays.getClass(cellClazz, dimensions - 1);

		int size = array.length;
		Object value = Array.newInstance(c, array.length);
		for (int i = 0; i < size; i++) {
			array[i].execute(ctx);
			Arrays.setArray(currentCellClazz, value, i, ctx.value);
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = clazz;
		ctx.value.array = value;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeByte(dimensions);
		os.writeByte(array.length);
		os.write(array);
	}

	public static NodeArrayValue decode(DecodeContext os) throws IOException {
		return new NodeArrayValue(os.readType(), os.readByte(), os.readArray(Node.class, os.readByte()));
	}
}
