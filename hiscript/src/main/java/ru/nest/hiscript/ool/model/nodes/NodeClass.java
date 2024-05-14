package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassLoadListener;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNoClassException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeClass extends HiNode {
	public NodeClass(HiClass clazz) {
		super("class", TYPE_CLASS, true);
		this.clazz = clazz;
	}

	private NodeClass() {
		super("class", TYPE_CLASS, true);
	}

	private HiClass clazz;

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
		try {
			return new NodeClass(os.readClass());
		} catch (HiNoClassException exc) {
			final NodeClass node = new NodeClass();
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
