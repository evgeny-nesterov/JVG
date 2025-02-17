package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.runtime.HiScriptRuntimeException;

public class HiDuplicateClassException extends HiScriptRuntimeException {
	public HiDuplicateClassException(String message) {
		super(message);
	}
}
