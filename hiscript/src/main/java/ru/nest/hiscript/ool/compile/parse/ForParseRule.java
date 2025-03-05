package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeFor;
import ru.nest.hiscript.tokenizer.SymbolType;
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
	public NodeFor visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(Words.FOR, tokenizer) != null) {
			expectSymbol(tokenizer, SymbolType.PARENTHESES_LEFT);

			ctx.enter(ContextType.FOR, startToken);
			HiNode initialization = DeclarationParseRule.getInstance().visit(tokenizer, ctx);
			if (initialization == null) {
				initialization = visitExpressions(tokenizer, ctx);
			}

			expectSymbol(tokenizer, SymbolType.SEMICOLON);
			NodeExpression condition = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
			expectSymbol(tokenizer, SymbolType.SEMICOLON);
			HiNode assignment = visitExpressions(tokenizer, ctx);
			expectSymbol(tokenizer, SymbolType.PARENTHESES_RIGHT);
			HiNode body = expectBody(tokenizer, ctx);

			ctx.exit();
			return new NodeFor(initialization, condition, assignment, body);
		}
		return null;
	}

	public NodeBlock visitExpressions(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		HiNode expression = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
		if (expression != null) {
			NodeBlock expressions = new NodeBlock("expressions");
			expressions.addStatement(expression);
			while (visitSymbol(tokenizer, SymbolType.COMMA) != null) {
				expression = expectExpression(tokenizer, ctx);
				expressions.addStatement(expression);
			}
			return expressions;
		}
		return null;
	}
}
