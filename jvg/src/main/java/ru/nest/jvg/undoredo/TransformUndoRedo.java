package ru.nest.jvg.undoredo;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.shape.JVGShape;

public class TransformUndoRedo extends JVGUndoRedo {
	private JVGComponent[] shapes;

	private AffineTransform transform;

	private AffineTransform[] transforms;

	public TransformUndoRedo(String name, JVGPane pane, JVGComponent shape, AffineTransform transform) {
		super(name, pane);
		this.shapes = new JVGComponent[] { shape };
		this.transform = transform;
	}

	public TransformUndoRedo(String name, JVGPane pane, JVGComponent[] shapes, AffineTransform transform) {
		super(name, pane);
		this.shapes = shapes;
		this.transform = transform;
	}

	public TransformUndoRedo(String name, JVGPane pane, JVGComponent[] shapes, AffineTransform[] transforms) {
		super(name, pane);
		this.shapes = shapes;
		this.transforms = transforms;
	}

	public TransformUndoRedo(String name, JVGPane pane, HashMap<JVGComponent, AffineTransform> transforms) {
		super(name, pane);
		this.shapes = new JVGComponent[transforms.size()];
		this.transforms = new AffineTransform[transforms.size()];

		int i = 0;
		Iterator<JVGComponent> iter = transforms.keySet().iterator();
		while (iter.hasNext()) {
			this.shapes[i] = iter.next();
			this.transforms[i] = transforms.get(this.shapes[i]);
			i++;
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		try {
			if (shapes.length == 1 && shapes[0] instanceof JVGShape) {
				AffineTransform it = transform.createInverse();
				JVGShape shape = (JVGShape) shapes[0];
				shape.transform(it);
			} else if (shapes.length > 1) {
				if (transform != null) {
					AffineTransform it = transform.createInverse();
					JVGUtil.transform(shapes, it);
				} else if (transforms != null) {
					AffineTransform[] invertTransforms = new AffineTransform[transforms.length];
					for (int i = 0; i < transforms.length; i++) {
						if (transforms[i] != null) {
							invertTransforms[i] = transforms[i].createInverse();
						}
					}

					JVGUtil.transform(shapes, invertTransforms);
				}
			}
			getPane().repaint();
		} catch (NoninvertibleTransformException exc) {
			throw new CannotUndoException();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (shapes == null) {
			return;
		}
		if (shapes.length == 1 && shapes[0] instanceof JVGShape) {
			((JVGShape) shapes[0]).transform(transform);
		} else if (shapes.length > 1) {
			if (transform != null) {
				JVGUtil.transform(shapes, transform);
			} else if (transforms != null) {
				JVGUtil.transform(shapes, transforms);
			}
		}
		getPane().repaint();
	}
}
