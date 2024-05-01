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

public class NodeArray extends HiNode {
	public NodeArray(Type cellType, HiNode[] dimensions) {
		super("array", TYPE_ARRAY, false);

		this.cellType = cellType;
		this.dimensions = dimensions;
		this.dimensionsCount = dimensions.length;
		this.type = Type.getArrayType(cellType, dimensionsCount);
	}

	private final int dimensionsCount;

	private int dimensionsCountActive;

	public Type cellType;

	public Type type;

	public HiClassArray clazz;

	public Class<?> arrayJavaClass;

	private final HiNode[] dimensions;

	// generic
	public boolean validateDeclarationGenericType(Type type, ValidationInfo validationInfo, CompileClassContext ctx) {
		if (cellType.parameters != null && cellType.parameters.length == 0) {
			this.type = type;
			this.cellType = type.cellTypeRoot;
			return true;
		} else {
			return this.type.validateMatch(type, validationInfo, ctx, getToken());
		}
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = this;
		HiClass clazz = type.getClass(ctx);
		ctx.nodeValueType.enclosingClass = clazz;
		ctx.nodeValueType.enclosingType = type;
		// TODO compileValue for arrays
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.type = type;
		return clazz;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (dimensions[0] == null) {
			validationInfo.error("invalid array size value", getToken());
		}
		dimensionsCountActive = -1;
		for (int i = 0; i < dimensionsCount; i++) {
			if (dimensions[i] != null) {
				if (dimensionsCountActive != -1) {
					validationInfo.error("invalid array size value", dimensions[i].getToken());
					valid = false;
				}
				valid &= dimensions[i].validate(validationInfo, ctx) && dimensions[i].expectIntValue(validationInfo, ctx);
			} else if (dimensionsCountActive == -1) {
				dimensionsCountActive = i;
			}
		}
		if (dimensionsCountActive == -1) {
			dimensionsCountActive = dimensionsCount;
		}

		clazz = (HiClassArray) type.getClass(ctx);
		if (clazz != null) {
			HiClass cellClass = clazz.getRootCellClass();
			int dimension = dimensionsCount - dimensionsCountActive;
			arrayJavaClass = HiArrays.getClass(cellClass, dimension);

			if (cellType.parameters != null) {
				valid = false;
				validationInfo.error("cannot create array with '" + cellType.getParametersDescr() + "'", getToken());
			}
		} else {
			valid = false;
		}
		return valid;
	}

	@Override
	public int getInvocationValueType() {
		return Value.EXECUTE;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		int[] dim = new int[dimensionsCountActive];
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

		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = clazz;
		ctx.value.array = Array.newInstance(arrayJavaClass, dim);
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
