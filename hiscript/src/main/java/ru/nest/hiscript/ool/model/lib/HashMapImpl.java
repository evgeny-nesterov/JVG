package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;

import java.util.HashMap;
import java.util.Map;

public class HashMapImpl extends ImplUtil {
	private static HashMap<Object, Object> getMap(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		return (HashMap<Object, Object>) o.userObject;
	}

	private static void setCtx(RuntimeContext ctx, Object object) {
		if (object instanceof HiObject) {
			HiObject o = (HiObject) object;
			o.ctx = ctx; // used to compute hash code
		}
	}

	public static void HashMap_void_init(RuntimeContext ctx) {
		HiObject o = ctx.getCurrentObject();
		o.userObject = new HashMap<>();
		returnVoid(ctx);
	}

	public static void HashMap_Object_get_Object(RuntimeContext ctx, Object key) {
		Map<?, ?> map = getMap(ctx);
		setCtx(ctx, key);
		Object value = map.get(key);
		ctx.value.set(value);
	}

	public static void HashMap_Object_put_Object_Object(RuntimeContext ctx, Object key, Object value) {
		Map<Object, Object> map = getMap(ctx);
		setCtx(ctx, key);
		value = map.put(key, value);
		ctx.value.set(value);
	}

	public static void HashMap_boolean_containsKey_Object(RuntimeContext ctx, Object key) {
		Map<?, ?> map = getMap(ctx);
		setCtx(ctx, key);
		returnBoolean(ctx, map.containsKey(key));
	}

	public static void HashMap_boolean_containsValue_Object(RuntimeContext ctx, Object value) {
		Map<Object, Object> map = getMap(ctx);
		setCtx(ctx, value);
		returnBoolean(ctx, map.containsValue(value));
	}

	public static void HashMap_int_size(RuntimeContext ctx) {
		Map<Object, Object> map = getMap(ctx);
		returnInt(ctx, map.size());
	}

	public static void HashMap_Object_remove_Object(RuntimeContext ctx, Object key) {
		Map<Object, Object> map = getMap(ctx);
		setCtx(ctx, key);
		Object value = map.remove(key);
		ctx.value.set(value);
	}

	public static void HashMap_ArrayList_keys(RuntimeContext ctx) {
		Map<Object, Object> map = getMap(ctx);
		// TODO: realize
	}

	public static void HashMap_ArrayList_values(RuntimeContext ctx) {
		Map<Object, Object> map = getMap(ctx);
		// TODO: realize
	}

	public static void HashMap_void_putAll_HashMap(RuntimeContext ctx, HiObject map) {
		Map<Object, Object> currentMap = getMap(ctx);
		// TODO: realize
	}

	public static void HashMap_void_clear(RuntimeContext ctx) {
		Map<Object, Object> map = getMap(ctx);
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
