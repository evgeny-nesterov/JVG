package ru.nest.hiscript.tokenizer;

public class CommentTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		int offset = tokenizer.getOffset();
		int line = tokenizer.getLine();
		int lineOffset = tokenizer.getLineOffset();

		if (tokenizer.getCurrent() == '/') {
			if (tokenizer.hasNext()) {
				tokenizer.next();
				if (tokenizer.getCurrent() == '/') {
					tokenizer.next();
					while (tokenizer.hasNext() && tokenizer.getCurrent() != '\n') {
						tokenizer.next();
					}
					return new CommentToken(line, offset, tokenizer.getOffset() - offset, lineOffset);
				} else if (tokenizer.getCurrent() == '*') {
					tokenizer.next();
					char prev = tokenizer.getCurrent();
					while (tokenizer.hasNext()) {
						if (prev == '*' && tokenizer.getCurrent() == '/') {
							tokenizer.next();
							return new CommentToken(line, offset, tokenizer.getOffset() - offset, lineOffset);
						}
						prev = tokenizer.getCurrent();
						tokenizer.next();
					}

					throw new TokenizerException("end of comment '*/' is expected", tokenizer.getLine(), tokenizer.getOffset() - 1, 1, tokenizer.getLineOffset());
				}
			}
		}

		return null;
	}
}
