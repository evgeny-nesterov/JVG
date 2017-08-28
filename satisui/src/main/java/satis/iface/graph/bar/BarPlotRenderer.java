package satis.iface.graph.bar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.ShadowBorder;

import satis.iface.graph.DinamicGraphArea;
import satis.iface.graph.DinamicPlot;
import satis.iface.graph.GraphContainer;
import satis.iface.graph.Group;
import satis.iface.graph.Plot;
import satis.iface.graph.PlotModel;
import satis.iface.graph.PlotRenderer;
import satis.iface.graph.axis.Axis;
import satis.iface.graph.backgrounds.LinePainter;
import satis.iface.graph.def.DefaultGrid;
import satis.iface.graph.def.DefaultGridRenderer;
import satis.iface.graph.grid.Grid;
import sun.awt.image.IntegerComponentRaster;

public class BarPlotRenderer implements PlotRenderer {
	public final static int BAR_ZILINDER_3D = 0;

	public final static int BAR_RECTANGLE = 1;

	private Color color = Color.gray;

	@Override
	public Color getPlotColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	private int space = 5;

	public int getSpace() {
		return space;
	}

	public void setSpace(int space) {
		this.space = space;
	}

	private int type = BAR_ZILINDER_3D;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public void paintPlot(Graphics g, Group group, Plot plot) {
		int h = Short.MAX_VALUE;
		Dimension size = group.getScreenSize();
		if (size != null) {
			h = size.height;
		}

		PlotModel model = plot.getModel();
		int len = model.size();

		BarPlotModel m = null;
		if (model instanceof BarPlotModel) {
			m = (BarPlotModel) model;
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (len > 1) {
			int type = this.type;
			if (model.getX(1) - model.getX(0) <= 10) {
				type = BAR_RECTANGLE;
			}

			for (int i = 0; i < len - 1; i++) {
				Color c = null;
				if (m != null) {
					c = m.getColor(i);
				}
				if (c == null) {
					c = color;
				}

				if (type == BAR_ZILINDER_3D) {
					paintZylinder(model.getX(i), model.getY(i), model.getX(i + 1) - model.getX(i), h - model.getY(i), c, g2d);
				} else {
					paintRectungle(model.getX(i), model.getY(i), model.getX(i + 1) - model.getX(i), h - model.getY(i), c, g2d);
				}
			}
		}

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void paintZylinder(int x, int y, int w, int h, Color c, Graphics2D g2d) {
		x += space;
		w -= 2 * space;
		int H = w / 6;
		H = 0;

		if (h > H) {
			g2d.setPaint(new MyPaint(c, x, w));
			g2d.fillRect(x, y, w, h - H);
			// g2d.fillArc(x, y + h - 2 * H, w, 2 * H, 180, 180);

			// g2d.setPaint(new GradientPaint(x + 2 * w / 5, y + H,
			// c.brighter(), x + 3 * w / 5, y - H * 3, Color.black));
			// g2d.fillArc(x, y - H, w, 2 * H, 0, 360);
		}
	}

	private void paintRectungle(int x, int y, int w, int h, Color c, Graphics2D g2d) {
		if (space > 0) {
			if (w >= 3 && w <= 10) {
				w--;
			} else if (w > 10) {
				x += space;
				w -= 2 * space;
			}
		}

		g2d.setColor(c);
		g2d.fillRect(x, y, w, h);
	}

	public static void main(String[] args) {
		BarPlotRenderer renderer = new BarPlotRenderer();
		renderer.setColor(Color.green);
		renderer.setSpace(1);

		int n = 60;
		Random r = new Random();
		double[] x = new double[n];
		double[] y = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = i;
			y[i] = r.nextInt(100);
		}
		DefaultBarPlotModel model = new DefaultBarPlotModel(x, y, new Color[] { Color.black, Color.red, Color.yellow, Color.gray, Color.green });

		Plot plot = new DinamicPlot();
		plot.setRenderer(renderer);
		plot.setModel(model);

		Group group = new Group();
		group.addPlot(plot);

		DinamicGraphArea graph = new DinamicGraphArea();
		graph.addGroup(group);

		DefaultGrid grid = new DefaultGrid(Grid.Y_AXIS, true, true);
		grid.setAddLineOnThreshhold(true);
		grid.setGridCount(6);
		grid.setMainGridPeriod(1);
		grid.setGridPoint(-12);
		grid.setPaintGrid(true);
		grid.setGridPoint(0);
		((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.gray);
		((DefaultGridRenderer) grid.getRenderer()).setFont(graph.getFont());
		group.setGridY(grid);

		grid = new DefaultGrid(Grid.X_AXIS, true, true);
		grid.setAddLineOnThreshhold(false);
		grid.setGridCount(n);
		grid.setMainGridPeriod(1);
		grid.setPaintGrid(true);
		grid.setGridPoint(0);
		((DefaultGridRenderer) grid.getRenderer()).setGridColor(Color.gray);
		((DefaultGridRenderer) grid.getRenderer()).setFont(graph.getFont());
		group.setGridX(grid);

		GraphContainer gc = new GraphContainer();
		gc.setBackgroundPainter(new LinePainter(new Color(230, 230, 230)));
		gc.setGraphBorder(BorderFactory.createCompoundBorder(new ShadowBorder(5, Color.lightGray), BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.lightGray, Color.darkGray)));
		gc.setBackground(new Color(255, 255, 255));
		gc.setObject(GraphContainer.GRAPHICS, graph);

		Axis axis = new Axis(group, Grid.Y_AXIS);
		axis.setFormat(new satis.iface.Format("%.2f"));
		gc.setObject(GraphContainer.AXIS_Y, axis);

		axis = new Axis(group, Grid.X_AXIS);
		axis.setFormat(new SimpleDateFormat(" yy-MM-dd "));
		gc.setObject(GraphContainer.AXIS_X, axis);

		JFrame frame = new JFrame();
		frame.setContentPane(new JScrollPane(gc));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(300, 300, 600, 400);
		frame.setVisible(true);
	}
}

class MyPaint implements Paint {
	Color color = Color.green;

