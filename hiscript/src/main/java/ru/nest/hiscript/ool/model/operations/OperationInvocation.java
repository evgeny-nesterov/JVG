package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeThis;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.lang.reflect.Array;

public class OperationInvocation extends BinaryOperation {
	private static HiOperation instance;

	public static HiOperation getInstance() {
		if (instance == null) {
			instance = new OperationInvocation();
		}
		return instance;
	}

	private OperationInvocation() {
		super(INVOCATION);
	}

	@Override
	public boolean isStatement() {
		return true;
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		ctx.nodeValueType.returnType = null;
		if (node1.type != null) {
			HiClass enclosingClass = node1.enclosingClass != null ? node1.enclosingClass : node1.type;
			ctx.enterObject(enclosingClass, node1.enclosingClass != null);
			if (node2.type == null) {
				if (node2.node.getInvocationValueType() != -1) {
					ctx.invocationNode = node1;
				} else {
					ctx.invocationNode = null;
				}
				node2.get(validationInfo, ctx);
				if (node2.type == null) {
					validationInfo.error("cannot resolve expression type", node2.node.getToken());
				}
				if (node1.returnType == NodeValueType.NodeValueReturnType.classValue && node1.type.isInterface && node2.node.getClass() == NodeThis.class) {
					validationInfo.error("'" + node1.type.getNameDescr() + "' is not an enclosing class", node1.node.getToken());
				}
			}
			ctx.exit();
			ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue; // after ctx.exit()
			return node2.type;
		}
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		return null;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		switch (v2.valueType) {
			case Value.NAME:
				invokeName(ctx, v1, v2);
				break;
			case Value.METHOD_INVOCATION:
				invokeMethod(ctx, v1, v2);
				break;
			case Value.EXECUTE:
				invokeExecute(ctx, v1, v2);
				break;
			case Value.TYPE_INVOCATION:
				invokeClass(ctx, v1, v2);
				break;
			default:
				ctx.throwRuntimeException("identifier is expected");
				break;
		}
	}

	public void invokeExecute(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.type.isPrimitive()) {
			ctx.throwRuntimeException("primitive type doesn't have a subclass " + name);
			return;
		}

