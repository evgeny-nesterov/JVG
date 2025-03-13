package ru.nest.hiscript.ool.model;

public class JavaString {
	private char[] chars;

	private int hashCode;

	public JavaString(char[] chars) {
		this.chars = chars;
	}

	public JavaString(String string) {
		this.chars = string.toCharArray();
	}

	public char[] getChars() {
		return chars;
	}

	@Override
	public boolean equals(Object o) {
		JavaString s = (JavaString) o;
		if (chars.length != s.chars.length) {
			return false;
		}
		int length = chars.length;
		for (int i = 0; i < length; i++) {
			if (chars[i] != s.chars[i]) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int hashCode = this.hashCode;
		if (hashCode == 0) {
			int length = chars.length;
			for (int i = 0; i < length; i++) {
				hashCode = 37 * hashCode + chars[i];
			}
			this.hashCode = hashCode;
		}
		return hashCode;
	}
}
