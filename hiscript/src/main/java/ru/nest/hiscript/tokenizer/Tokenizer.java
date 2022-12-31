package ru.nest.hiscript.tokenizer;

import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tokenizer {
	public Tokenizer(String s) {
		this.s = s;
		len = s.length();
	}

	private int len;

	private String s;

	private ValidationInfo validationInfo;

	public String getText(int beginIndex, int endIndex) {
		return s.substring(beginIndex, endIndex);
	}

	public String getText(Token token) {
		if (token == null) {
			return null;
		}
		return s.substring(token.getOffset(), token.getOffset() + token.getLength());
	}

	public String getTokenLineText(Token token) {
		if (token == null) {
			return null;
		}
		int startOffset = token.getOffset() - token.getLineOffset();
		int endOffset = s.indexOf('\n', token.getOffset());
		if (endOffset == -1) {
			endOffset = s.length();
		}
		return getText(startOffset, endOffset);
	}

	public String getText(int beginIndex, int endIndex, char except) {
		StringBuilder text = new StringBuilder(endIndex - beginIndex);
		for (int i = beginIndex; i < endIndex; i++) {
			char c = s.charAt(i);
			if (c != except) {
				text.append(c);
			}
		}
		return text.toString();
	}

	private boolean end = false;

	public boolean hasNext() {
		return !end;
	}

	private int line = 0;

	public int getLine() {
		return line;
	}

	private int lineOffset = -1;

	public int getLineOffset() {
		return lineOffset;
	}

	private char current;

	protected char getCurrent() {
		return current;
	}

	private int offset = -1;

	protected int getOffset() {
		return offset;
	}

	protected void next() {
		if (offset < len) {
			offset++;
			lineOffset++;
			end = offset == len;
			if (!end) {
				current = s.charAt(offset);
				if (current == '\n') {
					line++;
					lineOffset = -1;
				}
			} else {
				current = 0;
			}
		}
	}

	protected char lookForward() {
		if (offset < len - 1) {
			return s.charAt(offset + 1);
		} else {
			return 0;
		}
	}

	protected void rollback(int offset, int line, int lineOffset) {
		this.offset = offset - 1;
		next();
		this.line = line;
		this.lineOffset = lineOffset;
	}

	protected void skipWhitespaces() {
		while (isWhiteSpace(current) || current == '\n') {
			next();
		}
	}

	protected void skipLineWhitespaces() {
		while (isWhiteSpace(current)) {
			next();
		}
	}

	public static boolean isWhiteSpace(char c) {
		return c == ' ' || c == '\t' || c == '\r';
	}

	public Token nextToken() throws TokenizerException {
		if (offset == -1) {
			next();
		}

		// trim buffer when transaction is finished, ie after end commit or
		// rollback
		if (startOffsets.size() == 0 && tokenOffset == buffer.size()) {
			tokenOffset = 0;
			buffer.clear();
		}

		// return cached token
		if (tokenOffset < buffer.size()) {
			currentToken = buffer.get(tokenOffset);
			tokenOffset++;
			return currentToken;
		}

		if (!end) {
			skipWhitespaces();
			if (!end) {
				int curLine = line;
				int curOffset = offset;
				int curLineOffset = lineOffset;

				boolean notEOF = currentToken != null;
				currentToken = searchToken();
				if (currentToken == null) {
					if (!notEOF) {
						error("unexpected character", curLine, curOffset, 1, curLineOffset);
					}
					return null;
				}

				if (startOffsets.size() > 0) {
					// cache token if transaction is started
					buffer.add(currentToken);

					// set cursor position in buffer to its length
					// so after commit in this case buffer will be cleared
					tokenOffset = buffer.size();
				}
			} else {
				currentToken = null;
			}
		} else {
			currentToken = null;
		}
		return currentToken;
	}

	private List<Token> buffer = new ArrayList<>();

	private List<Integer> startOffsets = new ArrayList<>();

	private List<Token> startPrevTokens = new ArrayList<>();

	private int tokenOffset = 0;

	public void start() {
		startOffsets.add(tokenOffset);
		startPrevTokens.add(currentToken);
	}

	public void commit() {
		startOffsets.remove(startOffsets.size() - 1);

		// clear old info
		startPrevTokens.remove(startPrevTokens.size() - 1);

		// We may clear buffer up to position equals startOffset
		// but to improve performance it's better clear buffer fully.
		// We can do that when cursor position equals buffer length.
	}

	public void rollback() {
		tokenOffset = startOffsets.remove(startOffsets.size() - 1);
		currentToken = startPrevTokens.remove(startPrevTokens.size() - 1);
	}

	private List<TokenVisitor> visitors = new ArrayList<>();

	public void addVisitor(TokenVisitor visitor) {
		visitors.add(visitor);
	}

	private Token currentToken = null;

	public Token currentToken() {
		return currentToken;
	}

	public Token getBlockToken(Token startToken) {
		if (startToken == null) {
			return null;
		} else if (currentToken != null) {
			return new Token(startToken, currentToken);
		} else {
			return new Token(startToken.getLine(), startToken.getOffset(), len - startToken.getOffset(), startToken.getLineOffset());
		}
	}

	private Token searchToken() throws TokenizerException {
		int offset = this.offset;
		int line = this.line;
		int lineOffset = this.lineOffset;

		int size = visitors.size();
		for (int i = 0; i < size; i++) {
			TokenVisitor type = visitors.get(i);

			Token token = type.getToken(this);
			if (token != null) {
				return token;
			}

			rollback(offset, line, lineOffset);
		}
		return null;
	}

	public static Tokenizer getDefaultTokenizer(InputStream is) throws IOException {
		return getDefaultTokenizer(ParserUtil.readString(is));
	}

	public static Tokenizer getDefaultTokenizer(Reader r) throws IOException {
		return getDefaultTokenizer(ParserUtil.readString(r));
	}

	public static Tokenizer getDefaultTokenizer(String s) {
		Tokenizer t = new Tokenizer(s);
		t.addVisitor(new StringTokenVisitor());
		t.addVisitor(new CharTokenVisitor());
		t.addVisitor(new CommentTokenVisitor());
		t.addVisitor(new AnnotationTokenVisitor());
		t.addVisitor(new WordTokenVisitor());
		t.addVisitor(new NumberTokenVisitor());
		t.addVisitor(new SymbolTokenVisitor());
		return t;
	}

	public static Map<?, ?> properties = new HashMap<>();

	public static void main(String[] args) {
		String s = "'\\333'\n\n \"12\\\"3\"";

		Tokenizer t = getDefaultTokenizer(s);
		try {
			while (t.hasNext()) {
				Token token = t.nextToken();
				System.out.println(token);
			}
		} catch (TokenizerException exc) {
			exc.printStackTrace();
		}
	}

	public ValidationInfo getValidationInfo() {
		return validationInfo;
	}

	public void setValidationInfo(ValidationInfo validationInfo) {
		this.validationInfo = validationInfo;
	}

	public void error(String message) throws TokenizerException {
		error(message, currentToken());
	}

	public void error(String message, int line, int offset, int length, int lineOffset) throws TokenizerException {
		error(message, new Token(line, offset, length, lineOffset));
	}

	public void error(String message, Token token) throws TokenizerException {
		if (token == null && offset == len) {
			token = new Token(line, offset - 1, 1, lineOffset);
		}
		if (validationInfo != null) {
			validationInfo.error(message, token);
		} else {
			throw new TokenizerException(message, token);
		}
	}

	public int indexOf(Token bounds, String text) {
		int index = s.indexOf(text, bounds.getOffset());
		return index < bounds.getOffset() + bounds.getLength() ? index : -1;
	}

	public int getLinesCount(int start, int end) {
		int linesCount = 1;
		for (int i = start; i < s.length() && i < end; i++) {
			if (s.charAt(i) == '\n') {
				linesCount++;
			}
		}
		return linesCount;
	}

	public int getLineOffset(int offset) {
		int lineOffset = 0;
		for (int i = offset; i > 0; i--) {
			if (s.charAt(i) == '\n') {
				break;
			} else {
				lineOffset++;
			}
		}
		return lineOffset;
	}
}
