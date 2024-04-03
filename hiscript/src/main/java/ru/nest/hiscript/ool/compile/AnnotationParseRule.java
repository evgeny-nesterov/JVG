package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.HiNode;
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
	public NodeAnnotation visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		String name = visitAnnotationWord(tokenizer);
		if (name != null) {
			if (name.length() == 0) {
				name = "Empty" + new Object().hashCode();
				tokenizer.error("annotation name is expected");
			}

			List<NodeAnnotationArgument> args = new ArrayList<>();
			if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
				tokenizer.start();

				startToken = startToken(tokenizer);
				String argName = visitWord(NOT_SERVICE, tokenizer);
				if (argName != null && visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
					tokenizer.commit();

					HiNode argValue = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
					if (argValue == null) {
						tokenizer.error("argument value expected");
					}
					args.add(new NodeAnnotationArgument(argName, argValue, tokenizer.getBlockToken(startToken)));

					while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
						startToken = startToken(tokenizer);
						argName = expectWord(NOT_SERVICE, tokenizer);
						expectSymbol(tokenizer, Symbols.EQUATE);
						argValue = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
						if (argValue == null) {
							tokenizer.error("argument value expected");
						}
						args.add(new NodeAnnotationArgument(argName, argValue, tokenizer.getBlockToken(startToken)));
					}
				} else {
					tokenizer.rollback();

					HiNode argValue = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
					if (argValue != null) {
						args.add(new NodeAnnotationArgument("value", argValue, argValue.getToken()));
					}
				}
				expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
				return new NodeAnnotation(name, args.toArray(new NodeAnnotationArgument[args.size()]));
			}
		}
		return null;
	}

	public List<NodeAnnotation> visitAnnotations(Tokenizer tokenizer, CompileClassContext ctx, List<NodeAnnotation> annotations) throws TokenizerException, HiScriptParseException {
		NodeAnnotation annotation = visit(tokenizer, ctx);
		if (annotation != null) {
			if (annotations == null) {
				annotations = new ArrayList<>(1);
			}
			annotations.add(annotation);
			while ((annotation = visit(tokenizer, ctx)) != null) {
				annotations.add(annotation);
			}
			return annotations;
		}
		return null;
	}
}
