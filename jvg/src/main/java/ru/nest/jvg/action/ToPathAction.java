package ru.nest.jvg.action;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.RemoveUndoRedo;

public class ToPathAction extends JVGAction {
	public ToPathAction() {
		super("to-path");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		if (shapes != null && shapes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			for (JVGShape shape : shapes) {
				if (!(shape instanceof JVGPath)) {
					JVGContainer parent = shape.getParent();
					int index = parent.getChildIndex(shape);

					edit.add(new RemoveUndoRedo(pane, shape, parent, index));
					JVGPath newShape = convertToPath(shape, false);
					newShape.setSelected(true, false);
					edit.add(new AddUndoRedo(pane, newShape, null, -1, parent, index));
				}
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();

				appendMacrosCode(pane, "toPath(id);", JVGMacrosCode.ARG_ID);
			}
		}
	}

	public final static JVGPath convertToPath(JVGShape c, boolean coordinable) {
		JVGContainer parent = c.getParent();
		if (parent != null) {
			MutableGeneralPath path = c.getPath();

			JVGPath pathShape = c.getPane().getEditorKit().getFactory().createComponent(JVGPath.class, new Object[] { path, coordinable });

			// copy appearance
			c.copyTo(pathShape);
			if (c instanceof JVGStyledText) {
				if (c.getPaintersCount() == 0) {
					pathShape.setPainter(new FillPainter(ColorResource.black));
				}
			}

			AffineTransform transform = c.getTransform();
			pathShape.transform(transform);

			int index = parent.getChildIndex(c);
			parent.remove(c);
			parent.add(pathShape, index);

			return pathShape;
		}
		return null;
	}
}
