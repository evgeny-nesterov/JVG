package script.ool.model.nodes;

import java.io.IOException;
import java.lang.reflect.Array;

import script.ool.model.Arrays;
import script.ool.model.Clazz;
import script.ool.model.Constructor;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Type;
import script.ool.model.Value;
import script.ool.model.classes.ClazzArray;

public class NodeArray extends Node {
	public NodeArray(Type cellType, Node[] dimensions) {
		super("array", TYPE_ARRAY);

		this.dimensions = dimensions;
		dimensionsCount = dimensions.length;
		this.type = Type.getArrayType(cellType, dimensionsCount);
	}

	private int dimensionsCount;

	private int dimensionsCountActive;

	private int[] dim;

	public Type type;

	private Node[] dimensions;

	public void execute(RuntimeContext ctx) {
		if (dim == null) {
			for (dimensionsCountActive = 0; dimensionsCountActive < dimensionsCount; dimensionsCountActive++) {
				if (dimensions[dimensionsCountActive] == null) {
					break;
				}
			}
			dim = new int[dimensionsCountActive];
		}

		for (int i = 0; i < dimensionsCountActive; i++) {
			dimensions[i].execute(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}

			dim[i] = ctx.value.getInt();
			if (ctx.exitFromBlock()) {
				return;
			}
		}

		ClazzArray clazz = (ClazzArray) type.getClazz(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		Constructor constructor = clazz.searchConstructor(ctx);
		constructor.newInstance(ctx, null, null, null);

		Clazz cellClass = clazz.cellClass;
		int dimension = dimensionsCount - dimensionsCountActive;
		Class<?> c = Arrays.getClass(cellClass, dimension);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = clazz;
		ctx.value.array = Array.newInstance(c, dim);
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeByte(dimensionsCount);
		os.writeNullable(dimensions);
	}

	public static NodeArray decode(DecodeContext os) throws IOException {
		return new NodeArray(os.readType(), os.readNullableArray(Node.class, os.readByte()));
	}
}
