package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
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

	protected static String expectWord(int type, Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		String word = visitWord(type, tokenizer);
		if (word == null) {
			throw new HiScriptParseException("'" + WordToken.getWord(type) + "' is expected", tokenizer.currentToken());
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
					return wordToken.getType();
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

	protected static Type visitObjectType(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		Type type = null;
		String name = visitWord(Words.NOT_SERVICE, tokenizer);
		if (name != null) {
			type = Type.getType(null, name);
			while (visitSymbol(tokenizer, Symbols.POINT) != -1) {
				name = visitWord(Words.NOT_SERVICE, tokenizer);
				if (name == null) {
					throw new HiScriptParseException("identifier is expected", tokenizer.currentToken());
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

	protected static void expectSymbol(Tokenizer tokenizer, int type) throws TokenizerException, HiScriptParseException {
		if (visitSymbol(tokenizer, type) == -1) {
			throw new HiScriptParseException("'" + SymbolToken.getSymbol(type) + "' is expected", tokenizer.currentToken());
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
			throw new HiScriptParseException("expression is expected", tokenizer.currentToken());
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
			throw new HiScriptParseException("statement is expected", tokenizer.currentToken());
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
			throw new HiScriptParseException("expression is expected", tokenizer.currentToken());
		}
		return expression;
	}

	protected static AnnotatedModifiers visitAnnotatedModifiers(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Modifiers modifiers = null;
		List<NodeAnnotation> annotations = null;
		String word;
		while (true) {
			annotations = AnnotationParseRule.getInstance().visitAnnotations(tokenizer, ctx, annotations);

			if ((word = visitWord(tokenizer, PUBLIC, PROTECTED, PRIVATE)) != null) {
				if (modifiers != null && modifiers.getAccess() != ModifiersIF.ACCESS_DEFAULT) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'public' and 'public'", tokenizer.currentToken());
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setAccess(Modifiers.mapWordsToModification(WordToken.getType(word)));
				continue;
			}

			if (visitWord(tokenizer, FINAL) != null) {
				if (modifiers != null && modifiers.isFinal()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'final' and 'final'", tokenizer.currentToken());
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setFinal(true);
				continue;
			}

			if (visitWord(tokenizer, STATIC) != null) {
				if (modifiers != null && modifiers.isStatic()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'static' and 'static'", tokenizer.currentToken());
				} else if (modifiers != null && modifiers.isAbstract()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'static' and 'abstract'", tokenizer.currentToken());
				} else if (modifiers != null && modifiers.isDefault()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'static' and 'default'", tokenizer.currentToken());
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setStatic(true);
				continue;
			}

			if (visitWord(tokenizer, NATIVE) != null) {
				if (modifiers != null && modifiers.isNative()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'native' and 'native'", tokenizer.currentToken());
				} else if (modifiers != null && modifiers.isDefault()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'native' and 'default'", tokenizer.currentToken());
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setNative(true);
				continue;
			}

			if (visitWord(tokenizer, ABSTRACT) != null) {
				if (modifiers != null && modifiers.isAbstract()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'abstract' and 'abstract'", tokenizer.currentToken());
				} else if (modifiers != null && modifiers.isDefault()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'abstract' and 'default'", tokenizer.currentToken());
				} else if (modifiers != null && modifiers.isStatic()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'abstract' and 'static'", tokenizer.currentToken());
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setAbstract(true);
				continue;
			}

			if (visitWord(tokenizer, DEFAULT) != null) {
				if (modifiers != null && modifiers.isDefault()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'default' and 'default'", tokenizer.currentToken());
				} else if (modifiers != null && modifiers.isAbstract()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'default' and 'abstract'", tokenizer.currentToken());
				} else if (modifiers != null && modifiers.isStatic()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'default' and 'static'", tokenizer.currentToken());
				} else if (modifiers != null && modifiers.isNative()) {
					throw new HiScriptParseException("Illegal combination of modifiers: 'default' and 'native'", tokenizer.currentToken());
				}

				if (modifiers == null) {
					modifiers = new Modifiers();
				}

				modifiers.setDefault(true);
				continue;
			}
			break;
		}
		return new AnnotatedModifiers(annotations != null ? annotations.toArray(new NodeAnnotation[annotations.size()]) : null, modifiers);
	}

	public static void checkModifiers(Tokenizer tokenizer, Modifiers m, int... allowed) throws HiScriptParseException {
		int notAllowedModifier = m.check(allowed);
		if (notAllowedModifier != -1) {
			throw new HiScriptParseException("modifier '" + Modifiers.getName(notAllowedModifier) + "' is not allowed", tokenizer.currentToken());
		}
	}

	protected static HiNode[] visitArgumentsValues(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		List<HiNode> args = new ArrayList<>(3);
		NodeExpression arg = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (arg != null) {
			args.add(arg);
			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				arg = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
				if (arg == null) {
					throw new HiScriptParseException("expression is expected", tokenizer.currentToken());
				}
				args.add(arg);
			}
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
				if (arg == null) {
					throw new HiScriptParseException("argument is expected", tokenizer.currentToken());
				}
				if (arg.isVarargs()) {
					if (hasVarargs) {
						throw new HiScriptParseException("Varargs parameter must be the last in the list", tokenizer.currentToken());
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
