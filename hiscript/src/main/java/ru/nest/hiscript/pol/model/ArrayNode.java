package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.WordType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayNode extends Node {
	public ArrayNode(WordType type) {
		super("array");
		this.type = type;
	}

	private final WordType type;

	public WordType getType() {
		return type;
	}

	private final List<ExpressionNode> indexes = new ArrayList<>();

	public void addIndex(ExpressionNode index) {
		indexes.add(index);
		if (index != null) {
			index.setParent(this);
		}
	}

	private int[] dimensions;

	@Override
	public void compile() throws ExecuteException {
		int size = 0;
		for (ExpressionNode index : indexes) {
			if (index == null) {
				break;
			}
			index.compile();
			size++;
		}

		dimensions = new int[size];
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		for (int i = 0; i < dimensions.length; i++) {
			ExpressionNode index = indexes.get(i);
			index.execute(ctx);
			dimensions[i] = ctx.value.getInt();
		}

		Object array;
		if (dimensions.length < indexes.size()) {
			Class<?> arrayClass = Types.getArrayType(type, indexes.size() - dimensions.length);
			array = Array.newInstance(arrayClass, dimensions);
		} else {
			Class<?> clazz = Types.getType(type);
			array = Array.newInstance(clazz, dimensions);
		}

		ctx.value.type = type;
		ctx.value.dimension = indexes.size();
		ctx.value.array = array;
	}
}
