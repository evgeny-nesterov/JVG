package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeIdentifier extends Node {
	public NodeIdentifier(String name) {
		super("identifier", TYPE_IDENTIFIER);
		this.name = name.intern();
	}

	public String name;

	public String getName() {
		return name;
	}

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		Object resolvedIdentifier = ctx.resolveIdentifier(name); // field priority is higher than class priority
		if (resolvedIdentifier instanceof NodeVariable) {
			return ((Node) resolvedIdentifier).getValueType(validationInfo, ctx);
		} else {
			return (HiClass) resolvedIdentifier;
		}
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		// TODO check name
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.NAME;
		ctx.value.name = name;
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
	}

	public static NodeIdentifier decode(DecodeContext os) throws IOException {
		NodeIdentifier node = new NodeIdentifier(os.readUTF());
		return node;
	}
}
