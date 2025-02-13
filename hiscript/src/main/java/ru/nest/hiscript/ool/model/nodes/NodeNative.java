package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.lang.reflect.Method;

public class NodeNative extends HiNode {
	public NodeNative(HiClass clazz, HiClass returnType, String name, HiClass[] argTypes, String[] argNames) {
		super("native", TYPE_NATIVE, false);

		this.argNames = argNames;

		StringBuilder id = new StringBuilder();
		id.append(clazz.fullName.startsWith(HiClass.ROOT_CLASS_NAME) ? clazz.fullName.substring(1) : clazz.fullName);
		id.append('_');
		id.append(returnType != null ? returnType.fullName : "void");
		id.append('_');
		id.append(name);
		if (argTypes != null) {
			for (int i = 0; i < argTypes.length; i++) {
				id.append('_');
				id.append(argTypes[i].fullName);
			}
		}

		this.id = id.toString().intern();
		argCount = argNames != null ? argNames.length : 0;
	}

	private NodeNative(String[] argNames, String id) {
		super("native", TYPE_NATIVE, false);
		this.argNames = argNames;
		this.argCount = argNames != null ? argNames.length : 0;
		this.id = id;
	}

	private final int argCount;

	private final String[] argNames;

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
		Object[] args = new Object[1 + argCount];
		args[0] = ctx;
		for (int i = 0; i < argCount; i++) {
			HiField<?> f = ctx.getVariable(argNames[i]);
			args[i + 1] = f.get();
		}
		ctx.getClassLoader().getNative().invoke(ctx, method, args);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeByte(argCount);
		os.writeUTFArray(argNames);
		os.writeUTF(id);
	}

	public static NodeNative decode(DecodeContext os) throws IOException {
		return new NodeNative(os.readUTFArray(os.readByte()), os.readUTF());
	}
}
