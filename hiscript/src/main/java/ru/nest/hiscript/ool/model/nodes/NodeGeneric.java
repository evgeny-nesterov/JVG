package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;

public class NodeGeneric extends HiNode {
	public NodeGeneric(String genericName, boolean isSuper, Type genericType, int index) {
		super("generic", TYPE_GENERIC, false);
		this.genericName = genericName != null && genericName.length() > 0 ? genericName : null;
		this.isSuper = isSuper;
		this.genericType = genericType;
		this.index = index;
	}

	public String genericName;

	public final boolean isSuper;

	public final Type genericType;

	public final int index;

	public enum GenericSourceType {
		method, constructor, classSource, field
	}

	public GenericSourceType sourceType;

	public HiClass sourceClass;

	public HiClassGeneric clazz;

	public HiClass[] parametersClasses;

	public boolean isWildcard() {
		return genericName == null;
	}

	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx, boolean validType, int stage) {
		if (stage == 1) {
			parametersClasses = new HiClass[genericType.parameters != null ? genericType.parameters.length : 0];
			HiClass typeClass = validType ? genericType.getClass(ctx) : HiClass.OBJECT_CLASS;
			clazz = new HiClassGeneric(genericName, genericType, typeClass != null ? typeClass : HiClass.OBJECT_CLASS, parametersClasses, isSuper, sourceType, index, sourceClass, ctx);
			return typeClass != null;
		} else { // stage = 2
			boolean valid = true;
			for (int i = 0; i < parametersClasses.length; i++) {
				Type parameterType = genericType.parameters[i];
				HiClass parameterClass = parameterType.getClass(ctx);
				assert parameterClass != null;
				parametersClasses[i] = parameterClass;
			}
			valid &= genericType.validateClass(clazz.clazz, validationInfo, ctx, getToken());
			return valid;
		}
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// do nothing
	}

	@Override
	public String toString() {
		return new StringBuilder().append(isWildcard() ? "?" : genericName).append(isSuper ? " super " : " extends ").append(genericType).toString();
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullableUTF(genericName);
		os.writeBoolean(isSuper);
		os.writeType(genericType);
		os.writeInt(index);
		os.writeInt(sourceType.ordinal());
		os.writeClass(sourceClass);
		os.writeClass(clazz);
		os.writeClasses(parametersClasses);
	}

	public static NodeGeneric decode(DecodeContext os) throws IOException {
		NodeGeneric node = new NodeGeneric(os.readNullableUTF(), os.readBoolean(), os.readType(), os.readInt());
		node.sourceType = NodeGeneric.GenericSourceType.values()[os.readInt()];
		os.readClass(clazz -> node.sourceClass = clazz);
		os.readClass(clazz -> node.clazz = (HiClassGeneric) clazz);
		node.parametersClasses = os.readClasses();
		return node;
	}
}
