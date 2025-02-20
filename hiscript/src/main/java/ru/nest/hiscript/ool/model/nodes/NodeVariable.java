package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiNodeIF;

public interface NodeVariable extends HiNodeIF {
	String UNNAMED = "_";

	String getVariableName();

	String getVariableType();

	default boolean isUnnamed() {
		return UNNAMED.equals(getVariableName());
	}
}
