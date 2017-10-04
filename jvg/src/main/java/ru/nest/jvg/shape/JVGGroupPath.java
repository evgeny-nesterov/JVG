package ru.nest.jvg.shape;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import ru.nest.jvg.Filter;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.actionarea.JVGAddSubPathActionArea;
import ru.nest.jvg.actionarea.JVGCustomActionArea;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.geom.GeomUtil;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.paint.PainterFilter;
import ru.nest.strokes.ArrowStroke;

public class JVGGroupPath extends JVGPath {
	public final static int EDIT_MODE__NONE = 0;

	public final static int EDIT_MODE__MOVE_SUBPATH = 1;

	public final static int EDIT_MODE__ADD_SUBPATH = 2;

	private double length;

	private Resource<? extends Stroke> pathStroke;

	private JVGSubPath leadSubPath;

	private int editMode = EDIT_MODE__NONE;

	private int paintOrderType = JVGGroup.PAINT_ORDER_COMPONENT;

	public JVGGroupPath(Shape shape) {
		this(shape, true);
	}

	public JVGGroupPath(Shape shape, boolean coordinable) {
		super(shape, coordinable);

		setName("GroupPath");
		setAntialias(true);
		setFill(true);

		addPainter(new FillPainter(FillPainter.DEFAULT_COLOR));
		addPainter(new OutlinePainter(new StrokeResource<Stroke>(1f), ColorResource.black));

		leadSubPath = add(1, true);

		addMouseListener(new JVGMouseAdapter() {
			@Override
			public void mousePressed(JVGMouseEvent e) {
				if (e.getButton() == JVGMouseEvent.BUTTON1 && e.getClickCount() == 2) {
					if (isSelectable()) {
						if (!e.isControlDown()) {
							if (editMode != EDIT_MODE__MOVE_SUBPATH) {
								JVGSubPath p = (JVGSubPath) getChild(e.getX(), e.getY(), new Filter() {
									@Override
									public boolean pass(JVGComponent component) {
										return component instanceof JVGSubPath;
									}
								});

								if (p != null) {
									if (!e.isControlDown()) {
										getPane().getSelectionManager().clearSelection();
									}
									setSelected(false);
									p.setSelected(true);
									p.setFocused(true);
									e.consume();
								}
								setEditMode(EDIT_MODE__MOVE_SUBPATH);
							} else {
								setEditMode(EDIT_MODE__NONE);
							}
						} else {
							setEditMode(EDIT_MODE__ADD_SUBPATH);
						}
					}
				} else if (e.getButton() == JVGMouseEvent.BUTTON1 && e.getClickCount() == 3) {
					if (isSelectable() && editMode == EDIT_MODE__MOVE_SUBPATH) {
						JVGSubPath p = (JVGSubPath) getChild(e.getX(), e.getY(), new Filter() {
							@Override
							public boolean pass(JVGComponent component) {
								return component instanceof JVGSubPath;
							}
						});

						if (p != null) {
							JVGComponent[] childs = getChilds(JVGSubPath.class);
							if (childs != null) {
								getPane().getSelectionManager().setSelection(childs);
								p.setFocused(true);
								e.consume();
							}
						}
					}
				}
			}
		});

		setPathStroke(new StrokeResource<ArrowStroke>(new ArrowStroke(14, 14, 14, ArrowStroke.DIRECTION_DIRECT)));
	}

	protected void processMouseEvent(JVGMouseEvent e) {
		if (e.getButton() == JVGMouseEvent.BUTTON1 && e.getClickCount() == 1) {
			if (isSelectable() && editMode == EDIT_MODE__MOVE_SUBPATH) {
				boolean hasSelected = false;
				JVGComponent[] childs = getChilds(JVGSubPath.class);
				if (childs != null) {
					for (JVGComponent c : childs) {
						if (c.isSelected()) {
							hasSelected = true;
							break;
						}
					}
				}

				if (hasSelected) {
					JVGSubPath p = (JVGSubPath) getChild(e.getX(), e.getY(), new Filter() {
						@Override
						public boolean pass(JVGComponent component) {
							return component instanceof JVGSubPath;
						}
					});

					if (p != null) {
						if (!e.isControlDown()) {
							getPane().getSelectionManager().clearSelection();
						}
						setSelected(false);
						p.setSelected(true);
						p.setFocused(true);
						return;
					}
				}
			}
		}
		super.processMouseEvent(e);
	}

	private boolean locked = true;

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	private final static Filter ownActionsFilter = new Filter() {
		@Override
		public boolean pass(JVGComponent component) {
			return component.isVisible() && component instanceof JVGActionArea && !(component instanceof JVGCustomActionArea);
		}
	};

