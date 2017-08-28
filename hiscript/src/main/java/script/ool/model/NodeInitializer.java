package script.ool.model;

import java.io.IOException;

import script.ool.model.nodes.CodeContext;

public interface NodeInitializer extends Codable {
	public boolean isStatic();

	public void execute(RuntimeContext ctx);

	public void code(CodeContext os) throws IOException;
}
