package javax.swing.toolbar;

import java.io.Serializable;

public class ToolBarConstraints implements Cloneable, Serializable {
	private static final long serialVersionUID = 6502849623983501802L;

	private int row;

	private int position;

	private int locX;

	private int resizeOffsetX;

	private int minimumWidth;

	private int preferredWidth;

	private int currentWidth;

	public ToolBarConstraints() {
		resizeOffsetX = -1;
		minimumWidth = -1;
		preferredWidth = -1;
	}

	public ToolBarConstraints(int row, int position) {
		resizeOffsetX = -1;
		minimumWidth = -1;
		preferredWidth = -1;
		locX = -1;
		this.row = row;
		this.position = position;
	}

	public ToolBarConstraints(int row, int position, int minimumWidth) {
		resizeOffsetX = -1;
		preferredWidth = -1;
		locX = -1;
		this.minimumWidth = minimumWidth;
		this.row = row;
		this.position = position;
	}

	public ToolBarConstraints(int row, int position, int minimumWidth, int preferredWidth) {
		resizeOffsetX = -1;
		locX = -1;
		this.minimumWidth = minimumWidth;
		this.row = row;
		this.position = position;
		this.preferredWidth = preferredWidth;
	}

	public void reset() {
		resizeOffsetX = -1;
		minimumWidth = -1;
		preferredWidth = -1;
		locX = -1;
		row = -1;
		position = -1;
	}

	public void setCurrentWidth(int currentWidth) {
		this.currentWidth = currentWidth;
	}

	public int getCurrentWidth() {
		return currentWidth;
	}

	public void setPreferredWidth(int preferredWidth) {
		this.preferredWidth = preferredWidth;
	}

	public int getPreferredWidth() {
		return preferredWidth;
	}

	public void setLocX(int locX) {
		this.locX = locX;
	}

	public int getLocX() {
		return locX == -1 ? position : locX;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public void setMinimumWidth(int minimumWidth) {
		this.minimumWidth = minimumWidth;
	}

	public int getMinimumWidth() {
		return minimumWidth;
	}

	public void setResizeOffsetX(int resizeOffsetX) {
		this.resizeOffsetX = resizeOffsetX;
	}

	public int getResizeOffsetX() {
		return resizeOffsetX;
	}

	public void setConstraints(int row, int position) {
		this.row = row;
		this.position = position;
	}

	@Override
	public Object clone() {
		try {
			ToolBarConstraints c = (ToolBarConstraints) super.clone();
			return c;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}

	}

	@Override
	public String toString() {
		return "ToolBarConstraints[row: " + row + ", position: " + position + ", resizeOffsetX: " + resizeOffsetX + ", minimumWidth: " + minimumWidth + ", preferredWidth: " + preferredWidth + ", currentWidth: " + currentWidth + "]";
	}
}