	int x, w;

	public MyPaint(Color color, int x, int w) {
		this.color = color;
		this.x = x;
		this.w = w;
	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
		return new MyPaintContext(color, x, w);
	}

	@Override
	public int getTransparency() {
		return Transparency.OPAQUE;
	}
}

class MyPaintContext implements PaintContext {
	Color color = Color.green;

	int x, w;

	public MyPaintContext(Color color, int x, int w) {
		this.color = color;
		this.x = x;
		this.w = w;

		interp = new int[w];
		for (int i = 0; i < w; i++) {
			double X = Math.abs(i - this.w / 3.0) / w;
			double koef = 1 - X;
			if (koef > 1) {
				koef = 1;
			} else if (koef < 0) {
				koef = 0;
			}

			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();

			double fr = r + 70 / (1 + (double) r);
			double fg = g + 70 / (1 + (double) g);
			double fb = b + 70 / (1 + (double) b);

			double s = (fr + fg + fb) / 5.0;
			double d = s * koef * koef * koef;
			double k = 0.7 * koef;

			r = (int) (r * k + d);
			g = (int) (g * k + d);
			b = (int) (b * k + d);

			if (r > 255)
				r = 255;
			if (g > 255)
				g = 255;
			if (b > 255)
				b = 255;

			interp[i] = (r << 16) + (g << 8) + b;
		}
	}

	static ColorModel cachedModel;

	static WeakReference<Raster> cached;

	static synchronized Raster getCachedRaster(ColorModel cm, int w, int h) {
		if (cm == cachedModel) {
			if (cached != null) {
				Raster ras = (Raster) cached.get();
				if (ras != null && ras.getWidth() >= w && ras.getHeight() >= h) {
					cached = null;
					return ras;
				}
			}
		}
		return cm.createCompatibleWritableRaster(w, h);
	}

	static synchronized void putCachedRaster(ColorModel cm, Raster ras) {
		if (cached != null) {
			Raster cras = (Raster) cached.get();
			if (cras != null) {
				int cw = cras.getWidth();
				int ch = cras.getHeight();
				int iw = ras.getWidth();
				int ih = ras.getHeight();
				if (cw >= iw && ch >= ih) {
					return;
				}
				if (cw * ch >= iw * ih) {
					return;
				}
			}
		}
		cachedModel = cm;
		cached = new WeakReference<Raster>(ras);
	}

	private int interp[];

	private Raster saved;

	private ColorModel model = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);

	@Override
	public void dispose() {
		if (saved != null) {
			putCachedRaster(model, saved);
			saved = null;
		}
	}

	@Override
	public ColorModel getColorModel() {
		return model;
	}

	@Override
	public Raster getRaster(int x, int y, int w, int h) {
		Raster rast = saved;
		if (rast == null || rast.getWidth() < w || rast.getHeight() < h) {
			rast = getCachedRaster(model, w, h);
			saved = rast;
		}

		IntegerComponentRaster irast = (IntegerComponentRaster) rast;
		int[] pixels = irast.getDataStorage();
		int W = irast.getScanlineStride();
		int size = pixels.length;

		int pos = x - this.x;
		for (int i = 0; i < size; i += W) {
			System.arraycopy(interp, pos, pixels, i, w);
		}

		return rast;
	}
}
