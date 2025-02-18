package ru.nest.hiscript.ool.model;

public class MethodSignature extends ArgumentsSignature {
	public MethodSignature() {
	}

	public MethodSignature(MethodSignature signature) {
		set(signature.name, signature.argsClasses, signature.isVarargs);
	}

	public MethodSignature(String name, HiClass[] argsClasses, boolean isVarargs) {
		set(name, argsClasses, isVarargs);
	}

	public void set(String name, HiClass[] argsClasses, boolean isVarargs) {
		super.set(argsClasses, isVarargs);
		this.name = name.intern();
		hashCode = 31 * hashCode + name.hashCode();
		isLambda = name.startsWith(HiMethod.LAMBDA_METHOD_NAME);
	}

	public String name;

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
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private String descr;

	@Override
	public String toString() {
		return name + super.toString();
	}
}
