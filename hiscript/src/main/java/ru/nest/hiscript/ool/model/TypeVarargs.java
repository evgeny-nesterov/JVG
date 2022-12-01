package ru.nest.hiscript.ool.model;

public class TypeVarargs implements TypeArgumentIF {
	private Type type;

	public TypeVarargs(Type arrayType) {
		this.type = arrayType;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public boolean isVarargs() {
		return true;
	}

	@Override
	public String getName() {
		return type.getName();
	}
}
