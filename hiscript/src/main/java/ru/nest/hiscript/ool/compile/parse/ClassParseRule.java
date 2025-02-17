package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseVisitor;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiConstructor.BodyConstructorType;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.NodeInitializer;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.List;

import static ru.nest.hiscript.tokenizer.Words.*;

public class ClassParseRule extends ParserUtil {
	private final static ClassParseRule instance = new ClassParseRule();

	public static ClassParseRule getInstance() {
		return instance;
	}

	private ClassParseRule() {
	}

	public HiClass visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		int classType = visitWordType(tokenizer, Words.CLASS, Words.INTERFACE);
		if (classType != -1) {
			tokenizer.commit();

			boolean isInterface = classType == Words.INTERFACE;
			checkModifiers(tokenizer, annotatedModifiers.getModifiers(), annotatedModifiers.getToken(), PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC, ABSTRACT);

			String className = visitWord(Words.NOT_SERVICE, tokenizer);
			if (className == null) {
				tokenizer.error("class name is expected");
				className = "";
			}

			// @generics
			NodeGenerics generics = GenericsParseRule.getInstance().visit(tokenizer, ctx);
			if (generics != null) {
				generics.setSourceType(NodeGeneric.GenericSourceType.classSource);
			}

			// parse 'extends'
			List<Type> superClassesList = null;
			if (visitWord(Words.EXTENDS, tokenizer) != null) {
				Type superClassType = visitType(tokenizer, false, ctx.getEnv());
				if (superClassType != null) {
					superClassesList = new ArrayList<>(1);
					superClassesList.add(superClassType);
					while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
						superClassType = visitType(tokenizer, false, ctx.getEnv());
						if (superClassType != null) {
							superClassesList.add(superClassType);
						} else {
							tokenizer.error("illegal start of type");
						}
					}
				} else {
					tokenizer.error("illegal start of type");
				}
			}

