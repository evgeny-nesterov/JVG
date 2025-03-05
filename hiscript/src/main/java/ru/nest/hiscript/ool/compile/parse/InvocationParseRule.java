package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import static ru.nest.hiscript.tokenizer.WordType.NOT_SERVICE;
import static ru.nest.hiscript.tokenizer.WordType.UNNAMED_VARIABLE;

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

		String name = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
		if (name != null) {
			if (visitSymbol(tokenizer, SymbolType.PARENTHESES_LEFT) != null) {
				HiNode[] args = visitArgumentsValues(tokenizer, ctx);
				if (checkSymbol(tokenizer, SymbolType.PARENTHESES_RIGHT) != null) {
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
