package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldArray;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.fields.HiFieldVar;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.nodes.NodeVariable;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public abstract class HiField<T> extends HiNode implements NodeInitializer, NodeVariable, Cloneable {
	private final static String packageName = HiField.class.getPackage().getName() + ".fields";

	private static Map<String, Constructor<HiField<?>>> primitiveBuilders;

	private static java.lang.reflect.Constructor<HiField<?>> getConstructor(String name) {
		if (primitiveBuilders == null) {
			primitiveBuilders = new HashMap<>();
		}

		if (!primitiveBuilders.containsKey(name)) {
			switch (name) {
				case "char":
					registerBuilder("char", packageName + ".HiFieldChar");
					break;
				case "boolean":
					registerBuilder("boolean", packageName + ".HiFieldBoolean");
					break;
				case "byte":
					registerBuilder("byte", packageName + ".HiFieldByte");
					break;
				case "short":
					registerBuilder("short", packageName + ".HiFieldShort");
					break;
				case "int":
					registerBuilder("int", packageName + ".HiFieldInt");
					break;
				case "long":
					registerBuilder("long", packageName + ".HiFieldLong");
					break;
				case "float":
					registerBuilder("float", packageName + ".HiFieldFloat");
					break;
				case "double":
					registerBuilder("double", packageName + ".HiFieldDouble");
					break;
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
		super("field", TYPE_FIELD, false);
		this.type = type;
		this.name = name; //  != null ? name.intern() : null;
	}

	public HiField(HiClass clazz, String name) {
		super("field", TYPE_FIELD, false);
		this.type = Type.getType(clazz);
		this.clazz = clazz;
		this.name = name; //  != null ? name.intern() : null;
	}

	public abstract void get(RuntimeContext ctx, Value value);

	public abstract T get();

	public abstract Object getJava(RuntimeContext ctx);

	public abstract void set(RuntimeContext ctx, Value value);

	private NodeAnnotation[] annotations;

	public NodeAnnotation[] getAnnotations() {
		return annotations;
	}

	public void setAnnotations(NodeAnnotation[] annotations) {
		this.annotations = annotations;
	}

	private Modifiers modifiers = new Modifiers();

	public Modifiers getModifiers() {
		return modifiers;
	}

	public void setModifiers(Modifiers modifiers) {
		this.modifiers = modifiers != null ? modifiers : new Modifiers();
	}

	public Type type;

	private HiClass clazz;

	public String name;

	public HiNode initializer;

	public HiClass getClass(ClassResolver classResolver) {
		if (clazz == null) {
			clazz = type.getClass(classResolver);
		}
		return clazz;
	}

	// generic
	public void setGenericClass(ClassResolver ctx, Type objectType) {
		if (objectType == null) {
			// type is direct class type
			return;
		}
		HiClass fieldClass = getClass(ctx);
		if (fieldClass.isGeneric()) {
			HiClassGeneric fieldGenericClass = (HiClassGeneric) fieldClass;
			Type realFieldType = objectType.getParameterType(fieldGenericClass);
			if (realFieldType != null) {
				this.type = realFieldType;
				this.clazz = realFieldType.getClass(ctx);
			}
		}
	}

	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	public boolean isConstant(CompileClassContext ctx) {
		return modifiers.isStatic() && modifiers.isFinal();
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.type = type;
		return getClass(ctx);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = HiNode.validateAnnotations(validationInfo, ctx, annotations);
		if (initializer != null) {
			valid &= initializer.validate(validationInfo, ctx) && initializer.expectValue(validationInfo, ctx);
			NodeValueType valueType = initializer.getNodeValueType(validationInfo, ctx);
			if (type == Type.varType) {
				clazz = valueType.clazz;
				type = valueType.type;
			} else if (valueType.clazz != getClass(ctx) && !validateType(validationInfo, ctx, getClass(ctx), valueType)) {
				validationInfo.error("incompatible types; found " + valueType.clazz + ", required " + getClass(ctx), token);
				valid = false;
			}
		} else if (type == Type.varType) {
			validationInfo.error("var is not initialized", token);
			valid = false;
			clazz = HiClassPrimitive.VOID;
			type = Type.voidType;
		}
		if (type.isExtends || type.isSuper) {
			validationInfo.error("invalid field type", token);
			valid = false;
		}
		getClass(ctx);

		// generics
		if (type.parameters != null) { // after getClass
			if (type.parameters.length > 0) {
				valid &= type.validateClass(clazz, validationInfo, ctx, getToken());
			} else {
				validationInfo.error("type parameter expected", getToken());
				valid = false;
			}
		}

		valid &= ctx.addLocalVariable(this);
		return valid;
	}

	protected abstract boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType);

	@Override
	public void execute(RuntimeContext ctx) {
		declared = true;

		// do initialization work
		if (!initialized && !initializing) {
			// if there is no initializer then do default initialization,
			// ie initialization will be done in any case
			if (initializer != null) {
				// return default field value on cyclic fields references
				initializing = true;
				initializer.execute(ctx);
				initializing = false;

				if (ctx.exitFromBlock()) {
					return;
				}

				set(ctx, ctx.value);

				// after set to check field on final
				initialized = true;

				if (ctx.exitFromBlock()) {
					return;
				}
			}

			// initialize static field by default value without initializer
			if (modifiers.isStatic()) {
				initialized = true;
			}
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = getClass(ctx);
		get(ctx, ctx.value);
	}

	@Override
	public boolean isStatic() {
		return modifiers.isStatic();
	}

	public boolean declared = false;

	public boolean initialized = false;

	public boolean initializing = false;

	public boolean isInitialized(RuntimeContext ctx) {
		if (ctx.validating) {
			execute(ctx);
		}
		return initialized || initializing;
	}

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

	public static HiField<?> getField(Type type, String name, Token token) {
		HiField field;
		if (type.isArray()) {
			field = new HiFieldArray(type, name);
		} else if (type == Type.varType) {
			field = new HiFieldVar(type, name);
		} else if (type.isPrimitive()) {
			java.lang.reflect.Constructor<HiField<?>> constructor = getConstructor(type.name);
			if (constructor != null) {
				try {
					field = constructor.newInstance(name);
				} catch (Exception exc) {
					throw new HiScriptRuntimeException("undefined field type: " + type, exc);
				}
			} else {
				throw new HiScriptRuntimeException("cannot initialize field by type: " + type);
			}
		} else {
			field = new HiFieldObject(type, name);
		}
		field.setToken(token);
		return field;
	}

	public static HiField<?> getField(Type type, String name, HiNode initializer, Token token) {
		HiField<?> field = getField(type, name, token);
		if (field != null) {
			field.initializer = initializer;
		}
		return field;
	}

	public static HiField<?> getField(HiClass clazz, String name, Token token) {
		HiField field;
		if (clazz.isArray()) {
			field = new HiFieldArray(clazz, name);
		} else if (clazz.isPrimitive() && clazz != HiClassPrimitive.VOID) {
			java.lang.reflect.Constructor<HiField<?>> constructor = getConstructor(clazz.name);
			try {
				field = constructor.newInstance(name);
				field.clazz = clazz;
			} catch (Exception exc) {
				throw new HiScriptRuntimeException("undefined field type: " + clazz.name, exc);
			}
		} else {
			field = new HiFieldObject(clazz, name);
		}
		field.setToken(token);
		return field;
	}

	public static HiField<?> getField(HiClass clazz, String name, HiNode initializer, Token token) {
		HiField<?> field = getField(clazz, name, token);
		if (field != null) {
			field.initializer = initializer;
		}
		return field;
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
		HiField<?> field = HiField.getField(os.readType(), os.readUTF(), os.readNullable(HiNode.class), null);
		field.modifiers = Modifiers.decode(os);
		return field;
	}

	@Override
	public String toString() {
		try (RuntimeContext ctx = new RuntimeContext(null, true)) {
			String value = (clazz != null ? clazz.fullName : type.fullName) + " " + name + " = " + get();
			ctx.throwExceptionIf(false);
			return value;
		}
	}

	public String getStringValue(RuntimeContext ctx) {
		return ((HiObject) get()).getStringValue(ctx);
	}

	@Override
	public String getVariableName() {
		return name;
	}

	@Override
	public String getVariableType() {
		return clazz != null ? clazz.fullName : type.fullName;
	}
}
