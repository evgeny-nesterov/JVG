package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.ArgumentNode;
import ru.nest.hiscript.pol.model.ArgumentsNode;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class ArgumentsParseRule extends ParseRule<ArgumentsNode> {
	private final static ArgumentsParseRule instance = new ArgumentsParseRule();

	public static ArgumentsParseRule getInstance() {
		return instance;
	}

	private ArgumentsParseRule() {
	}

	@Override
	public ArgumentsNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		ArgumentNode argument = ArgumentParseRule.getInstance().visit(tokenizer);
		if (argument != null) {
			ArgumentsNode node = new ArgumentsNode();
			node.addArgument(argument);

			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				argument = ArgumentParseRule.getInstance().visit(tokenizer);
				if (argument == null) {
					throw new HiScriptParseException("argument expected", tokenizer.currentToken());
				}
				node.addArgument(argument);
			}

			return node;
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (ArgumentParseRule.getInstance().visit(tokenizer, handler)) {
			while (visitSymbol(tokenizer, handler, Symbols.COMMA) != -1) {
				if (!ArgumentParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccurred(tokenizer, handler, "argument expected");
				}
			}

			return true;
		}

		return false;
	}
}
