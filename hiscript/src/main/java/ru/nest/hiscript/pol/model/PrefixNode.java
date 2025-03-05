package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.SymbolType;

import java.util.ArrayList;
import java.util.List;

public class PrefixNode extends Node {
	public PrefixNode() {
		super("prefix");
	}

	private final List<Object> prefixes = new ArrayList<>();

	public void addPrefix(SymbolType operation) {
		prefixes.add(operation);
	}

	public void addPrefix(CastNode cast) {
		prefixes.add(cast);
		cast.setParent(this);
	}

	@Override
	public void compile() throws ExecuteException {
		for (Object prefix : prefixes) {
			if (prefix instanceof Node) {
				Node node = (Node) prefix;
				node.compile();
			}
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		int size = prefixes.size();
		for (int i = size - 1; i >= 0; i--) {
			Object prefix = prefixes.get(i);
			if (prefix instanceof CastNode) {
				CastNode cast = (CastNode) prefix;
				cast.execute(ctx);
			} else if (prefix instanceof SymbolType) {
				SymbolType operation = (SymbolType) prefix;
				Operations.doPrefixOperation(ctx.value, operation);
			}
		}
	}
}
