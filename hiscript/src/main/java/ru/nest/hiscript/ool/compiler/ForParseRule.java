package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeFor;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
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
	public NodeFor visit(Tokenizer tokenizer, CompileClassContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.FOR, tokenizer) != null) {
			Token startToken = tokenizer.currentToken();
			expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);

			properties.enter(RuntimeContext.FOR);
			Node initialization = DeclarationParseRule.getInstance().visit(tokenizer, properties);
			if (initialization == null) {
				initialization = visitExpressions(tokenizer, properties);
			}

			expectSymbol(tokenizer, Symbols.SEMICOLON);

			NodeExpression condition = ExpressionParseRule.getInstance().visit(tokenizer, properties);

			expectSymbol(tokenizer, Symbols.SEMICOLON);

			Node assignment = visitExpressions(tokenizer, properties);

			expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

			Node body = expectBody(tokenizer, properties);

			NodeFor forNode = new NodeFor(initialization, condition, assignment, body);
			forNode.setToken(tokenizer.getBlockToken(startToken));
			properties.exit();
			return forNode;
		}
		return null;
	}

	public NodeBlock visitExpressions(Tokenizer tokenizer, CompileClassContext properties) throws TokenizerException, ParseException {
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
