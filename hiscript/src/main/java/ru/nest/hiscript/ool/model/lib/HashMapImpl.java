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
		setCtx(ctx, key);
		ctx.value.set(getMap(ctx).get(key));
	}

	public static void HashMap_Object_put_Object_Object(RuntimeContext ctx, Object key, Object value) {
		setCtx(ctx, key);
		setCtx(ctx, value);
		ctx.value.set(getMap(ctx).put(key, value));
	}

	public static void HashMap_boolean_containsKey_Object(RuntimeContext ctx, Object key) {
		setCtx(ctx, key);
		returnBoolean(ctx, getMap(ctx).containsKey(key));
	}

	public static void HashMap_boolean_containsValue_Object(RuntimeContext ctx, Object value) {
		setCtx(ctx, value);
		returnBoolean(ctx, getMap(ctx).containsValue(value));
	}

	public static void HashMap_int_size(RuntimeContext ctx) {
		returnInt(ctx, getMap(ctx).size());
	}

	public static void HashMap_Object_remove_Object(RuntimeContext ctx, Object key) {
		setCtx(ctx, key);
		ctx.value.set(getMap(ctx).remove(key));
	}

	public static void HashMap_ArrayList_keys(RuntimeContext ctx) {
		returnArrayList(ctx, getMap(ctx).keySet());
	}

	public static void HashMap_ArrayList_values(RuntimeContext ctx) {
		returnArrayList(ctx, getMap(ctx).values());
	}

	public static void HashMap_void_putAll_HashMap(RuntimeContext ctx, HiObject map) {
		Map<Object, Object> currentMap = getMap(ctx);
		for (Map.Entry<?, ?> e : currentMap.entrySet()) {
			setCtx(ctx, e.getKey());
			setCtx(ctx, e.getValue());
		}
		Map<Object, Object> putMap = (Map) map.userObject;
		for (Map.Entry<?, ?> e : putMap.entrySet()) {
			setCtx(ctx, e.getKey());
			setCtx(ctx, e.getValue());
		}
		currentMap.putAll(putMap);
	}

	public static void HashMap_void_clear(RuntimeContext ctx) {
		getMap(ctx).clear();
		returnVoid(ctx);
	}

	public static void HashMap_Object_clone(RuntimeContext ctx) {
		HashMap<Object, Object> map = getMap(ctx);
		Object cloneMap = map.clone();
		// TODO: create clone Obj for HashMap
		// ctx.value.set(clone);
	}
}
