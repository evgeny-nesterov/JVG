package script.ool.compiler;

import script.ParseException;
import script.ool.model.Node;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public abstract class ParseRule<N extends Node> extends ParserUtil {
	// TODO: visit(Tokenizer tokenizer, Context properties)
	public abstract N visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException;
}
