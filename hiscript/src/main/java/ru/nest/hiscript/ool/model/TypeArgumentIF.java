package ru.nest.hiscript.ool.model;

public interface TypeArgumentIF {
	Type getType();

	HiClass getClass(ClassResolver classResolver);

	boolean isArray();

	boolean isVarargs();

	String getName();
}
