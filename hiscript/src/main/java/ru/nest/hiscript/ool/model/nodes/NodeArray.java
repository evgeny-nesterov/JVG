package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;
import java.lang.reflect.Array;

import ru.nest.hiscript.ool.model.Arrays;
import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Constructor;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.ClazzArray;

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

	@Override
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

	@Override
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
