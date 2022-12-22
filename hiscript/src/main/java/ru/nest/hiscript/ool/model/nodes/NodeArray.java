package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.lang.reflect.Array;

public class NodeArray extends HiNode {
	public NodeArray(Type cellType, HiNode[] dimensions) {
		super("array", TYPE_ARRAY);

		this.cellType = cellType;
		this.dimensions = dimensions;
		this.dimensionsCount = dimensions.length;
		this.type = Type.getArrayType(cellType, dimensionsCount);
	}

	private int dimensionsCount;

	private int dimensionsCountActive;

	private int[] dim;

	public Type cellType;

	public Type type;

	private HiNode[] dimensions;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return type.getClass(ctx);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		for (int i = 0; i < dimensionsCountActive; i++) {
			valid &= dimensions[i].validate(validationInfo, ctx) && dimensions[i].expectIntValue(validationInfo, ctx);
		}
		return valid;
	}

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

		HiClassArray clazz = (HiClassArray) type.getClass(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		HiConstructor constructor = clazz.searchConstructor(ctx, null);
		constructor.newInstance(ctx, null, null, null);

		HiClass cellClass = clazz.cellClass;
		int dimension = dimensionsCount - dimensionsCountActive;
		Class<?> c = HiArrays.getClass(cellClass, dimension);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = clazz;
		ctx.value.array = Array.newInstance(c, dim);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(cellType);
		os.writeByte(dimensionsCount);
		os.writeNullable(dimensions);
	}

	public static NodeArray decode(DecodeContext os) throws IOException {
		return new NodeArray(os.readType(), os.readNullableNodeArray(HiNode.class, os.readByte()));
	}
}
