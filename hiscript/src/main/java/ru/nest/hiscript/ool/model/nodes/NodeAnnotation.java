package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeAnnotation extends HiNode {
	public NodeAnnotation(String name, NodeAnnotationArgument[] args) {
		super("annotation", TYPE_ANNOTATION, false);
		this.name = name;
		this.args = args;
	}

	public String name;

	private final NodeAnnotationArgument[] args;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = true;
		HiClass clazz = ctx.getClass(name);
		if (clazz == null) {
			if (!validationInfo.hasMessage("annotation name is expected")) {
				validationInfo.error("cannot resolve class '" + name + "'", getToken());
				valid = false;
			}
		} else if (!clazz.isAnnotation()) {
			validationInfo.error("annotation class expected", getToken());
			valid = false;
		}
		if (args != null) {
			List<String> argsNames = new ArrayList<>(args.length);
			for (NodeAnnotationArgument arg : args) {
				valid &= arg.validate(validationInfo, ctx);
				if (argsNames.contains(arg.name)) {
					validationInfo.error("duplicate annotation argument", arg.getToken());
					valid = false;
				} else {
					argsNames.add(arg.name);
					if (clazz != null && clazz.isAnnotation()) {
						HiMethod method = clazz.searchMethod(ctx, arg.name);
						if (method == null || !method.isAnnotationArgument) {
							validationInfo.error("annotation argument with name '" + arg.name + "' is not found", arg.getToken());
							valid = false;
						} else {
							NodeValueType argValueType = arg.getNodeValueType(validationInfo, ctx);
							valid &= argValueType.valid;
							if (argValueType.valid && !argValueType.clazz.isInstanceof(method.returnClass)) {
								validationInfo.error("incompatible types: " + argValueType.clazz.getNameDescr() + " cannot be converted to " + method.returnClass.getNameDescr(), arg.valueNode.getToken());
								valid = false;
							}
						}
					}
				}
			}
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		throw new HiScriptRuntimeException("cannot execute annotation");
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(name);
		os.writeShortArray(args);
	}

	public static NodeAnnotation decode(DecodeContext os) throws IOException {
		return new NodeAnnotation(os.readUTF(), os.readShortNodeArray(NodeAnnotationArgument.class));
	}
}
