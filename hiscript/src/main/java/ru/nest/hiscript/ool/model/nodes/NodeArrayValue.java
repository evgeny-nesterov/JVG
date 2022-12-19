package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.lang.reflect.Array;

public class NodeArrayValue extends HiNode {
	public NodeArrayValue(Type type, int dimensions, HiNode[] array) {
		super("array-value", TYPE_ARRAY_VALUE);

		this.type = type;
		this.dimensions = dimensions;
		this.array = array;
	}

	public Type type;

	private int dimensions;

	private HiNode[] array;

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		return type.getArrayClass(ctx, dimensions);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		int size = array.length;
		for (int i = 0; i < size; i++) {
			valid &= array[i].validate(validationInfo, ctx) && array[i].expectValue(validationInfo, ctx);
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		HiClass cellClass = type.getClass(ctx);
		HiClass currentCellClass = dimensions > 1 ? cellClass.getArrayClass(dimensions - 1) : cellClass;
		HiClassArray clazz = currentCellClass.getArrayClass(1);
		Class<?> javaClass = HiArrays.getClass(cellClass, dimensions - 1);

		int size = array.length;
		Object value = Array.newInstance(javaClass, array.length);
		for (int i = 0; i < size; i++) {
			array[i].execute(ctx);
			HiArrays.setArray(currentCellClass, value, i, ctx.value);
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = clazz;
		ctx.value.array = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeByte(dimensions);
		os.writeByte(array.length);
		os.writeArray(array);
	}

	public static NodeArrayValue decode(DecodeContext os) throws IOException {
		return new NodeArrayValue(os.readType(), os.readByte(), os.readArray(HiNode.class, os.readByte()));
	}
}
