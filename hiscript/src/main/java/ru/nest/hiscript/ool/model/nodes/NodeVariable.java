package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiNodeIF;

public interface NodeVariable extends HiNodeIF {
	String getVariableName();

	String getVariableType();
}
