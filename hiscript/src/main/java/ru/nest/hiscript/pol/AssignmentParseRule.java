package ru.nest.hiscript.pol;

import java.util.ArrayList;
import java.util.List;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.AssignmentNode;
import ru.nest.hiscript.pol.model.ExpressionNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.VariableNode;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class AssignmentParseRule extends ParseRule<AssignmentNode> {
	private final static AssignmentParseRule instance = new AssignmentParseRule();

	public static AssignmentParseRule getInstance() {
		return instance;
	}

	private AssignmentParseRule() {
	}

	@Override
	public AssignmentNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();

		VariableNode variable = visitVariable(tokenizer);
		if (variable != null) {
			List<ExpressionNode> indexes = new ArrayList<>();
			while (visitSymbol(tokenizer, Symbols.SQUARE_BRACES_LEFT) != -1) {
				ExpressionNode index = ExpressionParseRule.getInstance().visit(tokenizer);
				if (index == null) {
					throw new ParseException("array dimension missing", tokenizer.currentToken());
				}
				expectSymbol(Symbols.SQUARE_BRACES_RIGHT, tokenizer);
				indexes.add(index);
			}

			int equateType = visitEquate(tokenizer);
			if (equateType != -1) {
				tokenizer.commit();
				Node value = ExpressionParseRule.getInstance().visit(tokenizer);
				if (value == null) {
					throw new ParseException("Expression is expected", tokenizer.currentToken());
				}
				return new AssignmentNode(variable.getNamespace(), variable.getVarName(), indexes, value, equateType);
			} else if (indexes.size() > 0) {
				throw new ParseException("not a statement", tokenizer.currentToken());
			}
		}

		tokenizer.rollback();
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();

		VariableNode variable = visitVariable(tokenizer, handler);
		if (variable != null) {
			List<ExpressionNode> indexes = new ArrayList<>();
			while (visitSymbol(tokenizer, handler, Symbols.SQUARE_BRACES_LEFT) != -1) {
				if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccurred(tokenizer, handler, "array dimension missing");
				}
				expectSymbol(Symbols.SQUARE_BRACES_RIGHT, tokenizer, handler);
			}

			int equateType = visitEquate(tokenizer, handler);
			if (equateType != -1) {
				tokenizer.commit();
				if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccurred(tokenizer, handler, "Expression is expected");
				}
				return true;
			} else if (indexes.size() > 0) {
				errorOccurred(tokenizer, handler, "not a statement");
			}
		}

		tokenizer.rollback();
		return false;
	}
}
