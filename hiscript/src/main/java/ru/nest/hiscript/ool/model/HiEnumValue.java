package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public class HiEnumValue implements Codeable {
	private final String name;

	private final int ordinal;

	private final HiNode[] arguments;

	private final Token token;

	public HiEnumValue(String name, int ordinal, HiNode[] arguments, Token token) {
		this.name = name;
		this.ordinal = ordinal;
		this.arguments = arguments;
		this.token = token;
	}

	public String getName() {
		return name;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public HiNode[] getArguments() {
		return arguments;
	}

	public Token getToken() {
		return token;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeUTF(name);
		os.writeShort(ordinal);
		os.writeShort(arguments != null ? arguments.length : 0);
		os.writeNullable(arguments);
		os.writeToken(token);
	}

	public static HiEnumValue decode(DecodeContext os) throws IOException {
		return new HiEnumValue(os.readUTF(), os.readShort(), os.readNullableNodeArray(HiNode.class, os.readShort()), os.readToken());
	}
}