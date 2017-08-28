package javax.swing.dock.tab;

public class TabDockConstraints {
	public final static int END = -2;

	public final static int CENTER = -3;

	private int index;

	public TabDockConstraints(int index) {
		this.index = index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
