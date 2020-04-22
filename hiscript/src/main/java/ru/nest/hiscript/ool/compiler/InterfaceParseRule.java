package ru.nest.hiscript.ool.compiler;

import java.util.ArrayList;
import java.util.List;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Field;
import ru.nest.hiscript.ool.model.Method;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class InterfaceParseRule extends ParserUtil {
	private final static InterfaceParseRule instance = new InterfaceParseRule();

	public static InterfaceParseRule getInstance() {
		return instance;
	}

	private InterfaceParseRule() {
	}

	public Clazz visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();

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

				interfacesList = new ArrayList<Type>(1);
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

			properties.clazz = new Clazz(null, properties.enclosingClass, interfaces, interfaceName, properties.classType);
			properties.clazz.isInterface = true;
			properties.clazz.modifiers = modifiers;

			visitContent(tokenizer, properties);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			return properties.clazz;
		}

		tokenizer.rollback();
		return null;
	}

	public void visitContent(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		Clazz clazz = properties.clazz;
		while (true) {
			// inner class / interface
			CompileContext innerProperties = new CompileContext(tokenizer, properties, clazz, Clazz.CLASS_TYPE_INNER);
			Clazz innerClass = ClassParseRule.getInstance().visit(tokenizer, innerProperties);
			if (innerClass == null) {
				innerClass = InterfaceParseRule.getInstance().visit(tokenizer, innerProperties);
			}

			if (innerClass != null) {
				innerClass.enclosingClass = clazz;
				properties.addClass(innerClass);
				continue;
			}

			// method
			Method method = visitMethod(tokenizer, properties);
			if (method != null) {
				properties.addMethod(method);
				continue;
			}

			// field
			if (visitFields(tokenizer, properties)) {
				continue;
			}

			break;
		}

		properties.initClass();
	}

	private Method visitMethod(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();
		Clazz clazz = properties.clazz;

		Modifiers modifiers = visitModifiers(tokenizer);
		Type type = visitType(tokenizer, true);
		if (type == null) {
			if (visitWord(Words.VOID, tokenizer) != null) {
				type = Type.getType("void");
			}
		} else {
			int dimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, dimension);
		}

		if (type != null) {
			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name != null) {
				if (visitSymbol(tokenizer, Symbols.PARANTHESIS_LEFT) != -1) {
					tokenizer.commit();
					properties.enter();

					checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC, ABSTRACT, NATIVE);
					modifiers.setAbstract(true);

					List<NodeArgument> arguments = new ArrayList<NodeArgument>();
					visitArguments(tokenizer, arguments, properties);
					for (NodeArgument argument : arguments) {
						properties.addLocalVariable(argument);
					}

					expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);

					// TODO: visit throws

					expectSymbol(tokenizer, Symbols.SEMICOLON);

					properties.exit();
					return new Method(clazz, modifiers, type, name, arguments, null);
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	private boolean visitFields(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
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
				Node initializer = ExpressionParseRule.getInstance().visit(tokenizer, properties);

				Type type = Type.getArrayType(baseType, addDimension);
				Field<?> field = Field.getField(type, name, initializer);
				field.setModifiers(modifiers);

				properties.addField(field);

				while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
					expectField(tokenizer, baseType, modifiers, properties);
				}
				expectSymbol(tokenizer, Symbols.SEMICOLON);
				return true;
			}
		}

		tokenizer.rollback();
		return false;
	}

	private void expectField(Tokenizer tokenizer, Type baseType, Modifiers modifiers, CompileContext properties) throws TokenizerException, ParseException {
		String name = expectWord(Words.NOT_SERVICE, tokenizer);
		int addDimension = visitDimension(tokenizer);
		expectSymbol(tokenizer, Symbols.EQUATE);
		Node initializer = ExpressionParseRule.getInstance().visit(tokenizer, properties);

		Type type = Type.getArrayType(baseType, addDimension);
		Field<?> field = Field.getField(type, name, initializer);
		field.setModifiers(modifiers);

		properties.addField(field);
	}
}
