package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeAssert;
import ru.nest.hiscript.ool.model.nodes.NodeByte;
import ru.nest.hiscript.ool.model.nodes.NodeChar;
import ru.nest.hiscript.ool.model.nodes.NodeDouble;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeFloat;
import ru.nest.hiscript.ool.model.nodes.NodeInt;
import ru.nest.hiscript.ool.model.nodes.NodeLong;
import ru.nest.hiscript.ool.model.nodes.NodeNumber;
import ru.nest.hiscript.ool.model.nodes.NodeShort;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.tokenizer.ByteToken;
import ru.nest.hiscript.tokenizer.CharToken;
import ru.nest.hiscript.tokenizer.CommentToken;
import ru.nest.hiscript.tokenizer.DoubleToken;
import ru.nest.hiscript.tokenizer.FloatToken;
import ru.nest.hiscript.tokenizer.IntToken;
import ru.nest.hiscript.tokenizer.LongToken;
import ru.nest.hiscript.tokenizer.ShortToken;
import ru.nest.hiscript.tokenizer.StringToken;
import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ParserUtil implements Words {
	public void skipComments(Tokenizer tokenizer) throws TokenizerException {
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

	protected String expectWord(int type, Tokenizer tokenizer) throws TokenizerException, ParseException {
		String word = visitWord(type, tokenizer);
		if (word == null) {
			throw new ParseException("'" + WordToken.getWord(type) + "' is expected", tokenizer.currentToken());
		}
		return word;
	}

	protected int visitWordType(Tokenizer tokenizer, int... types) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof WordToken) {
			WordToken wordToken = (WordToken) currentToken;

			for (int type : types) {
				if (wordToken.getType() == type) {
					tokenizer.nextToken();
					return wordToken.getType();
				}
			}
		}
		return -1;
	}

	protected String visitWord(Tokenizer tokenizer, int... types) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof WordToken) {
			WordToken wordToken = (WordToken) currentToken;

			for (int type : types) {
				if (wordToken.getType() == type) {
					tokenizer.nextToken();
					return wordToken.getWord();
				}
			}
		}
		return null;
	}

	protected int visitServiceWord(Tokenizer tokenizer, int... types) throws TokenizerException {
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

	protected Type visitType(Tokenizer tokenizer, boolean allowArray) throws TokenizerException, ParseException {
		Type type = null;
		String name = visitWord(tokenizer, BOOLEAN, CHAR, BYTE, SHORT, INT, FLOAT, LONG, DOUBLE, VAR);
		if (name != null) {
			if (name.equals("var")) {
				return Type.varType;
			} else {
				type = Type.getPrimitiveType(name);
			}
		}

		if (type == null) {
			type = visitObjectType(tokenizer);
		}

		if (allowArray && type != null) {
			int dimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, dimension);
		}
		return type;
	}

	protected Type visitObjectType(Tokenizer tokenizer) throws TokenizerException, ParseException {
		Type type = null;
		String name = visitWord(Words.NOT_SERVICE, tokenizer);
		if (name != null) {
			type = Type.getType(null, name);
			while (visitSymbol(tokenizer, Symbols.POINT) != -1) {
				name = visitWord(Words.NOT_SERVICE, tokenizer);
				if (name == null) {
					throw new ParseException("identifier is expected", tokenizer.currentToken());
				}
				type = Type.getType(type, name);
			}
		}
		return type;
	}

	protected NodeNumber visitNumber(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof DoubleToken) {
			DoubleToken token = (DoubleToken) currentToken;
			tokenizer.nextToken();
			return new NodeDouble(token.getNumber(), token.hasSign());
		} else if (currentToken instanceof FloatToken) {
			FloatToken token = (FloatToken) currentToken;
			tokenizer.nextToken();
			return new NodeFloat(token.getNumber(), token.hasSign());
		} else if (currentToken instanceof LongToken) {
			LongToken token = (LongToken) currentToken;
			tokenizer.nextToken();
			return new NodeLong(token.getNumber(), token.hasSign());
		} else if (currentToken instanceof IntToken) {
			IntToken token = (IntToken) currentToken;
			tokenizer.nextToken();
			return new NodeInt(token.getNumber(), token.hasSign());
		} else if (currentToken instanceof ShortToken) {
			ShortToken token = (ShortToken) currentToken;
			tokenizer.nextToken();
			return new NodeShort(token.getNumber(), token.hasSign());
		} else if (currentToken instanceof ByteToken) {
			ByteToken token = (ByteToken) currentToken;
			tokenizer.nextToken();
			return NodeByte.getInstance(token.getNumber(), token.hasSign());
		}
		return null;
	}

	protected NodeChar visitCharacter(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof CharToken) {
			CharToken token = (CharToken) currentToken;
			tokenizer.nextToken();
			return NodeChar.getInstance(token.getChar());
		}
		return null;
	}

	protected NodeString visitString(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof StringToken) {
			StringToken token = (StringToken) currentToken;
			tokenizer.nextToken();
			return new NodeString(token.getString());
		}
		return null;
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

	protected int checkSymbol(Tokenizer tokenizer, int... types) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof SymbolToken) {
			SymbolToken symbolToken = (SymbolToken) currentToken;
			for (int type : types) {
				if (symbolToken.getType() == type) {
					return type;
				}
			}
		}
		return -1;
	}

	protected void expectSymbol(Tokenizer tokenizer, int type) throws TokenizerException, ParseException {
		if (visitSymbol(tokenizer, type) == -1) {
			throw new ParseException("'" + SymbolToken.getSymbol(type) + "' is expected", tokenizer.currentToken());
		}
	}

	protected int visitDimension(Tokenizer tokenizer) throws TokenizerException {
		int dimension = 0;
		while (visitSymbol(tokenizer, Symbols.MASSIVE) != -1) {
			dimension++;
		}
		return dimension;
	}

	protected NodeExpression expectCondition(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		skipComments(tokenizer);

		expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);
		NodeExpression condition = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (condition == null) {
			throw new ParseException("expression is expected", tokenizer.currentToken());
		}
		expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
		return condition;
	}

	protected Node expectBody(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		skipComments(tokenizer);

		ctx.enter(RuntimeContext.BLOCK);
		Node body = StatementParseRule.getInstance().visit(tokenizer, ctx);
		ctx.exit();
		if (body == null) {
			throw new ParseException("statement is expected", tokenizer.currentToken());
		}

		if (!ctx.getCompiler().isAssertsActive() && body instanceof NodeAssert) {
			body = EmptyNode.getInstance();
		}
		return body;
	}

	protected NodeExpression expectExpression(Tokenizer tokenizer, CompileClassContext properties) throws TokenizerException, ParseException {
		skipComments(tokenizer);

		NodeExpression expression = ExpressionParseRule.getInstance().visit(tokenizer, properties);
		if (expression == null) {
			throw new ParseException("expression is expected", tokenizer.currentToken());
		}
		return expression;
	}

	protected Modifiers visitModifiers(Tokenizer tokenizer) throws TokenizerException, ParseException {
		Modifiers m = null;
		String word;
		while (true) {
			if ((word = visitWord(tokenizer, PUBLIC, PROTECTED, PRIVATE)) != null) {
				if (m != null && m.getAccess() != ModifiersIF.ACCESS_DEFAULT) {
					throw new ParseException("Illegal combination of modifiers: 'public' and 'public'", tokenizer.currentToken());
				}

				if (m == null) {
					m = new Modifiers();
				}

				m.setAccess(Modifiers.mapWordsToModification(WordToken.getType(word)));
				continue;
			}

			if ((word = visitWord(tokenizer, FINAL)) != null) {
				if (m != null && m.isFinal()) {
					throw new ParseException("Illegal combination of modifiers: 'final' and 'final'", tokenizer.currentToken());
				}

				if (m == null) {
					m = new Modifiers();
				}

				m.setFinal(true);
				continue;
			}

			if ((word = visitWord(tokenizer, STATIC)) != null) {
				if (m != null && m.isStatic()) {
					throw new ParseException("Illegal combination of modifiers: 'static' and 'static'", tokenizer.currentToken());
				} else if (m != null && m.isAbstract()) {
					throw new ParseException("Illegal combination of modifiers: 'static' and 'abstract'", tokenizer.currentToken());
				} else if (m != null && m.isDefault()) {
					throw new ParseException("Illegal combination of modifiers: 'static' and 'default'", tokenizer.currentToken());
				}

				if (m == null) {
					m = new Modifiers();
				}

				m.setStatic(true);
				continue;
			}

			if ((word = visitWord(tokenizer, NATIVE)) != null) {
				if (m != null && m.isNative()) {
					throw new ParseException("Illegal combination of modifiers: 'native' and 'native'", tokenizer.currentToken());
				} else if (m != null && m.isDefault()) {
					throw new ParseException("Illegal combination of modifiers: 'native' and 'default'", tokenizer.currentToken());
				}

				if (m == null) {
					m = new Modifiers();
				}

				m.setNative(true);
				continue;
			}

			if ((word = visitWord(tokenizer, ABSTRACT)) != null) {
				if (m != null && m.isAbstract()) {
					throw new ParseException("Illegal combination of modifiers: 'abstract' and 'abstract'", tokenizer.currentToken());
				} else if (m != null && m.isDefault()) {
					throw new ParseException("Illegal combination of modifiers: 'abstract' and 'default'", tokenizer.currentToken());
				} else if (m != null && m.isStatic()) {
					throw new ParseException("Illegal combination of modifiers: 'abstract' and 'static'", tokenizer.currentToken());
				}

				if (m == null) {
					m = new Modifiers();
				}

				m.setAbstract(true);
				continue;
			}

			if ((word = visitWord(tokenizer, DEFAULT)) != null) {
				if (m != null && m.isDefault()) {
					throw new ParseException("Illegal combination of modifiers: 'default' and 'default'", tokenizer.currentToken());
				} else if (m != null && m.isAbstract()) {
					throw new ParseException("Illegal combination of modifiers: 'default' and 'abstract'", tokenizer.currentToken());
				} else if (m != null && m.isStatic()) {
					throw new ParseException("Illegal combination of modifiers: 'default' and 'static'", tokenizer.currentToken());
				} else if (m != null && m.isNative()) {
					throw new ParseException("Illegal combination of modifiers: 'default' and 'native'", tokenizer.currentToken());
				}

				if (m == null) {
					m = new Modifiers();
				}

				m.setDefault(true);
				continue;
			}
			break;
		}

		if (m != null) {
			return m;
		} else {
			return new Modifiers();
		}
	}

	public void checkModifiers(Tokenizer tokenizer, Modifiers m, int... allowed) throws ParseException {
		int notAllowedModifier = m.check(allowed);
		if (notAllowedModifier != -1) {
			throw new ParseException("modifier '" + Modifiers.getName(notAllowedModifier) + "' is not allowed", tokenizer.currentToken());
		}
	}

	protected Node[] visitArgumentsValues(Tokenizer tokenizer, CompileClassContext properties) throws TokenizerException, ParseException {
		List<Node> args = new ArrayList<>(3);
		NodeExpression arg = ExpressionParseRule.getInstance().visit(tokenizer, properties);
		if (arg != null) {
			args.add(arg);
			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				arg = ExpressionParseRule.getInstance().visit(tokenizer, properties);
				if (arg == null) {
					throw new ParseException("expression is expected", tokenizer.currentToken());
				}
				args.add(arg);
			}
		}

		Node[] argsArray = new Node[args.size()];
		args.toArray(argsArray);
		return argsArray;
	}

	protected void visitArgumentsDefinitions(Tokenizer tokenizer, List<NodeArgument> arguments, CompileClassContext properties) throws TokenizerException, ParseException {
		NodeArgument arg = MethodArgumentParseRule.getInstance().visit(tokenizer, properties);
		if (arg != null) {
			arguments.add(arg);
			boolean hasVarargs = arg.isVarargs();
			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				arg = MethodArgumentParseRule.getInstance().visit(tokenizer, properties);
				if (arg == null) {
					throw new ParseException("argument is expected", tokenizer.currentToken());
				}
				if (arg.isVarargs()) {
					if (hasVarargs) {
						throw new ParseException("Varargs parameter must be the last in the list", tokenizer.currentToken());
					}
					hasVarargs = true;
				}
				arguments.add(arg);
			}
		}
	}

	public static String readString(InputStream is) throws IOException {
		return readString(new InputStreamReader(new BufferedInputStream(is, 2048)));
	}

	public static String readString(Reader r) throws IOException {
		try {
			StringBuilder buf = new StringBuilder();
			char[] b = new char[1024];
			int length;
			while ((length = r.read(b)) != -1) {
				buf.append(b, 0, length);
			}
			return buf.toString();
		} finally {
			r.close();
		}
	}
}
