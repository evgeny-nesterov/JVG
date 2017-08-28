package script.ool.compiler;

import script.ParseException;
import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.nodes.EmptyNode;
import script.ool.model.nodes.NodeClass;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class StatementParseRule extends ParseRule<Node> {
	private final static StatementParseRule instance = new StatementParseRule();

	public static StatementParseRule getInstance() {
		return instance;
	}

	private StatementParseRule() {
	}

	/**
	 * Available statements: class if while do-while for switch label return try declaration break continue block expression invocation,
	 * assignment, new object throw
	 */
	public Node visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitSymbol(tokenizer, SymbolToken.SEMICOLON) != -1) {
			return EmptyNode.getInstance();
		}

		// local class / interface
		CompileContext localProperties = new CompileContext(tokenizer, properties, properties.clazz, Clazz.CLASS_TYPE_LOCAL);
		Clazz clazz = ClassParseRule.getInstance().visit(tokenizer, localProperties);
		if (clazz == null) {
			clazz = InterfaceParseRule.getInstance().visit(tokenizer, localProperties);
		}
		if (clazz != null) {
			// check modifiers
			if (clazz.isStatic()) {
				throw new ParseException("Illegal modifier for the local class " + clazz.fullName + "; only abstract or final is permitted", tokenizer.currentToken());
			}

			properties.addLocalClass(clazz);
			return new NodeClass(clazz);
		}

		Node node = null;
		if ((node = IfParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = WhileParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = DoWhileParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = ForParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = SwitchParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = SynchronizedParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = LabelParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = ReturnParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = TryParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = ThrowParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = BreakParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = ContinueParseRule.getInstance().visit(tokenizer, properties)) != null) {
			return node;
		}

		if ((node = DeclarationParseRule.getInstance().visit(tokenizer, properties)) != null) {
			expectSymbol(tokenizer, SymbolToken.SEMICOLON);
			return node;
		}

		if (visitSymbol(tokenizer, SymbolToken.BRACES_LEFT) != -1) {
			node = BlockParseRule.getInstance().visit(tokenizer, properties);
			expectSymbol(tokenizer, SymbolToken.BRACES_RIGHT);
			if (node != null) {
				return node;
			} else {
				return EmptyNode.getInstance();
			}
		}

		// expression has to be parsed at the end
		if ((node = ExpressionParseRule.getInstance().visit(tokenizer, properties)) != null) {
			expectSymbol(tokenizer, SymbolToken.SEMICOLON);
			return node;
		}

		return null;
	}
}
