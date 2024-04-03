package ru.nest.hiscript.pol.model;

public abstract class Method {
	public Method(String namespace, String name, int[] argTypes, int[] argDimensions, int returnType, int returnDimension) {
		if (namespace == null) {
			namespace = "";
		}
		this.namespace = namespace.intern();
		this.name = name.intern();
		this.argTypes = argTypes;
		this.argDimensions = argDimensions;
		this.returnType = returnType;
		this.returnDimension = returnDimension;
		hashCode = getHash(namespace, name, argTypes);
	}

	public Method(String namespace, String name, int[] argTypes, int[] argDimensions, int returnType) {
		this(namespace, name, argTypes, argDimensions, returnType, 0);
	}

	private final String namespace;

	public String getNamespace() {
		return namespace;
	}

	private final String name;

	public String getName() {
		return name;
	}

	private final int[] argTypes;

	public int[] getArgTypes() {
		return argTypes;
	}

	private final int[] argDimensions;

	public int[] getArgDimensions() {
		return argDimensions;
	}

	private final int returnType;

	public int getReturnType() {
		return returnType;
	}

	private final int returnDimension;

	public int getReturnDimension() {
		return returnDimension;
	}

	public static int getHash(String namespace, String name, int... argTypes) {
		int hash = name.hashCode();
		for (int t : argTypes) {
			hash = 31 * hash + t;
		}
		if (namespace != null && namespace.length() > 0) {
			hash = 31 * hash + namespace.hashCode();
		}
		return hash;
	}

	private final int hashCode;

	@Override
	public int hashCode() {
		return hashCode;
	}

	public void invoke(RuntimeContext ctx, Node node, Object... values) throws ExecuteException {
		ctx.value.type = returnType;
		ctx.value.dimension = returnDimension;
	}

	@Override
	public String toString() {
		return getMethodDescr(namespace, name, argTypes, argDimensions, returnType, returnDimension);
	}

	public static String getMethodDescr(String namespace, String name, int[] argTypes, int[] argDimensions, int returnType) {
		return getMethodDescr(namespace, name, argTypes, argDimensions, returnType, 0);
	}

	public static String getMethodDescr(String namespace, String name, int[] argTypes, int[] argDimensions, int returnType, int returnDimension) {
		String args = "";
		for (int i = 0; i < argTypes.length; i++) {
			args += Types.getTypeDescr(argTypes[i], argDimensions != null ? argDimensions[i] : 0);
			if (i < argTypes.length - 1) {
				args += ", ";
			}
		}

		String descr = Types.getTypeDescr(returnType, returnDimension) + " ";
		if (namespace != null && namespace.length() > 0) {
			descr = namespace + ".";
		}
		descr += name + "(" + args + ")";
		return descr;
	}
}
