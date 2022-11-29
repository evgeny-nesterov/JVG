package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
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
	public NodeForIterator visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();
		if (visitWord(Words.FOR, tokenizer) != null) {
			expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);

			Type type = visitType(tokenizer, true);
			if (type != null) {
				String name = visitWord(Words.NOT_SERVICE, tokenizer);
				if (name == null) {
					throw new ParseException("variable name is expected", tokenizer.currentToken());
				}

				if (checkSymbol(tokenizer, Symbols.COLON) != -1) {
					tokenizer.commit();
					tokenizer.nextToken();

					properties.enter();

					NodeDeclaration declaration = new NodeDeclaration(type, name, null, new Modifiers());
					properties.addLocalVariable(declaration);

					Node iterable = visitExpressions(tokenizer, properties);

					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

					Node body = expectBody(tokenizer, properties);

					NodeForIterator node = new NodeForIterator(declaration, iterable, body);
					properties.exit();
					return node;
				}
			}
		}
		tokenizer.rollback();
		return null;
	}

	public NodeBlock visitExpressions(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		Node expression = ExpressionParseRule.getInstance().visit(tokenizer, properties);
		if (expression != null) {
			NodeBlock expressions = new NodeBlock("expressions");
			expressions.addStatement(expression);

			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				expression = expectExpression(tokenizer, properties);
				expressions.addStatement(expression);
			}
			return expressions;
		}

		return null;
	}
}
