package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.fields.HiFieldArray;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

public abstract class HiField<T> extends Node implements NodeInitializer, Cloneable {
	private final static String packageName = HiField.class.getPackage().getName() + ".fields";

	private static HashMap<String, java.lang.reflect.Constructor<HiField<?>>> primitiveBuilders;

	private static java.lang.reflect.Constructor<HiField<?>> getConstructor(String name) {
		if (primitiveBuilders == null) {
			primitiveBuilders = new HashMap<>();
		}

		if (!primitiveBuilders.containsKey(name)) {
			if (name.equals("char")) {
				registerBuilder("char", packageName + ".HiFieldChar");
			} else if (name.equals("boolean")) {
				registerBuilder("boolean", packageName + ".HiFieldBoolean");
			} else if (name.equals("byte")) {
				registerBuilder("byte", packageName + ".HiFieldByte");
			} else if (name.equals("short")) {
				registerBuilder("short", packageName + ".HiFieldShort");
			} else if (name.equals("int")) {
				registerBuilder("int", packageName + ".HiFieldInt");
			} else if (name.equals("long")) {
				registerBuilder("long", packageName + ".HiFieldLong");
			} else if (name.equals("float")) {
				registerBuilder("float", packageName + ".HiFieldFloat");
			} else if (name.equals("double")) {
				registerBuilder("double", packageName + ".HiFieldDouble");
			}
		}
		return primitiveBuilders.get(name);
	}

	private static void registerBuilder(String type, String className) {
		try {
			Class<?> c = Class.forName(className);
			java.lang.reflect.Constructor<?> constr = c.getConstructor(String.class);
			primitiveBuilders.put(type, (Constructor<HiField<?>>) constr);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public HiField(Type type, String name) {
		super("field", TYPE_FIELD);
		this.type = type;
		this.name = name != null ? name.intern() : null;
	}

	public abstract void get(RuntimeContext ctx, Value value);

	public abstract T get();

	public abstract void set(RuntimeContext ctx, Value value);

	private Modifiers modifiers = new Modifiers();

	public Modifiers getModifiers() {
		return modifiers;
	}

	public void setModifiers(Modifiers modifiers) {
		this.modifiers = modifiers != null ? modifiers : new Modifiers();
	}

	public Type type;

	private HiClass clazz;

	public HiClass getClass(RuntimeContext ctx) {
		if (clazz == null) {
			clazz = type.getClass(ctx);
		}
		return clazz;
	}

	public String name;

	public Node initializer;

	@Override
	public void execute(RuntimeContext ctx) {
		declared = true;

		// do initialization work
		if (!initialized) {
			// initialized = true; ???

			// if there is no initializer then do default initialization,
			// ie initialization will be done in any case
			if (initializer != null) {
				initializer.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}

				set(ctx, ctx.value);
				initialized = true;

				if (ctx.exitFromBlock()) {
					return;
				}

				// DEBUG
				// System.out.println(type.name + " " + name + " = " +
				// ctx.value.get());
			}
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = getClass(ctx);
		get(ctx, ctx.value);
	}

	@Override
	public boolean isStatic() {
		return modifiers.isStatic();
	}

	public boolean declared = false;

	public boolean initialized = false;

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException exc) {
			return null;
		}
	}

	public static boolean isPrimitive(String type) {
		return getConstructor(type) != null;
	}

	public static HiField<?> getField(Type type, String name) {
		if (type.isArray()) {
			return new HiFieldArray(type, name);
		}

		if (!type.isPrimitive()) {
			return new HiFieldObject(type, name);
		}

		java.lang.reflect.Constructor<HiField<?>> constructor = getConstructor(type.name);
		if (constructor != null) {
			try {
				return constructor.newInstance(name);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

	public static HiField<?> getField(Type type, String name, Node initializer) {
		HiField<?> field = null;
		if (type.isArray()) {
			field = new HiFieldArray(type, name);
		} else if (type.isPrimitive()) {
			java.lang.reflect.Constructor<HiField<?>> constr = getConstructor(type.name);
			try {
				field = constr.newInstance(name);
			} catch (Throwable exc) {
				exc.printStackTrace();
			}
		} else {
			field = new HiFieldObject(type, name);
		}

		if (field != null) {
			field.initializer = initializer;
		}

		return field;
	}

	public static boolean autoCast(HiClass src, HiClass dst) {
		// DEBUG
		// System.out.println("auto cast: " + src + " -> " + dst);
		if (src == dst) {
			return true;
		}

		if (src.isPrimitive() || dst.isPrimitive()) {
			return src.isPrimitive() && dst.isPrimitive() && HiFieldPrimitive.autoCast(src, dst);
		}

		if (src.isNull()) {
			return true;
		}

		if (src.isArray() || dst.isArray()) {
			if (dst == HiClass.OBJECT_CLASS) {
				return true;
			}
			if (!src.isArray() || !dst.isArray()) {
				return false;
			}

			HiClassArray asrc = (HiClassArray) src;
			HiClassArray adst = (HiClassArray) dst;
			if (asrc.dimension != adst.dimension) {
				return false;
			}

			return autoCast(asrc.cellClass, adst.cellClass);
		}
		return src.isInstanceof(dst);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeUTF(name);
		os.writeNullable(initializer);
		modifiers.code(os);
	}

	public static HiField<?> decode(DecodeContext os) throws IOException {
		HiField<?> field = HiField.getField(os.readType(), os.readUTF(), os.readNullable(Node.class));
		field.modifiers = Modifiers.decode(os);
		return field;
	}

	@Override
	public String toString() {
		return getClass(null).fullName + " " + name + " = " + get();
	}

	public String getStringValue() {
		return ((HiObject) get()).getStringValue();
	}
}
