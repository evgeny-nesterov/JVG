package script.ool.model;

import java.io.IOException;

import script.ool.model.nodes.CodeContext;

public interface Codable {
	public void code(CodeContext os) throws IOException;
}
