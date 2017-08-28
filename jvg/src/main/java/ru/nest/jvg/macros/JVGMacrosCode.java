package ru.nest.jvg.macros;

public class JVGMacrosCode implements CharSequence {
	private StringBuilder buf = new StringBuilder();

	public static int ARG_NONE = 0;

	public static int ARG_ID = 1;

	public static int ARG_IDS = 2;

	private int argType;

	public JVGMacrosCode(int argType) {
		this.argType = argType;
	}

	public JVGMacrosCode(int argType, CharSequence code) {
		this.argType = argType;
		append(code);
	}

	public JVGMacrosCode(int argType, String format, Object... args) {
		this.argType = argType;
		append(format, args);
	}

	public int getArgType() {
		return argType;
	}

	public String getCode() {
		return buf.toString();
	}

	public JVGMacrosCode append(String format, Object... args) {
		return append(String.format(format, args));
	}

	public JVGMacrosCode append(CharSequence code) {
		buf.append(code);
		return this;
	}

	@Override
	public int length() {
		return buf.length();
	}

	@Override
	public char charAt(int index) {
		return buf.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return buf.subSequence(start, end);
	}

	@Override
	public String toString() {
		return buf.toString();
	}
}
