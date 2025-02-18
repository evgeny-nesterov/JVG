package ru.nest.hiscript.pol.model;

public abstract class Method {
	public Method(String namespace, String name, int[] argsTypes, int[] argsDimensions, int returnType, int returnDimension) {
		if (namespace == null) {
			namespace = "";
		}
		this.namespace = namespace.intern();
		this.name = name.intern();
		this.argsTypes = argsTypes;
		this.argsDimensions = argsDimensions;
		this.returnType = returnType;
		this.returnDimension = returnDimension;
		hashCode = getHash(namespace, name, argsTypes);
	}

	public Method(String namespace, String name, int[] argsTypes, int[] argsDimensions, int returnType) {
		this(namespace, name, argsTypes, argsDimensions, returnType, 0);
	}

	private final String namespace;

	public String getNamespace() {
		return namespace;
	}

	private final String name;

	public String getName() {
		return name;
	}

	private final int[] argsTypes;

	public int[] getArgsTypes() {
		return argsTypes;
	}

	private final int[] argsDimensions;

	public int[] getArgsDimensions() {
		return argsDimensions;
	}

	private final int returnType;

	public int getReturnType() {
		return returnType;
	}

	private final int returnDimension;

	public int getReturnDimension() {
		return returnDimension;
	}

	public static int getHash(String namespace, String name, int... argsTypes) {
		int hash = name.hashCode();
		for (int t : argsTypes) {
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
		return getMethodDescr(namespace, name, argsTypes, argsDimensions, returnType, returnDimension);
	}

	public static String getMethodDescr(String namespace, String name, int[] argsTypes, int[] argsDimensions, int returnType) {
		return getMethodDescr(namespace, name, argsTypes, argsDimensions, returnType, 0);
	}

	public static String getMethodDescr(String namespace, String name, int[] argsTypes, int[] argsDimensions, int returnType, int returnDimension) {
		String args = "";
		for (int i = 0; i < argsTypes.length; i++) {
			args += Types.getTypeDescr(argsTypes[i], argsDimensions != null ? argsDimensions[i] : 0);
			if (i < argsTypes.length - 1) {
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
