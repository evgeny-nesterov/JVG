package ru.nest.xmleditor;

public class XMLParseException extends Exception {
	public XMLParseException(String message, int offset, int length) {
		super(message);
		this.offset = offset;
		this.length = length;
	}

	private int offset;

	public int getOffset() {
		return offset;
	}

	private int length;

	public int getLength() {
		return length;
	}
}
