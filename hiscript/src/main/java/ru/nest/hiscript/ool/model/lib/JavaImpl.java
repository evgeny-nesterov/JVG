package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.classes.HiClassJava;

public class JavaImpl extends ImplUtil {
	public static void Java_void_importClass_String_String(RuntimeContext ctx, HiObject className, HiObject javaClassName) throws ClassNotFoundException, NoSuchMethodException {
		String name = getString(ctx, className);
		HiClass clazz = HiClass.loadedClasses.get(name);
		if (clazz == null) {
			Class javaClass = Class.forName(getString(ctx, javaClassName));
			clazz = new HiClassJava(name, javaClass);
			HiClass.loadedClasses.put(name, clazz);
		} else if (!(clazz instanceof HiClassJava)) {
			ctx.throwRuntimeException("Can't import java class with name " + className);
		}
		returnVoid(ctx);
	}
}