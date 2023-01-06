package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassLoadListener;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNoClassException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.lang.reflect.Array;

public class NodeConstructor extends HiNode {
	public NodeConstructor(NodeType type, HiNode[] argValues) {
		super("constructor", TYPE_CONSTRUCTOR);
		this.type = type;
		this.argValues = argValues;
		name = type.getType().fullName;
	}

	public NodeConstructor(HiClass clazz, HiNode[] argValues) {
		super("constructor", TYPE_CONSTRUCTOR);
		this.clazz = clazz;
		this.argValues = argValues;
		name = clazz.getFullName(clazz.getClassLoader());
	}

	private NodeConstructor(HiNode[] argValues) {
		super("constructor", TYPE_CONSTRUCTOR);
		this.argValues = argValues;
	}

	public NodeType type;

	public HiClass[] argsClasses;

	public HiNode[] argValues;

	public String name;

	private HiClass clazz;

	private HiConstructor constructor;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = clazz != null ? clazz : type.getType().getClass(ctx);
		return ctx.nodeValueType.enclosingClass;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (argValues != null) {
			int size = argValues.length;
			argsClasses = new HiClass[size];
			for (int i = 0; i < size; i++) {
				HiNode argValue = argValues[i];
				if (argValue.validate(validationInfo, ctx) && argValue.expectValue(validationInfo, ctx)) {
					argsClasses[i] = argValue.getValueClass(validationInfo, ctx);
				} else {
					valid = false;
				}
			}
		}

		// resolve class
		if (clazz == null) {
			clazz = getValueClass(validationInfo, ctx);
		}
		if (clazz == HiClassPrimitive.VOID) {
			clazz = null;
		}

		if (clazz == null) {
			validationInfo.error("class not found: " + name, type.getToken());
			return false;
		} else if (clazz.isInterface) {
			validationInfo.error("cannot create object from interface '" + name + "'", getToken());
			return false;
		} else if (clazz.isEnum()) {
			validationInfo.error("Enum types cannot be instantiated", getToken());
		}

		if (clazz.type == HiClass.CLASS_TYPE_ANONYMOUS) {
			valid &= clazz.validate(validationInfo, ctx);
		}

		if (clazz.isStatic() && ctx.level.enclosingClass != null && ctx.level.isEnclosingObject) {
			validationInfo.error("qualified new of static class", type.getToken());
			valid = false;
		} else if (!clazz.isStatic() && ctx.level.enclosingClass == null && name.indexOf('.') != -1) {
			validationInfo.error("'" + name + "' is not an enclosing class", type.getToken());
			valid = false;
		}

		// resolve constructor
		if (!clazz.isAbstract()) {
			constructor = clazz.searchConstructor(ctx, argsClasses);
			if (constructor != null) {
				CompileClassContext.CompileClassLevel level = ctx.level;
				while (level != null) {
					if (level.type == RuntimeContext.CONSTRUCTOR && level.node == constructor) {
						validationInfo.error("recursive constructor invocation", getToken());
						valid = false;
					}
					level = level.parent;
				}
			} else {
				validationInfo.error("constructor not found: " + clazz.fullName, getToken());
				valid = false;
			}
		} else {
			validationInfo.error("'" + clazz.fullName + "' is abstract; cannot be instantiated", getToken());
			valid = false;
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// init by class
		clazz.init(ctx);
		ctx.addClass(clazz);

		HiObject outboundObject = ctx.getOutboundObject(clazz);
		invokeConstructor(ctx, clazz, constructor, argsClasses, argValues, null, outboundObject);
	}

	public static void invokeConstructor(RuntimeContext ctx, HiClass clazz, HiNode[] argValues, HiObject object, HiObject outboundObject) {
		// build argument class array and evaluate method arguments
		HiClass[] argsClasses = null;
		if (argValues != null) {
			int size = argValues.length;
			argsClasses = new HiClass[size];
			for (int i = 0; i < size; i++) {
				argValues[i].execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}
				argsClasses[i] = ctx.value.type;
			}
		}

		// get constructor
		HiConstructor constructor = clazz.searchConstructor(ctx, argsClasses);
		if (constructor == null) {
			ctx.throwRuntimeException("constructor not found: " + clazz.fullName);
			return;
		}

		RuntimeContext.StackLevel level = ctx.level;
		while (level != null) {
			if (level.type == RuntimeContext.CONSTRUCTOR && level.constructor == constructor) {
				ctx.throwRuntimeException("recursive constructor invocation");
				return;
			}
			level = level.parent;
		}

