package ru.nest.xmleditor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XMLParser {
	private InputStream is;

	private XMLHandler handler;

	public XMLParser(String text, XMLHandler handler) {
		this(new ByteArrayInputStream(text.getBytes()), handler);
	}

	public XMLParser(InputStream is, XMLHandler handler) {
		this.is = is;
		this.handler = handler;
	}

	private boolean end = false;

	private int current = -1;

	private int pos = -1;

	private void next() throws XMLParseException {
		if (!end) {
			try {
				current = is.read();
				// System.out.print((char)current);
				pos++;
				end = current == -1;
				return;
			} catch (IOException exc) {
				end = true;
				throw new XMLParseException("Can't read XML: " + exc.getMessage(), pos, 1);
			}
		}

		current = -1;
	}

	public void parse() throws XMLParseException {
		try {
			handler.startParseXML();

			next();
			readChilds(null);
		} finally {
			handler.stopParseXML();
		}
	}

	private void readElement() throws XMLParseException {
		// '<' has been readed
		String tagName = readTagName(true, false);
		readAttributes();

		omitSpaces();

		search("'>' or '/>' is expected", '/', '>');

		if (current == '/') {
			handler.serviceChar(pos);
			next();

			expectServiceCharacter('>');
		} else if (current == '>') {
			handler.serviceChar(pos);
			next();

			readChilds(tagName);
			readEndTag(tagName);
		}
	}

	/**
	 * try read: 'openTagName>', '</' is readed in last child
	 */
	private void readEndTag(String openTagName) throws XMLParseException {
		omitSpaces();

		String closeTagName = readTagName(false, false);
		if (!closeTagName.equals(openTagName)) {
			int length = closeTagName.length();
			handler.error("The element type '" + openTagName + "' must be terminated by the matching end tag </" + openTagName + ">", pos - length, length);
		}

		omitSpaces();
		expectServiceCharacter('>');
	}

	// '&' is used for specioal symbols
	private void readText() throws XMLParseException {
		int offset = pos;
		while (!end && current != '<') {
			if (Character.isLetter(current) || Character.isDigit(current) || isSpace(current) || current == '~' || current == '!' || current == '@' || current == '#' || current == '$' || current == '%' || current == '^' || current == '*' || current == '(' || current == ')' || current == '_' || current == '+' || current == '-' || current == '=' || current == '\\' || current == '|' || current == '/' || current == ';' || current == ':' || current == ',' || current == '.' || current == '?' || current == '`' || current == '[' || current == ']' || current == '{' || current == '}' || current == '�' || current == '$' || current == '>') {
			} else {
				handler.error("Invalid symbol passed to text: '" + (char) current + "'", pos, 1);
			}
			next();
		}

		int length = pos - offset;
		if (length > 0) {
			handler.text(offset, length);
		}
	}

	private void readInstruction() throws XMLParseException {
		// "<?" has been readed
		String tagName = readTagName(true, true);
		readAttributes();

		omitSpaces();
		expectServiceCharacter('?');
		expectServiceCharacter('>');
	}

	private boolean hasRootElements = false;

	private void readChilds(String parent) throws XMLParseException {
		while (!end) {
			if (parent == null) {
				omitSpaces();
				search("Content is not allowed in trailing section", '<');
			} else {
				readText();
			}

			if (current == '<') {
				handler.serviceChar(pos);
				next();

				if (current == '/') {
					if (parent != null) {
						// check on parent close tag
						handler.serviceChar(pos);
						next();
						return;
					}
				} else if (current == '?') {
					// check instructions
					if (hasRootElements) {
						handler.error("Processing instruction is not allowed here", pos, 1);
					}

					handler.serviceChar(pos);
					next();

					readInstruction();
					continue;
				} else if (current == '!') {
					handler.serviceChar(pos);
					next();

					if (current == '-') {
						// check comment
						handler.serviceChar(pos);
						next();

						readComment();
						continue;
					} else if (parent != null) {
						if (current == '[') {
							// check CDATA
							handler.serviceChar(pos);
							next();

							readCDATA();
							continue;
						}
					}
				}

				hasRootElements = true;
				readElement();
				continue;
			} else {
				if (!end) {
					handler.error("Content is not allowed in trailing section", pos, 1);
				}
			}

			break;
		}
	}

	private void readComment() throws XMLParseException {
		// "<!-" has been readed
		expectServiceCharacter('-');

		int offset = pos;
		while (!end) {
			if (buf.length() == 3) {
				buf.deleteCharAt(0);
			}
			buf.append((char) current);

			next();

			if (buf.length() == 3 && buf.charAt(0) == '-' && buf.charAt(1) == '-' && buf.charAt(2) == '>') {
				break;
			}
		}
		buf.delete(0, buf.length());

		handler.comment(offset, pos - offset);
		handler.serviceChar(pos - 3);
		handler.serviceChar(pos - 2);
		handler.serviceChar(pos - 1);
	}

	private void readCDATA() throws XMLParseException {
		// "<![" has been readed
		int offset = pos;
		String CDATA_tagName = readWord();
		if (!CDATA_tagName.equals("CDATA")) {
			int length = pos - offset;
			if (length == 0) {
				length = 1;
			}
			handler.error("'CDATA' is expected", offset, length);
		} else {
			handler.cdataName(offset, pos - offset);
		}
		expectServiceCharacter('[');

		offset = pos;
		while (!end) {
			if (buf.length() == 3) {
				buf.deleteCharAt(0);
			}
			buf.append((char) current);

			next();

			if (buf.length() == 3 && buf.charAt(0) == ']' && buf.charAt(1) == ']' && buf.charAt(2) == '>') {
				break;
			}
		}
		buf.delete(0, buf.length());

		handler.cdata(offset, pos - offset - 3);
		handler.serviceChar(pos - 3);
		handler.serviceChar(pos - 2);
		handler.serviceChar(pos - 1);
	}

	private void readAttributes() throws XMLParseException {
		omitSpaces();
		while (!end) {
			String name = readAttribute();
			if (name == null) {
				break;
			}
		}
	}

	private String readAttribute() throws XMLParseException {
		omitSpaces();

		int offset = pos;
		String attributeName = readWord();
		int length = pos - offset;
		if (length > 0) {
			handler.attributeName(offset, length);
			expectServiceCharacter('=');

			search("'\'' or '\"' is expected", '\'', '"');

			int quote = -1;
			if (current == '\'' || current == '"') {
				quote = current;
			}
			handler.serviceChar(pos);
			next();

			offset = pos;
			while (current != quote) {
				if (end || current == '>') {
					handler.error("'" + (char) quote + "' is expected", pos, 1);
				}

				buf.append((char) current);
				next();
			}
			int endQuoteIndex = pos;
			next();

			String attributeValue = buf.toString();
			buf.delete(0, buf.length());
			handler.attributeValue(offset, attributeValue.length());

			handler.serviceChar(endQuoteIndex);

			return attributeName;
		}

		return null;
	}

	private String readTagName(boolean open, boolean instraction) throws XMLParseException {
		omitSpaces();

		int offset = pos;
		String tagName = readWord();

		int length = pos - offset;
		if (length > 0 && (isSpace(current) || current == '/' || current == '>')) {
			if (instraction) {
				handler.instructionTag(offset, length);
			} else {
				handler.tag(open, offset, length);
			}
		} else {
			handler.error("Tag name is expected", offset, 1);
		}
		return tagName;
	}

	private StringBuilder buf = new StringBuilder();

	private String readWord() throws XMLParseException {
		if (!end && (Character.isLetter(current) || current == '_' || current == '-')) {
			buf.append((char) current);
			next();
			while (!end && (Character.isLetter(current) || Character.isDigit(current) || current == '_' || current == '-')) {
				buf.append((char) current);
				next();
			}

			String word = buf.toString();
			buf.delete(0, buf.length());
			return word;
		} else {
			return "";
		}
	}

	private void omitSpaces() throws XMLParseException {
		int offset = pos;
		while (!end && isSpace(current)) {
			next();
		}

		int length = pos - offset;
		if (length > 0) {
			handler.spaces(offset, length);
		}
	}

	private boolean isSpace(int c) {
		return c == ' ' || c == '\n' || c == '\r' || c == '\t';
	}

	private void expectServiceCharacter(char character) throws XMLParseException {
		String errorMessage = "'" + character + "' is expected";
		search(errorMessage, character);

		if (current != character) {
			throw new XMLParseException(errorMessage, pos, 1);
		}
		handler.serviceChar(pos);
		next();
	}

	private void search(String errorMessage, char... characters) throws XMLParseException {
		int offset = pos;
		WHILE: while (!end) {
			for (char character : characters) {
				if (current == character) {
					break WHILE;
				}
			}
			next();
		}

		int length = pos - offset;
		if (length > 0) {
			handler.error(errorMessage, offset, length);
		}
	}

	public static void main(String[] args) {
		try {
			// final String xml = "<a><![CDATA[123]]></a> \n";
			// InputStream is = new ByteArrayInputStream(xml.getBytes());
			// InputStream is = new
			// FileInputStream("C:/Games/SpaceForce - Rogue Universe/Uninstall/uninstall.xml");
			InputStream is = XMLParser.class.getResourceAsStream("/ru/nest/jvg/xml/test.xml");

			new XMLParser(is, new XMLHandler() {
				String shift = "";

				@Override
				public void serviceChar(int offset) {
					// System.out.println(xml.charAt(offset));
				}

				@Override
				public void tag(boolean open, int offset, int length) {
					// if (!open)
					// {
					// shift = shift.substring(2);
					// }
					// System.out.println(shift + (open ? "OPEN: " : "CLOSE: ")
					// + xml.substring(offset, offset + length));
					// if (open)
					// {
					// shift += "  ";
					// }
				}

				@Override
				public void spaces(int offset, int length) {
					// System.out.println("SPACE: " + length);
				}

				@Override
				public void attributeName(int offset, int length) {
					// System.out.println(shift + "   PARAM: " +
					// xml.substring(offset, offset + length));
				}

				@Override
				public void attributeValue(int offset, int length) {
					// System.out.println(shift + "   VALUE: " +
					// xml.substring(offset, offset + length));
				}

				@Override
				public void text(int offset, int length) {
					// System.out.println(shift + "   TEXT: " +
					// xml.substring(offset, offset + length));
				}

				@Override
				public void comment(int offset, int length) {
					// System.out.println(shift + "COMMENT: " +
					// xml.substring(offset, offset + length));
				}

				@Override
				public void cdataName(int offset, int length) {
					// System.out.println(shift + xml.substring(offset, offset +
					// length));
				}

				@Override
				public void cdata(int offset, int length) {
					// System.out.println(shift + "CDATA: " +
					// xml.substring(offset, offset + length));
				}

				@Override
				public void instructionTag(int offset, int length) {
				}

				@Override
				public void error(String message, int offset, int length) {
				}

				@Override
				public void startParseXML() {
				}

				@Override
				public void stopParseXML() {
				}
			}).parse();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
