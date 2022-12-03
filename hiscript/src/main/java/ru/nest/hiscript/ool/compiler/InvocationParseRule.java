package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class InvocationParseRule extends ParseRule<NodeInvocation> {
	private final static InvocationParseRule instance = new InvocationParseRule();

	public static InvocationParseRule getInstance() {
		return instance;
	}

	private InvocationParseRule() {
	}

	@Override
	public NodeInvocation visit(Tokenizer tokenizer, CompileContext ctx) throws TokenizerException, ParseException {
		tokenizer.start();

		String name = visitWord(tokenizer, NOT_SERVICE);
		if (name != null) {
			if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
				Node[] args = visitArgumentsValues(tokenizer, ctx);
				if (checkSymbol(tokenizer, Symbols.PARENTHESES_RIGHT) != -1) {
					tokenizer.nextToken();
					tokenizer.commit();
					NodeInvocation node = new NodeInvocation(name, args);
					return node;
				}
			}
		}

		tokenizer.rollback();
		return null;
	}
}
