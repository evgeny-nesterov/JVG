package script.pol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeclarationsNode extends Node {
	public DeclarationsNode(int type) {
		super("declarations");
		this.type = type;
	}

	private int type;

	public int getType() {
		return type;
	}

	public List<Integer> dimensions = new ArrayList<Integer>();

	public List<String> namespaces = new ArrayList<String>();

	public List<String> names = new ArrayList<String>();

	public Map<String, Node> variables = new HashMap<String, Node>();

	public void addVariable(String namespace, String name, int dimension, Node value) {
		String fullname;
		if (namespace != null && namespace.length() > 0) {
			fullname = namespace + "." + name;
		} else {
			fullname = name;
			namespace = "";
		}

		namespaces.add(namespace);
		names.add(name);
		dimensions.add(dimension);
		variables.put(fullname, value);

		if (value != null) {
			value.setParent(this);
		}
	}

	private Variable[] vars;

	public Variable[] getVariables() {
		return vars;
	}

	private int size;

	private Node statement;

	public void compile() throws ExecuteException {
		size = names.size();
		vars = new Variable[size];
		statement = getTopStatement();

		for (int i = 0; i < size; i++) {
			String namespace = namespaces.get(i);
			String name = names.get(i);
			int dimension = dimensions.get(i);
			vars[i] = new Variable(namespace, name, type, dimension);

			Node value = variables.get(vars[i].getFullname());
			if (value != null) {
				value.compile();
			}
		}
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		for (int i = 0; i < size; i++) {
			Variable var = statement.addVariable(vars[i]);
			Node value = variables.get(var.getFullname());
			if (value != null) {
				value.execute(ctx);
				Operations.doOperation(var.getValue(), ctx.value, Operations.EQUATE);
				var.define();
			}
		}
	}
}
