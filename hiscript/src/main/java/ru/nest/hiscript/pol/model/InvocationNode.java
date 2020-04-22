package ru.nest.hiscript.pol.model;

import java.util.ArrayList;
import java.util.List;

import ru.nest.hiscript.tokenizer.Words;

public class InvocationNode extends Node implements Value {
	public InvocationNode(String namespace, String methodName) {
		super("invocation");
		this.namespace = namespace;
		this.methodName = methodName;
	}

	private String namespace;

	public String getNamespace() {
		return namespace;
	}

	private String methodName;

	public String getMethodName() {
		return methodName;
	}

	private List<Node> arguments = new ArrayList<Node>();

	public void addArgument(Node argument) {
		arguments.add(argument);
		argument.setParent(this);
	}

	private int[] types;

	private int[] dimensions;

	private Object[] values;

	@Override
	public void compile() throws ExecuteException {
		int size = arguments.size();
		types = new int[size];
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
			throw new ExecuteException("method " + Method.getMethodDescr(namespace, methodName, types, dimensions, Words.VOID) + " not found");
		}

		for (int i = 0; i < size; i++) {
			int type = method.getArgTypes()[i];
			if (type != types[i]) {
				ctx.value.setValue(values[i], type);
				values[i] = ctx.value.getValue();
			}
		}

		method.invoke(ctx, this, values);
	}
}
