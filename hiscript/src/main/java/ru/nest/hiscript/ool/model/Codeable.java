package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.nodes.CodeContext;

import java.io.IOException;

public interface Codeable {
	void code(CodeContext os) throws IOException;
}
