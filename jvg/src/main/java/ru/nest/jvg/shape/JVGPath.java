package ru.nest.jvg.shape;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import ru.nest.jvg.event.JVGComponentEvent;
import ru.nest.jvg.geom.CoordinablePath;
import ru.nest.jvg.geom.CoordinablePathIterator;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.geom.coord.Coordinate;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;

public class JVGPath extends JVGShape {
	static List<Painter> defaultPainters = null;

	public static List<Painter> getDefaultPainters() {
		if (defaultPainters == null) {
			defaultPainters = new ArrayList<>();
			defaultPainters.add(new OutlinePainter(null, OutlinePainter.DEFAULT_COLOR));
		}
		return defaultPainters;
	}

	private Resource<? extends Stroke> pathStroke;

	protected StringBuffer pathId = new StringBuffer("path");

	protected MutableGeneralPath transformedPathShape = null;

	public JVGPath(Shape shape) {
		this(shape, true);
	}

	public JVGPath(Shape shape, boolean coordinable) {
		setShape(shape, coordinable);
		setName("Path");
		addPainter(new OutlinePainter(null, OutlinePainter.DEFAULT_COLOR));
	}

	@Override
	public void setShape(Shape shape, boolean coordinable) {
		super.setShape(shape, coordinable, shapeId, pathId);
	}

	@Override
	public void validate() {
		if (pathStroke != null && !pathStroke.isValid()) {
			invalidate();
		}

		if (!isValid()) {
			Shape pathShape = getShape(pathId);
			if (pathStroke != null) {
				pathStroke.validate();

				// shape
				Shape strokedShape = computeStrokedShape();
				setClientProperty(shapeId, new MutableGeneralPath(strokedShape));

				// transformed path shape
				transformedPathShape = new MutableGeneralPath(pathShape);
				transformedPathShape.transform(getTransform());
			} else {
				setClientProperty(shapeId, pathShape);
			}

			validateShape();

			if (transformedPathShape == null) {
				transformedPathShape = transformedShape;
			}
		}
	}

	protected Shape computeStrokedShape() {
		Shape pathShape = getShape(pathId);
		Shape strokedShape = pathStroke.getResource().createStrokedShape(pathShape);
		return strokedShape;
	}

	protected void validateShape() {
		super.validate();
	}

	@Override
	public void invalidate() {
		if (isValid()) {
			super.invalidate();
			transformedPathShape = null;
		}
	}

	public Shape getPathShape() {
		if (pathStroke != null) {
			return getShape(pathId);
		} else {
			return getShape();
		}
	}

	public MutableGeneralPath getTransformedPathShape() {
		if (transformedPathShape == null) {
			validate();
		}
		return transformedPathShape;
	}

	public void add(Shape shape) {
		shape = getInverseTransform().createTransformedShape(shape);

		Shape oldShape = getPathShape();
		boolean coordinable = oldShape instanceof CoordinablePath;

		Area area1 = new Area(shape);
		Area area2 = new Area(oldShape);
		area2.add(area1);

		setShape(area2, coordinable);
	}

	public void subtract(Shape shape) {
		shape = getInverseTransform().createTransformedShape(shape);

		Shape oldShape = getPathShape();
		boolean coordinable = oldShape instanceof CoordinablePath;

		Area area1 = new Area(shape);
		Area area2 = new Area(oldShape);
		area2.subtract(area1);

		setShape(area2, coordinable);
	}

	public void exclusiveOr(Shape shape) {
		shape = getInverseTransform().createTransformedShape(shape);

		Shape oldShape = getPathShape();
		boolean coordinable = oldShape instanceof CoordinablePath;

		Area area1 = new Area(shape);
		Area area2 = new Area(oldShape);
		area2.exclusiveOr(area1);

		setShape(area2, coordinable);
	}

	public void intersect(Shape shape) {
		shape = getInverseTransform().createTransformedShape(shape);

		Shape oldShape = getPathShape();
		boolean coordinable = oldShape instanceof CoordinablePath;

		Area area1 = new Area(shape);
		Area area2 = new Area(oldShape);
		area2.intersect(area1);

		setShape(area2, coordinable);
	}

	public boolean moveTo(double x, double y) {
		boolean ok = false;
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			ok = path.moveTo(new Coordinate(x), new Coordinate(y));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			ok = path.moveTo(x, y);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
		return ok;
	}

