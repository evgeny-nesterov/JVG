package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeCatch extends HiNode {
	public NodeCatch(Type[] excTypes, HiNode catchBody, String excName, Modifiers modifiers, NodeAnnotation[] annotations) {
		super("catch", TYPE_CATCH);
		this.excTypes = excTypes;
		this.catchBody = catchBody;
		this.excName = excName; // .intern();
		this.modifiers = modifiers; // .intern();
		this.annotations = annotations; // .intern();
	}

	public Modifiers modifiers;

	public NodeAnnotation[] annotations;

	public Type[] excTypes;

	private HiNode catchBody;

	private String excName;

	public HiClass[] excClasses; // TODO HiClassMixed

	public int getMatchedExceptionClass(RuntimeContext ctx) {
		HiObject exception = ctx.exception;
		for (int i = 0; i < excTypes.length; i++) {
			if (exception != ctx.exception) {
				// new runtime exception thrown while exception class resolving
				return -2;
			}
		}
		initClasses(ctx);
		for (int i = 0; i < excClasses.length; i++) {
			if (exception.clazz.isInstanceof(excClasses[i])) {
				return i;
			}
		}
		return -1;
	}

	private void initClasses(ClassResolver classResolver) {
		if (excClasses == null) {
			excClasses = new HiClass[excTypes.length];
		}
		for (int i = 0; i < excTypes.length; i++) {
			Type excType = excTypes[i];
			if (excClasses[i] == null) {
				excClasses[i] = excType.getClass(classResolver);
			}
		}
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		initClasses(ctx);

		for (int i = 0; i < excClasses.length; i++) {
			if (!excClasses[i].isInstanceof(HiClass.EXCEPTION_CLASS_NAME)) {
				validationInfo.error("incompatible types: " + excClasses[i].fullName + " cannot be converted to " + HiClass.EXCEPTION_CLASS_NAME, token);
				valid = false;
			}
		}

		NodeArgument field = new NodeArgument(excTypes[0], excName, modifiers, annotations);
		field.setToken(token);
		valid &= field.validate(validationInfo, ctx);

		if (catchBody != null) {
			valid &= catchBody.validateBlock(validationInfo, ctx);
		}
		return valid;
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

					HiFieldObject exc = (HiFieldObject) HiField.getField(excTypes[index], excName, null);
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
		modifiers.code(os);
		os.writeShortArray(annotations);
		os.writeTypes(excTypes);
		os.writeNullable(catchBody);
		os.writeUTF(excName);
	}

	public static NodeCatch decode(DecodeContext os) throws IOException {
		return new NodeCatch(os.readTypes(), os.readNullable(HiNode.class), os.readUTF(), Modifiers.decode(os), os.readShortNodeArray(NodeAnnotation.class));
	}
}
