package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;
import java.util.HashMap;

public class HiEnumValue implements Codeable {
	private String name;

	private int ordinal;

	private Node[] arguments;

	public HiEnumValue(String name, int ordinal, Node[] arguments) {
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

	public Node[] getArguments() {
		return arguments;
	}

	public void initEnumValue(RuntimeContext ctx) {
		if (arguments!=null) {
			for (Node argument : arguments) {

			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeUTF(name);
		os.writeShort(ordinal);
		os.writeShort(arguments.length);
		os.write(arguments);
	}

	public static HiEnumValue decode(DecodeContext os) throws IOException {
		return new HiEnumValue(os.readUTF(), os.readShort(), os.readNodeArray(Node.class, os.readShort()));
	}
}