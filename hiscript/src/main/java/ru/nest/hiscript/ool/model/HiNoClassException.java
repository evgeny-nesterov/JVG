package ru.nest.hiscript.ool.model;

public class HiNoClassException extends RuntimeException {
	private int index;

	public HiNoClassException(int index) {
		this.index = index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
