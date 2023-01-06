package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.OperationsGroup;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeAssert;
import ru.nest.hiscript.ool.model.nodes.NodeByte;
import ru.nest.hiscript.ool.model.nodes.NodeChar;
import ru.nest.hiscript.ool.model.nodes.NodeDouble;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeFloat;
import ru.nest.hiscript.ool.model.nodes.NodeInt;
import ru.nest.hiscript.ool.model.nodes.NodeLong;
import ru.nest.hiscript.ool.model.nodes.NodeNumber;
import ru.nest.hiscript.ool.model.nodes.NodeShort;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.tokenizer.AnnotationWordToken;
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
	public static void skipComments(Tokenizer tokenizer) throws TokenizerException {
		while (tokenizer.currentToken() instanceof CommentToken) {
			tokenizer.nextToken();
		}
	}

	protected static Token startToken(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);
		return tokenizer.currentToken();
	}

	protected static String visitWord(int type, Tokenizer tokenizer) throws TokenizerException {
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

	protected static String expectWord(int type, Tokenizer tokenizer) throws TokenizerException {
		String word = visitWord(type, tokenizer);
		if (word == null) {
			if (type != NOT_SERVICE) {
				word = WordToken.getWord(type);
				tokenizer.error("'" + word + "' is expected");
			} else {
				tokenizer.error("identifier is expected");
			}
		}
		return word;
	}

	protected static int visitWordType(Tokenizer tokenizer, int... types) throws TokenizerException {
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

	protected static String visitWord(Tokenizer tokenizer, int... types) throws TokenizerException {
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

	protected static int visitServiceWord(Tokenizer tokenizer, int... types) throws TokenizerException {
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

	protected static String visitAnnotationWord(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof AnnotationWordToken) {
			AnnotationWordToken wordToken = (AnnotationWordToken) currentToken;
			if (!wordToken.isService()) {
				tokenizer.nextToken();

				String name = wordToken.getWord();
				while (visitSymbol(tokenizer, Symbols.POINT) != -1) {
					name += ".";
					name += expectWord(NOT_SERVICE, tokenizer);
				}
				return name;
			}
		}
		return null;
	}

	protected static Type visitType(Tokenizer tokenizer, boolean allowArray) throws TokenizerException, HiScriptParseException {
		Type type = Type.getTypeByWord(visitWordType(tokenizer, BOOLEAN, CHAR, BYTE, SHORT, INT, FLOAT, LONG, DOUBLE, VAR));
		if (type == null) {
			type = visitObjectType(tokenizer);
		}
		if (allowArray && type != null) {
			int dimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, dimension);
		}
		return type;
	}

	protected static Type visitObjectType(Tokenizer tokenizer) throws TokenizerException {
		Type type = null;
		String name = visitWord(Words.NOT_SERVICE, tokenizer);
		if (name != null) {
			type = Type.getType(null, name);
			while (visitSymbol(tokenizer, Symbols.POINT) != -1) {
				name = visitWord(Words.NOT_SERVICE, tokenizer);
				if (name == null) {
					tokenizer.error("identifier is expected");
				}
				type = Type.getType(type, name);
			}
		}
		return type;
	}

	protected static NodeNumber visitNumber(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof DoubleToken) {
			DoubleToken token = (DoubleToken) currentToken;
			tokenizer.nextToken();
			return new NodeDouble(token.getNumber(), token.hasSign(), token);
		} else if (currentToken instanceof FloatToken) {
			FloatToken token = (FloatToken) currentToken;
			tokenizer.nextToken();
			return new NodeFloat(token.getNumber(), token.hasSign(), token);
		} else if (currentToken instanceof LongToken) {
			LongToken token = (LongToken) currentToken;
			tokenizer.nextToken();
			return new NodeLong(token.getNumber(), token.hasSign(), token);
		} else if (currentToken instanceof IntToken) {
			IntToken token = (IntToken) currentToken;
			tokenizer.nextToken();
			return new NodeInt(token.getNumber(), token.hasSign(), token);
		} else if (currentToken instanceof ShortToken) {
			ShortToken token = (ShortToken) currentToken;
			tokenizer.nextToken();
			return new NodeShort(token.getNumber(), token.hasSign(), token);
		} else if (currentToken instanceof ByteToken) {
			ByteToken token = (ByteToken) currentToken;
			tokenizer.nextToken();
			return NodeByte.getInstance(token.getNumber(), token.hasSign(), token);
		}
		return null;
	}

	protected static NodeChar visitCharacter(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof CharToken) {
			CharToken token = (CharToken) currentToken;
			tokenizer.nextToken();
			return NodeChar.getInstance(token.getChar(), token);
		}
		return null;
	}

	protected static NodeString visitString(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof StringToken) {
			StringToken token = (StringToken) currentToken;
			tokenizer.nextToken();
			return new NodeString(token.getString());
		}
		return null;
	}

	protected static int visitSymbol(Tokenizer tokenizer, int... types) throws TokenizerException {
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

	protected static int checkSymbol(Tokenizer tokenizer, int... types) throws TokenizerException {
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

	protected static void expectSymbol(Tokenizer tokenizer, int type) throws TokenizerException {
		if (visitSymbol(tokenizer, type) == -1) {
			tokenizer.error("'" + SymbolToken.getSymbol(type) + "' is expected");
		}
	}

	protected static int visitDimension(Tokenizer tokenizer) throws TokenizerException {
		int dimension = 0;
		while (visitSymbol(tokenizer, Symbols.MASSIVE) != -1) {
			dimension++;
		}
		return dimension;
	}

	protected static NodeExpression expectCondition(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		skipComments(tokenizer);

		expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);
		NodeExpression condition = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (condition == null) {
			tokenizer.error("expression is expected");
		}
		expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
		return condition;
	}

	protected static HiNode expectBody(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		skipComments(tokenizer);

		ctx.enter(RuntimeContext.BLOCK, startToken(tokenizer));
		HiNode body = StatementParseRule.getInstance().visit(tokenizer, ctx);
		ctx.exit();
		if (body == null) {
			tokenizer.error("statement is expected");
			body = EmptyNode.getInstance();
		}

		if (!ctx.getCompiler().isAssertsActive() && body instanceof NodeAssert) {
			body = EmptyNode.getInstance();
		}
		return body;
	}

	protected static NodeExpression expectExpression(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		skipComments(tokenizer);

		NodeExpression expression = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (expression == null) {
			tokenizer.error("expression is expected");
			expression = new NodeExpressionNoLS(new HiNode[0], new OperationsGroup[0]);
		}
		return expression;
	}

	protected static AnnotatedModifiers visitAnnotatedModifiers(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Token startToken = startToken(tokenizer);
		Modifiers modifiers = null;
		List<NodeAnnotation> annotations = null;
		int word;
		while (true) {
			annotations = AnnotationParseRule.getInstance().visitAnnotations(tokenizer, ctx, annotations);

			if ((word = visitWordType(tokenizer, PUBLIC, PROTECTED, PRIVATE)) != -1) {
				if (modifiers != null && modifiers.getAccess() != ModifiersIF.ACCESS_DEFAULT) {
					tokenizer.error("Illegal combination of modifiers: 'public' and 'public'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setAccess(Modifiers.mapWordsToModification(word));
				continue;
			}

			if (visitWordType(tokenizer, FINAL) != -1) {
				if (modifiers != null && modifiers.isFinal()) {
					tokenizer.error("Illegal combination of modifiers: 'final' and 'final'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setFinal(true);
				continue;
			}

			if (visitWordType(tokenizer, STATIC) != -1) {
				if (modifiers != null && modifiers.isStatic()) {
					tokenizer.error("Illegal combination of modifiers: 'static' and 'static'");
				} else if (modifiers != null && modifiers.isAbstract()) {
					tokenizer.error("Illegal combination of modifiers: 'static' and 'abstract'");
				} else if (modifiers != null && modifiers.isDefault()) {
					tokenizer.error("Illegal combination of modifiers: 'static' and 'default'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setStatic(true);
				continue;
			}

			if (visitWordType(tokenizer, NATIVE) != -1) {
				if (modifiers != null && modifiers.isNative()) {
					tokenizer.error("Illegal combination of modifiers: 'native' and 'native'");
				} else if (modifiers != null && modifiers.isDefault()) {
					tokenizer.error("Illegal combination of modifiers: 'native' and 'default'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setNative(true);
				continue;
			}

			if (visitWordType(tokenizer, ABSTRACT) != -1) {
				if (modifiers != null && modifiers.isAbstract()) {
					tokenizer.error("Illegal combination of modifiers: 'abstract' and 'abstract'");
				} else if (modifiers != null && modifiers.isDefault()) {
					tokenizer.error("Illegal combination of modifiers: 'abstract' and 'default'");
				} else if (modifiers != null && modifiers.isStatic()) {
					tokenizer.error("Illegal combination of modifiers: 'abstract' and 'static'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setAbstract(true);
				continue;
			}

			if (visitWordType(tokenizer, DEFAULT) != -1) {
				if (modifiers != null && modifiers.isDefault()) {
					tokenizer.error("Illegal combination of modifiers: 'default' and 'default'");
				} else if (modifiers != null && modifiers.isAbstract()) {
					tokenizer.error("Illegal combination of modifiers: 'default' and 'abstract'");
				} else if (modifiers != null && modifiers.isStatic()) {
					tokenizer.error("Illegal combination of modifiers: 'default' and 'static'");
				} else if (modifiers != null && modifiers.isNative()) {
					tokenizer.error("Illegal combination of modifiers: 'default' and 'native'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setDefault(true);
				continue;
			}
			break;
		}
		return new AnnotatedModifiers(annotations != null ? annotations.toArray(new NodeAnnotation[annotations.size()]) : null, modifiers, tokenizer.getBlockToken(startToken));
	}

	public static boolean checkModifiers(Tokenizer tokenizer, Modifiers m, Token modifiersToken, int... allowed) throws TokenizerException {
		return m.check(tokenizer, modifiersToken, allowed);
	}

	protected static HiNode[] visitArgumentsValues(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		List<HiNode> args = new ArrayList<>(3);
		NodeExpression arg = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (arg != null) {
			if (arg.isCastedIdentifier()) {
				tokenizer.rollback();
				return new HiNode[0];
			}

			tokenizer.commit();
			args.add(arg);
			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				Token token = tokenizer.currentToken();
				arg = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
				if (arg == null || arg.isCastedIdentifier()) {
					tokenizer.error("expression is expected", token);
				}
				args.add(arg);
			}
		} else {
			tokenizer.commit();
		}

		HiNode[] argsArray = new HiNode[args.size()];
		args.toArray(argsArray);
		return argsArray;
	}

	protected static void visitArgumentsDefinitions(Tokenizer tokenizer, List<NodeArgument> arguments, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		NodeArgument arg = MethodArgumentParseRule.getInstance().visit(tokenizer, ctx);
		if (arg != null) {
			arguments.add(arg);
			boolean hasVarargs = arg.isVarargs();
			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				arg = MethodArgumentParseRule.getInstance().visit(tokenizer, ctx);
				if (arg != null) {
					if (arg.isVarargs()) {
						if (hasVarargs) {
							tokenizer.error("Varargs parameter must be the last in the list");
						}
						hasVarargs = true;
					}
					if (arguments.contains(arg)) {
						tokenizer.error("duplicated argument '" + arg.name + "'", arg.getToken());
					}
					arguments.add(arg);
				} else {
					tokenizer.error("argument is expected");
				}
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
