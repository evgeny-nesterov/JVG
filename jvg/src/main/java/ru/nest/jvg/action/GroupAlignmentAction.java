package ru.nest.jvg.action;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.TransformUndoRedo;

/**
 * Align selected components relative to the focused components
 */
public class GroupAlignmentAction extends JVGAction {
	public final static int TOP = 0;

	public final static int LEFT = 1;

	public final static int BOTTOM = 2;

	public final static int RIGHT = 3;

	public final static int CENTER_HOR = 4;

	public final static int CENTER_VER = 5;

	public final static int CENTER = 6;

	public GroupAlignmentAction(int type) {
		super(JVGEditorKit.groupAlignmentActions[type]);
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGComponent source = pane.getFocusOwner();
			if (source != null) {
				JVGShape[] shapes = getShapes(e);
				align(pane, shapes, source, type);

				appendMacrosCode(pane, "groupAlign(ids, %s);", JVGMacrosCode.ARG_IDS, type);
			}
		}
	}

	public static void align(JVGPane pane, JVGShape[] shapes, JVGComponent source, int type) {
		if (shapes != null && shapes.length > 0 && source != null) {
			AffineTransform[] transforms = new AffineTransform[shapes.length];
			Rectangle2D bounds = source.getRectangleBounds();
			boolean isIdentity = true;
			for (int i = 0; i < shapes.length; i++) {
				JVGShape shape = shapes[i];
				if (shape != source) {
					Rectangle2D b = shape.getRectangleBounds();

					double translateX = 0;
					double translateY = 0;
					switch (type) {
						case TOP:
							translateY = bounds.getY() - b.getY();
							break;

						case LEFT:
							translateX = bounds.getX() - b.getX();
							break;

						case BOTTOM:
							translateY = bounds.getY() + bounds.getHeight() - b.getY() - b.getHeight();
							break;

						case RIGHT:
							translateX = bounds.getX() + bounds.getWidth() - b.getX() - b.getWidth();
							break;

						case CENTER_HOR:
							translateX = bounds.getX() + bounds.getWidth() / 2 - b.getX() - b.getWidth() / 2;
							break;

						case CENTER_VER:
							translateY = bounds.getY() + bounds.getHeight() / 2 - b.getY() - b.getHeight() / 2;
							break;

						case CENTER:
							translateX = bounds.getX() + bounds.getWidth() / 2 - b.getX() - b.getWidth() / 2;
							translateY = bounds.getY() + bounds.getHeight() / 2 - b.getY() - b.getHeight() / 2;
							break;
					}

					transforms[i] = AffineTransform.getTranslateInstance(translateX, translateY);
					if (!transforms[i].isIdentity()) {
						isIdentity = false;
					}
				} else {
					transforms[i] = new AffineTransform();
				}
			}

			if (!isIdentity) {
				JVGUtil.transform(shapes, transforms);
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, new TransformUndoRedo(JVGEditorKit.groupAlignmentActions[type], pane, shapes, transforms)));
				pane.repaint();
			}
		}
	}
}
