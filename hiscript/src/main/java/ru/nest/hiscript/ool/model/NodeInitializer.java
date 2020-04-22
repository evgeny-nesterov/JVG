package ru.nest.hiscript.ool.model;

import java.io.IOException;

import ru.nest.hiscript.ool.model.nodes.CodeContext;

public interface NodeInitializer extends Codable {
	public boolean isStatic();

	public void execute(RuntimeContext ctx);

	@Override
	public void code(CodeContext os) throws IOException;
}