	public boolean insertMoveTo(int index, double x, double y) {
		boolean ok = false;
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			ok = path.insertMoveTo(index, new Coordinate(x), new Coordinate(y));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			ok = path.insertMoveTo(index, x, y);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
		return ok;
	}

	public boolean setMoveTo(int index, double x, double y) {
		boolean ok = false;
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			ok = path.setMoveTo(index, new Coordinate(x), new Coordinate(y));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			ok = path.setMoveTo(index, x, y);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
		return ok;
	}

	public boolean moveTo(Coordinable x, Coordinable y) {
		boolean ok = false;
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			ok = path.moveTo(x, y);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			ok = path.moveTo(x.getCoord(), y.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
		return ok;
	}

	public boolean insertMoveTo(int index, Coordinable x, Coordinable y) {
		boolean ok = false;
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			ok = path.insertMoveTo(index, x, y);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			ok = path.insertMoveTo(index, x.getCoord(), y.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
		return ok;
	}

	public boolean setMoveTo(int index, Coordinable x, Coordinable y) {
		boolean ok = false;
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			ok = path.setMoveTo(index, x, y);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			ok = path.setMoveTo(index, x.getCoord(), y.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
		return ok;
	}

	public void lineTo(double x, double y) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.lineTo(new Coordinate(x), new Coordinate(y));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.lineTo(x, y);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void insertLineTo(int index, double x, double y) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.insertLineTo(index, new Coordinate(x), new Coordinate(y));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.insertLineTo(index, x, y);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void setLineTo(int index, double x, double y) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.setLineTo(index, new Coordinate(x), new Coordinate(y));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.setLineTo(index, x, y);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void lineTo(Coordinable x, Coordinable y) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.lineTo(x, y);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.lineTo(x.getCoord(), y.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void insertLineTo(int index, Coordinable x, Coordinable y) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.insertLineTo(index, x, y);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.insertLineTo(index, x.getCoord(), y.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void setLineTo(int index, Coordinable x, Coordinable y) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.setLineTo(index, x, y);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.setLineTo(index, x.getCoord(), y.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void quadTo(double x1, double y1, double x2, double y2) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.quadTo(new Coordinate(x1), new Coordinate(y1), new Coordinate(x2), new Coordinate(y2));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.quadTo(x1, y1, x2, y2);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void insertQuadTo(int index, double x1, double y1, double x2, double y2) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.insertQuadTo(index, new Coordinate(x1), new Coordinate(y1), new Coordinate(x2), new Coordinate(y2));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.insertQuadTo(index, x1, y1, x2, y2);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void setQuadTo(int index, double x1, double y1, double x2, double y2) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.setQuadTo(index, new Coordinate(x1), new Coordinate(y1), new Coordinate(x2), new Coordinate(y2));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.setQuadTo(index, x1, y1, x2, y2);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void quadTo(Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.quadTo(x1, y1, x2, y2);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.quadTo(x1.getCoord(), y1.getCoord(), x2.getCoord(), y2.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void insertQuadTo(int index, Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.insertQuadTo(index, x1, y1, x2, y2);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.insertQuadTo(index, x1.getCoord(), y1.getCoord(), x2.getCoord(), y2.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void setQuadTo(int index, Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.setQuadTo(index, x1, y1, x2, y2);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.setQuadTo(index, x1.getCoord(), y1.getCoord(), x2.getCoord(), y2.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.curveTo(new Coordinate(x1), new Coordinate(y1), new Coordinate(x2), new Coordinate(y2), new Coordinate(x3), new Coordinate(y3));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.curveTo(x1, y1, x2, y2, x3, y3);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void insertCurveTo(int index, double x1, double y1, double x2, double y2, double x3, double y3) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.insertCurveTo(index, new Coordinate(x1), new Coordinate(y1), new Coordinate(x2), new Coordinate(y2), new Coordinate(x3), new Coordinate(y3));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.insertCurveTo(index, x1, y1, x2, y2, x3, y3);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void setCurveTo(int index, double x1, double y1, double x2, double y2, double x3, double y3) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.setCurveTo(index, new Coordinate(x1), new Coordinate(y1), new Coordinate(x2), new Coordinate(y2), new Coordinate(x3), new Coordinate(y3));
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.setCurveTo(index, x1, y1, x2, y2, x3, y3);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void curveTo(Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2, Coordinable x3, Coordinable y3) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.curveTo(x1, y1, x2, y2, x3, y3);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.curveTo(x1.getCoord(), y1.getCoord(), x2.getCoord(), y2.getCoord(), x3.getCoord(), y3.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void insertCurveTo(int index, Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2, Coordinable x3, Coordinable y3) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.insertCurveTo(index, x1, y1, x2, y2, x3, y3);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.insertCurveTo(index, x1.getCoord(), y1.getCoord(), x2.getCoord(), y2.getCoord(), x3.getCoord(), y3.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void setCurveTo(int index, Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2, Coordinable x3, Coordinable y3) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.setCurveTo(index, x1, y1, x2, y2, x3, y3);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.setCurveTo(index, x1.getCoord(), y1.getCoord(), x2.getCoord(), y2.getCoord(), x3.getCoord(), y3.getCoord());
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public boolean closePath() {
		boolean ok = false;
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			ok = path.closePath();
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			ok = path.closePath();
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
		return ok;
	}

	public boolean getLastPoint(Point2D point) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			if (path.numCoords >= 2) {
				point.setLocation(path.pointCoords[path.numCoords - 2].getCoord(), path.pointCoords[path.numCoords - 1].getCoord());
				return true;
			}
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			if (path.numCoords >= 2) {
				point.setLocation(path.pointCoords[path.numCoords - 2], path.pointCoords[path.numCoords - 1]);
				return true;
			}
		}
		return false;
	}

	public boolean getFirstCurvePoint(int index, Point2D point) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			int coordIndex = path.getCoordIndex(index);
			point.setLocation(path.pointCoords[coordIndex - 2].getCoord(), path.pointCoords[coordIndex - 1].getCoord());
			return true;
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			int coordIndex = path.getCoordIndex(index);
			point.setLocation(path.pointCoords[coordIndex - 2], path.pointCoords[coordIndex - 1]);
			return true;
		} else {
			return false;
		}
	}

	public boolean getLastCurvePoint(int index, Point2D point) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			byte type = path.pointTypes[index];
			int curvesize = CoordinablePathIterator.curvesize[type];
			int coordIndex = path.getCoordIndex(index) + curvesize;
			point.setLocation(path.pointCoords[coordIndex - 2].getCoord(), path.pointCoords[coordIndex - 1].getCoord());
			return true;
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			byte type = path.pointTypes[index];
			int curvesize = CoordinablePathIterator.curvesize[type];
			int coordIndex = path.getCoordIndex(index) + curvesize;
			point.setLocation(path.pointCoords[coordIndex - 2], path.pointCoords[coordIndex - 1]);
			return true;
		} else {
			return false;
		}
	}

	public int getNumTypes() {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			return path.numTypes;
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			return path.numTypes;
		}
		return 0;
	}

	public int getNumCoords() {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			return path.numCoords;
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			return path.numCoords;
		}
		return 0;
	}

	public int getType(int index) {
		if (index >= 0) {
			Shape shape = getPathShape();
			if (shape instanceof CoordinablePath) {
				CoordinablePath path = (CoordinablePath) shape;
				return path.pointTypes[index];
			} else if (shape instanceof MutableGeneralPath) {
				MutableGeneralPath path = (MutableGeneralPath) shape;
				return path.pointTypes[index];
			}
		}
		return 0;
	}

	public double getCoord(int index) {
		if (index >= 0) {
			Shape shape = getPathShape();
			if (shape instanceof CoordinablePath) {
				CoordinablePath path = (CoordinablePath) shape;
				return path.pointCoords[index].getCoord();
			} else if (shape instanceof MutableGeneralPath) {
				MutableGeneralPath path = (MutableGeneralPath) shape;
				return path.pointCoords[index];
			}
		}
		return 0;
	}

	public void setCoord(int index, double value) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.pointCoords[index].setCoord(value);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.pointCoords[index] = value;
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public int getCoordsCount() {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			return path.numCoords;
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			return path.numCoords;
		}
		return 0;
	}

	public void setCoord(int index, Coordinable value) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.pointCoords[index] = value;
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.pointCoords[index] = value.getCoord();
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void setPoint(int index, double x, double y) {
		if (index >= 0) {
			Shape shape = getPathShape();
			if (shape instanceof CoordinablePath) {
				CoordinablePath path = (CoordinablePath) shape;
				path.pointCoords[index].setCoord(x);
				path.pointCoords[index + 1].setCoord(y);
			} else if (shape instanceof MutableGeneralPath) {
				MutableGeneralPath path = (MutableGeneralPath) shape;
				path.pointCoords[index] = x;
				path.pointCoords[index + 1] = y;
			}
			invalidate();

			JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
			dispatchEvent(event);
		}
	}

	public void setPoint(int index, Coordinable x, Coordinable y) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.pointCoords[index] = x;
			path.pointCoords[index + 1] = y;
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.pointCoords[index] = x.getCoord();
			path.pointCoords[index + 1] = y.getCoord();
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void deleteLast() {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.deleteLast();
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			path.deleteLast();
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	public void delete(int index) {
		if (index >= 0) {
			Shape shape = getPathShape();
			if (shape instanceof CoordinablePath) {
				CoordinablePath path = (CoordinablePath) shape;
				if (path.numTypes <= 1) {
					return;
				}
				path.delete(index);
			} else if (shape instanceof MutableGeneralPath) {
				MutableGeneralPath path = (MutableGeneralPath) shape;
				if (path.numTypes <= 1) {
					return;
				}
				path.delete(index);
			}
			invalidate();

			JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
			dispatchEvent(event);
		}
	}

	public void insertPoint(int coordIndex, Coordinable x, Coordinable y) {
		if (coordIndex >= 0) {
			Shape shape = getPathShape();
			if (shape instanceof CoordinablePath) {
				CoordinablePath path = (CoordinablePath) shape;
				path.insertPoint(coordIndex, x, y);
			} else if (shape instanceof MutableGeneralPath) {
				MutableGeneralPath path = (MutableGeneralPath) shape;
				path.insertPoint(coordIndex, x.getCoord(), y.getCoord());
			}
			invalidate();

			JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
			dispatchEvent(event);
		}
	}

	public void deletePoint(int coordIndex) {
		if (coordIndex >= 0) {
			Shape shape = getPathShape();
			if (shape instanceof CoordinablePath) {
				CoordinablePath path = (CoordinablePath) shape;
				if (path.numTypes <= 1) {
					return;
				}
				path.deletePoint(coordIndex);
			} else if (shape instanceof MutableGeneralPath) {
				MutableGeneralPath path = (MutableGeneralPath) shape;
				if (path.numTypes <= 1) {
					return;
				}
				path.deletePoint(coordIndex);
			}
			invalidate();

			JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
			dispatchEvent(event);
		}
	}

	public int getCoordIndex(double x, double y, int startIndex) {
		double[] point = { x, y };
		getInverseTransform().transform(point, 0, point, 0, 1);
		x = point[0];
		y = point[1];
		double delta = 2 / getPaneScale();

		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			return path.getCoordIndex(x, y, startIndex, delta);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			return path.getCoordIndex(x, y, startIndex, delta);
		}
		return -1;
	}

	public Coordinable[] getCurvePoints(int index) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			return path.getCurvePoints(index);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			Double[] floatCoords = path.getCurvePoints(index);
			if (floatCoords != null) {
				Coordinable[] coords = new Coordinable[floatCoords.length];
				for (int i = 0; i < floatCoords.length; i++) {
					coords[i] = new Coordinate(floatCoords[i]);
				}
				return coords;
			}
		}
		return null;
	}

	public Coordinable[] getPoint(int coordIndex) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			return new Coordinable[] { path.pointCoords[coordIndex], path.pointCoords[coordIndex + 1] };
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			return new Coordinable[] { new Coordinate(path.pointCoords[coordIndex]), new Coordinate(path.pointCoords[coordIndex + 1]) };
		}
		return null;
	}

	public int getCurveIndex(int coordIndex) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			return path.getCurveIndex(coordIndex);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			return path.getCurveIndex(coordIndex);
		}
		return -1;
	}

	public int getCoordIndex(int curveIndex) {
		Shape shape = getPathShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			return path.getCoordIndex(curveIndex);
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
			return path.getCoordIndex(curveIndex);
		}
		return -1;
	}

	public Resource<? extends Stroke> getPathStroke() {
		return pathStroke;
	}

	public void setPathStroke(Resource<? extends Stroke> pathStroke) {
		this.pathStroke = pathStroke;
		invalidate();
	}

	@Override
	public MutableGeneralPath getPath() {
		return getPath(pathId);
	}
}
