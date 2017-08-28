package satis.iface.graph;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import satis.iface.graph.def.DefaultDinamicPlotRenderer;
import satis.iface.graph.def.DefaultGrid;
import satis.iface.graph.def.DefaultGridRenderer;
import satis.iface.graph.def.DefaultPlotModel;
import satis.iface.graph.def.DefaultPlotRenderer;
import satis.iface.graph.grid.Grid;
import satis.iface.graph.paintobjects.AbstractActivePaintObject;
import satis.iface.graph.paintobjects.ActivePaintObject;
import satis.iface.graph.paintobjects.PaintObject;
import satis.iface.graph.paintobjects.Text;

public class DinamicGraphArea extends StaticGraphArea implements ActionListener {
	private static final long serialVersionUID = 1L;

	public final static int MARKER_ON_DRAG = 0;

	public final static int MOVE_ON_DRAG = 1;

	public final static int SELECT_ON_DRAG = 2;

	private Color selectedColor_Fill = new Color(180, 180, 180, 128);

	private Color selectedColor_Border = new Color(100, 100, 100);

	public DinamicGraphArea() {
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
	}

	@Override
	public void updateOnResizing() {
		if (isSelectionActive()) {
			double xKoef = getWidth() / oldWidth;
			double yKoef = getHeight() / oldHeight;
			setSelectionBounds(mouseX * xKoef, mouseY * yKoef, selectedWidth * xKoef, selectedHeight * yKoef);
		}

		super.updateOnResizing();
	}

	@Override
	public void compileGroup(Group group) {
		super.compileGroup(group);

		synchronized (group.plots) {
			for (Plot p : group.plots) {
				if (p instanceof DinamicPlot) {
					DinamicPlot plot = (DinamicPlot) p;
					if (plot.isDrawMarker()) {
						plot.updateMarker();
					}
				}
			}
		}
	}

	public void setMarkers(double markerX, double markerY) {
		boolean changed = false;
		for (Group group : groups) {
			for (Plot p : group.plots) {
				if (p instanceof DinamicPlot) {
					DinamicPlot plot = (DinamicPlot) p;
					changed |= markerX != plot.getMarkerX();
					plot.setMarker(group, markerX, markerY);
				}
			}
		}

		if (changed) {
			fireMarkersMoved();
		}
	}

	public void setMarkers(double realMarkerX) {
		boolean changed = false;
		for (Group group : groups) {
			for (Plot p : group.plots) {
				if (p instanceof DinamicPlot) {
					DinamicPlot plot = (DinamicPlot) p;
					changed |= realMarkerX != plot.getRealMarkerX();
					plot.setMarkerByReal(group, realMarkerX);
				}
			}
		}

		if (changed) {
			fireMarkersMoved();
		}
	}

	public void setMarkers(int markerIndex) {
		boolean changed = false;
		for (Group group : groups) {
			for (Plot p : group.plots) {
				if (p instanceof DinamicPlot) {
					DinamicPlot plot = (DinamicPlot) p;
					changed |= markerIndex != plot.getMarkerIndex();
					plot.setMarker(markerIndex);
				}
			}
		}

		if (changed) {
			fireMarkersMoved();
		}
	}

	public void updateMarkers() {
		boolean changed = false;
		for (Group group : groups) {
			for (Plot p : group.plots) {
				if (p instanceof DinamicPlot) {
					DinamicPlot plot = (DinamicPlot) p;

					double markerX = plot.getMarkerX();
					double markerY = plot.getMarkerY();
					plot.updateMarker();
					changed |= markerX != plot.getMarkerX() || markerY != plot.getMarkerY();
				}
			}
		}

		if (changed) {
			fireMarkersMoved();
		}
	}

	@Override
	public boolean isFocusable() {
		return true;
	}

	private double koef = 1.0 / 100.0;

	public void setShiftKoef(double koef) {
		this.koef = koef;
	}

	public double getShiftKoef() {
		return koef;
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
		super.processKeyEvent(e);

		if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				hideMarkers();
			} else if (e.getKeyCode() == KeyEvent.VK_F1) {
				setDragActionType(MARKER_ON_DRAG);
			} else if (e.getKeyCode() == KeyEvent.VK_F2) {
				setDragActionType(MOVE_ON_DRAG);
			} else if (e.getKeyCode() == KeyEvent.VK_F3) {
				setDragActionType(SELECT_ON_DRAG);
			} else if (e.getKeyCode() == KeyEvent.VK_F4) {
				setActualBounds();
			} else if (isScaleGraph) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_PLUS:
					case KeyEvent.VK_EQUALS:
						expand(-koef, -koef, koef, koef);
						break;

