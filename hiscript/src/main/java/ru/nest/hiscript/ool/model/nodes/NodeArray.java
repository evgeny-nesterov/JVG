package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import java.io.IOException;
import java.lang.reflect.Array;

public class NodeArray extends HiNode {
	public NodeArray(Type arrayType, HiNode[] dimensions) {
		super("array", TYPE_ARRAY, false);
		this.type = arrayType;
		this.cellType = arrayType.getCellType();
		this.dimensions = dimensions;
		this.dimensionsCount = dimensions.length;
	}

	private NodeArray(HiNode[] dimensions, int dimensionsCountActive) {
		super("array", TYPE_ARRAY, false);
		this.dimensions = dimensions;
		this.dimensionsCount = dimensions.length;
		this.dimensionsCountActive = dimensionsCountActive;
	}

	private Type cellType; // only for validation

	private Type type; // only for validation

	private final int dimensionsCount;

	private int dimensionsCountActive;

	private final HiNode[] dimensions; // last cells may have null values

	public HiClassArray clazz;

	private Class<?> arrayJavaClass;

	@Override
	public int getArrayDimension() {
		return dimensionsCount;
	}

	// @generics
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
		ctx.currentNode = this;
		boolean valid = true;
		dimensionsCountActive = -1;
		for (int i = 0; i < dimensionsCount; i++) {
			if (dimensions[i] != null) {
				if (dimensionsCountActive != -1) {
					validationInfo.error("invalid array size value", dimensions[i]);
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
		HiClass cellClass = clazz.getRootCellClass();
		int dimension = dimensionsCount - dimensionsCountActive;
		arrayJavaClass = HiArrays.getClass(cellClass, dimension);

		if (cellType.parameters != null) {
			valid = false;
			validationInfo.error("cannot create array with '" + cellType.getParametersDescr() + "'", getToken());
		}
		return valid;
	}

	@Override
	public ValueType getInvocationValueType() {
		return ValueType.EXECUTE;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		int[] dimensionsValues = new int[dimensionsCountActive];
		for (int i = 0; i < dimensionsCountActive; i++) {
			dimensions[i].execute(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}

			int dimensionValue = ctx.value.getInt();
			if (ctx.exitFromBlock()) {
				return;
			}
			if (dimensionValue < 0) {
				ctx.throwRuntimeException("negative array size");
				return;
			}
			dimensionsValues[i] = dimensionValue;
		}
		ctx.value.setArrayValue(clazz, Array.newInstance(arrayJavaClass, dimensionsValues));
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeByte(dimensions != null ? dimensions.length : 0);
		os.writeNullable(dimensions);
		os.writeByte(dimensionsCountActive);
		os.writeType(type);
		os.writeType(cellType);
		os.writeJavaClass(arrayJavaClass);
		os.writeClass(clazz);
	}

	public static NodeArray decode(DecodeContext os) throws IOException {
		NodeArray node = new NodeArray(os.readNullableNodeArray(HiNode.class, os.readByte()), os.readByte());
		node.type = os.readType();
		node.cellType = os.readType();
		node.arrayJavaClass = os.readJavaClass();
		os.readClass(clazz -> node.clazz = (HiClassArray) clazz);
		return node;
	}
}
