package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.List;

public class NewParseRule extends ParseRule<Node> {
	private final static NewParseRule instance = new NewParseRule();

	public static NewParseRule getInstance() {
		return instance;
	}

	private NewParseRule() {
	}

	@Override
	public Node visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.NEW, tokenizer) != null) {
			Token startToken = tokenizer.currentToken();
			Type type = visitType(tokenizer, false);
			if (type == null) {
				throw new ParseException("identifier is expected", tokenizer.currentToken());
			}

			int brace_type = visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT, Symbols.SQUARE_BRACES_LEFT, Symbols.MASSIVE);
			Node node = null;
			switch (brace_type) {
				case Symbols.PARENTHESES_LEFT:
					if (type.isPrimitive()) {
						throw new ParseException("'[' expected", tokenizer.currentToken());
					}
					node = visitNewObject(tokenizer, type, properties);
					break;

				case Symbols.SQUARE_BRACES_LEFT:
					node = visitNewArray(tokenizer, type, properties);
					break;

				case Symbols.MASSIVE:
					node = visitNewArrayValue(tokenizer, type, properties);
					break;
			}
			if (node != null) {
				node.setToken(tokenizer.getBlockToken(startToken));
				return node;
			}
		}
		return null;
	}

	// new <type>(<arguments>) {<body>}
	private Node visitNewObject(Tokenizer tokenizer, Type type, CompileContext ctx) throws TokenizerException, ParseException {
		Node[] arguments = visitArgumentsValues(tokenizer, ctx);

		expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

		if (visitSymbol(tokenizer, Symbols.BRACES_LEFT) != -1) {
			CompileContext innerProperties = new CompileContext(ctx, ctx.clazz, HiClass.CLASS_TYPE_ANONYMOUS);
			innerProperties.clazz = new HiClass(type, ctx.clazz, null, "", HiClass.CLASS_TYPE_ANONYMOUS);

			// TODO: do not allow parse constructors. ??? name is empty => constructors will be not found
			ClassParseRule.getInstance().visitContent(tokenizer, innerProperties, null);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			return new NodeConstructor(innerProperties.clazz, arguments);
		} else {
			NodeType nodeType = new NodeType(type);
			return new NodeConstructor(nodeType, arguments);
		}
	}

	// new a.b.c.d[<index>]...[<index>] []...[]
	private Node visitNewArray(Tokenizer tokenizer, Type type, CompileContext properties) throws TokenizerException, ParseException {
		List<Node> indexes = new ArrayList<>();

		Node index = ExpressionParseRule.getInstance().visit(tokenizer, properties);
		if (index == null) {
			throw new ParseException("index is expected", tokenizer.currentToken());
		}
		expectSymbol(tokenizer, Symbols.SQUARE_BRACES_RIGHT);
		indexes.add(index);

		while (visitSymbol(tokenizer, Symbols.SQUARE_BRACES_LEFT) != -1) {
			index = ExpressionParseRule.getInstance().visit(tokenizer, properties);
			indexes.add(index);
			expectSymbol(tokenizer, Symbols.SQUARE_BRACES_RIGHT);
		}

		while (visitSymbol(tokenizer, Symbols.MASSIVE) != -1) {
			indexes.add(null);
		}

		Node[] indexesArray = new Node[indexes.size()];
		indexes.toArray(indexesArray);

		return new NodeArray(type, indexesArray);
	}

	// new a.b.c.d[]...[] {{...}, ... ,{...}}
	private Node visitNewArrayValue(Tokenizer tokenizer, Type type, CompileContext properties) throws TokenizerException, ParseException {
		int dimensions = visitDimension(tokenizer) + 1;

		Node value = visitArrayValue(tokenizer, type, dimensions, properties);
		if (value == null) {
			throw new ParseException("dimension is expected", tokenizer.currentToken());
		}
		return value;
	}

	public NodeArrayValue visitArrayValue(Tokenizer tokenizer, Type type, int dimensions, CompileContext properties) throws TokenizerException, ParseException {
		if (visitSymbol(tokenizer, Symbols.BRACES_LEFT) != -1) {
			ArrayList<Node> list = new ArrayList<>(1);

			Node cell = visitCell(tokenizer, type, dimensions - 1, properties);
			if (cell != null) {
				list.add(cell);
				while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
					cell = visitCell(tokenizer, type, dimensions - 1, properties);
					if (cell == null) {
						throw new ParseException("expression is expected", tokenizer.currentToken());
					}
					list.add(cell);
				}
			}

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			Node[] array = new Node[list.size()];
			list.toArray(array);
			return new NodeArrayValue(type, dimensions, array);
		}
		return null;
	}

	public Node visitCell(Tokenizer tokenizer, Type type, int dimensions, CompileContext properties) throws TokenizerException, ParseException {
		Node cell = ExpressionParseRule.getInstance().visit(tokenizer, properties);
		if (cell != null) {
			return cell;
		}
		return visitArrayValue(tokenizer, type, dimensions, properties);
	}
}
