package ru.nest.jvg.action;

import java.awt.Font;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;
import ru.nest.jvg.undoredo.ShapeChangedUndoRedo;
import ru.nest.jvg.undoredo.TransformUndoRedo;

public class RemoveTransformAction extends JVGAction {
	public RemoveTransformAction() {
		super("remove-transform");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		if (shapes != null && shapes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			for (JVGShape shape : shapes) {
				AffineTransform transform = new AffineTransform(shape.getInverseTransform());
				if (shape instanceof JVGPath) {
					JVGPath path = (JVGPath) shape;

					Shape oldShape = path.getPathShape();
					MutableGeneralPath newShape = new MutableGeneralPath(path.getTransformedPathShape());

					path.transform(transform);
					edit.add(new TransformUndoRedo(getName(), pane, path, transform));

					path.setShape(newShape, false);
					edit.add(new ShapeChangedUndoRedo(getName(), pane, path, oldShape, newShape));
				} else if (shape instanceof JVGTextField) {
					JVGTextField text = (JVGTextField) shape;

					Resource<Font> oldFont = text.getFont();
					Resource<Font> newFont = new FontResource(oldFont.getResource().deriveFont(shape.getTransform()));

					text.transform(transform);
					edit.add(new TransformUndoRedo(getName(), pane, text, transform));

					text.setFont(newFont);
					edit.add(new PropertyUndoRedo(getName(), pane, text, "setFont", Resource.class, oldFont, newFont));
				}
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}

			appendMacrosCode(pane, "removeTransform(id);", JVGMacrosCode.ARG_ID);
		}
	}
}
