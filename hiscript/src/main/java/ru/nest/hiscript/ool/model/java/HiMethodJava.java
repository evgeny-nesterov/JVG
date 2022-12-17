package ru.nest.hiscript.ool.model.java;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassJava;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;

import java.lang.reflect.Method;

public class HiMethodJava extends HiMethod {
	public final static HiMethodJava NULL = new HiMethodJava();

	public Method method;

	private HiMethodJava() {
		super(null, null, null, null, "null", (NodeArgument[]) null, null, null, null);
	}

	public HiMethodJava(HiClassJava clazz, Method method, String name) {
		super(clazz, null, null, null, name, (NodeArgument[]) null, null, null, null);
		this.method = method;

		Class[] argJavaClasses = method.getParameterTypes();
		argCount = argJavaClasses.length;
		arguments = new NodeArgument[argCount];
		argNames = new String[argCount];
		for (int i = 0; i < argCount; i++) {
			Class argJavaClass = argJavaClasses[i];
			Type argType = null;
			if (argJavaClass.isPrimitive()) {
				argType = Type.getPrimitiveType(argJavaClass.getName());
			} else if (argJavaClass == String.class) {
				argType = Type.getTopType(HiClass.STRING_CLASS_NAME);
			}
			String argName = "arg" + i;
			arguments[i] = new NodeArgument(argType, argName, new Modifiers(), null);
			argNames[i] = argName;
		}
	}

	@Override
	public void invoke(RuntimeContext ctx, HiClass type, Object object, HiField<?>[] arguments) {
		Object javaObject = ((HiObject) object).userObject;

		Object[] javaArgs = new Object[arguments.length - 1];
		for (int i = 0; i < arguments.length - 1; i++) { // ignore enclosing object
			HiField<?> argument = arguments[i];
			Object argValue = argument.getJava(ctx);
			if (argValue == null && !arguments[i].type.isNull()) {
				ctx.throwRuntimeException("Inconvertible java class argument: " + arguments[i].type.fullName);
				return;
			}
			javaArgs[i] = argValue;
		}

		try {
			Object resultJavaValue = method.invoke(javaObject, javaArgs);
			Object resultValue = HiJava.convertFromJava(ctx, resultJavaValue);

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = type;
			ctx.value.set(resultValue);
		} catch (Exception e) {
			ctx.throwRuntimeException(e.toString());
		}
	}

	@Override
	public boolean isJava() {
		return true;
	}
}
