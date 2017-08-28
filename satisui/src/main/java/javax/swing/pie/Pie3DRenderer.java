package javax.swing.pie;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Pie3DRenderer implements PieRenderer {
	public Pie3DRenderer() {
	}

	public Pie3DRenderer(int height) {
		setPieHeight(height);
	}

	private int height = 20;

	public void setPieHeight(int height) {
		this.height = height;
	}

	public int getPieHeight() {
		return height;
	}

	private GeneralPath path = new GeneralPath();

	private Arc2D arc = new Arc2D.Double();

	private Polygon poly = new Polygon();

	@Override
	public void paint(Graphics g, Pie pie, int X, int Y, int W, int H) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		PieModel model = pie.getModel();
		int count = model.getCount();

		X += height;
		W -= 2 * height;
		if (W <= 20) {
			X -= 10;
			W = 20;
		}
		H -= height;
		if (H < 20) {
			H = 20;
		}

		double sum = 0;
		for (int i = 0; i < count; i++) {
			double value = model.getValue(i);
			if (value > 0.0) {
				sum += value;
			}
		}

		if (sum == 0) {
			sum = 1;
		}

		double cos = 1;
		if (H < W) {
			cos = H / (double) W;
		} else {
			H = W;
		}

		// --- sides ---
		double from = -pie.getStartAngle();
		sides.clear();
		for (int i = 0; i < count; i++) {
			double alfa = 360 * model.getValue(i) / sum;
			if (alfa > 0.0) {
				double to = from + alfa;
				int x = X;
				int y = Y;

				if (model.isOpen(i)) {
					double rad = toRadians(from + alfa / 2);
					x += (int) (pie.getRadialShift() * cos(rad));
					y += (int) (pie.getRadialShift() * sin(-rad) * cos);
				}

				// angles
				double f = from % 360;
				if (f < 0) {
					f += 360;
				}
				if ((f >= 0 && f < 90) || (f > 270 && f <= 360)) {
					if (alfa != 360) {
						sides.add(getAngle(f, i, x, y, W, H, model.getColor(i)));
					}
				}

				double t = to % 360;
				if (t < 0) {
					t += 360;
				}
				if (t > 90 && t < 270) {
					if (alfa != 360) {
						sides.add(getAngle(t, i, x, y, W, H, model.getColor(i)));
					}
				}

				// duga
				boolean top1 = f < 180;
				boolean top2 = t < 180;
				if (!(top1 && top2 && alfa < 180)) {
					double A = from;
					double B = alfa;
					if (!top1 && !top2 && alfa > 180) {
						A = from + alfa;
						B = -A % 180;
						sides.add(getDuga(A, B, i, x, y, W, H, model.getColor(i)));

						A = from;
						B = 180 - from % 180;
						sides.add(getDuga(A, B, i, x, y, W, H, model.getColor(i)));
					} else {
						if (top1) {
							double newA = (floor(from / 180) + 1) * 180;
							B -= newA - A;
							A = newA;
						}
						if (top2) {
							double newA = floor((from + alfa) / 180) * 180;
							B -= from + alfa - newA;
						}
						sides.add(getDuga(A, B, i, x, y, W, H, model.getColor(i)));
					}
				}

				from = to;
			}
		}
		Collections.sort(sides, comparatorSide);

		for (Side side : sides) {
			side.paint(g2d);
			if (side instanceof Angle) {
				angles.add((Angle) side);
			} else {
				dugas.add((Duga) side);
			}
		}

		// --- top ---
		from = -pie.getStartAngle();
		for (int i = 0; i < count; i++) {
			double alfa = 360 * model.getValue(i) / sum;
			if (alfa > 0.0) {
				int x = X;
				int y = Y;
				if (model.isOpen(i)) {
					double rad = toRadians(from + alfa / 2);
					x += (int) (pie.getRadialShift() * cos(rad));
					y += (int) (pie.getRadialShift() * sin(-rad) * cos);
				}

				if (alfa < 360) {
					arc.setArc(x, y, W, H, from, alfa, Arc2D.PIE);
				} else {
					arc.setArc(x, y, W, H, from, alfa, Arc2D.OPEN);
				}
				g2d.setColor(new Color(model.getColor(i).getRed(), model.getColor(i).getGreen(), model.getColor(i).getBlue(), 220));
				g2d.fill(arc);

				g2d.setColor(model.getColor(i).darker());
				g2d.draw(arc);

				from += alfa;
			}
		}

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private List<Side> sides = new ArrayList<Side>();

	private Comparator<Side> comparatorSide = new Comparator<Side>() {
		@Override
		public int compare(Side s1, Side s2) {
			double a1 = s1.getAngle();
			double a2 = s2.getAngle();

			double b1 = abs(270 - a1);
			if (b1 > 180) {
				b1 = 360 - b1;
			}
			double b2 = abs(270 - a2);
			if (b2 > 180) {
				b2 = 360 - b2;
			}

			if (b1 > b2) {
				return -1;
			} else {
				return 1;
			}
		}
	};

	private abstract class Side {
		public Side() {
		}

		public Side(int index, int x, int y, int W, int H, Color color) {
			set(index, x, y, W, H, color);
		}

		private void set(int index, int x, int y, int W, int H, Color color) {
			this.index = index;
			this.x = x;
			this.y = y;
			this.W = W;
			this.H = H;
			this.color = color;
		}

		public abstract double getAngle();

		public abstract void paint(Graphics2D g);

		int index;

		int x, y, W, H;

		Color color;

		GradientPaint grad1 = null;

		GradientPaint grad2 = null;

		Color gradColor = null;

		int gradH;

		void updateGradient() {
			if (grad1 == null || !gradColor.equals(color) || gradH != H) {
				Color color1 = createColor(color, 0.85);
				grad1 = new GradientPaint(0, (int) (H * 1.05), color1, 0, (int) (-H * 0.3), Color.black);

				Color color2 = createColor(color, 0.5);
				grad2 = new GradientPaint(0, (int) (H * 1.05), color2, 0, (int) (-H * 0.3), Color.black);

				gradColor = color;
				gradH = H;
			}
		}

		Color createColor(Color color, double FACTOR) {
			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();

			if ((r + g + b) / 3 < 40) {
				int i = (int) (1.0 / (1.0 - FACTOR));
				if (r == 0 && g == 0 && b == 0) {
					return new Color(10 * i, 10 * i, 10 * i);
				}

				if (r > 0 && r < i) {
					r = i;
				}
				if (g > 0 && g < i) {
					g = i;
				}
				if (b > 0 && b < i) {
					b = i;
				}
				return new Color(min((int) (r / FACTOR), 255), min((int) (g / FACTOR), 255), min((int) (b / FACTOR), 255));
			} else {
				return new Color(max((int) (r * FACTOR), 0), max((int) (g * FACTOR), 0), max((int) (b * FACTOR), 0));
			}
		}
	}

	private class Angle extends Side {
		public Angle(double angle, int index, int x, int y, int W, int H, Color color) {
			set(angle, index, x, y, W, H, color);
		}

		void set(double angle, int index, int x, int y, int W, int H, Color color) {
			super.set(index, x, y, W, H, color);
			this.angle = angle;
			sortAngle = angle % 360;
			if (sortAngle < 0) {
				sortAngle += 360;
			}
		}

		double sortAngle;

		double angle;

		@Override
		public double getAngle() {
			return sortAngle;
		}

		@Override
		public void paint(Graphics2D g) {
			double rad = toRadians(angle);
			int xc = x + W / 2;
			int yc = y + H / 2;
			int xe = (int) (xc + W / 2 * cos(rad));
			int ye = (int) (yc + H / 2 * sin(-rad));

			poly.reset();
			poly.addPoint(xc, yc);
			poly.addPoint(xc, yc + height);
			poly.addPoint(xe, ye + height);
			poly.addPoint(xe, ye);

			updateGradient();
			g.setPaint(grad1);
			g.fill(poly);
			g.setPaint(grad2);
			g.draw(poly);
		}
	}

	private class Duga extends Side {
		public Duga(double from, double angle, int index, int x, int y, int W, int H, Color color) {
			set(from, angle, index, x, y, W, H, color);
		}

		void set(double from, double angle, int index, int x, int y, int W, int H, Color color) {
			super.set(index, x, y, W, H, color);
			if (angle == 360.0) {
				from = 0;
			}

			this.from = from;
			this.angle = angle;

			double a1 = from % 360;
			if (a1 < 0) {
				a1 += 360;
			}

			double a2 = (from + angle) % 360;
			if (a2 < 0) {
				a2 += 360;
			}

			if (min(a1, a1 + angle) < 270 && max(a1, a1 + angle) > 270) {
				sortAngle = 270;
			} else if (abs(a1 - 270) < abs(a2 - 270)) {
				sortAngle = a1;
			} else {
				sortAngle = a2;
			}
		}

		double sortAngle;

		double from;

		double angle;

		@Override
		public double getAngle() {
			return sortAngle;
		}

		@Override
		public void paint(Graphics2D g) {
			arc.setArc(x, y, W, H, from, angle, Arc2D.OPEN);
			path.reset();
			path.append(arc, true);
			arc.setArc(x, y + height, W, H, from + angle, -angle, Arc2D.OPEN);
			path.append(arc, true);
			path.closePath();

			updateGradient();
			g.setPaint(grad1);
			g.fill(path);
			g.setPaint(grad2);
			g.draw(path);
		}
	}

	private List<Angle> angles = new ArrayList<Angle>();

	private Angle getAngle(double f, int i, int x, int y, int W, int H, Color color) {
		if (angles.size() > 0) {
			Angle angle = angles.remove(angles.size() - 1);
			angle.set(f, i, x, y, W, H, color);
			return angle;
		}
		return new Angle(f, i, x, y, W, H, color);
	}

	private List<Duga> dugas = new ArrayList<Duga>();

	Duga getDuga(double f, double a, int i, int x, int y, int W, int H, Color color) {
		if (dugas.size() > 0) {
			Duga duga = dugas.remove(dugas.size() - 1);
			duga.set(f, a, i, x, y, W, H, color);
			return duga;
		}
		return new Duga(f, a, i, x, y, W, H, color);
	}
}
