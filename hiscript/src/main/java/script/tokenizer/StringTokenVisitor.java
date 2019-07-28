package script.tokenizer;

public class StringTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		if (tokenizer.getCurrent() == '"') {
			tokenizer.next();
			StringBuilder buf = new StringBuilder();

			int offset = tokenizer.getOffset();
			int line = tokenizer.getLine();
			int lineOffset = tokenizer.getLineOffset();

			while (tokenizer.hasNext()) {
				if (tokenizer.getCurrent() == '"') {
					tokenizer.next();
					return new StringToken(buf.toString(), line, offset, tokenizer.getOffset() - offset, lineOffset);
				}

				char c = CharTokenVisitor.readCharacter(tokenizer);
				buf.append(c);
			}

			throw new TokenizerException("'\"' is expected", tokenizer.getLine(), tokenizer.getOffset() - 1, 1, tokenizer.getLineOffset());
		}

		return null;
	}
}
