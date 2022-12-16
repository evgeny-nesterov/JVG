package ru.nest.hiscript.tokenizer;

public class WordTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		if (tokenizer.hasNext() && Character.isLetter(tokenizer.getCurrent())) {
			int offset = tokenizer.getOffset();
			int line = tokenizer.getLine();
			int lineOffset = tokenizer.getLineOffset();

			tokenizer.next();

			while (tokenizer.hasNext() && (Character.isLetter(tokenizer.getCurrent()) || (tokenizer.getCurrent() >= '0' && tokenizer.getCurrent() <= '9') || tokenizer.getCurrent() == '_')) {
				tokenizer.next();
			}

			String word = tokenizer.getText(offset, tokenizer.getOffset());
			return new WordToken(word, line, offset, tokenizer.getOffset() - offset, lineOffset);
		}
		return null;
	}
}
