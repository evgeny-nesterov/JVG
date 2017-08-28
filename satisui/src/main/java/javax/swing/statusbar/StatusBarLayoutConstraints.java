package javax.swing.statusbar;

public class StatusBarLayoutConstraints {
	/** the component's index in the status bar */
	private int index;

	/** the component's preferred width */
	private int preferredWidth;

	/** Indicates whether the component may be resized horizontally to fill */
	private boolean resizeable;

	/** Creates a new instance of StatusBarLayoutConstraints */
	public StatusBarLayoutConstraints(int index, int preferredWidth, boolean resizeable) {
		this.index = index;
		this.preferredWidth = preferredWidth;
		this.resizeable = resizeable;
	}

	public int getPreferredWidth() {
		return preferredWidth;
	}

	public boolean isResizeable() {
		return resizeable;
	}

	public int getIndex() {
		return index;
	}
}
