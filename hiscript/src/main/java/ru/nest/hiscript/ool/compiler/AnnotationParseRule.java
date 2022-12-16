package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotationArgument;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.util.ArrayList;
import java.util.List;

public class AnnotationParseRule extends ParseRule<NodeAnnotation> {
	private final static AnnotationParseRule instance = new AnnotationParseRule();

	public static AnnotationParseRule getInstance() {
		return instance;
	}

	private AnnotationParseRule() {
	}

	@Override
	public NodeAnnotation visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		Token startToken = startToken(tokenizer);
		String name = visitAnnotationWord(tokenizer);
		if (name != null) {
			List<NodeAnnotationArgument> args = new ArrayList<>();
			if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
				tokenizer.start();
				String argName = visitWord(NOT_SERVICE, tokenizer);
				if (argName != null && visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
					tokenizer.commit();

					Node argValue = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
					if (argValue == null) {
						throw new ParseException("argument value expected", tokenizer.currentToken());
					}
					args.add(new NodeAnnotationArgument(argName, argValue));

					while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
						argName = expectWord(NOT_SERVICE, tokenizer);
						expectSymbol(tokenizer, Symbols.EQUATE);
						argValue = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
						if (argValue == null) {
							throw new ParseException("argument value expected", tokenizer.currentToken());
						}
						args.add(new NodeAnnotationArgument(argName, argValue));
					}
				} else {
					tokenizer.rollback();

					Node argValue = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
					if (argValue != null) {
						args.add(new NodeAnnotationArgument("value", argValue));
					}
				}
				expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

				NodeAnnotation node = new NodeAnnotation(name, args.toArray(new NodeAnnotationArgument[args.size()]));
				node.setToken(tokenizer.getBlockToken(startToken));
				return node;
			}
		}
		return null;
	}

	public NodeAnnotation[] visitAnnotations(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		List<NodeAnnotation> annotations = null;
		NodeAnnotation annotation = visit(tokenizer, ctx);
		if (annotation != null) {
			annotations = new ArrayList<>(1);
			annotations.add(annotation);
			while ((annotation = visit(tokenizer, ctx)) != null) {
				annotations.add(annotation);
			}
			return annotations.toArray(new NodeAnnotation[annotations.size()]);
		}
		return null;
	}
}
