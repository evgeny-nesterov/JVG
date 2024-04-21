package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeClass;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class StatementParseRule extends ParseRule<HiNode> {
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
	public HiNode visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		int emptyCount = 0;
		while (visitSymbol(tokenizer, Symbols.SEMICOLON) != -1) {
			emptyCount++;
		}
		if (emptyCount > 0) {
			return EmptyNode.getInstance();
		}

		// local class / interface
		HiClass clazz = ClassParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, ctx.clazz, ctx.type, HiClass.CLASS_TYPE_LOCAL));
		if (clazz == null) {
			clazz = EnumParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, ctx.clazz, ctx.type, HiClass.CLASS_TYPE_LOCAL));
		}
		if (clazz == null) {
			clazz = RecordParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, ctx.clazz, ctx.type, HiClass.CLASS_TYPE_LOCAL));
		}
		if (clazz == null) {
			clazz = AnnotationInterfaceParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, ctx.clazz, ctx.type, HiClass.CLASS_TYPE_LOCAL));
		}
		if (clazz != null) {
			return new NodeClass(clazz);
		}

		HiNode node;
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
			return node;
		}

		if ((node = AssertParseRule.getInstance().visit(tokenizer, ctx)) != null) {
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
			node.setStatement(true);
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return node;
		}
		return null;
	}
}
