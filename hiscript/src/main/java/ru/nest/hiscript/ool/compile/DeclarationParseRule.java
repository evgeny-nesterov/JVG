package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeDeclaration;
import ru.nest.hiscript.ool.model.nodes.NodeDeclarations;
import ru.nest.hiscript.tokenizer.SymbolToken;
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

	public NodeDeclarations visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx);
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
				} else if (tokenizer.currentToken() == null || checkSymbol(tokenizer, SymbolToken.BRACES_RIGHT) != -1) {
					expectSymbol(tokenizer, Symbols.SEMICOLON);
					isField = true;
				}

				if (isField) {
					Modifiers modifiers = annotatedModifiers.getModifiers();
					checkModifiers(tokenizer, modifiers, annotatedModifiers.getToken(), FINAL);

					NodeDeclarations declarations = new NodeDeclarations();
					declarations.add(type, varName, initializer, modifiers, annotatedModifiers.getAnnotations(), tokenizer.getBlockToken(startToken));

					// Search new declarations with the base type
					while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
						startToken = startToken(tokenizer);
						varName = expectWord(Words.NOT_SERVICE, tokenizer);
						if (varName == null) {
							tokenizer.rollback();
							return null;
						}
						addDimension = visitDimension(tokenizer);
						type = Type.getArrayType(baseType, addDimension);

						initializer = null;
						if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
							initializer = expectInitializer(tokenizer, cellType, type.getDimension(), ctx);
						}

						declarations.add(type, varName, initializer, modifiers, annotatedModifiers.getAnnotations(), tokenizer.getBlockToken(startToken));
					}
					tokenizer.commit();
					return declarations;
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	public HiNode visitInitializer(Tokenizer tokenizer, Type type, int dimensions, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
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

	public HiNode expectInitializer(Tokenizer tokenizer, Type type, int dimensions, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		HiNode initializer = visitInitializer(tokenizer, type, dimensions, ctx);
		if (initializer == null) {
			tokenizer.error("initializer is expected");
		}
		return initializer;
	}

	public NodeDeclaration visitSingle(Tokenizer tokenizer, CompileClassContext ctx, boolean initialized) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx);
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
				checkModifiers(tokenizer, annotatedModifiers.getModifiers(), annotatedModifiers.getToken());

				NodeDeclaration field = new NodeDeclaration(type, varName, initializer, annotatedModifiers.getModifiers(), annotatedModifiers.getAnnotations());
				field.setToken(tokenizer.getBlockToken(startToken));
				return field;
			}
		}

		tokenizer.rollback();
		return null;
	}
}
