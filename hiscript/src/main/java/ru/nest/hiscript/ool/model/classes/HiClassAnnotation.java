package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassLocationType;
import ru.nest.hiscript.ool.model.ClassType;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;

public class HiClassAnnotation extends HiClass {
	public HiClassAnnotation(HiClassLoader classLoader, HiClass enclosingClass, String name, ClassLocationType locationType) {
		super(classLoader, null, enclosingClass, name, locationType, null);
	}

	// for decode
	public HiClassAnnotation(String name, ClassLocationType locationType) {
		super(null, name, null, locationType);
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
		ctx.enter(ContextType.INITIALIZATION, this);
		if (methods != null) {
			for (HiMethod method : methods) {
				method.resolve(ctx);
				if (!method.returnClass.isConstant()) {
					validationInfo.error("invalid type '" + method.name + "' for annotation member", method);
					valid = false;
				} else if (method.isAnnotationArgument && method.body != null) {
					NodeValueType valueType = method.body.getNodeValueType(validationInfo, ctx);
					valid &= valueType.valid;
					if (valueType.isConstant) {
						if (valueType.valid && !valueType.clazz.isInstanceof(method.returnClass)) {
							validationInfo.error("incompatible types: " + valueType.clazz.getNameDescr() + " cannot be converted to " + method.returnClass.getNameDescr(), method.body);
							valid = false;
						}

						HiClass outboundClass = ctx.clazz;
						ctx.clazz = this;
						method.annotationDefaultValue = method.body.getObjectValue(validationInfo, ctx, method.body.getToken());
						ctx.clazz = outboundClass;
					} else {
						validationInfo.error("constant expected", method);
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
		super.code(os, ClassType.CLASS_ANNOTATION);
	}

	public static HiClass decode(DecodeContext os, int classIndex) throws IOException {
		return HiClass.decodeObject(os, ClassType.CLASS_ANNOTATION, classIndex);
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
