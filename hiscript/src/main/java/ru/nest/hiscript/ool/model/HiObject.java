package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.lib.ImplUtil;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;

import java.util.HashMap;
import java.util.Map;

public class HiObject {
	public HiObject(HiClass clazz, HiObject outboundObject) {
		this.clazz = clazz;
		this.outboundObject = outboundObject;
		mainObject = this;
	}

	public HiClass clazz;

	public HiField<?>[] fields = null;

	private HiObject superObject;

	public void setSuperObject(HiObject superObject) {
		this.superObject = superObject;
		superObject.setChildObject(this);
		// TODO: set field 'length'
	}

	public HiObject getSuperObject() {
		return superObject;
	}

	public HiObject outboundObject;

	private HiObject childObject;

	public void setChildObject(HiObject childObject) {
		this.childObject = childObject;
		mainObject = childObject.mainObject;
	}

	public HiObject getChildObject() {
		return childObject;
	}

	// object created with operator new
	private HiObject mainObject;

	public HiObject getMainObject() {
		return mainObject;
	}

	// used in native method implementations
	public Object userObject = null;

	public Object getUserObject() {
		HiObject o = this;
		while (o != null) {
			if (o.userObject != null) {
				return o.userObject;
			}
			o = o.superObject;
		}
		return null;
	}

	private Map<HiClass, Map<String, HiField<?>>> fields_map;

	public HiField<?> getField(String name) {
		return getField(name, clazz);
	}

	/**
	 * @param name
	 * @param clazz
	 *            Super class, one of interfaces or super interfaces
	 * @return
	 */
	public HiField<?> getField(String name, HiClass clazz) {
		if (fields_map != null) {
			Map<String, HiField<?>> class_fields_map = fields_map.get(clazz);
			if (class_fields_map != null) {
				HiField<?> field = class_fields_map.get(name);
				if (field != null) {
					return field;
				}
			}
		}

		HiField<?> field = null;

		// this
		if (fields != null && clazz == this.clazz) {
			for (HiField<?> f : fields) {
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
				fields_map = new HashMap<>(1);
			}

			Map<String, HiField<?>> class_fields_map = fields_map.get(clazz);
			if (class_fields_map == null) {
				class_fields_map = new HashMap<>(1);
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
		HiMethod method = clazz.searchMethod(ctx, "toString");
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

	// overridden toString
	public char[] toString(RuntimeContext ctx) {
		NodeInvocation.invoke(ctx, this, "toString");
		return ImplUtil.getChars(ctx.value.object);
	}

	public boolean equals(RuntimeContext ctx, HiObject object) {
		HiClass objectClass = HiClass.forName(ctx, "Object");
		HiMethod equalsMethod = clazz.searchMethod(ctx, "equals", objectClass);
		HiField objectField = new HiFieldObject(Type.objectType, equalsMethod.argNames[0], object);

		// enter into method
		ctx.enterMethod(equalsMethod, this, -1);
		boolean result;
		try {
			// register variables in method
			ctx.addVariable(objectField);

			// perform method invocation
			Value oldValue = ctx.value;
			try {
				equalsMethod.invoke(ctx, HiClassPrimitive.getPrimitiveClass("boolean"), this, new HiField[] {objectField});
				result = ctx.value.getBoolean();
			} finally {
				ctx.value = oldValue;
			}
		} finally {
			// exit from method
			ctx.exit();
			ctx.isReturn = false;
		}
		return result;
	}

	@Override
	public int hashCode() {
		return hashCode(ctx);
	}

	public int hashCode(RuntimeContext ctx) {
		HiMethod method = clazz.searchMethod(ctx, "hashCode");
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

	public String getStringValue() {
		return new String((char[]) getField("chars").get());
	}
}
