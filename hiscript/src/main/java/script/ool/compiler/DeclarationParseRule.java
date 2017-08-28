package script.ool.compiler;

import script.ParseException;
import script.ool.model.Modifiers;
import script.ool.model.Node;
import script.ool.model.Type;
import script.ool.model.nodes.NodeDeclaration;
import script.ool.model.nodes.NodeDeclarations;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.Words;

public class DeclarationParseRule extends ParseRule<NodeDeclarations> implements Words {
	private final static DeclarationParseRule instance = new DeclarationParseRule();

	public static DeclarationParseRule getInstance() {
		return instance;
	}

	private DeclarationParseRule() {
	}

	public NodeDeclarations visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();

		Modifiers modifiers = visitModifiers(tokenizer);
		Type baseType = visitType(tokenizer, true);
		if (baseType != null) {
			String varName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (varName != null) {
				Type cellType = baseType.isArray() ? baseType.cellTypeRoot : baseType;
				int addDimension = visitDimension(tokenizer);
				Type type = Type.getArrayType(baseType, addDimension);

				Node initializer = null;
				boolean isField = false;
				if (checkSymbol(tokenizer, Symbols.SEMICOLON, Symbols.COMMA) != -1) {
					isField = true;
				} else if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
					initializer = visitInitializer(tokenizer, cellType, type.getDimension(), properties);
					isField = true;
				}

				if (isField) {
					tokenizer.commit();
					checkModifiers(tokenizer, modifiers, FINAL, STATIC);

					NodeDeclarations declarations = new NodeDeclarations();
					NodeDeclaration field = declarations.add(type, varName, initializer, modifiers);
					properties.addLocalVariable(field);

					// Search new declarations with the base type
					while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
						varName = expectWord(Words.NOT_SERVICE, tokenizer);
						addDimension = visitDimension(tokenizer);
						type = Type.getArrayType(baseType, addDimension);

						initializer = null;
						if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
							initializer = expectInitializer(tokenizer, cellType, type.getDimension(), properties);
						}

						field = declarations.add(type, varName, initializer, modifiers);
						properties.addLocalVariable(field);
					}

					return declarations;
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	public Node visitInitializer(Tokenizer tokenizer, Type type, int dimensions, CompileContext properties) throws TokenizerException, ParseException {
		Node initializer = ExpressionParseRule.getInstance().visit(tokenizer, properties);
		if (initializer != null) {
			return initializer;
		}

		initializer = NewParseRule.getInstance().visitArrayValue(tokenizer, type, dimensions, properties);
		if (initializer != null) {
			return initializer;
		}

		return null;
	}

	public Node expectInitializer(Tokenizer tokenizer, Type type, int dimensions, CompileContext properties) throws TokenizerException, ParseException {
		Node initializer = visitInitializer(tokenizer, type, dimensions, properties);
		if (initializer == null) {
			throw new ParseException("initializer is expected", tokenizer.currentToken());
		}
		return initializer;
	}
}
