package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;

import java.util.ArrayList;
import java.util.List;

public class ArrayListImpl extends ImplUtil {
	private static ArrayList getArrayList(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		return (ArrayList) o.userObject;
	}

	public static void ArrayList_void_init_int(RuntimeContext ctx, int initialCapacity) {
		HiObject o = ctx.getCurrentObject();
		o.userObject = new ArrayList<>(initialCapacity);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.VOID;
	}

	public static void ArrayList_void_init_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		HiObject o = ctx.getCurrentObject();
		o.userObject = new ArrayList<>(a);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.VOID;
	}

	public static void ArrayList_boolean_add_Object(RuntimeContext ctx, Object element) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = getArrayList(ctx).add(element);
	}

	public static void ArrayList_Object_get_int(RuntimeContext ctx, int index) {
		Object element = getArrayList(ctx).get(index);
		ctx.value.set(element);
	}

	public static void ArrayList_Object_set_int_Object(RuntimeContext ctx, int index, Object value) {
		Object element = getArrayList(ctx).set(index, value);
		ctx.value.set(element);
	}

	public static void ArrayList_void_ensureCapacity_int(RuntimeContext ctx, int minCapacity) {
		getArrayList(ctx).ensureCapacity(minCapacity);
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.VOID;
	}

	public static void ArrayList_Object_remove_Object(RuntimeContext ctx, Object element) {
		if (element instanceof HiObject) {
			// for HiObject.equals(Object)
			((HiObject) element).ctx = ctx;
		}
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = getArrayList(ctx).remove(element);
	}

	public static void ArrayList_void_remove_int(RuntimeContext ctx, int index) {
		Object element = getArrayList(ctx).remove(index);
		ctx.value.set(element);
	}

	public static void ArrayList_int_size(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.INT;
		ctx.value.intNumber = getArrayList(ctx).size();
	}

	public static void ArrayList_int_indexOf_Object(RuntimeContext ctx, Object element) {
		if (element instanceof HiObject) {
			// for HiObject.equals(Object)
			((HiObject) element).ctx = ctx;
		}
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.INT;
		ctx.value.intNumber = getArrayList(ctx).indexOf(element);
	}

	public static void ArrayList_int_lastIndexOf_Object(RuntimeContext ctx, Object element) {
		if (element instanceof HiObject) {
			// for HiObject.equals(Object)
			((HiObject) element).ctx = ctx;
		}
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.INT;
		ctx.value.intNumber = getArrayList(ctx).lastIndexOf(element);
	}

	public static void ArrayList_boolean_contains_Object(RuntimeContext ctx, Object element) {
		if (element instanceof HiObject) {
			// for HiObject.equals(Object)
			((HiObject) element).ctx = ctx;
		}
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = getArrayList(ctx).contains(element);
	}

	public static void ArrayList_0Object_toArray(RuntimeContext ctx) {
		Object[] array = getArrayList(ctx).toArray();
		ctx.value.set(array);
	}

	public static void ArrayList_void_clear(RuntimeContext ctx) {
		getArrayList(ctx).clear();
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.VOID;
	}

	public static void ArrayList_void_clone(RuntimeContext ctx) {
		Object clone = getArrayList(ctx).clone();
		ctx.value.set(clone);
	}

	public static void ArrayList_boolean_addAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List a = (List) list.userObject;
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = getArrayList(ctx).addAll(a);
	}

	public static void ArrayList_boolean_addAll_int_ArrayList(RuntimeContext ctx, int index, HiObject list) {
		List a = (List) list.userObject;
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = getArrayList(ctx).addAll(index, a);
	}

	public static void ArrayList_boolean_removeAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		for (Object element : a) {
			if (element instanceof HiObject) {
				// for HiObject.equals(Object)
				((HiObject) element).ctx = ctx;
			}
		}
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = getArrayList(ctx).removeAll(a);
	}

	public static void ArrayList_boolean_retainAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		for (Object element : a) {
			if (element instanceof HiObject) {
				// for HiObject.equals(Object)
				((HiObject) element).ctx = ctx;
			}
		}
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = getArrayList(ctx).retainAll(a);
	}

	public static void ArrayList_boolean_containsAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		for (Object element : a) {
			if (element instanceof HiObject) {
				// for HiObject.equals(Object)
				((HiObject) element).ctx = ctx;
			}
		}
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = getArrayList(ctx).containsAll(a);
	}
}
