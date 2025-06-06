package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.WordType;

import java.util.ArrayList;
import java.util.List;

public class InvocationNode extends Node implements Value {
	public InvocationNode(String namespace, String methodName) {
		super("invocation");
		this.namespace = namespace;
		this.methodName = methodName;
	}

	private final String namespace;

	public String getNamespace() {
		return namespace;
	}

	private final String methodName;

	public String getMethodName() {
		return methodName;
	}

	private final List<Node> arguments = new ArrayList<>();

	public void addArgument(Node argument) {
		arguments.add(argument);
		argument.setParent(this);
	}

	private WordType[] types;

	private int[] dimensions;

	private Object[] values;

	@Override
	public void compile() throws ExecuteException {
		int size = arguments.size();
		types = new WordType[size];
		dimensions = new int[size];
		values = new Object[size];

		for (int i = 0; i < size; i++) {
			Node node = arguments.get(i);
			node.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		int size = arguments.size();
		for (int i = 0; i < size; i++) {
			Node node = arguments.get(i);
			node.execute(ctx);

			types[i] = ctx.value.type;
			dimensions[i] = ctx.value.dimension;
			values[i] = ctx.value.getValue();
		}

		Method method = getMethod(ctx, namespace, methodName, types, dimensions);
		if (method == null) {
			throw new ExecuteException("method " + Method.getMethodDescr(namespace, methodName, types, dimensions, WordType.VOID) + " not found");
		}

		for (int i = 0; i < size; i++) {
			WordType type = method.getArgsTypes()[i];
			if (type != types[i]) {
				ctx.value.setValue(values[i], type);
				values[i] = ctx.value.getValue();
			}
		}

		method.invoke(ctx, this, values);
	}
}
