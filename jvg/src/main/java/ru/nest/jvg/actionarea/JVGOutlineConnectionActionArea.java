package ru.nest.jvg.actionarea;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.conn.ConnectionClient;
import ru.nest.jvg.conn.ConnectionServer;
import ru.nest.jvg.conn.Position;
import ru.nest.jvg.geom.CoordinablePathIterator;
import ru.nest.jvg.geom.GeomUtil;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGShape;

/**
 * Server connection for outlines of components.
 */
public class JVGOutlineConnectionActionArea extends JVGAbstractConnectionActionArea implements ConnectionServer {
	public JVGOutlineConnectionActionArea() {
		super(SERVER);
		setName("outline-connection");
	}

	@Override
	public boolean contains(double x, double y) {
		return false;
	}

	@Override
	public Shape computeActionBounds() {
		return null;
	}

	private Position acceptLine(MutableGeneralPath path, double clientX, double clientY, final int index1, final int index2) {
		double x1 = path.pointCoords[index1];
		double y1 = path.pointCoords[index1 + 1];
		double x2 = path.pointCoords[index2];
		double y2 = path.pointCoords[index2 + 1];

		final double alfa = GeomUtil.proectionToLine(x1, y1, x2, y2, clientX, clientY);
		if (alfa > 0 && alfa < 1) {
			double distanceSq = GeomUtil.distanceToLineSq(x1, y1, x2, y2, alfa, clientX, clientY);
			if (distanceSq <= 9) {
				return new Position() {
					@Override
					public double getServerX() {
						JVGShape shape = (JVGShape) getParent();
						MutableGeneralPath path = shape.getTransformedShape();

						double x1 = path.pointCoords[index1];
						double x2 = path.pointCoords[index2];
						return x1 + alfa * (x2 - x1);
					}

					@Override
					public double getServerY() {
						JVGShape shape = (JVGShape) getParent();
						MutableGeneralPath path = shape.getTransformedShape();

						double y1 = path.pointCoords[index1 + 1];
						double y2 = path.pointCoords[index2 + 1];
						return y1 + alfa * (y2 - y1);
					}
				};
			}
		}
		return null;
	}

