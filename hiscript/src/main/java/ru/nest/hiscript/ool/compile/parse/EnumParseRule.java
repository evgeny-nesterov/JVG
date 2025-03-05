package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import static ru.nest.hiscript.tokenizer.WordType.*;

public class EnumParseRule extends ParserUtil {
	private final static EnumParseRule instance = new EnumParseRule();

	public static EnumParseRule getInstance() {
		return instance;
	}

	private EnumParseRule() {
	}

	public HiClass visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		if (visitWord(ENUM, tokenizer) != null) {
			tokenizer.commit();
			Modifiers.Changeable modifiers = annotatedModifiers.getModifiers().change();
			checkModifiers(tokenizer, modifiers, annotatedModifiers.getToken(), PUBLIC, PROTECTED, PRIVATE, STATIC);
			modifiers.setFinal(true);
			modifiers.setStatic(true);

			String enumName = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
			if (enumName == null) {
				enumName = "";
				tokenizer.error("enum name is expected");
			}

			expectSymbol(tokenizer, SymbolType.BRACES_LEFT);

			ctx.clazz = new HiClassEnum(ctx.getClassLoader(), enumName, ctx.classLocationType);
			ctx.clazz.modifiers = modifiers;
			ctx.clazz.annotations = annotatedModifiers.getAnnotations();

			visitContent(tokenizer, ctx);

			expectSymbol(tokenizer, SymbolType.BRACES_RIGHT);
			return ctx.clazz;
		}

		tokenizer.rollback();
		return null;
	}

	public void visitContent(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Token enumValueToken = startToken(tokenizer);
		String enumName = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
		if (enumName != null) {
			int ordinal = 0;
			int errorPos = 0;
			while (true) {
				HiNode[] args = null;
				if (visitSymbol(tokenizer, SymbolType.PARENTHESES_LEFT) != null) {
					args = visitArgumentsValues(tokenizer, ctx);
					expectSymbol(tokenizer, SymbolType.PARENTHESES_RIGHT);
				}
				ctx.addEnum(new HiEnumValue(enumName, ordinal++, args, enumValueToken));

				if (visitSymbol(tokenizer, SymbolType.COMMA) != null) {
				} else if (visitSymbol(tokenizer, SymbolType.SEMICOLON) != null || checkSymbol(tokenizer, SymbolType.BRACES_RIGHT) != null) {
					break;
				} else {
					if (errorPos == tokenizer.currentToken().getOffset()) {
						break;
					}
					tokenizer.error("expected ',', '(' or ';'");
					errorPos = tokenizer.currentToken().getOffset();
				}

				enumValueToken = startToken(tokenizer);
				enumName = expectWords(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
				if (enumName == null) {
					enumName = "";
				}
			}
		}

		ClassParseRule.getInstance().visitContent(tokenizer, ctx, null);
	}
}
