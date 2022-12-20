package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeDeclaration;
import ru.nest.hiscript.ool.model.nodes.NodeDeclarations;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class DeclarationParseRule extends ParseRule<NodeDeclarations> implements Words {
	private final static DeclarationParseRule instance = new DeclarationParseRule();

	public static DeclarationParseRule getInstance() {
		return instance;
	}

	private DeclarationParseRule() {
	}

	public NodeDeclarations visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, ParseException {
		tokenizer.start();

		NodeAnnotation[] annotations = AnnotationParseRule.getInstance().visitAnnotations(tokenizer, ctx);
		Modifiers modifiers = visitModifiers(tokenizer);
		Type baseType = visitType(tokenizer, true);
		if (baseType != null) {
			String varName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (varName != null) {
				Type cellType = baseType.isArray() ? baseType.cellTypeRoot : baseType;
				int addDimension = visitDimension(tokenizer);
				Type type = Type.getArrayType(baseType, addDimension);

				HiNode initializer = null;
				boolean isField = false;
				if (checkSymbol(tokenizer, Symbols.SEMICOLON, Symbols.COMMA) != -1) {
					isField = true;
				} else if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
					initializer = visitInitializer(tokenizer, cellType, type.getDimension(), ctx);
					isField = true;
				}

				if (isField) {
					tokenizer.commit();
					checkModifiers(tokenizer, modifiers, FINAL, STATIC);

					NodeDeclarations declarations = new NodeDeclarations();
					declarations.add(type, varName, initializer, modifiers, annotations);

					// Search new declarations with the base type
					while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
						varName = expectWord(Words.NOT_SERVICE, tokenizer);
						addDimension = visitDimension(tokenizer);
						type = Type.getArrayType(baseType, addDimension);

						initializer = null;
						if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
							initializer = expectInitializer(tokenizer, cellType, type.getDimension(), ctx);
						}

						declarations.add(type, varName, initializer, modifiers, annotations);
					}
					return declarations;
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	public HiNode visitInitializer(Tokenizer tokenizer, Type type, int dimensions, CompileClassContext ctx) throws TokenizerException, ParseException {
		HiNode initializer = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (initializer != null) {
			return initializer;
		}

		initializer = NewParseRule.getInstance().visitArrayValue(tokenizer, type, dimensions, ctx);
		if (initializer != null) {
			return initializer;
		}
		return null;
	}

	public HiNode expectInitializer(Tokenizer tokenizer, Type type, int dimensions, CompileClassContext ctx) throws TokenizerException, ParseException {
		HiNode initializer = visitInitializer(tokenizer, type, dimensions, ctx);
		if (initializer == null) {
			throw new ParseException("initializer is expected", tokenizer.currentToken());
		}
		return initializer;
	}

	public NodeDeclaration visitSingle(Tokenizer tokenizer, CompileClassContext ctx, boolean initialized) throws TokenizerException, ParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);

		NodeAnnotation[] annotations = AnnotationParseRule.getInstance().visitAnnotations(tokenizer, ctx);
		Modifiers modifiers = visitModifiers(tokenizer);
		Type baseType = visitType(tokenizer, true);
		if (baseType != null) {
			String varName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (varName != null) {
				Type cellType = baseType.isArray() ? baseType.cellTypeRoot : baseType;
				int addDimension = visitDimension(tokenizer);
				Type type = Type.getArrayType(baseType, addDimension);

				HiNode initializer = null;
				if (initialized) {
					expectSymbol(tokenizer, Symbols.EQUATE);
					initializer = visitInitializer(tokenizer, cellType, type.getDimension(), ctx);
				}

				tokenizer.commit();
				checkModifiers(tokenizer, modifiers);

				NodeDeclaration field = new NodeDeclaration(type, varName, initializer, modifiers, annotations);
				field.setToken(tokenizer.getBlockToken(startToken));
				return field;
			}
		}

		tokenizer.rollback();
		return null;
	}
}
