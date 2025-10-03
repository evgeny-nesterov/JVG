package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.ClassLocationType;
import ru.nest.hiscript.ool.model.ClassType;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class HiClassPrimitive extends HiClass {
	public final static HiClassPrimitive CHAR = new HiClassPrimitive(CHAR_TYPE, "char");

	public final static HiClassPrimitive BOOLEAN = new HiClassPrimitive(BOOLEAN_TYPE, "boolean") {
		HiObject trueValue;

		HiObject falseValue;

		// @autoboxing
		@Override
		public HiObject box(RuntimeContext ctx, Value value) {
			boolean booleanValue = value.getBoolean();
			if (trueValue == null) {
				ctx.value.valueClass = this;
				ctx.value.bool = true;
				trueValue = super.box(ctx, ctx.value);

				ctx.value.valueClass = this;
				ctx.value.bool = false;
				falseValue = super.box(ctx, ctx.value);
			}
			return booleanValue ? trueValue : falseValue;
		}
	};

	public final static HiClassPrimitive BYTE = new HiClassPrimitive(BYTE_TYPE, "byte") {
		final HiObject[] cachedValues = new HiObject[-Byte.MIN_VALUE + Byte.MAX_VALUE + 1];

		// @autoboxing
		@Override
		public HiObject box(RuntimeContext ctx, Value value) {
			int intValue = value.getInt();
			if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
				HiObject object = cachedValues[intValue + (-Byte.MIN_VALUE)];
				if (object == null) {
					ctx.value.valueClass = this;
					ctx.value.byteNumber = (byte) intValue;
					object = super.box(ctx, ctx.value);
					cachedValues[intValue + (-Byte.MIN_VALUE)] = object;
				}
				return object;
			}
			return super.box(ctx, value);
		}
	};

	public final static HiClassPrimitive SHORT = new HiClassPrimitive(SHORT_TYPE, "short") {
		final HiObject[] cachedValues = new HiObject[-Byte.MIN_VALUE + Byte.MAX_VALUE + 1];

		// @autoboxing
		@Override
		public HiObject box(RuntimeContext ctx, Value value) {
			int intValue = value.getInt();
			if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
				HiObject object = cachedValues[intValue + (-Byte.MIN_VALUE)];
				if (object == null) {
					ctx.value.valueClass = this;
					ctx.value.shortNumber = (short) intValue;
					object = super.box(ctx, ctx.value);
					cachedValues[intValue + (-Byte.MIN_VALUE)] = object;
				}
				return object;
			}
			return super.box(ctx, value);
		}
	};

	public final static HiClassPrimitive INT = new HiClassPrimitive(INT_TYPE, "int") {
		final HiObject[] cachedValues = new HiObject[-Byte.MIN_VALUE + Byte.MAX_VALUE + 1];

		// @autoboxing
		@Override
		public HiObject box(RuntimeContext ctx, Value value) {
			int intValue = value.getInt();
			if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
				HiObject object = cachedValues[intValue + (-Byte.MIN_VALUE)];
				if (object == null) {
					ctx.value.valueClass = this;
					ctx.value.intNumber = intValue;
					object = super.box(ctx, ctx.value);
					cachedValues[intValue + (-Byte.MIN_VALUE)] = object;
				}
				return object;
			}
			return super.box(ctx, value);
		}
	};

	public final static HiClassPrimitive FLOAT = new HiClassPrimitive(FLOAT_TYPE, "float");

	public final static HiClassPrimitive LONG = new HiClassPrimitive(LONG_TYPE, "long") {
		final HiObject[] cachedValues = new HiObject[-Byte.MIN_VALUE + Byte.MAX_VALUE + 1];

		// @autoboxing
		@Override
		public HiObject box(RuntimeContext ctx, Value value) {
			long longValue = value.getLong();
			if (longValue >= Byte.MIN_VALUE && longValue <= Byte.MAX_VALUE) {
				HiObject object = cachedValues[(int) longValue + (-Byte.MIN_VALUE)];
				if (object == null) {
					ctx.value.valueClass = this;
					ctx.value.longNumber = longValue;
					object = super.box(ctx, ctx.value);
					cachedValues[(int) longValue + (-Byte.MIN_VALUE)] = object;
				}
				return object;
			}
			return super.box(ctx, value);
		}
	};

	public final static HiClassPrimitive DOUBLE = new HiClassPrimitive(DOUBLE_TYPE, "double");

	public final static HiClassPrimitive VOID = new HiClassPrimitive(VOID_TYPE, "void");

	public final PrimitiveType typeId;

	private HiClassPrimitive(PrimitiveType typeId, String name) {
		super(HiClassLoader.primitiveClassLoader, null, null, name, ClassLocationType.top, null);
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

	@Override
	public PrimitiveType getPrimitiveType() {
		return typeId;
	}

	private HiClass autoboxClass;

	@Override
	public HiClass getAutoboxClass() {
		return autoboxClass;
	}

	public void setAutoboxingClass(HiClass autoboxClass) {
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
		return this == INT || this == BYTE || this == SHORT || this == CHAR;
	}

	@Override
	public boolean isLongNumber() {
		return this == LONG || isIntNumber();
	}

	@Override
	public boolean isObject() {
		return false;
	}

	// @autoboxing
	public HiObject box(RuntimeContext ctx, Value value) {
		HiField valueField = HiField.getField(this, "value", null);
		valueField.set(ctx, value);
		HiConstructor constructor = autoboxClass.getConstructor(ctx, this);
		return constructor.newInstance(ctx, null, new HiField[] {valueField}, null);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeEnum(ClassType.CLASS_PRIMITIVE);
		os.writeUTF(name);
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		return getPrimitiveClass(os.readUTF());
	}

	@Override
	public Class getJavaClass(HiRuntimeEnvironment env) {
		// TODO use typeId
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

	@Override
	public String getNameDescr() {
		return name;
	}
}