	private final static Filter deepActionsFilter = new Filter() {
		@Override
		public boolean pass(JVGComponent component) {
			if (component.isVisible()) {
				if (component instanceof JVGCustomActionArea) {
					return true;
				}
				if (component instanceof JVGActionArea && component.getParent().isFocused()) {
					return true;
				}
			}
			return false;
		}
	};

	@Override
	public void paintShape(Graphics2D g) {
		// group is transparent
	}

	@Override
	public void paintChildren(Graphics2D g) {
		switch (paintOrderType) {
			case JVGGroup.PAINT_ORDER_COMPONENT:
				super.paintChildren(g);
				break;
			case JVGGroup.PAINT_ORDER_FILL_FIRST:
			case JVGGroup.PAINT_ORDER_OUTLINE_FIRST:
				final PainterFilter f1 = new PainterFilter() {
					@Override
					public boolean pass(JVGShape c, Painter painter) {
						return painter.getType() == (paintOrderType == JVGGroup.PAINT_ORDER_OUTLINE_FIRST ? Painter.OUTLINE : Painter.FILL);
					}
				};

				int childs_count = this.childrenCount;
				if (childs_count > 0) {
					JVGComponent[] childs = this.children;
					for (int i = 0; i < childs_count; i++) {
						JVGComponent c = childs[i];
						if (c.isVisible() && c instanceof JVGShape) {
							JVGShape shape = (JVGShape) c;
							shape.setPainterFilter(f1);
						}
					}
				}
				super.paintChildren(g);

				if (childs_count > 0) {
					PainterFilter f2 = new PainterFilter() {
						@Override
						public boolean pass(JVGShape c, Painter painter) {
							return !f1.pass(c, painter);
						}
					};

					JVGComponent[] childs = this.children;
					for (int i = 0; i < childs_count; i++) {
						JVGComponent c = childs[i];
						if (c.isVisible() && c instanceof JVGShape) {
							JVGShape shape = (JVGShape) c;
							shape.setPainterFilter(f2);
						}
					}
				}
				super.paintChildren(g);
				break;
		}
	}

	@Override
	public void invalidate() {
		if (validateTree) {
			super.invalidate();
		}
	}

	@Override
	public void validate() {
		if (!isValid()) {
			if (isSelectable()) {
				JVGComponent[] a = getChilds(JVGAddSubPathActionArea.class);
				if (a != null && a.length > 0) {
					JVGActionArea aa = (JVGActionArea) a[0];
					boolean active = editMode == EDIT_MODE__ADD_SUBPATH;
					aa.setActive(active);
				}
			}

			Arrays.sort(children, 0, childrenCount, new Comparator<JVGComponent>() {
				@Override
				public int compare(JVGComponent o1, JVGComponent o2) {
					boolean is1 = o1 instanceof JVGSubPath;
					boolean is2 = o2 instanceof JVGSubPath;
					if (is1 && is2) {
						JVGSubPath p1 = (JVGSubPath) o1;
						JVGSubPath p2 = (JVGSubPath) o2;
						return -p1.compareTo(p2);
					}
					if (is1 && !is2) {
						return -1;
					}
					if (!is1 && is2) {
						return 1;
					}
					return 0;
				}
			});

			MutableGeneralPath path = getPath(pathId);
			length = GeomUtil.getPathLength(path);

			validateTree = false;
			validateShape();

			JVGComponent[] childs = getChilds(JVGSubPath.class);
			if (childs != null) {
				Arrays.sort(childs);
				PathIterator it = new FlatteningPathIterator(path.getPathIterator(null), 1);
				double points[] = new double[6];
				double moveX = 0, moveY = 0;
				double lastX = 0, lastY = 0;
				double thisX = 0, thisY = 0;
				int type = 0;
				double totalLength = 0;
				MutableGeneralPath elementPath = new MutableGeneralPath();
				while (!it.isDone()) {
					type = it.currentSegment(points);
					switch (type) {
						case PathIterator.SEG_MOVETO:
							moveX = lastX = points[0];
							moveY = lastY = points[1];
							elementPath.moveTo(moveX, moveY);
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
							double r = Math.sqrt(dx * dx + dy * dy);
							double nextLength = totalLength + r;

							double r1 = totalLength / length;
							double r2 = nextLength / length;
							for (int i = 0; i < childs.length; i++) {
								JVGComponent c = childs[i];
								if (c instanceof JVGSubPath) {
									JVGSubPath p = (JVGSubPath) c;
									if (!p.isLead() && p.getPosition() >= r1 && p.getPosition() <= r2) {
										double deltaRatio = (p.getPosition() - r1) / (r2 - r1);
										double x = lastX + deltaRatio * (thisX - lastX);
										double y = lastY + deltaRatio * (thisY - lastY);

										elementPath.lineTo(x, y);
										p.setShape(elementPath, false);

										// start collecting path for next element
										elementPath = new MutableGeneralPath();
										elementPath.moveTo(x, y);
									}
								}
							}

							elementPath.lineTo(thisX, thisY);

							totalLength = nextLength;
							lastX = thisX;
							lastY = thisY;
							break;
					}
					it.next();
				}

				leadSubPath.setShape(elementPath, false);
				//				leadSubPath.validate();
			}
			validateTree = true;

			// compute children shapes
			valid = true;
			validateTree();

			if (getPathStroke() != null) {
				// shape
				Shape strokedShape = computeStrokedShape();
				if (strokedShape != path) {
					// TODO ???
					//setClientProperty(shapeId, new MutableGeneralPath(strokedShape));

					// transformed path shape
					transformedPathShape = new MutableGeneralPath(path);
					transformedPathShape.transform(getTransform());
				}
			} else {
				setClientProperty(shapeId, path);
			}

			validateTree = false;
			valid = false;
			validateShape();
			valid = true;
			validateTree = true;

			if (transformedPathShape == null) {
				transformedPathShape = transformedShape;
			}
		}
	}

