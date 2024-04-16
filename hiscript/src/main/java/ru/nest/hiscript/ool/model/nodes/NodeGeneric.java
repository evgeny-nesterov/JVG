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
	public NodeGeneric(String genericName, boolean isSuper, Type genericType, int index) {
		super("generic", TYPE_GENERICS, false);
		this.genericName = genericName;
		this.isSuper = isSuper;
		this.genericType = genericType;
		this.index = index;
	}

	public final String genericName;

	public final boolean isSuper;

	public final Type genericType;

	public final int index;

	/**
	 * RuntimeContext.METHOD
	 * RuntimeContext.CONSTRUCTOR
	 * RuntimeContext.STATIC_CLASS
	 */
	public int sourceType;

	public HiClass sourceClass;

	public HiClassGeneric clazz;

	public HiClass[] parametersClasses;

	public boolean  isWildcard() {
		return genericName == null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass typeClass = genericType.getClass(ctx);
		parametersClasses = new HiClass[genericType.parameters != null ? genericType.parameters.length : 0];
		for (int i = 0; i < parametersClasses.length; i++) {
			Type parameter = genericType.parameters[i];
			parametersClasses[i] = parameter.getClass(ctx);
		}

		clazz = new HiClassGeneric(genericName, genericType, typeClass != null ? typeClass : HiClass.OBJECT_CLASS, parametersClasses, isSuper, sourceType, index, sourceClass, ctx);
		return typeClass != null;
	}

	@Override
	public void execute(RuntimeContext ctx) {
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullableUTF(genericName);
		os.writeBoolean(isSuper);
		os.writeType(genericType);
		os.writeInt(index);
		os.writeInt(sourceType);
	}

	public static NodeGeneric decode(DecodeContext os) throws IOException {
		NodeGeneric node = new NodeGeneric(os.readNullableUTF(), os.readBoolean(), os.readType(), os.readInt());
		node.sourceType = os.readInt();
		return node;
	}
}
