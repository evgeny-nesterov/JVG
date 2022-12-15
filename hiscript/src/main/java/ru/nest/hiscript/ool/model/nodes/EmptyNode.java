package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

import java.io.IOException;

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
