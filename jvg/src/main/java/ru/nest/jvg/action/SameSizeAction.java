package ru.nest.jvg.action;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.TransformUndoRedo;

public class SameSizeAction extends JVGAction {
	public final static int HORIZONTAL = 0;

	public final static int VERTICAL = 1;

	public final static int BOTH = 2;

	public SameSizeAction(int type) {
		super(JVGEditorKit.sameSizeActions[type]);
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGComponent c = pane.getFocusOwner();
			JVGSelectionModel selectionModel = getSelectionManager(e);
			if (c != null && selectionModel != null) {
				JVGComponent[] selection = selectionModel.getSelection();
				if (selection != null && selection.length > 0) {
					AffineTransform[] transforms = new AffineTransform[selection.length];
					Rectangle2D bounds = c.getRectangleBounds();
					boolean isIdentity = true;
					for (int i = 0; i < selection.length; i++) {
						JVGComponent s = selection[i];
						if (s instanceof JVGShape) {
							if (s != c) {
								JVGShape shape = (JVGShape) s;
								Rectangle2D b = shape.getRectangleBounds();

								double scaleX = 1;
								double scaleY = 1;
								if (type == HORIZONTAL || type == BOTH) {
									scaleX = bounds.getWidth() / b.getWidth();
								}
								if (type == VERTICAL || type == BOTH) {
									scaleY = bounds.getHeight() / b.getHeight();
								}

								double translateX = b.getX();
								double translateY = b.getY();

								transforms[i] = new AffineTransform();
								transforms[i].translate(translateX, translateY);
								transforms[i].concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
								transforms[i].translate(-translateX, -translateY);
								if (!transforms[i].isIdentity()) {
									isIdentity = false;
								}
							} else {
								transforms[i] = new AffineTransform();
							}
						}
					}

					if (!isIdentity) {
						JVGUtil.transform(selection, transforms);
						pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, new TransformUndoRedo(getName(), pane, selection, transforms)));
						pane.repaint();

						appendMacrosCode(c.getPane(), "setSameSizes(ids, %s);", JVGMacrosCode.ARG_IDS, type);
					}
				}
			}
		}
	}
}