					case KeyEvent.VK_MINUS:
						expand(koef, koef, -koef, -koef);
						break;

					case KeyEvent.VK_4:
					case KeyEvent.VK_RIGHT:
						shiftPercent(-koef, 0);
						break;

					case KeyEvent.VK_6:
					case KeyEvent.VK_LEFT:
						shiftPercent(koef, 0);
						break;

					case KeyEvent.VK_8:
					case KeyEvent.VK_UP:
						shiftPercent(0, koef);
						break;

					case KeyEvent.VK_2:
					case KeyEvent.VK_DOWN:
						shiftPercent(0, -koef);
						break;
				}
			}
		}
	}

	public void hideMarkers() {
		boolean changed = false;
		for (Group group : groups) {
			for (Plot p : group.plots) {
				if (p instanceof DinamicPlot) {
					DinamicPlot plot = (DinamicPlot) p;
					changed |= plot.isDrawMarker();
					plot.setDrawMarker(false);
				}
			}
		}

		if (changed) {
			fireMarkersHided();
			repaint();
		}
	}

	public void setActualBounds() {
		setActualBounds(true, true);
	}

	public void setActualBounds(boolean isX, boolean isY) {
		for (Group group : groups) {
			group.setActualBounds(isX, isY);
		}

		fireGroupsRescaled();
		setSelectionSize(0, 0);
		repaintImage();
		repaint();
	}

	public void setInnerBounds(double x, double y, double w, double h) {
		for (Group group : groups) {
			group.setInnerBounds(x, resize_vertically ? y : Double.MAX_VALUE, w, resize_vertically ? h : Double.MAX_VALUE);
		}

		fireGroupsRescaled();
		setSelectionSize(0, 0);
		repaintImage();
		repaint();
	}

	public void setBounds(double x, double y, double w, double h) {
		for (Group group : groups) {
			group.setBounds(x, resize_vertically ? y : Double.MAX_VALUE, w, resize_vertically ? h : Double.MAX_VALUE);
			group.setActualBounds();
		}

		fireGroupsRescaled();
		setSelectionSize(0, 0);
		repaintImage();
		repaint();
	}

	private double mouseX, mouseY, selectedWidth, selectedHeight, oldWidth, oldHeight;

	public double getSelectionX() {
		return mouseX;
	}

	public double getSelectionY() {
		return mouseY;
	}

	public double getSelectionWidth() {
		return selectedWidth;
	}

	public double getSelectionHeight() {
		return selectedHeight;
	}

	public boolean isSelectionActive() {
		return isSelectGraph && selectedWidth != 0 && selectedHeight != 0;
	}

	private void setSelectionLocation(double x, double y) {
		mouseX = x;
		if (resize_vertically) {
			mouseY = y;
		} else {
			mouseY = 0;
		}
	}

	private void setSelectionSize(double w, double h) {
		selectedWidth = w;
		if (resize_vertically) {
			selectedHeight = h;
		} else {
			selectedHeight = getHeight();
		}
		oldWidth = getWidth();
		oldHeight = getHeight();
	}

	private void setSelectionBounds(double x, double y, double w, double h) {
		setSelectionLocation(x, y);
		setSelectionSize(w, h);
	}

	private boolean canSelect = true;

	@Override
	public void processMouseEvent(MouseEvent e) {
		if (currentActivePaintObject != null) {
			currentActivePaintObject.processMouseEvent(currentActiveGroup, e);
			if (e.isConsumed()) {
				return;
			}
		}

		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			requestFocus();

			if (e.getButton() == MouseEvent.BUTTON1) {
				switch (dragActionType) {
					case MARKER_ON_DRAG:
						if (isShowMarkers) {
							setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							setMarkers(e.getX(), e.getY());
							repaint();
						}
						break;

					case MOVE_ON_DRAG:
						if (isDragGraph) {
							setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
							setSelectionLocation(e.getX(), e.getY());
						}
						break;

					case SELECT_ON_DRAG:
						if (isSelectGraph) {
							boolean isStretched = false;
							if (selectedWidth != 0 && selectedHeight != 0) {
								double w = selectedWidth, x = mouseX;
								if (selectedWidth < 0) {
									w *= -1;
									x -= w;
								}

								double h = selectedHeight, y = mouseY;
								if (selectedHeight < 0) {
									h *= -1;
									y -= h;
								}

								if (e.getX() >= x && e.getX() <= x + w && e.getY() >= y && e.getY() <= y + h) {
									// Stretch selected
									stretch(x, y, w, h);
									isStretched = true;
									setSelectionSize(0, 0);
								}
							}

							canSelect = !isStretched;

							if (!isStretched) {
								// Begin selecting
								setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
								setSelectionBounds(e.getX(), e.getY(), 0, 0);
							}
						}
						break;
				}
			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				setCursor(Cursor.getDefaultCursor());
				canSelect = true;
			}
		}

		super.processMouseEvent(e);
	}

	public ActivePaintObject paintObjectAt(int x, int y) {
		for (Group group : groups) {
			ActivePaintObject ao = paintObjectAt(group, x, y);
			if (ao != null) {
				return ao;
			}
		}

		return null;
	}

	private void findPaintObject(int x, int y) {
		currentActivePaintObject = null;
		currentActiveGroup = null;

		for (Group group : groups) {
			ActivePaintObject ao = paintObjectAt(group, x, y);
			if (ao != null) {
				currentActivePaintObject = ao;
				currentActiveGroup = group;
				return;
			}
		}
	}

	public ActivePaintObject paintObjectAt(Group group, int x, int y) {
		for (PaintObject o : group.paintObjects) {
			try {
				if (o instanceof ActivePaintObject) {
					ActivePaintObject ao = (ActivePaintObject) o;
					if (ao.isVisible() && ao.contains(x, y)) {
						return ao;
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}

		return null;
	}

	private int dragActionType = MARKER_ON_DRAG;

	public void setDragActionType(int dragActionType) {
		this.dragActionType = dragActionType;

		if (selectedWidth != 0 && selectedHeight != 0) {
			setSelectionSize(0, 0);
			repaint();
		}
	}

	public int getDragActionType() {
		return dragActionType;
	}

	@Override
	public void processMouseWheelEvent(MouseWheelEvent e) {
		super.processMouseWheelEvent(e);

		for (Group group : groups) {
			for (Plot p : group.plots) {
				if (p instanceof DinamicPlot) {
					DinamicPlot plot = (DinamicPlot) p;
					if (plot.isDrawMarker()) {
						int index = plot.getMarkerIndex();
						if (e.getWheelRotation() != 0) {
							index += e.getWheelRotation();
							if (e.getWheelRotation() > 0 && plot.getMarkerX() != plot.getModel().getX(plot.getMarkerIndex())) {
								index--;
							}

							if (index < 0) {
								index = 0;
							} else if (index > plot.getModel().size() - 1) {
								index = plot.getModel().size() - 1;
							}
							plot.setMarker(index);
						}
					}
				}
			}
		}

		fireMarkersMoved();
		repaint();
	}

	@Override
	public void processMouseMotionEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_MOVED) {
			ActivePaintObject oldActivePaintObject = currentActivePaintObject;
			Group oldActiveGroup = currentActiveGroup;

			findPaintObject(e.getX(), e.getY());

			if (oldActivePaintObject != currentActivePaintObject) {
				if (oldActivePaintObject != null) {
					oldActivePaintObject.processMouseEvent(oldActiveGroup, new MouseEvent(e.getComponent(), MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));
				}

				if (currentActivePaintObject != null) {
					currentActivePaintObject.processMouseEvent(currentActiveGroup, new MouseEvent(e.getComponent(), MouseEvent.MOUSE_ENTERED, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));
				}

				repaintImage();
			}
		}

		if (currentActivePaintObject != null) {
			currentActivePaintObject.processMouseEvent(currentActiveGroup, e);
			if (e.isConsumed()) {
				return;
			}
		}

		if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			switch (dragActionType) {
				case MARKER_ON_DRAG:
					if (isShowMarkers) {
						setMarkers(e.getX(), e.getX());
					}
					break;

				case MOVE_ON_DRAG:
					if (isDragGraph) {
						shift(e.getX() - mouseX, e.getY() - mouseY);
						setSelectionLocation(e.getX(), e.getY());
					}
					break;

				case SELECT_ON_DRAG:
					if (isSelectGraph && canSelect) {
						setSelectionSize(e.getX() - mouseX, e.getY() - mouseY);
					}
					break;
			}
			repaint();
		}

		super.processMouseMotionEvent(e);
	}

	private Bounds transformBounds = new Bounds();

	public void transformGroup(Group group, double x, double y, double w, double h, boolean changeScale) {
		try {
			transformBounds.x = x;
			transformBounds.y = resize_vertically ? y : Double.MAX_VALUE;
			transformBounds.w = w;
			transformBounds.h = resize_vertically ? h : Double.MAX_VALUE;

			checkBounds(transformBounds, group.getBounds(), changeScale);
			group.setInnerBounds(transformBounds);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void expand(double topKoef, double leftKoef, double bottomKoef, double rightKoef) {
		for (Group group : groups) {
			Bounds b = group.getInnerBounds();
			transformGroup(group, b.x + b.w * leftKoef, b.y + b.h * topKoef, b.w * (1 + rightKoef - leftKoef), b.h * (1 + bottomKoef - topKoef), true);
		}

		fireGroupsRescaled();
		repaintImage();
		repaint();
	}

	public void stretch(double x, double y, double w, double h) // pixels
	{
		double screenWidth = getWidth();
		double screenHeight = getHeight();
		if (screenWidth > 0 && screenHeight > 0) {
			for (Group group : groups) {
				Bounds b = group.getInnerBounds();
				double kx = b.getW() / screenWidth;
				double ky = b.getH() / screenHeight;

				transformGroup(group, b.getX() + kx * x, b.getY() + ky * (screenHeight - (y + h)), kx * w, ky * h, true);
			}

			fireGroupsRescaled();
			repaintImage();
			repaint();
		}
	}

	public void transform(double x, double y, double w, double h) {
		for (Group group : groups) {
			transformGroup(group, x, y, w, h, true);
		}

		fireGroupsRescaled();
		repaintImage();
		repaint();
	}

	public void transformHorizontal(double x, double w) {
		for (Group group : groups) {
			Bounds b = group.getInnerBounds();
			transformGroup(group, x, b.y, w, b.h, true);
		}

		fireGroupsRescaled();
		repaintImage();
		repaint();
	}

	public void centrate(double centerX) {
		for (Group group : groups) {
			Bounds b = group.getInnerBounds();
			transformGroup(group, centerX - b.w / 2, b.y, b.w, b.h, true);
		}

		fireGroupsRescaled();
		repaintImage();
		repaint();
	}

	public void shiftPercent(double koefX, double koefY) // koef = [0, 1]
	{
		for (Group group : groups) {
			Bounds b = group.getInnerBounds();
			transformGroup(group, b.getX() - b.getW() * koefX, b.getY() + b.getH() * koefY, b.getW(), b.getH(), false);
		}

		fireGroupsRescaled();

		// double dx = koefX * getWidth();
		// double dy = koefY * getHeight();
		// scrollImage((int) dx, (int) dy); ????

		repaintImage();
		repaint();
	}

	public void shift(double dx, double dy) // pixels
	{
		double screenWidth = getWidth();
		double screenHeight = getHeight();
		if (screenWidth > 0 && screenHeight > 0) {
			double kx = dx / screenWidth;
			double ky = dy / screenHeight;
			shiftPercent(kx, ky);
			// for (Group group : groups)
			// {
			// Bounds b = group.getInnerBounds();
			// transformGroup(group,
			// b.getX() - b.getW() * kx,
			// b.getY() + b.getH() * ky,
			// b.getW(),
			// b.getH(), false);
			// }
			//
			// fireGroupsRescaled();
			// repaintImage();
			// repaint();
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (currentActivePaintObject != null) {
			currentActivePaintObject.paintActive(currentActiveGroup, g);
		}

		// marker is always over other graphics objects
		if (isShowMarkers) {
			for (Group group : groups) {
				for (Plot p : group.plots) {
					if (p.isVisible() && p instanceof DinamicPlot) {
						DinamicPlot plot = (DinamicPlot) p;
						paintMarker(g, group, plot);
					}
				}
			}
		}

		if (isSelectionActive()) {
			int w = (int) selectedWidth, x = (int) mouseX;
			if (selectedWidth < 0) {
				w *= -1;
				x -= w;
			}

			int h = (int) selectedHeight, y = (int) mouseY;
			if (selectedHeight < 0) {
				h *= -1;
				y -= h;
			}

			g.setColor(selectedColor_Fill);
			g.fillRect(x, y, w, h);

			g.setColor(selectedColor_Border);
			javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
		}
	}

	protected void paintMarker(Graphics g, Group group, DinamicPlot plot) {
		if (plot.isDrawMarker()) {
			PlotRenderer renderer = plot.getPlotRenderer();
			if (renderer instanceof DinamicPlotRenderer) {
				DinamicPlotRenderer pb = (DinamicPlotRenderer) renderer;
				pb.paintMarker(g, group, plot);
			}
		}
	}

	public static DinamicGraphArea getGraphExample() {
		final DinamicGraphArea g = new DinamicGraphArea();
		g.setBackground(Color.white);

		try {
			Random ran = new Random();
			final double[][] y = new double[4][400];
			final double[][] x = new double[4][400];

			for (int i = 0; i < x[0].length; i++) {
				double xx = i / 200.0;
				double x1 = i * Math.pow(10, 7) + Math.pow(10, 12);
				if (Math.sin((1 + 2 * Math.sin(Math.PI * xx) * 2) * xx) >= 0) {
					y[0][i] = 10.0 + 3.0 * (ran.nextDouble() - 0.5);
				} else {
					y[0][i] = 5.0 + 2.0 * (ran.nextDouble() - 0.5);
				}

				int N = 100;

				x[0][i] = x1;
				x[1][i] = x1;
				x[2][i] = x1;
				x[3][i] = x1;

				if (i % N >= 40 && i % N <= 60) {
					y[0][i] = 20.0 + 2.1 * (ran.nextDouble() - 0.5);
				} else if (i % N >= 20 && i % N < 40) {
					y[0][i] = 10.0 + 10.0 * (i % N - 20) / 20.0 + 5.0 * (ran.nextDouble() - 0.5);
				} else if (i % N > 60 && i % N <= 80) {
					y[0][i] = 20.0 - 10.0 * (i % N - 60) / 20.0 + 5.0 * (ran.nextDouble() - 0.5);
				} else {
					y[0][i] = 10.0 + 1.1 * (ran.nextDouble() - 0.5);
				}

				y[1][i] = 50.0 - 12.0 * xx + 2 * (ran.nextDouble() - 0.5);
				y[2][i] = 100 + Math.exp(xx) + 1 + 2 * Math.sin(Math.PI * xx) + 2 * (ran.nextDouble() - 0.5);
				y[3][i] = 2000.0 + 500.0 * Math.sin(Math.PI * xx * 3);
			}

			DefaultDinamicPlotRenderer r1 = new DefaultDinamicPlotRenderer();
			r1.setPlotColor(Color.red);
			r1.setMarkerColor(Color.darkGray);

			DefaultDinamicPlotRenderer r2 = new DefaultDinamicPlotRenderer();
			r2.setPlotColor(Color.blue);
			r2.setMarkerColor(Color.black);

			DefaultPlotRenderer r3 = new DefaultPlotRenderer();
			r3.setPlotColor(Color.black);

			DefaultPlotRenderer r4 = new DefaultPlotRenderer();
			r4.setPlotColor(Color.green);

			DinamicPlot p1 = new DinamicPlot();
			p1.setTitle("First");
			p1.setRenderer(r1);
			DefaultPlotModel m1 = (DefaultPlotModel) p1.getModel();
			m1.setData(x[0], y[0]);

			Plot p2 = new Plot();
			p2.setTitle("Second");
			p2.setRenderer(r3);
			DefaultPlotModel m2 = (DefaultPlotModel) p2.getModel();
			m2.setData(x[1], y[1]);

			Plot p3 = new Plot();
			p3.setTitle("Third");
			p3.setRenderer(r4);
			DefaultPlotModel m3 = (DefaultPlotModel) p3.getModel();
			m3.setData(x[2], y[2]);

			DinamicPlot p4 = new DinamicPlot();
			p4.setTitle("Fourth");
			p4.setRenderer(r2);
			DefaultPlotModel m4 = (DefaultPlotModel) p4.getModel();
			m4.setData(x[3], y[3]);

			Group group1 = new Group();
			group1.addPlot(p1);
			group1.addPlot(p2);
			group1.addPlot(p3);

			Text text1 = new Text("Graphics", x[0][200], 40);
			Text text2 = new Text("Graphics", x[0][200], 60);
			Text text3 = new Text("Graphics", x[0][200], 80);
			text2.setHorizontalAlignment(Text.LEFT);
			text3.setHorizontalAlignment(Text.RIGHT);
			// group1.addPaintObject(text1);
			// group1.addPaintObject(text2);
			// group1.addPaintObject(text3);
			group1.addPaintObject(new AbstractActivePaintObject(false) {
				int ix = 100, iy = 100;

				double X = x[0][200], Y = y[0][200];

				@Override
				public void translate(double dx, double dy) {
					X += dx;
					Y += dy;
				}

				@Override
				public boolean contains(int x, int y) {
					return x >= ix - 2 && x <= ix + 2 && y >= iy - 2 && y <= iy + 2;
				}

				@Override
				public void paint(Group group, Graphics g) {
					g.setColor(Color.green);
					g.fillRect(ix - 2, iy - 2, 5, 5);
				}

				public void paintActive(Graphics g) {
					g.setColor(Color.black);
					g.drawRect(ix - 2, iy - 2, 5, 5);
				}

				@Override
				public void compile(Group group) {
					ix = (int) group.modelToViewX(X);
					iy = (int) group.modelToViewY(Y);
				}

				private Cursor cursor;

				@Override
				public void processMouseEvent(Group group, MouseEvent e) {
					super.processMouseEvent(group, e);

					if (e.getID() == MouseEvent.MOUSE_ENTERED) {
						cursor = g.getCursor();
						g.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					} else if (e.getID() == MouseEvent.MOUSE_EXITED) {
						g.setCursor(cursor);
					}
				}
			});
			satis.iface.graph.paintobjects.Polygon p = new satis.iface.graph.paintobjects.Polygon(new double[] { x[0][100], x[0][200], x[0][300], x[0][200] }, new double[] { 40, 80, 60, 40 });
			p.setColor(new Color(255, 0, 0, 128));
			p.setPaintOver(true);
			p.setFill(true);
			// group1.addPaintObject(p);

			DefaultGrid grid = new DefaultGrid(Grid.Y_AXIS, true, false);
			grid.setAddLineOnThreshhold(true);
			grid.setGridCount(6);
			grid.setMainGridPeriod(1);
			grid.setGridPoint(-12);
			grid.setPaintGrid(true);
			((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.red);
			((DefaultGridRenderer) grid.getRenderer()).setFont(g.getFont());
			group1.setGridY(grid);

			grid = new DefaultGrid(Grid.X_AXIS, true, false);
			grid.setAddLineOnThreshhold(true);
			grid.setGridCount(6);
			grid.setMainGridPeriod(1);
			grid.setPaintGrid(true);
			((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.red);
			((DefaultGridRenderer) grid.getRenderer()).setFont(g.getFont());
			group1.setGridX(grid);

			Group group2 = new Group();
			group2.addPlot(p4);

			grid = new DefaultGrid(Grid.Y_AXIS, true, false);
			grid.setGridCount(4);
			grid.setMainGridPeriod(1);
			grid.setPaintGrid(false);
			((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.blue);
			((DefaultGridRenderer) grid.getRenderer()).setFont(g.getFont());
			group2.setGridY(grid);

			grid = new DefaultGrid(Grid.X_AXIS, true, false);
			grid.setGridCount(4);
			grid.setMainGridPeriod(1);
			grid.setPaintGrid(false);
			((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.blue);
			((DefaultGridRenderer) grid.getRenderer()).setFont(g.getFont());
			group2.setGridX(grid);

			g.addGroup(group1);
			g.addGroup(group2);

			group1.setBounds(x[0][0], 0, x[0][x[0].length - 1] - x[0][0], 120);
			group2.setBounds(x[0][0], 1000, x[0][x[0].length - 1] - x[0][0], 1700);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return g;
	}

	// --- Marker listener ---
	private List<MarkerListener> markerListeners = new ArrayList<MarkerListener>();

	public void addMarkerListener(MarkerListener listener) {
		if (listener != null) {
			synchronized (markerListeners) {
				markerListeners.add(listener);
			}
		}
	}

	public void removeMarkerListener(MarkerListener listener) {
		if (listener != null) {
			synchronized (markerListeners) {
				markerListeners.remove(listener);
			}
		}
	}

	public void fireMarkersHided() {
		synchronized (markerListeners) {
			for (MarkerListener listener : markerListeners) {
				listener.markersHided();
			}
		}
	}

	public void fireMarkersMoved() {
		synchronized (markerListeners) {
			for (MarkerListener listener : markerListeners) {
				listener.markersMoved();
			}
		}
	}

	private boolean isScaleGraph = true;

	public boolean isScaleGraph() {
		return isScaleGraph;
	}

	public void setScaleGraph(boolean isScaleGraph) {
		this.isScaleGraph = isScaleGraph;
	}

	private boolean isDragGraph = true;

	public boolean isDragGraph() {
		return isDragGraph;
	}

	public void setDragGraph(boolean isDragGraph) {
		this.isDragGraph = isDragGraph;
	}

	private boolean isSelectGraph = true;

	public boolean isSelectGraph() {
		return isSelectGraph;
	}

	public void setSelectGraph(boolean isSelectGraph) {
		this.isSelectGraph = isSelectGraph;
	}

	private boolean isShowMarkers = true;

	public boolean isShowMarkers() {
		return isShowMarkers;
	}

	public void setShowMarkers(boolean isShowMarkers) {
		this.isShowMarkers = isShowMarkers;
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setBounds(100, 100, 600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(getGraphExample(), BorderLayout.CENTER);
		f.setVisible(true);
		f.toFront();
	}

	private boolean isLimitExpandHorizontal = true;

	public boolean isLimitExpandHorizontal() {
		return isLimitExpandHorizontal;
	}

	public void setLimitExpandHorizontal(boolean isLimitExpandHorizontal) {
		this.isLimitExpandHorizontal = isLimitExpandHorizontal;
	}

	private boolean isLimitExpandVertical = false;

	public boolean isLimitExpandVertical() {
		return isLimitExpandVertical;
	}

	public void setLimitExpandVertical(boolean isLimitExpandVertical) {
		this.isLimitExpandVertical = isLimitExpandVertical;
	}

	public void checkBounds(Bounds bounds, Bounds limits, boolean changeScale) {
		if (isLimitExpandHorizontal) {
			if (bounds.w > limits.w) {
				bounds.x = limits.x;
				bounds.w = limits.w;
			} else if (bounds.x < limits.x) {
				if (changeScale) {
					bounds.w = bounds.x + bounds.w - limits.x;
				}
				bounds.x = limits.x;
			} else if (bounds.x + bounds.w > limits.x + limits.w) {
				if (changeScale) {
					bounds.w = limits.x + limits.w - bounds.x;
				} else {
					bounds.x = limits.x + limits.w - bounds.w;
				}
			}
		}

		if (isLimitExpandVertical) {
			if (bounds.h > limits.h) {
				bounds.y = limits.y;
				bounds.h = limits.h;
			} else if (bounds.y < limits.y) {
				if (changeScale) {
					bounds.h = bounds.y + bounds.h - limits.y;
				}
				bounds.y = limits.y;
			} else if (bounds.y + bounds.h > limits.y + limits.h) {
				if (changeScale) {
					bounds.h = limits.y + limits.h - bounds.y;
				} else {
					bounds.y = limits.y + limits.h - bounds.h;
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("marker")) {
			setDragActionType(DinamicGraphArea.MARKER_ON_DRAG);
		} else if (cmd.equals("select")) {
			setDragActionType(DinamicGraphArea.SELECT_ON_DRAG);
		} else if (cmd.equals("drag")) {
			setDragActionType(DinamicGraphArea.MOVE_ON_DRAG);
		} else if (cmd.equals("to-actual-size")) {
			setActualBounds();
		}
	}

	private boolean resize_vertically = true;

	public void setResizeVertically(boolean resize_vertically) {
		this.resize_vertically = resize_vertically;
	}
}