			// parse 'implements'
			List<Type> interfacesList = null;
			if (visitWord(Words.IMPLEMENTS, tokenizer) != null) {
				Type interfaceType = visitType(tokenizer, false, ctx.getEnv());
				if (interfaceType != null) {
					interfacesList = new ArrayList<>(1);
					interfacesList.add(interfaceType);
					while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
						interfaceType = visitType(tokenizer, false, ctx.getEnv());
						if (interfaceType != null) {
							interfacesList.add(interfaceType);
						} else {
							tokenizer.error("illegal start of type");
						}
					}
				} else {
					tokenizer.error("illegal start of type");
				}
			}

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			if (isInterface) {
				Type[] interfaces = null;
				if (superClassesList != null) {
					interfaces = new Type[superClassesList.size()];
					superClassesList.toArray(interfaces);
				}

				if (interfacesList != null) {
					tokenizer.error("interface cannot implements another interfaces");
				}

				ctx.clazz = new HiClass(ctx.getClassLoader(), null, ctx.enclosingClass, interfaces, className, generics, ctx.classType, ctx);
			} else {
				Type superClassType = superClassesList != null ? superClassesList.get(0) : null;
				if (superClassesList != null && superClassesList.size() > 1) {
					tokenizer.error("cannot extends multiple classes");
				}

				Type[] interfaces = null;
				if (interfacesList != null) {
					interfaces = new Type[interfacesList.size()];
					interfacesList.toArray(interfaces);
				}

				ctx.clazz = new HiClass(ctx.getClassLoader(), superClassType, ctx.enclosingClass, interfaces, className, generics, ctx.classType, ctx);
			}
			ctx.clazz.isInterface = isInterface;
			ctx.clazz.modifiers = annotatedModifiers.getModifiers();

			visitContent(tokenizer, ctx, null);

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			ctx.clazz.setToken(tokenizer.getBlockToken(startToken));
			ctx.clazz.annotations = annotatedModifiers.getAnnotations();
			return ctx.clazz;
		}

		tokenizer.rollback();
		return null;
	}

	public void visitContent(Tokenizer tokenizer, CompileClassContext ctx, ParseVisitor visitor) throws TokenizerException, HiScriptParseException {
		while (visitContentElement(tokenizer, ctx, visitor)) ;
		ctx.initClass();
	}

	public boolean visitContentElement(Tokenizer tokenizer, CompileClassContext ctx, ParseVisitor visitor) throws TokenizerException, HiScriptParseException {
		HiClass clazz = ctx.clazz;
		Type type = ctx.type;

		if (visitor != null && visitor.visit(tokenizer, ctx)) {
			return true;
		}

		// inner class / interface
		HiClass innerClass = ClassParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, clazz, type, HiClass.CLASS_TYPE_INNER));
		if (innerClass == null) {
			innerClass = EnumParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, clazz, type, HiClass.CLASS_TYPE_INNER));
		}
		if (innerClass == null) {
			innerClass = RecordParseRule.getInstance().visit(tokenizer, new CompileClassContext(ctx, clazz, type, HiClass.CLASS_TYPE_INNER));
		}
		if (innerClass != null) {
			// TODO keep in class only runtime annotations
			innerClass.enclosingClass = clazz;
			ctx.addClass(innerClass);
			return true;
		}

		// constructor
		HiConstructor constructor = visitConstructor(tokenizer, ctx);
		if (constructor != null) {
			ctx.addConstructor(constructor);
			return true;
		}

		// method
		HiMethod method = visitMethod(tokenizer, ctx, PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC, ABSTRACT, NATIVE, DEFAULT, SYNCHRONIZED);
		if (method != null) {
			// TODO keep in method only runtime annotations
			ctx.addMethod(method);
			return true;
		}

		// field
		if (visitFields(tokenizer, ctx)) {
			return true;
		}

		// block
		NodeInitializer block = visitBlock(tokenizer, ctx);
		if (block != null) {
			ctx.addBlockInitializer(block);
			return true;
		}
		return false;
	}

	private HiConstructor visitConstructor(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);
		HiClass clazz = ctx.clazz;
		Type type = ctx.type;

		// @generics
		NodeGenerics generics = GenericsParseRule.getInstance().visit(tokenizer, ctx);
		if (generics != null) {
			generics.setSourceType(NodeGeneric.GenericSourceType.constructor);
		}

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, true);
		String name = visitWord(Words.NOT_SERVICE, tokenizer);
		if (name != null) {
			if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
				if (!name.equals(clazz.name) || clazz.type == HiClass.CLASS_TYPE_ANONYMOUS) {
					tokenizer.error("invalid method declaration; return type is expected");
				}

				tokenizer.commit();
				checkModifiers(tokenizer, annotatedModifiers.getModifiers(), annotatedModifiers.getToken(), PUBLIC, PROTECTED, PRIVATE);
				ctx.enter(RuntimeContext.CONSTRUCTOR, startToken); // before arguments

				// visit arguments
				List<NodeArgument> arguments = new ArrayList<>();
				visitArgumentsDefinitions(tokenizer, arguments, ctx);

				expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

				Type[] exceptionTypes = visitExceptionTypes(tokenizer, ctx);

				expectSymbol(tokenizer, Symbols.BRACES_LEFT);

				NodeConstructor enclosingConstructor = null;
				BodyConstructorType bodyConstructorType = BodyConstructorType.NONE;

				// visit super or this constructor invocation
				tokenizer.start();
				try {
					int constructorType = visitServiceWord(tokenizer, THIS, SUPER);
					if (constructorType != -1) {
						if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
							HiNode[] args = visitArgumentsValues(tokenizer, ctx);
							expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
							expectSymbol(tokenizer, Symbols.SEMICOLON);

							if (constructorType == SUPER) {
								type = clazz.superClassType != null ? clazz.superClassType : Type.objectType;
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
				HiNode body = BlockParseRule.getInstance().visit(tokenizer, ctx);
				expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
				ctx.exit();

				HiConstructor constructor = new HiConstructor(clazz, type, annotatedModifiers.getAnnotations(), annotatedModifiers.getModifiers(), generics, arguments, exceptionTypes, body, enclosingConstructor, bodyConstructorType);
				constructor.setToken(tokenizer.getBlockToken(startToken));
				return constructor;
			}
		}

		tokenizer.rollback();
		return null;
	}

	public HiMethod visitMethod(Tokenizer tokenizer, CompileClassContext ctx, int... allowed) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);
		HiClass clazz = ctx.clazz;

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, true);

		// @generics
		NodeGenerics generics = GenericsParseRule.getInstance().visit(tokenizer, ctx);
		if (generics != null) {
			generics.setSourceType(NodeGeneric.GenericSourceType.method);
		}

		Type type = visitType(tokenizer, true, ctx.getEnv());
		if (type == null) {
			if (visitWord(Words.VOID, tokenizer) != null) {
				type = Type.voidType;
			}
		} else {
			int dimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, dimension, ctx.getEnv());
		}

		if (type != null) {
			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name != null) {
				if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
					tokenizer.commit();
					ctx.enter(RuntimeContext.METHOD, startToken);

					Token modifiersToken = startToken(tokenizer);
					Modifiers modifiers = annotatedModifiers.getModifiers();
					checkModifiers(tokenizer, modifiers, annotatedModifiers.getToken(), allowed);

					List<NodeArgument> arguments = new ArrayList<>();
					visitArgumentsDefinitions(tokenizer, arguments, ctx);

					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

					Type[] exceptionTypes = visitExceptionTypes(tokenizer, ctx);

					HiNode body = null;
					if (modifiers.isNative() || modifiers.isAbstract()) {
						expectSymbol(tokenizer, Symbols.SEMICOLON);
					} else {
						if (checkSymbol(tokenizer, Symbols.SEMICOLON) != -1) {
							tokenizer.nextToken();
							modifiers = modifiers.change().setAbstract(true);
							if (!clazz.isInterface) {
								tokenizer.error("modifier 'abstract' is expected", modifiersToken);
							}
						} else {
							expectSymbol(tokenizer, Symbols.BRACES_LEFT);
							body = BlockParseRule.getInstance().visit(tokenizer, ctx);
							expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
						}
					}

					ctx.exit();

					HiMethod method = new HiMethod(clazz, annotatedModifiers.getAnnotations(), modifiers, generics, type, name, arguments, exceptionTypes, body);
					method.setToken(tokenizer.getBlockToken(startToken));
					return method;
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	public Type[] visitExceptionTypes(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Type[] exceptionTypes = null;
		if (visitWordType(tokenizer, Words.THROWS) != -1) {
			Type exceptionType = visitType(tokenizer, true, ctx.getEnv());
			if (exceptionType == null) {
				tokenizer.error("identifier expected");
				return null;
			}
			List<Type> exceptionTypesList = new ArrayList<>(1);
			exceptionTypesList.add(exceptionType);
			if (checkSymbol(tokenizer, Symbols.COMMA) != -1) {
				tokenizer.nextToken();
				exceptionType = visitType(tokenizer, true, ctx.getEnv());
				if (exceptionType != null) {
					exceptionTypesList.add(exceptionType);
				} else {
					tokenizer.error("identifier expected");
				}
			}
			exceptionTypes = exceptionTypesList.toArray(new Type[exceptionTypesList.size()]);
		}
		return exceptionTypes;
	}

	public boolean visitFields(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		Token startToken = startToken(tokenizer);
		Type baseType = visitType(tokenizer, true, ctx.getEnv());
		if (baseType != null) {
			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name != null) {
				int addDimension = visitDimension(tokenizer);

				HiNode initializer = null;
				boolean isField = false;
				if (checkSymbol(tokenizer, Symbols.SEMICOLON, Symbols.COMMA) != -1) {
					isField = true;
				} else if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
					initializer = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
					isField = true;
				}

				if (isField) {
					tokenizer.commit();
					Modifiers modifiers = annotatedModifiers.getModifiers();
					checkModifiers(tokenizer, modifiers, annotatedModifiers.getToken(), PUBLIC, PROTECTED, PRIVATE, FINAL, STATIC);

					Type type = Type.getArrayType(baseType, addDimension, ctx.getEnv());
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
		}

		tokenizer.rollback();
		return false;
	}

	private void expectField(Tokenizer tokenizer, Type baseType, Modifiers modifiers, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Token startToken = startToken(tokenizer);
		String name = expectWord(Words.NOT_SERVICE, tokenizer);
		int addDimension = visitDimension(tokenizer);

		HiNode initializer = null;
		if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
			initializer = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		}

		Type type = Type.getArrayType(baseType, addDimension, ctx.getEnv());
		HiField<?> field = HiField.getField(type, name, initializer, tokenizer.getBlockToken(startToken));
		field.setModifiers(modifiers);

		ctx.addField(field);
	}

	private NodeInitializer visitBlock(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		boolean isStatic = visitWordType(tokenizer, STATIC) != -1;
		if (visitSymbol(tokenizer, Symbols.BRACES_LEFT) != -1) {
			tokenizer.commit();
			ctx.enter(RuntimeContext.BLOCK, tokenizer.currentToken());

			NodeBlock block = BlockParseRule.getInstance().visit(tokenizer, ctx);
			if (block != null) {
				block.setStatic(isStatic);
			}
			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			ctx.exit();
			return block;
		}

		tokenizer.rollback();
		return null;
	}
}