	/**
	 * / x(t) = x1 + 2 (xc - x1) t + (x1 - 2 xc + x2) t^2 = Ax t^2 + Bx t + x1 \ y(t) = y1 + 2 (yc - y1) t + (y1 - 2 yc + y2) t^2 = Ay t^2 + By t
	 * + y1 -(x - x0) / (y - y0) = y'(t) / x'(t) x'(t) (x(t) - x0) + y'(t) (y(t) - y0) = 0 (2Ax t + Bx) (Ax t^2 + Bx t + x1 - x0) = (2Ay t + By)
	 * (Ay t^2 + By t + y1 - y0) (2 Ax^2) t^3+ (3 Ax Bx) t^2 + (Bx^2 + 2 Ax (x1 - x0)) t + Bx (x1 - x0) = ...(y)
	 */
	private Position acceptQuad(MutableGeneralPath path, double clientX, double clientY, final int index) {
		double x1 = path.pointCoords[index - 2];
		double y1 = path.pointCoords[index - 1];
		double xc = path.pointCoords[index];
		double yc = path.pointCoords[index + 1];
		double x2 = path.pointCoords[index + 2];
		double y2 = path.pointCoords[index + 3];

		double minX = Math.min(x1, Math.min(x2, xc)) - 5;
		double maxX = Math.max(x1, Math.max(x2, xc)) + 5;
		double minY = Math.min(y1, Math.min(y2, yc)) - 5;
		double maxY = Math.max(y1, Math.max(y2, yc)) + 5;
		if (clientX < minX || clientX > maxX || clientY < minY || clientY > maxY) {
			return null;
		}

		double xc_x1 = xc - x1;
		double yc_y1 = yc - y1;

		double Ax = x2 - xc - xc_x1;
		double Bx = xc_x1 + xc_x1;
		double Cx = x1 - clientX;
		double Ay = y2 - yc - yc_y1;
		double By = yc_y1 + yc_y1;
		double Cy = y1 - clientY;

		double[] eqn = new double[4];
		eqn[0] = Bx * Cx + By * Cy;
		eqn[1] = Ax * Cx + Ay * Cy;
		eqn[1] += eqn[1];
		eqn[1] += Bx * Bx + By * By;
		eqn[2] = Ax * Bx + Ay * By;
		eqn[2] += eqn[2] + eqn[2];
		eqn[3] = Ax * Ax + Ay * Ay;
		eqn[3] += eqn[3];

		int roots = CubicCurve2D.solveCubic(eqn);
		if (roots > 0) {
			// System.out.println(clientX + ", " + clientY);
			// System.out.println("(" + x1 + ", " + y1 + ") (" + xc + ", " + yc
			// + ") (" + x2 + ", " + y2 + ")");

			double min_dist_sq = Double.MAX_VALUE, minT = 0;
			for (int i = 0; i < roots; i++) {
				double t = eqn[i];
				double t_sq = t * t;

				double x = Ax * t_sq + Bx * t + x1;
				double y = Ay * t_sq + By * t + y1;
				double dx = x - clientX;
				double dy = y - clientY;
				double dist_sq = dx * dx + dy * dy;
				// System.out.println("      " + i + "> alfa=" + t + " (" + x +
				// ", " + y + ") (" + dx + ", " + dy + ") dist=" +
				// Math.sqrt(dist_sq));

				if (dist_sq < min_dist_sq) {
					min_dist_sq = dist_sq;
					minT = t;
				}
			}

			if (min_dist_sq < 20) {
				final double alfa = minT, alfa_sq = minT * minT;
				return new Position() {
					@Override
					public double getServerX() {
						JVGShape shape = (JVGShape) getParent();
						MutableGeneralPath path = shape.getTransformedShape();

						double x1 = path.pointCoords[index - 2];
						double xc = path.pointCoords[index];
						double x2 = path.pointCoords[index + 2];

						double Ax = x1 - xc - xc + x2;
						double Bx = xc - x1;
						Bx += Bx;

						return Ax * alfa_sq + Bx * alfa + x1;
					}

					@Override
					public double getServerY() {
						JVGShape shape = (JVGShape) getParent();
						MutableGeneralPath path = shape.getTransformedShape();

						double y1 = path.pointCoords[index - 1];
						double yc = path.pointCoords[index + 1];
						double y2 = path.pointCoords[index + 3];

						double Ay = y1 - yc - yc + y2;
						double By = yc - y1;
						By += By;

						return Ay * alfa_sq + By * alfa + y1;
					}
				};
			}
		}
		return null;
	}

