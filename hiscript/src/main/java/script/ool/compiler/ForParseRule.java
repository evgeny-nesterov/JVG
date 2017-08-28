package script.ool.compiler;

import script.ParseException;
import script.ool.model.Node;
import script.ool.model.nodes.NodeBlock;
import script.ool.model.nodes.NodeExpression;
import script.ool.model.nodes.NodeFor;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class ForParseRule extends ParseRule<NodeFor> {
	private final static ForParseRule instance = new ForParseRule();

	public static ForParseRule getInstance() {
		return instance;
	}

	private ForParseRule() {
	}

	public NodeFor visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(WordToken.FOR, tokenizer) != null) {
			expectSymbol(tokenizer, SymbolToken.PARANTHESIS_LEFT);

			properties.enter();
			Node initialization = DeclarationParseRule.getInstance().visit(tokenizer, properties);
			if (initialization == null) {
				initialization = visitExpressions(tokenizer, properties);
			}

			expectSymbol(tokenizer, SymbolToken.SEMICOLON);

			NodeExpression condition = ExpressionParseRule.getInstance().visit(tokenizer, properties);

			expectSymbol(tokenizer, SymbolToken.SEMICOLON);

			Node assignment = visitExpressions(tokenizer, properties);

			expectSymbol(tokenizer, SymbolToken.PARANTHESIS_RIGHT);

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
