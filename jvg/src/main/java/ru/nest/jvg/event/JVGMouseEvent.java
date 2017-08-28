package ru.nest.jvg.event;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import ru.nest.jvg.JVGComponent;

public class JVGMouseEvent extends JVGInputEvent {
	public final static int MOUSE_CLICKED = MouseEvent.MOUSE_CLICKED;

	public final static int MOUSE_PRESSED = MouseEvent.MOUSE_PRESSED;

	public final static int MOUSE_RELEASED = MouseEvent.MOUSE_RELEASED;

	public final static int MOUSE_ENTERED = MouseEvent.MOUSE_ENTERED;

	public final static int MOUSE_EXITED = MouseEvent.MOUSE_EXITED;

	public final static int MOUSE_MOVED = MouseEvent.MOUSE_MOVED;

	public final static int MOUSE_DRAGGED = MouseEvent.MOUSE_DRAGGED;

	public final static int MOUSE_WHEEL = MouseEvent.MOUSE_WHEEL;

	public static final int NOBUTTON = MouseEvent.NOBUTTON;

	public final static int BUTTON1 = MouseEvent.BUTTON1;

	public final static int BUTTON2 = MouseEvent.BUTTON2;

	public final static int BUTTON3 = MouseEvent.BUTTON3;

	public JVGMouseEvent(InputEvent originEvent, JVGComponent source, int id, long when, int modifiers, double x, double y, double adjustX, double adjustY, int clickCount, int button) {
		super(originEvent, source, id, when, modifiers);

		this.x = x;
		this.y = y;
		this.adjustX = adjustX;
		this.adjustY = adjustY;
		this.clickCount = clickCount;
		this.button = button;
	}

	double x;

	public double getX() {
		return x;
	}

	double y;

	public double getY() {
		return y;
	}

	double adjustX;

	public double getAdjustedX() {
		return adjustX;
	}

	double adjustY;

	public double getAdjustedY() {
		return adjustY;
	}

	int clickCount;

	public int getClickCount() {
		return clickCount;
	}

	int button;

	public int getButton() {
		return button;
	}

	public synchronized void translatePoint(int x, int y) {
		this.x += x;
		this.y += y;
	}

	public synchronized void convert() {
		this.x = adjustX;
		this.y = adjustY;
	}

	@Override
	public boolean isShiftDown() {
		return (modifiers & InputEvent.SHIFT_MASK) != 0;
	}

	@Override
	public boolean isControlDown() {
		return (modifiers & InputEvent.CTRL_MASK) != 0;
	}

	@Override
	public boolean isMetaDown() {
		return (modifiers & InputEvent.META_MASK) != 0;
	}

	@Override
	public boolean isAltDown() {
		return (modifiers & InputEvent.ALT_MASK) != 0;
	}

	@Override
	public boolean isAltGraphDown() {
		return (modifiers & InputEvent.ALT_GRAPH_MASK) != 0;
	}

	@Override
	public String paramString() {
		StringBuffer str = new StringBuffer(80);

		switch (id) {
			case MOUSE_PRESSED:
				str.append("MOUSE_PRESSED");
				break;

			case MOUSE_RELEASED:
				str.append("MOUSE_RELEASED");
				break;

			case MOUSE_CLICKED:
				str.append("MOUSE_CLICKED");
				break;

			case MOUSE_ENTERED:
				str.append("MOUSE_ENTERED");
				break;

			case MOUSE_EXITED:
				str.append("MOUSE_EXITED");
				break;

			case MOUSE_MOVED:
				str.append("MOUSE_MOVED");
				break;

			case MOUSE_DRAGGED:
				str.append("MOUSE_DRAGGED");
				break;

			case MOUSE_WHEEL:
				str.append("MOUSE_WHEEL");
				break;

			default:
				str.append("unknown type");
		}

		// (x,y) coordinates
		str.append(",(").append(x).append(",").append(y).append(")");

		str.append(",button=").append(getButton());

		if (getModifiers() != 0) {
			str.append(",modifiers=").append(getMouseModifiersText(modifiers));
		}

		if (getModifiersEx() != 0) {
			str.append(",extModifiers=").append(getModifiersExText(modifiers));
		}

		str.append(",clickCount=").append(clickCount);

		return str.toString();
	}

	public static String getMouseModifiersText(int modifiers) {
		return MouseEvent.getMouseModifiersText(modifiers);
	}

	public static String getModifiersExText(int modifiers) {
		return InputEvent.getModifiersExText(modifiers);
	}

	public Point2D getPoint() {
		double x;
		double y;
		synchronized (this) {
			x = this.x;
			y = this.y;
		}
		return new Point2D.Double(x, y);
	}

	public boolean isLeftMouseButton() {
		return (getModifiers() & InputEvent.BUTTON1_MASK) != 0;
	}

	public boolean isMiddleMouseButton() {
		return (getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK;
	}

	public boolean isRightMouseButton() {
		return (getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK;
	}
}
