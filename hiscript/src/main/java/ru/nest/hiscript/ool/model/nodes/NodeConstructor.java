package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.ClassLoadListener;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.NoClassException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;

public class NodeConstructor extends Node {
	public NodeConstructor(NodeType type, Node[] argValues) {
		super("constructor", TYPE_CONSTRUCTOR);
		this.type = type;
		this.argValues = argValues;
		name = type.getType().fullName.intern();
	}

	public NodeConstructor(HiClass clazz, Node[] argValues) {
		super("constructor", TYPE_CONSTRUCTOR);
		this.clazz = clazz;
		this.argValues = argValues;
		name = clazz.fullName.intern();
	}

	private NodeConstructor(Node[] argValues) {
		super("constructor", TYPE_CONSTRUCTOR);
		this.argValues = argValues;
		name = clazz.fullName.intern();
	}

	public NodeType type;

	public HiClass clazz;

	public Node[] argValues;

	public String name;

	@Override
	public void execute(RuntimeContext ctx) {
		if (clazz == null) {
			// init by type
			clazz = type.getType().getClass(ctx);

			if (clazz == null) {
				ctx.throwException("class not found: " + name);
				return;
			}

			if (clazz.isInterface) {
				ctx.throwException("cannot create object from interface '" + name + "'");
				return;
			}
		} else {
			if (clazz.isInterface) {
				ctx.throwException("cannot create object from interface '" + name + "'");
				return;
			}

			// init by class
			clazz.init(ctx);
			ctx.addClass(clazz);
		}

		HiObject outboundObject = ctx.getOutboundObject(clazz);
		invokeConstructor(ctx, clazz, argValues, null, outboundObject);
	}

	public static void invokeConstructor(RuntimeContext ctx, HiClass clazz, Node[] argValues, HiObject object, HiObject outboundObject) {
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

				types[i] = ctx.value.type;

				Type type = Type.getType(types[i]);
				arguments[i] = HiField.getField(type, null);
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
			ctx.throwException("constructor not found: " + clazz.fullName);
			return;
		}

		// set names of arguments
		if (arguments != null) {
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
		os.write(argValues);
	}

	public static NodeConstructor decode(DecodeContext os) throws IOException {
		boolean isType = os.readBoolean();
		if (isType) {
			NodeType type = (NodeType) os.read(Node.class);
			Node[] argValues = os.readArray(Node.class, os.readByte());
			return new NodeConstructor(type, argValues);
		} else {
			try {
				HiClass clazz = os.readClass();
				Node[] argValues = os.readArray(Node.class, os.readByte());
				return new NodeConstructor(clazz, argValues);
			} catch (NoClassException exc) {
				Node[] argValues = os.readArray(Node.class, os.readByte());
				final NodeConstructor node = new NodeConstructor(argValues);
				os.addClassLoadListener(new ClassLoadListener() {
					@Override
					public void classLoaded(HiClass clazz) {
						node.clazz = clazz;
					}
				}, exc.getIndex());
				return node;
			}
		}
	}
}
