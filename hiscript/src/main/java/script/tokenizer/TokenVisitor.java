package script.tokenizer;

public interface TokenVisitor {
	public Token getToken(Tokenizer tokenizer) throws TokenizerException;
}
