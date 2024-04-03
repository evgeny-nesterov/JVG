package ru.nest.jvg.shape.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import ru.nest.jvg.geom.MutableGeneralPathIterator;
import ru.nest.jvg.geom.Pathable;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;

public class EndingsPainter extends Painter {
	public EndingsPainter() {
		this(new Endings());
	}

	public EndingsPainter(int type, boolean direct, int figure, boolean fill, Resource<Color> color) {
		this(type, direct, figure, fill);
		setPaint(new ColorDraw(color));
	}

	public EndingsPainter(Endings endings, Resource<Color> color) {
		this(endings);
		setPaint(new ColorDraw(color));
	}

	public EndingsPainter(int type, boolean direct, int figure, boolean fill, final Painter drawSource) {
		this(type, direct, figure, fill);
		setPaint(new AbstractDraw() {
			@Override
			public Paint getPaint(JVGShape component, Shape shape, AffineTransform transform) {
				Draw draw = drawSource != null ? drawSource.getPaint() : null;
				return draw != null ? draw.getPaint(component, shape, transform) : null;
			}

			@Override
			public void setResource(Resource resource) {
				Draw draw = drawSource != null ? drawSource.getPaint() : null;
				if (draw != null) {
					draw.setResource(resource);
				}
			}

			@Override
			public Resource getResource() {
				Draw draw = drawSource != null ? drawSource.getPaint() : null;
				return draw != null ? draw.getResource() : null;
			}
		});
	}

	public EndingsPainter(int type, boolean direct, int figure, boolean fill) {
		this(new Endings(type, direct, figure, fill));
	}

	public EndingsPainter(Endings endings) {
		setEndings(endings);
	}

	private Endings endings;

	public Endings getEndings() {
		return endings;
	}

	public void setEndings(Endings endings) {
		this.endings = endings;
	}

	@Override
	public void paint(Graphics2D g, JVGShape component, Shape shape) {
		if (shape instanceof Pathable) {
			Pathable path = (Pathable) shape;
			drawEndings(g, path, endings);
		}
	}

	public static void drawEndings(Graphics2D g, Pathable path, Endings endings) {
		try {
			switch (endings.getEndingType()) {
				case Endings.TYPE_FIRST_ENDING:
					drawStart(g, path, endings);
					break;

				case Endings.TYPE_LAST_ENDING:
					drawEnd(g, path, endings);
					break;

				case Endings.TYPE_BOTH_ENDING:
					drawStart(g, path, endings);
					drawEnd(g, path, endings);
					break;

				case Endings.TYPE_ALL_ENDINGS:
					if (endings.isDirect()) {
						drawAllDirect(g, path, endings);
					} else {
						drawAllIndirect(g, path, endings);
					}
					break;
			}
		} catch (Throwable thr) {
			System.err.println("cannot draw outline endings: " + thr);
			thr.printStackTrace();
		}
	}

	private static int getPrevCoordIndex(Pathable path, int curveIndex, int coordIndex) {
		if (curveIndex > 0) {
			int prevType = path.getCurveType(curveIndex - 1);
			switch (prevType) {
				case PathIterator.SEG_LINETO:
				case PathIterator.SEG_QUADTO:
				case PathIterator.SEG_CUBICTO:
					return coordIndex - 2;
				case PathIterator.SEG_CLOSE:
					int movetoCurveIndex = curveIndex - 1;
					int movetoCoordIndex = coordIndex;
					while (movetoCurveIndex > 0 && path.getCurveType(movetoCurveIndex) != PathIterator.SEG_MOVETO) {
						movetoCoordIndex -= MutableGeneralPathIterator.curvesize[path.getCurveType(movetoCurveIndex)];
						movetoCurveIndex--;
					}
					return movetoCurveIndex;
			}
		}
		return -1;
	}

	private static int getNextCoordIndex(Pathable path, int curveIndex, int coordIndex) {
		if (curveIndex > 0) {
			int type = path.getCurveType(curveIndex);
			switch (type) {
				case PathIterator.SEG_LINETO:
				case PathIterator.SEG_QUADTO:
				case PathIterator.SEG_CUBICTO:
					return coordIndex + 2;
				case PathIterator.SEG_CLOSE:
					int movetoCurveIndex = curveIndex - 1;
					int movetoCoordIndex = coordIndex;
					while (movetoCurveIndex > 0 && path.getCurveType(movetoCurveIndex) != PathIterator.SEG_MOVETO) {
						movetoCoordIndex -= MutableGeneralPathIterator.curvesize[path.getCurveType(movetoCurveIndex)];
						movetoCurveIndex--;
					}
					return movetoCurveIndex;
			}
		}
		return -1;
	}

