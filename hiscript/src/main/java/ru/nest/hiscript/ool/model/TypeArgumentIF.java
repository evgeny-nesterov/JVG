package ru.nest.hiscript.ool.model;

public interface TypeArgumentIF {
	public Type getType();

	public boolean isArray();

	public boolean isVarargs();

	public String getName();
}
