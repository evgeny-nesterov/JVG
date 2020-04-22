package ru.nest.hiscript.ool.compiler;

import java.util.ArrayList;
import java.util.List;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Constructor;
import ru.nest.hiscript.ool.model.Constructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.Field;
import ru.nest.hiscript.ool.model.Method;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.NodeInitializer;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class ClassParseRule extends ParserUtil {
	private final static ClassParseRule instance = new ClassParseRule();

	public static ClassParseRule getInstance() {
		return instance;
	}

	private ClassParseRule() {
	}

	public Clazz visit(Tokenizer tokenizer, CompileContext ctx) throws TokenizerException, ParseException {
		tokenizer.start();

		Modifiers modifiers = visitModifiers(tokenizer);
		if (visitWord(Words.CLASS, tokenizer) != null) {
			tokenizer.commit();
			checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC, ABSTRACT);

			String className = visitWord(Words.NOT_SERVICE, tokenizer);
			if (className == null) {
				throw new ParseException("class name is expected", tokenizer.currentToken());
			}

			// parse 'extends'
			Type superClassType;
			if (visitWord(Words.EXTENDS, tokenizer) != null) {
				superClassType = visitType(tokenizer, false);
				if (superClassType == null) {
					throw new ParseException("illegal start of type", tokenizer.currentToken());
				}
			} else if (!"Object".equals(className)) {
				superClassType = Type.ObjectType;
			} else {
				superClassType = null;
			}

			// parse 'implements'
			List<Type> interfacesList = null;
			if (visitWord(Words.IMPLEMENS, tokenizer) != null) {
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

			ctx.clazz = new Clazz(superClassType, ctx.enclosingClass, interfaces, className, ctx.classType);
			ctx.clazz.modifiers = modifiers;

			visitContent(tokenizer, ctx);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			return ctx.clazz;
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
				if (!clazz.isTopLevel() && !clazz.isStatic()) {
					if (innerClass.isInterface) {
						throw new ParseException("The member interface " + innerClass.fullName + " can only be defined inside a top-level class or interface", tokenizer.currentToken());
					}

					// check on valid static modifier
					if (innerClass.isStatic() && !(innerClass.topClass != null && innerClass.topClass.fullName.equals("@root"))) {
						throw new ParseException("The member type " + innerClass.fullName + " cannot be declared static; static types can only be declared in static or top level types", tokenizer.currentToken());
					}
				}

				innerClass.enclosingClass = clazz;
				properties.addClass(innerClass);
				continue;
			}

			// constructor
			Constructor constructor = visitConstructor(tokenizer, properties);
			if (constructor != null) {
				properties.addConstructor(constructor);
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

			// block
			NodeInitializer block = visitBlock(tokenizer, properties);
			if (block != null) {
				properties.addBlockInitializer(block);
				continue;
			}

			break;
		}

		if (properties.constructors == null) {
			Constructor defaultConstructor = new Constructor(clazz, new Modifiers(), (List<NodeArgument>) null, null, null, BodyConstructorType.NONE);
			properties.addConstructor(defaultConstructor);
		}

		properties.initClass();
	}

	private Constructor visitConstructor(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();
		Clazz clazz = properties.clazz;

		Modifiers modifiers = visitModifiers(tokenizer);
		String name = visitWord(Words.NOT_SERVICE, tokenizer);
		if (name != null) {
			if (visitSymbol(tokenizer, Symbols.PARANTHESIS_LEFT) != -1) {
				if (!name.equals(clazz.name) || clazz.type == Clazz.CLASS_TYPE_ANONYMOUS) {
					throw new ParseException("invalid method declaration; return type is expected", tokenizer.currentToken());
				}

				tokenizer.commit();
				checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE);
				properties.enter(); // before arguments

				// visit arguments
				List<NodeArgument> arguments = new ArrayList<NodeArgument>();
				visitArguments(tokenizer, arguments, properties);
				for (NodeArgument argument : arguments) {
					properties.addLocalVariable(argument);
				}

				expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);
				expectSymbol(tokenizer, Symbols.BRACES_LEFT);

				NodeConstructor enclosingConstructor = null;
				BodyConstructorType bodyConstructorType = BodyConstructorType.NONE;

				// visit super or this constructor invocation
				tokenizer.start();
				try {
					int constructorType = visitServiceWord(tokenizer, THIS, SUPER);
					if (constructorType != -1) {
						if (visitSymbol(tokenizer, Symbols.PARANTHESIS_LEFT) != -1) {
							Node[] args = visitArguments(tokenizer, properties);
							expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);
							expectSymbol(tokenizer, Symbols.SEMICOLON);

							Type type;
							if (constructorType == SUPER) {
								type = clazz.superClassType;
								bodyConstructorType = BodyConstructorType.SUPER;
							} else {
								type = Type.getType(clazz);
								bodyConstructorType = BodyConstructorType.THIS;
							}

							NodeType nodeType = new NodeType(type);
							enclosingConstructor = new NodeConstructor(nodeType, args);
						}
					}
				} finally {
					if (enclosingConstructor != null) {
						tokenizer.commit();
					} else {
						tokenizer.rollback();
					}
				}

				// visit body
				Node body = BlockParseRule.getInstance().visit(tokenizer, properties);
				expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
				properties.exit();

				return new Constructor(clazz, modifiers, arguments, body, enclosingConstructor, bodyConstructorType);
			}
		}

		tokenizer.rollback();
		return null;
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
					checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC, ABSTRACT, NATIVE);
					properties.enter();

					List<NodeArgument> arguments = new ArrayList<NodeArgument>();
					visitArguments(tokenizer, arguments, properties);
					for (NodeArgument argument : arguments) {
						properties.addLocalVariable(argument);
					}

					expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);

					// TODO: visit throws

					Node body = null;
					if (modifiers.isNative() || modifiers.isAbstract()) {
						expectSymbol(tokenizer, Symbols.SEMICOLON);
					} else {
						expectSymbol(tokenizer, Symbols.BRACES_LEFT);
						body = BlockParseRule.getInstance().visit(tokenizer, properties);
						expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
					}

					properties.exit();
					return new Method(clazz, modifiers, type, name, arguments, body);
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
				int addDimension = visitDimension(tokenizer);

				Node initializer = null;
				boolean isField = false;
				if (checkSymbol(tokenizer, Symbols.SEMICOLON, Symbols.COMMA) != -1) {
					isField = true;
				} else if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
					initializer = ExpressionParseRule.getInstance().visit(tokenizer, properties);
					isField = true;
				}

				if (isField) {
					tokenizer.commit();
					checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC);

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
		}

		tokenizer.rollback();
		return false;
	}

	private void expectField(Tokenizer tokenizer, Type baseType, Modifiers modifiers, CompileContext properties) throws TokenizerException, ParseException {
		String name = expectWord(Words.NOT_SERVICE, tokenizer);
		int addDimension = visitDimension(tokenizer);

		Node initializer = null;
		if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
			initializer = ExpressionParseRule.getInstance().visit(tokenizer, properties);
		}

		Type type = Type.getArrayType(baseType, addDimension);
		Field<?> field = Field.getField(type, name, initializer);
		field.setModifiers(modifiers);

		properties.addField(field);
	}

	private NodeInitializer visitBlock(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();

		boolean isStatic = visitWord(tokenizer, STATIC) != null;
		if (visitSymbol(tokenizer, Symbols.BRACES_LEFT) != -1) {
			tokenizer.commit();
			properties.enter();

			NodeBlock block = BlockParseRule.getInstance().visit(tokenizer, properties);
			if (block != null) {
				block.setStatic(isStatic);
			}
			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			properties.exit();
			return block;
		}

		tokenizer.rollback();
		return null;
	}
}
