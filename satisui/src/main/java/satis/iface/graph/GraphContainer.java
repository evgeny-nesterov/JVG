package satis.iface.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.layout.VerticalFlowLayout;

import satis.iface.graph.axis.AxisTitle;
import satis.iface.graph.axis.DinamicAxis;
import satis.iface.graph.axis.LabelAxisMarkerRenderer;
import satis.iface.graph.backgrounds.LinePainter;
import satis.iface.graph.def.DefaultLegendRenderer;
import satis.iface.graph.grid.Grid;
import satis.iface.graph.legend.Legend;

public class GraphContainer extends JPanel {
	private static final long serialVersionUID = 1L;

	public final static int GRAPHICS = 0;

	public final static int AXIS_X = 1;

	public final static int AXIS_Y = 2;

	public final static int AXIS_TITLE_X = 3;

	public final static int AXIS_TITLE_Y = 4;

	public final static int TITLE = 5;

	public final static int EMPTY = 6;

	public final static int RIGHT_SPACE = 7;

	private JPanel pnlMain = new JPanel();

	private JPanel pnlGraphContainer = new JPanel();

	private JPanel pnlAxisX = new JPanel();

	private JPanel pnlTitle = new JPanel();

	private JPanel pnlEmpty = new JPanel();

	private JPanel pnlRightSpace = new JPanel();

	private TreeMap<Integer, JComponent> graphObjects;

	public GraphContainer() {
		graphObjects = new TreeMap<Integer, JComponent>();

		try {
			jbInit();
		} catch (Exception Exc) {
			Exc.printStackTrace();
		}
	}