	/**
	 * / x(t) = x1 + 3 (xc1 - x1) t + 3 (x1 - 2 xc1 + xc2) t^2 + (x2 - 3 xc2 + 3 xc1 - x1) t^3 = Ax t^3 + Bx t^2 + Cx t + x1 \ y(t) = y1 + 3 (yc1
	 * - y1) t + 3 (y1 - 2 yc1 + yc2) t^2 + (y2 - 3 yc2 + 3 yc1 - y1) y^3 = Ay t^3 + By t^2 + y1 t + y1 -(x - x0) / (y - y0) = y'(t) / x'(t)
	 * x'(t) (x(t) - x0) + y'(t) (y(t) - y0) = 0 (3 Ax t^2 + 2 Bx t + Cx) (Ax t^3 + Bx t^2 + Cx t + x1 - x0) + ...(y) = 0
	 */
	private Position acceptCubic(MutableGeneralPath path, double clientX, double clientY, final int index) {
		double x1 = path.pointCoords[index - 2];
		double y1 = path.pointCoords[index - 1];
		double xc1 = path.pointCoords[index];
		double yc1 = path.pointCoords[index + 1];
		double xc2 = path.pointCoords[index + 2];
		double yc2 = path.pointCoords[index + 3];
		double x2 = path.pointCoords[index + 4];
		double y2 = path.pointCoords[index + 5];

		double minX = Math.min(x1, Math.min(x2, Math.min(xc1, xc2))) - 5;
		double maxX = Math.max(x1, Math.max(x2, Math.max(xc1, xc2))) + 5;
		double minY = Math.min(y1, Math.min(y2, Math.min(yc1, yc2))) - 5;
		double maxY = Math.max(y1, Math.max(y2, Math.max(yc1, yc2))) + 5;
		if (clientX < minX || clientX > maxX || clientY < minY || clientY > maxY) {
			return null;
		}

		double xc1_xc2 = xc1 - xc2;
		double xc1_x1 = xc1 - x1;

		double Ax = x2 + (xc1_xc2 + xc1_xc2 + xc1_xc2) - x1;
		double Bx = -xc1_x1 - xc1_xc2;
		Bx += Bx + Bx;
		double Cx = xc1_x1;
		Cx += Cx + Cx;
		double Dx = x1 - clientX;

		double yc1_yc2 = yc1 - yc2;
		double yc1_y1 = yc1 - y1;

		double Ay = y2 + (yc1_yc2 + yc1_yc2 + yc1_yc2) - y1;
		double By = -yc1_y1 - yc1_yc2;
		By += By + By;
		double Cy = yc1_y1;
		Cy += Cy + Cy;
		double Dy = y1 - clientY;

		double[] eqn = new double[6];
		eqn[0] = Cx * Dx + Cy * Dy;
		eqn[1] = Bx * Dx + By * Dy;
		eqn[1] += eqn[1];
		eqn[1] += Cx * Cx + Cy * Cy;
		eqn[2] = Ax * Dx + Bx * Cx + Ay * Dy + By * Cy;
		eqn[2] += eqn[2] + eqn[2];
		eqn[3] = Ax * Cx + Ay * Cy;
		eqn[3] += eqn[3] + Bx * Bx + By * By;
		eqn[3] += eqn[3]; // 4 * (Ax * Cx + Ay * Cy) + 2 * (Bx * Bx + By * By);
		eqn[4] = Ax * Bx + Ay * By;
		eqn[4] += eqn[4] + eqn[4] + eqn[4] + eqn[4]; // 5 * (Ax * Bx + Ay * By);
		eqn[5] = Ax * Ax + Ay * Ay;
		eqn[5] += eqn[5] + eqn[5]; // 3 * (Ax * Ax + Ay * Ay)

		double[] eqn_d1 = new double[5];
		eqn_d1[0] = eqn[1];
		eqn_d1[1] = eqn[2] + eqn[2];
		eqn_d1[2] = eqn[3] + eqn[3] + eqn[3];
		eqn_d1[3] = eqn[4] + eqn[4] + eqn[4] + eqn[4];
		eqn_d1[4] = eqn[5] + eqn[5] + eqn[5] + eqn[5] + eqn[5];

		double[] eqn_d2 = new double[4];
		eqn_d2[0] = eqn[2] + eqn[2];
		eqn_d2[1] = 6 * eqn[3];
		eqn_d2[2] = 12 * eqn[4];
		eqn_d2[3] = 20 * eqn[5];

		int rootsDerCount = 0;
		double[] rootsDer = new double[5];

		// System.out.println("====================");

		double[] res = new double[7];
		int roots = CubicCurve2D.solveCubic(eqn_d2, res);
		Arrays.sort(res, 0, roots);

		while (roots > 0 && res[roots - 1] >= 1.0) {
			roots--;
		}

		res[roots++] = 1;
		double last = GeomUtil.eval(eqn_d1, 0), lastT = 0;
		for (int i = 0; i < roots; i++) {
			double t = res[i];
			double f = GeomUtil.eval(eqn_d1, t);

			if ((last >= 0 && f <= 0) || (last <= 0 && f >= 0)) {
				double root = GeomUtil.solve(eqn_d1, lastT, last, t, f, 0);
				// System.out.println(rootsDerCount + " derivative root: " +
				// root);
				rootsDer[rootsDerCount++] = root;
			}

			last = f;
			lastT = t;
		}
		rootsDer[rootsDerCount++] = 1;

		roots = 0;
		last = GeomUtil.eval(eqn, 0);
		lastT = 0;
		for (int i = 0; i < rootsDerCount; i++) {
			double t = rootsDer[i];
			double f = GeomUtil.eval(eqn, t);

			if ((last >= 0 && f <= 0) || (last <= 0 && f >= 0)) {
				double root = GeomUtil.solve(eqn, lastT, last, t, f, 0);
				// System.out.println(roots + " root: " + root);
				res[roots++] = root;
			}

			last = f;
			lastT = t;
		}

		double min_dist_sq = Double.MAX_VALUE, minT = 0;
		for (int i = 0; i < roots; i++) {
			double t = res[i];
			double t_q = t * t;
			double t_c = t_q * t;

			double x = Ax * t_c + Bx * t_q + Cx * t + x1;
			double y = Ay * t_c + By * t_q + Cy * t + y1;
			double dx = x - clientX;
			double dy = y - clientY;
			double dist_sq = dx * dx + dy * dy;
			// System.out.println("      " + i + "> alfa=" + t + " (" + x + ", "
			// + y + ") (" + dx + ", " + dy + ") dist=" + Math.sqrt(dist_sq));

			if (dist_sq < min_dist_sq) {
				min_dist_sq = dist_sq;
				minT = t;
			}
		}

		if (min_dist_sq < 20) {
			final double alfa = minT, alfa_sq = minT * minT, alfa_cub = alfa_sq * alfa;
			return new Position() {
				@Override
				public double getServerX() {
					JVGShape shape = (JVGShape) getParent();
					MutableGeneralPath path = shape.getTransformedShape();

					double x1 = path.pointCoords[index - 2];
					double xc1 = path.pointCoords[index];
					double xc2 = path.pointCoords[index + 2];
					double x2 = path.pointCoords[index + 4];

					double xc1_xc2 = xc1 - xc2;
					double xc1_x1 = xc1 - x1;

					double Ax = x2 + (xc1_xc2 + xc1_xc2 + xc1_xc2) - x1;
					double Bx = -xc1_x1 - xc1_xc2;
					Bx += Bx + Bx;
					double Cx = xc1_x1;
					Cx += Cx + Cx;

					return Ax * alfa_cub + Bx * alfa_sq + Cx * alfa + x1;
				}

				@Override
				public double getServerY() {
					JVGShape shape = (JVGShape) getParent();
					MutableGeneralPath path = shape.getTransformedShape();

					double y1 = path.pointCoords[index - 1];
					double yc1 = path.pointCoords[index + 1];
					double yc2 = path.pointCoords[index + 3];
					double y2 = path.pointCoords[index + 5];

					double yc1_yc2 = yc1 - yc2;
					double yc1_y1 = yc1 - y1;

					double Ay = y2 + (yc1_yc2 + yc1_yc2 + yc1_yc2) - y1;
					double By = -yc1_y1 - yc1_yc2;
					By += By + By;
					double Cy = yc1_y1;
					Cy += Cy + Cy;

					return Ay * alfa_cub + By * alfa_sq + Cy * alfa + y1;
				}
			};
		}

		return null;
	}

