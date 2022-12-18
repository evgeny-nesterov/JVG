package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
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
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

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
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node1, NodeExpressionNoLS.NodeOperandType node2) {
		if (node1.type != null) {
			ctx.enterObject(node1.type);
			if (node2.type == null) {
				node2.type = node2.getType(validationInfo, ctx);
				node2.isValue = node2.node.isValue();
			}
			ctx.exit();
			return node2.type;
		}
		return null;
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
				ctx.throwRuntimeException("identifier is expected");
		}
	}

	public void invokeExecute(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.type.isPrimitive()) {
			ctx.throwRuntimeException("primitive type doesn't have a subclass " + name);
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
					NodeConstructor constructorNode = (NodeConstructor) valueNode;
					typeName = constructorNode.name;
				} else if (valueNode instanceof NodeArray) {
					NodeArray arrayNode = (NodeArray) valueNode;
					typeName = arrayNode.type.fullName;
				} else if (valueNode instanceof NodeArrayValue) {
					NodeArrayValue arrayValueNode = (NodeArrayValue) valueNode;
					typeName = arrayValueNode.type.fullName;
				}

				ctx.throwRuntimeException("class '" + typeName + "' cannot be resolved to a type");
				return;
			}
		}

		if (enterObject != null) {
			ctx.enterObject(enterObject, null);
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

	public boolean invokeName(RuntimeContext ctx, Value v1, Value v2) {
		return invokeName(ctx, v1, v2.name);
	}

	public static boolean invokeName(RuntimeContext ctx, Value v1, String name) {
		if (v1.type.isPrimitive()) {
			ctx.throwRuntimeException("primitive type doesn't have a field " + name);
			return false;
		}

		HiField<?> field = null;
		HiClass clazz = null;
		HiObject object;
		// find by pattern: <VARIABLE|ARRAY>.<STATIC CLASS>
		if (v1.valueType == Value.VARIABLE || v1.valueType == Value.VALUE || v1.valueType == Value.ARRAY_INDEX) {
			clazz = v1.type;
			if (clazz.isArray()) {
				if (name.equals("length")) {
					if (v1.array == null) {
						ctx.throwRuntimeException("null pointer");
						return false;
					}

					v1.valueType = Value.VALUE;
					v1.type = HiClass.getPrimitiveClass("int");
					v1.intNumber = Array.getLength(v1.array);
					return true;
				}
			} else {
				object = v1.object;
				if (object == null) {
					ctx.throwRuntimeException("null pointer");
					return false;
				}

				field = object.getField(ctx, name, clazz);
			}

			if (field == null) {
				ctx.throwRuntimeException("type " + clazz.fullName + " doesn't contain field " + name);
				return false;
			}
		} else if (v1.valueType == Value.CLASS) {
			clazz = v1.type;

			// find by pattern: <ENUM CLASS>.<ENUM VALUE>
			if (clazz.isEnum()) {
				field = ((HiClassEnum) clazz).getEnumValue(name);
			}

			if (field == null) {
				// find by pattern: <CLASS>.<STATIC FIELD>
				field = clazz.getField(ctx, name);
				if (field != null && !field.getModifiers().isStatic()) {
					field = null;
				}

				// find by pattern: <CLASS>.<CLASS>
				if (field == null) {
					clazz = clazz.getChild(ctx, name);
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
			return true;
		} else if (clazz != null) {
			v1.valueType = Value.CLASS;
			v1.type = clazz;
			return true;
		} else {
			String text = "can't find symbol; variable " + name;
			clazz = ctx.level.clazz;
			if (clazz != null) {
				text += "; location " + clazz.fullName;
			}
			ctx.throwRuntimeException(text);
			return false;
		}
	}

	public void invokeMethod(RuntimeContext ctx, Value v1, Value v2) {
		String name = v2.name;
		if (v1.type.isPrimitive()) {
			ctx.throwRuntimeException("primitive type doesn't have a method " + name);
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
					// inf cycle
					// obj = obj.getMainObject();
					clazz = obj.clazz;
				}
				object = obj;
			}

			if (object == null) {
				ctx.throwRuntimeException("null pointer");
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
			ctx.throwRuntimeException(text);
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
				if (type != null) {
					arguments[i] = HiField.getField(type, null);
					arguments[i].set(ctx, ctx.value);
				}
			}
		}

		if ((v1ValueType == Value.VARIABLE || v1ValueType == Value.VALUE) && clazz != v1Clazz) {
			// find super method
			HiMethod superMethod = v1Clazz.searchMethod(ctx, name, types);
			if (superMethod == null) {
				ctx.throwRuntimeException("can't find method " + v1Clazz.fullName + "." + name);
				return;
			}
		}

		// find method
		HiMethod method = clazz.searchMethod(ctx, name, types);
		if (method == null) {
			if (ctx.exception == null) {
				ctx.throwRuntimeException("can't find method " + clazz.fullName + "." + name);
			}
			return;
		}

		if (!method.isJava()) {
			if (isStatic && !method.modifiers.isStatic()) {
				ctx.throwRuntimeException("can't invoke not static method from static context");
				return;
			}

			if (method.modifiers.isAbstract()) {
				ctx.throwRuntimeException("can't invoke abstract method");
			}
		}

		// set names and types of arguments
		if (types != null) {
			int size = types.length;
			if (method.hasVarargs()) {
				int varargsSize = types.length - method.arguments.length + 1;
				int mainSize = size - varargsSize;
				Type varargsArrayType = method.arguments[method.arguments.length - 1].getType();
				HiClass varargsClass = varargsArrayType.getCellType().getClass(ctx);
				HiClass varargsArrayClass = varargsArrayType.getClass(ctx);
				HiField<?> varargsField = HiField.getField(varargsArrayType, null);

				Class<?> _varargClass = Arrays.getClass(varargsClass, 0);
				Object array = Array.newInstance(_varargClass, varargsSize);
				for (int i = 0; i < varargsSize; i++) {
					v1.type = types[mainSize + i];
					arguments[mainSize + i].get(ctx, v1);
					Arrays.setArrayIndex(varargsClass, array, i, v1, v2);
				}

				ctx.value.type = varargsArrayClass;
				ctx.value.array = array;
				varargsField.set(ctx, ctx.value);

				arguments[mainSize] = varargsField;
				int newSize = mainSize + 1;
				for (int i = newSize; i < size; i++) {
					arguments[i] = null;
				}
				size = newSize;
			}

			for (int i = 0; i < size; i++) {
				HiClass argClass = arguments[i] != null ? arguments[i].getClass(ctx) : HiClassNull.NULL;

				// on null argument update field class from ClazzNull on argument class
				if (argClass.isNull()) {
					arguments[i] = HiField.getField(method.arguments[i].getType(), null);
					ctx.value.type = HiClassNull.NULL;
					arguments[i].set(ctx, ctx.value);
				} else if (!argClass.isArray()) {
					ctx.value.type = argClass;
					arguments[i].get(ctx, ctx.value);
					arguments[i] = HiField.getField(method.arguments[i].getType(), null);
					arguments[i].set(ctx, ctx.value);
				}
				// TODO: update array cell type

				arguments[i].name = method.argNames[i];
				arguments[i].initialized = true;
			}
		}

		// enter into method
		ctx.enterMethod(method, obj);
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
