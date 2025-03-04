package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeDeclaration;
import ru.nest.hiscript.ool.model.nodes.NodeForIterator;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import static ru.nest.hiscript.tokenizer.Words.*;

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
		if (visitWord(FOR, tokenizer) != null) {
			expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);

			startToken = startToken(tokenizer);
			AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
			Type type = visitType(tokenizer, true, ctx.getEnv());
			if (type != null) {
				String name = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
				if (name == null) {
					tokenizer.error("variable name is expected");
				}

				if (checkSymbol(tokenizer, Symbols.COLON) != -1) {
					tokenizer.commit();
					tokenizer.nextToken();

					ctx.enter(ContextType.FOR, startToken);

					NodeDeclaration declaration = new NodeDeclaration(type, name, null, annotatedModifiers.getModifiers(), annotatedModifiers.getAnnotations());
					declaration.setToken(tokenizer.getBlockToken(startToken));

					HiNode iterable = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
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
