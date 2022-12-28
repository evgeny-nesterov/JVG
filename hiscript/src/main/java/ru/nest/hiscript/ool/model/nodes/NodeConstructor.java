package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassLoadListener;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.NoClassException;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeConstructor extends HiNode {
	public NodeConstructor(NodeType type, HiNode[] argValues) {
		super("constructor", TYPE_CONSTRUCTOR);
		this.type = type;
		this.argValues = argValues;
		name = type.getType().fullName; // .intern();
	}

	public NodeConstructor(HiClass clazz, HiNode[] argValues) {
		super("constructor", TYPE_CONSTRUCTOR);
		this.clazz = clazz;
		this.argValues = argValues;
		name = clazz.getFullName(clazz.getClassLoader()); // .intern();
	}

	private NodeConstructor(HiNode[] argValues) {
		super("constructor", TYPE_CONSTRUCTOR);
		this.argValues = argValues;
	}

	public NodeType type;

	public HiNode[] argValues;

	public String name;

	private HiClass clazz;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return clazz != null ? clazz : type.getType().getClass(ctx);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (argValues != null) {
			for (HiNode argValue : argValues) {
				valid &= argValue.validate(validationInfo, ctx) && argValue.expectValue(validationInfo, ctx);
			}
		}

		// resolve class
		if (clazz == null) {
			clazz = getValueClass(validationInfo, ctx);
		}
		if (clazz == null) {
			validationInfo.error("class not found: " + name, type.getToken());
			valid = false;
		} else {
			if (clazz.isInterface) {
				validationInfo.error("cannot create object from interface '" + name + "'", type.getToken());
				valid = false;
			}
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// init by class
		clazz.init(ctx);
		ctx.addClass(clazz);

		HiObject outboundObject = ctx.getOutboundObject(clazz);
		invokeConstructor(ctx, clazz, argValues, null, outboundObject);
	}

	public static void invokeConstructor(RuntimeContext ctx, HiClass clazz, HiNode[] argValues, HiObject object, HiObject outboundObject) {
		// build argument class array and
		// evaluate method arguments
		HiClass[] types = null;
		HiField<?>[] arguments = null;
		if (argValues != null) {
			int size = argValues.length;
			arguments = new HiField<?>[size];
			types = new HiClass[size];
			for (int i = 0; i < size; i++) {
				argValues[i].execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}

				HiClass type = ctx.value.type;
				types[i] = type;

				arguments[i] = HiField.getField(type, null, argValues[i].getToken());
				if (arguments[i] == null) {
					ctx.throwRuntimeException("argument with type '" + type.fullName + "' is not found");
					return;
				}
				arguments[i].set(ctx, ctx.value);
				if (ctx.exitFromBlock()) {
					return;
				}
				arguments[i].initialized = true;
			}
		}

		// get constructor
		HiConstructor constructor = clazz.searchConstructor(ctx, types);
		if (constructor == null) {
			ctx.throwRuntimeException("constructor not found: " + clazz.fullName);
			return;
		}

		RuntimeContext.StackLevel l = ctx.level;
		while (l != null) {
			if (l.type == RuntimeContext.CONSTRUCTOR && l.constructor == constructor) {
				ctx.throwRuntimeException("recursive constructor invocation");
				return;
			}
			l = l.parent;
		}

		// set names of arguments
		if (!clazz.isJava() && arguments != null) {
			int size = arguments.length;
			for (int i = 0; i < size; i++) {
				arguments[i].name = constructor.arguments[i].name;
			}
		}

		constructor.newInstance(ctx, arguments, object, outboundObject);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);

		os.writeBoolean(type != null);
		if (type != null) {
			os.write(type);
		} else {
			os.writeClass(clazz);
		}

		os.writeByte(argValues != null ? argValues.length : 0);
		os.writeArray(argValues);
	}

	public static NodeConstructor decode(DecodeContext os) throws IOException {
		boolean isType = os.readBoolean();
		if (isType) {
			NodeType type = (NodeType) os.read(HiNode.class);
			HiNode[] argValues = os.readArray(HiNode.class, os.readByte());
			return new NodeConstructor(type, argValues);
		} else {
			try {
				HiClass clazz = os.readClass();
				HiNode[] argValues = os.readArray(HiNode.class, os.readByte());
				return new NodeConstructor(clazz, argValues);
			} catch (NoClassException exc) {
				HiNode[] argValues = os.readArray(HiNode.class, os.readByte());
				final NodeConstructor node = new NodeConstructor(argValues);
				os.addClassLoadListener(new ClassLoadListener() {
					@Override
					public void classLoaded(HiClass clazz) {
						node.clazz = clazz;
						node.name = clazz.fullName; // .intern();
					}
				}, exc.getIndex());
				return node;
			}
		}
	}
}
