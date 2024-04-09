package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassVar;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.List;

public class NodeMethodReference extends NodeExpression {
	public NodeMethodReference(HiNode node, String name) {
		super("::", TYPE_METHOD_REFERENCE);
		this.node = node;
		this.name = name;
	}

	private final HiNode node;

	private final String name;

	private List<HiMethod> methods;

	private HiClass lambdaClass;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		return HiClassVar.VAR;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass functionalInterface = ctx.consumeInvocationClass();

		boolean valid = node.validate(validationInfo, ctx);
		HiClass clazz = node.getValueClass(validationInfo, ctx);
		methods = clazz.searchMethodsByName(ctx, name);
		if (methods.size() == 0) {
			validationInfo.error("method with name '" + name + "' not found in " + clazz.fullName, token);
			valid = false;
		}

		if (functionalInterface != null) {
			valid &= init(validationInfo, ctx, functionalInterface);
		}
		return valid;
	}

	private boolean init(ValidationInfo validationInfo, CompileClassContext ctx, HiClass functionalInterface) {
		boolean valid = true;
		if (functionalInterface.isInterface) {
			List<HiMethod> abstractMethods = functionalInterface.getAbstractMethods(ctx);
			if (abstractMethods.size() == 1) {
				if (methods.size() > 0) {
					HiMethod functionalMethod = abstractMethods.get(0);
					HiMethod matchedMethod = null;
					for (HiMethod m : methods) {
						HiMethod im = functionalInterface.getInterfaceAbstractMethod(ctx, m.argClasses);
						if (functionalMethod == im) {
							matchedMethod = m;
							break;
						}
					}
					if (matchedMethod != null) {
						lambdaClass = matchedMethod.createLambdaClass(ctx, functionalInterface);
						if (!HiClass.autoCast(ctx, matchedMethod.returnClass, functionalMethod.returnClass, false, true)) {
							validationInfo.error("incompatible return type '" + matchedMethod.returnClass.fullName + "' of method " + matchedMethod.clazz.fullName + "." + matchedMethod + "; expected return type '" + functionalMethod.returnClass.fullName + "'", getToken());
							valid = false;
						}
					} else {
						validationInfo.error("method '" + name + "' of class '" + methods.get(0).clazz + "' doesn't match to the method '" + functionalMethod + "' of functional interface '" + functionalInterface.fullName + "'", getToken());
						valid = false;
					}
				}
			} else {
				validationInfo.error("functional interface not match to '" + functionalInterface.fullName + "'", getToken());
				valid = false;
			}
		} else {
			validationInfo.error("functional interface not match to '" + functionalInterface.fullName + "'", getToken());
			valid = false;
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		node.execute(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		HiObject object = null;
		if (ctx.value.valueType == Value.VALUE) {
			object = ctx.value.object;
		}
		HiMethod.execute(ctx, lambdaClass, object);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.write(node);
		os.writeUTF(name);
	}

	public static NodeMethodReference decode(DecodeContext os) throws IOException {
		return new NodeMethodReference(os.read(HiNode.class), os.readUTF());
	}
}
