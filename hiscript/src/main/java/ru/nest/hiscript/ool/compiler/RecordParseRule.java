package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.classes.HiClassRecord;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.List;

public class RecordParseRule extends ParserUtil {
	private final static RecordParseRule instance = new RecordParseRule();

	public static RecordParseRule getInstance() {
		return instance;
	}

	private RecordParseRule() {
	}

	public HiClass visit(Tokenizer tokenizer, CompileContext ctx) throws TokenizerException, ParseException {
		tokenizer.start();

		Modifiers modifiers = visitModifiers(tokenizer);
		if (visitWord(Words.RECORD, tokenizer) != null) {
			tokenizer.commit();
			checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, STATIC);

			String recordName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (recordName == null) {
				throw new ParseException("record name is expected", tokenizer.currentToken());
			}

			expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);

			List<NodeArgument> arguments = new ArrayList<>();
			visitArgumentsDefinitions(tokenizer, arguments, ctx);
			expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

			boolean hasContent = false;
			if (checkSymbol(tokenizer, Symbols.SEMICOLON) != -1) {
				tokenizer.nextToken();
			} else {
				expectSymbol(tokenizer, Symbols.BRACES_LEFT);
				hasContent = true;
			}

			HiClassRecord record = new HiClassRecord(recordName, ctx.classType);
			record.defaultConstructor = new HiConstructor(record, Modifiers.PUBLIC(), arguments, null, null, HiConstructor.BodyConstructorType.NONE);

			ctx.clazz = record;
			ctx.clazz.modifiers = modifiers;

			if (hasContent) {
				expectSymbol(tokenizer, Symbols.BRACES_LEFT);
				visitContent(tokenizer, ctx);
				expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			}
			return ctx.clazz;
		}

		tokenizer.rollback();
		return null;
	}

	public void visitContent(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		// TODO specific content

		ClassParseRule.getInstance().visitContent(tokenizer, properties);
	}
}
