package javax.swing.sorttable;

public class SortHeaderObject {
	private String columnId;

	private boolean active = true;

	private boolean pressed = false;

	private boolean visible = true;

	private boolean isRised;

	public SortHeaderObject(String columnId) {
		this.columnId = columnId;
	}

	public SortHeaderObject(String columnId, boolean active, boolean pressed) {
		this.columnId = columnId;
		this.active = active;
		this.pressed = pressed;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isPressed() {
		return pressed;
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	public String getColumnId() {
		return columnId;
	}

	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isRised() {
		return isRised;
	}

	public void setRised(boolean isRised) {
		this.isRised = isRised;
	}
}
