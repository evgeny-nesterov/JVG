package ru.nest.hiscript.ool.model;

public interface ClassResolver {
	HiClass getCurrentClass();

	HiClass getClass(String name);

	HiClass getLocalClass(HiClass clazz, String name);

	void processResolverException(String message);

	HiClassLoader getClassLoader();

	HiCompiler getCompiler();
}
