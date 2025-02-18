package ru.nest.hiscript.ool.model.java;

import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassJava;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.lang.reflect.Method;

public class HiMethodJava extends HiMethod {
	public final static HiMethodJava NULL = new HiMethodJava();

	public Method method;

	private HiMethodJava() {
		super(null, null, null, null, null, "null", (NodeArgument[]) null, null, null);
	}

	public HiMethodJava(ClassResolver classResolver, HiClassJava clazz, Method method, String name) {
		super(clazz, null, null, null, null, name, (NodeArgument[]) null, null, null);
		this.method = method;

		Class[] argsJavaClasses = method.getParameterTypes();
		argsCount = argsJavaClasses.length;
		arguments = new NodeArgument[argsCount];
		argsNames = new String[argsCount];
		for (int i = 0; i < argsCount; i++) {
			Class argsJavaClass = argsJavaClasses[i];
			Type argType;
			if (argsJavaClass.isPrimitive()) {
				argType = Type.getPrimitiveType(argsJavaClass.getName());
			} else if (argsJavaClass.isArray()) {
				Class cellClass = argsJavaClass.getComponentType();
				int dimension = 1;
				while (cellClass.isArray()) {
					cellClass = cellClass.getComponentType();
					dimension++;
				}
				Type cellType = Type.getTopType(cellClass.getSimpleName(), classResolver.getEnv());
				argType = Type.getArrayType(cellType, dimension, classResolver.getEnv());
			} else {
				argType = Type.getTopType(argsJavaClass.getSimpleName(), classResolver.getEnv());
			}
			String argName = "arg" + i;
			arguments[i] = new NodeArgument(argType, argName, new Modifiers(), null);
			arguments[i].clazz = argType.getType().getClass(classResolver);
			argsNames[i] = argName;
		}
	}

	@Override
	public void invoke(RuntimeContext ctx, HiClass clazz, Object object, HiField<?>[] arguments) {
		Object javaObject = ((HiObject) object).userObject;

		Object[] javaArgs = new Object[arguments.length - 1];
		for (int i = 0; i < arguments.length - 1; i++) { // ignore enclosing object
			HiField<?> argument = arguments[i];
			Object argValue = argument.getJava(ctx);
			if (argValue == null && !arguments[i].getClass(ctx).isNull()) {
				ctx.throwRuntimeException("inconvertible java class argument: " + arguments[i].type.fullName);
				return;
			}
			javaArgs[i] = argValue;
		}

		try {
			Object resultJavaValue = method.invoke(javaObject, javaArgs);
			Object resultValue = HiJava.convertFromJava(ctx, resultJavaValue);
			ctx.setValue(resultValue);
		} catch (Exception e) {
			ctx.throwRuntimeException(e.toString());
		}
	}

	@Override
	public boolean isJava() {
		return true;
	}
}
