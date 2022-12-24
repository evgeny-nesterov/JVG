package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class HiClassAnnotation extends HiClass {
	public HiClassAnnotation(HiClassLoader classLoader, HiClass enclosingClass, String name, int type) {
		super(classLoader, null, enclosingClass, name, type, null);
	}

	@Override
	public boolean isAnnotation() {
		return true;
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = super.validate(validationInfo, ctx);
		ctx.enter(RuntimeContext.INITIALIZATION, this);
		if (fields != null) {
			for (HiField field : fields) {
				if (!field.getClass(ctx).isConstant()) {
					validationInfo.error("invalid type '" + field.name + "' for annotation member", field.getToken());
					valid = false;
				}
			}
		}
		if (methods != null) {
			for (HiMethod method : methods) {
				method.resolve(ctx);
				if (!method.returnClass.isConstant()) {
					validationInfo.error("invalid type '" + method.name + "' for annotation member", method.getToken());
					valid = false;
				} else if (method.isAnnotationArgument && method.body != null) {
					if (method.body.getValueType(validationInfo, ctx).isConstant) {
						HiClass outboundClass = ctx.clazz;
						ctx.clazz = this;
						method.annotationDefaultValue = method.body.getObjectValue(validationInfo, ctx, method.body.getToken());
						ctx.clazz = outboundClass;
					} else {
						validationInfo.error("constant expected", method.getToken());
						valid = false;
					}
				}
			}
		}
		ctx.exit();
		return valid;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeByte(HiClass.CLASS_ANNOTATION);
		// TODO
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		// TODO
		return null;
	}

	@Override
	public Class getJavaClass() {
		return null;
	}
}
