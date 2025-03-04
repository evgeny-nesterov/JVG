package ru.nest.hiscript.ool.runtime;

public enum ValueType {
	UNDEFINED,

	VALUE,

	TYPE,

	VARIABLE,

	CLASS,

	METHOD_INVOCATION,

	ARRAY_INDEX,

	NAME,

	// for node
	EXECUTE,

	// for .class, .this, .super
	TYPE_INVOCATION
}
