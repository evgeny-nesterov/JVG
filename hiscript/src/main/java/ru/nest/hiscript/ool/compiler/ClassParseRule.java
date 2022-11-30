package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiConstructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
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

import java.util.ArrayList;
import java.util.List;

public class ClassParseRule extends ParserUtil {
	private final static ClassParseRule instance = new ClassParseRule();

	public static ClassParseRule getInstance() {
		return instance;
	}

	private ClassParseRule() {
	}

	public HiClass visit(Tokenizer tokenizer, CompileContext ctx) throws TokenizerException, ParseException {
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
				superClassType = Type.objectType;
			} else {
				superClassType = null;
			}

			// parse 'implements'
			List<Type> interfacesList = null;
			if (visitWord(Words.IMPLEMENTS, tokenizer) != null) {
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

			ctx.clazz = new HiClass(superClassType, ctx.enclosingClass, interfaces, className, ctx.classType);
			ctx.clazz.modifiers = modifiers;

			visitContent(tokenizer, ctx);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			return ctx.clazz;
		}

		tokenizer.rollback();
		return null;
	}

	public void visitContent(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		HiClass clazz = properties.clazz;
		while (true) {
			// inner class / interface
			CompileContext innerProperties = new CompileContext(tokenizer, properties, clazz, HiClass.CLASS_TYPE_INNER);
			HiClass innerClass = ClassParseRule.getInstance().visit(tokenizer, innerProperties);
			if (innerClass == null) {
				innerClass = InterfaceParseRule.getInstance().visit(tokenizer, innerProperties);
			}
			if (innerClass == null) {
				innerClass = EnumParseRule.getInstance().visit(tokenizer, innerProperties);
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
			HiConstructor constructor = visitConstructor(tokenizer, properties);
			if (constructor != null) {
				properties.addConstructor(constructor);
				continue;
			}

			// method
			HiMethod method = visitMethod(tokenizer, properties, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC, ABSTRACT, NATIVE);
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
			HiConstructor defaultConstructor = new HiConstructor(clazz, new Modifiers(), (List<NodeArgument>) null, null, null, BodyConstructorType.NONE);
			properties.addConstructor(defaultConstructor);
		}

		properties.initClass();
	}

	private HiConstructor visitConstructor(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();
		HiClass clazz = properties.clazz;

		Modifiers modifiers = visitModifiers(tokenizer);
		String name = visitWord(Words.NOT_SERVICE, tokenizer);
		if (name != null) {
			if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
				if (!name.equals(clazz.name) || clazz.type == HiClass.CLASS_TYPE_ANONYMOUS) {
					throw new ParseException("invalid method declaration; return type is expected", tokenizer.currentToken());
				}

				tokenizer.commit();
				checkModifiers(tokenizer, modifiers, PUBLIC, PROTECTED, PRIVATE);
				properties.enter(); // before arguments

				// visit arguments
				List<NodeArgument> arguments = new ArrayList<>();
				visitArgumentsDefinitions(tokenizer, arguments, properties);
				for (NodeArgument argument : arguments) {
					properties.addLocalVariable(argument);
				}

				expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
				expectSymbol(tokenizer, Symbols.BRACES_LEFT);

				NodeConstructor enclosingConstructor = null;
				BodyConstructorType bodyConstructorType = BodyConstructorType.NONE;

				// visit super or this constructor invocation
				tokenizer.start();
				try {
					int constructorType = visitServiceWord(tokenizer, THIS, SUPER);
					if (constructorType != -1) {
						if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
							Node[] args = visitArgumentsValues(tokenizer, properties);
							expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
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

				return new HiConstructor(clazz, modifiers, arguments, body, enclosingConstructor, bodyConstructorType);
			}
		}

		tokenizer.rollback();
		return null;
	}

	public HiMethod visitMethod(Tokenizer tokenizer, CompileContext properties, int... allowed) throws TokenizerException, ParseException {
		tokenizer.start();
		HiClass clazz = properties.clazz;

		Modifiers modifiers = visitModifiers(tokenizer);
		Type type = visitType(tokenizer, true);
		if (type == null) {
			if (visitWord(Words.VOID, tokenizer) != null) {
				type = Type.getPrimitiveType("void");
			}
		} else {
			int dimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, dimension);
		}

		if (type != null) {
			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name != null) {
				if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
					tokenizer.commit();
					properties.enter();

					checkModifiers(tokenizer, modifiers, allowed);

					List<NodeArgument> arguments = new ArrayList<>();
					visitArgumentsDefinitions(tokenizer, arguments, properties);
					for (NodeArgument argument : arguments) {
						properties.addLocalVariable(argument);
					}

					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

					Type[] exceptionTypes = null;
					if (visitWordType(tokenizer, Words.THROWS) != -1) {
						Type exceptionType = visitType(tokenizer, true);
						if (exceptionType == null) {
							throw new ParseException("identifier expected", tokenizer.currentToken());
						}
						List<Type> exceptionTypesList = new ArrayList<>(1);
						exceptionTypesList.add(exceptionType);
						if (checkSymbol(tokenizer, Symbols.COMMA) != -1) {
							tokenizer.nextToken();
							exceptionType = visitType(tokenizer, true);
							if (exceptionType == null) {
								throw new ParseException("identifier expected", tokenizer.currentToken());
							}
							exceptionTypesList.add(exceptionType);
						}
						exceptionTypes = exceptionTypesList.toArray(new Type[exceptionTypesList.size()]);
					}

					Node body = null;
					if (modifiers.isNative() || modifiers.isAbstract()) {
						expectSymbol(tokenizer, Symbols.SEMICOLON);
					} else {
						if (checkSymbol(tokenizer, Symbols.SEMICOLON) != -1) {
							tokenizer.nextToken();
							modifiers.setAbstract(true);
						} else {
							expectSymbol(tokenizer, Symbols.BRACES_LEFT);
							body = BlockParseRule.getInstance().visit(tokenizer, properties);
							expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
						}
					}

					properties.exit();
					return new HiMethod(clazz, modifiers, type, name, arguments, exceptionTypes, body);
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	public boolean visitFields(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
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
					HiField<?> field = HiField.getField(type, name, initializer);
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
		HiField<?> field = HiField.getField(type, name, initializer);
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
