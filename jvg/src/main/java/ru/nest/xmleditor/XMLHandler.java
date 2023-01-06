package ru.nest.xmleditor;

public interface XMLHandler {
	void startParseXML();

	void stopParseXML();

	void serviceChar(int offset);

	void tag(boolean open, int offset, int length);

	void spaces(int offset, int length);

	void attributeName(int offset, int length);

	void attributeValue(int offset, int length);

	void comment(int offset, int length);

	void text(int offset, int length);

	void cdataName(int offset, int length);

	void cdata(int offset, int length);

	void instructionTag(int offset, int length);

	void error(String message, int offset, int length) throws XMLParseException;
}
