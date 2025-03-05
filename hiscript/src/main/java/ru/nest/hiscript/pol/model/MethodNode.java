package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.WordType;

public class MethodNode extends Node {
	public MethodNode(String name, WordType type, int dimension, ArgumentsNode arguments, BlockNode body) {
		super("method");
		this.name = name;
		this.type = type;
		this.dimension = dimension;
		this.arguments = arguments;
		this.body = body;
		isBlock = true;

		if (arguments != null) {
			arguments.setParent(this);
		}

		if (body != null) {
			body.setParent(this);
		}
	}

	private final String name;

	public String getMethodName() {
		return name;
	}

	private final WordType type;

	public WordType getType() {
		return type;
	}

	private final int dimension;

	public int getDimension() {
		return dimension;
	}

	private final ArgumentsNode arguments;

	public ArgumentsNode getArguments() {
		return arguments;
	}

	private final BlockNode body;

	public BlockNode getBody() {
		return body;
	}

	private Method method;

	public Method getMethod() {
		return method;
	}

	@Override
	public void compile() throws ExecuteException {
		WordType[] types;
		int[] dimensions;
		if (arguments != null) {
			arguments.compile();
			types = arguments.getTypes();
			dimensions = arguments.getDimensions();
		} else {
			types = new WordType[0];
			dimensions = new int[0];
		}

		if (body != null) {
			body.compile();
		}

		method = new Method(null, name, types, dimensions, type, dimension) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... values) throws ExecuteException {
				super.invoke(ctx, parent, values);

				int argsCount;
				if (arguments != null) {
					arguments.execute(ctx);
					argsCount = arguments.getNames().length;
				} else {
					argsCount = 0;
				}

				for (int i = 0; i < argsCount; i++) {
					WordType type = Types.getType(values[i].getClass());
					ctx.value.setValue(values[i], type);

					Variable var = getVariable(arguments.getNames()[i]);
					Operations.doOperation(var.getValue(), ctx.value, SymbolType.EQUATE);
				}

				if (body != null) {
					body.execute(ctx);
				}
				removeVariables();

				ctx.isExit = false;
				ctx.value.cast(type, dimension);
			}
		};
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		Node parent = getParent();
		if (parent != null) {
			parent.addMethod(ctx, method);
		}
	}
}
