package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.Arrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.lang.reflect.Array;

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

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		// TODO
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		int size = array.length;
		for (int i = 0; i < size; i++) {
			valid &= array[i].validate(validationInfo, ctx);
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		HiClass cellClass = type.getClass(ctx);
		HiClass currentCellClass = dimensions > 1 ? HiClass.getArrayClass(cellClass, dimensions - 1) : cellClass;
		HiClassArray clazz = HiClass.getArrayClass(currentCellClass, 1);
		Class<?> c = Arrays.getClass(cellClass, dimensions - 1);

		int size = array.length;
		Object value = Array.newInstance(c, array.length);
		for (int i = 0; i < size; i++) {
			array[i].execute(ctx);
			Arrays.setArray(currentCellClass, value, i, ctx.value);
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
		return new NodeArrayValue(os.readType(), os.readByte(), os.readArray(Node.class, os.readByte()));
	}
}
