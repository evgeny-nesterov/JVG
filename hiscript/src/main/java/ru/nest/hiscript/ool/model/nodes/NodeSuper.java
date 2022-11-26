package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeSuper extends Node {
	public final static NodeSuper instance = new NodeSuper();

	private NodeSuper() {
		super("super", SUPER);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		HiObject currentObject = ctx.getCurrentObject();
		if (currentObject == null || currentObject.getSuperObject() == null) {
			ctx.throwRuntimeException("can not access super");
			return;
		}

		HiObject superObject = currentObject.getSuperObject();
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = superObject.clazz;
		ctx.value.object = superObject;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeSuper decode(DecodeContext os) throws IOException {
		return instance;
	}
}
