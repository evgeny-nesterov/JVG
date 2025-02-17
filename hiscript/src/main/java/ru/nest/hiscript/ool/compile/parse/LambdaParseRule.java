package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.util.ArrayList;
import java.util.List;

import static ru.nest.hiscript.tokenizer.Words.NOT_SERVICE;

public class LambdaParseRule extends ParseRule<HiMethod> {
	private final static LambdaParseRule instance = new LambdaParseRule();

	public static LambdaParseRule getInstance() {
		return instance;
	}

	private LambdaParseRule() {
	}

	@Override
	public HiMethod visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		List<NodeArgument> arguments = new ArrayList<>();
		String argName = visitWord(tokenizer, NOT_SERVICE);
		if (argName != null) {
			NodeArgument argNode = new NodeArgument(Type.varType, argName, null, null);
			argNode.setToken(startToken);
			arguments.add(argNode);
		} else {
			if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) == -1) {
				tokenizer.rollback();
				return null;
			}

			visitArgumentsDefinitions(tokenizer, arguments, ctx);
			if (arguments.size() == 0) {
				Token argToken = startToken(tokenizer);
				argName = visitWord(tokenizer, NOT_SERVICE);
				while (argName != null) {
					NodeArgument argNode = new NodeArgument(Type.varType, argName, null, null);
					argNode.setToken(argToken);
					arguments.add(argNode);
					if (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
						argName = visitWord(NOT_SERVICE, tokenizer);
					} else {
						argName = null;
					}
				}
			}

			if (visitSymbol(tokenizer, Symbols.PARENTHESES_RIGHT) == -1) {
				tokenizer.rollback();
				return null;
			}
		}

		if (visitSymbol(tokenizer, Symbols.REFERENCE) == -1) {
			tokenizer.rollback();
			return null;
		}

		tokenizer.commit();

		HiNode body = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (body == null) {
			body = expectBody(tokenizer, ctx);
		}

		NodeArgument[] argumentsArray = arguments != null ? arguments.toArray(new NodeArgument[arguments.size()]) : null;
		return new HiMethod(argumentsArray, body);
	}
}
