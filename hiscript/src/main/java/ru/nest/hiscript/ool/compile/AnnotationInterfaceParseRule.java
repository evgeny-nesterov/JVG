package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
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

	public HiClass visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx);
		if (visitWord(Words.ANNOTATION_INTERFACE, tokenizer) != null) {
			tokenizer.commit();

			Modifiers modifiers = annotatedModifiers.getModifiers();
			checkModifiers(tokenizer, modifiers, annotatedModifiers.getToken(), PUBLIC, PROTECTED, PRIVATE, STATIC, ABSTRACT);
			modifiers.setStatic(true);
			modifiers.setAbstract(true);

			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name == null) {
				name = "Null" + new Object().hashCode();
				tokenizer.error("annotation class name is expected");
			}

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			ctx.clazz = new HiClassAnnotation(ctx.getClassLoader(), ctx.enclosingClass, name, ctx.classType);
			ctx.clazz.isInterface = true;
			ctx.clazz.modifiers = modifiers;
			ctx.clazz.annotations = annotatedModifiers.getAnnotations();

			visitContent(tokenizer, ctx);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			ctx.clazz.setToken(tokenizer.getBlockToken(startToken));
			return ctx.clazz;
		}

		tokenizer.rollback();
		return null;
	}

	public void visitContent(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		while (true) {
			// method
			HiMethod method = visitMethod(tokenizer, ctx, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC, ABSTRACT);
			if (method != null) {
				ctx.addMethod(method);
				continue;
			}

			// field
			if (ClassParseRule.getInstance().visitFields(tokenizer, ctx)) {
				continue;
			}
			break;
		}

		ctx.initClass();
	}

	public HiMethod visitMethod(Tokenizer tokenizer, CompileClassContext ctx, int... allowed) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);
		HiClass clazz = ctx.clazz;

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx);
		Type type = visitType(tokenizer, true);
		if (type != null) {
			int dimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, dimension);

			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name != null) {
				if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
					tokenizer.commit();
					ctx.enter(RuntimeContext.METHOD, startToken);

					checkModifiers(tokenizer, annotatedModifiers.getModifiers(), annotatedModifiers.getToken(), allowed);
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

					HiNode defaultValue = null;
					if (visitWord(Words.DEFAULT, tokenizer) != null) {
						defaultValue = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
					}
					expectSymbol(tokenizer, Symbols.SEMICOLON);

					ctx.exit();
					HiMethod method = new HiMethod(clazz, annotatedModifiers.getAnnotations(), annotatedModifiers.getModifiers(), type, name, (NodeArgument[]) null, null, defaultValue);
					method.isAnnotationArgument = true;
					method.setToken(tokenizer.getBlockToken(startToken));
					return method;
				}
			}
		}

		tokenizer.rollback();
		return null;
	}
}
