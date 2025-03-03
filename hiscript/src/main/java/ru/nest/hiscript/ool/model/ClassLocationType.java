package ru.nest.hiscript.ool.model;

public enum ClassLocationType {
	/**
	 * No outbound class
	 */
	top,

	/**
	 * static (NESTED) and not
	 */
	inner,

	/**
	 * in method, constructor
	 */
	local,

	/**
	 * like new Object() {...}
	 */
	anonymous
}