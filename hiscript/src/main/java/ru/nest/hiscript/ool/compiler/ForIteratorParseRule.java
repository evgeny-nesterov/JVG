package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeDeclaration;
import ru.nest.hiscript.ool.model.nodes.NodeForIterator;
import ru.nest.hiscript.tokenizer.Symbols;
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
	public NodeForIterator visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		tokenizer.start();
		if (visitWord(Words.FOR, tokenizer) != null) {
			expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);

			NodeAnnotation[] annotations = AnnotationParseRule.getInstance().visitAnnotations(tokenizer, ctx);
			Type type = visitType(tokenizer, true);
			if (type != null) {
				String name = visitWord(Words.NOT_SERVICE, tokenizer);
				if (name == null) {
					throw new ParseException("variable name is expected", tokenizer.currentToken());
				}

				if (checkSymbol(tokenizer, Symbols.COLON) != -1) {
					tokenizer.commit();
					tokenizer.nextToken();

					ctx.enter(RuntimeContext.FOR);

					NodeDeclaration declaration = new NodeDeclaration(type, name, null, new Modifiers(), annotations);
					Node iterable = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
					Node body = expectBody(tokenizer, ctx);

					NodeForIterator node = new NodeForIterator(declaration, iterable, body);
					ctx.exit();
					return node;
				}
			}
		}
		tokenizer.rollback();
		return null;
	}
}
