package ru.nest.hiscript.ool.runtime;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.lib.ImplUtil;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiObject {
	public HiObject(RuntimeContext ctx, HiClass clazz, Type type, HiObject outboundObject) {
		this.ctx = ctx;
		this.clazz = clazz;
		this.type = type;
		this.outboundObject = outboundObject;
		mainObject = this;
	}

	public HiClass clazz;

	public Type type;

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

	// object created with operator new
	private HiObject mainObject;

	public HiObject getMainObject() {
		return mainObject;
	}

	// used in native method implementations
	public Object userObject = null;

	public Object getUserObject() {
		HiObject o = this;
		while (o != null && o.userObject == null) {
			o = o.superObject;
		}
		return o != null ? o.userObject : null;
	}

	private Map<HiClass, Map<String, HiField<?>>> fieldsMap;

	public HiField<?> getField(RuntimeContext ctx, String name) {
		return getField(ctx, name, clazz, true);
	}

	public HiField<?> getField(RuntimeContext ctx, String name, boolean searchOutbound) {
		return getField(ctx, name, clazz, searchOutbound);
	}

	public HiField<?> getField(RuntimeContext ctx, String name, HiClass clazz) {
		return getField(ctx, name, clazz, true);
	}

	/**
	 * @param name
	 * @param clazz Super class, one of interfaces or super interfaces
	 * @return
	 */
	public HiField<?> getField(RuntimeContext ctx, String name, HiClass clazz, boolean searchOutbound) {
		if (fieldsMap != null) {
			Map<String, HiField<?>> classFieldsMap = fieldsMap.get(clazz);
			if (classFieldsMap != null) {
				HiField<?> field = classFieldsMap.get(name);
				if (field != null) {
					return field;
				}
			}
		}

		HiField<?> field = null;
		if (!clazz.isJava()) {
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
				field = clazz.getField(ctx, name);
				if (field != null && !field.isStatic()) {
					field = null;
				}
			}

			// super object (after this)
			HiField<?> protectedField = null;
			if (field == null && superObject != null) {
				field = superObject.getField(ctx, name);
				if (field != null && field.isProtected()) {
					protectedField = field;
					field = null;
				}
			}

			// static: super class
			if (field == null && clazz.superClass != null) {
				field = clazz.superClass.getField(ctx, name);
				if (field != null && !field.isStatic()) {
					field = null;
				}
			}

			// outbound object (after super)
			if (field == null && searchOutbound) {
				field = getOutboundField(ctx, name);
			}

			// static: enclosing class
			if (field == null && !clazz.isTopLevel()) {
				field = clazz.enclosingClass.getField(ctx, name);
				if (field != null && !field.isStatic()) {
					field = null;
				}
			}

			if (field == null) {
				field = protectedField;
			}
		} else {
			field = clazz.getField(ctx, name);
		}

		// cache
		if (field != null) {
			if (fieldsMap == null) {
				fieldsMap = new HashMap<>(1);
			}

			Map<String, HiField<?>> classFieldsMap = fieldsMap.computeIfAbsent(clazz, k -> new HashMap<>(1));
			classFieldsMap.put(name, field);
		}
		return field;
	}

	public HiField<?> getOutboundField(RuntimeContext ctx, String name) {
		if (outboundObject != null) {
			return outboundObject.getField(ctx, name);
		}
		return null;
	}

	public RuntimeContext ctx;

	public String toStringNative() {
		return clazz.name + "@" + Integer.toHexString(System.identityHashCode(this));
	}

	@Override
	public String toString() {
		// do not use this.ctx here, as this.ctx.value will be changed (for example it may affect on runtime values during debugging)
		return toString(new RuntimeContext(clazz.getEnv(), clazz.getClassLoader()));
	}

	public String toString(RuntimeContext ctx) {
		HiMethod method = clazz.searchMethod(ctx, "toString");
		if (method.clazz.superClass == null) {
			// is Object
			return toStringNative();
		}

		ctx.enterMethod(method, this);
		try {
			method.invoke(ctx, clazz, this, null);
		} finally {
			ctx.exit();
			ctx.isReturn = false;
		}
		return ImplUtil.getString(ctx, (HiObject) ctx.value.object);
	}

	// overridden toString
	public char[] getStringChars(RuntimeContext ctx) {
		NodeInvocation.invoke(ctx, this, "toString");
		return ImplUtil.getChars(ctx, (HiObject) ctx.value.object);
	}

	public void getAutoboxedValue(RuntimeContext ctx, Value value) {
		HiField<?> field = getField(ctx, "value");
		field.get(ctx, value);
	}

	@Override
	public boolean equals(Object object) {
		RuntimeContext ctx = this.ctx != null ? this.ctx : new RuntimeContext(clazz.getEnv(), clazz.getClassLoader());
		return equals(ctx, (HiObject) object);
	}

	public boolean equals(RuntimeContext ctx, HiObject object) {
		if (this == object) {
			return true;
		}

		HiClass objectClass = HiClass.forName(ctx, HiClass.OBJECT_CLASS_NAME);
		HiMethod equalsMethod = clazz.searchMethod(ctx, "equals", objectClass);

		// enter into method
		ctx.enterMethod(equalsMethod, this);
		boolean result;
		try {
			// register variables in method
			HiField objectField = new HiFieldObject(Type.objectType, equalsMethod.argsNames[0], object);
			ctx.addVariable(objectField);

			// perform method invocation
			Value oldValue = ctx.value;
			try {
				equalsMethod.invoke(ctx, HiClassPrimitive.getPrimitiveClass("boolean"), this, null);
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
		RuntimeContext ctx = this.ctx != null ? this.ctx : new RuntimeContext(clazz.getEnv(), clazz.getClassLoader());
		return hashCode(ctx);
	}

	public int hashCode(RuntimeContext ctx) {
		HiMethod method = clazz.searchMethod(ctx, "hashCode");
		if (method.clazz.superClass == null) {
			// is Object
			return System.identityHashCode(this);
		}

		ctx.enterMethod(method, this);
		try {
			method.invoke(ctx, clazz, this, null);
		} finally {
			ctx.exit();
			ctx.isReturn = false;
		}
		return ctx.value.intNumber;
	}

	public Object getAutoboxValue(RuntimeContext ctx) {
		return getField(ctx, "value").get();
	}

	public String getStringValue(RuntimeContext ctx) {
		return new String((char[]) getField(ctx, "chars").get());
	}

	public Object getJavaValue(RuntimeContext ctx) {
		switch (clazz.fullName) {
			case "Byte":
			case "Short":
			case "Integer":
			case "Long":
			case "Float":
			case "Double":
			case "Character":
			case "Boolean":
				return getAutoboxValue(ctx);
			case "String":
				return getStringValue(ctx);
			case "HashMap":
				Map<?, ?> map = (Map<?, ?>) userObject;
				Map javaMap = new HashMap(map.size());
				for (Map.Entry<?, ?> e : map.entrySet()) {
					Object key = getJavaValue(ctx, e.getKey());
					Object value = getJavaValue(ctx, e.getValue());
					javaMap.put(key, value);
				}
				return javaMap;
			case "ArrayList":
				List<?> list = (List<?>) userObject;
				List javaList = new ArrayList(list.size());
				for (Object value : list) {
					javaList.add(getJavaValue(ctx, value));
				}
				return javaList;
			case "Iterator":
				// TODO
		}
		if (clazz.isJava()) {
			return userObject;
		}
		ctx.throwRuntimeException("cannot convert to java value: " + clazz.getNameDescr());
		return null;
	}

	public static Object getJavaValue(RuntimeContext ctx, Object value) {
		if (value instanceof HiObject) {
			HiObject object = (HiObject) value;
			return object.getJavaValue(ctx);
		} else {
			return value;
		}
	}
}
