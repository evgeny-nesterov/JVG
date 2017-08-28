package ru.nest.jvg.action;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.TransformUndoRedo;

public class SameSpacesAction extends JVGAction {
	public final static int HORIZONTAL = 0;

	public final static int VERTICAL = 1;

	public final static int BOTH = 2;

	public SameSpacesAction(int type) {
		super(JVGEditorKit.sameSpacesActions[type]);
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGSelectionModel selectionModel = getSelectionManager(e);
			if (selectionModel != null) {
				JVGComponent[] selection = selectionModel.getSelection();
				if (selection != null && selection.length > 0) {
					HashMap<JVGShape, JVGShape> shapes = new HashMap<JVGShape, JVGShape>();
					for (int i = 0; i < selection.length; i++) {
						JVGComponent c = selection[i];
						if (c instanceof JVGShape) {
							JVGShape s = (JVGShape) c;
							shapes.put(s, s);
						}
					}
					JVGUtil.getRoots(shapes);

					if (shapes.size() > 2) {
						boolean first = true;
						double minX = 0, minY = 0, maxX = 0, maxY = 0, sumW = 0, sumH = 0;

						TreeMap<Double, JVGShape> sortedShapesX = new TreeMap<Double, JVGShape>();
						TreeMap<Double, JVGShape> sortedShapesY = new TreeMap<Double, JVGShape>();
						HashMap<JVGComponent, AffineTransform> transforms = new HashMap<JVGComponent, AffineTransform>();
						for (int i = 0; i < selection.length; i++) {
							JVGComponent c = selection[i];
							if (shapes.containsKey(c)) {
								JVGShape s = (JVGShape) c;
								Rectangle2D r = s.getRectangleBounds();

								double x = r.getX();
								while (sortedShapesX.containsKey(x)) {
									x += 1;
								}
								sortedShapesX.put(x, s);

								double y = r.getY();
								while (sortedShapesY.containsKey(y)) {
									y += 1;
								}
								sortedShapesY.put(y, s);

								transforms.put(s, new AffineTransform());

								if (first) {
									minX = r.getX();
									minY = r.getY();
									maxX = r.getX() + r.getWidth();
									maxY = r.getY() + r.getHeight();
									first = false;
								} else {
									if (minX > r.getX()) {
										minX = r.getX();
									}
									if (maxX < r.getX() + r.getWidth()) {
										maxX = r.getX() + r.getWidth();
									}

									if (minY > r.getY()) {
										minY = r.getY();
									}
									if (maxY < r.getY() + r.getHeight()) {
										maxY = r.getY() + r.getHeight();
									}
								}

								sumW += r.getWidth();
								sumH += r.getHeight();
							}
						}

						double interW = (maxX - minX - sumW) / (shapes.size() - 1);
						double interH = (maxY - minY - sumH) / (shapes.size() - 1);

						if (type == HORIZONTAL || type == BOTH) {
							double x = minX;
							Iterator<JVGShape> iter = sortedShapesX.values().iterator();
							while (iter.hasNext()) {
								JVGShape s = iter.next();
								Rectangle2D r = s.getRectangleBounds();
								AffineTransform t = transforms.get(s);
								t.translate(x - r.getX(), 0);
								x += r.getWidth() + interW;
							}
						}

						if (type == VERTICAL || type == BOTH) {
							double y = minY;
							Iterator<JVGShape> iter = sortedShapesY.values().iterator();
							while (iter.hasNext()) {
								JVGShape s = iter.next();
								Rectangle2D r = s.getRectangleBounds();
								AffineTransform t = transforms.get(s);
								t.translate(0, y - r.getY());
								y += r.getHeight() + interH;
							}
						}

						JVGUtil.transform(transforms);
						pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, new TransformUndoRedo(getName(), pane, transforms)));
						pane.repaint();

						appendMacrosCode(pane, "setSameSpaces(ids, %s);", JVGMacrosCode.ARG_IDS, type);
					}
				}
			}
		}
	}
}
