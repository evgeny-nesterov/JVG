package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeFor;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class ForParseRule extends ParseRule<NodeFor> {
	private final static ForParseRule instance = new ForParseRule();

	public static ForParseRule getInstance() {
		return instance;
	}

	private ForParseRule() {
	}

	@Override
	public NodeFor visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.FOR, tokenizer) != null) {
			expectSymbol(tokenizer, Symbols.PARANTHESIS_LEFT);

			properties.enter();
			Node initialization = DeclarationParseRule.getInstance().visit(tokenizer, properties);
			if (initialization == null) {
				initialization = visitExpressions(tokenizer, properties);
			}

			expectSymbol(tokenizer, Symbols.SEMICOLON);

			NodeExpression condition = ExpressionParseRule.getInstance().visit(tokenizer, properties);

			expectSymbol(tokenizer, Symbols.SEMICOLON);

			Node assignment = visitExpressions(tokenizer, properties);

			expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);

			Node body = expectBody(tokenizer, properties);

			NodeFor node = new NodeFor(initialization, condition, assignment, body);
			properties.exit();
			return node;
		}
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
