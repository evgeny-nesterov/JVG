package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeCatch;
import ru.nest.hiscript.ool.model.nodes.NodeDeclaration;
import ru.nest.hiscript.ool.model.nodes.NodeTry;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.util.ArrayList;
import java.util.List;

import static ru.nest.hiscript.tokenizer.Words.*;

public class TryParseRule extends ParseRule<NodeTry> {
	private final static TryParseRule instance = new TryParseRule();

	public static TryParseRule getInstance() {
		return instance;
	}

	private TryParseRule() {
	}

	@Override
	public NodeTry visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(TRY, tokenizer) != null) {
			NodeDeclaration[] resources = null;
			if (checkSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
				tokenizer.nextToken();

				List<NodeDeclaration> resourcesList = new ArrayList<>(1);
				NodeDeclaration resource = DeclarationParseRule.getInstance().visitSingle(tokenizer, ctx, true);
				if (resource != null) {
					resourcesList.add(resource);
				} else {
					tokenizer.error("declaration expected");
				}
				while (checkSymbol(tokenizer, Symbols.SEMICOLON) != -1) {
					tokenizer.nextToken();
					resource = DeclarationParseRule.getInstance().visitSingle(tokenizer, ctx, true);
					if (resource != null) {
						resourcesList.add(resource);
					} else {
						tokenizer.error("declaration expected");
					}
				}
				resources = resourcesList.toArray(new NodeDeclaration[resourcesList.size()]);
				expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
			}

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);
			HiNode tryBody = BlockParseRule.getInstance().visit(tokenizer, ctx);
			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			List<NodeCatch> catchNodes = null;
			while (true) {
				HiNode catchBody;
				List<Type> excTypes = new ArrayList<>(1);
				String excName;
				Token startCatchToken = startToken(tokenizer);
				if (visitWord(CATCH, tokenizer) != null) {
					expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);

					AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
					checkModifiers(tokenizer, annotatedModifiers.getModifiers(), annotatedModifiers.getToken(), FINAL);

					Type excType = visitObjectType(tokenizer, ctx.getEnv());
					// TODO check excType extends Exception
					excTypes.add(excType);
					while (visitSymbol(tokenizer, Symbols.BITWISE_OR) != -1) {
						excType = visitObjectType(tokenizer, ctx.getEnv());
						// TODO check excType extends Exception
						excTypes.add(excType);
					}
					excName = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
					if (excName == null) {
						tokenizer.error("identifier is expected");
					}
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

					expectSymbol(tokenizer, Symbols.BRACES_LEFT);
					catchBody = BlockParseRule.getInstance().visit(tokenizer, ctx);
					expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

					if (catchNodes == null) {
						catchNodes = new ArrayList<>(1);
					}

					if (excName != null) {
						NodeCatch catchNode = new NodeCatch(excTypes != null ? excTypes.toArray(new Type[excTypes.size()]) : null, catchBody, excName, annotatedModifiers.getModifiers(), annotatedModifiers.getAnnotations());
						catchNode.setToken(tokenizer.getBlockToken(startCatchToken));
						catchNodes.add(catchNode);
					}
				} else {
					break;
				}
			}

			HiNode finallyBody = null;
			if (visitWord(FINALLY, tokenizer) != null) {
				expectSymbol(tokenizer, Symbols.BRACES_LEFT);
				finallyBody = BlockParseRule.getInstance().visit(tokenizer, ctx);
				expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			}
			return new NodeTry(tryBody, catchNodes != null ? catchNodes.toArray(new NodeCatch[catchNodes.size()]) : null, finallyBody, resources);
		}
		return null;
	}
}
