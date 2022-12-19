package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class EnumParseRule extends ParserUtil {
	private final static EnumParseRule instance = new EnumParseRule();

	public static EnumParseRule getInstance() {
		return instance;
	}

	private EnumParseRule() {
	}

	public HiClass visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		tokenizer.start();

		NodeAnnotation[] annotations = AnnotationParseRule.getInstance().visitAnnotations(tokenizer, ctx);
		Modifiers modifiers = visitModifiers(tokenizer);
		if (visitWord(Words.ENUM, tokenizer) != null) {
			tokenizer.commit();
			checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, STATIC);
			modifiers.setFinal(true);
			modifiers.setStatic(true);

			String enumName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (enumName == null) {
				throw new ParseException("enum name is expected", tokenizer.currentToken());
			}

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			ctx.clazz = new HiClassEnum(ctx.getClassLoader(), enumName, ctx.classType);
			ctx.clazz.modifiers = modifiers;
			ctx.clazz.annotations = annotations;

			visitContent(tokenizer, ctx);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			return ctx.clazz;
		}

		tokenizer.rollback();
		return null;
	}

	public void visitContent(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		String enumName = visitWord(Words.NOT_SERVICE, tokenizer);
		if (enumName != null) {
			int ordinal = 0;
			while (true) {
				HiNode[] args = null;
				if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
					args = visitArgumentsValues(tokenizer, ctx);
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
				}
				ctx.addEnum(new HiEnumValue(enumName, ordinal++, args));

				if (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				} else if (visitSymbol(tokenizer, Symbols.SEMICOLON) != -1 || checkSymbol(tokenizer, Symbols.BRACES_RIGHT) != -1) {
					break;
				} else {
					throw new ParseException("expected ',', '(' or ';'", tokenizer.currentToken());
				}

				enumName = expectWord(Words.NOT_SERVICE, tokenizer);
			}
		}

		ClassParseRule.getInstance().visitContent(tokenizer, ctx, null);
	}
}
