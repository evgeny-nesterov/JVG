package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeDeclaration;
import ru.nest.hiscript.ool.model.nodes.NodeForIterator;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

/**
 * for (<type> <name> : <iterable>) {<body>}
 */
public class ForIteratorParseRule extends ParseRule<NodeForIterator> {
	private final static ForIteratorParseRule instance = new ForIteratorParseRule();

	public static ForIteratorParseRule getInstance() {
		return instance;
	}

	private ForIteratorParseRule() {
	}

	@Override
	public NodeForIterator visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		if (visitWord(Words.FOR, tokenizer) != null) {
			expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);

			startToken = startToken(tokenizer);
			AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx);
			Type type = visitType(tokenizer, true);
			if (type != null) {
				String name = visitWord(Words.NOT_SERVICE, tokenizer);
				if (name == null) {
					throw new HiScriptParseException("variable name is expected", tokenizer.currentToken());
				}

				if (checkSymbol(tokenizer, Symbols.COLON) != -1) {
					tokenizer.commit();
					tokenizer.nextToken();

					ctx.enter(RuntimeContext.FOR, startToken);

					NodeDeclaration declaration = new NodeDeclaration(type, name, null, annotatedModifiers.getModifiers(), annotatedModifiers.getAnnotations());
					declaration.setToken(tokenizer.getBlockToken(startToken));

					HiNode iterable = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
					HiNode body = expectBody(tokenizer, ctx);

					ctx.exit();
					return new NodeForIterator(declaration, iterable, body);
				}
			}
		}
		tokenizer.rollback();
		return null;
	}
}
