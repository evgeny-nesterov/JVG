package ru.nest.jvg.resource;

public abstract class Resource<V> implements Cloneable {
	private String name;

	private boolean valid = true;

	public Resource(String name) {
		this.name = name;
	}

	public Resource() {
		this.name = null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name != null ? name : "";
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (Exception exc) {
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Resource<?>)) {
			return false;
		}

		if (o == this) {
			return true;
		}

		V r1 = getResource();
		Object r2 = ((Resource<?>) o).getResource();
		if (r1 == null || r2 == null) {
			return r1 == null && r2 == null;
		} else {
			return r1.equals(r2);
		}
	}

	public abstract V getResource();

	public abstract void setResource(V resource);

	public void invalidate() {
		valid = false;
	}

	public void validate() {
		valid = true;
	}

	public boolean isValid() {
		return valid;
	}
}
