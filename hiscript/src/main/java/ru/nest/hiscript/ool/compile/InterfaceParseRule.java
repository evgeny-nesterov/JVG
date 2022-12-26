package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.List;

public class InterfaceParseRule extends ParserUtil {
	private final static InterfaceParseRule instance = new InterfaceParseRule();

	public static InterfaceParseRule getInstance() {
		return instance;
	}

	private InterfaceParseRule() {
	}

	public HiClass visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx);
		if (visitWord(Words.INTERFACE, tokenizer) != null) {
			tokenizer.commit();
			checkModifiers(tokenizer, annotatedModifiers.getModifiers(), PUBLIC, PROTECTED, PRIVATE, STATIC, ABSTRACT);

			String interfaceName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (interfaceName == null) {
				tokenizer.error("interface name is expected");
			}

			// parse 'extends'
			List<Type> interfacesList = null;
			if (visitWord(Words.EXTENDS, tokenizer) != null) {
				Type interfaceType = visitType(tokenizer, false);
				if (interfaceType == null) {
					tokenizer.error("illegal start of type");
				}

				interfacesList = new ArrayList<>(1);
				interfacesList.add(interfaceType);

				while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
					interfaceType = visitType(tokenizer, false);
					if (interfaceType == null) {
						tokenizer.error("illegal start of type");
					}
					interfacesList.add(interfaceType);
				}
			}

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			Type[] interfaces = null;
			if (interfacesList != null) {
				interfaces = new Type[interfacesList.size()];
				interfacesList.toArray(interfaces);
			}

			ctx.clazz = new HiClass(ctx.getClassLoader(), null, ctx.enclosingClass, interfaces, interfaceName, ctx.classType, ctx);
			ctx.clazz.isInterface = true;
			ctx.clazz.modifiers = annotatedModifiers.getModifiers();

			visitContent(tokenizer, ctx);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			ctx.clazz.token = tokenizer.getBlockToken(startToken);
			ctx.clazz.annotations = annotatedModifiers.getAnnotations();
			return ctx.clazz;
		}

		tokenizer.rollback();
		return null;
	}

	public void visitContent(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		HiClass clazz = ctx.clazz;
		while (true) {
			// inner class / interface
			CompileClassContext innerProperties = new CompileClassContext(ctx, clazz, HiClass.CLASS_TYPE_INNER);
			HiClass innerClass = ClassParseRule.getInstance().visit(tokenizer, innerProperties);
			if (innerClass == null) {
				innerClass = InterfaceParseRule.getInstance().visit(tokenizer, innerProperties);
			}
			if (innerClass == null) {
				innerClass = EnumParseRule.getInstance().visit(tokenizer, innerProperties);
			}
			if (innerClass != null) {
				innerClass.enclosingClass = clazz;
				ctx.addClass(innerClass);
				continue;
			}

			// method
			HiMethod method = ClassParseRule.getInstance().visitMethod(tokenizer, ctx, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC, ABSTRACT, NATIVE, DEFAULT);
			if (method != null) {
				ctx.addMethod(method);
				continue;
			}

			// field
			if (visitFields(tokenizer, ctx)) {
				continue;
			}
			break;
		}

		ctx.initClass();
	}

	protected boolean visitFields(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx);
		Type baseType = visitType(tokenizer, true);
		if (baseType != null) {
			Token startToken = startToken(tokenizer);
			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name != null) {
				tokenizer.commit();

				Modifiers modifiers = annotatedModifiers.getModifiers();
				checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC);
				modifiers.setFinal(true);
				modifiers.setStatic(true);

				int addDimension = visitDimension(tokenizer);
				expectSymbol(tokenizer, Symbols.EQUATE);
				HiNode initializer = ExpressionParseRule.getInstance().visit(tokenizer, ctx);

				Type type = Type.getArrayType(baseType, addDimension);
				HiField<?> field = HiField.getField(type, name, initializer, tokenizer.getBlockToken(startToken));
				field.setModifiers(modifiers);
				field.setAnnotations(annotatedModifiers.getAnnotations());

				ctx.addField(field);

				while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
					expectField(tokenizer, baseType, modifiers, ctx);
				}
				expectSymbol(tokenizer, Symbols.SEMICOLON);
				return true;
			}
		}

		tokenizer.rollback();
		return false;
	}

	private void expectField(Tokenizer tokenizer, Type baseType, Modifiers modifiers, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Token startToken = startToken(tokenizer);
		String name = expectWord(Words.NOT_SERVICE, tokenizer);
		int addDimension = visitDimension(tokenizer);
		expectSymbol(tokenizer, Symbols.EQUATE);
		HiNode initializer = ExpressionParseRule.getInstance().visit(tokenizer, ctx);

		Type type = Type.getArrayType(baseType, addDimension);
		HiField<?> field = HiField.getField(type, name, initializer, tokenizer.getBlockToken(startToken));
		field.setModifiers(modifiers);

		ctx.addField(field);
	}
}
