package ru.nest.hiscript.pol.support;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import ru.nest.hiscript.pol.model.ExecuteException;
import ru.nest.hiscript.pol.model.Method;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.RuntimeContext;
import ru.nest.hiscript.tokenizer.Words;

public class GraphicsSupport {
	public final static String NAMESPACE = "g";

	private Graphics2D g;

	private List<Method> methods = new ArrayList<Method>();

	public GraphicsSupport() {
		init();
	}

	public void export(RuntimeContext ctx) throws ExecuteException {
		ctx.addMethods(methods);
	}

	public void setGraphics(Graphics2D g) {
		this.g = g;
	}

	private void init() {
		// set
		methods.add(new Method(NAMESPACE, "setColor", new int[] { Words.INT }, new int[] { 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				int color = (Integer) arguments[0];
				g.setColor(new Color(color, true));
			}
		});
		methods.add(new Method(NAMESPACE, "setColor", new int[] { Words.INT, Words.INT, Words.INT }, new int[] { 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				int red = (Integer) arguments[0];
				int green = (Integer) arguments[1];
				int blue = (Integer) arguments[2];
				g.setColor(new Color(red, green, blue));
			}
		});
		methods.add(new Method(NAMESPACE, "clipRect", new int[] { Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE }, new int[] { 0, 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				Double x = (Double) arguments[0];
				Double y = (Double) arguments[1];
				Double w = (Double) arguments[2];
				Double h = (Double) arguments[3];
				g.clip(new Rectangle2D.Double(x, y, w, h));
			}
		});
		methods.add(new Method(NAMESPACE, "setPaintMode", new int[] {}, new int[] {}, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				g.setPaintMode();
			}
		});
		methods.add(new Method(NAMESPACE, "setXORMode", new int[] { Words.INT, Words.INT, Words.INT }, new int[] { 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				int red = (Integer) arguments[0];
				int green = (Integer) arguments[1];
				int blue = (Integer) arguments[2];
				g.setXORMode(new Color(red, green, blue));
			}
		});
		methods.add(new Method(NAMESPACE, "setFontSize", new int[] { Words.DOUBLE }, new int[] { 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Font font = g.getFont();
				if (font != null) {
					double size = (Double) arguments[0];
					g.setFont(font.deriveFont((float) size));
				}
			}
		});
		methods.add(new Method(NAMESPACE, "setFont", new int[] { Words.STRING, Words.INT, Words.DOUBLE }, new int[] { 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				String name = (String) arguments[0];
				int style = (Integer) arguments[1];
				double size = (Double) arguments[2];
				g.setFont(new Font(name, style, (int) size));
			}
		});

		// transform
		methods.add(new Method(NAMESPACE, "translate", new int[] { Words.DOUBLE, Words.DOUBLE }, new int[] { 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				Double x = (Double) arguments[0];
				Double y = (Double) arguments[1];
				g.translate(x, y);
			}
		});
		methods.add(new Method(NAMESPACE, "rotate", new int[] { Words.DOUBLE }, new int[] { 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				Double angle = (Double) arguments[0];
				g.rotate(angle);
			}
		});
		methods.add(new Method(NAMESPACE, "rotate", new int[] { Words.DOUBLE, Words.DOUBLE, Words.DOUBLE }, new int[] { 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				Double angle = (Double) arguments[0];
				Double x = (Double) arguments[1];
				Double y = (Double) arguments[2];
				g.rotate(angle, x, y);
			}
		});
		methods.add(new Method(NAMESPACE, "scale", new int[] { Words.DOUBLE, Words.DOUBLE }, new int[] { 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				Double sx = (Double) arguments[0];
				Double sy = (Double) arguments[1];
				g.scale(sx, sy);
			}
		});
		methods.add(new Method(NAMESPACE, "shear", new int[] { Words.DOUBLE, Words.DOUBLE }, new int[] { 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				Double sx = (Double) arguments[0];
				Double sy = (Double) arguments[1];
				g.shear(sx, sy);
			}
		});
		methods.add(new Method(NAMESPACE, "transform", new int[] { Words.DOUBLE }, new int[] { 1 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				double[] matrix = (double[]) arguments[0];
				g.transform(new AffineTransform(matrix));
			}
		});

