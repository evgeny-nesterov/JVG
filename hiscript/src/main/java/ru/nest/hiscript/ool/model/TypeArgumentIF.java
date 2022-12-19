package ru.nest.hiscript.ool.model;

public interface TypeArgumentIF {
	Type getType();

	boolean isArray();

	boolean isVarargs();

	String getName();
}
