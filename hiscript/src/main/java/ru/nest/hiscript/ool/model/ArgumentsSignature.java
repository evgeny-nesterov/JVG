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

	public void set(HiClass[] argsClasses, boolean isVarargs) {
		if (argsClasses == null) {
			argsClasses = new HiClass[0];
		}
		this.argsClasses = argsClasses;
		this.isVarargs = isVarargs;
		argsCount = argsClasses != null ? argsClasses.length : 0;
		hashCode = 0;
		for (int i = 0; i < argsCount; i++) {
			hashCode = 31 * hashCode + (argsClasses[i] != null ? argsClasses[i].hashCode() : 0);
		}
		if (isVarargs) {
			hashCode = 31 * hashCode + 1;
		}
	}

	public int argsCount;

	public HiClass[] argsClasses;

	protected int hashCode;

	protected boolean isVarargs;

	public HiClass.ArgClassPriorityType getArgsPriority(ArgumentsSignature m, ArgumentsSignature args) {
		if (equalsArgs(args)) {
			return HiClass.ArgClassPriorityType.higher;
		} else if (m.equalsArgs(args)) {
			return HiClass.ArgClassPriorityType.lower;
		}
		return _getArgsPriority(m, args);
	}

	public HiClass.ArgClassPriorityType getPriority(ArgumentsSignature m, ArgumentsSignature args) {
		if (equals(args)) {
			return HiClass.ArgClassPriorityType.higher;
		} else if (m.equals(args)) {
			return HiClass.ArgClassPriorityType.lower;
		}
		return _getArgsPriority(m, args);
	}

	private HiClass.ArgClassPriorityType _getArgsPriority(ArgumentsSignature m, ArgumentsSignature args) {
		HiClass.ArgClassPriorityType argsPriority = HiClass.ArgClassPriorityType.equals;
		if (argsClasses != null && m.argsClasses != null && args.argsClasses != null) {
			if (args.argsCount > 0) {
				for (int i = 0; i < args.argsCount; i++) {
					// assumed args.argsCount >= argsCount - 1
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
			return equalsArgs((ArgumentsSignature) o);
		}
		return false;
	}

	public boolean equalsArgs(ArgumentsSignature s) {
		if (s.isVarargs != isVarargs) {
			return false;
		}

		if (argsClasses != null || s.argsClasses != null) {
			if (argsClasses == null || s.argsClasses == null) {
				return false;
			}

			if (argsCount != s.argsCount) {
				return false;
			}

			for (int i = 0; i < argsCount; i++) {
				if (argsClasses[i] != s.argsClasses[i]) {
					return false;
				}
			}
		}
		return true;
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
			for (int i = 0; i < argsCount; i++) {
				if (i != 0) {
					buf.append(", ");
				}
				if (isVarargs && i == argsCount - 1) {
					buf.append(argsClasses[i].getArrayType().getNameDescr());
					buf.append("...");
				} else {
					buf.append(argsClasses[i].getNameDescr());
				}
			}
			buf.append(')');
			descr = buf.toString();
		}
		return descr;
	}
}
