package script.ool.model.lib;

import script.ool.model.Clazz;
import script.ool.model.Obj;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.nodes.NodeString;

public class ClassImpl extends ImplUtil {
	public static void Class_String_getName(RuntimeContext ctx) {
		Clazz clazz = (Clazz) ctx.value.object.userObject;
		NodeString.createString(ctx, clazz.name.toCharArray());
	}

	public static void Class_String_getFullName(RuntimeContext ctx) {
		Clazz clazz = (Clazz) ctx.value.object.userObject;
		NodeString.createString(ctx, clazz.fullName.toCharArray());
	}

	public static void Class_Class_forName_String(RuntimeContext ctx, Obj string) {
		String name = getString(string);
		Clazz clazz = Clazz.forName(ctx, name);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = getClassClass(ctx);
		if (clazz != null) {
			ctx.value.object = getClassObject(ctx, clazz);
		} else {
			ctx.value.object = null;
		}
	}
}
