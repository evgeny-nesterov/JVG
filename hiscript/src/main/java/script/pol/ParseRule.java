package script.pol;

import script.ParseException;
import script.pol.model.ByteNode;
import script.pol.model.CharacterNode;
import script.pol.model.DoubleNode;
import script.pol.model.FloatNode;
import script.pol.model.IntNode;
import script.pol.model.LongNode;
import script.pol.model.Node;
import script.pol.model.Operations;
import script.pol.model.ShortNode;
import script.pol.model.StringNode;
import script.pol.model.Types;
import script.pol.model.VariableNode;
import script.tokenizer.ByteToken;
import script.tokenizer.CharToken;
import script.tokenizer.CommentToken;
import script.tokenizer.DoubleToken;
import script.tokenizer.FloatToken;
import script.tokenizer.IntToken;
import script.tokenizer.LongToken;
import script.tokenizer.ShortToken;
import script.tokenizer.StringToken;
import script.tokenizer.SymbolToken;
import script.tokenizer.Token;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public abstract class ParseRule<N extends Node> {
	protected void skipComments(Tokenizer tokenizer) throws TokenizerException {
		while (tokenizer.currentToken() instanceof CommentToken) {
			tokenizer.nextToken();
		}
	}

	protected String visitWord(int type, Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof WordToken) {
			WordToken wordToken = (WordToken) currentToken;
			if (wordToken.getType() == type) {
				tokenizer.nextToken();
				return wordToken.getWord();
			}
		}

		return null;
	}

	protected String visitWord(int type, Tokenizer tokenizer, CompileHandler handler) {
		try {
			skipComments(tokenizer);

			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof WordToken) {
				WordToken wordToken = (WordToken) currentToken;
				if (wordToken.getType() == type) {
					tokenizer.nextToken();
					return wordToken.getWord();
				}
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return null;
	}

	protected int visitWords(Tokenizer tokenizer, int... types) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof WordToken) {
			WordToken wordToken = (WordToken) currentToken;

			for (int type : types) {
				if (wordToken.getType() == type) {
					tokenizer.nextToken();
					return type;
				}
			}
		}

		return -1;
	}

	protected int visitWords(Tokenizer tokenizer, CompileHandler handler, int... types) {
		try {
			skipComments(tokenizer);

			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof WordToken) {
				WordToken wordToken = (WordToken) currentToken;

				for (int type : types) {
					if (wordToken.getType() == type) {
						tokenizer.nextToken();
						return type;
					}
				}
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return -1;
	}

	protected int visitType(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof WordToken) {
			WordToken wordToken = (WordToken) currentToken;
			if (Types.isType(wordToken.getType())) {
				tokenizer.nextToken();
				return wordToken.getType();
			}
		}

		return -1;
	}

	protected int visitType(Tokenizer tokenizer, CompileHandler handler) {
		try {
			skipComments(tokenizer);

			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof WordToken) {
				WordToken wordToken = (WordToken) currentToken;
				if (Types.isType(wordToken.getType())) {
					tokenizer.nextToken();
					return wordToken.getType();
				}
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return -1;
	}

	protected Node visitNumber(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof DoubleToken) {
			DoubleToken token = (DoubleToken) currentToken;
			tokenizer.nextToken();
			return new DoubleNode(token.getNumber());
		} else if (currentToken instanceof FloatToken) {
			FloatToken token = (FloatToken) currentToken;
			tokenizer.nextToken();
			return new FloatNode(token.getNumber());
		} else if (currentToken instanceof LongToken) {
			LongToken token = (LongToken) currentToken;
			tokenizer.nextToken();
			return new LongNode(token.getNumber());
		} else if (currentToken instanceof IntToken) {
			IntToken token = (IntToken) currentToken;
			tokenizer.nextToken();
			return new IntNode(token.getNumber());
		} else if (currentToken instanceof ShortToken) {
			ShortToken token = (ShortToken) currentToken;
			tokenizer.nextToken();
			return new ShortNode(token.getNumber());
		} else if (currentToken instanceof ByteToken) {
			ByteToken token = (ByteToken) currentToken;
			tokenizer.nextToken();
			return new ByteNode(token.getNumber());
		}

		return null;
	}

	protected boolean visitNumber(Tokenizer tokenizer, CompileHandler handler) {
		try {
			skipComments(tokenizer);

			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof DoubleToken || currentToken instanceof FloatToken || currentToken instanceof LongToken || currentToken instanceof IntToken || currentToken instanceof ShortToken || currentToken instanceof ByteToken) {
				tokenizer.nextToken();
				return true;
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return false;
	}

	protected CharacterNode visitCharacter(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof CharToken) {
			CharToken token = (CharToken) currentToken;
			tokenizer.nextToken();
			return new CharacterNode(token.getChar());
		}

		return null;
	}

	protected boolean visitCharacter(Tokenizer tokenizer, CompileHandler handler) {
		try {
			skipComments(tokenizer);

			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof CharToken) {
				tokenizer.nextToken();
				return true;
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return false;
	}

	protected StringNode visitString(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof StringToken) {
			StringToken token = (StringToken) currentToken;
			tokenizer.nextToken();
			return new StringNode(token.getString());
		}

		return null;
	}

	protected boolean visitString(Tokenizer tokenizer, CompileHandler handler) {
		try {
			skipComments(tokenizer);

			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof StringToken) {
				tokenizer.nextToken();
				return true;
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return false;
	}

	protected int visitSymbol(Tokenizer tokenizer, int... types) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof SymbolToken) {
			SymbolToken symbolToken = (SymbolToken) currentToken;
			for (int type : types) {
				if (symbolToken.getType() == type) {
					tokenizer.nextToken();
					return type;
				}
			}
		}

		return -1;
	}

	protected int visitSymbol(Tokenizer tokenizer, CompileHandler handler, int... types) {
		try {
			skipComments(tokenizer);

			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof SymbolToken) {
				SymbolToken symbolToken = (SymbolToken) currentToken;
				for (int type : types) {
					if (symbolToken.getType() == type) {
						tokenizer.nextToken();
						return type;
					}
				}
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return -1;
	}

	protected void expectSymbol(int type, Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitSymbol(tokenizer, type) == -1) {
			throw new ParseException("'" + SymbolToken.getSymbol(type) + "' is expected", tokenizer.currentToken());
		}
	}

	protected void expectSymbol(int type, Tokenizer tokenizer, CompileHandler handler) {
		int offset = tokenizer.currentToken() != null ? tokenizer.currentToken().getOffset() : 0;
		Token lastToken = null;
		try {
			while (visitSymbol(tokenizer, type) == -1) {
				lastToken = tokenizer.currentToken();
				if (tokenizer.hasNext()) {
					tokenizer.nextToken();
				} else {
					break;
				}
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		if (lastToken != null) {
			handler.errorOccured(tokenizer.getLine(), offset, 1, "'" + SymbolToken.getSymbol(type) + "' is expected");
			// lastToken.getOffset() + lastToken.getLength() - offset
		}
	}

	protected int visitEquate(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof SymbolToken) {
			SymbolToken symbolToken = (SymbolToken) currentToken;
			if (Operations.isEquate(symbolToken.getType())) {
				tokenizer.nextToken();
				return symbolToken.getType();
			}
		}

		return -1;
	}

	protected int visitEquate(Tokenizer tokenizer, CompileHandler handler) {
		try {
			skipComments(tokenizer);

			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof SymbolToken) {
				SymbolToken symbolToken = (SymbolToken) currentToken;
				if (Operations.isEquate(symbolToken.getType())) {
					tokenizer.nextToken();
					return symbolToken.getType();
				}
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return -1;
	}

	protected int visitDimension(Tokenizer tokenizer) throws TokenizerException {
		int dimension = 0;
		while (visitSymbol(tokenizer, SymbolToken.MASSIVE) != -1) {
			dimension++;
		}
		return dimension;
	}

	protected int visitDimension(Tokenizer tokenizer, CompileHandler handler) {
		int dimension = 0;
		try {
			while (visitSymbol(tokenizer, SymbolToken.MASSIVE) != -1) {
				dimension++;
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}
		return dimension;
	}

	protected VariableNode visitVariable(Tokenizer tokenizer) throws TokenizerException, ParseException {
		String namespace = null;
		String variableName = visitWord(WordToken.NOT_SERVICE, tokenizer);
		if (visitSymbol(tokenizer, SymbolToken.POINT) != -1) {
			namespace = variableName;
			variableName = visitWord(WordToken.NOT_SERVICE, tokenizer);
		}

		if (variableName != null) {
			return new VariableNode(namespace, variableName);
		}
		return null;
	}

	protected VariableNode visitVariable(Tokenizer tokenizer, CompileHandler handler) {
		try {
			String namespace = null;
			String variableName = visitWord(WordToken.NOT_SERVICE, tokenizer);
			if (visitSymbol(tokenizer, SymbolToken.POINT) != -1) {
				namespace = variableName;
				variableName = visitWord(WordToken.NOT_SERVICE, tokenizer);
			}

			if (variableName != null) {
				return new VariableNode(namespace, variableName);
			}
		} catch (Exception exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}
		return null;
	}

	public void errorOccured(Tokenizer tokenizer, CompileHandler handler, String message) {
		errorOccured(handler, message, tokenizer.currentToken());
	}

	public void errorOccured(CompileHandler handler, String message, Token token) {
		if (token != null) {
			handler.errorOccured(token.getLine(), token.getOffset(), token.getLength(), message);
		}
	}

	public abstract N visit(Tokenizer tokenizer) throws TokenizerException, ParseException;

	public abstract boolean visit(Tokenizer tokenizer, CompileHandler handler);
}
