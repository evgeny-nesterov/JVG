package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.CharacterNode;
import ru.nest.hiscript.pol.model.DoubleNode;
import ru.nest.hiscript.pol.model.FloatNode;
import ru.nest.hiscript.pol.model.IntNode;
import ru.nest.hiscript.pol.model.LongNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.StringNode;
import ru.nest.hiscript.pol.model.Types;
import ru.nest.hiscript.pol.model.VariableNode;
import ru.nest.hiscript.tokenizer.CharToken;
import ru.nest.hiscript.tokenizer.DoubleToken;
import ru.nest.hiscript.tokenizer.FloatToken;
import ru.nest.hiscript.tokenizer.IntToken;
import ru.nest.hiscript.tokenizer.LongToken;
import ru.nest.hiscript.tokenizer.OperationSymbols;
import ru.nest.hiscript.tokenizer.StringToken;
import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

public abstract class ParseRule<N extends Node> {
	protected String visitWord(int type, Tokenizer tokenizer) throws TokenizerException {
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

	public static class NamespaceName {
		public String name;

		public String namespace;
	}

	protected NamespaceName visitNamespaceName(Tokenizer tokenizer) throws TokenizerException {
		String name = visitWord(Words.NOT_SERVICE, tokenizer);
		if (name != null) {
			NamespaceName namespaceName = new NamespaceName();
			namespaceName.name = name;
			if (visitSymbol(tokenizer, Symbols.POINT) != -1) {
				namespaceName.namespace = namespaceName.name;
				namespaceName.name = visitWord(Words.NOT_SERVICE, tokenizer);
			}
			return namespaceName;
		}
		return null;
	}

	protected String visitWord(int type, Tokenizer tokenizer, CompileHandler handler) {
		try {
			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof WordToken) {
				WordToken wordToken = (WordToken) currentToken;
				if (wordToken.getType() == type) {
					tokenizer.nextToken();
					return wordToken.getWord();
				}
			}
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return null;
	}

	protected int visitWords(Tokenizer tokenizer, int... types) throws TokenizerException {
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
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return -1;
	}

	protected int visitType(Tokenizer tokenizer) throws TokenizerException {
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
			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof WordToken) {
				WordToken wordToken = (WordToken) currentToken;
				if (Types.isType(wordToken.getType())) {
					tokenizer.nextToken();
					return wordToken.getType();
				}
			}
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return -1;
	}

	protected Node visitNumber(Tokenizer tokenizer) throws TokenizerException {
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
		}
		return null;
	}

	protected boolean visitNumber(Tokenizer tokenizer, CompileHandler handler) {
		try {
			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof DoubleToken || currentToken instanceof FloatToken || currentToken instanceof LongToken || currentToken instanceof IntToken) {
				tokenizer.nextToken();
				return true;
			}
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return false;
	}

	protected CharacterNode visitCharacter(Tokenizer tokenizer) throws TokenizerException {
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
			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof CharToken) {
				tokenizer.nextToken();
				return true;
			}
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return false;
	}

	protected StringNode visitString(Tokenizer tokenizer) throws TokenizerException {
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
			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof StringToken) {
				tokenizer.nextToken();
				return true;
			}
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return false;
	}

	protected int visitSymbol(Tokenizer tokenizer, int... types) throws TokenizerException {
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
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return -1;
	}

	protected void expectSymbol(int type, Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		if (visitSymbol(tokenizer, type) == -1) {
			throw new HiScriptParseException("'" + SymbolToken.getSymbol(type) + "' is expected", tokenizer.currentToken());
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
			errorOccurred(tokenizer, handler, exc.getMessage());
		}

		if (lastToken != null) {
			handler.errorOccurred(tokenizer.getLine(), offset, 1, "'" + SymbolToken.getSymbol(type) + "' is expected");
			// lastToken.getOffset() + lastToken.getLength() - offset
		}
	}

	protected int visitEquate(Tokenizer tokenizer) throws TokenizerException {
		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof SymbolToken) {
			SymbolToken symbolToken = (SymbolToken) currentToken;
			if (OperationSymbols.isEquate(symbolToken.getType())) {
				tokenizer.nextToken();
				return symbolToken.getType();
			}
		}
		return -1;
	}

	protected int visitEquate(Tokenizer tokenizer, CompileHandler handler) {
		try {
			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof SymbolToken) {
				SymbolToken symbolToken = (SymbolToken) currentToken;
				if (OperationSymbols.isEquate(symbolToken.getType())) {
					tokenizer.nextToken();
					return symbolToken.getType();
				}
			}
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return -1;
	}

	protected int visitDimension(Tokenizer tokenizer) throws TokenizerException {
		int dimension = 0;
		while (visitSymbol(tokenizer, Symbols.MASSIVE) != -1) {
			dimension++;
		}
		return dimension;
	}

	protected int visitDimension(Tokenizer tokenizer, CompileHandler handler) {
		int dimension = 0;
		try {
			while (visitSymbol(tokenizer, Symbols.MASSIVE) != -1) {
				dimension++;
			}
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return dimension;
	}

	protected VariableNode visitVariable(Tokenizer tokenizer) throws TokenizerException {
		String namespace = null;
		String variableName = visitWord(Words.NOT_SERVICE, tokenizer);
		if (visitSymbol(tokenizer, Symbols.POINT) != -1) {
			namespace = variableName;
			variableName = visitWord(Words.NOT_SERVICE, tokenizer);
		}

		if (variableName != null) {
			return new VariableNode(namespace, variableName);
		}
		return null;
	}

	protected VariableNode visitVariable(Tokenizer tokenizer, CompileHandler handler) {
		try {
			String namespace = null;
			String variableName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (visitSymbol(tokenizer, Symbols.POINT) != -1) {
				namespace = variableName;
				variableName = visitWord(Words.NOT_SERVICE, tokenizer);
			}

			if (variableName != null) {
				return new VariableNode(namespace, variableName);
			}
		} catch (Exception exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return null;
	}

	public void errorOccurred(Tokenizer tokenizer, CompileHandler handler, String message) {
		errorOccurred(handler, message, tokenizer.currentToken());
	}

	public void errorOccurred(CompileHandler handler, String message, Token token) {
		if (token != null) {
			handler.errorOccurred(token.getLine(), token.getOffset(), token.getLength(), message);
		}
	}

	public abstract N visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException;

	public abstract boolean visit(Tokenizer tokenizer, CompileHandler handler);
}
