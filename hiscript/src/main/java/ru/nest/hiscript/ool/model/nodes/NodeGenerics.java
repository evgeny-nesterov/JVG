package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.Objects;

public class NodeGenerics extends HiNode {
	public NodeGenerics(NodeGeneric[] generics) {
		super("generics", TYPE_GENERICS, false);
		this.generics = generics;
	}

	public final NodeGeneric[] generics;

	public void setSourceType(int sourceType) {
		if (generics != null) {
			for (NodeGeneric generic : generics) {
				generic.sourceType = sourceType;
			}
		}
	}

	public HiClassGeneric getGenericClass(ClassResolver classResolver, String name) {
		NodeGeneric generic = getGeneric(name);
		if (generic != null && generic.clazz != null) {
			generic.clazz.init(classResolver);
			// TODO return HiClassParameterized
			return generic.clazz;
		}
		return null;
	}

	public NodeGeneric getGeneric(String name) {
		for (int i = 0; i < generics.length; i++) {
			NodeGeneric generic = generics[i];
			if (name.equals(generic.genericName)) {
				return generic;
			}
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		boolean hasDuplicate = false;
		for (int i = 0; i < generics.length; i++) {
			NodeGeneric generic = generics[i];
			generic.sourceClass = ctx.clazz;
			valid &= generic.validate(validationInfo, ctx);
			if (!hasDuplicate) {
				for (int j = i + 1; j < generics.length; j++) {
					NodeGeneric generic2 = generics[j];
					if (Objects.equals(generic.genericName, generic2.genericName)) {
						validationInfo.error("duplicate type parameter: '" + generic.genericName + "'", generic2.getToken());
						valid = false;
						hasDuplicate = true;
					}
				}
			}
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeInt(generics.length);
		os.writeArray(generics);
	}

	public static NodeGenerics decode(DecodeContext os) throws IOException {
		return new NodeGenerics(os.readArray(NodeGeneric.class, os.readInt()));
	}
}
