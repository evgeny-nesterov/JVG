package ru.nest.hiscript.pol.model;

import java.util.ArrayList;

public class ArgumentsNode extends Node {
	public ArgumentsNode() {
		super("arguments");
	}

	private boolean value;

	public boolean getBoolean() {
		return value;
	}

	private ArrayList<ArgumentNode> arguments = new ArrayList<ArgumentNode>();

	public void addArgument(ArgumentNode argument) {
		arguments.add(argument);
		argument.setParent(this);
	}

	private int[] types;

	public int[] getTypes() {
		return types;
	}

	private int[] dimensions;

	public int[] getDimensions() {
		return dimensions;
	}

	private String[] names;

	public String[] getNames() {
		return names;
	}

	@Override
	public void compile() throws ExecuteException {
		int size = arguments.size();
		types = new int[size];
		dimensions = new int[size];
		names = new String[size];

		for (int i = 0; i < size; i++) {
			ArgumentNode argument = arguments.get(i);
			argument.compile();

			types[i] = argument.getType();
			dimensions[i] = argument.getDimension();
			names[i] = argument.getArgName();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		int size = arguments.size();
		for (int i = 0; i < size; i++) {
			ArgumentNode argument = arguments.get(i);
			argument.execute(ctx);
		}
	}
}
