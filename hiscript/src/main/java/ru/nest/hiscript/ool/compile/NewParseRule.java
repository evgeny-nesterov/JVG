package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.List;

public class NewParseRule extends ParseRule<HiNode> {
	private final static NewParseRule instance = new NewParseRule();

	public static NewParseRule getInstance() {
		return instance;
	}

	private NewParseRule() {
	}

	@Override
	public HiNode visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(Words.NEW, tokenizer) != null) {
			Type type = visitType(tokenizer, false);
			if (type == null) {
				tokenizer.error("identifier is expected");
			}
			NodeGenerics generics = GenericsParseRule.getInstance().visit(tokenizer, ctx);
			if (generics != null) {
				generics.setSourceType(NodeGeneric.GenericSourceType.classSource);
			}

			int braceType = visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT, Symbols.SQUARE_BRACES_LEFT, Symbols.MASSIVE);
			HiNode node = null;
			switch (braceType) {
				case Symbols.PARENTHESES_LEFT:
					// new Type<generics>(arg1, ...)
					if (type.isPrimitive()) {
						tokenizer.error("'[' expected"); // primitive array expected
					}
					node = visitNewObject(tokenizer, type, generics, ctx, startToken);
					break;

				case Symbols.SQUARE_BRACES_LEFT:
					// new Type[size1][size2]...[]...
					if (generics != null) {
						tokenizer.error("generic array creation", generics.getToken());
					}
					node = visitNewArray(tokenizer, type, ctx);
					break;

				case Symbols.MASSIVE:
					// new Type[]...[]{{...},...}
					if (generics != null) {
						tokenizer.error("generic array creation", generics.getToken());
					}
					node = visitNewArrayValue(tokenizer, type, ctx);
					break;
			}
			return node;
		}
		return null;
	}

	// new <type>(<arguments>) {<body>}
	private HiNode visitNewObject(Tokenizer tokenizer, Type type, NodeGenerics generics, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		HiNode[] arguments = visitArgumentsValues(tokenizer, ctx);

		expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

		if (visitSymbol(tokenizer, Symbols.BRACES_LEFT) != -1) {
			CompileClassContext innerCtx = new CompileClassContext(ctx, ctx.clazz, ctx.type, HiClass.CLASS_TYPE_ANONYMOUS);
			innerCtx.clazz = new HiClass(ctx.getClassLoader(), type, ctx.clazz, ctx.type, null, "", generics, HiClass.CLASS_TYPE_ANONYMOUS, ctx);

			// TODO: do not allow parse constructors. ??? name is empty => constructors will be not found
			ClassParseRule.getInstance().visitContent(tokenizer, innerCtx, null);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			innerCtx.clazz.setToken(tokenizer.getBlockToken(startToken));
			return new NodeConstructor(innerCtx.clazz, type, arguments);
		} else {
			NodeType nodeType = new NodeType(type);
			return new NodeConstructor(nodeType, arguments);
		}
	}

	// new a.b.c.d[<index>]...[<index>] []...[]
	private HiNode visitNewArray(Tokenizer tokenizer, Type type, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		List<HiNode> indexes = new ArrayList<>();

		HiNode index = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (index == null) {
			tokenizer.error("index is expected");
		}
		expectSymbol(tokenizer, Symbols.SQUARE_BRACES_RIGHT);
		indexes.add(index);

		while (visitSymbol(tokenizer, Symbols.SQUARE_BRACES_LEFT) != -1) {
			index = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
			indexes.add(index);
			expectSymbol(tokenizer, Symbols.SQUARE_BRACES_RIGHT);
		}

		while (visitSymbol(tokenizer, Symbols.MASSIVE) != -1) {
			indexes.add(null);
		}

		HiNode[] indexesArray = new HiNode[indexes.size()];
		indexes.toArray(indexesArray);
		return new NodeArray(type, indexesArray);
	}

	// new a.b.c.d[]...[] {{...}, ... ,{...}}
	private HiNode visitNewArrayValue(Tokenizer tokenizer, Type type, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		int dimensions = visitDimension(tokenizer) + 1;

		HiNode value = visitArrayValue(tokenizer, type, dimensions, ctx);
		if (value == null) {
			tokenizer.error("dimension is expected");
		}
		return value;
	}

	public NodeArrayValue visitArrayValue(Tokenizer tokenizer, Type type, int dimensions, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		if (visitSymbol(tokenizer, Symbols.BRACES_LEFT) != -1) {
			List<HiNode> list = new ArrayList<>(1);

			HiNode cell = visitCell(tokenizer, type, dimensions - 1, ctx);
			if (cell != null) {
				list.add(cell);
				while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
					cell = visitCell(tokenizer, type, dimensions - 1, ctx);
					if (cell == null) {
						tokenizer.error("expression is expected");
					}
					list.add(cell);
				}
			}

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			HiNode[] array = new HiNode[list.size()];
			list.toArray(array);
			return new NodeArrayValue(type, dimensions, array);
		}
		return null;
	}

	public HiNode visitCell(Tokenizer tokenizer, Type type, int dimensions, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		HiNode cell = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (cell != null) {
			return cell;
		}
		return visitArrayValue(tokenizer, type, dimensions, ctx);
	}
}
