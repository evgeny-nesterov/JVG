package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.Words;

public class CharacterNode extends Node implements Value {
	public CharacterNode(char character) {
		super("character");
		this.character = character;
	}

	private char character;

	public char getCharacter() {
		return character;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.type = Words.CHAR;
		ctx.value.dimension = 0;
		ctx.value.character = character;
	}
}
