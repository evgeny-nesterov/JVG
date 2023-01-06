package ru.nest.hiscript.ool.model;

public class MethodSignature implements Cloneable {
	public MethodSignature() {
	}

	public MethodSignature(MethodSignature signature) {
		set(signature.name, signature.argClasses);
	}

	public MethodSignature(String name, HiClass[] argClasses) {
		set(name, argClasses);
	}

	public void set(String name, HiClass[] argClasses) {
		this.name = name.intern();
		this.argClasses = argClasses;
		argCount = argClasses != null ? argClasses.length : 0;
		hashCode = name.hashCode();
		for (int i = 0; i < argCount; i++) {
			hashCode += 31 * (argClasses[i] != null ? argClasses[i].hashCode() : 0);
		}
		isLambda = name.startsWith(HiMethod.LAMBDA_METHOD_NAME);
	}

	public int argCount;

	public String name;

	public HiClass[] argClasses;

	private int hashCode;

	private boolean isLambda;

	public boolean isLambda() {
		return isLambda;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MethodSignature) {
			MethodSignature m = (MethodSignature) o;
			if (!m.name.equals(name) && !m.isLambda && !isLambda) {
				return false;
			}

			if (argClasses != null || m.argClasses != null) {
				if (argClasses == null || m.argClasses == null) {
					return false;
				}

				if (argCount != m.argCount) {
					return false;
				}

				for (int i = 0; i < argCount; i++) {
					if (argClasses[i] != m.argClasses[i]) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private String descr;

	@Override
	public String toString() {
		if (descr == null) {
			StringBuilder buf = new StringBuilder();
			buf.append(name);
			buf.append('(');
			for (int i = 0; i < argCount; i++) {
				if (i != 0) {
					buf.append(", ");
				}
				buf.append(argClasses[i].name);
			}
			buf.append(')');
			descr = buf.toString();
		}
		return descr;
	}
}
