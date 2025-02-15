package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeIdentifier extends HiNode {
	public NodeIdentifier(String name, int dimension) {
		super("identifier", TYPE_IDENTIFIER, false);
		this.name = name.intern();
		this.dimension = dimension;
	}

	public String name;

	public int dimension;

	public HiNodeIF resolvedIdentifier; // only for validation

	private HiClass clazz;

	public String getName() {
		return name;
	}

	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	public boolean isConstant(CompileClassContext ctx) {
		HiNodeIF resolvedIdentifier = ctx.resolveIdentifier(name);
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
			ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.classValue;
			ctx.nodeValueType.type = Type.getType(clazz);
			return this.clazz = clazz;
		} else {
			Object resolvedIdentifier = this.resolvedIdentifier != null ? this.resolvedIdentifier : ctx.resolveIdentifier(name); // field priority is higher than class priority
			if (resolvedIdentifier instanceof NodeVariable) {
				HiNode resolvedValueVariable = (HiNode) resolvedIdentifier;
				HiClass clazz = resolvedValueVariable.getValueClass(validationInfo, ctx);
				Type type = ctx.nodeValueType.type;
				ctx.nodeValueType.resolvedValueVariable = resolvedValueVariable;

				// @generics
				if (clazz.isGeneric()) {
					HiClass enclosingClass = ctx.level.enclosingClass != null ? ctx.level.enclosingClass : ctx.clazz;
					if (enclosingClass != null) {
						Type enclosingType = ctx.level.enclosingType != null ? ctx.level.enclosingType : ctx.type;
						clazz = enclosingClass.resolveGenericClass(ctx, null, (HiClassGeneric) clazz);
						type = ctx.nodeValueType.type;
						if (clazz.isGeneric() && enclosingType != null && enclosingType.parameters != null) {
							Type parameterType = enclosingType.getParameterType((HiClassGeneric) clazz);
							if (parameterType != null) {
								clazz = parameterType.getClass(ctx);
								type = parameterType;
							}
						}
					}
				}

				ctx.nodeValueType.enclosingClass = clazz;
				ctx.nodeValueType.enclosingType = type;
				ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
				ctx.nodeValueType.type = type;
				return this.clazz = clazz;
			} else if (resolvedIdentifier instanceof HiClass) {
				ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.classValue;
				ctx.nodeValueType.type = Type.getType((HiClass) resolvedIdentifier);
				return this.clazz = (HiClass) resolvedIdentifier;
			}
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = true;
		boolean local = false;
		resolvedIdentifier = ctx.resolveIdentifier(name, true, true, true);
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
			if (resolvedIdentifier instanceof HasModifiers) {
				nonStaticField = !((HasModifiers) resolvedIdentifier).isStatic();
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
		ctx.value.valueClass = clazz;
	}

	public static boolean resolve(RuntimeContext ctx, Value value) {
		// object
		if (resolveVariable(ctx, value)) {
			return true;
		}

		// class
		if (resolveClass(ctx, value)) {
			return true;
		}

		if (ctx.root != null) {
			return resolve(ctx.root, value);
		}
		return false;
	}

	public static boolean resolveVariable(RuntimeContext ctx, Value value) {
		String name = value.name;
		if (value.nameDimensions > 0) {
			return false;
		}

		HiField<?> field = ctx.getVariable(name);
		if (field != null) {
			ctx.value.valueType = Value.VALUE;
			ctx.value.valueClass = field.getClass(ctx);
			field.execute(ctx);
			if (ctx.exitFromBlock()) {
				return true;
			}

			// @generics
			if (ctx.value.valueClass.isGeneric()) {
				HiObject currentObject = ctx.getCurrentObject();
				HiClass objectClass = currentObject.clazz;
				Type objectType = currentObject.type;
				ctx.value.valueClass = objectClass.resolveGenericClass(ctx, objectType, (HiClassGeneric) ctx.value.valueClass);
			}

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
		int nameDimensions = v.nameDimensions;
		HiClass clazz = HiClassPrimitive.getPrimitiveClass(name);
		if (clazz == null) {
			clazz = ctx.getClass(name);
		}
		if (clazz != null) {
			if (nameDimensions > 0) {
				clazz = clazz.getArrayClass(nameDimensions);
			}
			v.valueType = Value.CLASS;
			v.valueClass = clazz;
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
		os.writeClass(clazz);
	}

	public static NodeIdentifier decode(DecodeContext os) throws IOException {
		NodeIdentifier node = new NodeIdentifier(os.readUTF(), os.readByte());
		os.readClass(c -> node.clazz = c);
		return node;
	}
}
