package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Arrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;

import java.lang.reflect.Array;

public class OperationInvocation extends BinaryOperation {
	private static Operation instance;

	public static Operation getInstance() {
		if (instance == null) {
			instance = new OperationInvocation();
		}
		return instance;
	}

	private OperationInvocation() {
		super(".", INVOCATION);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		switch (v2.valueType) {
			case Value.NAME:
				invokeName(ctx, v1, v2);
				break;

			case Value.METHOD:
				invokeMethod(ctx, v1, v2);
				break;

			case Value.EXECUTE:
				invokeExecute(ctx, v1, v2);
				break;

			default:
				ctx.throwException("identifier is expected");
		}
	}

	public void invokeClass(RuntimeContext ctx, Value v1, Value v2) {
	}

	public void invokeExecute(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.type.isPrimitive()) {
			ctx.throwException("primitive type doesn't have a subclass " + name);
			return;
		}

		// a.new B(), where v1=a, v2=new B()
		Node valueNode = v2.node;

		HiObject enterObject = null;
		if (valueNode instanceof NodeConstructor || valueNode instanceof NodeArray || valueNode instanceof NodeArrayValue) {
			// Check previous operand on whether it's an object and not an array
			if (!v1.type.isArray() && v1.type.isObject()) {
				enterObject = v1.object;
			} else {
				String typeName = "";
				if (valueNode instanceof NodeConstructor) {
					NodeConstructor constrNode = (NodeConstructor) valueNode;
					typeName = constrNode.name;
				} else if (valueNode instanceof NodeArray) {
					NodeArray arrayNode = (NodeArray) valueNode;
					typeName = arrayNode.type.fullName;
				} else if (valueNode instanceof NodeArrayValue) {
					NodeArrayValue arrayValueNode = (NodeArrayValue) valueNode;
					typeName = arrayValueNode.type.fullName;
				}

				ctx.throwException("class '" + typeName + "' cannot be resolved to a type");
				return;
			}
		}

		if (enterObject != null) {
			ctx.enterObject(enterObject, -1);
		}

