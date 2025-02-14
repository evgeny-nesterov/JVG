package ru.nest.hiscript.ool.model;

public class ArgumentsSignature implements Cloneable {
	public ArgumentsSignature() {
	}

	public ArgumentsSignature(ArgumentsSignature signature) {
		set(signature.argsClasses, signature.isVarargs);
	}

	public ArgumentsSignature(HiClass[] argsClasses, boolean isVarargs) {
		set(argsClasses, isVarargs);
	}

	public void set(HiClass[] argClasses, boolean isVarargs) {
		this.argsClasses = argClasses;
		this.isVarargs = isVarargs;
		argCount = argClasses != null ? argClasses.length : 0;
		hashCode = 0;
		for (int i = 0; i < argCount; i++) {
			hashCode = 31 * hashCode + (argClasses[i] != null ? argClasses[i].hashCode() : 0);
		}
		if (isVarargs) {
			hashCode = 31 * hashCode + 1;
		}
	}

	public int argCount;

	public HiClass[] argsClasses;

	protected int hashCode;

	protected boolean isVarargs;

	public HiClass.ArgClassPriorityType getArgsPriority(ArgumentsSignature m, ArgumentsSignature args) {
		if (equals(args)) {
			return HiClass.ArgClassPriorityType.higher;
		} else if (m.equals(args)) {
			return HiClass.ArgClassPriorityType.lower;
		}
		HiClass.ArgClassPriorityType argsPriority = HiClass.ArgClassPriorityType.equals;
		if (argsClasses != null && m.argsClasses != null && args.argsClasses != null) {
			if (args.argCount > 0) {
				for (int i = 0; i < args.argCount; i++) {
					// assumed args.argCount >= argCount - 1
					HiClass c1 = i < argsClasses.length - 1 ? argsClasses[i] : (isVarargs ? argsClasses[argsClasses.length - 1].getArrayType() : argsClasses[argsClasses.length - 1]);
					HiClass c2 = i < m.argsClasses.length - 1 ? m.argsClasses[i] : (m.isVarargs ? m.argsClasses[m.argsClasses.length - 1].getArrayType() : m.argsClasses[m.argsClasses.length - 1]);
					HiClass argClass = args.argsClasses[i];
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
		if (o instanceof ArgumentsSignature) {
			ArgumentsSignature m = (ArgumentsSignature) o;
			if (m.isVarargs != isVarargs) {
				return false;
			}

			if (argsClasses != null || m.argsClasses != null) {
				if (argsClasses == null || m.argsClasses == null) {
					return false;
				}

				if (argCount != m.argCount) {
					return false;
				}

				for (int i = 0; i < argCount; i++) {
					if (argsClasses[i] != m.argsClasses[i]) {
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
			buf.append('(');
			for (int i = 0; i < argCount; i++) {
				if (i != 0) {
					buf.append(", ");
				}
				buf.append(argsClasses[i].name);
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
