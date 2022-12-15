package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public abstract class ParseRule<N extends Node> extends ParserUtil {
	public abstract N visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException;
}
