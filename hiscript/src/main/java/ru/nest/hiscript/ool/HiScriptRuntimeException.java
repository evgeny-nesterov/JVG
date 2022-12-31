package ru.nest.hiscript.ool;

public class HiScriptRuntimeException extends RuntimeException {
	public HiScriptRuntimeException(String message) {
		super(message);
	}

	public HiScriptRuntimeException(String message, Throwable exc) {
		super(message, exc);
	}
}
