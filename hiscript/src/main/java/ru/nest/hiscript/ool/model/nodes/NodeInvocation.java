package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.Operations;
import ru.nest.hiscript.ool.model.OperationsIF;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeInvocation extends HiNode {
	public NodeInvocation(String name, boolean innerInvocation, HiNode[] arguments) {
		this(name, arguments);
		setInner(innerInvocation);
	}

	public NodeInvocation(String name, HiNode[] arguments) {
		super("invocation", TYPE_INVOCATION);
		this.name = name; // .intern();
		this.arguments = arguments;
	}

	private String name;

	private HiNode[] arguments;

	private boolean innerInvocation;

	private HiClass invocationClass;

	private boolean isEnclosingObject;

	private HiMethod method;

	public void setInner(boolean innerInvocation) {
		this.innerInvocation = innerInvocation;
	}

	@Override
	public HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		invocationClass = ctx.consumeInvocationClass();
		isEnclosingObject = invocationClass != null ? ctx.level.isEnclosingObject : false;

		// args has to be evaluated without invocationClass context
		HiClass[] argumentsClasses = new HiClass[arguments != null ? arguments.length : 0];
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				// set null for not valid argument
				argumentsClasses[i] = arguments[i].getValueClass(validationInfo, ctx);
			}
		}

		ctx.nodeValueType.resolvedValueVariable = this;
		if (invocationClass != null) {
			method = invocationClass.searchMethod(ctx, name, argumentsClasses);
			if (method != null) {
				return method.returnClass;
			}
		} else {
			while (ctx != null) {
				method = ctx.clazz.searchMethod(ctx, name, argumentsClasses);
				if (method != null) {
					return method.returnClass;
				}
				ctx = ctx.parent;
			}
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass returnClass = getValueClass(validationInfo, ctx);

		boolean valid = method != null;
		if (arguments != null) {
			for (HiNode argument : arguments) {
				valid &= argument.validate(validationInfo, ctx) && argument.expectValue(validationInfo, ctx);
			}
		}

		if (method != null) {
			if (method.modifiers.isAbstract()) {
				validationInfo.error("Cannot invoke abstract method", token);
				valid = false;
			}
			if (!innerInvocation && !method.modifiers.isStatic() && !isEnclosingObject) {
				validationInfo.error("Non-static method '" + method + "' cannot be referenced from a static context", token);
				valid = false;
			}
		} else {
			validationInfo.error("Cannot resolve method '" + name + "'" + (invocationClass != null ? " in '" + invocationClass.fullName + "'" : ""), token);
			valid = false;
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		if (!innerInvocation) {
			ctx.value.valueType = Value.METHOD;
			ctx.value.name = name;
			ctx.value.arguments = arguments;
		} else {
			Value[] vs = ctx.getValues(1);
			try {
				// v1 - contains value as object
				ctx.value.object = ctx.level.object;
				ctx.value.type = ctx.level.clazz;
				if (ctx.value.object != null) {
					ctx.value.valueType = Value.VALUE;
				} else {
					// static context
					ctx.value.valueType = Value.CLASS;
				}

				// v2 - contains method attributes (name, arguments)
				Value v = vs[0];
				v.valueType = Value.METHOD;
				v.name = name;
				v.arguments = arguments;

				HiOperation o = Operations.getOperation(OperationsIF.INVOCATION);
				o.doOperation(ctx, ctx.value, v);
			} finally {
				ctx.putValues(vs);
			}
		}
	}

	// TODO: do more usable
	public static void invoke(RuntimeContext ctx, HiObject object, String methodName, HiNode... arguments) {
		Value[] vs = ctx.getValues(1);
		try {
			// v1 - contains value as object
			ctx.value.valueType = Value.VALUE;
			ctx.value.object = object;
			ctx.value.type = object.clazz;

			// v2 - contains method attributes (name, arguments)
			Value v = vs[0];
			v.valueType = Value.METHOD;
			v.name = methodName;
			v.arguments = arguments;

			HiOperation o = Operations.getOperation(OperationsIF.INVOCATION);
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
	}

	public static NodeInvocation decode(DecodeContext os) throws IOException {
		return new NodeInvocation(os.readUTF(), os.readBoolean(), os.readArray(HiNode.class, os.readByte()));
	}
}