	public void setObject(int type, JComponent c) // c==null => remove object
	{
		int index = 0;
		switch (type) {
			case AXIS_TITLE_X:
			case AXIS_X:
				index = pnlAxisX.getComponentCount();
				if (c != null) {
					pnlAxisX.add(c, new GridBagConstraints(0, index, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				}
				break;

			case AXIS_TITLE_Y:
			case AXIS_Y:
				index = pnlAxisY.getComponentCount();
				if (c != null) {
					pnlAxisY.add(c, new GridBagConstraints(index, 0, 1, 1, 0.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
				}
				break;

			case TITLE:
				pnlTitle.removeAll();
				if (c != null) {
					pnlTitle.add(c, BorderLayout.CENTER);
				}
				break;

			case GRAPHICS:
				pnlGraphContainer.removeAll();
				if (c != null) {
					pnlGraphContainer.add(c, BorderLayout.CENTER);
				}
				break;

			case EMPTY:
				pnlEmpty.removeAll();
				if (c != null) {
					pnlEmpty.add(c, BorderLayout.CENTER);
				}
				break;

			case RIGHT_SPACE:
				pnlRightSpace.removeAll();
				if (c != null) {
					pnlRightSpace.add(c, BorderLayout.CENTER);
				}
				break;
		}

		if (c != null) {
			graphObjects.put(Integer.valueOf(type + 1000 * index), c);

			if (c instanceof GraphListener) {
				JComponent graph = getObject(GRAPHICS);
				if (graph != null && graph instanceof StaticGraphArea) {
					((StaticGraphArea) graph).addGraphListener((GraphListener) c);
				}
			}

			if (c instanceof MarkerListener) {
				JComponent graph = getObject(GRAPHICS);
				if (graph != null && graph instanceof DinamicGraphArea) {
					((DinamicGraphArea) graph).addMarkerListener((MarkerListener) c);
				}
			}
		} else {
			c = getObject(type);
			if (c != null) {
				if (c instanceof GraphListener) {
					JComponent graph = getObject(GRAPHICS);
					if (graph != null && graph instanceof StaticGraphArea) {
						((StaticGraphArea) graph).removeGraphListener((GraphListener) c);
					}
				}

				if (c instanceof MarkerListener) {
					JComponent graph = getObject(GRAPHICS);
					if (graph != null && graph instanceof DinamicGraphArea) {
						((DinamicGraphArea) graph).removeMarkerListener((MarkerListener) c);
					}
				}
			}

			graphObjects.remove(Integer.valueOf(type));
		}
	}

	public void removeObject(JComponent c) {
		synchronized (graphObjects) {
			for (Integer type : graphObjects.keySet()) {
				JComponent comp = graphObjects.get(type);
				if (c == comp) {
					setObject(type.intValue() % 1000, null);
					break;
				}
			}
		}
	}

	public void clear() {
		synchronized (graphObjects) {
			graphObjects.clear();
			pnlGraphContainer.removeAll();
			pnlAxisX.removeAll();
			pnlAxisY.removeAll();
			pnlTitle.removeAll();
			pnlEmpty.removeAll();
			pnlRightSpace.removeAll();
		}
	}

	public JComponent getObject(int type) {
		return graphObjects.get(Integer.valueOf(type));
	}

	public JComponent getObject(int type, int index) {
		return graphObjects.get(Integer.valueOf(type + 1000 * index));
	}

	private Painter backgroundPainter = null;

	public void setBackgroundPainter(Painter backgroundPainter) {
		this.backgroundPainter = backgroundPainter;
		repaint();
	}

	public Painter getBackgroundPainter() {
		return backgroundPainter;
	}

	class AxisYPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public Dimension getOriginalPreferredSize() {
			return super.getPreferredSize();
		}

		public Dimension getOriginalMinimumSize() {
			return super.getMinimumSize();
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension size = getOriginalPreferredSize();

			if (group != null) {
				for (int i = 0; i < group.getCount(); i++) {
					GraphContainer graph = group.get(i);
					if (graph != GraphContainer.this) {
						Dimension cur_size = graph.pnlAxisY.getOriginalPreferredSize();
						size.width = Math.max(size.width, cur_size.width);
					}
				}
			}

			return size;
		}

		@Override
		public Dimension getMinimumSize() {
			Dimension size = getOriginalMinimumSize();

			if (group != null) {
				for (int i = 0; i < group.getCount(); i++) {
					GraphContainer graph = group.get(i);
					if (graph != GraphContainer.this) {
						Dimension cur_size = graph.pnlAxisY.getOriginalMinimumSize();
						size.width = Math.max(size.width, cur_size.width);
					}
				}
			}

			return size;
		}
	};

	protected AxisYPanel pnlAxisY = new AxisYPanel();

	private void jbInit() throws Exception {
		setLayout(new BorderLayout());

		pnlMain.setLayout(new GridBagLayout());
		pnlMain.setOpaque(false);

		pnlGraphContainer.setLayout(new BorderLayout());
		pnlGraphContainer.setOpaque(false);

		pnlAxisX.setLayout(new GridBagLayout());
		pnlAxisX.setOpaque(false);

		pnlAxisY.setLayout(new GridBagLayout());
		pnlAxisY.setOpaque(false);

		pnlTitle.setLayout(new BorderLayout());
		pnlTitle.setOpaque(false);

		pnlEmpty.setLayout(new BorderLayout());
		pnlEmpty.setOpaque(false);

		pnlRightSpace.setLayout(new BorderLayout());
		pnlRightSpace.setOpaque(false);

		pnlMain.add(pnlGraphContainer, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pnlMain.add(pnlAxisX, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pnlMain.add(pnlAxisY, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pnlMain.add(pnlEmpty, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pnlMain.add(pnlRightSpace, new GridBagConstraints(2, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		add(pnlMain, BorderLayout.CENTER);
		add(pnlTitle, BorderLayout.NORTH);
	}

	public void setGraphBorder(Border border) {
		pnlGraphContainer.setBorder(border);
	}

	public Border getGraphBorder() {
		return pnlGraphContainer.getBorder();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (backgroundPainter != null) {
			backgroundPainter.paint(g, getWidth(), getHeight());
		}
	}

	protected GraphGroup group = null;

	protected void setGroup(GraphGroup group) {
		this.group = group;
	}

	public static GraphContainer getExample(boolean addAxisY) {
		final NumberFormat f1f = new DecimalFormat("#.#");

		final NumberFormat f2f = new DecimalFormat("#.#");

		GraphContainer gc = new GraphContainer();
		// gc.setBackgroundPainter(new
		// ImagePainter(Images.getImage("Texture.gif").getImage()));
		gc.setBackgroundPainter(new LinePainter(Color.lightGray));
		// gc.setGraphBorder(BorderFactory.createCompoundBorder(
		// new ShadowBorder(5),
		// BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
		// Color.lightGray, Color.darkGray)));
		// gc.setGraphBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
		// Color.lightGray, Color.darkGray));
		gc.setBackground(new Color(50, 250, 250));

		class DinamicTitle extends JLabel implements MarkerListener {
			private static final long serialVersionUID = 1L;

			private SimpleDateFormat df = new SimpleDateFormat("yy-MMM-dd HH:mm:ss");

			private Date date = new Date();

			private StaticGraphArea g;

			public DinamicTitle(StaticGraphArea g) {
				this.g = g;
				setHorizontalAlignment(SwingConstants.CENTER);
			}

			@Override
			public String getText() {
				if (g != null) {
					Group group = g.getGroup(0);
					if (group != null) {
						DinamicPlot plot = (DinamicPlot) group.getPlot(0);
						if (plot != null && plot.isDrawMarker()) {
							date.setTime((long) plot.getRealMarkerX());
							return "Time: " + df.format(date) + "    val: " + f1f.format(plot.getRealMarkerY());
						} else {
							return "Time: -|-   val: -|-";
						}
					}
				}

				return "";
			}

			@Override
			public void markersMoved() {
				repaint();
			}

			@Override
			public void markersHided() {
				repaint();
			}
		}

		DinamicGraphArea graph = DinamicGraphArea.getGraphExample();
		gc.setObject(GraphContainer.GRAPHICS, graph);
		gc.setObject(GraphContainer.TITLE, new DinamicTitle(graph));

		DinamicAxis axis = new DinamicAxis(graph.getGroup(0), Grid.X_AXIS);
		axis.setFormat(new SimpleDateFormat(" yy-MMM-dd HH:mm "));
		axis.setMarkerRenderer(new LabelAxisMarkerRenderer(new SimpleDateFormat(" yy-MMM-dd HH:mm:ss "), Color.black, Color.lightGray));
		gc.setObject(GraphContainer.AXIS_X, axis);

		axis = new DinamicAxis(graph.getGroup(1), Grid.X_AXIS);
		axis.setFormat(new SimpleDateFormat(" yy-MM-dd HH:mm:ss "));
		gc.setObject(GraphContainer.AXIS_X, axis);

		gc.setObject(GraphContainer.AXIS_TITLE_X, new AxisTitle("Axis of absciss", Grid.X_AXIS));
		gc.setObject(GraphContainer.AXIS_TITLE_Y, new AxisTitle("Axis of ordinate", Grid.Y_AXIS));
		gc.setObject(GraphContainer.AXIS_X, new Slider(graph.getGroup(0)));

		if (addAxisY) {
			axis = new DinamicAxis(graph.getGroup(1), Grid.Y_AXIS);
			axis.setFormat(f1f);
			axis.setMarkerRenderer(new LabelAxisMarkerRenderer());
			gc.setObject(GraphContainer.AXIS_Y, axis);

			axis = new DinamicAxis(graph.getGroup(0), Grid.Y_AXIS);
			axis.setFormat(f2f);
			gc.setObject(GraphContainer.AXIS_Y, axis);
		}

		Legend l = new Legend();
		l.setRenderer(new DefaultLegendRenderer(graph));
		JPanel pnlLegend = new JPanel();
		pnlLegend.setOpaque(false);
		pnlLegend.setLayout(new VerticalFlowLayout(FlowLayout.CENTER));
		pnlLegend.add(l);
		gc.setObject(GraphContainer.RIGHT_SPACE, pnlLegend);

		return gc;
	}

	public static void main(String[] args) {
		GraphContainer g1 = getExample(true);
		GraphContainer g2 = getExample(false);
		GraphGroup group = new GraphGroup();
		group.add(g1);
		group.add(g2);

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 500, 600, 500);
		JPanel pnl = new JPanel();
		pnl.setLayout(new GridLayout(2, 1));
		pnl.add(g1, BorderLayout.NORTH);
		pnl.add(g2, BorderLayout.CENTER);
		f.getContentPane().add(pnl, BorderLayout.CENTER);
		f.setVisible(true);
		f.validate();
		f.repaint();
	}
}
