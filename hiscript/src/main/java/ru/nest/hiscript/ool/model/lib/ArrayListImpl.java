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
		ctx.value.valueClass = HiClassPrimitive.VOID;
	}

	public static void ArrayList_void_init_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		HiObject o = ctx.getCurrentObject();
		o.userObject = new ArrayList<>(a);

		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClassPrimitive.VOID;
	}

	public static void ArrayList_boolean_add_O(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		ctx.value.valueType = Value.VALUE;
		ctx.value.bool = getArrayList(ctx).add(element);
		ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
	}

	public static void ArrayList_O_get_int(RuntimeContext ctx, int index) {
		Object element = getArrayList(ctx).get(index);
		ctx.value.set(element);
	}

	public static void ArrayList_O_set_int_O(RuntimeContext ctx, int index, Object value) {
		setCtx(ctx, value);
		Object element = getArrayList(ctx).set(index, value);
		ctx.value.set(element);
	}

	public static void ArrayList_void_ensureCapacity_int(RuntimeContext ctx, int minCapacity) {
		getArrayList(ctx).ensureCapacity(minCapacity);
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClassPrimitive.VOID;
	}

	public static void ArrayList_boolean_remove_O(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		ctx.value.valueType = Value.VALUE;
		ctx.value.bool = getArrayList(ctx).remove(element);
		ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
	}

	public static void ArrayList_O_remove_int(RuntimeContext ctx, int index) {
		Object element = getArrayList(ctx).remove(index);
		ctx.value.set(element);
	}

	public static void ArrayList_int_size(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.intNumber = getArrayList(ctx).size();
		ctx.value.valueClass = HiClassPrimitive.INT;
	}

	public static void ArrayList_int_indexOf_O(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		ctx.value.valueType = Value.VALUE;
		ctx.value.intNumber = getArrayList(ctx).indexOf(element); // call equals
		ctx.value.valueClass = HiClassPrimitive.INT;
	}

	public static void ArrayList_int_lastIndexOf_O(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		ctx.value.valueType = Value.VALUE;
		ctx.value.intNumber = getArrayList(ctx).lastIndexOf(element);
		ctx.value.valueClass = HiClassPrimitive.INT;
	}

	public static void ArrayList_boolean_contains_Object(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		ctx.value.valueType = Value.VALUE;
		ctx.value.bool = getArrayList(ctx).contains(element);
		ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
	}

	public static void ArrayList_0Object_toArray(RuntimeContext ctx) {
		Object[] array = getArrayList(ctx).toArray();
		ctx.value.set(array);
	}

	public static void ArrayList_void_clear(RuntimeContext ctx) {
		getArrayList(ctx).clear();
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClassPrimitive.VOID;
	}

	public static void ArrayList_void_clone(RuntimeContext ctx) {
		Object clone = getArrayList(ctx).clone();
		ctx.value.set(clone);
	}

	public static void ArrayList_boolean_addAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List a = (List) list.userObject;
		setCtx(ctx, a);
		ctx.value.valueType = Value.VALUE;
		ctx.value.bool = getArrayList(ctx).addAll(a);
		ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
	}

	public static void ArrayList_boolean_addAll_int_ArrayList(RuntimeContext ctx, int index, HiObject list) {
		List a = (List) list.userObject;
		setCtx(ctx, a);
		ctx.value.valueType = Value.VALUE;
		ctx.value.bool = getArrayList(ctx).addAll(index, a);
		ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
	}

	public static void ArrayList_boolean_removeAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		setCtx(ctx, a);
		ctx.value.valueType = Value.VALUE;
		ctx.value.bool = getArrayList(ctx).removeAll(a);
		ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
	}

	public static void ArrayList_boolean_retainAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		setCtx(ctx, a);
		ctx.value.valueType = Value.VALUE;
		ctx.value.bool = getArrayList(ctx).retainAll(a);
		ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
	}

	public static void ArrayList_boolean_containsAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		setCtx(ctx, a);
		ctx.value.valueType = Value.VALUE;
		ctx.value.bool = getArrayList(ctx).containsAll(a);
		ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
	}
}