	@Override
	public Position accept(ConnectionClient client, double clientX, double clientY) {
		if (getSource() == client.getSource()) {
			return null;
		}

		JVGComponent parent = getParent();
		if (parent instanceof JVGShape) {
			JVGShape shape = (JVGShape) parent;
			Rectangle2D bounds = shape.getRectangleBounds();
			if (clientX >= bounds.getX() - 10 && clientX <= bounds.getX() + bounds.getWidth() + 10 && clientY >= bounds.getY() - 10 && clientY <= bounds.getY() + bounds.getHeight() + 10) {
				MutableGeneralPath path = shape.getTransformedShape();
				int coord = 0, moveIndex = 0;
				for (int t = 0; t < path.numTypes; t++) {
					int type = path.pointTypes[t];
					int numcoords = CoordinablePathIterator.curvesize[type];

					// curve range [coord, coord + numcoords]
					if (type != PathIterator.SEG_MOVETO) {
						Position pos = null;
						switch (type) {
							case PathIterator.SEG_CLOSE:
								pos = acceptLine(path, clientX, clientY, moveIndex, coord - 2);
								break;

							case PathIterator.SEG_LINETO:
								pos = acceptLine(path, clientX, clientY, coord - 2, coord);
								break;

							case PathIterator.SEG_QUADTO:
								pos = acceptQuad(path, clientX, clientY, coord);
								break;

							case PathIterator.SEG_CUBICTO:
								pos = acceptCubic(path, clientX, clientY, coord);
								break;
						}

						if (pos != null) {
							return pos;
						}
					} else {
						moveIndex = coord;
					}

					coord += numcoords;
				}
			}
		}
		return null;
	}

	@Override
	public int getPositionsCount() {
		return 0;
	}

	@Override
	public Position getPosition(int index) {
		return null;
	}
}
