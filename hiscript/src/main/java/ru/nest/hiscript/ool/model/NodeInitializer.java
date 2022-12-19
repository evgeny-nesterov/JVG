package ru.nest.hiscript.ool.model;

import java.io.IOException;

import ru.nest.hiscript.ool.model.nodes.CodeContext;

public interface NodeInitializer extends Codeable {
	boolean isStatic();

	void execute(RuntimeContext ctx);

	@Override
	void code(CodeContext os) throws IOException;
}
