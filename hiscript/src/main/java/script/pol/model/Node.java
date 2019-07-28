package script.pol.model;

import java.util.HashMap;
import java.util.Map;

public abstract class Node {
	public Node(String name) {
		this.name = name;
	}

	private Node parent;

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	private String name;

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	private Map<String, Variable> variables = null;

	public Variable addVariable(Variable var) throws ExecuteException {
		if (getVariable(var.getFullname()) != null) {
			throw new ExecuteException(var.getFullname() + " is already defined");
		}

		if (variables == null) {
			variables = new HashMap<String, Variable>();
		}

		variables.put(var.getFullname(), var);
		return var;
	}

	public void addVariables(Map<String, Variable> variables) throws ExecuteException {
		if (variables != null) {
			if (this.variables == null) {
				this.variables = new HashMap<String, Variable>();
			}
			this.variables.putAll(variables);
		}
	}

	public Variable getVariable(String fullname) {
		Variable var = variables != null ? variables.get(fullname) : null;
		if (var == null && parent != null) {
			var = parent.getVariable(fullname);
		}
		return var;
	}

	public Map<String, Variable> getAllVariables() {
		Map<String, Variable> variables = new HashMap<String, Variable>();
		getAllVariables(variables);
		return variables;
	}

	private void getAllVariables(Map<String, Variable> variables) {
		if (this.variables != null) {
			variables.putAll(this.variables);
		}

		if (parent != null) {
			parent.getAllVariables(variables);
		}
	}

	public void removeVariables() {
		if (variables != null) {
			variables.clear();
		}
	}

	public Node getTopStatement() {
		Node parent = getParent();
		while (parent != null) {
			if (parent.isBlock) {
				return parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	private Methods methods = null;

	public Method getMethod(RuntimeContext ctx, String namespace, String name, int[] argTypes, int[] argDimensions) {
		if (methods != null) {
			Method method = methods.get(namespace, name, argTypes, argDimensions);
			if (method != null) {
				return method;
			}
		}

		if (parent != null) {
			return parent.getMethod(ctx, namespace, name, argTypes, argDimensions);
		} else if (ctx != null) {
			return ctx.getMethod(namespace, name, argTypes, argDimensions);
		}

		return null;
	}

	public void addMethod(RuntimeContext ctx, Method method) throws ExecuteException {
		Method m = getMethod(ctx, method.getNamespace(), method.getName(), method.getArgTypes(), method.getArgDimensions());
		if (m != null) {
			throw new ExecuteException(method.toString() + " is already defined");
		}

		if (methods == null) {
			methods = new Methods();
		}

		methods.add(method);
	}

	private HashMap<String, Class<?>> classes = null;

	public Class<?> addClass(Class<?> clazz) throws ExecuteException {
		if (getClass(clazz.getName()) != null) {
			throw new ExecuteException(clazz.getName() + " is already defined");
		}

		if (classes == null) {
			classes = new HashMap<String, Class<?>>();
		}

		classes.put(clazz.getName(), clazz);
		return clazz;
	}

	public Class<?> getClass(String name) {
		Class<?> clazz = classes != null ? classes.get(name) : null;
		if (clazz == null && parent != null) {
			clazz = parent.getClass(name);
		}
		return clazz;
	}

	public abstract void execute(RuntimeContext ctx) throws ExecuteException;

	public abstract void compile() throws ExecuteException;

	protected boolean isBlock = false;
}
