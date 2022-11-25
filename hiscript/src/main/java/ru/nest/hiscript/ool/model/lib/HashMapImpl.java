package ru.nest.hiscript.ool.model.lib;

import java.util.HashMap;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;

// TODO: take to account hashCode of the object and equate method
public class HashMapImpl extends ImplUtil {
	private static HashMap<Object, Object> getMap(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		HashMap<Object, Object> map = (HashMap<Object, Object>) o.userObject;
		return map;
	}

	private static void setCtx(RuntimeContext ctx, Object object) {
		if (object instanceof HiObject) {
			HiObject o = (HiObject) object;
			o.ctx = ctx;
		}
	}

	public static void HashMap_void_init(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		o.userObject = new HashMap<Object, Object>();
		returnVoid(ctx);
	}

	public static void HashMap_Object_get_Object(RuntimeContext ctx, Object key) {
		HashMap<?, ?> map = getMap(ctx);
		setCtx(ctx, key);
		Object value = map.get(key);
		ctx.value.set(value);
	}

	public static void HashMap_Object_put_Object_Object(RuntimeContext ctx, Object key, Object value) {
		HashMap<Object, Object> map = getMap(ctx);
		setCtx(ctx, key);
		value = map.put(key, value);
		ctx.value.set(value);
	}

	public static void HashMap_boolean_containsKey_Object(RuntimeContext ctx, Object key) {
		HashMap<?, ?> map = getMap(ctx);
		setCtx(ctx, key);
		returnBoolean(ctx, map.containsKey(key));
	}

	public static void HashMap_boolean_containsValue_Object(RuntimeContext ctx, Object value) {
		HashMap<Object, Object> map = getMap(ctx);
		setCtx(ctx, value);
		returnBoolean(ctx, map.containsValue(value));
	}

	public static void HashMap_int_size(RuntimeContext ctx) {
		HashMap<Object, Object> map = getMap(ctx);
		returnInt(ctx, map.size());
	}

	public static void HashMap_Object_remove_Object(RuntimeContext ctx, Object key) {
		HashMap<Object, Object> map = getMap(ctx);
		setCtx(ctx, key);
		Object value = map.remove(key);
		ctx.value.set(value);
	}

	public static void HashMap_ArrayList_keys(RuntimeContext ctx) {
		HashMap<Object, Object> map = getMap(ctx);
		// TODO: realize
	}

	public static void HashMap_ArrayList_values(RuntimeContext ctx) {
		HashMap<Object, Object> map = getMap(ctx);
		// TODO: realize
	}

	public static void HashMap_void_putAll_HashMap(RuntimeContext ctx, HiObject map) {
		HashMap<Object, Object> currentMap = getMap(ctx);
		// TODO: realize
	}

	public static void HashMap_void_clear(RuntimeContext ctx) {
		HashMap<Object, Object> map = getMap(ctx);
		map.clear();
		returnVoid(ctx);
	}

	public static void HashMap_Object_clone(RuntimeContext ctx) {
		HashMap<Object, Object> map = getMap(ctx);
		Object cloneMap = map.clone();
		// TODO: create clone Obj for HashMap
		// ctx.value.set(clone);
	}
}
