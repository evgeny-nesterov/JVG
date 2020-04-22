package ru.nest.hiscript.ool.model.lib;

import java.util.ArrayList;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Obj;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class ArrayListImpl extends ImplUtil {
	public static void ArrayList_void_init_int(RuntimeContext ctx, int initialCapacity) {
		Obj o = ctx.getCurrentObject();
		o.userObject = new ArrayList<Object>(initialCapacity);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	public static void ArrayList_void_init_ArrayList(RuntimeContext ctx, Obj list) {
		ArrayList<?> a = (ArrayList<?>) list.userObject;

		Obj o = ctx.getCurrentObject();
		o.userObject = new ArrayList<Object>(a);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	public static void ArrayList_boolean_add_Object(RuntimeContext ctx, Object element) {
		Obj o = ctx.getCurrentObject();
		ArrayList<Object> a = (ArrayList<Object>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = a.add(element);
	}

	public static void ArrayList_Object_get_int(RuntimeContext ctx, int index) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		Object element = a.get(index);

		ctx.value.set(element);
	}

	public static void ArrayList_Object_set_int_Object(RuntimeContext ctx, int index, Object value) {
		Obj o = ctx.getCurrentObject();
		ArrayList<Object> a = (ArrayList<Object>) o.userObject;
		Object element = a.set(index, value);

		ctx.value.set(element);
	}

	public static void ArrayList_void_ensureCapacity_int(RuntimeContext ctx, int minCapacity) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		a.ensureCapacity(minCapacity);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	public static void ArrayList_Object_remove_Object(RuntimeContext ctx, Object element) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		element = a.remove(element);

		ctx.value.set(element);
	}

	public static void ArrayList_void_remove_int(RuntimeContext ctx, int index) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		a.remove(index);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	public static void ArrayList_int_size(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("int");
		ctx.value.intNumber = a.size();
	}

	public static void ArrayList_int_indexOf_Object(RuntimeContext ctx, Object element) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("int");
		ctx.value.intNumber = a.indexOf(element);
	}

	public static void ArrayList_int_lastIndexOf_Object(RuntimeContext ctx, Object element) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("int");
		ctx.value.intNumber = a.lastIndexOf(element);
	}

	public static void ArrayList_boolean_contains_Object(RuntimeContext ctx, Object element) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = a.contains(element);
	}

	public static void ArrayList_0Object_toArray(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		Object[] array = a.toArray();

		ctx.value.set(array);
	}

	public static void ArrayList_void_clear(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		a.clear();

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	public static void ArrayList_void_clone(RuntimeContext ctx) {
		Obj o = ctx.getCurrentObject();
		ArrayList<?> a = (ArrayList<?>) o.userObject;
		Object clone = a.clone();

		ctx.value.set(clone);
	}

	public static void ArrayList_boolean_addAll_ArrayList(RuntimeContext ctx, Obj list) {
		ArrayList a1 = (ArrayList) list.userObject;

		Obj o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = a2.addAll(a1);
	}

	public static void ArrayList_boolean_addAll_int_ArrayList(RuntimeContext ctx, int index, Obj list) {
		ArrayList a1 = (ArrayList) list.userObject;

		Obj o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = a2.addAll(index, a1);
	}

	public static void ArrayList_boolean_removeAll_ArrayList(RuntimeContext ctx, Obj list) {
		ArrayList<?> a1 = (ArrayList<?>) list.userObject;

		Obj o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = a2.removeAll(a1);
	}

	public static void ArrayList_boolean_retainAll_ArrayList(RuntimeContext ctx, Obj list) {
		ArrayList<?> a1 = (ArrayList<?>) list.userObject;

		Obj o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = a2.retainAll(a1);
	}

	public static void ArrayList_boolean_containsAll_ArrayList(RuntimeContext ctx, Obj list) {
		ArrayList<?> a1 = (ArrayList<?>) list.userObject;

		Obj o = ctx.getCurrentObject();
		ArrayList<?> a2 = (ArrayList<?>) o.userObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = a2.containsAll(a1);
	}
}
