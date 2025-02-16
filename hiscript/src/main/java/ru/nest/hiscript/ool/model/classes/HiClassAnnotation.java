package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;

import java.io.IOException;

public class HiClassAnnotation extends HiClass {
	public HiClassAnnotation(HiClassLoader classLoader, HiClass enclosingClass, String name, int type) {
		super(classLoader, null, enclosingClass, name, type, null);
	}

	// for decode
	public HiClassAnnotation(String name, int type) {
		super(null, name, null, type);
		// init(...) is in decode
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
		ctx.currentNode = this;
		boolean valid = super.validate(validationInfo, ctx);
		ctx.enter(RuntimeContext.INITIALIZATION, this);
		if (methods != null) {
			for (HiMethod method : methods) {
				method.resolve(ctx);
				if (!method.returnClass.isConstant()) {
					validationInfo.error("invalid type '" + method.name + "' for annotation member", method.getToken());
					valid = false;
				} else if (method.isAnnotationArgument && method.body != null) {
					NodeValueType valueType = method.body.getNodeValueType(validationInfo, ctx);
					valid &= valueType.valid;
					if (valueType.isConstant) {
						if (valueType.valid && !valueType.clazz.isInstanceof(method.returnClass)) {
							validationInfo.error("incompatible types: " + valueType.clazz.getNameDescr() + " cannot be converted to " + method.returnClass.getNameDescr(), method.body.getToken());
							valid = false;
						}

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
		super.code(os, CLASS_ANNOTATION);
	}

	public static HiClass decode(DecodeContext os, int classIndex) throws IOException {
		return HiClass.decodeObject(os, CLASS_ANNOTATION, classIndex);
	}

	@Override
	public Class getJavaClass(HiRuntimeEnvironment env) {
		return null;
	}

	@Override
	public String getNameDescr() {
		return fullName;
	}
}
