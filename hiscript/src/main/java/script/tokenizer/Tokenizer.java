package script.tokenizer;

import java.util.ArrayList;
import java.util.HashMap;

public class Tokenizer {
	public Tokenizer(String s) {
		this.s = s;
		len = s.length();
	}

	private int len;

	private String s;

	public String getText(int beginIndex, int endIndex) {
		return s.substring(beginIndex, endIndex);
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

	protected char look_forward() {
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
		while (current == ' ' || current == '\n' || current == '\t' || current == '\r') {
			next();
		}
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

				currentToken = searchToken();
				if (currentToken == null) {
					throw new TokenizerException("unexpected character", curLine, curOffset, 1, curLineOffset);
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

	private ArrayList<Token> buffer = new ArrayList<Token>();

	private ArrayList<Integer> startOffsets = new ArrayList<Integer>();

	private ArrayList<Token> startPrevTokens = new ArrayList<Token>();

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
		// but to emprove performence it's better clear buffer fully.
		// We can do that when cursor position equals buffer length.
	}

	public void rollback() {
		tokenOffset = startOffsets.remove(startOffsets.size() - 1);
		currentToken = startPrevTokens.remove(startPrevTokens.size() - 1);
	}

	private ArrayList<TokenVisitor> visitors = new ArrayList<TokenVisitor>();

	public void addVisitor(TokenVisitor visitor) {
		visitors.add(visitor);
	}

	private Token currentToken = null;

	public Token currentToken() {
		return currentToken;
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

	public static Tokenizer getDefaultTokenizer(String s) {
		Tokenizer t = new Tokenizer(s);
		t.addVisitor(new StringTokenVisitor());
		t.addVisitor(new CharTokenVisitor());
		t.addVisitor(new CommentTokenVisitor());
		t.addVisitor(new WordTokenVisitor());
		t.addVisitor(new NumberTokenVisitor());
		t.addVisitor(new SymbolTokenVisitor());
		return t;
	}

	public static HashMap<?, ?> properties = new HashMap<Object, Object>();

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
}
