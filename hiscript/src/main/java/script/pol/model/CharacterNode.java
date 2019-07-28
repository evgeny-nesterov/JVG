package script.pol.model;

import script.tokenizer.Words;

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
	public void compile() throws ExecuteException {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Words.CHAR;
		ctx.value.dimension = 0;
		ctx.value.character = character;
	}
}
