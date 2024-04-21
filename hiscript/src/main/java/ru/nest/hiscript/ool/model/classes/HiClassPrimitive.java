package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HiClassPrimitive extends HiClass {
	// TODO cache?
	public final static HiClassPrimitive CHAR = new HiClassPrimitive(PrimitiveTypes.CHAR, "char");

	public final static HiClassPrimitive BOOLEAN = new HiClassPrimitive(PrimitiveTypes.BOOLEAN, "boolean") {
		HiObject trueValue;

		HiObject falseValue;

		@Override
		public void setAutoboxClass(HiClass autoboxClass) {
			super.setAutoboxClass(autoboxClass);

			RuntimeContext ctx = new RuntimeContext(null);
			ctx.value.valueClass = this;
			ctx.value.bool = true;
			trueValue = super.autobox(ctx, ctx.value);

			ctx.value.valueClass = this;
			ctx.value.bool = false;
			falseValue = super.autobox(ctx, ctx.value);
		}

		@Override
		public HiObject autobox(RuntimeContext ctx, Value value) {
			return value.getBoolean() ? trueValue : falseValue;
		}
	};

	public final static HiClassPrimitive BYTE = new HiClassPrimitive(PrimitiveTypes.BYTE, "byte") {
		final HiObject[] cachedValues = new HiObject[-Byte.MIN_VALUE + Byte.MAX_VALUE + 1];

		@Override
		public void setAutoboxClass(HiClass autoboxClass) {
			super.setAutoboxClass(autoboxClass);

			RuntimeContext ctx = new RuntimeContext(null);
			for (int intValue = Byte.MIN_VALUE; intValue <= Byte.MAX_VALUE; intValue++) {
				ctx.value.valueClass = this;
				ctx.value.byteNumber = (byte) intValue;
				cachedValues[intValue + (-Byte.MIN_VALUE)] = super.autobox(ctx, ctx.value);
			}
		}

		@Override
		public HiObject autobox(RuntimeContext ctx, Value value) {
			int intValue = value.getInt();
			if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
				return cachedValues[intValue + (-Byte.MIN_VALUE)];
			}
			return super.autobox(ctx, value);
		}
	};

	public final static HiClassPrimitive SHORT = new HiClassPrimitive(PrimitiveTypes.SHORT, "short") {
		final HiObject[] cachedValues = new HiObject[-Byte.MIN_VALUE + Byte.MAX_VALUE + 1];

		@Override
		public void setAutoboxClass(HiClass autoboxClass) {
			super.setAutoboxClass(autoboxClass);

			RuntimeContext ctx = new RuntimeContext(null);
			for (int intValue = Byte.MIN_VALUE; intValue <= Byte.MAX_VALUE; intValue++) {
				ctx.value.valueClass = this;
				ctx.value.shortNumber = (short) intValue;
				cachedValues[intValue + (-Byte.MIN_VALUE)] = super.autobox(ctx, ctx.value);
			}
		}

		@Override
		public HiObject autobox(RuntimeContext ctx, Value value) {
			int intValue = value.getInt();
			if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
				return cachedValues[intValue + (-Byte.MIN_VALUE)];
			}
			return super.autobox(ctx, value);
		}
	};

	public final static HiClassPrimitive INT = new HiClassPrimitive(PrimitiveTypes.INT, "int") {
		final HiObject[] cachedValues = new HiObject[-Byte.MIN_VALUE + Byte.MAX_VALUE + 1];

		@Override
		public void setAutoboxClass(HiClass autoboxClass) {
			super.setAutoboxClass(autoboxClass);

			RuntimeContext ctx = new RuntimeContext(null);
			for (int intValue = Byte.MIN_VALUE; intValue <= Byte.MAX_VALUE; intValue++) {
				ctx.value.valueClass = this;
				ctx.value.intNumber = intValue;
				cachedValues[intValue + (-Byte.MIN_VALUE)] = super.autobox(ctx, ctx.value);
			}
		}

		@Override
		public HiObject autobox(RuntimeContext ctx, Value value) {
			int intValue = value.getInt();
			if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
				return cachedValues[intValue + (-Byte.MIN_VALUE)];
			}
			return super.autobox(ctx, value);
		}
	};

	public final static HiClassPrimitive FLOAT = new HiClassPrimitive(PrimitiveTypes.FLOAT, "float");

	public final static HiClassPrimitive LONG = new HiClassPrimitive(PrimitiveTypes.LONG, "long") {
		final HiObject[] cachedValues = new HiObject[-Byte.MIN_VALUE + Byte.MAX_VALUE + 1];

		@Override
		public void setAutoboxClass(HiClass autoboxClass) {
			super.setAutoboxClass(autoboxClass);

			RuntimeContext ctx = new RuntimeContext(null);
			for (int intValue = Byte.MIN_VALUE; intValue <= Byte.MAX_VALUE; intValue++) {
				ctx.value.valueClass = this;
				ctx.value.longNumber = intValue;
				cachedValues[intValue + (-Byte.MIN_VALUE)] = super.autobox(ctx, ctx.value);
			}
		}

		@Override
		public HiObject autobox(RuntimeContext ctx, Value value) {
			long longValue = value.getLong();
			if (longValue >= Byte.MIN_VALUE && longValue <= Byte.MAX_VALUE) {
				return cachedValues[(int) longValue + (-Byte.MIN_VALUE)];
			}
			return super.autobox(ctx, value);
		}
	};

	public final static HiClassPrimitive DOUBLE = new HiClassPrimitive(PrimitiveTypes.DOUBLE, "double");

	public final static HiClassPrimitive VOID = new HiClassPrimitive(PrimitiveTypes.VOID, "void");

	public final int typeId;

	private HiClassPrimitive(int typeId, String name) {
		super(HiClass.systemClassLoader, null, null, name, CLASS_TYPE_TOP, null);
		this.typeId = typeId;
	}

	public static Map<String, HiClassPrimitive> primitiveClasses = new HashMap<>();

	static {
		primitiveClasses.put("char", CHAR);
		primitiveClasses.put("boolean", BOOLEAN);
		primitiveClasses.put("byte", BYTE);
		primitiveClasses.put("short", SHORT);
		primitiveClasses.put("int", INT);
		primitiveClasses.put("float", FLOAT);
		primitiveClasses.put("long", LONG);
		primitiveClasses.put("double", DOUBLE);
		primitiveClasses.put("void", VOID);
	}

	public static HiClassPrimitive getPrimitiveClass(String name) {
		return primitiveClasses.get(name);
	}

	public static HiClass getAutoboxClass(String name) {
		HiClassPrimitive primitiveClass = primitiveClasses.get(name);
		return primitiveClass != null ? primitiveClass.autoboxClass : null;
	}

	public int getPrimitiveType() {
		return typeId;
	}

	private HiClass autoboxClass;

	@Override
	public HiClass getAutoboxClass() {
		return autoboxClass;
	}

	public void setAutoboxClass(HiClass autoboxClass) {
		this.autoboxClass = autoboxClass;
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public boolean isNumber() {
		return this != BOOLEAN && this != VOID;
	}

	@Override
	public boolean isIntNumber() {
		return this == INT || this == LONG || this == BYTE || this == SHORT || this == CHAR;
	}

	@Override
	public boolean isObject() {
		return false;
	}

	// autobox
	public HiObject autobox(RuntimeContext ctx, Value value) {
		HiField valueField = HiField.getField(this, "value", null);
		valueField.set(ctx, value);
		HiConstructor constructor = autoboxClass.getConstructor(ctx, this);
		return constructor.newInstance(ctx, null, new HiField[] {valueField}, null);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeByte(HiClass.CLASS_PRIMITIVE);
		os.writeUTF(name);
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		return getPrimitiveClass(os.readUTF());
	}

	@Override
	public Class getJavaClass() {
		switch (name) {
			case "short":
				return short.class;
			case "byte":
				return byte.class;
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "double":
				return double.class;
			case "float":
				return float.class;
			case "char":
				return char.class;
			case "boolean":
				return boolean.class;
		}
		return null;
	}
}
