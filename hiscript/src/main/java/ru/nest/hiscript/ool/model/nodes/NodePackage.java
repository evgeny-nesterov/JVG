package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.WordToken;

import java.io.IOException;

public class NodePackage extends HiNode {
	public NodePackage(WordToken[] pathTokens, Token token) {
		super("package", TYPE_PACKAGE, token, false);
		this.pathTokens = pathTokens;
		path = new String[pathTokens.length];
		for (int i = 0; i < pathTokens.length; i++) {
			path[i] = pathTokens[i].getWord();
		}
	}

	// for decode
	private NodePackage(String[] path, Token token) {
		super("package", TYPE_PACKAGE, token, false);
		this.path = path;
	}

	private WordToken[] pathTokens;

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

	public static NodePackage decode(DecodeContext os, Token token) throws IOException {
		return new NodePackage(os.readUTFArray(os.readByte()), token);
	}
}
