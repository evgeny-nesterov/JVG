package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.WordType;

public class CharacterNode extends Node implements Value {
	public CharacterNode(char character) {
		super("character");
		this.character = character;
	}

	private final char character;

	public char getCharacter() {
		return character;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.type = WordType.CHAR;
		ctx.value.dimension = 0;
		ctx.value.character = character;
	}
}
