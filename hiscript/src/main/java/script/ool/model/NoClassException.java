package script.ool.model;

public class NoClassException extends RuntimeException {
	private int index;

	public NoClassException(int index) {
		this.index = index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