		// draw
		methods.add(new Method(NAMESPACE, "drawLine", new int[] { Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE }, new int[] { 0, 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Double x1 = (Double) arguments[0];
				Double y1 = (Double) arguments[1];
				Double x2 = (Double) arguments[2];
				Double y2 = (Double) arguments[3];
				g.draw(new Line2D.Double(x1, y1, x2, y2));
			}
		});
		methods.add(new Method(NAMESPACE, "drawRect", new int[] { Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE }, new int[] { 0, 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				Double x = (Double) arguments[0];
				Double y = (Double) arguments[1];
				Double w = (Double) arguments[2];
				Double h = (Double) arguments[3];
				g.draw(new Rectangle2D.Double(x, y, w, h));
			}
		});
		methods.add(new Method(NAMESPACE, "fillRect", new int[] { Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE }, new int[] { 0, 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Double x = (Double) arguments[0];
				Double y = (Double) arguments[1];
				Double w = (Double) arguments[2];
				Double h = (Double) arguments[3];
				g.fill(new Rectangle2D.Double(x, y, w, h));
			}
		});
		methods.add(new Method(NAMESPACE, "drawArc", new int[] { Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.INT }, new int[] { 0, 0, 0, 0, 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Double x = (Double) arguments[0];
				Double y = (Double) arguments[1];
				Double w = (Double) arguments[2];
				Double h = (Double) arguments[3];
				Double start = (Double) arguments[4];
				Double extent = (Double) arguments[5];
				Integer type = (Integer) arguments[6];
				g.draw(new Arc2D.Double(x, y, w, h, start, extent, type));
			}
		});
		methods.add(new Method(NAMESPACE, "fillArc", new int[] { Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.DOUBLE, Words.INT }, new int[] { 0, 0, 0, 0, 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Double x = (Double) arguments[0];
				Double y = (Double) arguments[1];
				Double w = (Double) arguments[2];
				Double h = (Double) arguments[3];
				Double start = (Double) arguments[4];
				Double extent = (Double) arguments[5];
				Integer type = (Integer) arguments[6];
				g.fill(new Arc2D.Double(x, y, w, h, start, extent, type));
			}
		});
		methods.add(new Method(NAMESPACE, "drawPolygon", new int[] { Words.DOUBLE, Words.DOUBLE, Words.INT, Words.BOOLEAN }, new int[] { 1, 1, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				double[] x = (double[]) arguments[0];
				double[] y = (double[]) arguments[1];
				if (x != null && y != null) {
					int npoints = (Integer) arguments[2];
					npoints = Math.min(npoints, x.length);
					npoints = Math.min(npoints, y.length);
					if (npoints > 0) {
						boolean close = (Boolean) arguments[3];
						GeneralPath p = new GeneralPath();
						p.moveTo(x[0], y[0]);
						for (int i = 1; i < npoints; i++) {
							p.lineTo(x[i], y[i]);
						}
						if (close) {
							p.closePath();
						}
						g.draw(p);
					}
				}
			}
		});
		methods.add(new Method(NAMESPACE, "fillPolygon", new int[] { Words.DOUBLE, Words.DOUBLE, Words.INT }, new int[] { 1, 1, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				double[] x = (double[]) arguments[0];
				double[] y = (double[]) arguments[1];
				if (x != null && y != null) {
					int npoints = (Integer) arguments[2];
					npoints = Math.min(npoints, x.length);
					npoints = Math.min(npoints, y.length);
					if (npoints > 0) {
						GeneralPath p = new GeneralPath();
						p.moveTo(x[0], y[0]);
						for (int i = 1; i < npoints; i++) {
							p.lineTo(x[i], y[i]);
						}
						p.closePath();
						g.fill(p);
					}
				}
			}
		});
		methods.add(new Method(NAMESPACE, "drawPath", new int[] { Words.DOUBLE }, new int[] { 1 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				GeneralPath path = getPath((double[]) arguments[0]);
				if (path != null) {
					g.draw(path);
				}
			}
		});
		methods.add(new Method(NAMESPACE, "fillPath", new int[] { Words.DOUBLE }, new int[] { 1 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				GeneralPath path = getPath((double[]) arguments[0]);
				if (path != null) {
					g.fill(path);
				}
			}
		});
		methods.add(new Method(NAMESPACE, "drawString", new int[] { Words.STRING, Words.DOUBLE, Words.DOUBLE }, new int[] { 0, 0, 0 }, Words.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				String text = (String) arguments[0];
				if (text != null && text.length() > 0) {
					double x = (Double) arguments[1];
					double y = (Double) arguments[2];
					g.drawString(text, (float) x, (float) y);
				}
			}
		});
	}

	private GeneralPath getPath(double[] c) {
		if (c != null && c.length > 2) {
			GeneralPath p = new GeneralPath();
			p.moveTo(c[0], c[1]);
			int i = 2;
			while (i < c.length) {
				if (c[i] == 0 && i < c.length - 1) {
					p.moveTo(c[i++], c[i++]);
				} else if (c[i] == 1 && i < c.length - 1) {
					p.lineTo(c[i++], c[i++]);
				} else if (c[i] == 2 && i < c.length - 3) {
					p.quadTo(c[i++], c[i++], c[i++], c[i++]);
				} else if (c[i] == 3 && i < c.length - 5) {
					p.curveTo(c[i++], c[i++], c[i++], c[i++], c[i++], c[i++]);
				} else if (c[i] == -1) {
					p.closePath();
				} else {
					break;
				}
			}
			return p;
		}
		return null;
	}
}
