package ru.nest.hiscript.ool.model;

public class HiIllegalArgumentException extends RuntimeException {
	private final String argumentName;

	public HiIllegalArgumentException(String message, String argumentName) {
		super(message);
		this.argumentName = argumentName;
	}

	public String getArgumentName() {
		return argumentName;
	}
}
