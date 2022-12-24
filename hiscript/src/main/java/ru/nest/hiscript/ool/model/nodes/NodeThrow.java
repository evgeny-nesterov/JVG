package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeThrow extends HiNode {
	public NodeThrow(HiNode exception) {
		super("throw", TYPE_THROW);
		this.exception = exception;
	}

	private HiNode exception;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = exception.validate(validationInfo, ctx);
		HiClass excClass = exception.getValueClass(validationInfo, ctx);
		if (excClass.isInstanceof(HiClass.EXCEPTION_CLASS_NAME)) {
			if (!excClass.isInstanceof(HiClass.RUNTIME_EXCEPTION_CLASS_NAME)) {
				CompileClassContext.CompileClassLevel level = ctx.level;
				boolean checked = false;
				WHILE:
				while (level != null) {
					switch (level.type) {
						case RuntimeContext.METHOD:
							if (excClass.isInstanceofAny(((HiMethod) level.node).throwsClasses)) {
								checked = true;
							}
							break WHILE;
						case RuntimeContext.CONSTRUCTOR:
							if (excClass.isInstanceofAny(((HiConstructor) level.node).throwsClasses)) {
								checked = true;
							}
							break WHILE;
						case RuntimeContext.TRY:
							NodeTry tryNode = (NodeTry) level.node;
							if (tryNode.catches != null) {
								for (NodeCatch catchNode : tryNode.catches) {
									if (excClass.isInstanceofAny(ctx, catchNode.excTypes)) {
										checked = true;
										break WHILE;
									}
								}
							}
							break;
						default:
							break;
					}
					level = level.parent;
				}
				if (!checked) {
					validationInfo.error("Unreported exception " + excClass.fullName + ": exception must be caught or declared to be thrown", exception.getToken());
					valid = false;
				}
			}
		} else {
			validationInfo.error("incompatible types: " + excClass.fullName + " cannot be converted to " + HiClass.EXCEPTION_CLASS_NAME, token);
			valid = false;
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		exception.execute(ctx);
		ctx.exception = ctx.value.getObject();
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		exception.code(os);
	}

	public static NodeThrow decode(DecodeContext os) throws IOException {
		return new NodeThrow(os.read(HiNode.class));
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
