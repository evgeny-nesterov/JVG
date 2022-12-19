package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;

public class HiEnumValue implements Codeable {
	private String name;

	private int ordinal;

	private HiNode[] arguments;

	public HiEnumValue(String name, int ordinal, HiNode[] arguments) {
		this.name = name;
		this.ordinal = ordinal;
		this.arguments = arguments;
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

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeUTF(name);
		os.writeShort(ordinal);
		os.writeShort(arguments != null ? arguments.length : 0);
		os.writeNullable(arguments);
	}

	public static HiEnumValue decode(DecodeContext os) throws IOException {
		return new HiEnumValue(os.readUTF(), os.readShort(), os.readNullableNodeArray(HiNode.class, os.readShort()));
	}
}