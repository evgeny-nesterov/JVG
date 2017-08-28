package ru.nest.jvg.action;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.TransformUndoRedo;

public class AlignmentAction extends JVGAction {
	public final static int TOP = 0;

	public final static int LEFT = 1;

	public final static int BOTTOM = 2;

	public final static int RIGHT = 3;

	public final static int CENTER_HOR = 4;

	public final static int CENTER_VER = 5;

	public final static int CENTER = 6;

	public AlignmentAction(int type) {
		super(JVGEditorKit.alignmentActions[type]);
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGShape[] shapes = getShapes(e);
			if (shapes != null && shapes.length > 0) {
				AffineTransform transform = align(pane, shapes, type);
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, new TransformUndoRedo(getName(), pane, shapes, transform)));
				pane.repaint();

				appendMacrosCode(pane, "setAlign(id, %s);", JVGMacrosCode.ARG_ID, type);
			}
		}
	}

	public static AffineTransform alignCurrentArea(JVGPane pane, JVGShape[] shapes, int type) {
		if (shapes != null && shapes.length > 0) {
			double tx = 0, ty = 0;

			Double zoom = (Double) pane.getClientProperty("zoom");
			if (zoom == null) {
				zoom = 1.0;
			}

			Rectangle2D totalBounds = JVGUtil.getBounds(shapes);
			Rectangle vr = pane.getVisibleRect();
			AffineTransform paneTransform = pane.getTransform();

			switch (type) {
				case TOP:
					ty = (vr.y - paneTransform.getTranslateY()) / zoom - totalBounds.getY();
					break;

				case LEFT:
					tx = (vr.x - paneTransform.getTranslateX()) / zoom - totalBounds.getX();
					break;

				case BOTTOM:
					ty = (vr.y - paneTransform.getTranslateY()) / zoom - totalBounds.getY() + (vr.height / zoom - totalBounds.getHeight());
					break;

				case RIGHT:
					tx = (vr.x - paneTransform.getTranslateX()) / zoom - totalBounds.getX() + (vr.width / zoom - totalBounds.getWidth());
					break;

				case CENTER_HOR:
					tx = (vr.x - paneTransform.getTranslateX()) / zoom - totalBounds.getX() + (vr.width / zoom - totalBounds.getWidth()) / 2;
					break;

				case CENTER_VER:
					ty = (vr.y - paneTransform.getTranslateY()) / zoom - totalBounds.getY() + (vr.height / zoom - totalBounds.getHeight()) / 2;
					break;

				case CENTER:
					tx = (vr.x - paneTransform.getTranslateX()) / zoom - totalBounds.getX() + (vr.width / zoom - totalBounds.getWidth()) / 2;
					ty = (vr.y - paneTransform.getTranslateY()) / zoom - totalBounds.getY() + (vr.height / zoom - totalBounds.getHeight()) / 2;
					break;
			}

			AffineTransform transform = AffineTransform.getTranslateInstance(tx, ty);
			for (JVGShape shape : shapes) {
				shape.transform(transform);
			}
			return transform;
		} else {
			return null;
		}
	}

	public static AffineTransform align(JVGPane pane, JVGShape[] shapes, int type) {
		if (shapes != null && shapes.length > 0) {
			double tx = 0, ty = 0, w = pane.getWidth(), h = pane.getHeight();

			Double zoom = (Double) pane.getClientProperty("zoom");
			if (zoom != null) {
				w /= zoom;
				h /= zoom;
			}

			Insets insets = (Insets) pane.getClientProperty("document-insets");
			if (insets != null) {
				w -= insets.top + insets.bottom;
				h -= insets.left + insets.right;
			}

			Rectangle2D totalBounds = JVGUtil.getBounds(shapes);
			switch (type) {
				case TOP:
					ty = -totalBounds.getY();
					break;

				case LEFT:
					tx = -totalBounds.getX();
					break;

				case BOTTOM:
					ty = h - totalBounds.getY() - totalBounds.getHeight();
					break;

				case RIGHT:
					tx = w - totalBounds.getX() - totalBounds.getWidth();
					break;

				case CENTER_HOR:
					tx = (w - totalBounds.getWidth()) / 2.0 - totalBounds.getX();
					break;

				case CENTER_VER:
					ty = (h - totalBounds.getHeight()) / 2.0 - totalBounds.getY();
					break;

				case CENTER:
					tx = (w - totalBounds.getWidth()) / 2.0 - totalBounds.getX();
					ty = (h - totalBounds.getHeight()) / 2.0 - totalBounds.getY();
					break;
			}

			AffineTransform transform = AffineTransform.getTranslateInstance(tx, ty);
			for (JVGShape shape : shapes) {
				shape.transform(transform);
			}
			return transform;
		} else {
			return null;
		}
	}
}
