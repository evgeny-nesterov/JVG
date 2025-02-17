package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

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
		returnVoid(ctx);
	}

	public static void ArrayList_void_init_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		HiObject o = ctx.getCurrentObject();
		o.userObject = new ArrayList<>(a);
		returnVoid(ctx);
	}

	public static void ArrayList_boolean_add_O(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		returnBoolean(ctx, getArrayList(ctx).add(element));
	}

	public static void ArrayList_O_get_int(RuntimeContext ctx, int index) {
		Object element = getArrayList(ctx).get(index);
		ctx.setValue(element);
	}

	public static void ArrayList_O_set_int_O(RuntimeContext ctx, int index, Object value) {
		setCtx(ctx, value);
		Object element = getArrayList(ctx).set(index, value);
		ctx.setValue(element);
	}

	public static void ArrayList_void_ensureCapacity_int(RuntimeContext ctx, int minCapacity) {
		getArrayList(ctx).ensureCapacity(minCapacity);
		returnVoid(ctx);
	}

	public static void ArrayList_boolean_remove_O(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		returnBoolean(ctx, getArrayList(ctx).remove(element));
	}

	public static void ArrayList_O_remove_int(RuntimeContext ctx, int index) {
		Object element = getArrayList(ctx).remove(index);
		ctx.setValue(element);
	}

	public static void ArrayList_int_size(RuntimeContext ctx) {
		returnInt(ctx, getArrayList(ctx).size());
	}

	public static void ArrayList_int_indexOf_O(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		returnInt(ctx, getArrayList(ctx).indexOf(element)); // call equals
	}

	public static void ArrayList_int_lastIndexOf_O(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		returnInt(ctx, getArrayList(ctx).lastIndexOf(element));
	}

	public static void ArrayList_boolean_contains_Object(RuntimeContext ctx, Object element) {
		setCtx(ctx, element);
		returnBoolean(ctx, getArrayList(ctx).contains(element));
	}

	public static void ArrayList_0Object_toArray(RuntimeContext ctx) {
		Object[] array = getArrayList(ctx).toArray();
		ctx.setValue(array);
	}

	public static void ArrayList_void_clear(RuntimeContext ctx) {
		getArrayList(ctx).clear();
		returnVoid(ctx);
	}

	public static void ArrayList_void_clone(RuntimeContext ctx) {
		Object clone = getArrayList(ctx).clone();
		ctx.setValue(clone);
	}

	public static void ArrayList_boolean_addAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List a = (List) list.userObject;
		setCtx(ctx, a);
		returnBoolean(ctx, getArrayList(ctx).addAll(a));
	}

	public static void ArrayList_boolean_addAll_int_ArrayList(RuntimeContext ctx, int index, HiObject list) {
		List a = (List) list.userObject;
		setCtx(ctx, a);
		returnBoolean(ctx, getArrayList(ctx).addAll(index, a));
	}

	public static void ArrayList_boolean_removeAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		setCtx(ctx, a);
		returnBoolean(ctx, getArrayList(ctx).removeAll(a));
	}

	public static void ArrayList_boolean_retainAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		setCtx(ctx, a);
		returnBoolean(ctx, getArrayList(ctx).retainAll(a));
	}

	public static void ArrayList_boolean_containsAll_ArrayList(RuntimeContext ctx, HiObject list) {
		List<?> a = (List<?>) list.userObject;
		setCtx(ctx, a);
		returnBoolean(ctx, getArrayList(ctx).containsAll(a));
	}
}
