package ru.nest.hiscript.pol.model;

public class Variable {
	public Variable(String namespace, String name, int type, int dimension) {
		if (namespace != null && namespace.length() > 0) {
			this.fullname = namespace + "." + name;
		} else {
			this.fullname = name.intern();
			namespace = "";
		}

		this.namespace = namespace.intern();
		this.name = name.intern();

		value = new ValueContainer();
		value.type = type;
		value.dimension = dimension;
	}

	private final String fullname;

	public String getFullname() {
		return fullname;
	}

	private final String namespace;

	public String getNamespace() {
		return namespace;
	}

	private final String name;

	public String getName() {
		return name;
	}

	private final ValueContainer value;

	public ValueContainer getValue() {
		return value;
	}

	private boolean isDefined = false;

	public boolean isDefined() {
		return isDefined;
	}

	public void define() {
		isDefined = true;
	}
}
