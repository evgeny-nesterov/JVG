package ru.nest.hiscript.ool.model;

import java.io.IOException;

import ru.nest.hiscript.ool.model.nodes.CodeContext;

public interface Codeable {
	void code(CodeContext os) throws IOException;
}
