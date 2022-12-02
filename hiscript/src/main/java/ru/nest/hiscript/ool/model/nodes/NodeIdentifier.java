package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

import java.io.IOException;

public class NodeIdentifier extends Node {
	public NodeIdentifier(String name) {
		super("identifier", TYPE_IDENTIFIER);
		this.name = name.intern();
	}

	private String name;

	public String getName() {
		return name;
	}

	private String castedVariableName;

	public String getCastedVariableName() {
		return castedVariableName;
	}

	public void setCastedVariableName(String castedVariableName) {
		this.castedVariableName = castedVariableName;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.NAME;
		ctx.value.name = name;
		ctx.value.castedVariableName = castedVariableName;
	}

	public static boolean resolve(RuntimeContext ctx, Value value, boolean checkInitialization) {
		// object
		if (resolveVariable(ctx, value, checkInitialization)) {
			return true;
		}

		// class
		if (resolveClass(ctx, value)) {
			return true;
		}
		return false;
	}

	public static boolean resolveVariable(RuntimeContext ctx, Value value, boolean checkInitialization) {
		String name = value.name;
		HiField<?> var = ctx.getVariable(name);
		if (var != null) {
			if (checkInitialization && !var.initialized) {
				ctx.throwRuntimeException("variable not initialized: " + var.name);
			}

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = var.type.getClass(ctx);
			var.execute(ctx);

			ctx.value.copyTo(value);
			value.valueType = Value.VARIABLE;
			value.name = name;
			value.variable = var;
			return true;
		}
		return false;
	}

	public static boolean resolveClass(RuntimeContext ctx, Value v) {
		String name = v.name;
		HiClass clazz = ctx.getClass(name);
		if (clazz != null) {
			v.valueType = Value.CLASS;
			v.type = clazz;
			v.name = name;
			return true;
		}
		return false;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(name);
		os.writeNullableUTF(castedVariableName);
	}

	public static NodeIdentifier decode(DecodeContext os) throws IOException {
		NodeIdentifier node = new NodeIdentifier(os.readUTF());
		node.castedVariableName = os.readNullableUTF();
		return node;
	}
}
