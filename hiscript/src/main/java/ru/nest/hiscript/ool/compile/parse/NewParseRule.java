package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.ClassLocationType;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

import java.util.ArrayList;
import java.util.List;

public class NewParseRule extends ParseRule<HiNode> {
	private final static NewParseRule instance = new NewParseRule();

	public static NewParseRule getInstance() {
		return instance;
	}

	private NewParseRule() {
	}

	@Override
	public HiNode visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(WordType.NEW, tokenizer) != null) {
			Type type = visitType(tokenizer, false, ctx.getEnv());
			if (type == null) {
				tokenizer.error("identifier is expected");
			}

			// @generics
			NodeGenerics generics = GenericsParseRule.getInstance().visit(tokenizer, ctx);
			if (generics != null) {
				generics.setSourceType(NodeGeneric.GenericSourceType.classSource);
			}

			SymbolType braceType = visitSymbol(tokenizer, SymbolType.PARENTHESES_LEFT, SymbolType.SQUARE_BRACES_LEFT, SymbolType.MASSIVE);
			HiNode node = null;
			if (braceType != null) {
				switch (braceType) {
					case PARENTHESES_LEFT:
						// new Type<generics>(arg1, ...)
						if (type.isPrimitive()) {
							tokenizer.error("'[' expected"); // primitive array expected
						}
						node = visitNewObject(tokenizer, type, generics, ctx, startToken);
						break;

					case SQUARE_BRACES_LEFT:
						// new Type[size1][size2]...[]...
						if (generics != null) {
							tokenizer.error("generic array creation", generics.getToken());
						}
						node = visitNewArray(tokenizer, type, ctx);
						break;

					case MASSIVE:
						// new Type[]...[]{{...},...}
						if (generics != null) {
							tokenizer.error("generic array creation", generics.getToken());
						}
						node = visitNewArrayValue(tokenizer, type, ctx);
						break;
				}
			}
			return node;
		}
		return null;
	}

	// new <type>(<arguments>) {<body>}
	private HiNode visitNewObject(Tokenizer tokenizer, Type type, NodeGenerics generics, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		HiNode[] arguments = visitArgumentsValues(tokenizer, ctx);

		expectSymbol(tokenizer, SymbolType.PARENTHESES_RIGHT);

		if (visitSymbol(tokenizer, SymbolType.BRACES_LEFT) != null) {
			CompileClassContext innerCtx = new CompileClassContext(ctx, ctx.clazz, ctx.type, ClassLocationType.anonymous);
			innerCtx.clazz = new HiClass(ctx.getClassLoader(), type, ctx.clazz, null, "", generics, ClassLocationType.anonymous, ctx);

			// TODO: do not allow parse constructors. ??? name is empty => constructors will be not found
			ClassParseRule.getInstance().visitContent(tokenizer, innerCtx, null);

			expectSymbol(tokenizer, SymbolType.BRACES_RIGHT);

			innerCtx.clazz.setToken(tokenizer.getBlockToken(startToken));
			return new NodeConstructor(innerCtx.clazz, type, arguments);
		} else {
			NodeType nodeType = new NodeType(type);
			return new NodeConstructor(nodeType, arguments);
		}
	}

	// new a.b.c.d[<index>]...[<index>] []...[]
	private HiNode visitNewArray(Tokenizer tokenizer, Type type, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		List<HiNode> indexes = new ArrayList<>();

		HiNode index = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
		if (index == null) {
			tokenizer.error("index is expected");
		}
		expectSymbol(tokenizer, SymbolType.SQUARE_BRACES_RIGHT);
		indexes.add(index);

		while (true) {
			if (visitSymbol(tokenizer, SymbolType.SQUARE_BRACES_LEFT) != null) {
				index = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
				indexes.add(index);
				expectSymbol(tokenizer, SymbolType.SQUARE_BRACES_RIGHT);
			} else if (visitSymbol(tokenizer, SymbolType.MASSIVE) != null) {
				if (indexes.size() == 0) {
					tokenizer.error("index is expected");
				}
				indexes.add(null);
			} else {
				break;
			}
		}

		HiNode[] indexesArray = new HiNode[indexes.size()];
		indexes.toArray(indexesArray);
		Type arrayType = Type.getArrayType(type, indexesArray.length, ctx.getEnv());
		return new NodeArray(arrayType, indexesArray);
	}

	// new a.b.c.d[]...[] {{...}, ... ,{...}}
	private HiNode visitNewArrayValue(Tokenizer tokenizer, Type type, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		int dimensions = visitDimension(tokenizer) + 1;

		HiNode value = visitArrayValue(tokenizer, type, dimensions, ctx);
		if (value == null) {
			tokenizer.error("dimension is expected");
		}
		return value;
	}

	public NodeArrayValue visitArrayValue(Tokenizer tokenizer, Type type, int requiredDimensions, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Token startToken = startToken(tokenizer);
		if (visitSymbol(tokenizer, SymbolType.BRACES_LEFT) != null) {
			if (requiredDimensions > 0) {
				List<HiNode> list = new ArrayList<>(1);
				int actualDimensions = Integer.MAX_VALUE;
				HiNode cell = visitCell(tokenizer, type, requiredDimensions - 1, ctx);
				if (cell != null) {
					list.add(cell);
					if (actualDimensions > 0) {
						actualDimensions = Math.min(cell.getArrayDimension(), actualDimensions);
					}
					while (visitSymbol(tokenizer, SymbolType.COMMA) != null) {
						cell = visitCell(tokenizer, type, requiredDimensions - 1, ctx);
						if (cell != null) {
							if (actualDimensions > 0) {
								actualDimensions = Math.min(cell.getArrayDimension(), actualDimensions);
							}
							list.add(cell);
						} else {
							tokenizer.error("expression expected");
						}
					}
					actualDimensions++;
				} else {
					actualDimensions = 1;
				}

				expectSymbol(tokenizer, SymbolType.BRACES_RIGHT);

				HiNode[] array = new HiNode[list.size()];
				list.toArray(array);
				return new NodeArrayValue(type, requiredDimensions, array);
			} else {
				tokenizer.error("array initializer is not allowed here", startToken);
			}
		}
		return null;
	}

	public HiNode visitCell(Tokenizer tokenizer, Type type, int requiredDimensions, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		HiNode cell = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
		if (cell != null) {
			return cell;
		}
		return visitArrayValue(tokenizer, type, requiredDimensions, ctx);
	}
}
