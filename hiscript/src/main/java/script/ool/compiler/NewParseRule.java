package script.ool.compiler;

import java.util.ArrayList;
import java.util.List;

import script.ParseException;
import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.Type;
import script.ool.model.nodes.NodeArray;
import script.ool.model.nodes.NodeArrayValue;
import script.ool.model.nodes.NodeConstructor;
import script.ool.model.nodes.NodeType;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.Words;

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
			Type type = visitType(tokenizer, false);
			if (type == null) {
				throw new ParseException("identificator is expected", tokenizer.currentToken());
			}

			int brace_type = visitSymbol(tokenizer, Symbols.PARANTHESIS_LEFT, Symbols.SQUARE_BRACES_LEFT, Symbols.MASSIVE);
			switch (brace_type) {
				case Symbols.PARANTHESIS_LEFT:
					if (type.isPrimitive()) {
						throw new ParseException("'[' expected", tokenizer.currentToken());
					}
					return visitNewObject(tokenizer, type, properties);

				case Symbols.SQUARE_BRACES_LEFT:
					return visitNewArray(tokenizer, type, properties);

				case Symbols.MASSIVE:
					return visitNewArrayValue(tokenizer, type, properties);
			}
		}

		return null;
	}

	// new <type>(<arguments>) {<body>}
	private Node visitNewObject(Tokenizer tokenizer, Type type, CompileContext properties) throws TokenizerException, ParseException {
		Node[] arguments = visitArguments(tokenizer, properties);

		expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);

		if (visitSymbol(tokenizer, Symbols.BRACES_LEFT) != -1) {
			CompileContext innerProperties = new CompileContext(tokenizer, properties, properties.clazz, Clazz.CLASS_TYPE_ANONYMOUS);
			innerProperties.clazz = new Clazz(type, properties.clazz, null, "", Clazz.CLASS_TYPE_ANONYMOUS);

			// TODO: do not allow parse constructors. ??? name is empty => ocnstructors will be not found
			ClassParseRule.getInstance().visitContent(tokenizer, innerProperties);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			return new NodeConstructor(innerProperties.clazz, arguments);
		} else {
			NodeType nodeType = new NodeType(type);
			return new NodeConstructor(nodeType, arguments);
		}
	}

	// new a.b.c.d[<index>]...[<index>] []...[]
	private Node visitNewArray(Tokenizer tokenizer, Type type, CompileContext properties) throws TokenizerException, ParseException {
		List<Node> indexes = new ArrayList<Node>();

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
			ArrayList<Node> list = new ArrayList<Node>(1);

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

		cell = visitArrayValue(tokenizer, type, dimensions, properties);
		if (cell != null) {
			return cell;
		}

		return null;
	}
}
