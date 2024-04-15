package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeGeneric extends HiNode {
	public NodeGeneric(String name, boolean isSuper, Type type) {
		super("generic", TYPE_GENERICS, false);
		this.name = name;
		this.isSuper = isSuper;
		this.type = type;
	}

	public final String name;

	public final boolean isSuper;

	public final Type type;

	public HiClassGeneric clazz;

	public boolean isWildcard() {
		return name == null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass typeClass = type.getClass(ctx);
		clazz = new HiClassGeneric(name, typeClass != null ? typeClass : HiClass.OBJECT_CLASS, isSuper);
		return typeClass != null;
	}

	@Override
	public void execute(RuntimeContext ctx) {
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullableUTF(name);
		os.writeBoolean(isSuper);
		os.writeType(type);
	}

	public static NodeGeneric decode(DecodeContext os) throws IOException {
		return new NodeGeneric(os.readNullableUTF(), os.readBoolean(), os.readType());
	}
}