		// a.new B(), where v1=a, v2=new B()
		HiNodeIF valueNode = v2.node;

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
		return invokeName(ctx, v1, v2.name, v2.nameDimensions);
	}

	public static boolean invokeName(RuntimeContext ctx, Value v1, String name, int nameDimension) {
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
					v1.type = HiClassPrimitive.INT;
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
					clazz = clazz.getInnerClass(ctx, name, true);
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
			if (nameDimension > 0) {
				clazz = clazz.getArrayClass(nameDimension);
			}
			v1.valueType = Value.CLASS;
			v1.type = clazz;
			return true;
		} else {
			String text = "cannot find symbol; variable " + name;
			clazz = ctx.level.clazz;
			if (clazz != null) {
				text += "; location " + clazz.fullName;
			}
			ctx.throwRuntimeException(text);
			return false;
		}
	}

	public void invokeClass(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.valueType != Value.CLASS) {
			String text = "cannot find symbol; variable " + name;
			HiClass clazz = ctx.level.clazz;
			if (clazz != null) {
				text += "; location " + clazz.fullName;
			}
			ctx.throwRuntimeException(text);
			return;
		}

		HiNodeIF valueNode = v2.node;
		Value oldValue = ctx.value;
		ctx.value = v1;
		try {
			valueNode.execute(ctx, v1.type);
		} finally {
			ctx.value = oldValue;
		}
	}

	public void invokeMethod(RuntimeContext ctx, Value v1, Value v2) {
		String name = v2.name;
		if (v1.type.isPrimitive()) {
			ctx.throwRuntimeException("primitive type doesn't have a method " + name);
			return;
		}

		HiNode[] argValues = v2.arguments;
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
					if (v1.lambdaClass != null) {
						// In this case object is used as container of original method on which functional method is mapped.
						// Methods names (object and functional) may be different.
						clazz = v1.lambdaClass;
					} else {
						clazz = obj.clazz;
					}
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
			String text = "cannot find symbol; variable " + name;
			HiClass location = ctx.level.clazz;
			if (location != null) {
				text += "; location " + location.fullName;
			}
			ctx.throwRuntimeException(text);
			return;
		}

		// build argument class array and evaluate method arguments
		HiClass[] argsClasses = null;
		HiField<?>[] argsFields = null;
		if (argValues != null) {
			int size = argValues.length;
			argsClasses = new HiClass[size];
			argsFields = new HiField[size + 1]; // +vararg
			for (int i = 0; i < size; i++) {
				argValues[i].execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}

				HiClass type = ctx.value.type;
				argsClasses[i] = type;

				if (type != null) {
					argsFields[i] = HiField.getField(type, null, argValues[i].getToken());
					argsFields[i].set(ctx, ctx.value);
				}
			}
		}

		HiMethod method = null;
		if ((v1ValueType == Value.VARIABLE || v1ValueType == Value.VALUE) && clazz != v1Clazz) {
			// find super method
			HiMethod superMethod = v1Clazz.searchMethod(ctx, name, argsClasses);
			if (superMethod == null) {
				ctx.throwRuntimeException("cannot find method " + v1Clazz.fullName + "." + name);
				return;
			}
			if (superMethod.modifiers.isDefault() && v1Clazz.isInterface) {
				method = superMethod;
			}
		}

		// find method
		if (method == null) {
			method = clazz.searchMethod(ctx, name, argsClasses);
			if (method == null) {
				ctx.throwRuntimeException("cannot find method " + clazz.fullName + "." + name);
				return;
			}
		}

		if (!method.isJava()) {
			if (isStatic && !method.modifiers.isStatic()) {
				ctx.throwRuntimeException("cannot invoke not static method from static context");
				return;
			}

			if (method.modifiers.isAbstract()) {
				ctx.throwRuntimeException("cannot invoke abstract method");
				return;
			}
		}

		// set names and types of arguments
		if (argsClasses != null) {
			int size = argsClasses.length;
			if (method.hasVarargs()) {
				int varargsSize = argsClasses.length - method.arguments.length + 1;
				int mainSize = size - varargsSize;
				Type varargsArrayType = method.arguments[method.arguments.length - 1].getType();
				HiClass varargsClass = varargsArrayType.getCellType().getClass(ctx);
				HiClass varargsArrayClass = varargsArrayType.getClass(ctx);
				HiField<?> varargsField = HiField.getField(varargsArrayClass, method.arguments[method.arguments.length - 1].name, method.arguments[method.arguments.length - 1].getToken());

				Class<?> _varargClass = HiArrays.getClass(varargsClass, 0);
				Object array = Array.newInstance(_varargClass, varargsSize);
				for (int i = 0; i < varargsSize; i++) {
					v1.type = argsClasses[mainSize + i];
					argsFields[mainSize + i].get(ctx, v1);

					// autobox
					if (varargsClass.isPrimitive()) {
						if (v1.type.isObject()) {
							v1.unbox();
						}
					}

					HiArrays.setArray(varargsClass, array, i, v1);
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

				// autobox
				HiClass expectedArgClass = method.arguments[i < method.arguments.length ? i : method.arguments.length - 1].getArgClass();
				HiClass origArgClass = argClass;
				if (argClass.isPrimitive() && expectedArgClass.isObject()) {
					argClass = argClass.getAutoboxClass();
				}

				// on null argument update field class from ClazzNull on argument class
				HiField argsField;
				if (origArgClass.isNull()) {
					argsField = HiField.getField(argClass, method.arguments[i].name, method.arguments[i].getToken());
					ctx.value.type = HiClassNull.NULL;
					argsField.set(ctx, ctx.value);
					argsFields[i] = argsField;
				} else if (!origArgClass.isArray()) {
					ctx.value.type = origArgClass;
					argsFields[i].get(ctx, ctx.value);
					argsField = HiField.getField(argClass, method.arguments[i].name, method.arguments[i].getToken());
					argsField.set(ctx, ctx.value);
					argsFields[i] = argsField;
				} else {
					argsField = argsFields[i];
				}
				// TODO: update array cell type

				argsField.name = method.argNames[i];
				argsField.initialized = true;
			}
		}

		// enter into method
		ctx.enterMethod(method, obj);
		try {
			// register variables in method
			ctx.addVariables(argsFields);

			// perform method invocation
			Value oldValue = ctx.value;
			try {
				ctx.value = v1;
				method.invoke(ctx, clazz, object, argsFields);

				// autobox
				if (method.returnClass != null && method.returnClass != TYPE_VOID && method.returnClass.isPrimitive()) {
					if (v1.type.isObject()) {
						v1.unbox();
					}
				}

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