	protected Shape computeStrokedShape() {
		Area a = null;
		for (int i = 0; i < childrenCount; i++) {
			JVGComponent c = children[i];
			if (c instanceof JVGSubPath) {
				JVGSubPath p = (JVGSubPath) c;
				if (a == null) {
					a = new Area(p.getShape());
				} else {
					a.add(new Area(p.getShape()));
				}
			}
		}

		if (a != null && !a.isEmpty()) {
			return a;
		} else {
			return getShape(pathId);
		}
	}

	private boolean validateTree = true;

	protected void validateTree() {
		if (validateTree) {
			super.validateTree();
		}
	}

	@Override
	public JVGComponent getComponent(double x, double y) {
		if (locked) {
			// get controls only
			JVGComponent control = super.getDeepestComponent(x, y, deepActionsFilter);
			if (control != null) {
				return control;
			}

			// get actions only
			JVGComponent c = super.getComponent(x, y, ownActionsFilter);
			if (c != null) {
				return c;
			} else {
				return this;
			}
		} else {
			return super.getComponent(x, y);
		}
	}

	@Override
	public void setSelected(boolean selected, boolean clear) {
		if (locked) {
			super.setSelected(selected, clear);
		}
	}

	@Override
	public boolean contains(double x, double y, Filter filter) {
		return containsChilds(x, y, filter);
	}

	@Override
	public void selectChilds(Shape shape, Set<JVGComponent> selection) {
		if (!locked) {
			super.selectChilds(shape, selection);
		}
	}

	@Override
	protected boolean isContainedIn(Shape selectionShape) {
		if (selectionShape instanceof Rectangle2D) {
			return super.isContainedIn(selectionShape);
		} else {
			int childrenCount = this.childrenCount;
			JVGComponent[] children = this.children;
			for (int i = 0; i < childrenCount; i++) {
				if (children[i] instanceof JVGShape) {
					JVGShape child = (JVGShape) children[i];
					if (!child.isContainedIn(selectionShape)) {
						return false;
					}
				}
			}
			return true;
		}
	}

