package script.pol.model;

public class CharacterNode extends Node implements Value {
	public CharacterNode(char character) {
		super("character");
		this.character = character;
	}

	private char character;

	public char getCharacter() {
		return character;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Types.CHAR;
		ctx.value.dimension = 0;
		ctx.value.character = character;
	}
}
