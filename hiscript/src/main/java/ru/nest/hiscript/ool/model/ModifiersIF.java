package ru.nest.hiscript.ool.model;

public interface ModifiersIF {
	int ACCESS_DEFAULT = 0b1;

	int ACCESS_PUBLIC = 0b10;

	int ACCESS_PROTECTED = 0b100;

	int ACCESS_PRIVATE = 0b1000;

	int STATIC = 0b10000;

	int NATIVE = 0b100000;

	int FINAL = 0b1000000;

	int ABSTRACT = 0b10000000;

	int SYNCHRONIZED = 0b100000000;

	int DEFAULT = 0b1000000000;
}
