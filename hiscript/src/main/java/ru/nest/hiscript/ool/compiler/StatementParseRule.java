package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeClass;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

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
	@Override
	public Node visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		if (visitSymbol(tokenizer, Symbols.SEMICOLON) != -1) {
			return EmptyNode.getInstance();
		}

		// local class / interface
		HiClass clazz = ClassParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, ctx.clazz, HiClass.CLASS_TYPE_LOCAL));
		if (clazz == null) {
			clazz = InterfaceParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, ctx.clazz, HiClass.CLASS_TYPE_LOCAL));
		}
		if (clazz == null) {
			clazz = EnumParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, ctx.clazz, HiClass.CLASS_TYPE_LOCAL));
		}
		if (clazz == null) {
			clazz = RecordParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, ctx.clazz, HiClass.CLASS_TYPE_LOCAL));
		}
		if (clazz != null) {
			return new NodeClass(clazz);
		}

		Node node;
		if ((node = IfParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = WhileParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = DoWhileParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		// before ForParseRule
		if ((node = ForIteratorParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = ForParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = SwitchParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = SynchronizedParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = LabelParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = ReturnParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = TryParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = ThrowParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = BreakParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = ContinueParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			return node;
		}

		if ((node = DeclarationParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return node;
		}

		if ((node = AssertParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return node;
		}

		if (visitSymbol(tokenizer, Symbols.BRACES_LEFT) != -1) {
			node = BlockParseRule.getInstance().visit(tokenizer, ctx);
			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			if (node != null) {
				return node;
			} else {
				return EmptyNode.getInstance();
			}
		}

		// expression has to be parsed at the end
		if ((node = ExpressionParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return node;
		}
		return null;
	}
}
