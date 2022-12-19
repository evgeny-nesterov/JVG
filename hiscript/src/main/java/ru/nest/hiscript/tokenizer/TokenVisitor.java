package ru.nest.hiscript.tokenizer;

public interface TokenVisitor {
	Token getToken(Tokenizer tokenizer) throws TokenizerException;
}
