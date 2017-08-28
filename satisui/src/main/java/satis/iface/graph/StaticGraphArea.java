package satis.iface.graph;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BufferedImagePanel;
import javax.swing.JFrame;
import javax.swing.JPanel;

import satis.iface.graph.def.DefaultGrid;
import satis.iface.graph.def.DefaultGridRenderer;
import satis.iface.graph.def.DefaultLegendRenderer;
import satis.iface.graph.def.DefaultPlotModel;
import satis.iface.graph.def.DefaultPlotRenderer;
import satis.iface.graph.grid.Grid;
import satis.iface.graph.legend.Legend;
import satis.iface.graph.paintobjects.ActivePaintObject;
import satis.iface.graph.paintobjects.PaintObject;

public class StaticGraphArea extends BufferedImagePanel implements GroupListener {
	private static final long serialVersionUID = 1L;

	public StaticGraphArea() {
		setLayout(null); // it's necessary for legend
		setOpaque(false);

		// --- Part of the constructor of the class BufferedImagePanel ---
		enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				currentGraphArea = StaticGraphArea.this;
			}
		});
	}

	private static StaticGraphArea currentGraphArea = null;

	public static StaticGraphArea getFocusedGraphArea() {
		return currentGraphArea;
	}

	@Override
	public void removeNotify() {
		if (currentGraphArea == this) {
			currentGraphArea = null;
		}

		super.removeNotify();
	}

	public void compileGroup(Group group) {
		group.compile(getSize());
	}

	public void compileGroups() {
		synchronized (groups) {
			for (Group group : groups) {
				compileGroup(group);
			}
		}
	}

	@Override
	public void updateOnResizing() {
		super.updateOnResizing();
		fireGraphResized();
	}

	protected ActivePaintObject currentActivePaintObject = null;

	protected Group currentActiveGroup = null;

	@Override
	public void paintImage(Graphics g) {
		synchronized (groups) {
			for (Group group : groups) {
				for (PaintObject o : group.paintObjects) {
					if (o.isVisible() && !o.isPaintOver() && o != currentActivePaintObject) {
						o.paint(currentActiveGroup, g);
					}
				}

				if (group.getGridHor() != null) {
					group.getGridHor().paintGrid(g, group, getWidth(), getHeight());
				}

				if (group.getGridVer() != null) {
					group.getGridVer().paintGrid(g, group, getWidth(), getHeight());
				}

				for (Plot plot : group.plots) {
					if (plot.isVisible()) {
						PlotRenderer renderer = plot.getPlotRenderer();
						if (renderer != null) {
							renderer.paintPlot(g, group, plot);
						}
					}
				}

				for (PaintObject o : group.paintObjects) {
					if (o.isVisible() && o.isPaintOver() && o != currentActivePaintObject) {
						o.paint(currentActiveGroup, g);
					}
				}
			}
		}
	}

	protected List<Group> groups = new ArrayList<Group>();

	public Iterator<Group> getGroups() {
		synchronized (groups) {
			return groups.iterator();
		}
	}

	public int getGroupsCount() {
		synchronized (groups) {
			return groups.size();
		}
	}

	public Group getGroup(int index) {
		synchronized (groups) {
			return groups.get(index);
		}
	}

	public void addGroup(Group g) {
		synchronized (groups) {
			groups.add(g);
			g.addGroupListener(this);
			fireGroupAdded(g);
		}
	}

	public void removeGroup(Group g) {
		synchronized (groups) {
			groups.remove(g);
			g.removeGroupListener(this);
			fireGroupRemoved(g);
		}
	}

	public static StaticGraphArea getExample() {
		Random ran = new Random();
		double[][] y = new double[2][400];
		double[][] x = new double[2][400];

		for (int i = 0; i < x[0].length; i++) {
			double xx = i / 200.0;
			double x1 = i * Math.pow(10, 7) + Math.pow(10, 12);
			x[0][i] = x1;
			x[1][i] = x1;

			int N = 100;
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
			// y[2][i] = 100 + Math.exp(xx) + 1 + 2 * Math.sin(Math.PI * xx) + 2
			// * (ran.nextDouble() - 0.5);
			// y[3][i] = -2000.0 - 500.0 * Math.sin(Math.PI * xx * 3);
		}

		Plot p1 = new Plot();
		DefaultPlotModel m1 = (DefaultPlotModel) p1.getModel();
		m1.setData(x[0], y[0]);
		DefaultPlotRenderer b1 = (DefaultPlotRenderer) p1.getPlotRenderer();
		b1.setPlotColor(Color.blue);

		Plot p2 = new Plot();
		DefaultPlotModel m2 = (DefaultPlotModel) p2.getModel();
		m2.setData(x[1], y[1]);
		DefaultPlotRenderer b2 = (DefaultPlotRenderer) p2.getPlotRenderer();
		b2.setPlotColor(Color.cyan);

		Group group1 = new Group();
		group1.addPlot(p1);

		Group group2 = new Group();
		group2.addPlot(p2);

		StaticGraphArea g = new StaticGraphArea();
		g.addGroup(group1);
		g.addGroup(group2);

		DefaultGrid grid = new DefaultGrid(Grid.Y_AXIS, true, false);
		grid.setGridCount(6);
		grid.setMainGridPeriod(3);
		grid.setPaintGrid(true);
		((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.red);
		((DefaultGridRenderer) grid.getRenderer()).setFont(g.getFont());
		group1.setGridY(grid);

		grid = new DefaultGrid(Grid.X_AXIS, true, false);
		grid.setGridCount(6);
		grid.setMainGridPeriod(3);
		grid.setPaintGrid(true);
		((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.red);
		((DefaultGridRenderer) grid.getRenderer()).setLabelColor(Color.black);
		((DefaultGridRenderer) grid.getRenderer()).setFont(g.getFont());
		group1.setGridX(grid);

		grid = new DefaultGrid(Grid.Y_AXIS, true, false);
		grid.setGridCount(4);
		grid.setMainGridPeriod(2);
		grid.setPaintGrid(false);
		((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.blue);
		((DefaultGridRenderer) grid.getRenderer()).setLabelColor(Color.black);
		((DefaultGridRenderer) grid.getRenderer()).setFont(g.getFont());
		group2.setGridY(grid);

		grid = new DefaultGrid(Grid.X_AXIS, true, false);
		grid.setGridCount(4);
		grid.setMainGridPeriod(2);
		grid.setPaintGrid(false);
		((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.blue);
		((DefaultGridRenderer) grid.getRenderer()).setFont(g.getFont());
		group2.setGridX(grid);

		return g;
	}

	public static void main(String[] args) {
		try {
			JFrame f = new JFrame();
			f.setBackground(Color.white);
			f.setBounds(100, 100, 600, 400);
			f.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			f.getContentPane().setLayout(new BorderLayout());

			StaticGraphArea g = getExample();

			Legend l = new Legend();
			l.setOpaque(true);
			l.setBackground(Color.white);
			l.setBorder(BorderFactory.createEtchedBorder());
			l.setRenderer(new DefaultLegendRenderer(g));
			l.pack();

			JPanel pnl = new JPanel();
			pnl.setOpaque(false);
			pnl.setLayout(null);
			pnl.add(l);

			f.setGlassPane(pnl);
			pnl.setVisible(true);

			f.getContentPane().add(g, BorderLayout.CENTER);
			f.setVisible(true);
			f.toFront();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	// --- Graph listener ---
	private ArrayList<GraphListener> graphListeners = new ArrayList<GraphListener>();

	public void addGraphListener(GraphListener listener) {
		if (listener != null) {
			synchronized (graphListeners) {
				graphListeners.add(listener);
			}
		}
	}

	public void removeGraphListener(GraphListener listener) {
		if (listener != null) {
			synchronized (graphListeners) {
				graphListeners.remove(listener);
			}
		}
	}

	protected void fireGraphResized() {
		compileGroups();

		synchronized (graphListeners) {
			for (GraphListener listener : graphListeners) {
				listener.graphResized(this);
			}
		}
	}

	public void fireGroupsRescaled() {
		fireGroupRescaled(null);
	}

	/**
	 * group==null => all groups rescaled
	 */
	public void fireGroupRescaled(Group group) {
		if (group != null) {
			compileGroup(group);
		} else {
			compileGroups();
		}

		synchronized (graphListeners) {
			for (GraphListener listener : graphListeners) {
				listener.groupRescaled(this, group);
			}
		}
	}

	public void fireGroupAdded(Group g) {
		synchronized (graphListeners) {
			for (GraphListener listener : graphListeners) {
				listener.groupAdded(this, g);
			}
		}
	}

	public void fireGroupRemoved(Group g) {
		synchronized (graphListeners) {
			for (GraphListener listener : graphListeners) {
				listener.groupRemoved(this, g);
			}
		}
	}

	// --- Implementation ---
	@Override
	public void plotAdded(Group group, Plot plot) {
		repaintImage();
		repaint();
	}

	@Override
	public void plotRemoved(Group group, Plot plot) {
		repaintImage();
		repaint();
	}

	@Override
	public void plotDataChanged(Group group, PlotModel model) {
		compileGroup(group);
		repaintImage();
		repaint();
	}

	@Override
	public void boundsChanged(Group group) {
		fireGroupRescaled(group);
		repaintImage();
		repaint();
	}

	@Override
	public void innerBoundsChanged(Group group) {
		fireGroupRescaled(group);
		repaintImage();
		repaint();
	}
}