	private static void drawAllDirect(Graphics2D g, Pathable path, Endings endings) {
		int coordIndex = 0;
		int lastType = 0;
		int movetoCoordIndex = 0;
		int curvesCount = path.getCurvesCount();
		for (int i = 0; i < curvesCount; i++) {
			int type = path.getCurveType(i);
			int size = MutableGeneralPathIterator.curvesize[type];
			coordIndex += size;

			switch (type) {
				case PathIterator.SEG_LINETO:
					int index = lastType != PathIterator.SEG_CLOSE ? coordIndex - 2 : movetoCoordIndex;
					drawFigure(g, path.getCoord(index - 2), path.getCoord(index - 1), path.getCoord(coordIndex - 2), path.getCoord(coordIndex - 1), endings);
					break;

				case PathIterator.SEG_QUADTO:
				case PathIterator.SEG_CUBICTO:
					drawFigure(g, path.getCoord(coordIndex - 4), path.getCoord(coordIndex - 3), path.getCoord(coordIndex - 2), path.getCoord(coordIndex - 1), endings);
					break;

				case PathIterator.SEG_CLOSE:
					drawFigure(g, path.getCoord(coordIndex - 2), path.getCoord(coordIndex - 1), path.getCoord(movetoCoordIndex), path.getCoord(movetoCoordIndex + 1), endings);
					break;

				case PathIterator.SEG_MOVETO:
					movetoCoordIndex = coordIndex;
					break;
			}

			lastType = type;
		}
	}

	private static void drawAllIndirect(Graphics2D g, Pathable path, Endings endings) {
		int coordIndex = 0;
		int lastType = 0;
		int movetoCoordIndex = 0;
		int curvesCount = path.getCurvesCount();
		for (int i = 0; i < curvesCount; i++) {
			int type = path.getCurveType(i);

			switch (type) {
				case PathIterator.SEG_LINETO:
				case PathIterator.SEG_QUADTO:
				case PathIterator.SEG_CUBICTO:
					int index = lastType != PathIterator.SEG_CLOSE ? coordIndex - 2 : movetoCoordIndex;
					drawFigure(g, path.getCoord(coordIndex), path.getCoord(coordIndex + 1), path.getCoord(index), path.getCoord(index + 1), endings);
					break;

				case PathIterator.SEG_CLOSE:
					drawFigure(g, path.getCoord(movetoCoordIndex), path.getCoord(movetoCoordIndex + 1), path.getCoord(coordIndex - 2), path.getCoord(coordIndex - 1), endings);
					break;

				case PathIterator.SEG_MOVETO:
					movetoCoordIndex = coordIndex;
					break;
			}

			int size = MutableGeneralPathIterator.curvesize[type];
			coordIndex += size;
			lastType = type;
		}
	}

	private static void drawStart(Graphics2D g, Pathable path, Endings endings) {
		int type = path.getCurveType(0);
		double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		switch (type) {
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_QUADTO:
			case PathIterator.SEG_CUBICTO:
				x1 = 0;
				y1 = 0;
				x2 = path.getCoord(0);
				y2 = path.getCoord(1);
				break;

			case PathIterator.SEG_MOVETO:
				if (path.getCurvesCount() == 1) {
					return;
				}
				x1 = path.getCoord(0);
				y1 = path.getCoord(1);
				x2 = path.getCoord(2);
				y2 = path.getCoord(3);
				break;
		}

		if (x1 == x2 && y1 == y2) {
			return;
		}

		drawFigure(g, x2, y2, x1, y1, endings);
	}

	private static void drawEnd(Graphics2D g, Pathable path, Endings endings) {
		int curveIndex = path.getCurvesCount() - 1;
		int type = path.getCurveType(curveIndex);
		int coordIndex = path.getCoordsCount() - 1;
		if (type == PathIterator.SEG_MOVETO) {
			curveIndex--;
			coordIndex -= 2;
		}

		double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		switch (type) {
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_QUADTO:
			case PathIterator.SEG_CUBICTO:
				x1 = path.getCoord(coordIndex - 3);
				y1 = path.getCoord(coordIndex - 2);
				x2 = path.getCoord(coordIndex - 1);
				y2 = path.getCoord(coordIndex);
				break;

			case PathIterator.SEG_CLOSE:
				int movetoCurveIndex = curveIndex;
				int movetoCoordIndex = coordIndex;
				while (movetoCurveIndex > 0 && path.getCurveType(movetoCurveIndex) != PathIterator.SEG_MOVETO) {
					movetoCoordIndex -= MutableGeneralPathIterator.curvesize[path.getCurveType(movetoCurveIndex)];
					movetoCurveIndex--;
				}

				x1 = path.getCoord(coordIndex - 1);
				y1 = path.getCoord(coordIndex);
				x2 = path.getCoord(movetoCoordIndex - 1);
				y2 = path.getCoord(movetoCoordIndex);
				break;
		}

		drawFigure(g, x1, y1, x2, y2, endings);
	}

