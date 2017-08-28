package ru.nest.toi;

import java.awt.Color;

public class TOIDefaultColor implements TOIColor {
	@Override
	public Color getColor(TOIObject o, Color defaultColor) {
		Color c = o.getColor();
		return c != null ? c : defaultColor;
	}
}
