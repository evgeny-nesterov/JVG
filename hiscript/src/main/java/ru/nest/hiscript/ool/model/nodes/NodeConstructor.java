package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class NodeConstructor extends HiNode {
	public NodeConstructor(NodeType nodeType, HiNode[] argsValues) {
		super("constructor", TYPE_CONSTRUCTOR, true);
		this.nodeType = nodeType;
		this.type = nodeType.getType();
		this.argsValues = argsValues;
		name = type.getType().fullName;
	}

	public NodeConstructor(HiClass clazz, Type type, HiNode[] argsValues) {
		super("constructor", TYPE_CONSTRUCTOR, true);
		this.clazz = clazz;
		this.type = type;
		this.argsValues = argsValues;
		name = clazz.getFullName(clazz.getClassLoader());
	}

	private NodeConstructor(Type type, HiNode[] argsValues) {
		super("constructor", TYPE_CONSTRUCTOR, true);
		this.type = type;
		this.argsValues = argsValues;
		// clazz has to be deserialized
	}

	public NodeConstructor(HiConstructor constructor) {
		super("constructor", TYPE_CONSTRUCTOR, true);

		HiNode[] argsValues = null;
		if (constructor.arguments != null) {
			argsValues = new HiNode[constructor.arguments.length];
			for (int i = 0; i < argsValues.length; i++) {
				NodeArgument arg = constructor.arguments[i];
				argsValues[i] = new NodeIdentifier(arg.name, 0);
			}
		}

		this.clazz = constructor.clazz;
		this.type = constructor.type;
		this.argsValues = argsValues;
		this.constructor = constructor;
		name = constructor.clazz.getFullName(clazz.getClassLoader());
	}

	public NodeType nodeType;

	public HiClass[] argsClasses;

	public HiNode[] argsValues;

	private String name;

	private HiClass clazz;

	private Type type;

	private Type[] superTypes;

	private HiConstructor constructor;

	public String getName() {
		return name;
	}

	// @generics
	public boolean validateDeclarationGenericType(Type type, ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (this.type.parameters != null && this.type.parameters.length == 0) {
			this.type = type;
		} else {
			valid = this.type.validateMatch(type, validationInfo, ctx, getToken());
		}

		if (valid && constructor != null && constructor.bodyConstructorType == HiConstructor.BodyConstructorType.SUPER && clazz.superClass != null) {
			NodeConstructor nc = this;
			List<Type> superTypes = null;
			Type t = nc.type;
			while (nc != null) {
				HiClass c = nc.clazz;

				// @generics
				if (c.superClass.generics != null && t.parameters != null && t.parameters.length > 0) {
					int genericParametersCount = c.superClass.generics.generics.length;
					Type[] superTypeParameters = new Type[genericParametersCount];
					for (int i = 0; i < genericParametersCount; i++) {
						NodeGeneric superGeneric = c.superClass.generics.generics[i];
						Type superTypeParameter = t.parameters[i];
						superTypeParameters[i] = superTypeParameter;
						HiClass superTypeClass = superTypeParameter.getClass(ctx);
						if (superTypeClass != null && !superTypeClass.isInstanceof(superGeneric.clazz.clazz)) {
							validationInfo.error("cannot cast " + superTypeClass.getNameDescr() + " to " + superGeneric.clazz.getNameDescr(), superGeneric.getToken());
						}
					}
					Type superType = Type.getParameterizedType(t, superTypeParameters);
					if (superTypes == null) {
						superTypes = new ArrayList<>(1);
					}
					superTypes.add(superType);
					t = superType;
					nc = constructor.bodyConstructor;
				} else {
					break;
				}
			}
			if (superTypes != null) {
				this.superTypes = superTypes.toArray(new Type[superTypes.size()]);
			}
		}
		return valid;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = clazz != null ? clazz : type.getClass(ctx);
		ctx.nodeValueType.enclosingType = type;
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;

		ctx.nodeValueType.type = ctx.nodeValueType.enclosingType;
		return ctx.nodeValueType.enclosingClass;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		return validate(validationInfo, ctx, clazz);
	}

	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx, HiClass instanceClass) {
		boolean isInstanceClass = clazz == instanceClass;

		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		if (argsValues != null) {
			int size = argsValues.length;
			argsClasses = new HiClass[size];
			for (int i = 0; i < size; i++) {
				HiNode argValue = argsValues[i];
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
			validationInfo.error("class not found: " + type.fullName, getToken());
			return false;
		} else if (clazz.isInterface) {
			validationInfo.error("cannot create object from interface '" + clazz.getNameDescr() + "'", getToken());
			return false;
		} else if (clazz.isEnum()) {
			validationInfo.error("enum types cannot be instantiated", getToken());
		}

		// @generics
		if (clazz.generics != null) {
			valid &= type.validateClass(clazz, validationInfo, ctx, getToken());
		} else if (nodeType != null && type.parameters != null) {
			validationInfo.error("type '" + type + "' does not have type parameters", getToken());
			valid = false;
		}
		if (type != null && type.parameters != null) {
			for (int i = 0; i < type.parameters.length; i++) {
				Type parameterType = type.parameters[i];
				if (parameterType.isWildcard()) {
					validationInfo.error("wildcard type '" + parameterType + "' cannot be instantiated directly", getToken());
					valid = false;
				}
			}
		}

		if (clazz.type == HiClass.CLASS_TYPE_ANONYMOUS) {
			valid &= clazz.validate(validationInfo, ctx);
		}

		if (isInstanceClass) {
			if (clazz.isStatic() && ctx.level.enclosingClass != null && ctx.level.isEnclosingObject) {
				validationInfo.error("qualified new of static class", getToken());
				valid = false;
			} else if (!clazz.isStatic()) {
				if (ctx.level.enclosingClass == null && getName().indexOf('.') != -1) {
					validationInfo.error("'" + getName() + "' is not an enclosing class", getToken());
					valid = false;
				}
				if (ctx.level.enclosingClass != null && !ctx.level.isEnclosingObject) {
					validationInfo.error("cannot create", getToken());
					valid = false;
				}
			}

			if (clazz.isAbstract()) {
				validationInfo.error("'" + clazz.getNameDescr() + "' is abstract; cannot be instantiated", getToken());
				valid = false;
			}
		}

		// resolve constructor
		if (!clazz.isInterface) {
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
				validationInfo.error("constructor not found: " + clazz.getNameDescr(), getToken());
				valid = false;
			}
		}
		return valid;
	}

	@Override
	public int getInvocationValueType() {
		return Value.EXECUTE;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// init by class
		clazz.init(ctx);
		ctx.addClass(clazz);

		HiObject outboundObject = ctx.getOutboundObject(clazz);
		invokeConstructor(ctx, clazz, type, constructor, argsClasses, argsValues, null, outboundObject);
	}

	public static void invokeConstructor(RuntimeContext ctx, HiClass clazz, Type type, HiNode[] argsValues, HiObject object, HiObject outboundObject) {
		// build argument class array and evaluate method arguments
		HiClass[] argsClasses = null;
		if (argsValues != null) {
			int size = argsValues.length;
			argsClasses = new HiClass[size];
			for (int i = 0; i < size; i++) {
				argsValues[i].execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}
				argsClasses[i] = ctx.value.valueClass;
			}
		}

		HiConstructor constructor = clazz.searchConstructor(ctx, argsClasses);

		RuntimeContext.StackLevel level = ctx.level;
		while (level != null) {
			if (level.levelType == RuntimeContext.CONSTRUCTOR && level.constructor == constructor) {
				ctx.throwRuntimeException("recursive constructor invocation");
				return;
			}
			level = level.parent;
		}

		invokeConstructor(ctx, clazz, type, constructor, argsClasses, argsValues, object, outboundObject);
	}

	public static void invokeConstructor(RuntimeContext ctx, HiClass clazz, Type type, HiConstructor constructor, HiClass[] argsClasses, HiNode[] argsValues, HiObject object, HiObject outboundObject) {
		HiField[] argsFields = getArgumentsFields(ctx, clazz, constructor, argsValues);
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
					ctx.value.valueClass = argsClasses[mainSize + i];
					argsFields[mainSize + i].get(ctx, ctx.value);
					HiArrays.setArray(varargsClass, array, i, ctx.value);
				}

				ctx.value.valueClass = varargsArrayClass;
				ctx.value.object = array;
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
					ctx.value.valueClass = HiClassNull.NULL;
					argsFields[i].set(ctx, ctx.value);
				} else if (!argClass.isArray()) {
					ctx.value.valueClass = argClass;
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

		constructor.newInstance(ctx, type, argsFields, object, outboundObject);
	}

	/**
	 * build argument class array and evaluate method arguments
	 */
	public static HiField[] getArgumentsFields(RuntimeContext ctx, HiClass clazz, HiConstructor constructor, HiNode[] argsValues) {
		HiField[] argsFields = null;
		if (argsValues != null) {
			int size = argsValues.length;
			argsFields = new HiField<?>[size + (constructor.hasVarargs() ? 1 : 0)]; //
			for (int i = 0; i < size; i++) {
				HiNode argValue = argsValues[i];
				argValue.execute(ctx);

				HiField argField = null;
				HiClass argClass = ctx.value.valueClass;

				// @autoboxing
				if (argClass.isPrimitive()) {
					HiClass dstArgClass = constructor.arguments[i < constructor.arguments.length ? i : constructor.arguments.length - 1].getArgClass(ctx);
					if (dstArgClass.isObject()) {
						HiObject autoboxValue = ((HiClassPrimitive) argClass).box(ctx, ctx.value);
						argField = HiField.getField(argClass.getAutoboxClass(), null, argValue.getToken());
						argField.set(autoboxValue, autoboxValue.clazz);
					}
				}

				if (argField == null) {
					argField = HiField.getField(argClass, null, argValue.getToken());
					argField.set(ctx, ctx.value);
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
		os.writeByte(argsValues != null ? argsValues.length : 0);
		os.writeArray(argsValues);
		os.writeType(type);
		os.writeTypes(superTypes);
		os.writeClass(clazz);
		os.writeClasses(argsClasses);
		constructor.codeLink(os);
	}

	public static NodeConstructor decode(DecodeContext os) throws IOException {
		HiNode[] argsValues = os.readArray(HiNode.class, os.readByte());
		NodeConstructor node = new NodeConstructor(os.readType(), argsValues);
		node.superTypes = os.readTypes();
		os.readClass(clazz -> node.clazz = clazz);
		node.argsClasses = os.readClasses();
		HiConstructor.decodeLink(os, constructor -> node.constructor = constructor);
		return node;
	}
}
