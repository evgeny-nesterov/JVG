package script.pol;

public interface CompileHandler {
	public void errorOccured(int line, int offset, int length, String msg);
}
