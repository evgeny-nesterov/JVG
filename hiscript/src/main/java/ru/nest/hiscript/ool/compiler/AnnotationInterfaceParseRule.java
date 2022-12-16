package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class AnnotationInterfaceParseRule extends ParserUtil {
	private final static AnnotationInterfaceParseRule instance = new AnnotationInterfaceParseRule();

	public static AnnotationInterfaceParseRule getInstance() {
		return instance;
	}

	private AnnotationInterfaceParseRule() {
	}

	public HiClass visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);

		Modifiers modifiers = visitModifiers(tokenizer);
		if (visitWord(Words.ANNOTATION_INTERFACE, tokenizer) != null) {
			tokenizer.commit();
			checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, STATIC, ABSTRACT);
			modifiers.setAbstract(true);

			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name == null) {
				throw new ParseException("annotation class name is expected", tokenizer.currentToken());
			}

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			ctx.clazz = new HiClassAnnotation(name, ctx.classType);
			ctx.clazz.isInterface = true;
			ctx.clazz.modifiers = modifiers;

			visitContent(tokenizer, ctx);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			ctx.clazz.token = tokenizer.getBlockToken(startToken);
			return ctx.clazz;
		}

		tokenizer.rollback();
		return null;
	}

	public void visitContent(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		while (true) {
			// method
			HiMethod method = visitMethod(tokenizer, ctx, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC, ABSTRACT);
			if (method != null) {
				ctx.addMethod(method);
				continue;
			}

			// field
			if (InterfaceParseRule.getInstance().visitFields(tokenizer, ctx)) {
				continue;
			}
			break;
		}

		ctx.initClass();
	}

	public HiMethod visitMethod(Tokenizer tokenizer, CompileClassContext ctx, int... allowed) throws TokenizerException, ParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);
		HiClass clazz = ctx.clazz;

		Modifiers modifiers = visitModifiers(tokenizer);
		Type type = visitType(tokenizer, true);
		if (type != null) {
			int dimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, dimension);

			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name != null) {
				if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
					tokenizer.commit();
					ctx.enter(RuntimeContext.METHOD);

					checkModifiers(tokenizer, modifiers, allowed);
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

					Node defaultValue = null;
					if (visitWord(Words.DEFAULT, tokenizer) != null) {
						defaultValue = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
					}
					expectSymbol(tokenizer, Symbols.SEMICOLON);

					ctx.exit();
					return new HiMethod(clazz, modifiers, type, name, (NodeArgument[]) null, null, defaultValue, tokenizer.getBlockToken(startToken));
				}
			}
		}

		tokenizer.rollback();
		return null;
	}
}
