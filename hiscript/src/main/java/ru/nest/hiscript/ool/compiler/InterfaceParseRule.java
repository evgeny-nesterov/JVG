package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
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

	public HiClass visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);

		Modifiers modifiers = visitModifiers(tokenizer);
		if (visitWord(Words.INTERFACE, tokenizer) != null) {
			tokenizer.commit();
			checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, STATIC, ABSTRACT);

			String interfaceName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (interfaceName == null) {
				throw new ParseException("interface name is expected", tokenizer.currentToken());
			}

			// parse 'extends'
			List<Type> interfacesList = null;
			if (visitWord(Words.EXTENDS, tokenizer) != null) {
				Type interfaceType = visitType(tokenizer, false);
				if (interfaceType == null) {
					throw new ParseException("illegal start of type", tokenizer.currentToken());
				}

				interfacesList = new ArrayList<>(1);
				interfacesList.add(interfaceType);

				while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
					interfaceType = visitType(tokenizer, false);
					if (interfaceType == null) {
						throw new ParseException("illegal start of type", tokenizer.currentToken());
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

			ctx.clazz = new HiClass(null, ctx.enclosingClass, interfaces, interfaceName, ctx.classType);
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

	protected boolean visitFields(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		tokenizer.start();

		Modifiers modifiers = visitModifiers(tokenizer);
		Type baseType = visitType(tokenizer, true);
		if (baseType != null) {
			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name != null) {
				tokenizer.commit();

				checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC);
				modifiers.setFinal(true);
				modifiers.setStatic(true);

				int addDimension = visitDimension(tokenizer);
				expectSymbol(tokenizer, Symbols.EQUATE);
				Node initializer = ExpressionParseRule.getInstance().visit(tokenizer, ctx);

				Type type = Type.getArrayType(baseType, addDimension);
				HiField<?> field = HiField.getField(type, name, initializer);
				field.setModifiers(modifiers);

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

	private void expectField(Tokenizer tokenizer, Type baseType, Modifiers modifiers, CompileClassContext ctx) throws TokenizerException, ParseException {
		String name = expectWord(Words.NOT_SERVICE, tokenizer);
		int addDimension = visitDimension(tokenizer);
		expectSymbol(tokenizer, Symbols.EQUATE);
		Node initializer = ExpressionParseRule.getInstance().visit(tokenizer, ctx);

		Type type = Type.getArrayType(baseType, addDimension);
		HiField<?> field = HiField.getField(type, name, initializer);
		field.setModifiers(modifiers);

		ctx.addField(field);
	}
}
