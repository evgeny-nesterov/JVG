package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.paint.ShadowPainter;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class ShadowAction extends JVGAction {
	public ShadowAction(int type) {
		super("shadow");
		this.type = type;
	}

	public ShadowAction() {
		super("no-shadow");
		noShadow = true;
	}

	private boolean noShadow = false;

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		JVGPane pane = getPane(e);
		setShadow(pane, type, noShadow, shapes);
	}

	public static void setShadow(JVGPane pane, int type, boolean noShadow, JVGShape... shapes) {
		if (shapes != null && shapes.length > 0) {
			String name = noShadow ? "no-shadow" : "shadow";
			CompoundUndoRedo edit = new CompoundUndoRedo(name, pane);

			for (JVGShape shape : shapes) {
				ShadowPainter painter = null;
				int count = shape.getPaintersCount();
				for (int i = count - 1; i >= 0; i--) {
					Painter p = shape.getPainter(i);
					if (p instanceof ShadowPainter) {
						painter = (ShadowPainter) p;
						break;
					}
				}

				if (noShadow) {
					if (painter != null) {
						int index = shape.getPainterIndex(painter);
						shape.removePainter(painter);

						edit.add(new PropertyUndoRedo(name, pane, shape, "addPainter", new Class[] { int.class, Painter.class }, new Object[] { index, painter }, "removePainter", new Class[] { Painter.class }, new Object[] { painter }));
					}
				} else if (painter == null) {
					painter = new ShadowPainter(type);
					painter.setPaint(new ColorDraw(ShadowPainter.DEFAULT_COLOR));

					shape.addPainter(0, painter);

					edit.add(new PropertyUndoRedo(name, pane, shape, "removePainter", new Class[] { Painter.class }, new Object[] { painter }, "addPainter", new Class[] { int.class, Painter.class }, new Object[] { 0, painter }));
				} else if (painter != null) {
					int oldType = painter.getShadowType();
					if (oldType != type) {
						painter.setShadowType(type);
						edit.add(new PropertyUndoRedo(name, pane, painter, "setShadowType", oldType, type));
					}
				}
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}

			// TODO: add macrosCOde
		}
	}
}
