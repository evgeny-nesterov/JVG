package ru.nest.jvg.event;

import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;

import ru.nest.jvg.JVGComponent;

public class JVGMouseWheelEvent extends JVGMouseEvent {
	public static final int WHEEL_UNIT_SCROLL = MouseWheelEvent.WHEEL_UNIT_SCROLL;

	public static final int WHEEL_BLOCK_SCROLL = MouseWheelEvent.WHEEL_BLOCK_SCROLL;

	public JVGMouseWheelEvent(InputEvent originEvent, JVGComponent source, int id, long when, int modifiers, double x, double y, double adjustX, double adjustY, int clickCount, int button, int scrollType, int scrollAmount, int wheelRotation) {
		super(originEvent, source, id, when, modifiers, x, y, adjustX, adjustY, clickCount, button);
		this.scrollType = scrollType;
		this.scrollAmount = scrollAmount;
		this.wheelRotation = wheelRotation;
	}

	int scrollType;

	public int getScrollType() {
		return scrollType;
	}

	int scrollAmount;

	public int getScrollAmount() {
		return scrollAmount;
	}

	int wheelRotation;

	public int getWheelRotation() {
		return wheelRotation;
	}

	public int getUnitsToScroll() {
		return scrollAmount * wheelRotation;
	}

	@Override
	public String paramString() {
		String scrollTypeStr = null;

		if (getScrollType() == WHEEL_UNIT_SCROLL) {
			scrollTypeStr = "WHEEL_UNIT_SCROLL";
		} else if (getScrollType() == WHEEL_BLOCK_SCROLL) {
			scrollTypeStr = "WHEEL_BLOCK_SCROLL";
		} else {
			scrollTypeStr = "unknown scroll type";
		}
		return super.paramString() + ",scrollType=" + scrollTypeStr + ",scrollAmount=" + getScrollAmount() + ",wheelRotation=" + getWheelRotation();
	}
}
