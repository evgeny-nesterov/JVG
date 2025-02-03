package ru.nest.hiscript.ool.model;

public class MethodSignature implements Cloneable {
	public MethodSignature() {
	}

	public MethodSignature(MethodSignature signature) {
		set(signature.name, signature.argClasses, signature.isVarargs);
	}

	public MethodSignature(String name, HiClass[] argClasses, boolean isVarargs) {
		set(name, argClasses, isVarargs);
	}

	public void set(String name, HiClass[] argClasses, boolean isVarargs) {
		this.name = name.intern();
		this.argClasses = argClasses;
		this.isVarargs = isVarargs;
		argCount = argClasses != null ? argClasses.length : 0;
		hashCode = name.hashCode();
		for (int i = 0; i < argCount; i++) {
			hashCode = 31 * hashCode + (argClasses[i] != null ? argClasses[i].hashCode() : 0);
		}
		if (isVarargs) {
			hashCode = 31 * hashCode + 1;
		}
		isLambda = name.startsWith(HiMethod.LAMBDA_METHOD_NAME);
	}

	public int argCount;

	public String name;

	public HiClass[] argClasses;

	private int hashCode;

	private boolean isVarargs;

	private boolean isLambda;

	public boolean isLambda() {
		return isLambda;
	}

	public HiClass.ArgClassPriorityType getArgsPriority(MethodSignature m, MethodSignature args) {
		if (equals(args)) {
			return HiClass.ArgClassPriorityType.higher;
		} else if (m.equals(args)) {
			return HiClass.ArgClassPriorityType.lower;
		}
		HiClass.ArgClassPriorityType argsPriority = HiClass.ArgClassPriorityType.equals;
		if (argClasses != null && m.argClasses != null && args.argClasses != null) {
			if (args.argCount > 0) {
				for (int i = 0; i < args.argCount; i++) {
					// assumed args.argCount >= argCount - 1
					HiClass c1 = i < argClasses.length - 1 ? argClasses[i] : (isVarargs ? argClasses[argClasses.length - 1].getArrayType() : argClasses[argClasses.length - 1]);
					HiClass c2 = i < m.argClasses.length - 1 ? m.argClasses[i] : (m.isVarargs ? m.argClasses[m.argClasses.length - 1].getArrayType() : m.argClasses[m.argClasses.length - 1]);
					HiClass argClass = args.argClasses[i];
					HiClass.ArgClassPriorityType argPriority = c1.getArgPriority(c2, argClass);
					switch (argPriority) {
						case higher:
							if (argsPriority == HiClass.ArgClassPriorityType.lower) {
								return HiClass.ArgClassPriorityType.nonComparable;
							} else {
								argsPriority = HiClass.ArgClassPriorityType.higher;
							}
							break;
						case lower:
							if (argsPriority == HiClass.ArgClassPriorityType.higher) {
								return HiClass.ArgClassPriorityType.nonComparable;
							} else {
								argsPriority = HiClass.ArgClassPriorityType.lower;
							}
							break;
						case equals:
							break;
						case nonComparable:
							return HiClass.ArgClassPriorityType.nonComparable;
					}
				}
			}
			if (!isVarargs && m.isVarargs) {
				argsPriority = HiClass.ArgClassPriorityType.higher;
			} else if (isVarargs && !m.isVarargs) {
				argsPriority = HiClass.ArgClassPriorityType.lower;
			}
		}
		return argsPriority;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MethodSignature) {
			MethodSignature m = (MethodSignature) o;
			if (!m.name.equals(name) && !m.isLambda && !isLambda) {
				return false;
			}

			if (m.isVarargs != isVarargs) {
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
				if (isVarargs && i == argCount - 1) {
					buf.append("...");
				}
			}
			buf.append(')');
			descr = buf.toString();
		}
		return descr;
	}
}
