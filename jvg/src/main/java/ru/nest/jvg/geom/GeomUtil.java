package ru.nest.jvg.geom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class GeomUtil {
	/**
	 * Approximate a set of points by line aX+b. Return massive {a, b} as result.
	 */
	public static double interpolate(double[] coordinates, int offset, int length, double[] result) {
		// sum[(aX+b - Y)^2] -> min, i=1,N

		// sum[aX+b] = sum[Y]
		// sum[(aX+b)X] = sum[XY]

		// a sum[X] + b N = sum[Y]
		// a sum[X^2] + b sum[X] = sum[XY]

		// D = sum[X]^2 - N sum[X^2]
		// if D = 0 then x(y) = sum[X] / N else
		// a = (sum[Y] sum[X] - N sum[XY]) / D
		// b = (sum[X] sum[XY] - sum[Y] sum[X^2]) / D

		int N = length / 2, end = offset + length;
		double Sx = 0, Sx2 = 0, Sy = 0, Sxy = 0;
		for (int i = offset; i < end; i += 2) {
			Sx += coordinates[i];
			Sy += coordinates[i + 1];
			Sx2 += coordinates[i] * coordinates[i];
			Sxy += coordinates[i] * coordinates[i + 1];
		}
		double D = Sx * Sx - N * Sx2;
		if (D == 0) {
			double x = N * Sx2;
			result[0] = x;
			result[1] = coordinates[1];
			result[2] = x;
			result[3] = coordinates[coordinates.length - 1];

			double disp = 0;
			for (int i = offset; i < end; i += 2) {
				double delta = x - coordinates[i];
				disp += delta * delta;
			}
			disp = Math.sqrt(disp / N);
			return disp;
		} else {
			double a = (Sy * Sx - N * Sxy) / D;
			double b = (Sy * Sxy - Sy * Sx2) / D;

			result[0] = a * coordinates[0] + b;
			result[1] = coordinates[1];
			result[2] = a * coordinates[coordinates.length - 2] + b;
			result[3] = coordinates[coordinates.length - 1];

			double disp = 0;
			for (int i = offset; i < end; i += 2) {
				double delta = a * coordinates[i] + b - coordinates[i + 1];
				disp += delta * delta;
			}
			disp = Math.sqrt(disp / N);
			return disp;
		}
	}

	public static boolean solveLinear(double[][] matr, double[] right, double[] result) {
		int J;
		double k;
		int n = right.length;
		int[] b = new int[n], c = new int[n];

		for (int I = 0; I < n; I++) {
			J = 0;
			FIND: {
				for (J = 0; J < n; J++) {
					if (matr[J][I] != 0 && b[J] == 0) {
						break FIND;
					}
				}
				return false;
			}
			b[J] = 1;
			c[I] = J;

			for (int j = 0; j < n; j++) {
				if (j != J) {
					k = matr[j][I] / matr[J][I];
					for (int i = 1; i < n; i++) {
						matr[j][i] = matr[j][i] - k * matr[J][i];
					}
					matr[j][I] = 0;
					right[j] = right[j] - k * right[J];
				}
			}
		}

		for (int i = 0; i < n; i++) {
			if (matr[c[i]][i] != 0) {
				result[c[i]] = right[c[i]] / matr[c[i]][i];
			} else {
				result[c[i]] = 0;
			}
		}

		return true;
	}

	/**
	 * Calculations: r = r1 + alfa * (r2 - r1), a, b = ? : (a - b) * (r2 - r1) = 0 b (r2 - r1) = a (r2 - r1) b = r1 + x * (r2 - r1), x = ?
	 * r1(r2-r1) + x(r2-r1)^2 = a(r2-r1) x = (a-r1)(r2-r1)/(r2-r1)^2 x=[0,1] => b=[r1,r2]
	 */
	public static double proectionToLine(double x1, double y1, double x2, double y2, double clientX, double clientY) {
		double minX = Math.min(x1, x2) - 5;
		double maxX = Math.max(x1, x2) + 5;
		double minY = Math.min(y1, y2) - 5;
		double maxY = Math.max(y1, y2) + 5;
		if (clientX < minX || clientX > maxX || clientY < minY || clientY > maxY) {
			return Double.MAX_VALUE;
		}

		double a_r1_x = clientX - x1;
		double a_r1_y = clientY - y1;
		double r2_r1_x = x2 - x1;
		double r2_r1_y = y2 - y1;
		double alfa = (a_r1_x * r2_r1_x + a_r1_y * r2_r1_y) / (r2_r1_x * r2_r1_x + r2_r1_y * r2_r1_y);

		return alfa;
	}

	public static double distanceToLineSegmentSq(double x1, double y1, double x2, double y2, double clientX, double clientY) {
		double alfa = proectionToLine(x1, y1, x2, y2, clientX, clientY);
		if (alfa > 0 && alfa < 1) {
			return distanceToLineSq(x1, y1, x2, y2, alfa, clientX, clientY);
		}

		return Double.MAX_VALUE;
	}

	public static double distanceToLineSq(double x1, double y1, double x2, double y2, double alfa, double clientX, double clientY) {
		double bx = x1 + alfa * (x2 - x1);
		double by = y1 + alfa * (y2 - y1);
		double a_b_x = clientX - bx;
		double a_b_y = clientY - by;
		double a_b_sq = a_b_x * a_b_x + a_b_y * a_b_y;

		return a_b_sq;
	}

	private final static double EPSILON = 1e-1;

	private final static int MAX_STACK_LENGTH = 32;

	public static double solve(double[] koef, double x1, double y1, double x2, double y2, int stackSize) {
		double middleX = (y2 * x1 - y1 * x2) / (y2 - y1);
		double middle = eval(koef, middleX);
		if (Math.abs(middle) < EPSILON || stackSize == MAX_STACK_LENGTH) {
			return middleX;
		}

		if ((y1 > 0 && middle < 0) || (y1 < 0 && middle > 0)) {
			return solve(koef, x1, y1, middleX, middle, stackSize + 1);
		} else {
			return solve(koef, middleX, middle, x2, y2, stackSize + 1);
		}
	}

	public static double eval(double[] koef, double x) {
		double y = 0, pow = 1;
		for (int i = 0; i < koef.length; i++) {
			if (i > 0) {
				pow *= x;
			}
			y += pow * koef[i];
		}
		return y;
	}

	private static double[] p1 = new double[6];

	private static double[] p2 = new double[6];

	public static final int curvesize[] = { 2, 2, 4, 6, 0 };

	public static boolean equals(Shape s1, Shape s2) {
		PathIterator pi1 = s1.getPathIterator(null);
		PathIterator pi2 = s2.getPathIterator(null);
		while (!pi1.isDone() && !pi2.isDone()) {
			int t1 = pi1.currentSegment(p1);
			int t2 = pi2.currentSegment(p2);
			if (t1 != t2) {
				return false;
			} else {
				if (!equals(p1, p2, curvesize[t1])) {
					return false;
				}
			}
			pi1.next();
			pi2.next();
		}

		if (!pi1.isDone() || !pi2.isDone()) {
			return false;
		}

		return true;
	}

	public static boolean equals(double[] p1, double[] p2, int length) {
		for (int i = 0; i < length; i++) {
			if (p1[i] != p2[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * x(t) = a0 + a1 * t + a2 * t^2 y(t) = b0 + b1 * t + b2 * t^2 ao = x0 b0 = y0
	 */
	public static double approximate(double x0, double y0, double[] coordinates, int offset, int length, double[] koefX, double[] koefY) {
		koefX[0] = x0; // a0
		koefY[0] = y0; // b0

		double endX = coordinates[offset + length - 2];
		double endY = coordinates[offset + length - 1];

		double minX = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		int endOffsed = offset + length;
		for (int i = offset; i < endOffsed; i += 2) {
			minX = Math.min(minX, coordinates[i]);
			maxX = Math.max(maxX, coordinates[i]);
			minY = Math.min(minY, coordinates[i + 1]);
			maxY = Math.max(maxY, coordinates[i + 1]);
		}

		int koefLength = Math.min(koefX.length, koefY.length);
		double[] koefXMin = new double[koefLength];
		double[] koefXMax = new double[koefLength];
		double[] koefYMin = new double[koefLength];
		double[] koefYMax = new double[koefLength];
		// for(int i = 1; i < koefLength; i++)
		// {
		// koefXMin[i] = koefX[i] = 2 * (minX - 10 * (maxX - minX) - x0);
		// koefXMax[i] = 2 * (maxX + 10 * (maxX - minX) - x0);
		// koefYMin[i] = koefY[i] = minY - y0 - 1000;
		// koefYMax[i] = maxY - y0 + 1000;
		// }

		koefXMin[1] = koefX[1] = 2 * (minX - x0);
		koefXMax[1] = 2 * (maxX - x0);
		koefYMin[1] = koefY[1] = 2 * (minY - y0);
		koefYMax[1] = 2 * (maxY - y0);

		koefXMin[2] = koefX[2] = x0 - 2 * (maxX + 0 * (maxX - minX)) + endX;
		koefXMax[2] = x0 - 2 * (minX - 0 * (maxX - minX)) + endX;
		koefYMin[2] = koefY[2] = y0 - 2 * (maxY + 0 * (maxY - minY)) + endY;
		koefYMax[2] = y0 - 2 * (minY - 0 * (maxY - minY)) + endY;

		int netSize = 2;
		int maxHopes = 32;

		double[] koefXOpt = new double[koefLength];
		double[] koefYOpt = new double[koefLength];
		double distSqOpt = Double.MAX_VALUE;
		int hopes = 0;
		int hopesPrecision = 0;

		int[] koefIndexes = new int[2 * (koefLength - 1)];
		RECURSE_ALL: while (true) {
			boolean isEdgeOpt = true;
			double distSqOptLocal = Double.MAX_VALUE;
			RECURSE_NET: while (true) {
				boolean isEdge = false;
				for (int i = 0; i < koefIndexes.length; i++) {
					isEdge |= koefIndexes[i] == 0 || koefIndexes[i] == netSize;

					if (koefIndexes[i] > netSize) {
						if (i == koefIndexes.length - 1) {
							koefIndexes[i] = 0;
							break RECURSE_NET;
						}

						koefIndexes[i] = 0;
						koefIndexes[i + 1]++;
					}
				}

				for (int i = 0; i < koefIndexes.length; i++) {
					if (i < koefLength - 1) {
						int index = i + 1;
						koefX[index] = koefXMin[index] + koefIndexes[i] * (koefXMax[index] - koefXMin[index]) / netSize;
					} else {
						int index = i - koefLength + 2;
						koefY[index] = koefYMin[index] + koefIndexes[i] * (koefYMax[index] - koefYMin[index]) / netSize;
					}
				}

				koefX[2] = endX - koefX[1] - x0;
				koefY[2] = endY - koefY[1] - y0;

				double distSq = getDistanceSq(coordinates, offset, length, koefX, koefY);
				if (distSq < distSqOptLocal) {
					System.arraycopy(koefX, 0, koefXOpt, 0, koefLength);
					System.arraycopy(koefY, 0, koefYOpt, 0, koefLength);
					isEdgeOpt = isEdge;
					distSqOptLocal = distSq;
				}

				koefIndexes[0]++;
			}

			hopes++;
			if (!isEdgeOpt) {
				hopesPrecision++;
			}

			if ((hopesPrecision >= 6 && distSqOpt != distSqOptLocal && distSqOpt / distSqOptLocal < 1.00001) || hopes == maxHopes) {
				System.out.println("hopes: " + hopes + ", hopesPrecision: " + hopesPrecision + ", distSqOpt / distSqOptLocal: " + distSqOpt / distSqOptLocal);
				getDistanceSq(coordinates, offset, length, koefXOpt, koefYOpt);
				break RECURSE_ALL;
			}

			distSqOpt = distSqOptLocal;

			for (int i = 1; i < koefLength; i++) {
				double widthX = koefXMax[i] - koefXMin[i];
				double widthY = koefYMax[i] - koefYMin[i];
				if (!isEdgeOpt) {
					widthX /= netSize;
					widthY /= netSize;
				}
				koefXMin[i] = koefXOpt[i] - widthX / 2.0;
				koefXMax[i] = koefXOpt[i] + widthX / 2.0;
				koefYMin[i] = koefYOpt[i] - widthY / 2.0;
				koefYMax[i] = koefYOpt[i] + widthY / 2.0;
			}
		}

		return distSqOpt;
	}

	static int sqCount = 0;

	public static double getDistanceSq(double[] coordinates, int offset, int length, double[] koefX, double[] koefY) {
		sqCount++;
		if (getPow(koefX) < 2 || getPow(koefY) < 2) {
			return Double.MAX_VALUE;
		}

		int pointsCount = length / 2;
		double sum = 0;
		double t = 0;

		int endOffset = offset + length;
		for (int i = offset; i < endOffset; i += 2) {
			if (i == 0) {
				t = 0;
			} else if (i == endOffset - 2) {
				t = 1;
			} else {
				t = getT(coordinates[i], koefX, t, 1);
				if (t == Double.MAX_VALUE) {
					return Double.MAX_VALUE;
				}
			}

			double distSq = getDistanceSq(coordinates[i], coordinates[i + 1], koefX, koefY, t);
			sum += distSq;
		}

		return sum / pointsCount;
	}

	public static double getDistanceSq(double x, double y, double[] koefX, double[] koefY, double t) {
		double curveX = getValue(koefX, t);
		double curveY = getValue(koefY, t);
		double distX = curveX - x;
		double distY = curveY - y;
		double distSq = distX * distX + distY * distY;
		return distSq;
	}

	public static double getValue(double[] koef, double coord) {
		double res = 0;
		double pow = 1;
		for (int i = 0; i < koef.length; i++) {
			if (koef[i] != 0) {
				res += koef[i] * pow;
			}

			if (i != koef.length - 1) {
				pow *= coord;
			}
		}
		return res;
	}

	public static double getT(double coord, double[] koef, double minT, double maxT) {
		double[] roots = koef.clone();
		roots[0] -= coord;

		int pow = getPow(koef);
		int rootsCount = 0;
		switch (pow) {
			case 1:
				rootsCount = 1;
				roots[0] = (coord - koef[0]) / koef[1];
				break;

			case 2:
				rootsCount = QuadCurve2D.solveQuadratic(roots, roots);
				break;

			case 3:
				rootsCount = CubicCurve2D.solveCubic(roots, roots);
				break;
		}

		if (rootsCount == 0) {
			return Double.MAX_VALUE;
		}

		double t = maxT;
		boolean found = false;
		for (int i = 0; i < rootsCount; i++) {
			if (roots[i] < t && roots[i] >= minT) {
				t = roots[i];
				found = true;
			}
		}

		if (t < minT || !found) {
			t = minT;
		}

		return t;
	}

	public static int getPow(double[] koef) {
		for (int i = koef.length - 1; i >= 1; i--) {
			if (koef[i] != 0) {
				return i;
			}
		}
		return 0;
	}

	public static double getPathLength(Shape shape) {
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 1);
		double points[] = new double[6];
		double moveX = 0, moveY = 0;
		double lastX = 0, lastY = 0;
		double thisX = 0, thisY = 0;
		int type = 0;
		double total = 0;
		while (!it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = lastX = points[0];
					moveY = lastY = points[1];
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					thisX = points[0];
					thisY = points[1];
					double dx = thisX - lastX;
					double dy = thisY - lastY;
					total += Math.sqrt(dx * dx + dy * dy);
					lastX = thisX;
					lastY = thisY;
					break;
			}
			it.next();
		}
		return total;
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 700, 700);
		f.setContentPane(new JLabel() {
			MutableGeneralPath path = new MutableGeneralPath();
			GeneralPath pathAppr = new GeneralPath();
			double mx;
			double my;
			int coordIndex = 0;

			{
				setOpaque(true);
				setBackground(Color.white);

				addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						path.reset();
						pathAppr.reset();

						mx = e.getX();
						my = e.getY();
						path.moveTo(mx, my);
						pathAppr.moveTo(mx, my);
						repaint();
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						long time = System.currentTimeMillis();

						double[] koefX = new double[3];
						double[] koefY = new double[3];
						double distSq = GeomUtil.approximate(path.pointCoords[0], path.pointCoords[1], path.pointCoords, 0, path.numCoords, koefX, koefY);
						System.out.println("distSq = " + distSq);

						for (int i = 0; i < koefX.length; i++) {
							System.out.print(koefX[i] + " ");
						}
						System.out.println();
						for (int i = 0; i < koefY.length; i++) {
							System.out.print(koefY[i] + " ");
						}
						System.out.println();
						System.out.println("time: " + (System.currentTimeMillis() - time) + " ms, sqCount: " + sqCount);

						pathAppr.quadTo((koefX[1] / 2 + koefX[0]), (koefY[1] / 2 + koefY[0]), (koefX[2] + koefX[0] - koefX[1]), (koefY[2] + koefY[0] - koefY[1]));

						// for(int i = 0; i <= 100; i++)
						// {
						// double t = i / 100.0;
						// double x = getValue(koefX, t);
						// double y = getValue(koefY, t);
						// if(i == 0)
						// {
						// pathAppr.moveTo(x, y);
						// }
						// else
						// {
						// pathAppr.lineTo(x, y);
						// }
						// }

						repaint();
					}
				});

				addMouseMotionListener(new MouseMotionAdapter() {
					GeneralPath add = new GeneralPath();

					@Override
					public void mouseDragged(MouseEvent e) {
						mx = e.getX();
						my = e.getY();
						path.lineTo(mx, my);

						// if(path.numCoords - coordIndex > 4)
						// {
						// double[] koefX = new double[3];
						// double[] koefY = new double[3];
						// double distSq =
						// GeomUtil.approximate(path.pointCoords[0],
						// path.pointCoords[1],
						// path.pointCoords, coordIndex, path.numCoords -
						// coordIndex,
						// koefX, koefY);
						// if(distSq > 1)
						// {
						// if(path.numCoords - coordIndex == 6)
						// {
						// pathAppr.lineTo(path.pointCoords[path.numCoords - 4],
						// path.pointCoords[path.numCoords - 3]);
						// coordIndex = path.numCoords - 4;
						// }
						// else
						// {
						// pathAppr.quadTo((koefX[1] / 2 - koefX[0]),
						// (koefY[1] / 2 - koefY[0]),
						// path.pointCoords[path.numCoords - 2],
						// path.pointCoords[path.numCoords - 1]);
						// coordIndex = path.numCoords - 2;
						// }
						// }
						// }

						repaint();
					}
				});
			}

			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Color.black);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.draw(path);

				g2d.setColor(Color.blue);
				g2d.draw(pathAppr);
			}
		});
		f.setVisible(true);
	}
}
