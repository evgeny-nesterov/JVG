package ru.nest.jvg.actionarea;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class ActionUtil implements Constants {
	public static String getDescr(int type) {
		switch (type) {
			case TOP:
				return "top";

			case LEFT:
				return "left";

			case BOTTOM:
				return "bottom";

			case RIGHT:
				return "right";

			case TOP_LEFT:
				return "top-left";

			case TOP_RIGHT:
				return "top-right";

			case BOTTOM_LEFT:
				return "bottom-left";

			case BOTTOM_RIGHT:
				return "bottom-right";
		}

		return "";
	}

	public static Shape computeScaleBounds(Rectangle2D bounds, int type, double width, double height) {
		if (bounds == null) {
			return null;
		}

		double x;
		double y;
		switch (type) {
			case TOP:
				x = bounds.getX() + bounds.getWidth() / 2; // - width / 2.0;
				y = bounds.getY(); // - height;
				break;

			case LEFT:
				x = bounds.getX(); // - width;
				y = bounds.getY() + bounds.getHeight() / 2; // - height / 2.0;
				break;

			case BOTTOM:
				x = bounds.getX() + bounds.getWidth() / 2; // - width / 2.0;
				y = bounds.getY() + bounds.getHeight();
				break;

			case RIGHT:
				x = bounds.getX() + bounds.getWidth();
				y = bounds.getY() + bounds.getHeight() / 2; // - height / 2.0;
				break;

			case TOP_LEFT:
				x = bounds.getX(); // - width;
				y = bounds.getY(); // - height;
				break;

			case TOP_RIGHT:
				x = bounds.getX() + bounds.getWidth();
				y = bounds.getY(); // - height;
				break;

			case BOTTOM_LEFT:
				x = bounds.getX(); // - width;
				y = bounds.getY() + bounds.getHeight();
				break;

			case BOTTOM_RIGHT:
				x = bounds.getX() + bounds.getWidth();
				y = bounds.getY() + bounds.getHeight();
				break;

			default:
				x = bounds.getX() + bounds.getWidth() / 2; // - width / 2.0;
				y = bounds.getY() + bounds.getHeight() / 2; // - height / 2.0;
				break;
		}

		return new Rectangle2D.Double(x - width / 2.0, y - height / 2.0, width, height);
	}
}
