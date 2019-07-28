package script.pol;

import java.util.ArrayList;
import java.util.List;

import script.ParseException;
import script.pol.model.AssignmentNode;
import script.pol.model.ExpressionNode;
import script.pol.model.Node;
import script.pol.model.VariableNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

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
			List<ExpressionNode> indexes = new ArrayList<ExpressionNode>();
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
			List<ExpressionNode> indexes = new ArrayList<ExpressionNode>();
			while (visitSymbol(tokenizer, handler, Symbols.SQUARE_BRACES_LEFT) != -1) {
				if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccured(tokenizer, handler, "array dimension missing");
				}
				expectSymbol(Symbols.SQUARE_BRACES_RIGHT, tokenizer, handler);
			}

			int equateType = visitEquate(tokenizer, handler);
			if (equateType != -1) {
				tokenizer.commit();
				if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccured(tokenizer, handler, "Expression is expected");
				}
				return true;
			} else if (indexes.size() > 0) {
				errorOccured(tokenizer, handler, "not a statement");
			}
		}

		tokenizer.rollback();
		return false;
	}
}
