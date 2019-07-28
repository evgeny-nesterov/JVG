package script.ool.model;

import java.util.HashMap;
import java.util.Map;

import script.ool.model.lib.ImplUtil;
import script.ool.model.nodes.NodeInvocation;

public class Obj {
	public Obj(Clazz clazz, Obj outboundObject) {
		this.clazz = clazz;
		this.outboundObject = outboundObject;
		mainObject = this;
	}

	public Clazz clazz;

	public Field<?>[] fields = null;

	private Obj superObject;

	public void setSuperObject(Obj superObject) {
		this.superObject = superObject;
		superObject.setChildObject(this);
		// TODO: set field 'length'
	}

	public Obj getSuperObject() {
		return superObject;
	}

	public Obj outboundObject;

	private Obj childObject;

	public void setChildObject(Obj childObject) {
		this.childObject = childObject;
		mainObject = childObject.mainObject;
	}

	public Obj getChildObject() {
		return childObject;
	}

	// object created with operator new
	private Obj mainObject;

	public Obj getMainObject() {
		return mainObject;
	}

	// used in native method implementations
	public Object userObject = null;

	public Object getUserObject() {
		Obj o = this;
		while (o != null) {
			if (o.userObject != null) {
				return o.userObject;
			}
			o = o.superObject;
		}
		return null;
	}

	private Map<Clazz, Map<String, Field<?>>> fields_map;

	public Field<?> getField(String name) {
		return getField(name, clazz);
	}

	/**
	 * @param name
	 * @param clazz
	 *            Super class, one of interfaces or super interfaces
	 * @return
	 */
	public Field<?> getField(String name, Clazz clazz) {
		if (fields_map != null) {
			Map<String, Field<?>> class_fields_map = fields_map.get(clazz);
			if (class_fields_map != null) {
				Field<?> field = class_fields_map.get(name);
				if (field != null) {
					return field;
				}
			}
		}

		Field<?> field = null;

		// this
		if (fields != null && clazz == this.clazz) {
			for (Field<?> f : fields) {
				if (f.name.equals(name)) {
					field = f;
					break;
				}
			}
		}

		// static: class fields
		if (field == null) {
			field = clazz.getField(name);
			if (field != null && !field.isStatic()) {
				field = null;
			}
		}

		// super object (after this)
		if (field == null && superObject != null) {
			field = superObject.getField(name);
		}

		// static: super class
		if (field == null && clazz.superClass != null) {
			field = clazz.superClass.getField(name);
			if (field != null && !field.isStatic()) {
				field = null;
			}
		}

		// outbound object (after super)
		if (field == null && outboundObject != null) {
			field = outboundObject.getField(name);
		}

		// static: outbound class
		if (field == null && !clazz.isTopLevel()) {
			field = clazz.enclosingClass.getField(name);
			if (field != null && !field.isStatic()) {
				field = null;
			}
		}

		// cache
		if (field != null) {
			if (fields_map == null) {
				fields_map = new HashMap<Clazz, Map<String, Field<?>>>(1);
			}

			Map<String, Field<?>> class_fields_map = fields_map.get(clazz);
			if (class_fields_map == null) {
				class_fields_map = new HashMap<String, Field<?>>(1);
				fields_map.put(clazz, class_fields_map);
			}
			class_fields_map.put(name, field);
		}

		return field;
	}

	public RuntimeContext ctx;

	public String toStringNative() {
		return clazz.name + "@" + Integer.toHexString(System.identityHashCode(this));
	}

	@Override
	public String toString() {
		Method method = clazz.searchMethod(ctx, "toString");
		if (method.clazz.superClass == null) {
			// is Object
			return toStringNative();
		}

		ctx.enterMethod(method, this, -1);
		try {
			method.invoke(ctx, clazz, this, null);
		} finally {
			ctx.exit();
			ctx.isReturn = false;
		}
		return ImplUtil.getString(ctx.value.object);
	}

	// overriden toString
	public char[] toString(RuntimeContext ctx) {
		NodeInvocation.invoke(ctx, this, "toString");
		return ImplUtil.getChars(ctx.value.object);
	}

	@Override
	public int hashCode() {
		Method method = clazz.searchMethod(ctx, "hashCode");
		if (method.clazz.superClass == null) {
			// is Object
			return System.identityHashCode(this);
		}

		ctx.enterMethod(method, this, -1);
		try {
			method.invoke(ctx, clazz, this, null);
		} finally {
			ctx.exit();
			ctx.isReturn = false;
		}
		return ctx.value.intNumber;
	}
}
