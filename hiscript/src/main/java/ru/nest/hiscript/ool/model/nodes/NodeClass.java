package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;

public class NodeClass extends HiNode {
	public NodeClass(HiClass clazz) {
		super("class", TYPE_CLASS, true);
		this.clazz = clazz;
	}

	private NodeClass() {
		super("class", TYPE_CLASS, true);
	}

	public HiClass clazz;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		valid &= clazz.validate(validationInfo, ctx);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.addClass(clazz);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeClass(clazz);
	}

	public static NodeClass decode(DecodeContext os) throws IOException {
		NodeClass node = new NodeClass();
		os.readClass(clazz -> node.clazz = clazz);
		return node;
	}
}
