package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;

import java.io.IOException;

public class NodeCatch extends Node {
	public NodeCatch(Type[] excTypes, Node catchBody, String excName) {
		super("catch", TYPE_CATCH);
		this.excTypes = excTypes;
		this.catchBody = catchBody;
		this.excName = excName.intern();
	}

	private Type[] excTypes;

	private Node catchBody;

	private String excName;

	private HiClass[] excClasses; // TODO HiClassMixed

	public int getMatchedExceptionClass(RuntimeContext ctx) {
		HiObject exception = ctx.exception;
		if (excClasses == null) {
			excClasses = new HiClass[excTypes.length];
			for (int i = 0; i < excTypes.length; i++) {
				Type excType = excTypes[i];
				excClasses[i] = excType.getClass(ctx);
				if (exception != ctx.exception) {
					// new runtime exception thrown while exception class resolving
					return -2;
				}
			}
		}

		for (int i = 0; i < excClasses.length; i++) {
			if (exception.clazz.isInstanceof(excClasses[i])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		if (ctx.exception != null && !ctx.exception.clazz.name.equals("AssertException")) {
			HiObject exception = ctx.exception;
			int index = getMatchedExceptionClass(ctx);
			if (index < 0) {
				return;
			}

			HiClass excClass = excClasses[index];
			if (exception.clazz.isInstanceof(excClass)) {
				ctx.exception = null;
				if (catchBody != null) {
					ctx.enter(RuntimeContext.CATCH, token);

					HiFieldObject exc = (HiFieldObject) HiField.getField(excTypes[index], excName);
					exc.set(exception);
					exc.initialized = true;

					ctx.addVariable(exc);

					try {
						catchBody.execute(ctx);
					} finally {
						ctx.exit();
					}
				}
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeTypes(excTypes);
		os.writeNullable(catchBody);
		os.writeUTF(excName);
	}

	public static NodeCatch decode(DecodeContext os) throws IOException {
		return new NodeCatch(os.readTypes(), os.readNullable(Node.class), os.readUTF());
	}
}
