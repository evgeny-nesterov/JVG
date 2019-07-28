package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Field;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeIdentificator extends Node {
	public NodeIdentificator(String name) {
		super("identificator", TYPE_IDENTIFICATOR);
		this.name = name.intern();
	}

	private String name;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.NAME;
		ctx.value.name = name;
	}

	public static boolean resolve(RuntimeContext ctx, Value v, boolean checkInitialization) {
		// object
		if (resolveVariable(ctx, v, checkInitialization)) {
			return true;
		}

		// class
		if (resolveClass(ctx, v)) {
			return true;
		}

		return false;
	}

	public static boolean resolveVariable(RuntimeContext ctx, Value v, boolean checkInitialization) {
		String name = v.name;
		Field<?> var = ctx.getVariable(name);
		if (var != null) {
			if (checkInitialization && !var.initialized) {
				ctx.throwException("variable not initialized: " + var.name);
			}

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = var.type.getClazz(ctx);
			var.execute(ctx);

			ctx.value.copyTo(v);
			v.valueType = Value.VARIABLE;
			v.name = name;
			v.variable = var;
			return true;
		}
		return false;
	}

	public static boolean resolveClass(RuntimeContext ctx, Value v) {
		String name = v.name;
		Clazz clazz = ctx.getClass(name);
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
	}

	public static NodeIdentificator decode(DecodeContext os) throws IOException {
		return new NodeIdentificator(os.readUTF());
	}
}
