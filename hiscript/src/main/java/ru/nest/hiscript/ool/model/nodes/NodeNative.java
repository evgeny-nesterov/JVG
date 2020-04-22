package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Field;
import ru.nest.hiscript.ool.model.Native;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

public class NodeNative extends Node {
	public NodeNative(Clazz clazz, Clazz returnType, String name, Clazz[] argTypes, String[] argNames) {
		super("native", TYPE_NATIVE);

		this.argNames = argNames;

		StringBuilder id = new StringBuilder();
		id.append(clazz.fullName);
		id.append('_');
		id.append(returnType.fullName);
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
		super("native", TYPE_NATIVE);
		this.argNames = argNames;
		this.argCount = argNames != null ? argNames.length : 0;
		this.id = id;
	}

	private int argCount;

	private String[] argNames;

	private String id;

	@Override
	public void execute(RuntimeContext ctx) {
		Object[] args = new Object[1 + argCount];
		args[0] = ctx;
		for (int i = 0; i < argCount; i++) {
			Field<?> f = ctx.getVariable(argNames[i]);
			args[i + 1] = f.get();
		}
		Native.invoke(ctx, id, args);
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
