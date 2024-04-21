package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeDouble extends NodeNumber {
	private final static String name = "double";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	// TODO NaN, Infinite

	public NodeDouble(double value, boolean hasSign, Token token) {
		super(name, TYPE_DOUBLE, hasSign, token);
		this.value = value;
	}

	private final double value;

	public double getValue() {
		return value;
	}

	@Override
	public Object getConstantValue() {
		return value;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.compileValue;
		ctx.nodeValueType.type = Type.doubleType;
		return HiClassPrimitive.DOUBLE;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = type;
		ctx.value.doubleNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeDouble(value);
		os.writeBoolean(hasSign);
	}

	public static NodeDouble decode(DecodeContext os) throws IOException {
		return new NodeDouble(os.readDouble(), os.readBoolean(), null);
	}
}
