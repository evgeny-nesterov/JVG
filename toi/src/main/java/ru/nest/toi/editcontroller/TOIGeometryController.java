package ru.nest.toi.editcontroller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import ru.nest.toi.MutablePath;
import ru.nest.toi.SelectionRotater;
import ru.nest.toi.TOIController;
import ru.nest.toi.TOIObject;
import ru.nest.toi.TOIObjectControl;
import ru.nest.toi.TOIPane;
import ru.nest.toi.TOIPane.PathCoord;
import ru.nest.toi.objects.TOIPath;

public class TOIGeometryController implements TOIController {
	private TOIPane pane;

	private double mx, my;

	private TOIObject pressedObject;

	private PathCoord pressedPoint;

	private TOIObject sr;

	private Stroke editedStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 2f, 2f }, 0f);

	private Stroke selectedPointStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 2f, 2f }, 0f);

	private boolean dragged;

	private Rectangle2D selectionRect;

	private SelectionRotater selectionRotater;

	private TOIObjectControl objectControl;

	private boolean pressed = false;

	@Override
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_MOVED) {
			objectControl = pane.getObjectControl(x, y);
		}
		if (objectControl != null) {
			objectControl.processMouseEvent(e, x, y, adjustX, adjustY);
			if (e.getID() == MouseEvent.MOUSE_RELEASED) {
				objectControl = null;
			} else if (e.getID() == MouseEvent.MOUSE_MOVED) {
				pane.setCursor(objectControl.getCursor());
			}
			e.consume();
			return;
		}

		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			pressedPoint = pane.getPathCoord(x, y);
			if (e.getButton() == MouseEvent.BUTTON1) {
				mx = x;
				my = y;
				pressed = true;

				if (pressedPoint == null) {
					pressedObject = pane.getObject(x, y);
					pane.setFocusedObject(pressedObject);
				} else {
					pressedObject = pressedPoint.getPath();
					pane.setFocusedObject(pressedPoint.getPath());
				}

				if (pane.getFocusedObject() != null) {
					if (e.isControlDown()) {
						if (!pane.getFocusedObject().isSelected()) {
							pane.select(pane.getFocusedObject(), true);
							sr = pane.getFocusedObject();
						}
					} else if (!pane.getFocusedObject().isSelected()) {
						pane.clearSelection();
						pane.select(pane.getFocusedObject(), true);
					}
				} else {
					pane.clearSelection();
					selectionRect = new Rectangle2D.Double(mx, my, 0, 0);
					selectionRotater = new SelectionRotater(pane);
					selectionRotater.start();
				}
				pane.requestFocus();
				pane.repaint();
			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (pane.getFocusedObject() != null) {
				if (e.isControlDown()) {
					if (!dragged && pane.getFocusedObject().isSelected() && sr == null) {
						pane.select(pane.getFocusedObject(), false);
					}
				}
			}

			if (selectionRect != null) {
				selectionRotater.stopRotate();
				selectionRect = null;
			}

			dragged = false;
			sr = null;
			pressed = false;
			pane.repaint();

			if (e.getButton() == MouseEvent.BUTTON3) {
				showPopup(e, x, y, adjustX, adjustY);
			}
		} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			if (pressed) {
				dragged = true;
				if (pressedPoint != null) {
					pressedPoint.getPath().getPath().doubleCoords[pressedPoint.getCoordIndex()] += x - mx;
					pressedPoint.getPath().getPath().doubleCoords[pressedPoint.getCoordIndex() + 1] += y - my;
					pressedPoint.getPath().invalidate();
					mx = x;
					my = y;
					pane.repaint();
				} else if (pressedObject != null) {
					for (TOIObject o : pane.getSelectedObjects()) {
						o.translate(x - mx, y - my);
					}
					mx = x;
					my = y;
					pane.repaint();
				}

				if (selectionRect != null) {
					double x1 = Math.min(mx, x);
					double y1 = Math.min(my, y);
					double x2 = Math.max(mx, x);
					double y2 = Math.max(my, y);
					selectionRect.setRect(x1, y1, x2 - x1, y2 - y1);
					pane.selectObjectsInRect(selectionRect);
					pane.repaint();
				}
			}
		} else if (e.getID() == MouseEvent.MOUSE_MOVED) {
			PathCoord point = pane.getPathCoord(x, y);
			if (point != null) {
				pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				TOIObject o = pane.getObject(x, y);
				if (o != null) {
					pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				} else {
					pane.setCursor(Cursor.getDefaultCursor());
				}
			}
		}
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
		AffineTransform t = null;
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				t = AffineTransform.getTranslateInstance(-1, 0);
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				t = AffineTransform.getTranslateInstance(1, 0);
			} else if (e.getKeyCode() == KeyEvent.VK_UP) {
				t = AffineTransform.getTranslateInstance(0, -1);
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				t = AffineTransform.getTranslateInstance(0, 1);
			} else if (e.getKeyCode() == KeyEvent.VK_TAB) {
				if (pressedPoint != null) {
					int index = pressedPoint.getCoordIndex();
					if (!e.isControlDown()) {
						index += 2;
						if (index >= pressedPoint.getPath().getPath().numCoords) {
							index = 0;
						}
					} else {
						index -= 2;
						if (index < 0) {
							index = pressedPoint.getPath().getPath().numCoords - 2;
						}
					}
					pressedPoint.setCoordIndex(index);
					pane.repaint();
				}
			}
		}

		if (t != null) {
			if (pressedPoint != null) {
				Point2D p = new Point2D.Double(pressedPoint.getPath().getPath().doubleCoords[pressedPoint.getCoordIndex()], pressedPoint.getPath().getPath().doubleCoords[pressedPoint.getCoordIndex() + 1]);
				t.transform(p, p);

				pressedPoint.getPath().getPath().doubleCoords[pressedPoint.getCoordIndex()] = p.getX();
				pressedPoint.getPath().getPath().doubleCoords[pressedPoint.getCoordIndex() + 1] = p.getY();
				pressedPoint.getPath().invalidate();
			} else {
				for (TOIObject o : pane.getSelectedObjects()) {
					o.transform(t);
				}
			}
			pane.repaint();
		}
		e.consume();
	}

	@Override
	public void paint(Graphics2D g, Graphics2D gt, TOIObject o, int type) {
		if (type == PAINT_UNDER_SELECTION) {
			// draw connected points for curves
			if (o instanceof TOIPath) {
				TOIPath path = (TOIPath) o;
				int coordIndex = 0;
				int[] curvesizes = { 2, 2, 4, 6, 0 };
				for (int i = 0; i < path.getPath().numTypes; i++) {
					int curvesize = curvesizes[path.getPath().pointTypes[i]];
					for (int j = 0; j < curvesize; j += 2) {
						double x = path.getPath().doubleCoords[coordIndex];
						double y = path.getPath().doubleCoords[coordIndex + 1];
						if (coordIndex > 0 && curvesize > 2) {
							double prevx = path.getPath().doubleCoords[coordIndex - 2];
							double prevy = path.getPath().doubleCoords[coordIndex - 1];
							Stroke oldStroke = g.getStroke();
							g.setStroke(editedStroke);
							g.setColor(Color.gray);
							g.draw(pane.getTransform().createTransformedShape(new Line2D.Double(prevx, prevy, x, y)));
							g.setStroke(oldStroke);
						}
						coordIndex += 2;
					}
				}
			}
		} else if (type == PAINT_OVER_SELECTION) {
			if (o instanceof TOIPath) {
				// draw path points
				TOIPath path = (TOIPath) o;
				int coordIndex = 0;
				int[] curvesizes = { 2, 2, 4, 6, 0 };
				for (int i = 0; i < path.getPath().numTypes; i++) {
					int curvesize = curvesizes[path.getPath().pointTypes[i]];
					for (int j = 0; j < curvesize; j += 2) {
						double x = path.getPath().doubleCoords[coordIndex];
						double y = path.getPath().doubleCoords[coordIndex + 1];
						g.setColor(j == curvesize - 2 ? Color.red : Color.white);
						double size = 5 / pane.getZoom();
						Shape rect = pane.getTransform().createTransformedShape(new Rectangle2D.Double(x - size / 2, y - size / 2, size, size));
						g.fill(rect);
						g.setColor(Color.blue);
						g.draw(rect);
						coordIndex += 2;
					}
				}
			}

			if (pressedPoint != null) {
				double x = pressedPoint.getPath().getPath().doubleCoords[pressedPoint.getCoordIndex()];
				double y = pressedPoint.getPath().getPath().doubleCoords[pressedPoint.getCoordIndex() + 1];
				double size = 9 / pane.getZoom();
				Shape rect = pane.getTransform().createTransformedShape(new Rectangle2D.Double(x - size / 2, y - size / 2, size, size));

				Stroke oldStroke = g.getStroke();
				g.setColor(Color.black);
				g.draw(rect);
				g.setStroke(selectedPointStroke);
				g.setColor(Color.white);
				g.draw(rect);
				g.setStroke(oldStroke);
			}
		} else if (type == PAINT_TOP) {
			if (selectionRect != null && selectionRotater.getStroke() != null) {
				Stroke oldStroke = g.getStroke();
				g.setStroke(selectionRotater.getStroke());
				g.setXORMode(Color.white);
				g.setColor(Color.black);
				g.draw(pane.getTransform().createTransformedShape(selectionRect));
				g.setPaintMode();
				g.setStroke(oldStroke);
			}
		}
	}

	@Override
	public void install(TOIPane pane) {
		this.pane = pane;
	}

	@Override
	public void uninstall() {
	}

	public void showPopup(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		JPopupMenu popup = new JPopupMenu();
		if (pressedPoint != null) {
			JMenu menuAddSegment = new JMenu("Вставить сегмент");
			popup.add(menuAddSegment);

			JMenuItem menuAddSegmentLine = new JMenuItem("Прямая линия");
			menuAddSegmentLine.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MutablePath path = pressedPoint.getPath().getPath();
					int segmentIndex = path.getSegmentIndex(pressedPoint.getCoordIndex()) + 1;
					int coordIndex = path.getCoordIndex(segmentIndex);
					double x = path.getCoord(coordIndex - 2);
					double y = path.getCoord(coordIndex - 1);
					pressedPoint.getPath().getPath().addSegment(segmentIndex, PathIterator.SEG_LINETO, new double[] { x, y });
					pressedPoint.getPath().invalidate();
					pane.repaint();
				}
			});
			menuAddSegment.add(menuAddSegmentLine);

			JMenuItem menuAddSegmentQuad = new JMenuItem("Квадратичная кривая");
			menuAddSegmentQuad.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MutablePath path = pressedPoint.getPath().getPath();
					int segmentIndex = path.getSegmentIndex(pressedPoint.getCoordIndex()) + 1;
					int coordIndex = path.getCoordIndex(segmentIndex);
					double x = path.getCoord(coordIndex - 2);
					double y = path.getCoord(coordIndex - 1);
					pressedPoint.getPath().getPath().addSegment(segmentIndex, PathIterator.SEG_QUADTO, new double[] { x, y, x, y });
					pressedPoint.getPath().invalidate();
					pane.repaint();
				}
			});
			menuAddSegment.add(menuAddSegmentQuad);

			JMenuItem menuAddSegmentCubic = new JMenuItem("Кубическая кривая");
			menuAddSegmentCubic.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MutablePath path = pressedPoint.getPath().getPath();
					int segmentIndex = path.getSegmentIndex(pressedPoint.getCoordIndex()) + 1;
					int coordIndex = path.getCoordIndex(segmentIndex);
					double x = path.getCoord(coordIndex - 2);
					double y = path.getCoord(coordIndex - 1);
					pressedPoint.getPath().getPath().addSegment(segmentIndex, PathIterator.SEG_CUBICTO, new double[] { x, y, x, y, x, y });
					pressedPoint.getPath().invalidate();
					pane.repaint();
				}
			});
			menuAddSegment.add(menuAddSegmentCubic);

			JMenuItem menuDeleteSegment = new JMenuItem("Удалить сегмент");
			menuDeleteSegment.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int segmentIndex = pressedPoint.getPath().getPath().getSegmentIndex(pressedPoint.getCoordIndex());
					pressedPoint.getPath().getPath().deleteSegment(segmentIndex);
					pressedPoint.getPath().invalidate();
					pane.repaint();
				}
			});
			popup.add(menuDeleteSegment);
		} else if (pressedObject != null) {

		}

		if (popup.getComponentCount() > 0) {
			popup.show(pane, e.getX(), e.getY());
		}
	}
}
