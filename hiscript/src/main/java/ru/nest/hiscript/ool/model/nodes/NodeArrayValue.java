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
		super("array-value", TYPE_ARRAY_VALUE, false);
		this.type = type;
		this.dimensions = dimensions;
		this.array = array;
	}

	public Type type;

	public final int dimensions;

	public final HiNode[] array;

	@Override
	public int getArrayDimension() {
		return dimensions;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass clazz = type.getArrayClass(ctx, dimensions);
		// TODO compileValue for arrays
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.type = Type.getType(clazz);
		return clazz;
	}

	private HiClass cellClass;

	private Class<?> javaClass;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = true;
		int size = array.length;
		HiClass rootCellClass = type.getClass(ctx);
		HiClass cellClass = dimensions > 1 ? rootCellClass.getArrayClass(dimensions - 1) : rootCellClass;
		setCellClass(cellClass);
		for (int i = 0; i < size; i++) {
			ctx.level.variableClass = cellClass;
			ctx.level.variableNode = array[i];
			valid &= array[i].validate(validationInfo, ctx) && array[i].expectValueClass(validationInfo, ctx, cellClass);
		}
		return valid;
	}

	private void setCellClass(HiClass cellClass) {
		HiClass rootCellClass = cellClass.isArray() ? ((HiClassArray) cellClass).getRootCellClass() : cellClass;
		this.cellClass = cellClass;
		this.javaClass = HiArrays.getClass(rootCellClass, dimensions - 1);
	}

	@Override
	public int getInvocationValueType() {
		return Value.EXECUTE;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		int size = array.length;
		Object value = Array.newInstance(javaClass, array.length);
		for (int i = 0; i < size; i++) {
			array[i].execute(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}
			HiArrays.setArray(cellClass, value, i, ctx.value);
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = cellClass.getArrayClass();
		ctx.value.object = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeByte(dimensions);
		os.writeByte(array.length);
		os.writeArray(array);
		os.writeClass(cellClass);
	}

	public static NodeArrayValue decode(DecodeContext os) throws IOException {
		NodeArrayValue node = new NodeArrayValue(os.readType(), os.readByte(), os.readArray(HiNode.class, os.readByte()));
		os.readClass(cellClass -> node.setCellClass(cellClass));
		return node;
	}
}