	@Override
	public void setAntialias(boolean antialias) {
		super.setAntialias(antialias);
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			if (childs[i] instanceof JVGShape) {
				JVGShape child = (JVGShape) childs[i];
				child.setAntialias(antialias);
			}
		}
	}

	public JVGSubPath add(double position) {
		return add(position, false);
	}

	/**
	 * @param position in percent of path'es group length 
	 */
	private JVGSubPath add(double position, boolean lead) {
		JVGSubPath subPath = new JVGSubPath(position, lead);
		subPath.setPathStroke(getPathStroke());
		subPath.setFocusable(isFocusable());
		subPath.setOriginalBounds(isOriginalBounds());
		subPath.setFill(isFill());
		subPath.setAntialias(isAntialias());
		subPath.setComposePaint(getComposePaint());
		subPath.setCursor(getCursor());
		subPath.setSelectable(isSelectable());

		// copy painters
		List<Painter> painters = null;
		if (this.painters != null) {
			painters = new ArrayList<Painter>(this.painters.size());
			for (Painter painter : this.painters) {
				painters.add((Painter) painter.clone());
			}
		}
		subPath.setPainters(painters);

		add(subPath);

		invalidate();
		return subPath;
	}

	@Override
	public void setPathStroke(Resource<? extends Stroke> pathStroke) {
		this.pathStroke = pathStroke;
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			if (childs[i] instanceof JVGPath) {
				JVGPath child = (JVGPath) childs[i];
				child.setPathStroke(pathStroke);
			}
		}
		invalidate();
	}

	@Override
	public Resource<? extends Stroke> getPathStroke() {
		return pathStroke;
	}

	//	@Override
	//	public void addPainter(int index, Painter painter) {
	//		super.addPainter(index, painter);
	//		int childs_count = this.childrenCount;
	//		JVGComponent[] childs = this.children;
	//		for(int i = 0; i < childs_count; i++) {
	//			if(childs[i] instanceof JVGPath) {
	//				JVGPath child = (JVGPath)childs[i];
	//				child.addPainter(index, (Painter)painter.clone());
	//			}
	//		}
	//		invalidate();
	//	}
	//
	//	@Override
	//	public Painter removePainter(int index) {
	//		Painter painter = super.removePainter(index);
	//		if(painter != null) {
	//			int childs_count = this.childrenCount;
	//			JVGComponent[] childs = this.children;
	//			for(int i = 0; i < childs_count; i++) {
	//				if(childs[i] instanceof JVGPath) {
	//					JVGPath child = (JVGPath)childs[i];
	//					child.removePainter(painter);
	//				}
	//			}
	//			invalidate();
	//		}
	//		return painter;
	//	}

	@Override
	public void remove(int index) {
		synchronized (getLock()) {
			if (index >= 0 && index < childrenCount) {
				JVGComponent c = children[index];
				if (c == leadSubPath) {
					return;
				}
			}
			super.remove(index);
		}
	}

	public JVGSubPath getLead() {
		return leadSubPath;
	}

	public PathPoint getClosestPoint(double x, double y) {
		Point2D closestPoint = null;
		double minDistance = Double.MAX_VALUE;
		double minPos = 0;

		PathIterator it = new FlatteningPathIterator(getPath().getPathIterator(null), 1);
		double points[] = new double[6];
		double moveX = 0, moveY = 0;
		double x1 = 0, y1 = 0;
		double x2 = 0, y2 = 0;
		double totalLength = 0;
		while (!it.isDone()) {
			int type = it.currentSegment(points);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = x1 = points[0];
					moveY = y1 = points[1];
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					x2 = points[0];
					y2 = points[1];

					double dx21 = x2 - x1;
					double dy21 = y2 - y1;
					double dx1 = x1 - x;
					double dy1 = y1 - y;
					double r_sq = dx21 * dx21 + dy21 * dy21;
					double alfa = -(dx21 * dx1 + dy21 * dy1) / r_sq;
					double r = Math.sqrt(r_sq);

					double x_;
					double y_;
					double dist;
					double pos;
					if (alfa <= 0) {
						x_ = x1;
						y_ = y1;
						dist = Math.sqrt(dx1 * dx1 + dy1 * dy1);
						pos = totalLength;
					} else if (alfa >= 1) {
						x_ = x2;
						y_ = y2;
						double dx2 = x2 - x;
						double dy2 = y2 - y;
						dist = Math.sqrt(dx2 * dx2 + dy2 * dy2);
						pos = totalLength + r;
					} else {
						x_ = x1 + alfa * dx21;
						y_ = y1 + alfa * dy21;
						double dx_ = x_ - x;
						double dy_ = y_ - y;
						dist = Math.sqrt(dx_ * dx_ + dy_ * dy_);
						pos = totalLength + alfa * r;
					}

					if (dist < minDistance) {
						minDistance = dist;
						if (closestPoint == null) {
							closestPoint = new Point2D.Double();
						}
						closestPoint.setLocation(x_, y_);
						minPos = pos;
					}

					totalLength += r;
					x1 = x2;
					y1 = y2;
					break;
			}
			it.next();
		}

		if (closestPoint != null) {
			PathPoint p = new PathPoint();
			p.point = closestPoint;
			p.dist = minDistance;
			p.percentPos = minPos / length;
			p.pos = minPos;
			return p;
		}
		return null;
	}

	public int getEditMode() {
		return editMode;
	}

	public void setEditMode(int editMode) {
		if (this.editMode != editMode) {
			this.editMode = editMode;
			invalidate();
		}
	}

	public static class PathPoint {
		public Point2D point;

		public double dist;

		public double percentPos;

		public double pos;
	}

	@Override
	public void paint(Graphics2D g) {
		super.paint(g);
	}

	protected void setPaintOrderType(int paintOrderType) {
		this.paintOrderType = paintOrderType;
	}
}
