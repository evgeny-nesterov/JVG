package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeIdentifier extends HiNode {
	public NodeIdentifier(String name, int dimension) {
		super("identifier", TYPE_IDENTIFIER);
		this.name = name; // .intern();
		this.dimension = dimension;
	}

	public String name;

	public int dimension;

	public String getName() {
		return name;
	}

	@Override
	public boolean isConstant(CompileClassContext ctx) {
		Object resolvedIdentifier = ctx.resolveIdentifier(name);
		if (resolvedIdentifier instanceof HiNode) {
			return ((HiNode) resolvedIdentifier).isConstant(ctx);
		}
		return false;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (dimension > 0) {
			HiClass clazz = HiClassPrimitive.getPrimitiveClass(name);
			if (clazz == null) {
				clazz = ctx.getClass(name);
			}
			if (dimension > 0) {
				clazz = clazz.getArrayClass(dimension);
			}
			return clazz;
		} else {
			Object resolvedIdentifier = ctx.resolveIdentifier(name); // field priority is higher than class priority
			if (resolvedIdentifier instanceof NodeVariable) {
				return ((HiNode) resolvedIdentifier).getValueClass(validationInfo, ctx);
			} else if (resolvedIdentifier instanceof HiClass) {
				return (HiClass) resolvedIdentifier;
			}
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		Object resolvedIdentifier = ctx.resolveIdentifier(name);
		if (resolvedIdentifier == null) {
			validationInfo.error("Can't resolve symbol '" + name + "'", token);
			return false;
		} else if (resolvedIdentifier instanceof NodeArgument) {
			// arguments are always initialized
		} else if (resolvedIdentifier instanceof HiNode && ctx.level.objectClass == null && !ctx.initializedNodes.contains(resolvedIdentifier)) {
			validationInfo.error("Variable '" + name + "' is not initialized", token);
			return false;
		}
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.NAME;
		ctx.value.name = name;
		ctx.value.nameDimensions = dimension;
	}

	public static boolean resolve(RuntimeContext ctx, Value value, boolean checkInitialization) {
		// object
		if (resolveVariable(ctx, value, checkInitialization)) {
			return true;
		}

		// class
		if (resolveClass(ctx, value)) {
			return true;
		}
		return false;
	}

	public static boolean resolveVariable(RuntimeContext ctx, Value value, boolean checkInitialization) {
		String name = value.name;
		if (value.nameDimensions > 0) {
			return false;
		}

		HiField<?> var = ctx.getVariable(name);
		if (var != null) {
			if (checkInitialization && !var.isInitialized(ctx)) {
				ctx.throwRuntimeException("variable not initialized: " + var.name);
				return true;
			}

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = var.type.getClass(ctx);
			var.execute(ctx);

			ctx.value.copyTo(value);
			value.valueType = Value.VARIABLE;
			value.name = name;
			value.variable = var;
			return true;
		}
		return false;
	}

	public static boolean resolveClass(RuntimeContext ctx, Value v) {
		String name = v.name;
		HiClass clazz = HiClassPrimitive.getPrimitiveClass(name);
		if (clazz == null) {
			clazz = ctx.getClass(name);
		}
		if (clazz != null) {
			if (v.nameDimensions > 0) {
				clazz = clazz.getArrayClass(v.nameDimensions);
			}
			v.valueType = Value.CLASS;
			v.type = clazz;
			v.name = name;
			return true;
		}
		return false;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(name);
		os.writeByte(dimension);
	}

	public static NodeIdentifier decode(DecodeContext os) throws IOException {
		return new NodeIdentifier(os.readUTF(), os.readByte());
	}
}
