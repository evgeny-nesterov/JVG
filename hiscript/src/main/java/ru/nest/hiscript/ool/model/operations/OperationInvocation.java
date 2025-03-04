package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeThis;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import java.lang.reflect.Array;

import static ru.nest.hiscript.ool.model.OperationType.*;

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
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		assert node1.clazz != null;

		ctx.nodeValueType.returnType = null;
		HiClass enclosingClass = node1.enclosingClass != null ? node1.enclosingClass : node1.clazz;
		Type enclosingType = node1.enclosingType != null ? node1.enclosingType : node1.type;
		boolean isEnclosingObject = node1.enclosingClass != null;

		ctx.enterObject(enclosingClass, enclosingType, isEnclosingObject);
		if (node2.clazz == null) {
			if (node2.node.getInvocationValueType() != ValueType.UNDEFINED) {
				ctx.invocationNode = node1;
			} else {
				ctx.invocationNode = null;
			}
			node2.get(validationInfo, ctx);
			assert node2.clazz != null;

			if (node1.returnType == NodeValueType.NodeValueReturnType.classValue) {
				if (node1.clazz.isInterface && node2.node.getClass() == NodeThis.class) {
					validationInfo.error("'" + node1.clazz.getNameDescr() + "' is not an enclosing class", node1.node);
				}
			}
		}
		ctx.exit();
		ctx.nodeValueType.returnType = node2.returnType != NodeValueType.NodeValueReturnType.noValue ? node2.returnType : NodeValueType.NodeValueReturnType.runtimeValue; // after ctx.exit()

		// @generics
		HiClass clazz = node2.clazz;
		if (clazz.isGeneric() && enclosingClass != null && enclosingClass.isGeneric()) {
			HiClassGeneric genericClass = (HiClassGeneric) clazz;
			HiClassGeneric genericEnclosingClass = (HiClassGeneric) enclosingClass;
			if (genericClass.sourceClass == genericEnclosingClass.clazz) {
				clazz = genericEnclosingClass.parametersClasses[genericClass.index];
			}
		}
		return clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		switch (v2.valueType) {
			case NAME:
				invokeName(ctx, v1, v2);
				break;
			case METHOD_INVOCATION:
				invokeMethod(ctx, v1, v2);
				break;
			case EXECUTE:
				invokeExecute(ctx, v1, v2);
				break;
			case TYPE_INVOCATION:
				invokeClass(ctx, v1, v2);
				break;
		}
	}

	public void invokeExecute(RuntimeContext ctx, Value v1, Value v2) {
		// a.new B(), where v1=a, v2=new B()
		HiNodeIF valueNode = v2.node;

		HiObject enterObject = null;
		if (valueNode instanceof NodeConstructor || valueNode instanceof NodeArray || valueNode instanceof NodeArrayValue) {
			// Check previous operand on whether it's an object and not an array
			assert v1.valueClass.isObject() && !v1.valueClass.isArray(); // checked in validation
			enterObject = (HiObject) v1.object;
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
		HiField<?> field = null;
		HiClass clazz = null;
		HiObject object;
		// find by pattern: <VARIABLE|ARRAY>.<STATIC CLASS>
		if (v1.valueType == ValueType.VARIABLE || v1.valueType == ValueType.VALUE || v1.valueType == ValueType.ARRAY_INDEX) {
			clazz = v1.valueClass;
			if (clazz.isArray()) {
				if (name.equals("length")) {
					if (v1.object == null) {
						ctx.throwRuntimeException("null pointer");
						return false;
					}
					v1.setIntValue(Array.getLength(v1.object));
					return true;
				}
			} else {
//				HiField fieldDefinition = clazz.getField(ctx, name);
//				if (fieldDefinition != null && fieldDefinition.isStatic()) {
//					field = fieldDefinition;
//				} else {
				object = (HiObject) v1.object;
				if (object == null) {
					ctx.throwRuntimeException("null pointer");
					return false;
				}
				field = object.getField(ctx, name, clazz);
//				}
			}
			assert field != null; // checked in validation
		} else if (v1.valueType == ValueType.CLASS) {
			clazz = v1.valueClass;

			// find by pattern: <ENUM CLASS>.<ENUM VALUE>
			if (clazz.isEnum()) {
				field = ((HiClassEnum) clazz).getEnumValue(name);
			}

			if (field == null) {
				// find by pattern: <CLASS>.<STATIC FIELD>
				field = clazz.getField(ctx, name);
				if (field != null && !field.isStatic()) {
					field = null;
				}

				// find by pattern: <CLASS>.<CLASS>
				if (field == null) {
					clazz = clazz.getInnerClass(ctx, name, true);
				}
			}
		}

		if (field != null) {
			v1.valueType = ValueType.VALUE;
			v1.valueClass = field.getClass(ctx);

			// @generics
			if (v1.valueClass.isGeneric()) {
				v1.valueClass = clazz.resolveGenericClass(ctx, null, (HiClassGeneric) v1.valueClass);
			}

			field.get(ctx, v1);
			if (v1.valueClass.isGeneric()) {
				v1.valueClass = clazz.resolveGenericClass(ctx, null, (HiClassGeneric) v1.valueClass);
			}

			v1.valueType = ValueType.VARIABLE;
			v1.name = name;
			v1.variable = field;
			return true;
		} else {
			assert clazz != null; // checked in validation
			if (nameDimension > 0) {
				clazz = clazz.getArrayClass(nameDimension);
			}
			v1.valueType = ValueType.CLASS;
			v1.valueClass = clazz;
			return true;
		}
	}

	public void invokeClass(RuntimeContext ctx, Value v1, Value v2) {
		assert v1.valueType == ValueType.CLASS; // checked in validation

		HiNodeIF valueNode = v2.node;
		Value oldValue = ctx.value;
		ctx.value = v1;
		try {
			valueNode.execute(ctx, v1.valueClass);
		} finally {
			ctx.value = oldValue;
		}
	}

	public void invokeMethod(RuntimeContext ctx, Value v1, Value v2) {
		String name = v2.name;
		HiNode[] argsValues = v2.arguments;
		ValueType v1ValueType = v1.valueType;
		HiClass v1Clazz = v1.valueClass;
		HiClass clazz = v1Clazz;
		HiObject obj = null;
		Object object = null;
		boolean isStatic = false;
		if (v1.valueType == ValueType.VARIABLE || v1.valueType == ValueType.VALUE || v1.valueType == ValueType.ARRAY_INDEX) {
			if (v1.valueClass.isArray()) {
				object = v1.object;
				obj = null;
			} else {
				obj = (HiObject) v1.object;
				if (obj != null) {
					// infinite cycle
					// obj = obj.getMainObject();
					if (v1.originalValueClass != null && v1.originalValueClass.isLambda()) {
						// In this case object is used as container of original method on which functional method is mapped.
						// Methods names (object and functional) may be different.
						clazz = v1.originalValueClass;
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
		} else {
			assert v1.valueType == ValueType.CLASS; // checked in validation
			isStatic = true;
		}

		// build argument class array and evaluate method arguments
		HiClass[] argsClasses = null;
		HiField<?>[] argsFields = null;
		if (argsValues != null) {
			int size = argsValues.length;
			argsClasses = new HiClass[size];
			argsFields = new HiField[size + 1]; // +vararg
			for (int i = 0; i < size; i++) {
				argsValues[i].execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}

				HiClass valueClass = ctx.value.valueClass;
				argsClasses[i] = valueClass;

				if (valueClass != null) {
					if (ctx.value.valueType == ValueType.NAME) {
						assert NodeIdentifier.resolve(ctx, ctx.value); // node resolved in validation
					}

					argsFields[i] = HiField.getField(valueClass, null, argsValues[i].getToken());
					argsFields[i].set(ctx, ctx.value);
				}
			}
		}

		HiMethod method = null;
		if ((v1ValueType == ValueType.VARIABLE || v1ValueType == ValueType.VALUE) && clazz != v1Clazz) {
			// find super method
			HiMethod superMethod = v1Clazz.searchMethod(ctx, name, argsClasses);
			assert superMethod != null; // checked in validation
			if (superMethod.isDefault() && v1Clazz.isInterface) {
				method = superMethod;
			}
		}

		// find method
		if (method == null) {
			method = clazz.searchMethod(ctx, name, argsClasses);
			assert method != null; // checked in validation
		}

		if (!method.isJava()) {
			assert !(isStatic && !method.isStatic()); // checked in validation
			assert !method.isAbstract(); // checked in validation
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
					v1.valueClass = argsClasses[mainSize + i];
					argsFields[mainSize + i].get(ctx, v1);

					// @autoboxing
					if (varargsClass.isPrimitive()) {
						if (v1.valueClass.isObject()) {
							v1.unbox();
						}
					}

					HiArrays.setArray(varargsClass, array, i, v1);
					if (ctx.exitFromBlock()) {
						return;
					}
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
				HiClass argClass = argsFields[i].getClass(ctx);

				// @autoboxing
				NodeArgument methodArgument = i < method.arguments.length ? method.arguments[i] : method.arguments[method.arguments.length - 1];
				HiClass expectedArgClass = methodArgument.getArgClass(ctx);
				HiClass origArgClass = argClass;
				if (argClass.isPrimitive()) {
					if (expectedArgClass.isObject()) {
						argClass = argClass.getAutoboxClass();
					}
				} else if (expectedArgClass.isPrimitive()) {
					if (argClass.getAutoboxedPrimitiveClass() != null) {
						argClass = argClass.getAutoboxedPrimitiveClass();
					}
				}

				// on null argument update field class from ClazzNull on argument class
				HiField argsField;
				if (origArgClass.isNull()) {
					argsField = HiField.getField(argClass, methodArgument.name, methodArgument.getToken());
					ctx.value.valueClass = HiClassNull.NULL;
					argsField.set(ctx, ctx.value);
					argsFields[i] = argsField;
				} else if (!origArgClass.isArray()) {
					ctx.value.valueClass = origArgClass;
					argsFields[i].get(ctx, ctx.value);
					argsField = HiField.getField(argClass, methodArgument.name, methodArgument.getToken());
					argsField.set(ctx, ctx.value);
					argsFields[i] = argsField;
				} else {
					argsField = argsFields[i];
				}
				// TODO: update array cell type

				if (ctx.exitFromBlock()) {
					return;
				}

				argsField.name = methodArgument.name;
				argsField.initialized = true;
			}
		}

		if (ctx.exitFromBlock()) {
			return;
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
				if (ctx.value.valueClass.isNull()) {
					ctx.value.valueClass = method.returnClass;
				}

				// @autoboxing
				if (method.returnClass != null && method.returnClass != HiClassPrimitive.VOID && method.returnClass.isPrimitive()) {
					if (v1.valueClass.isObject()) {
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
