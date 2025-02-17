package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.HiCompiler;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;

public interface ClassResolver {
	HiClass getCurrentClass();

	HiClass getClass(String name);

	HiClass getLocalClass(HiClass clazz, String name);

	void processResolverException(String message);

	HiClassLoader getClassLoader();

	HiCompiler getCompiler();

	HiRuntimeEnvironment getEnv();
}
