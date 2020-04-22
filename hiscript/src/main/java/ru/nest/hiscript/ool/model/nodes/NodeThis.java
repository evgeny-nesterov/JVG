package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Obj;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeThis extends Node {
	public final static NodeThis instance = new NodeThis();

	private NodeThis() {
		super("this", THIS);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		Obj currentObject = ctx.getCurrentObject();
		if (currentObject == null) {
			ctx.throwException("can not accet to this");
			return;
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = currentObject.clazz;
		ctx.value.object = currentObject;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeThis decode(DecodeContext os) throws IOException {
		return instance;
	}
}
