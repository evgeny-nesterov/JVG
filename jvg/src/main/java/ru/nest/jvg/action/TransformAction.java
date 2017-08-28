package ru.nest.jvg.action;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.macros.JVGMacros;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.TransformUndoRedo;

public class TransformAction extends JVGAction {
	private final static int ANY = 0;

	private final static int TRANSLATE_BY_WEIGHT = 1;

	private final static int SCALE_BY_PIXEL = 2;

	public TransformAction(String name, AffineTransform transform) {
		super(name);
		this.transform = transform;
		type = ANY;
	}

	public TransformAction(String name, AffineTransform transform, double weightX, double weightY) {
		super(name);
		this.transform = transform;
		this.weightX = weightX;
		this.weightY = weightY;
		type = TRANSLATE_BY_WEIGHT;
	}

	public TransformAction(String name, double weightX, double weightY, int deltaPixelsX, int deltaPixelsY) {
		super(name);
		this.weightX = weightX;
		this.weightY = weightY;
		this.deltaPixelsX = deltaPixelsX;
		this.deltaPixelsY = deltaPixelsY;
		type = SCALE_BY_PIXEL;
	}

	private int type;

	private AffineTransform transform;

	private double weightX, weightY;

	private int deltaPixelsX, deltaPixelsY;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGShape[] shapes = getShapes(e);
			if (shapes != null && shapes.length > 0) {
				Rectangle2D bounds = JVGUtil.getBounds(shapes);
				AffineTransform transform = null;

				if (type == TRANSLATE_BY_WEIGHT) {
					double x = bounds.getX() + weightX * bounds.getWidth();
					double y = bounds.getY() + weightY * bounds.getHeight();

					transform = new AffineTransform();
					transform.translate(x, y);
					transform.concatenate(this.transform);
					transform.translate(-x, -y);
				} else if (type == ANY) {
					transform = this.transform;
				} else if (type == SCALE_BY_PIXEL) {
					double x = bounds.getX() + weightX * bounds.getWidth();
					double y = bounds.getY() + weightY * bounds.getHeight();
					double scaleX = (bounds.getWidth() + deltaPixelsX) / bounds.getWidth();
					double scaleY = (bounds.getHeight() + deltaPixelsY) / bounds.getHeight();

					transform = new AffineTransform();
					transform.translate(x, y);
					transform.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
					transform.translate(-x, -y);
				}

				if (transform != null) {
					JVGUtil.transform(shapes, transform);
					pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, new TransformUndoRedo(getName(), pane, shapes, transform)));
					pane.repaint();

					JVGMacros.appendTransform(pane, transform);
				}
			}
		}
	}
}
