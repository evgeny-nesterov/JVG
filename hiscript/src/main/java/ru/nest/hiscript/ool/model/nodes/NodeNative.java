package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;
import java.lang.reflect.Method;

public class NodeNative extends HiNode {
	public NodeNative(HiClass clazz, HiClass returnType, String name, HiClass[] argsTypes, String[] argsNames) {
		super("native", TYPE_NATIVE, false);

		this.argsNames = argsNames;

		StringBuilder id = new StringBuilder();
		id.append(clazz.fullName.startsWith(HiClass.ROOT_CLASS_NAME) ? clazz.fullName.substring(1) : clazz.fullName);
		id.append('_');
		id.append(returnType != null ? returnType.fullName : "void");
		id.append('_');
		id.append(name);
		if (argsTypes != null) {
			for (int i = 0; i < argsTypes.length; i++) {
				id.append('_');
				id.append(argsTypes[i].fullName);
			}
		}

		this.id = id.toString().intern();
		argsCount = argsNames != null ? argsNames.length : 0;
	}

	private NodeNative(String[] argsNames, String id) {
		super("native", TYPE_NATIVE, false);
		this.argsNames = argsNames;
		this.argsCount = argsNames != null ? argsNames.length : 0;
		this.id = id;
	}

	private final int argsCount;

	private final String[] argsNames;

	private final String id;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		// TODO check whether native method exists
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// define method before set arguments
		Method method = ctx.getClassLoader().getNative().findMethod(ctx, id);
		if (ctx.exitFromBlock()) {
			return;
		}

		// invoke
		Object[] args = new Object[1 + argsCount];
		args[0] = ctx;
		for (int i = 0; i < argsCount; i++) {
			HiField<?> f = ctx.getVariable(argsNames[i]);
			args[i + 1] = f.get();
		}
		ctx.getClassLoader().getNative().invoke(ctx, method, args);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeByte(argsCount);
		os.writeUTFArray(argsNames);
		os.writeUTF(id);
	}

	public static NodeNative decode(DecodeContext os) throws IOException {
		return new NodeNative(os.readUTFArray(os.readByte()), os.readUTF());
	}
}
