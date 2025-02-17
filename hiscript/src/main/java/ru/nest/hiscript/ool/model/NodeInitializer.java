package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;

public interface NodeInitializer extends Codeable, TokenAccessible {
	boolean isStatic();

	void execute(RuntimeContext ctx);

	@Override
	void code(CodeContext os) throws IOException;
}
