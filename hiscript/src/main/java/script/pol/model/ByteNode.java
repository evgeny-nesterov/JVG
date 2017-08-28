package script.pol.model;

public class ByteNode extends Node implements Value {
	public ByteNode(byte value) {
		super("byte");
		this.value = value;
	}

	private byte value;

	public byte getNumber() {
		return value;
	}

	public void compile() throws ExecuteException {
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Types.BYTE;
		ctx.value.dimension = 0;
		ctx.value.byteNumber = value;
	}
}
