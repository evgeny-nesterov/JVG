package javax.swing.closetab;

import java.util.EventObject;

public class TabRolloverEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	/** the tab index of the rollover */
	private int index;

	/** the x-coord */
	private int x;

	/** the y-coord */
	private int y;

	/**
	 * Creates a new instance of TabRolloverEvent with the specified object as the source of this event.
	 * 
	 * @param the
	 *            source object
	 */
	public TabRolloverEvent(Object source, int index) {
		this(source, index, -1, -1);
	}

	/**
	 * Creates a new instance of TabRolloverEvent with the specified object as the source of this event.
	 * 
	 * @param the
	 *            source object
	 */
	public TabRolloverEvent(Object source, int index, int x, int y) {
		super(source);
		this.index = index;
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the tab index where this event originated.
	 * 
	 * @return the tab index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * The x-coord of the underlying mouse event.
	 * 
	 * @return the x-coord
	 */
	public int getX() {
		return x;
	}

	/**
	 * The y-coord of the underlying mouse event.
	 * 
	 * @return the y-coord
	 */
	public int getY() {
		return y;
	}
}
