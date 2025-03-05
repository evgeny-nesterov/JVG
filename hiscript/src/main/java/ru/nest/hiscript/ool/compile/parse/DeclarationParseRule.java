package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeDeclaration;
import ru.nest.hiscript.ool.model.nodes.NodeDeclarations;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import static ru.nest.hiscript.tokenizer.WordType.*;

public class DeclarationParseRule extends ParseRule<NodeDeclarations> {
	private final static DeclarationParseRule instance = new DeclarationParseRule();

	public static DeclarationParseRule getInstance() {
		return instance;
	}

	private DeclarationParseRule() {
	}

	public NodeDeclarations visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		Type baseType = visitType(tokenizer, true, ctx.getEnv());
		if (baseType != null) {
			String varName = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
			if (varName != null) {
				Type cellType = baseType.isArray() ? baseType.cellTypeRoot : baseType;
				int addDimension = visitDimension(tokenizer);
				Type type = Type.getArrayType(baseType, addDimension, ctx.getEnv());

				HiNode initializer = null;
				boolean isField = false;
				if (checkSymbol(tokenizer, SymbolType.SEMICOLON, SymbolType.COMMA) != null) {
					isField = true;
				} else if (visitSymbol(tokenizer, SymbolType.EQUATE) != null) {
					initializer = visitInitializer(tokenizer, cellType, type.getDimension(), ctx);
					if (initializer == null) {
						tokenizer.error("expression expected");
					}
					isField = true;
				} else if (tokenizer.currentToken() == null || checkSymbol(tokenizer, SymbolType.BRACES_RIGHT) != null) {
					expectSymbol(tokenizer, SymbolType.SEMICOLON);
					isField = true;
				}

				if (isField) {
					Modifiers modifiers = annotatedModifiers.getModifiers();
					checkModifiers(tokenizer, modifiers, annotatedModifiers.getToken(), FINAL);

					NodeDeclarations declarations = new NodeDeclarations();
					declarations.add(type, varName, initializer, modifiers, annotatedModifiers.getAnnotations(), tokenizer.getBlockToken(startToken));

					// Search new declarations with the base type
					while (visitSymbol(tokenizer, SymbolType.COMMA) != null) {
						startToken = startToken(tokenizer);
						varName = expectWords(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
						if (varName == null) {
							tokenizer.rollback();
							return null;
						}
						addDimension = visitDimension(tokenizer);
						type = Type.getArrayType(baseType, addDimension, ctx.getEnv());

						initializer = null;
						if (visitSymbol(tokenizer, SymbolType.EQUATE) != null) {
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
		HiNode initializer = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
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

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		Type baseType = visitType(tokenizer, true, ctx.getEnv());
		if (baseType != null) {
			String varName = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
			if (varName != null) {
				Type cellType = baseType.isArray() ? baseType.cellTypeRoot : baseType;
				int addDimension = visitDimension(tokenizer);
				Type type = Type.getArrayType(baseType, addDimension, ctx.getEnv());

				HiNode initializer = null;
				if (initialized) {
					expectSymbol(tokenizer, SymbolType.EQUATE);
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
