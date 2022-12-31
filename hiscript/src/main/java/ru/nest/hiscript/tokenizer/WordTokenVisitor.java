package ru.nest.hiscript.tokenizer;

public class WordTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) {
		if (tokenizer.hasNext() && isWordSymbol(tokenizer.getCurrent())) {
			int offset = tokenizer.getOffset();
			int line = tokenizer.getLine();
			int lineOffset = tokenizer.getLineOffset();

			tokenizer.next();

			while (tokenizer.hasNext() && (isWordSymbol(tokenizer.getCurrent()) || (tokenizer.getCurrent() >= '0' && tokenizer.getCurrent() <= '9'))) {
				tokenizer.next();
			}

			String word = tokenizer.getText(offset, tokenizer.getOffset());
			return new WordToken(word, line, offset, tokenizer.getOffset() - offset, lineOffset);
		}
		return null;
	}

	private boolean isWordSymbol(char c) {
		return Character.isLetter(c) || c == '_' || c == '$';
	}
}
