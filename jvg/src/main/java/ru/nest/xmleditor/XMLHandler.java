package ru.nest.xmleditor;

public interface XMLHandler {
	public void startParseXML();

	public void stopParseXML();

	public void serviceChar(int offset);

	public void tag(boolean open, int offset, int length);

	public void spaces(int offset, int length);

	public void attributeName(int offset, int length);

	public void attributeValue(int offset, int length);

	public void comment(int offset, int length);

	public void text(int offset, int length);

	public void cdataName(int offset, int length);

	public void cdata(int offset, int length);

	public void instructionTag(int offset, int length);

	public void error(String message, int offset, int length) throws XMLParseException;
}
