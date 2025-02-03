package ru.nest.hiscript.ool.model.java;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;

import java.lang.reflect.Constructor;

public class HiConstructorJava extends HiConstructor {
	private final Constructor constructor;

	public HiConstructorJava(HiClass clazz, Type type, Constructor constructor) {
		super(clazz, type, null, null, null, (NodeArgument[]) null, null, null, null, null);
		this.constructor = constructor;
	}

	@Override
	public HiObject newInstance(RuntimeContext ctx, Type type, HiField<?>[] arguments, HiObject object, HiObject outboundObject) {
		if (object == null) {
			object = new HiObject(ctx, clazz, type, null);
		}

		try {
			Object[] javaArgs = new Object[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				HiField<?> argument = arguments[i];
				Object argValue = argument.getJava(ctx);
				if (argValue == null && !arguments[i].type.isNull()) {
					ctx.throwRuntimeException("inconvertible java class argument: " + arguments[i].type.fullName);
					return null;
				}
				javaArgs[i] = argValue;
			}
			object.userObject = constructor.newInstance(javaArgs);

			ctx.value.valueType = Value.VALUE;
			ctx.value.valueClass = clazz;
			ctx.value.object = object;
			ctx.value.lambdaClass = null;
			return object;
		} catch (Exception e) {
			ctx.throwRuntimeException(e.getMessage());
			return null;
		}
	}
}
