package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.Type;

public interface NodeVariable extends HiNodeIF {
	String UNNAMED = "_";

	String getVariableName();

	Type getVariableType();

	default HiClass getVariableClass(ClassResolver classResolver) {
		return getVariableType().getClass(classResolver);
	}

	default boolean isUnnamed() {
		return UNNAMED.equals(getVariableName());
	}
}
