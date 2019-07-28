package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Node;
import script.ool.model.RuntimeContext;

/**
 * This type of node presents any empty statement, for example, statement ';'.
 */
public class EmptyNode extends Node {
	private final static EmptyNode instance = new EmptyNode();

	public static EmptyNode getInstance() {
		return instance;
	}

	private EmptyNode() {
		super("empty", TYPE_EMPTY);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// do nothing
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static EmptyNode decode(DecodeContext os) throws IOException {
		return instance;
	}
}
