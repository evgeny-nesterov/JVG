package ru.nest.jvg.actionarea;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.event.UndoableEditEvent;
import javax.swing.menu.WMenu;
import javax.swing.menu.WMenuItem;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.action.PathDeleteCurveAction;
import ru.nest.jvg.action.PathDeleteCurvePointAction;
import ru.nest.jvg.event.JVGKeyEvent;
import ru.nest.jvg.event.JVGKeyListener;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGMouseListener;
import ru.nest.jvg.geom.CoordinablePathIterator;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.undoredo.PathGeomUndoRedo;

public class JVGPathGeomActionArea extends JVGActionArea implements JVGKeyListener {
	private final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(JVGPathGeomActionArea.class.getResource("cursors/cursor_move_point.png")).getImage(), new Point(15, 15), "pencil");

	private double oldX, oldY;

	private int pressedPointIndex = -1;

	private int focusedPointIndex = -1;

	public JVGPathGeomActionArea() {
		super(VISIBILITY_TYPE_FOCUSED);

		addMouseListener(new JVGMouseAdapter() {
			@Override
			public void mousePressed(JVGMouseEvent e) {
				x = e.getX();
				y = e.getY();

				focusedPointIndex = pressedPointIndex = -1;
				pressedButton = e.getButton();

				JVGComponent parent = getParent();
				if (parent instanceof JVGPath) {
					JVGPath path = (JVGPath) parent;
					focusedPointIndex = pressedPointIndex = path.getCoordIndex(x, y, 0);
					if (pressedPointIndex >= 0) {
						convertedX = oldX = path.getCoord(2 * pressedPointIndex);
						convertedY = oldY = path.getCoord(2 * pressedPointIndex + 1);
					}
				}
			}

			@Override
			public void mouseReleased(JVGMouseEvent e) {
				if (pressedPointIndex >= 0) {
					JVGComponent parent = getParent();
					if (parent instanceof JVGPath) {
						final JVGPath path = (JVGPath) parent;
						double newX = path.getCoord(2 * pressedPointIndex);
						double newY = path.getCoord(2 * pressedPointIndex + 1);
						if (oldX != newX || oldY != newY) {
							fireUndoableEditUpdate(new UndoableEditEvent(parent, new PathGeomUndoRedo(getPane(), path, pressedPointIndex, oldX, oldY, newX, newY)));
						}

						if (pressedButton == JVGMouseEvent.BUTTON3) {
							final int coordIndex = 2 * focusedPointIndex;
							final int curveIndex = path.getCurveIndex(coordIndex);
							final int curveCoordIndex = path.getCoordIndex(curveIndex);
							final int type = path.getType(curveIndex);
							final int curveSize = CoordinablePathIterator.curvesize[type];
							final boolean isLastCurvePoint = coordIndex == (curveCoordIndex + curveSize - 2);
							final double firstX = curveIndex > 0 ? path.getCoord(curveCoordIndex - 2) : 0;
							final double firstY = curveIndex > 0 ? path.getCoord(curveCoordIndex - 1) : 0;
							final double lastX = path.getCoord(curveCoordIndex + curveSize - 2);
							final double lastY = path.getCoord(curveCoordIndex + curveSize - 1);

							WMenu menuPopup = new WMenu();
							JPopupMenu popup = menuPopup.getPopupMenu();

							if (!isLastCurvePoint) {
								WMenuItem menuRemovePoint = new WMenuItem(lm.getValue("path-geom-action.remove-point", "Remove point"));
								menuRemovePoint.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										PathDeleteCurvePointAction action = new PathDeleteCurvePointAction(path, coordIndex);
										action.doAction();
									}
								});
								popup.add(menuRemovePoint);
							}

							if (isLastCurvePoint && curveIndex > 0) {
								WMenuItem menuRemoveCurve = new WMenuItem(lm.getValue("path-geom-action.remove-curve", "Remove curve"));
								menuRemoveCurve.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										PathDeleteCurveAction action = new PathDeleteCurveAction(path, curveIndex);
										action.doAction();
									}
								});
								popup.add(menuRemoveCurve);
							}

							// change path element
							if (coordIndex > 0) {
								WMenu menuChange = new WMenu(lm.getValue("path-geom-action.replace-element", "Replace"));
								popup.add(menuChange);

								if (type != PathIterator.SEG_MOVETO) {
									WMenuItem menuChangeToMove = new WMenuItem(lm.getValue("path-geom-action.replace-element-tomove", "To move"));
									menuChangeToMove.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											// TODO do through action
											path.setMoveTo(curveIndex, lastX, lastY);
											path.repaint();
										}
									});
									menuChange.add(menuChangeToMove);
								}

								if (type != PathIterator.SEG_LINETO) {
									WMenuItem menuChangeToLine = new WMenuItem(lm.getValue("path-geom-action.replace-element-toline", "To line"));
									menuChangeToLine.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											// TODO do through action
											path.setLineTo(curveIndex, lastX, lastY);
											path.repaint();
										}
									});
									menuChange.add(menuChangeToLine);
								}

								if (type != PathIterator.SEG_QUADTO) {
									WMenuItem menuChangeToQuad = new WMenuItem(lm.getValue("path-geom-action.replace-element-toquad", "To quad"));
									menuChangeToQuad.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											// TODO do through action
											path.setQuadTo(curveIndex, ((firstX + lastX) / 2), ((firstY + lastY) / 2), lastX, lastY);
											path.repaint();
										}
									});
									menuChange.add(menuChangeToQuad);
								}

								if (type != PathIterator.SEG_CUBICTO) {
									WMenuItem menuChangeToCurve = new WMenuItem(lm.getValue("path-geom-action.replace-element-tocurve", "To curve"));
									menuChangeToCurve.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											// TODO do through action
											path.setCurveTo(curveIndex, ((2 * firstX + lastX) / 3), ((2 * firstY + lastY) / 3), ((firstX + 2 * lastX) / 3), ((firstY + 2 * lastY) / 3), lastX, lastY);
											path.repaint();
										}
									});
									menuChange.add(menuChangeToCurve);
								}
							}

							// insert path element after current
							WMenu menuInsert = new WMenu(lm.getValue("path-geom-action.insert-element", "Insert"));
							popup.add(menuInsert);

							WMenuItem menuInsertMove = new WMenuItem(lm.getValue("path-geom-action.insert-element-move", "Move"));
							menuInsertMove.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									// TODO do through action
									path.insertMoveTo(curveIndex, lastX, lastY);
									path.repaint();
								}
							});
							menuInsert.add(menuInsertMove);

							WMenuItem menuInsertLine = new WMenuItem(lm.getValue("path-geom-action.insert-element-line", "Line"));
							menuInsertLine.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									// TODO do through action
									path.insertLineTo(curveIndex, lastX, lastY);
									path.repaint();
								}
							});
							menuInsert.add(menuInsertLine);

							WMenuItem menuInsertQuad = new WMenuItem(lm.getValue("path-geom-action.insert-element-quad", "Quad"));
							menuInsertQuad.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									// TODO do through action
									path.insertQuadTo(curveIndex, ((firstX + lastX) / 2), ((firstY + lastY) / 2), lastX, lastY);
									path.repaint();
								}
							});
							menuInsert.add(menuInsertQuad);

							WMenuItem menuInsertCurve = new WMenuItem(lm.getValue("path-geom-action.insert-element-curve", "Curve"));
							menuInsertCurve.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									// TODO do through action
									path.insertCurveTo(curveIndex, ((2 * firstX + lastX) / 3), ((2 * firstY + lastY) / 3), ((firstX + 2 * lastX) / 3), ((firstY + 2 * lastY) / 3), lastX, lastY);
									path.repaint();
								}
							});
							menuInsert.add(menuInsertCurve);

							MouseEvent oe = (MouseEvent) e.getOriginEvent();
							popup.show(oe.getComponent(), oe.getX(), oe.getY());
							e.consume();
						}
					}
				}

				dx = 0;
				dy = 0;
				pressedPointIndex = -1;
				repaint();
			}

			@Override
			public void mouseDragged(JVGMouseEvent e) {
				if (pressedButton == JVGMouseEvent.BUTTON1 && pressedPointIndex >= 0) {
					JVGComponent parent = getParent();
					if (parent instanceof JVGPath) {
						JVGPath path = (JVGPath) parent;
						double[] point = { e.getAdjustedX(), e.getAdjustedY() };
						path.getInverseTransform().transform(point, 0, point, 0, 1);
						double newConvertedX = point[0];
						double newConvertedY = point[1];

						x = e.getX();
						y = e.getY();
						dx = newConvertedX - convertedX;
						dy = newConvertedY - convertedY;

						if (dx != 0 || dy != 0) {
							path.setPoint(2 * pressedPointIndex, newConvertedX, newConvertedY);
							convertedX = newConvertedX + getDeltaX() - dx;
							convertedY = newConvertedY + getDeltaY() - dy;
							path.repaint();
						}
					}
				}
			}
		});

		setCursor(Cursor.getDefaultCursor());
		setName("path-geom");
	}

	private JVGMouseListener parentMouseListener = new JVGMouseAdapter() {
		@Override
		public void mousePressed(JVGMouseEvent e) {
			if (focusedPointIndex != -1) {
				focusedPointIndex = -1;
				repaint();
			}
		}
	};

	@Override
	public void addNotify() {
		super.addNotify();
		parent.addKeyListener(this);
		parent.addMouseListener(parentMouseListener);
	}

	@Override
	public void removeNotify() {
		parent.removeMouseListener(parentMouseListener);
		parent.removeKeyListener(this);
		super.removeNotify();
	}

	private boolean keyTranslationActive = false;

	@Override
	public void keyPressed(JVGKeyEvent e) {
		if (focusedPointIndex != -1) {
			JVGPane pane = getPane();
			double dx = !e.isControlDown() ? pane.getIncrement() : 1 / pane.getScaleX();
			double dy = !e.isControlDown() ? pane.getIncrement() : 1 / pane.getScaleY();
			switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					translate(focusedPointIndex, 0, -dy, !e.isControlDown());
					e.consume();
					break;

				case KeyEvent.VK_LEFT:
					translate(focusedPointIndex, -dx, 0, !e.isControlDown());
					e.consume();
					break;

				case KeyEvent.VK_DOWN:
					translate(focusedPointIndex, 0, dy, !e.isControlDown());
					e.consume();
					break;

				case KeyEvent.VK_RIGHT:
					translate(focusedPointIndex, dx, 0, !e.isControlDown());
					e.consume();
					break;

				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_TAB:
					if (!e.isShiftDown()) {
						selectNextPoint();
					} else {
						selectPrevPoint();
					}
					e.consume();
					break;

				case KeyEvent.VK_DELETE:
					e.consume();
					break;
			}
		}
	}

	private void selectNextPoint() {
		JVGComponent parent = getParent();
		if (parent instanceof JVGPath) {
			JVGPath path = (JVGPath) parent;
			focusedPointIndex = (focusedPointIndex + 1) % (path.getNumCoords() / 2);
			repaint();
		}
	}

	private void selectPrevPoint() {
		JVGComponent parent = getParent();
		if (parent instanceof JVGPath) {
			JVGPath path = (JVGPath) parent;
			focusedPointIndex--;
			if (focusedPointIndex == -1) {
				focusedPointIndex = path.getNumCoords() / 2;
			}
			repaint();
		}
	}

	@Override
	public void keyReleased(JVGKeyEvent e) {
		if (keyTranslationActive) {
			JVGComponent parent = getParent();
			if (parent instanceof JVGPath) {
				final JVGPath path = (JVGPath) parent;
				double newX = path.getCoord(2 * focusedPointIndex);
				double newY = path.getCoord(2 * focusedPointIndex + 1);
				if (oldX != newX || oldY != newY) {
					fireUndoableEditUpdate(new UndoableEditEvent(parent, new PathGeomUndoRedo(getPane(), path, focusedPointIndex, oldX, oldY, newX, newY)));
				}
			}
			keyTranslationActive = false;
		}
	}

	@Override
	public void keyTyped(JVGKeyEvent e) {
	}

	private void translate(int pointIndex, double dx, double dy, boolean adjust) {
		if (dx != 0 || dy != 0) {
			JVGComponent parent = getParent();
			if (parent instanceof JVGPath) {
				JVGPath path = (JVGPath) parent;
				int coordIndex = 2 * pointIndex;

				Point2D p = new Point2D.Double(path.getCoord(coordIndex), path.getCoord(coordIndex + 1));
				if (!keyTranslationActive) {
					keyTranslationActive = true;
					this.oldX = p.getX();
					this.oldY = p.getY();
				}

				path.getTransform().transform(p, p);
				p.setLocation(p.getX() + dx, p.getY() + dy);
				if (adjust) {
					getPane().adjustPoint(p);
				}
				path.getInverseTransform().transform(p, p);

				path.setPoint(coordIndex, p.getX(), p.getY());
				path.repaint();
			}
		}
	}

	private int pressedButton;

	public int getIndex(double x, double y) {
		JVGComponent parent = getParent();
		if (parent instanceof JVGPath) {
			JVGPath path = (JVGPath) parent;
			return path.getCoordIndex(x, y, 0);
		}
		return -1;
	}

	private double dx, dy, x, y, convertedX, convertedY;

	public double getDeltaX() {
		return dx;
	}

	public double getDeltaY() {
		return dy;
	}

	@Override
	protected Shape computeActionBounds() {
		return null;
	}

	@Override
	public boolean contains(double x, double y) {
		if (!isActive() || (getActionsFilter() != null && !getActionsFilter().pass(this))) {
			return false;
		} else {
			int index = getIndex(x, y);
			return index >= 0;
		}
	}

	private final static Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] { 5f, 5f }, 0f);

	private final static Stroke strokeFocus = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] { 1f, 1f }, 0f);

	private final static Stroke strokeFocusedCurve = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] { 1f, 2f }, 0f);

	@Override
	public void paintAction(Graphics2D g, AffineTransform transform) {
		// TODO transform
		JVGComponent parent = getParent();
		if (parent instanceof JVGPath) {
			JVGPath path = (JVGPath) parent;
			MutableGeneralPath shape = path.getTransformedPathShape();
			Stroke oldStroke = g.getStroke();

			// draw lines
			double lastX = 0, lastY = 0;
			int coord = 0;
			for (int t = 0; t < shape.numTypes; t++) {
				int type = shape.pointTypes[t];
				int numcoords = CoordinablePathIterator.curvesize[type];
				for (int i = 0; i < numcoords; i += 2) {
					double x = shape.pointCoords[coord++];
					double y = shape.pointCoords[coord++];
					if (type == PathIterator.SEG_QUADTO || type == PathIterator.SEG_CUBICTO || (type == PathIterator.SEG_MOVETO && i > 2)) {
						g.setStroke(stroke);
						g.setColor(Color.gray);
						g.draw(transform.createTransformedShape(new Line2D.Double(lastX, lastY, x, y)));
						g.setStroke(oldStroke);
					}
					lastX = x;
					lastY = y;
				}
			}

			// draw focused curve and point
			if (focusedPointIndex >= 0) {
				// draw focused curve
				if (focusedPointIndex > 0) {
					int coordIndex = 2 * focusedPointIndex;
					int curveIndex = path.getCurveIndex(coordIndex);
					int curveCoordIndex = path.getCoordIndex(curveIndex);
					int type = path.getType(curveIndex);

					int i = curveCoordIndex - 2;
					Shape curve = null;
					switch (type) {
						case PathIterator.SEG_MOVETO:
						case PathIterator.SEG_LINETO:
							curve = transform.createTransformedShape(new Line2D.Double(shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++]));
							break;
						case PathIterator.SEG_QUADTO:
							curve = transform.createTransformedShape(new QuadCurve2D.Double(shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++]));
							break;
						case PathIterator.SEG_CUBICTO:
							curve = transform.createTransformedShape(new CubicCurve2D.Double(shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++], shape.pointCoords[i++]));
							break;
					}

					if (curve != null) {
						g.setColor(Color.white);
						g.draw(curve);
						g.setStroke(strokeFocusedCurve);
						g.setColor(Color.blue);
						g.draw(curve);
						g.setStroke(oldStroke);
					}
				}

				// draw focused point
				double x = shape.pointCoords[2 * focusedPointIndex];
				double y = shape.pointCoords[2 * focusedPointIndex + 1];
				if (isActionActive()) {
					g.setXORMode(Color.white);
					g.setColor(Color.lightGray);
					g.draw(transform.createTransformedShape(new Line2D.Double(-10000, y, 10000, y)));
					g.draw(transform.createTransformedShape(new Line2D.Double(x, -10000, x, 10000)));
					g.draw(transform.createTransformedShape(new Line2D.Double(x - 10000, y - 10000, x + 10000, y + 10000)));
					g.draw(transform.createTransformedShape(new Line2D.Double(x - 10000, y + 10000, x + 10000, y - 10000)));
					g.setPaintMode();
				} else {
					double w = 7 / transform.getScaleX();
					double h = 7 / transform.getScaleX();
					Shape rect = transform.createTransformedShape(new Rectangle2D.Double(x - w / 2, y - h / 2, w, h));
					g.setColor(Color.white);
					g.draw(rect);
					g.setStroke(strokeFocus);
					g.setColor(Color.black);
					g.draw(rect);
					g.setStroke(oldStroke);
				}
			}

			// draw points
			coord = 0;
			for (int t = 0; t < shape.numTypes; t++) {
				int type = shape.pointTypes[t];
				int numcoords = CoordinablePathIterator.curvesize[type];
				double w = 3 / transform.getScaleX();
				double h = 3 / transform.getScaleX();
				for (int i = 0; i < numcoords; i += 2) {
					double x = shape.pointCoords[coord++];
					double y = shape.pointCoords[coord++];
					Shape rect = transform.createTransformedShape(new Rectangle2D.Double(x - w / 2, y - h / 2, w, h));

					g.setColor(Color.white);
					g.fill(rect);
					if (i == numcoords - 2) {
						g.setColor(Color.red);
					} else {
						g.setColor(Color.blue);
					}
					g.draw(rect);
				}
			}
		}
	}

	@Override
	public Cursor getCursor() {
		return cursor;
	}
}
