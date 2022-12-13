package ru.nest.hiscript.pol;

public interface CompileHandler {
	void errorOccurred(int line, int offset, int length, String msg);
}
