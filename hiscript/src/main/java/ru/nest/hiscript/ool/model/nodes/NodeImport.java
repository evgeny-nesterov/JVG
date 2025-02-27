package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class NodeImport extends HiNode {
	public NodeImport(String[] path, Token token) {
		super("import", TYPE_IMPORT, token, false);
		this.path = path;
	}

	private String[] path;

	public String[] getPath() {
		return path;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// not supported
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeByte(path.length);
		os.writeUTFArray(path);
	}

	public static NodeImport decode(DecodeContext os, Token token) throws IOException {
		return new NodeImport(os.readUTFArray(os.readByte()), token);
	}
}
