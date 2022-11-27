package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.AssignmentsNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class AssignmentsParseRule extends ParseRule<AssignmentsNode> {
	private final static AssignmentsParseRule instance = new AssignmentsParseRule();

	public static AssignmentsParseRule getInstance() {
		return instance;
	}

	private AssignmentsParseRule() {
	}

	@Override
	public AssignmentsNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		Node assignment = visitStatement(tokenizer);
		if (assignment != null) {
			AssignmentsNode node = new AssignmentsNode();
			node.addAssignment(assignment);

			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				assignment = visitStatement(tokenizer);
				if (assignment == null) {
					throw new ParseException("statement is expected", tokenizer.currentToken());
				}
				node.addAssignment(assignment);
			}

			return node;
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		try {
			if (visitStatement(tokenizer, handler)) {
				while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
					if (!visitStatement(tokenizer, handler)) {
						errorOccurred(tokenizer, handler, "statement is expected");
					}
				}

				return true;
			}
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}

		return false;
	}

	private Node visitStatement(Tokenizer tokenizer) throws TokenizerException, ParseException {
		Node node = AssignmentParseRule.getInstance().visit(tokenizer);
		if (node != null) {
			return node;
		}

		node = IncrementParseRule.getInstance().visit(tokenizer);
		if (node != null) {
			return node;
		}

		node = InvocationParseRule.getInstance().visit(tokenizer);
		if (node != null) {
			return node;
		}

		return null;
	}

	private boolean visitStatement(Tokenizer tokenizer, CompileHandler handler) {
		if (AssignmentParseRule.getInstance().visit(tokenizer, handler)) {
			return true;
		}

		if (IncrementParseRule.getInstance().visit(tokenizer, handler)) {
			return true;
		}

		if (InvocationParseRule.getInstance().visit(tokenizer, handler)) {
			return true;
		}

		return false;
	}
}
