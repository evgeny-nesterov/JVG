package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotationArgument;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.util.ArrayList;
import java.util.List;

import static ru.nest.hiscript.tokenizer.WordType.NOT_SERVICE;
import static ru.nest.hiscript.tokenizer.WordType.UNNAMED_VARIABLE;

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
			if (visitSymbol(tokenizer, SymbolType.PARENTHESES_LEFT) != null) {
				tokenizer.start();

				startToken = startToken(tokenizer);
				String argName = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
				if (argName != null && visitSymbol(tokenizer, SymbolType.EQUATE) != null) {
					tokenizer.commit();

					HiNode argValue = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
					if (argValue == null) {
						argValue = EmptyNode.getInstance();
						tokenizer.error("argument value expected");
					}
					args.add(new NodeAnnotationArgument(argName, argValue, tokenizer.getBlockToken(startToken)));

					while (visitSymbol(tokenizer, SymbolType.COMMA) != null) {
						startToken = startToken(tokenizer);
						argName = expectWords(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
						expectSymbol(tokenizer, SymbolType.EQUATE);
						argValue = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
						if (argValue == null) {
							tokenizer.error("argument value expected");
						}
						args.add(new NodeAnnotationArgument(argName, argValue, tokenizer.getBlockToken(startToken)));
					}
				} else {
					tokenizer.rollback();

					HiNode argValue = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
					if (argValue != null) {
						args.add(new NodeAnnotationArgument("value", argValue, argValue.getToken()));
					}
				}
				expectSymbol(tokenizer, SymbolType.PARENTHESES_RIGHT);
			}
			return new NodeAnnotation(name, args.toArray(new NodeAnnotationArgument[args.size()]));
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
