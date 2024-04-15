package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.Objects;

public class NodeGenerics extends HiNode {
	public NodeGenerics(NodeGeneric[] generics) {
		super("generics", TYPE_GENERICS, false);
		this.generics = generics;
	}

	public final NodeGeneric[] generics;

	public NodeGeneric getGeneric(String name) {
		for (int i = 0; i < generics.length; i++) {
			NodeGeneric generic = generics[i];
			if (name.equals(generic.name)) {
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
			valid &= generic.validate(validationInfo, ctx);
			if (!hasDuplicate) {
				for (int j = i + 1; j < generics.length; j++) {
					NodeGeneric generic2 = generics[j];
					if (Objects.equals(generic.name, generic2.name)) {
						validationInfo.error("duplicate type parameter: '" + generic.name + "'", generic2.getToken());
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
