package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.parse.AnnotationParseRule;
import ru.nest.hiscript.ool.compile.parse.ExpressionParseRule;
import ru.nest.hiscript.ool.compile.parse.MethodArgumentParseRule;
import ru.nest.hiscript.ool.compile.parse.StatementParseRule;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeAssert;
import ru.nest.hiscript.ool.model.nodes.NodeCastedIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeChar;
import ru.nest.hiscript.ool.model.nodes.NodeDouble;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeFloat;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeInt;
import ru.nest.hiscript.ool.model.nodes.NodeLong;
import ru.nest.hiscript.ool.model.nodes.NodeNumber;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.AnnotationWordToken;
import ru.nest.hiscript.tokenizer.CharToken;
import ru.nest.hiscript.tokenizer.CommentToken;
import ru.nest.hiscript.tokenizer.DoubleToken;
import ru.nest.hiscript.tokenizer.FloatToken;
import ru.nest.hiscript.tokenizer.IntToken;
import ru.nest.hiscript.tokenizer.LongToken;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.nest.hiscript.ool.model.nodes.NodeVariable.UNNAMED;
import static ru.nest.hiscript.tokenizer.Words.*;

public class ParserUtil {
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

	protected static boolean checkWord(int type, Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof WordToken) {
			WordToken wordToken = (WordToken) currentToken;
			if (wordToken.getType() == type) {
				return true;
			}
		}
		return false;
	}

	protected static String expectWord(int type, Tokenizer tokenizer) throws TokenizerException {
		String word = visitWord(type, tokenizer);
		if (word == null) {
			if (type != NOT_SERVICE && type != UNNAMED_VARIABLE) {
				tokenizer.error("'" + WordToken.getWord(type) + "' is expected");
			} else {
				tokenizer.error("identifier is expected");
			}
		}
		return word;
	}

	protected static String expectWords(Tokenizer tokenizer, int... types) throws TokenizerException {
		if (types.length == 1) {
			return expectWord(types[0], tokenizer);
		}

		String word = visitWord(tokenizer, types);
		if (word == null) {
			String message = "";
			for (int type : types) {
				if (message.length() > 0) {
					message += " or ";
				}
				if (type != NOT_SERVICE && type != UNNAMED_VARIABLE) {
					tokenizer.error("'" + WordToken.getWord(type) + "'");
				} else {
					tokenizer.error("identifier");
				}
			}
			message += " are expected";
			tokenizer.error(message);
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
					name += "." + expectWords(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
				}
				return name;
			}
		}
		return null;
	}

	/**
	 * Visit complex type:
	 * T - simple type name (String, Integer, A, B, etc.)
	 * TT - simple type with package: T.T... (A.B.C, etc.)
	 * P - parameterized type: TT<C,...> (A.B<C, D>, etc.)
	 * A - array type: T[]..., TT[]..., P[]... (A[], A.B[][], A.B<C, D>[][], etc.)
	 * N - any type: T, TT, P, A
	 * C - complex type: N, ?, ? extends N, ? super N
	 */
	protected static Type visitType(Tokenizer tokenizer, boolean allowArray, HiRuntimeEnvironment env) throws TokenizerException, HiScriptParseException {
		Type type = Type.getTypeByWord(visitWordType(tokenizer, BOOLEAN, CHAR, BYTE, SHORT, INT, FLOAT, LONG, DOUBLE, VAR));
		if (type == null) {
			type = visitObjectType(tokenizer, env);
		}
		if (allowArray && type != null) {
			int dimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, dimension, env);
		}
		return type;
	}

	/**
	 * Visit simple type:
	 * T - simple type name (String, Integer, A, B, etc.)
	 * P - parameterized type: T<C,...> (A, A<B, C>, etc.)
	 * A - array type: P[]... (A[], A[][], A<B, C></>[][], etc.)
	 * S - simple type: T, P, A (all from visitType except TT and ?)
	 * C - complex type from visitType
	 */
	protected static Type visitSimpleType(Tokenizer tokenizer, boolean allowArray, HiRuntimeEnvironment env) throws TokenizerException, HiScriptParseException {
		Type type = Type.getTypeByWord(visitWordType(tokenizer, BOOLEAN, CHAR, BYTE, SHORT, INT, FLOAT, LONG, DOUBLE, VAR));
		if (type == null) {
			type = visitSimpleObjectType(tokenizer, env, null);
		}
		if (allowArray && type != null) {
			int dimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, dimension, env);
		}
		return type;
	}

	protected static Type visitObjectType(Tokenizer tokenizer, HiRuntimeEnvironment env) throws TokenizerException, HiScriptParseException {
		Type type = visitSimpleObjectType(tokenizer, env, null);
		if (type != null) {
			while (visitSymbol(tokenizer, Symbols.POINT) != -1) {
				type = visitSimpleObjectType(tokenizer, env, type);
			}
		} else if (visitSymbol(tokenizer, Symbols.QUESTION) != -1) {
			Type extendedType = Type.objectType;
			int extendsType = visitWordType(tokenizer, Words.EXTENDS, Words.SUPER);
			if (extendsType != -1) {
				extendedType = visitObjectType(tokenizer, env);
			}
			type = Type.getExtendedType(extendedType, extendsType == Words.SUPER);
		}
		return type;
	}

	protected static Type visitSimpleObjectType(Tokenizer tokenizer, HiRuntimeEnvironment env, Type parent) throws TokenizerException, HiScriptParseException {
		Type type = null;
		tokenizer.start();
		String name = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
		if (name != null) {
			type = Type.getType(parent, name, env);

			tokenizer.start();
			if (visitSymbol(tokenizer, Symbols.LOWER) != -1) {
				List<Type> parametersList = new ArrayList<>();
				do {
					Token parameterTypeToken = startToken(tokenizer);
					Type parameterType = visitType(tokenizer, true, env);
					if (parameterType != null) {
						if (parameterType.isPrimitive()) {
							tokenizer.error("type argument cannot be of primitive type", parameterTypeToken);
						} else {
							parametersList.add(parameterType);
						}
					} else {
						break;
					}
				} while (visitSymbol(tokenizer, Symbols.COMMA) != -1);
				if (visitGreater(tokenizer, false)) {
					tokenizer.commit();
					type = Type.getParameterizedType(type, parametersList.toArray(new Type[parametersList.size()]));
				} else {
					tokenizer.rollback();
				}
			} else {
				tokenizer.commit();
			}
		}
		tokenizer.commit();
		return type;
	}

	protected static NodeNumber visitNumber(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof DoubleToken) {
			DoubleToken token = (DoubleToken) currentToken;
			tokenizer.nextToken();
			return new NodeDouble(token.getNumber(), token);
		} else if (currentToken instanceof FloatToken) {
			FloatToken token = (FloatToken) currentToken;
			tokenizer.nextToken();
			return new NodeFloat(token.getNumber(), token);
		} else if (currentToken instanceof LongToken) {
			LongToken token = (LongToken) currentToken;
			tokenizer.nextToken();
			return new NodeLong(token.getNumber(), token);
		} else if (currentToken instanceof IntToken) {
			IntToken token = (IntToken) currentToken;
			tokenizer.nextToken();
			return new NodeInt(token.getNumber(), token);
		}
		return null;
	}

	protected static NodeChar visitCharacter(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof CharToken) {
			CharToken token = (CharToken) currentToken;
			tokenizer.nextToken();
			return new NodeChar(token.getChar(), token);
		}
		return null;
	}

	protected static NodeString visitString(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof StringToken) {
			StringToken token = (StringToken) currentToken;
			tokenizer.nextToken();
			return new NodeString(token.getString(), token);
		}
		return null;
	}

	protected static boolean visitGreater(Tokenizer tokenizer, boolean expect) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof SymbolToken) {
			SymbolToken symbolToken = (SymbolToken) currentToken;
			if (symbolToken.getType() == Symbols.BITWISE_SHIFT_RIGHT) {
				tokenizer.repeat(new SymbolToken(Symbols.GREATER, currentToken.getLine(), currentToken.getOffset() + 1, 1, currentToken.getLineOffset() + 1), 1);
				tokenizer.nextToken();
				return true;
			} else if (symbolToken.getType() == Symbols.BITWISE_SHIFT_RIGHT_CYCLIC) {
				tokenizer.repeat(new SymbolToken(Symbols.GREATER, currentToken.getLine(), currentToken.getOffset() + 1, 1, currentToken.getLineOffset() + 1), 2);
				tokenizer.nextToken();
				return true;
			} else if (symbolToken.getType() == Symbols.GREATER) {
				tokenizer.nextToken();
				return true;
			}
		}
		if (expect) {
			tokenizer.error("'" + SymbolToken.getSymbol(Symbols.GREATER) + "' is expected");
		}
		return false;
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

	protected static int expectSymbol(Tokenizer tokenizer, int... types) throws TokenizerException {
		int symbol = visitSymbol(tokenizer, types);
		if (symbol == -1) {
			tokenizer.error(Arrays.stream(types).mapToObj(type -> "'" + SymbolToken.getSymbol(type) + "'").collect(Collectors.joining(" or ")) + " is expected");
		}
		return symbol;
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
		NodeExpression condition = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
		if (condition == null) {
			tokenizer.error("expression expected");
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

		NodeExpression expression = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
		if (expression == null) {
			tokenizer.error("expression expected");
			expression = new NodeExpressionNoLS(new HiNode[0], new HiOperation[0]);
		}
		return expression;
	}

	protected static AnnotatedModifiers visitAnnotatedModifiers(Tokenizer tokenizer, CompileClassContext ctx, boolean sync) throws TokenizerException, HiScriptParseException {
		Token startToken = startToken(tokenizer);
		Modifiers.Changeable modifiers = null;
		List<NodeAnnotation> annotations = null;
		int word;
		while (true) {
			annotations = AnnotationParseRule.getInstance().visitAnnotations(tokenizer, ctx, annotations);

			if ((word = visitWordType(tokenizer, PUBLIC, PROTECTED, PRIVATE)) != -1) {
				if (modifiers != null && !modifiers.isDefaultAccess()) {
					tokenizer.error("illegal combination of modifiers: '" + modifiers.getName(modifiers.getAccess()) + "' and '" + Modifiers.mapWordsToModification(word) + "'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers.Changeable();
				}

				modifiers.setAccess(Modifiers.mapWordsToModification(word));
				continue;
			}

			if (visitWordType(tokenizer, FINAL) != -1) {
				if (modifiers != null && modifiers.isFinal()) {
					tokenizer.error("illegal combination of modifiers: 'final' and 'final'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers.Changeable();
				}

				modifiers.setFinal(true);
				continue;
			}

			if (visitWordType(tokenizer, STATIC) != -1) {
				if (modifiers != null && modifiers.isStatic()) {
					tokenizer.error("illegal combination of modifiers: 'static' and 'static'");
				} else if (modifiers != null && modifiers.isAbstract()) {
					tokenizer.error("illegal combination of modifiers: 'static' and 'abstract'");
				} else if (modifiers != null && modifiers.isDefault()) {
					tokenizer.error("illegal combination of modifiers: 'static' and 'default'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers.Changeable();
				}

				modifiers.setStatic(true);
				continue;
			}

			if (visitWordType(tokenizer, NATIVE) != -1) {
				if (modifiers != null && modifiers.isNative()) {
					tokenizer.error("illegal combination of modifiers: 'native' and 'native'");
				} else if (modifiers != null && modifiers.isDefault()) {
					tokenizer.error("illegal combination of modifiers: 'native' and 'default'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers.Changeable();
				}

				modifiers.setNative(true);
				continue;
			}

			if (visitWordType(tokenizer, ABSTRACT) != -1) {
				if (modifiers != null && modifiers.isAbstract()) {
					tokenizer.error("illegal combination of modifiers: 'abstract' and 'abstract'");
				} else if (modifiers != null && modifiers.isDefault()) {
					tokenizer.error("illegal combination of modifiers: 'abstract' and 'default'");
				} else if (modifiers != null && modifiers.isStatic()) {
					tokenizer.error("illegal combination of modifiers: 'abstract' and 'static'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers.Changeable();
				}

				modifiers.setAbstract(true);
				continue;
			}

			if (visitWordType(tokenizer, DEFAULT) != -1) {
				if (modifiers != null && modifiers.isDefault()) {
					tokenizer.error("illegal combination of modifiers: 'default' and 'default'");
				} else if (modifiers != null && modifiers.isAbstract()) {
					tokenizer.error("illegal combination of modifiers: 'default' and 'abstract'");
				} else if (modifiers != null && modifiers.isStatic()) {
					tokenizer.error("illegal combination of modifiers: 'default' and 'static'");
				} else if (modifiers != null && modifiers.isNative()) {
					tokenizer.error("illegal combination of modifiers: 'default' and 'native'");
				}

				if (modifiers == null) {
					modifiers = new Modifiers.Changeable();
				}

				modifiers.setDefault(true);
				continue;
			}

			if (sync && visitWordType(tokenizer, SYNCHRONIZED) != -1) {
				if (modifiers == null) {
					modifiers = new Modifiers.Changeable();
				}

				modifiers.setSynchronized(true);
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
		NodeExpression arg = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
		if (arg != null) {
			if (arg.isCastedIdentifier()) {
				tokenizer.rollback();
				return new HiNode[0];
			}

			tokenizer.commit();
			args.add(arg);
			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				Token token = tokenizer.currentToken();
				arg = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
				if (arg == null || arg.isCastedIdentifier()) {
					tokenizer.error("expression expected", token);
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
							tokenizer.error("varargs parameter must be the last in the list");
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

	public static HiNode visitIdentifier(Tokenizer tokenizer, CompileClassContext ctx, boolean visitCastAfterIdentifier, boolean requireCast, boolean createOnlyCastedIdentifier) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token identifierToken = startToken(tokenizer);
		Type parameterizedType = visitSimpleType(tokenizer, true, ctx.getEnv()); // visit A or A<B,C,...>
		IF:
		if (parameterizedType != null) {
			NodeCastedIdentifier[] castedRecordArguments = null;
			String castedVariableName = null;
			if (visitCastAfterIdentifier) {
				if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
					List<NodeCastedIdentifier> identifiersList = new ArrayList<>();
					visitCastedIdentifiers(tokenizer, identifiersList, ctx);
					if (requireCast) {
						if (visitSymbol(tokenizer, Symbols.PARENTHESES_RIGHT) == -1) {
							break IF;
						}
					} else {
						expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
					}
					if (identifiersList.size() > 0) {
						castedRecordArguments = identifiersList.toArray(new NodeCastedIdentifier[identifiersList.size()]);
					}
				}
				castedVariableName = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
			}

			boolean hasCast = castedRecordArguments != null || castedVariableName != null;
			int dimension = parameterizedType.getDimension();
			if (!hasCast && parameterizedType.parameters == null) {
				castedVariableName = parameterizedType.cellTypeRoot != null ? parameterizedType.cellTypeRoot.name : parameterizedType.name;
				parameterizedType = Type.varType;
			}

			if (hasCast || !requireCast) {
				if (hasCast) {
					tokenizer.commit();
					NodeCastedIdentifier identifier = new NodeCastedIdentifier(parameterizedType, castedVariableName, castedRecordArguments);
					identifier.setToken(tokenizer.getBlockToken(identifierToken));
					return identifier;
				} else if (castedRecordArguments == null) {
					if (UNNAMED.equals(castedVariableName) || createOnlyCastedIdentifier) {
						tokenizer.commit();
						// @unnamed
						NodeCastedIdentifier identifier = new NodeCastedIdentifier(Type.varType, castedVariableName, null);
						identifier.setToken(tokenizer.getBlockToken(identifierToken));
						return identifier;
					} else if (parameterizedType == null || parameterizedType.parameters == null) {
						tokenizer.commit();
						NodeIdentifier identifier = new NodeIdentifier(castedVariableName, dimension);
						identifier.setToken(tokenizer.getBlockToken(identifierToken));
						return identifier;
					}
				}
			}
		}
		tokenizer.rollback();
		return null;
	}

	public static void visitCastedIdentifiers(Tokenizer tokenizer, List<NodeCastedIdentifier> identifiers, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		HiNode identifier = visitIdentifier(tokenizer, ctx, true, false, true);
		if (identifier != null) {
			identifiers.add((NodeCastedIdentifier) identifier);
			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				identifier = visitIdentifier(tokenizer, ctx, true, false, true);
				if (identifier != null) {
					identifiers.add((NodeCastedIdentifier) identifier);
				} else {
					tokenizer.error("identifier is expected");
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
