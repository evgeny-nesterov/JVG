package ru.nest.hiscript.pol.model;

import java.util.Map;

public class ScriptUtil {
	public static Node execute(RuntimeContext ctx, Node parent, String s) throws ExecuteException {
		return execute(ctx, parent, s, null);
	}

	public static Node execute(RuntimeContext ctx, Node parent, String s, Map<String, Variable> variables) throws ExecuteException {
		ru.nest.hiscript.pol.Compiler p = ru.nest.hiscript.pol.Compiler.getDefaultCompiler(s);
		try {
			Node node = p.build();
			if (node != null) {
				node.addVariables(variables);

				node.setParent(parent);
				node.isBlock = false;
				node.compile();
				node.execute(ctx);
				return node;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new ExecuteException(exc.getMessage());
		}

		return null;
	}
}
