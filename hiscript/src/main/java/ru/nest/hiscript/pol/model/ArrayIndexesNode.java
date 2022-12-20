package ru.nest.hiscript.pol.model;

import java.util.ArrayList;
import java.util.List;

public class ArrayIndexesNode extends Node {
	public ArrayIndexesNode() {
		super("array-indexes");
	}

	private List<ExpressionNode> indexes = new ArrayList<>();

	public void addIndex(ExpressionNode index) {
		indexes.add(index);
		index.setParent(this);
	}

	private ValueContainer buf1;

	private ValueContainer buf2;

	@Override
	public void compile() throws ExecuteException {
		buf1 = new ValueContainer();
		buf2 = new ValueContainer();
		for (ExpressionNode index : indexes) {
			index.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		buf1.type = ctx.value.type;

		buf2.type = buf1.type;
		buf2.dimension = ctx.value.dimension;
		ctx.value.copy(buf2);

		int count = indexes.size();
		for (int i = 0; i < count; i++) {
			buf1.dimension = buf2.dimension;
			buf2.copy(buf1);
			buf2.dimension--;

			ExpressionNode index = indexes.get(i);
			index.execute(ctx);
			buf2.setArrayValue(buf1, ctx.value.getInt());
		}

		ctx.value.type = buf2.type;
		ctx.value.dimension = buf2.dimension;
		buf2.copy(ctx.value);
	}
}
