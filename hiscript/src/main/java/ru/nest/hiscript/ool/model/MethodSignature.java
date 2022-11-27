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
	}

	public int argCount;

	public String name;

	public HiClass[] argClasses;

	@Override
	public boolean equals(Object o) {
		if (o instanceof MethodSignature) {
			MethodSignature m = (MethodSignature) o;
			if (m.name != name) {
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
		int code = name.hashCode();
		for (int i = 0; i < argCount; i++) {
			code += 31 * argClasses[i].hashCode();
		}
		return code;
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
