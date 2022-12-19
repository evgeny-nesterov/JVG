package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public abstract class ParseRule<N extends HiNode> extends ParserUtil {
	public abstract N visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException;
}
