package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeIdentifier extends HiNode {
	public NodeIdentifier(String name, int dimension) {
		super("identifier", TYPE_IDENTIFIER);
		this.name = name.intern();
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
			// <type>[][]...[]
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
				HiNode resolvedValueVariable = (HiNode) resolvedIdentifier;
				HiClass clazz = resolvedValueVariable.getValueClass(validationInfo, ctx);
				ctx.nodeValueType.resolvedValueVariable = resolvedValueVariable;
				return clazz;
			} else if (resolvedIdentifier instanceof HiClass) {
				return (HiClass) resolvedIdentifier;
			}
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		boolean local = false;
		Object resolvedIdentifier = ctx.resolveIdentifier(name, true, true, true);
		if (resolvedIdentifier != null) {
			local = true;
		} else {
			resolvedIdentifier = ctx.resolveIdentifier(name);
		}

		if (resolvedIdentifier == null) {
			validationInfo.error("cannot resolve symbol '" + name + "'", token);
			valid = false;
		} else if (resolvedIdentifier instanceof NodeArgument) {
			// arguments are always initialized
		} else if (resolvedIdentifier instanceof HiNode && ctx.level.enclosingClass == null && !ctx.initializedNodes.contains(resolvedIdentifier)) {
			validationInfo.error("variable '" + name + "' is not initialized", token);
			valid = false;
		}

		if (!local) {
			boolean nonStaticField = false;
			if (resolvedIdentifier instanceof HiField) {
				nonStaticField = !((HiField) resolvedIdentifier).isStatic();
			} else if (resolvedIdentifier instanceof NodeDeclaration) {
				Modifiers modifiers = ((NodeDeclaration) resolvedIdentifier).modifiers;
				nonStaticField = modifiers != null ? !modifiers.isStatic() : true;
			}
			if (nonStaticField && ctx.isStaticContext()) {
				validationInfo.error("non-static field '" + name + "' cannot be accessed from static context", token);
				valid = false;
			}
		}
		return valid;
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

		if (ctx.root != null) {
			return resolve(ctx.root, value, checkInitialization);
		}
		return false;
	}

	public static boolean resolveVariable(RuntimeContext ctx, Value value, boolean checkInitialization) {
		String name = value.name;
		if (value.nameDimensions > 0) {
			return false;
		}

		HiField<?> field = ctx.getVariable(name);
		if (field != null) {
			if (checkInitialization && !field.isInitialized(ctx)) {
				ctx.throwRuntimeException("variable not initialized: " + field.name);
				return true;
			}

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = field.getClass(ctx);
			field.execute(ctx);

			ctx.value.copyTo(value);
			value.valueType = Value.VARIABLE;
			value.name = name;
			value.variable = field;
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
