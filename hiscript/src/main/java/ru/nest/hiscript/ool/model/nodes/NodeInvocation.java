package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.OperationType;
import ru.nest.hiscript.ool.model.Operations;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import java.io.IOException;

import static ru.nest.hiscript.ool.model.nodes.NodeVariable.*;

public class NodeInvocation extends HiNode {
	public NodeInvocation(String name, boolean innerInvocation, HiNode[] arguments) {
		this(name, arguments);
		setInner(innerInvocation);
	}

	public NodeInvocation(String name, HiNode[] arguments) {
		super("invocation", TYPE_INVOCATION, true);
		this.name = name.intern();
		this.arguments = arguments;
	}

	private final String name;

	private final HiNode[] arguments;

	private boolean innerInvocation;

	private HiClass invocationClass;

	private Type invocationType;

	private boolean isEnclosingObject;

	public HiMethod method;

	private HiClass returnClass;

	public void setInner(boolean innerInvocation) {
		this.innerInvocation = innerInvocation;
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		invocationType = ctx.level.enclosingType;
		invocationClass = ctx.consumeInvocationClass();
		isEnclosingObject = invocationClass != null && ctx.level.isEnclosingObject;

		// args has to be evaluated without invocationClass context
		HiClass[] argumentsClasses = new HiClass[arguments != null ? arguments.length : 0];
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				// set null for not valid argument
				HiNode argument = arguments[i];
				if (ctx.level.parent != null) {
					ctx.level.parent.variableNode = argument;
				}
				argumentsClasses[i] = argument.getValueClass(validationInfo, ctx);
			}
		}

		ctx.nodeValueType.resolvedValueVariable = this;
		if (invocationClass != null) {
			method = invocationClass.searchMethod(ctx, name, argumentsClasses);
			if (method != null) {
				HiClass returnClass = method.getReturnClass(ctx, invocationClass, invocationType, argumentsClasses);

				// @generics
				if (invocationType.parameters != null) {
					boolean validArguments = true;
					if (method.argsClasses != null) {
						for (int i = 0; i < method.argsClasses.length; i++) {
							HiClass methodArgumentClass = method.argsClasses[i];
							if (methodArgumentClass.isGeneric()) { // not primitive
								Type argumentType = invocationType.getParameterType((HiClassGeneric) methodArgumentClass);
								HiClass argumentClass = argumentsClasses[i];
								if (argumentType.isWildcard() && !argumentType.isSuper && argumentClass.isPrimitive()) {
									validArguments = false;
									break;
								} else {
									methodArgumentClass = argumentType.getClass(ctx);

									// @autoboxing
									if (argumentClass.isPrimitive()) {
										argumentClass = argumentClass.getAutoboxClass();
									}

									if (!argumentClass.isInstanceof(methodArgumentClass)) {
										validArguments = false;
										break;
									}
								}
							}
						}
					}
					if (!validArguments) {
						String message = "'" + method.getSignatureText(invocationType) + "' in '" + invocationType + "' cannot be applied to '(";
						for (int i = 0; i < argumentsClasses.length; i++) {
							if (i > 0) {
								message += ", ";
							}
							message += argumentsClasses[i].getNameDescr();
						}
						message += "')";
						validationInfo.error(message, getToken());
					}
				}

				// @generics
				Type type = method.returnType;
				if (invocationType.parameters != null) {
					Type resolvedType = null;
					if (returnClass != null && returnClass.generics != null) {
						NodeGeneric nodeGeneric = returnClass.generics.getGeneric(type.name);
						if (nodeGeneric != null) {
							resolvedType = invocationType.getParameterType(nodeGeneric.clazz);
						}
						if (resolvedType == null && type.parameters != null) {
							for (int i = 0; i < type.parameters.length; i++) {
								if (invocationClass.generics.getGenericClass(ctx, type.parameters[i].name) != null) {
									type.parameters[i] = invocationType.getParameterType(returnClass.generics.generics[i].clazz);
								}
							}
						}
					}
					if (resolvedType == null && method.returnClass != null && method.returnClass.isGeneric()) {
						resolvedType = invocationType.getParameterType((HiClassGeneric) method.returnClass);
					}
					if (resolvedType != null) {
						type = resolvedType;
					}
				}

				ctx.nodeValueType.returnType = returnClass != HiClassPrimitive.VOID ? NodeValueType.NodeValueReturnType.runtimeValue : NodeValueType.NodeValueReturnType.noValue;
				ctx.nodeValueType.enclosingClass = returnClass;
				ctx.nodeValueType.enclosingType = type;
				ctx.nodeValueType.type = type;
				return returnClass;
			}
		} else {
			while (ctx != null) {
				if (ctx.clazz != null) {
					method = ctx.clazz.searchMethod(ctx, name, argumentsClasses);
					if (method != null) {
						ctx.nodeValueType.returnType = method.returnClass != HiClassPrimitive.VOID ? NodeValueType.NodeValueReturnType.runtimeValue : NodeValueType.NodeValueReturnType.noValue;
						ctx.nodeValueType.enclosingClass = method.returnClass;
						ctx.nodeValueType.enclosingType = method.returnType;
						return method.returnClass;
					}
				}
				ctx = ctx.parent;
			}
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		returnClass = getValueClass(validationInfo, ctx);

		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());

		// @unnamed
		if (UNNAMED.equals(name)) {
			validationInfo.error("keyword '_' cannot be used as an identifier", token);
			valid = false;
		}

		valid &= method != null;
		if (arguments != null) {
			if (method != null) {
				if (method.clazz.isInterface && method.isPrivate() && invocationClass != null && invocationClass != method.clazz) {
					validationInfo.error("private method '" + method + "' is not accessible from outside of interface " + method.clazz.getNameDescr(), token);
					valid = false;
				}
				int mainArgsCount = method.hasVarargs() ? method.argsCount - 1 : method.argsCount;
				for (int i = 0; i < mainArgsCount; i++) {
					HiNode argument = arguments[i];
					ctx.setObjectContext(method.argsClasses[i], method.arguments[i].getType(), true);
					valid &= argument.validate(validationInfo, ctx) && argument.expectValueClass(validationInfo, ctx, method.argsClasses[i]);
				}
				if (method.hasVarargs()) {
					HiClass varargClass = ((HiClassArray) method.argsClasses[mainArgsCount]).cellClass;
					Type varargType = method.arguments[mainArgsCount].getType().cellType;
					for (int i = mainArgsCount; i < arguments.length; i++) {
						HiNode argument = arguments[i];
						ctx.setObjectContext(varargClass, varargType, true);
						valid &= argument.validate(validationInfo, ctx) && argument.expectValueClass(validationInfo, ctx, varargClass);
					}
				}
				ctx.clearObjectContext();
			} else {
				for (HiNode argument : arguments) {
					valid &= argument.validate(validationInfo, ctx) && argument.expectValue(validationInfo, ctx);
				}
			}
		}

		if (method != null) {
			if (!innerInvocation && !method.isStatic() && !isEnclosingObject) {
				validationInfo.error("non-static method '" + method + "' cannot be referenced from a static context", token);
				valid = false;
			}
		} else {
			validationInfo.error("cannot resolve method '" + name + "'" + (invocationClass != null ? " in '" + invocationClass.getNameDescr() + "'" : ""), token);
			valid = false;
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		if (!innerInvocation) {
			ctx.value.valueType = ValueType.METHOD_INVOCATION;
			ctx.value.name = name;
			ctx.value.arguments = arguments;
			ctx.value.valueClass = returnClass;
		} else {
			Value[] values = ctx.getValues(1);
			try {
				// v1 - contains value as object
				ctx.value.object = ctx.level.object;
				ctx.value.valueClass = ctx.level.clazz;
				ctx.value.originalValueClass = null;
				if (ctx.value.object != null) {
					ctx.value.valueType = ValueType.VALUE;
				} else {
					// static context
					ctx.value.valueType = ValueType.CLASS;
				}

				// v2 - contains method attributes (name, arguments)
				Value value = values[0];
				value.valueType = ValueType.METHOD_INVOCATION;
				value.name = name;
				value.arguments = arguments;
				value.node = this;

				HiOperation operation = Operations.getOperation(OperationType.INVOCATION);
				operation.doOperation(ctx, ctx.value, value);
			} finally {
				ctx.putValues(values);
			}
		}
	}

	// TODO do more usable
	public static void invoke(RuntimeContext ctx, HiObject object, String methodName, HiNode... arguments) {
		Value[] vs = ctx.getValues(1);
		try {
			// v1 - contains value as object
			ctx.value.setObjectValue(object.clazz, object);

			// v2 - contains method attributes (name, arguments)
			Value v = vs[0];
			v.valueType = ValueType.METHOD_INVOCATION;
			v.name = methodName;
			v.arguments = arguments;

			HiOperation o = Operations.getOperation(OperationType.INVOCATION);
			o.doOperation(ctx, ctx.value, v);
		} finally {
			ctx.putValues(vs);
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(name);
		os.writeBoolean(innerInvocation);
		os.writeByte(arguments != null ? arguments.length : 0);
		os.writeArray(arguments);
		os.writeClass(returnClass);
	}

	public static NodeInvocation decode(DecodeContext os) throws IOException {
		NodeInvocation node = new NodeInvocation(os.readUTF(), os.readBoolean(), os.readArray(HiNode.class, os.readByte()));
		os.readClass(clazz -> node.returnClass = clazz);
		return node;
	}
}
