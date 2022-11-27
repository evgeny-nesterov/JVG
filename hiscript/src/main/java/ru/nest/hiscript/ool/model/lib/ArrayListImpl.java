package ru.nest.hiscript.ool.model.lib;

import java.util.ArrayList;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class ArrayListImpl extends ImplUtil {
	public static void ArrayList_void_init_int(RuntimeContext ctx, int initialCapacity) {
		HiObject o = ctx.getCurrentObject();
		o.userObject = new ArrayList<>(initialCapacity);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("void");
	}

	public static void ArrayList_void_init_ArrayList(RuntimeContext ctx, HiObject list) {
		ArrayList<?> a = (ArrayList<?>) list.userObject;

		HiObject o = ctx.getCurrentObject();
		o.userObject = new ArrayList<>(a);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("void");
	}

	public static void ArrayList_boolean_add_Object(RuntimeContext ctx, Object element) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<Object> a = (ArrayList<Object>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = a.add(element);
	}

	public static void ArrayList_Object_get_int(RuntimeContext ctx, int index) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		Object element = a.get(index);

		ctx.value.set(element);
	}

	public static void ArrayList_Object_set_int_Object(RuntimeContext ctx, int index, Object value) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<Object> a = (ArrayList<Object>) o.userObject;
		Object element = a.set(index, value);

		ctx.value.set(element);
	}

	public static void ArrayList_void_ensureCapacity_int(RuntimeContext ctx, int minCapacity) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		a.ensureCapacity(minCapacity);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("void");
	}

	public static void ArrayList_Object_remove_Object(RuntimeContext ctx, Object element) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		element = a.remove(element);

		ctx.value.set(element);
	}

	public static void ArrayList_void_remove_int(RuntimeContext ctx, int index) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		a.remove(index);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("void");
	}

	public static void ArrayList_int_size(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = a.size();
	}

	public static void ArrayList_int_indexOf_Object(RuntimeContext ctx, Object element) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = a.indexOf(element);
	}

	public static void ArrayList_int_lastIndexOf_Object(RuntimeContext ctx, Object element) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = a.lastIndexOf(element);
	}

	public static void ArrayList_boolean_contains_Object(RuntimeContext ctx, Object element) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = a.contains(element);
	}

	public static void ArrayList_0Object_toArray(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		Object[] array = a.toArray();

		ctx.value.set(array);
	}

	public static void ArrayList_void_clear(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		a.clear();

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("void");
	}

	public static void ArrayList_void_clone(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		Object clone = a.clone();

		ctx.value.set(clone);
	}

	public static void ArrayList_boolean_addAll_ArrayList(RuntimeContext ctx, HiObject list) {
		ArrayList a1 = (ArrayList) list.userObject;

		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = a2.addAll(a1);
	}

	public static void ArrayList_boolean_addAll_int_ArrayList(RuntimeContext ctx, int index, HiObject list) {
		ArrayList a1 = (ArrayList) list.userObject;

		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = a2.addAll(index, a1);
	}

	public static void ArrayList_boolean_removeAll_ArrayList(RuntimeContext ctx, HiObject list) {
		ArrayList<?> a1 = (ArrayList<?>) list.userObject;

		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = a2.removeAll(a1);
	}

	public static void ArrayList_boolean_retainAll_ArrayList(RuntimeContext ctx, HiObject list) {
		ArrayList<?> a1 = (ArrayList<?>) list.userObject;

		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = a2.retainAll(a1);
	}

	public static void ArrayList_boolean_containsAll_ArrayList(RuntimeContext ctx, HiObject list) {
		ArrayList<?> a1 = (ArrayList<?>) list.userObject;

		HiObject o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = a2.containsAll(a1);
	}
}