		Value oldValue = ctx.value;
		ctx.value = v1;
		try {
			valueNode.execute(ctx);
		} finally {
			ctx.value = oldValue;
			if (enterObject != null) {
				ctx.exit();
			}
		}
	}

	public void invokeName(RuntimeContext ctx, Value v1, Value v2) {
		String name = v2.name;
		if (v1.type.isPrimitive()) {
			ctx.throwException("primitive type doesn't have a field " + name);
			return;
		}

		HiField<?> field = null;
		HiClass clazz = null;
		HiObject object = null;
		// find by pattern: <VARIABLE|ARRAY>.<STATIC CLASS>
		if (v1.valueType == Value.VARIABLE || v1.valueType == Value.VALUE || v1.valueType == Value.ARRAY_INDEX) {
			clazz = v1.type;
			if (clazz.isArray()) {
				if (name.equals("length")) {
					if (v1.array == null) {
						ctx.throwException("null pointer");
						return;
					}

					v1.valueType = Value.VALUE;
					v1.type = HiClass.getPrimitiveClass("int");
					v1.intNumber = Array.getLength(v1.array);
					return;
				}
			} else {
				object = v1.object;
				if (object == null) {
					ctx.throwException("null pointer");
					return;
				}

				field = object.getField(name);
			}

			if (field == null) {
				ctx.throwException("type " + clazz.fullName + " doesn't contain field " + name);
				return;
			}
		} else if (v1.valueType == Value.CLASS) {
			clazz = v1.type;

			// find by pattern: <CLASS>.<STATIC FIELD>
			field = clazz.getField(name);
			if (field != null && !field.getModifiers().isStatic()) {
				field = null;
			}

			// find by pattern: <CLASS>.<STATIC CLASS>
			if (field == null) {
				clazz = clazz.getChildren(ctx, name);
				if (clazz != null && !clazz.modifiers.isStatic()) {
					clazz = null;
				}
			}
		}

		if (field != null) {
			HiClass fieldType = field.getClass(ctx);
			v1.valueType = Value.VALUE;
			v1.type = fieldType;

			field.get(ctx, v1);

			v1.valueType = Value.VARIABLE;
			v1.variable = field;
		} else if (clazz != null) {
			v1.valueType = Value.CLASS;
			v1.type = clazz;
		} else {
			String text = "can't find symbol; variable " + name;
			clazz = ctx.level.clazz;
			if (clazz != null) {
				text += "; location " + clazz.fullName;
			}
			ctx.throwException(text);
		}
	}

	public void invokeMethod(RuntimeContext ctx, Value v1, Value v2) {
		String name = v2.name;
		if (v1.type.isPrimitive()) {
			ctx.throwException("primitive type doesn't have a method " + name);
			return;
		}

		Node[] argValues = v2.arguments;
		int v1ValueType = v1.valueType;
		HiClass v1Clazz = v1.type;
		HiClass clazz = v1Clazz;
		HiObject obj = null;
		Object object = null;
		boolean isStatic = false;
		if (v1.valueType == Value.VARIABLE || v1.valueType == Value.VALUE || v1.valueType == Value.ARRAY_INDEX) {
			if (v1.type.isArray()) {
				object = v1.array;
				obj = null;
			} else {
				obj = v1.object;
				if (obj != null) {
					obj = obj.getMainObject();
					clazz = obj.clazz;
				}
				object = obj;
			}

			if (object == null) {
				ctx.throwException("null pointer");
				return;
			}
		} else if (v1.valueType == Value.CLASS) {
			isStatic = true;
		} else {
			String text = "can't find symbol; variable " + name;
			HiClass location = ctx.level.clazz;
			if (location != null) {
				text += "; location " + location.fullName;
			}
			ctx.throwException(text);
			return;
		}

		// build argument class array and
		// evaluate method arguments
		HiClass[] types = null;
		HiField<?>[] arguments = null;
		if (argValues != null) {
			int size = argValues.length;
			types = new HiClass[size];
			arguments = new HiField[size + 1];
			for (int i = 0; i < size; i++) {
				argValues[i].execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}
				types[i] = ctx.value.type;

				Type type = Type.getType(types[i]);
				arguments[i] = HiField.getField(type, null);
				arguments[i].set(ctx, ctx.value);
			}
		}

		if ((v1ValueType == Value.VARIABLE || v1ValueType == Value.VALUE) && clazz != v1Clazz) {
			// find super method
			HiMethod superMethod = v1Clazz.searchMethod(ctx, name, types);
			if (superMethod == null) {
				ctx.throwException("can't find method " + v1Clazz.fullName + "." + name);
				return;
			}
		}

		// find method
		HiMethod method = clazz.searchMethod(ctx, name, types);
		if (method == null) {
			ctx.throwException("can't find method " + clazz.fullName + "." + name);
			return;
		}

		if (isStatic && !method.modifiers.isStatic()) {
			ctx.throwException("can't invoke not static method from static context");
			return;
		}

		// set names and types of arguments
		if (types != null) {
			int size = types.length;
			if (method.hasVararg()) {
				int varargSize = types.length - method.arguments.length + 1;
				int mainSize = size - varargSize;
				Type varargArrayType = method.arguments[method.arguments.length - 1].type;
				HiClass varargClass = varargArrayType.getCellType().getClass(ctx);
				HiClass varargArrayClass = varargArrayType.getClass(ctx);
				HiField<?> varargField = HiField.getField(varargArrayType, null);

				Class<?> _varargClass = Arrays.getClass(varargClass, 0);
				Object array = Array.newInstance(_varargClass, varargSize);
				for (int i = 0; i < varargSize; i++) {
					v1.type = types[mainSize + i];
					arguments[mainSize + i].get(ctx, v1);
					Arrays.setArrayIndex(varargClass, array, i, v1, v2);
				}

				ctx.value.array = array;
				ctx.value.type = varargArrayClass;
				varargField.set(ctx, ctx.value);

				arguments[mainSize] = varargField;
				int newSize = mainSize + 1;
				for (int i = newSize; i < size; i++) {
					arguments[i] = null;
				}
				size = newSize;
			}

			for (int i = 0; i < size; i++) {
				HiClass argClass = arguments[i].getClass(ctx);

				// on null argument update field class from ClazzNull on argument class
				if (argClass.isNull()) {
					arguments[i] = HiField.getField(method.arguments[i].type, null);
					ctx.value.type = HiClassNull.NULL;
					arguments[i].set(ctx, ctx.value);
				} else if (!argClass.isArray()) {
					ctx.value.type = argClass;
					arguments[i].get(ctx, ctx.value);
					arguments[i] = HiField.getField(method.arguments[i].type, null);
					arguments[i].set(ctx, ctx.value);
				}
				// TODO: update array cell type

				arguments[i].name = method.argNames[i];
				arguments[i].initialized = true;
			}
		}

		// enter into method
		ctx.enterMethod(method, obj, -1);
		try {
			// register variables in method
			ctx.addVariables(arguments);

			// perform method invocation
			Value oldValue = ctx.value;
			try {
				ctx.value = v1;
				method.invoke(ctx, clazz, object, arguments);
				if (ctx.exitFromBlock()) {
					return;
				}
			} finally {
				ctx.value = oldValue;
			}
		} finally {
			// exit from method
			ctx.exit();
			ctx.isReturn = false;
		}
	}
}
