package ru.nest.hiscript.tokenizer;

public class AnnotationTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		if (tokenizer.hasNext() && tokenizer.getCurrent() == '@') {
			int offset = tokenizer.getOffset();
			int line = tokenizer.getLine();
			int lineOffset = tokenizer.getLineOffset();

			tokenizer.next();

			while (tokenizer.hasNext() && (Character.isLetter(tokenizer.getCurrent()) || (tokenizer.getCurrent() >= '0' && tokenizer.getCurrent() <= '9') || tokenizer.getCurrent() == '_')) {
				tokenizer.next();
			}

			String word = tokenizer.getText(offset + 1, tokenizer.getOffset());
			if (word.length() == 0) {
				tokenizer.error("unexpected token", line, offset, 1, lineOffset);
			}
			return new AnnotationWordToken(word, line, offset, tokenizer.getOffset() - offset, lineOffset);
		}
		return null;
	}
}
