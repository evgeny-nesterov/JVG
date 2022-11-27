package ru.nest.hiscript.pol;

public interface CompileHandler {
	public void errorOccurred(int line, int offset, int length, String msg);
}
