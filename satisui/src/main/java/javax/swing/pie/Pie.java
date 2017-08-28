package javax.swing.pie;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;

import javax.swing.BufferedImagePanel;
import javax.swing.JFrame;

public class Pie extends BufferedImagePanel {
	private static final long serialVersionUID = 1L;

	public Pie() {
		enableEvents(AWTEvent.COMPONENT_EVENT_MASK);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mx = e.getX();
				my = e.getY();

				if (e.getClickCount() == 2) {
					DefaultPieModel model = (DefaultPieModel) getModel();
					int count = model.getCount();

					double sum = 0;
					for (int i = 0; i < count; i++) {
						sum += model.getValue(i);
					}

					if (sum == 0) {
						sum = 1;
					}

					int X = 50;
					int Y = 50;
					int W = getWidth() - 100;
					int H = getHeight() - 100;
					double cos = 1;
					if (H < W) {
						cos = H / (double) W;
					} else {
						H = W;
					}

					double from = -getStartAngle();
					for (int i = 0; i < count; i++) {
						int x = X;
						int y = Y;

						double alfa = 360 * model.getValue(i) / sum;

						if (model.isOpen(i)) {
							double rad = Math.toRadians(from + alfa / 2);
							x += (int) (radialShift * Math.cos(rad));
							y += (int) (radialShift * Math.sin(-rad) * cos);
						}

						Arc2D arc1 = new Arc2D.Double(Arc2D.PIE);
						arc1.setFrame(x, y, W, H);
						arc1.setAngleStart(from);
						arc1.setAngleExtent(alfa);

						if (arc1.contains(e.getX(), e.getY())) {
							model.getPiece(i).setOpen(!model.isOpen(i));
							repaintImage();
							repaint();
							break;
						}

						from += alfa;
					}
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int X = 50;
				int Y = 50;
				int W = getWidth() - 100;
				int H = getHeight() - 100;
				// double cos = 1;
				if (H < W) {
					// cos = H / (double) W;
				} else {
					H = W;
				}
				double cx = X + W / 2.0;
				double cy = Y + H / 2.0;

				double x1 = mx - cx;
				double y1 = (my - cy);// / cos;
				double x2 = e.getX() - cx;
				double y2 = (e.getY() - cy);// / cos;

				double r1 = Math.sqrt(x1 * x1 + y1 * y1);
				double r2 = Math.sqrt(x2 * x2 + y2 * y2);

				if (r1 == 0 || r2 == 0) {
					return;
				}

				double delta = Math.asin((x1 * y2 - x2 * y1) / (r1 * r2)) * 180 / Math.PI;

				double start = getStartAngle() + delta;
				start = start % 360;
				if (start > 0) {
					start -= 360;
				}
				setStartAngle(start);

				repaintImage();
				repaint();

				mx = e.getX();
				my = e.getY();
			}
		});
	}

	private int mx, my;

	private PieModel model;

	public PieModel getModel() {
		if (model == null) {
			model = new DefaultPieModel();
		}

		return model;
	}

	public void setModel(PieModel model) {
		PieModel oldValue = this.model;
		this.model = model;
		this.firePropertyChange("model", oldValue, model);
	}

	private PieRenderer renderer;

	public PieRenderer getRenderer() {
		if (renderer == null) {
			renderer = new PieFlatRenderer();
		}

		return renderer;
	}

	public void setRenderer(PieRenderer renderer) {
		PieRenderer oldValue = this.renderer;
		this.renderer = renderer;
		this.firePropertyChange("renderer", oldValue, renderer);
	}

	private double startAngle = 0.0001;

	public void setStartAngle(double startAngle) {
		if (this.startAngle != startAngle) {
			double oldValue = this.startAngle;
			this.startAngle = startAngle;
			firePropertyChange("start-angle", oldValue, startAngle);
		}
	}

	public double getStartAngle() {
		return startAngle;
	}

	private Insets insets = null;

	@Override
	public Insets getInsets() {
		return insets;
	}

	public void setInsets(Insets insets) {
		Insets oldValue = insets;
		this.insets = insets;
		this.firePropertyChange("insets", oldValue, insets);
	}

	private int radialShift = 0;

	public int getRadialShift() {
		return radialShift;
	}

	public void setRadialShift(int radialShift) {
		int oldValue = radialShift;
		this.radialShift = radialShift;
		this.firePropertyChange("radial-shift", oldValue, radialShift);
	}

	@Override
	public void paintImage(Graphics g) {
		int x = radialShift;
		int y = radialShift;
		int w = getWidth() - 2 * radialShift;
		int h = getHeight() - 2 * radialShift;
		if (insets != null) {
			x += insets.left;
			y += insets.top;
			w -= insets.left + insets.right;
			h -= insets.top + insets.bottom;
		}

		PieRenderer renderer = getRenderer();
		renderer.paint(g, this, x, y, w, h);
	}

	public static void main(String[] args) {
		Pie pie = new Pie();
		pie.setRadialShift(5);
		pie.setRenderer(new Pie3DRenderer(10));
		pie.setStartAngle(0);
		DefaultPieModel model = (DefaultPieModel) pie.getModel();

		Piece p = model.addPiece(new Piece());
		p.setValue(73);
		p.setColor(Color.green);
		p.setOpen(false);

		p = model.addPiece(new Piece());
		p.setValue(3);
		p.setColor(Color.black);
		p.setOpen(true);

		p = model.addPiece(new Piece());
		p.setValue(15);
		p.setColor(Color.red);
		p.setOpen(true);

		p = model.addPiece(new Piece());
		p.setValue(8);
		p.setColor(Color.yellow);
		p.setOpen(true);

		p = model.addPiece(new Piece());
		p.setValue(1);
		p.setColor(Color.gray);
		p.setOpen(true);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(pie);
		f.setBounds(100, 600, 300, 300);
		f.setVisible(true);
	}
}
