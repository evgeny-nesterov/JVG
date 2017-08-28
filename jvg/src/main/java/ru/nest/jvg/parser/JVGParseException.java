package ru.nest.jvg.parser;

public class JVGParseException extends Exception {
	public JVGParseException(String message) {
		super(message);
	}

	public JVGParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
