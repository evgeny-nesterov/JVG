package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

import static ru.nest.hiscript.tokenizer.WordType.*;

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

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		if (visitWord(ANNOTATION_INTERFACE, tokenizer) != null) {
			tokenizer.commit();

			Modifiers.Changeable modifiers = annotatedModifiers.getModifiers().change();
			checkModifiers(tokenizer, modifiers, annotatedModifiers.getToken(), PUBLIC, PROTECTED, PRIVATE, STATIC, ABSTRACT);
			modifiers.setStatic(true);
			modifiers.setAbstract(true);

			String name = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
			if (name == null) {
				name = "Null" + new Object().hashCode();
				tokenizer.error("annotation class name is expected");
			}

			expectSymbol(tokenizer, SymbolType.BRACES_LEFT);

			ctx.clazz = new HiClassAnnotation(ctx.getClassLoader(), ctx.enclosingClass, name, ctx.classLocationType);
			ctx.clazz.isInterface = true;
			ctx.clazz.modifiers = modifiers;
			ctx.clazz.annotations = annotatedModifiers.getAnnotations();

			visitContent(tokenizer, ctx);

			expectSymbol(tokenizer, SymbolType.BRACES_RIGHT);
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

	public HiMethod visitMethod(Tokenizer tokenizer, CompileClassContext ctx, WordType... allowed) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);
		HiClass clazz = ctx.clazz;

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		Type type = visitType(tokenizer, true, ctx.getEnv());
		if (type != null) {
			int dimension = visitDimension(tokenizer);

			// @generics
			NodeGenerics generics = GenericsParseRule.getInstance().visit(tokenizer, ctx);
			if (generics != null) {
				generics.setSourceType(NodeGeneric.GenericSourceType.method);
			}

			type = Type.getArrayType(type, dimension, ctx.getEnv());

			String name = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
			if (name != null) {
				if (visitSymbol(tokenizer, SymbolType.PARENTHESES_LEFT) != null) {
					tokenizer.commit();
					ctx.enter(ContextType.METHOD, startToken);

					checkModifiers(tokenizer, annotatedModifiers.getModifiers(), annotatedModifiers.getToken(), allowed);
					expectSymbol(tokenizer, SymbolType.PARENTHESES_RIGHT);

					HiNode defaultValue = null;
					if (visitWord(DEFAULT, tokenizer) != null) {
						defaultValue = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
						if (defaultValue == null) {
							tokenizer.error("value expected", tokenizer.currentToken());
						}
					}
					expectSymbol(tokenizer, SymbolType.SEMICOLON);

					ctx.exit();
					HiMethod method = new HiMethod(clazz, annotatedModifiers.getAnnotations(), annotatedModifiers.getModifiers(), generics, type, name, (NodeArgument[]) null, null, defaultValue);
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
