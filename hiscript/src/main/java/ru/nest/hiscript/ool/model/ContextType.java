package ru.nest.hiscript.ool.model;

public enum ContextType {
	SAME,

	// local context
	METHOD,

	// local context
	CONSTRUCTOR,

	// local context
	INITIALIZATION,

	// transparent for return
	BLOCK,

	FOR,

	WHILE,

	IF,

	// transparent for return
	DO_WHILE,

	SWITCH,

	TRY,

	// used with try
	CATCH,

	// used with try
	FINALLY,

	// transparent for return
	LABEL,

	START,

	OBJECT,

	// transparent for return
	SYNCHRONIZED,

	STATIC_CLASS
}
