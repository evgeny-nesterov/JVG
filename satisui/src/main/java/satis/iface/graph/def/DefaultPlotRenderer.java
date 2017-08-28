package satis.iface.graph.def;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

import javax.swing.JFrame;

import satis.iface.graph.DinamicGraphArea;
import satis.iface.graph.DinamicPlot;
import satis.iface.graph.Group;
import satis.iface.graph.Plot;
import satis.iface.graph.PlotModel;
import satis.iface.graph.PlotRenderer;
import satis.iface.graph.def.outliners.BarPlotOutliner;
import satis.iface.graph.def.outliners.LinePlotOutliner;
import satis.iface.graph.def.outliners.PlotOutliner;
import satis.iface.graph.def.points.PointsPainter;

public class DefaultPlotRenderer implements PlotRenderer {
	public final static int FILL_NONE = 0;

	public final static int FILL_SOLID = 1;

	public final static int FILL_GRADIENT = 2;

	private final static Color transparentColor = new Color(255, 255, 255, 0);

	public DefaultPlotRenderer() {
		this(Color.black, null, null, FILL_NONE);
	}

	public DefaultPlotRenderer(Color color) {
		this(color, null, null, FILL_NONE);
	}

	public DefaultPlotRenderer(Color color, Stroke stroke) {
		this(color, stroke, null, FILL_NONE);
	}

	public DefaultPlotRenderer(Color color, PlotOutliner plotOutliner, int fillType) {
		this(color, null, plotOutliner, fillType);
	}

	public DefaultPlotRenderer(Color color, Stroke stroke, PlotOutliner plotOutliner, int fillType) {
		setPlotColor(color);
		setStroke(stroke);
		setPlotOutliner(plotOutliner);
		setFillType(fillType);
	}

	private Stroke stroke = null;

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public Stroke getStroke() {
		return stroke;
	}

	private int fillType = 0;

	public void setFillType(int fillType) {
		this.fillType = fillType;
	}

	public int getFillType() {
		return fillType;
	}

	private Color color = Color.darkGray;

	@Override
	public Color getPlotColor() {
		return color;
	}

	public void setPlotColor(Color color) {
		if (color == null) {
			color = Color.darkGray;
		}

		this.color = color;
	}

	private Color fillColor = null;

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	private boolean drawOutLine = true;

	public boolean isDrawOutLine() {
		return drawOutLine;
	}

	public void setDrawOutLine(boolean drawOutLine) {
		this.drawOutLine = drawOutLine;
	}

	private PointsPainter pointsPainter = null;

	public PointsPainter getPointsPainter() {
		return pointsPainter;
	}

	public void setPointsPainter(PointsPainter pointsPainter) {
		this.pointsPainter = pointsPainter;
	}

	private PlotOutliner plotOutliner = null;

	public PlotOutliner getPlotOutliner() {
		return plotOutliner;
	}

	public void setPlotOutliner(PlotOutliner plotOutliner) {
		if (plotOutliner == null) {
			plotOutliner = new LinePlotOutliner();
		}

		this.plotOutliner = plotOutliner;
	}

	private GeneralPath path = null;

	@Override
	public void paintPlot(Graphics g, Group group, Plot plot) {
		PlotModel model = plot.getModel();
		int len = model.size();

		// --- create polygon ---
		if (path == null) {
			path = new GeneralPath();
		} else {
			path.reset();
		}

		if (fillType != FILL_NONE) {
			path.moveTo(model.getX(0), Short.MAX_VALUE);
			path.lineTo(model.getX(0), model.getY(0));
		} else {
			path.moveTo(model.getX(0), model.getY(0));
		}

		plotOutliner.start(path, model);
		int y1 = Math.min(model.getY(0), model.getY(len - 1));
		int y2 = Math.max(model.getY(0), model.getY(len - 1));
		for (int i = 0; i < len; i++) {
			plotOutliner.addPoints(path, model, i);

			int y = model.getY(i);
			if (y1 > y) {
				y1 = y;
			}
			if (y2 < y) {
				y2 = y;
			}
		}
		plotOutliner.end(path, model);

		if (fillType != FILL_NONE) {
			path.lineTo(model.getX(len - 1), Short.MAX_VALUE);
		}

		// ------------
		// --- draw ---
		// ------------
		Graphics2D g2d = (Graphics2D) g;

		// --- first fill ---
		if (fillType != FILL_NONE) {
			Color fc = (fillColor != null) ? fillColor : color;
			switch (fillType) {
				case FILL_SOLID:
					g2d.setColor(fc);
					g2d.fill(path);
					break;

				case FILL_GRADIENT:
					g2d.setPaint(new GradientPaint(0, y1, fc, 0, y2, transparentColor, false));
					g2d.fill(path);
					g2d.setPaint(null);
					break;
			}
		}

		// --- draw outline ---
		if (drawOutLine) {
			Stroke oldStroke = null;
			if (stroke != null) {
				oldStroke = g2d.getStroke();
				g2d.setStroke(stroke);
			}

			g2d.setColor(color);
			g2d.draw(path);

			if (stroke != null) {
				g2d.setStroke(oldStroke);
			}
		}

		if (pointsPainter != null) {
			for (int i = 0; i < len; i++) {
				pointsPainter.paintPoint(g, model, i);
			}
		}
	}

	public static void main(String[] args) {
		DefaultDinamicPlotRenderer renderer = new DefaultDinamicPlotRenderer(new Color(0, 0, 200), new BarPlotOutliner(1, 0.5), DefaultPlotRenderer.FILL_NONE);
		renderer.setFillColor(new Color(100, 100, 255));
		renderer.setDrawOutLine(true);
		renderer.setPointsPainter(new PointsPainter() {
			@Override
			public void paintPoint(Graphics g, PlotModel model, int index) {
				g.setColor(Color.black);
				g.drawArc(model.getX(index) - 2, model.getY(index) - 2, 5, 5, 0, 360);
			}
		});

		DefaultPlotModel model = new DefaultPlotModel(new double[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, new double[] { 1, 2, 5, 4, 6, 6, 3, 2, 5, 4 });
		model.getPrefferedBounds().y -= 8;
		model.getPrefferedBounds().h += 16;

		Plot plot = new DinamicPlot();
		plot.setRenderer(renderer);
		plot.setModel(model);

		Group group = new Group();
		group.addPlot(plot);

		DinamicGraphArea graph = new DinamicGraphArea();
		graph.addGroup(group);

		JFrame frame = new JFrame();
		frame.setContentPane(graph);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(300, 300, 600, 400);
		frame.setVisible(true);
	}
}