		invokeConstructor(ctx, clazz, constructor, argsClasses, argValues, object, outboundObject);
	}

	public static void invokeConstructor(RuntimeContext ctx, HiClass clazz, HiConstructor constructor, HiClass[] argsClasses, HiNode[] argValues, HiObject object, HiObject outboundObject) {
		HiField[] argsFields = getArgumentsFields(ctx, clazz, constructor, argValues);
		if (ctx.exitFromBlock()) {
			return;
		}

		// set names and types of arguments
		if (argsClasses != null) {
			int size = argsClasses.length;
			if (constructor.hasVarargs()) {
				int varargsSize = argsClasses.length - constructor.arguments.length + 1;
				int mainSize = size - varargsSize;
				Type varargsArrayType = constructor.arguments[constructor.arguments.length - 1].getType();
				HiClass varargsClass = varargsArrayType.getCellType().getClass(ctx);
				HiClass varargsArrayClass = varargsArrayType.getClass(ctx);
				HiField<?> varargsField = HiField.getField(varargsArrayClass, constructor.arguments[constructor.arguments.length - 1].name, constructor.arguments[constructor.arguments.length - 1].getToken());

				Class<?> _varargClass = HiArrays.getClass(varargsClass, 0);
				Object array = Array.newInstance(_varargClass, varargsSize);
				for (int i = 0; i < varargsSize; i++) {
					ctx.value.type = argsClasses[mainSize + i];
					argsFields[mainSize + i].get(ctx, ctx.value);
					HiArrays.setArray(varargsClass, array, i, ctx.value);
				}

				ctx.value.type = varargsArrayClass;
				ctx.value.array = array;
				varargsField.set(ctx, ctx.value);

				argsFields[mainSize] = varargsField;
				int newSize = mainSize + 1;
				for (int i = newSize; i < size; i++) {
					argsFields[i] = null;
				}
				size = newSize;
			}

			for (int i = 0; i < size; i++) {
				HiClass argClass = argsFields[i] != null ? argsFields[i].getClass(ctx) : HiClassNull.NULL;

				// on null argument update field class from ClazzNull on argument class
				if (argClass.isNull()) {
					argsFields[i] = HiField.getField(argClass, constructor.arguments[i].name, constructor.arguments[i].getToken());
					ctx.value.type = HiClassNull.NULL;
					argsFields[i].set(ctx, ctx.value);
				} else if (!argClass.isArray()) {
					ctx.value.type = argClass;
					argsFields[i].get(ctx, ctx.value);
					argsFields[i] = HiField.getField(argClass, constructor.arguments[i].name, constructor.arguments[i].getToken());
					argsFields[i].set(ctx, ctx.value);
				}
				// TODO: update array cell type

				if (!clazz.isJava() && i < constructor.arguments.length) {
					argsFields[i].name = constructor.arguments[i].name;
				}
				argsFields[i].initialized = true;
			}
		}

		constructor.newInstance(ctx, argsFields, object, outboundObject);
	}

	/**
	 * build argument class array and evaluate method arguments
	 */
	public static HiField[] getArgumentsFields(RuntimeContext ctx, HiClass clazz, HiConstructor constructor, HiNode[] argValues) {
		HiField[] argsFields = null;
		if (argValues != null) {
			int size = argValues.length;
			argsFields = new HiField<?>[size + (constructor.hasVarargs() ? 1 : 0)]; //
			for (int i = 0; i < size; i++) {
				argValues[i].execute(ctx);
				if (ctx.exitFromBlock()) {
					return null;
				}

				HiClass argClass = ctx.value.type;
				HiField argField = HiField.getField(argClass, null, argValues[i].getToken());
				if (argField == null) {
					ctx.throwRuntimeException("argument with type '" + argClass.fullName + "' is not found");
					return null;
				}
				argField.set(ctx, ctx.value);
				if (ctx.exitFromBlock()) {
					return null;
				}

				argField.initialized = true;
				argsFields[i] = argField;

				if (!clazz.isJava() && i < constructor.arguments.length) {
					argsFields[i].name = constructor.arguments[i].name;
				}
			}
		}
		return argsFields;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);

		os.writeBoolean(type != null);
		if (type != null) {
			os.write(type);
		} else {
			os.writeClass(clazz);
		}

		os.writeByte(argValues != null ? argValues.length : 0);
		os.writeArray(argValues);
	}

	public static NodeConstructor decode(DecodeContext os) throws IOException {
		boolean isType = os.readBoolean();
		if (isType) {
			NodeType type = (NodeType) os.read(HiNode.class);
			HiNode[] argValues = os.readArray(HiNode.class, os.readByte());
			return new NodeConstructor(type, argValues);
		} else {
			try {
				HiClass clazz = os.readClass();
				HiNode[] argValues = os.readArray(HiNode.class, os.readByte());
				return new NodeConstructor(clazz, argValues);
			} catch (HiNoClassException exc) {
				HiNode[] argValues = os.readArray(HiNode.class, os.readByte());
				final NodeConstructor node = new NodeConstructor(argValues);
				os.addClassLoadListener(new ClassLoadListener() {
					@Override
					public void classLoaded(HiClass clazz) {
						node.clazz = clazz;
						node.name = clazz.fullName.intern();
					}
				}, exc.getIndex());
				return node;
			}
		}
	}
}
