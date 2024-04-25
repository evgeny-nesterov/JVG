package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NodeGenerics extends HiNode {
	public NodeGenerics(NodeGeneric[] generics) {
		super("generics", TYPE_GENERICS, false);
		this.generics = generics;
	}

	public final NodeGeneric[] generics;

	public void setSourceType(NodeGeneric.GenericSourceType sourceType) {
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

	private boolean checkCyclic(NodeGeneric generic, List<String> processed, List<String> ignore, List<NodeGeneric> order) {
		try {
			if (generic.genericName != null && generic.genericType != null && !ignore.contains(generic.genericName)) {
				if (processed.contains(generic.genericName)) {
					return false;
				}
				processed.add(generic.genericName);
				for (NodeGeneric generic2 : generics) {
					if (generic.genericType.name.equals(generic2.genericName)) {
						if (!checkCyclic(generic2, processed, ignore, order)) {
							return false;
						}
					}
				}
			}
		} finally {
			if (!order.contains(generic)) {
				order.add(generic);
			}
		}
		return true;
	}

	private List<String> checkCyclic(ValidationInfo validationInfo, List<NodeGeneric> order) {
		List<String> invalidGenerics = null;
		List<String> ignore = new ArrayList<>();
		for (NodeGeneric generic : generics) {
			List<String> processed = new ArrayList<>();
			if (!checkCyclic(generic, processed, ignore, order)) {
				if (invalidGenerics == null) {
					invalidGenerics = new ArrayList<>(1);
				}
				invalidGenerics.addAll(processed);
				validationInfo.error("cyclic inheritance involving '" + generic.genericName + "'", generic.getToken());
			}
			ignore.addAll(processed);
		}
		return invalidGenerics;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (generics.length == 0) {
			return true;
		}

		boolean valid = true;
		List<NodeGeneric> initOrder = new ArrayList<>();
		List<String> invalidGenerics = checkCyclic(validationInfo, initOrder);
		valid &= invalidGenerics == null || invalidGenerics.size() == 0;

		boolean hasDuplicate = false;
		for (int i = 0; i < initOrder.size(); i++) {
			NodeGeneric generic = initOrder.get(i);
			generic.sourceClass = ctx.clazz;
			boolean validType = invalidGenerics == null || !invalidGenerics.contains(generic.genericName);
			valid &= generic.validate(validationInfo, ctx, validType, 1);
			if (!hasDuplicate) {
				for (int j = i + 1; j < initOrder.size(); j++) {
					NodeGeneric generic2 = initOrder.get(j);
					if (Objects.equals(generic.genericName, generic2.genericName)) {
						validationInfo.error("duplicate type parameter: '" + generic.genericName + "'", generic2.getToken());
						valid = false;
						hasDuplicate = true;
					}
				}
			}
		}
		for (int i = 0; i < initOrder.size(); i++) {
			NodeGeneric generic = initOrder.get(i);
			valid &= generic.validate(validationInfo, ctx, true, 2);
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (NodeGeneric generic : generics) {
			if (s.length() > 0) {
				s.append(", ");
			}
			s.append(generic);
		}
		return s.toString();
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
