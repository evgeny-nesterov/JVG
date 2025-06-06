package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;
import java.util.Set;

public class NodeThrow extends HiNode {
	public NodeThrow(HiNode exception) {
		super("throw", TYPE_THROW, true);
		this.exception = exception;
	}

	private final HiNode exception;

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		return label == null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		valid &= exception.validate(validationInfo, ctx);
		HiClass excClass = exception.getValueClass(validationInfo, ctx);
		if (excClass.isInstanceof(HiClass.EXCEPTION_CLASS_NAME)) {
			if (!excClass.isInstanceof(HiClass.RUNTIME_EXCEPTION_CLASS_NAME)) {
				CompileClassContext.CompileClassLevel level = ctx.level;
				boolean checked = false;
				WHILE:
				while (level != null) {
					switch (level.type) {
						case METHOD:
							if (excClass.isInstanceofAny(((HiMethod) level.node).throwsClasses)) {
								checked = true;
							}
							break WHILE;
						case CONSTRUCTOR:
							if (excClass.isInstanceofAny(((HiConstructor) level.node).throwsClasses)) {
								checked = true;
							}
							break WHILE;
						case TRY:
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
					validationInfo.error("unreported exception " + excClass.getNameDescr() + ": exception must be caught or declared to be thrown", exception);
					valid = false;
				}
				if (level != null) {
					ctx.level.terminate(level);
				}
			}
		} else {
			validationInfo.error("incompatible types: " + excClass.getNameDescr() + " cannot be converted to " + HiClass.EXCEPTION_CLASS_NAME, token);
			valid = false;
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		exception.execute(ctx);
		ctx.exception = (HiObject) ctx.value.getObject();
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		exception.code(os);
	}

	public static NodeThrow decode(DecodeContext os) throws IOException {
		return new NodeThrow(os.read(HiNode.class));
	}
}