	private static void drawFigure(Graphics2D g, double x1, double y1, double x2, double y2, Endings endings) {
		switch (endings.getFigure()) {
			case Endings.FIGURE_CIRCLE:
				drawCircle(g, x1, y1, x2, y2, endings);
				break;

			case Endings.FIGURE_SQUARE:
				drawSquare(g, x1, y1, x2, y2, endings);
				break;

			case Endings.FIGURE_ARROW:
				drawArraw(g, x1, y1, x2, y2, endings);
				break;

			case Endings.FIGURE_ROMB:
				drawRomb(g, x1, y1, x2, y2, endings);
				break;
		}
	}

	private static void drawCircle(Graphics2D g, double x1, double y1, double x2, double y2, Endings endings) {
		if (endings.isFill()) {
			g.fillArc((int) x2 - 2, (int) y2 - 2, 5, 5, 0, 360);
		} else {
			g.drawArc((int) x2 - 2, (int) y2 - 2, 5, 5, 0, 360);
		}
	}

	private static int[] xarray = new int[4];

	private static int[] yarray = new int[4];

	private static void drawArraw(Graphics2D g, double x1, double y1, double x2, double y2, Endings endings) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double r = Math.sqrt(dx * dx + dy * dy);
		if (r != 0) {
			double koef = 12.0 / r;
			double dx_ = dx * koef;
			double dy_ = dy * koef;
			double x_ = x2 - dx_;
			double y_ = y2 - dy_;
			double dxp = -dy_ / 3.0;
			double dyp = dx_ / 3.0;

			if (endings.isDirect()) {
				xarray[0] = (int) x2;
				xarray[1] = (int) (x_ + dxp);
				xarray[2] = (int) (x_ - dxp);

				yarray[0] = (int) y2;
				yarray[1] = (int) (y_ + dyp);
				yarray[2] = (int) (y_ - dyp);
			} else {
				xarray[0] = (int) x_;
				xarray[1] = (int) (x2 + dxp);
				xarray[2] = (int) (x2 - dxp);

				yarray[0] = (int) y_;
				yarray[1] = (int) (y2 + dyp);
				yarray[2] = (int) (y2 - dyp);
			}
			drawPolygon(g, 3, endings);
		}
	}

	private static void drawSquare(Graphics2D g, double x1, double y1, double x2, double y2, Endings endings) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double r = Math.sqrt(dx * dx + dy * dy);
		if (r != 0) {
			double koef = 3.0 / r;
			double dx_ = dx * koef;
			double dy_ = dy * koef;
			double x_1 = x2 - dx_;
			double y_1 = y2 - dy_;
			double x_2 = x2 + dx_;
			double y_2 = y2 + dy_;
			double dxp = -dy_;
			double dyp = dx_;

			xarray[0] = (int) (x_1 + dxp);
			xarray[1] = (int) (x_1 - dxp);
			xarray[2] = (int) (x_2 - dxp);
			xarray[3] = (int) (x_2 + dxp);

			yarray[0] = (int) (y_1 + dyp);
			yarray[1] = (int) (y_1 - dyp);
			yarray[2] = (int) (y_2 - dyp);
			yarray[3] = (int) (y_2 + dyp);

			drawPolygon(g, 4, endings);
		}
	}

	private static void drawRomb(Graphics2D g, double x1, double y1, double x2, double y2, Endings endings) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double r = Math.sqrt(dx * dx + dy * dy);
		if (r != 0) {
			double koef = 5.0 / r;
			double dx_ = dx * koef;
			double dy_ = dy * koef;
			double x_1 = x2 - dx_;
			double y_1 = y2 - dy_;
			double x_2 = x2 + dx_;
			double y_2 = y2 + dy_;
			double dxp = -dy_;
			double dyp = dx_;

			xarray[0] = (int) (x_1);
			xarray[1] = (int) (x2 - dxp);
			xarray[2] = (int) (x_2);
			xarray[3] = (int) (x2 + dxp);

			yarray[0] = (int) (y_1);
			yarray[1] = (int) (y2 - dyp);
			yarray[2] = (int) (y_2);
			yarray[3] = (int) (y2 + dyp);

			drawPolygon(g, 4, endings);
		}
	}

	private static void drawPolygon(Graphics2D g, int size, Endings endings) {
		if (endings.isFill()) {
			g.fillPolygon(xarray, yarray, size);
		} else {
			g.drawPolygon(xarray, yarray, size);
		}
	}

	@Override
	public int getType() {
		return ENDINGS;
	}

	@Override
	public Object clone() {
		EndingsPainter painter = (EndingsPainter) super.clone();
		painter.endings = (Endings) endings.clone();
		return painter;
	}
}
