package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import static ru.nest.hiscript.tokenizer.Words.NOT_SERVICE;

public class InvocationParseRule extends ParseRule<NodeInvocation> {
	private final static InvocationParseRule instance = new InvocationParseRule();

	public static InvocationParseRule getInstance() {
		return instance;
	}

	private InvocationParseRule() {
	}

	@Override
	public NodeInvocation visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		String name = visitWord(tokenizer, NOT_SERVICE);
		if (name != null) {
			if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
				HiNode[] args = visitArgumentsValues(tokenizer, ctx);
				if (checkSymbol(tokenizer, Symbols.PARENTHESES_RIGHT) != -1) {
					tokenizer.nextToken();
					tokenizer.commit();
					return new NodeInvocation(name, args);
				}
			}
		}

		tokenizer.rollback();
		return null;
	}
}
