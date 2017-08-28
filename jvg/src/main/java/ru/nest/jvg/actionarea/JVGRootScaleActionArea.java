package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.JVGSelectionModel;

public class JVGRootScaleActionArea extends JVGRootTransformActionArea {
	public final static int TOP = 0;

	public final static int LEFT = 1;

	public final static int BOTTOM = 2;

	public final static int RIGHT = 3;

	public final static int TOP_LEFT = 4;

	public final static int TOP_RIGHT = 5;

	public final static int BOTTOM_LEFT = 6;

	public final static int BOTTOM_RIGHT = 7;

	private double width = 4;

	private double height = 4;

	public JVGRootScaleActionArea(int type) {
		this.type = type;
		setName("scale-" + getDescr());
	}

	private int type;

	public int getType() {
		return type;
	}

	private AffineTransform t = new AffineTransform();

	@Override
	public AffineTransform getTransform(double x, double y, double dx, double dy) {
		JVGComponent p = getParent();
		if (p instanceof JVGRoot) {
			Rectangle2D bounds = null;
			JVGRoot root = (JVGRoot) p;
			JVGPane pane = root.getPane();
			if (pane != null) {
				JVGSelectionModel selectionModel = pane.getSelectionManager();
				if (selectionModel.getSelectionCount() > 1) {
					bounds = selectionModel.getSelectionBounds();
				}
			}

			if (bounds != null) {
				double translateX = 0;
				double translateY = 0;
				double scaleX = 1;
				double scaleY = 1;

				if (type == TOP || type == TOP_RIGHT || type == TOP_LEFT) {
					if (dy > 0) {
						double delta = bounds.getHeight() - dy;
						if (delta < 1) {
							dy = bounds.getHeight() - 1;
						}
					}

					translateY = bounds.getY() + bounds.getHeight();
					scaleY = (bounds.getHeight() - dy) / bounds.getHeight();
				} else if (type == BOTTOM || type == BOTTOM_RIGHT || type == BOTTOM_LEFT) {
					if (dy < 0) {
						double delta = bounds.getHeight() + dy;
						if (delta < 1) {
							dy = 1 - bounds.getHeight();
						}
					}

					translateY = bounds.getY();
					scaleY = (bounds.getHeight() + dy) / bounds.getHeight();
				}

				if (type == LEFT || type == TOP_LEFT || type == BOTTOM_LEFT) {
					if (dx > 0) {
						double delta = bounds.getWidth() - dx;
						if (delta < 1) {
							dx = bounds.getWidth() - 1;
						}
					}

					translateX = bounds.getX() + bounds.getWidth();
					scaleX = (bounds.getWidth() - dx) / bounds.getWidth();
				} else if (type == RIGHT || type == TOP_RIGHT || type == BOTTOM_RIGHT) {
					if (dx < 0) {
						double delta = bounds.getWidth() + dx;
						if (delta < 1) {
							dx = 1 - bounds.getWidth();
						}
					}

					translateX = bounds.getX();
					scaleX = (bounds.getWidth() + dx) / bounds.getWidth();
				}

				t.setToTranslation(translateX, translateY);
				t.scale(scaleX, scaleY);
				t.translate(-translateX, -translateY);

				this.dx = dx;
				this.dy = dy;
				return t;
			}
		}

		t.setToIdentity();
		return t;
	}

	private double dx;

	@Override
	public double getDeltaX() {
		return dx;
	}

	private double dy;

	@Override
	public double getDeltaY() {
		return dy;
	}

	private boolean busy = false;

	@Override
	protected Shape computeActionBounds() {
		if (busy) {
			return null;
		}

		JVGComponent p = getParent();
		if (p instanceof JVGRoot) {
			Rectangle2D bounds = null;
			JVGRoot root = (JVGRoot) p;
			JVGPane pane = root.getPane();
			if (pane != null) {
				JVGSelectionModel selectionModel = pane.getSelectionManager();
				if (selectionModel.getSelectionCount() > 1) {
					busy = true;
					bounds = selectionModel.getSelectionBounds();
					busy = false;
				}
			}

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
		} else {
			return null;
		}
	}

	@Override
	protected Color getColor() {
		return Color.white;
	}

	@Override
	protected Color getBorderColor() {
		return Color.black;
	}

	@Override
	public Cursor getCursor() {
		switch (type) {
			case BOTTOM:
			case TOP:
				return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);

			case LEFT:
			case RIGHT:
				return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

			case TOP_LEFT:
			case BOTTOM_RIGHT:
				return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);

			case TOP_RIGHT:
			case BOTTOM_LEFT:
				return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
		}

		return Cursor.getDefaultCursor();
	}

	public String getDescr() {
		return ActionUtil.getDescr(type);
	}
}
