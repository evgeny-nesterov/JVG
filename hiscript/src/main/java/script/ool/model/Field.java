package script.ool.model;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import script.ool.model.classes.ClazzArray;
import script.ool.model.fields.FieldArray;
import script.ool.model.fields.FieldObject;
import script.ool.model.fields.FieldPrimitive;
import script.ool.model.nodes.CodeContext;
import script.ool.model.nodes.DecodeContext;

public abstract class Field<T> extends Node implements NodeInitializer, Cloneable {
	private final static String packageName = Field.class.getPackage().getName() + ".fields";

	private static HashMap<String, java.lang.reflect.Constructor<Field<?>>> primitiveBuilders;

	private static java.lang.reflect.Constructor<Field<?>> getConstructor(String name) {
		if (primitiveBuilders == null) {
			primitiveBuilders = new HashMap<String, Constructor<Field<?>>>();
		}

		if (!primitiveBuilders.containsKey(name)) {
			if (name.equals("char")) {
				registerBuilder("char", packageName + ".FieldChar");
			} else if (name.equals("boolean")) {
				registerBuilder("boolean", packageName + ".FieldBoolean");
			} else if (name.equals("byte")) {
				registerBuilder("byte", packageName + ".FieldByte");
			} else if (name.equals("short")) {
				registerBuilder("short", packageName + ".FieldShort");
			} else if (name.equals("int")) {
				registerBuilder("int", packageName + ".FieldInt");
			} else if (name.equals("long")) {
				registerBuilder("long", packageName + ".FieldLong");
			} else if (name.equals("float")) {
				registerBuilder("float", packageName + ".FieldFloat");
			} else if (name.equals("double")) {
				registerBuilder("double", packageName + ".FieldDouble");
			}
		}

		return primitiveBuilders.get(name);
	}

	private static void registerBuilder(String type, String className) {
		try {
			Class<?> c = Class.forName(className);
			java.lang.reflect.Constructor<?> constr = c.getConstructor(String.class);
			primitiveBuilders.put(type, (Constructor<Field<?>>) constr);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public Field(Type type, String name) {
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

	private Clazz clazz;

	public Clazz getClazz(RuntimeContext ctx) {
		if (clazz == null) {
			clazz = type.getClazz(ctx);
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
			initialized = true;

			// if there is no initializer then do default initialization,
			// ie initialization will be done in any case
			if (initializer != null) {

				initializer.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}

				initialized = false;
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
		ctx.value.type = getClazz(ctx);
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

	public static Field<?> getField(Type type, String name) {
		if (type.isArray()) {
			return new FieldArray(type, name);
		}

		if (!type.isPrimitive()) {
			return new FieldObject(type, name);
		}

		java.lang.reflect.Constructor<Field<?>> constr = getConstructor(type.name);
		try {
			return constr.newInstance(name);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return null;
	}

	public static Field<?> getField(Type type, String name, Node initializer) {
		Field<?> field = null;
		if (type.isArray()) {
			field = new FieldArray(type, name);
		} else if (type.isPrimitive()) {
			java.lang.reflect.Constructor<Field<?>> constr = getConstructor(type.name);
			try {
				field = constr.newInstance(name);
			} catch (Throwable exc) {
				exc.printStackTrace();
			}
		} else {
			field = new FieldObject(type, name);
		}

		if (field != null) {
			field.initializer = initializer;
		}

		return field;
	}

	public static boolean autoCast(Clazz src, Clazz dst) {
		// DEBUG
		// System.out.println("auto cast: " + src + " -> " + dst);
		if (src == dst) {
			return true;
		}

		if (src.isPrimitive() || dst.isPrimitive()) {
			return src.isPrimitive() && dst.isPrimitive() && FieldPrimitive.autoCast(src, dst);
		}

		if (src.isNull()) {
			return true;
		}

		if (src.isArray() || dst.isArray()) {
			if (dst == Clazz.OBJECT_CLASS) {
				return true;
			}
			if (!src.isArray() || !dst.isArray()) {
				return false;
			}

			ClazzArray asrc = (ClazzArray) src;
			ClazzArray adst = (ClazzArray) dst;
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

	public static Field<?> decode(DecodeContext os) throws IOException {
		Field<?> field = Field.getField(os.readType(), os.readUTF(), os.readNullable(Node.class));
		field.modifiers = Modifiers.decode(os);
		return field;
	}

	@Override
	public String toString() {
		return getClazz(null).fullName + " " + name + " = " + get();
	}
}
