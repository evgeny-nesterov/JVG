package ru.nest.jvg;

import ru.nest.hiscript.pol.model.ExecuteException;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.RuntimeContext;
import ru.nest.hiscript.pol.model.Types;
import ru.nest.hiscript.pol.model.Variable;
import ru.nest.hiscript.pol.support.GraphicsSupport;
import ru.nest.hiscript.pol.support.SwingSupport;
import ru.nest.hiscript.tokenizer.WordType;
import ru.nest.jvg.action.GroupAction;
import ru.nest.jvg.action.PathOperationAction;
import ru.nest.jvg.action.ToPathAction;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.event.JVGComponentEvent;
import ru.nest.jvg.event.JVGKeyEvent;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGMouseWheelEvent;
import ru.nest.jvg.event.JVGPeerEvent;
import ru.nest.jvg.event.JVGPropertyChangeEvent;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.parser.DocumentFormat;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.ScriptResource;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.OutlinePainter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ===============================================
// JVG methods:
// ===============================================
// void invalidate(long id)
// void repaint()
// long getParent(long id)
// long getRoot()
// long[] getComponents()
// int getChildrenCount(long id)
// long getChild(long id, int index)
// long getChild(long id, string name)
// long getComponent(double x, double y)
// long getDeepestComponent(double x, double y)
// void setComponentIndex(long id, int index)
// void remove(long id)
// void remove(ling[] ids)
// void removeAll(long id)
// string getSource(long[] ids)
// string getSource(long id)

// void setProperty(long id, string key, string value)
// string getProperty(long id, string key)
// boolean hasProperty(long id, string key)
// void removeProperty(long id, string key)

// void setProperty(long id, string type, string key, string value)
// string getProperty(long id, string type, string key)
// boolean hasProperty(long id, string type, string key)
// void removeProperty(long id, string type, string key)

// void setProperty(string key, string value)
// string getProperty(string key)
// boolean hasProperty(string key)
// void removeProperty(string key)

// void setFillColor(long id, int color)
// int getFillColor()
// void setOutlineColor(long id, int color)
// int getOutlineColor()
// void setStroke(float width, int cap, int join, float miterlimit, float dash[], float dash_phase)
// float getStrokeWidth()
// int getStrokeCap()
// int getStrokeJoin()
// float getStrokeMiterLimit()
// float[] getStrokeDash()
// float getStrokeDashPhase()
// double[] toInitial(long id, double x, double y, double dx, double dy, double directionX, double directionY)
// void transform(long id, double[] matrix)
// void scale(long id, scaleX, scaleY)
// void translate(long id, double tx, double ty)
// void shear(long id, double sx, double sy)
// void rotate(long id, double angle)
// void rotate(long id, double angle, double centerX, double centerY)
// double getX(long id)
// double getY(long id)
// double getWidth(long id)
// double getHeight(long id)
// double getInitialX(long id)
// double getInitialY(long id)
// double getInitialWidth(long id)
// double getInitialHeight(long id)
// long toPath(long id) - return id of a new shape
// void intersect(long focusID, long[] ids)
// void subtract(long focusID, long[] ids)
// void union(long focusID, long[] ids)
// void xor(long focusID, long[] ids)

// int getPathCoordCount(long id)
// int getPathCurvesCount(long id)
// float[] getPathCoords(long id)
// float getPathCoord(long id, int index)
// float getInitialPathCoord(long id, int index)
// int[] getPathCurveTypes(long id)
// void setPathCoord(long id, int index, float coordValue)
// void setPathCoords(long id, int[] indexes, float[] coordValues)
// void movePathCoords(long id, int[] indexes, float delta)
// void setPathPoint(long id, int index, float x, float y)
// void movePathPoints(long id, int[] indexes, double dx, double dy)
// void closePath(long id)
// void moveTo(long id, float x, float y)
// void lineTo(long id, float x, float y)
// void quadTo(long id, float cx, float cy, float x, float y)
// void curveTo(long id, float cx1, float cy1, float cx2, float cy2, float x, float y)
// void setToolTipText(string tooltip)

// string getToolTipText()
// boolean isVisible(long id)
// void setVisible(long id, boolean isVisible)
// void toFront(long id)
// void toBack(long id)
// void toUp(long id)
// void toDown(long id)
// boolean isClipped(long id)
// void setClipped(long id, boolean isClipped)
// boolean isFocused(long id)
// void requestFocus(long id)
// void repaint(long id)
// string getName(long id)
// boolean contains(long id, double x, double y)
// boolean isAntialias(long id)
// void setAntialias(long id, boolean isAntialias)
// boolean isFill(long id)
// void setFill(long id, boolean isFill)
// boolean isFocusable(long id)
// void setFocusable(long id, boolean isFocusable)
// void setCursor(long id, string type)
// int getCursor(long id)
// long group(long[] ids)
// long[] ungroup(long groupID)

public class JVGScriptSupport {
	public final static String NAMESPACE = "jvg";

	private JVGPane pane;

	private Map<String, String> properties = new HashMap<>();

	private SwingSupport swingSupport;

	private GraphicsSupport graphicsSupport;

	private RuntimeContext ctx;

	{
		try {
			ctx = new RuntimeContext();
		} catch (ExecuteException exc) {
			exc.printStackTrace();
		}
	}

	public JVGScriptSupport(JVGPane pane) {
		this.pane = pane;
		try {
			init();
			swingSupport = new SwingSupport(pane);
			swingSupport.export(ctx);

			graphicsSupport = new GraphicsSupport();
			graphicsSupport.export(ctx);
		} catch (ExecuteException exc) {
			exc.printStackTrace();
		}
	}

	public void addMethod(ru.nest.hiscript.pol.model.Method method) throws ExecuteException {
		if (method != null) {
			ctx.addMethod(method);
		}
	}

	private void init() throws ExecuteException {
		// Source
		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getSource", new WordType[] {WordType.LONG}, new int[] {0}, WordType.STRING) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					JVGBuilder build = JVGBuilder.create(DocumentFormat.jvg);
					try {
						ctx.value.string = build.build(new JVGComponent[] {component}, "UTF8");
						return;
					} catch (JVGParseException exc) {
					}
				}
				ctx.value.string = "";
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getSource", new WordType[] {WordType.LONG}, new int[] {1}, WordType.STRING) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape[] shapes = getShapes(arguments);
				JVGBuilder build = JVGBuilder.create(DocumentFormat.jvg);
				try {
					ctx.value.string = build.build(shapes, "UTF8");
					return;
				} catch (JVGParseException exc) {
				}
				ctx.value.string = "";
			}
		});

		// Structure
		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getParent", new WordType[] {WordType.LONG}, new int[] {0}, WordType.LONG) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					JVGContainer parent = component.getParent();
					if (parent != null && parent.getId() != null) {
						ctx.value.longNumber = parent.getId();
						return;
					}
				}
				// no parent
				ctx.value.longNumber = Long.MIN_VALUE;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "invalidate", new WordType[] {WordType.LONG}, new int[] {0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					component.invalidate();
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "repaint", new WordType[] {}, new int[] {}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				pane.repaint();
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getRoot", new WordType[] {}, new int[] {}, WordType.LONG) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				ctx.value.longNumber = pane.getRoot().getId();
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getComponents", new WordType[] {}, new int[] {}, WordType.LONG, 1) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				long[] ids = pane.getAllComponents();
				ctx.value.array = ids;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getChildrenCount", new WordType[] {WordType.LONG}, new int[] {0}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGContainer) {
					JVGContainer container = (JVGContainer) component;
					ctx.value.intNumber = container.getChildCount();
				} else {
					ctx.value.intNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getChild", new WordType[] {WordType.LONG, WordType.INT}, new int[] {0, 0}, WordType.LONG) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGContainer) {
					int index = (Integer) arguments[1];
					JVGContainer container = (JVGContainer) component;
					if (index >= 0 && index < container.getChildCount()) {
						JVGComponent child = container.getChild(index);
						ctx.value.longNumber = child.getId();
						return;
					}
				}
				ctx.value.longNumber = Long.MIN_VALUE;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getChild", new WordType[] {WordType.LONG, WordType.STRING}, new int[] {0, 0}, WordType.LONG) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGContainer) {
					String name = (String) arguments[1];
					JVGContainer container = (JVGContainer) component;
					if (name != null && name.length() > 0) {
						JVGComponent child = container.getChild(name);
						ctx.value.longNumber = child.getId();
						return;
					}
				}
				ctx.value.longNumber = Long.MIN_VALUE;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getComponent", new WordType[] {WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0}, WordType.LONG) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				double x = (Double) arguments[0];
				double y = (Double) arguments[1];
				JVGComponent component = pane.getRoot().getComponent(x, y);
				if (component != null) {
					ctx.value.longNumber = component.getId();
				} else {
					ctx.value.longNumber = Long.MIN_VALUE;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getDeepestComponent", new WordType[] {WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0}, WordType.LONG) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				double x = (Double) arguments[0];
				double y = (Double) arguments[1];
				JVGComponent component = pane.getRoot().getDeepestComponent(x, y);
				if (component != null) {
					ctx.value.longNumber = component.getId();
				} else {
					ctx.value.longNumber = Long.MIN_VALUE;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setComponentIndex", new WordType[] {WordType.LONG, WordType.INT}, new int[] {0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent child = getComponent(arguments);
				if (child != null) {
					JVGContainer parent = child.getParent();
					if (parent != null) {
						int index = (Integer) arguments[1];
						if (index >= 0 && index < parent.getChildCount()) {
							parent.setComponentIndex(child, index);
						}
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "remove", new WordType[] {WordType.LONG}, new int[] {0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					JVGContainer parent = component.getParent();
					if (parent != null) {
						parent.remove(component);
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "remove", new WordType[] {WordType.LONG}, new int[] {1}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				long[] ids = (long[]) arguments[0];
				for (int i = 0; i < ids.length; i++) {
					JVGComponent component = pane.getComponent(ids[i]);
					JVGContainer parent = component.getParent();
					if (parent != null) {
						parent.remove(component);
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setProperty", new WordType[] {WordType.LONG, WordType.STRING, WordType.STRING}, new int[] {0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					String parameter = (String) arguments[1];
					String value = (String) arguments[2];
					Map<String, String> hash = (Map<String, String>) component.getClientProperty("component-properties");
					if (hash == null) {
						hash = new HashMap<>();
						component.setClientProperty("component-properties", hash);
					}
					hash.put(parameter, value);
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getProperty", new WordType[] {WordType.LONG, WordType.STRING}, new int[] {0, 0}, WordType.STRING) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					String parameter = (String) arguments[1];
					Map<String, String> hash = (Map<String, String>) component.getClientProperty("component-properties");
					if (hash != null) {
						String value = hash.get(parameter);
						if (value != null) {
							ctx.value.string = value;
							return;
						}
					}
				}
				ctx.value.string = "";
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "hasProperty", new WordType[] {WordType.LONG, WordType.STRING}, new int[] {0, 0}, WordType.BOOLEAN) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					String parameter = (String) arguments[1];
					Map<String, String> hash = (Map<String, String>) component.getClientProperty("component-properties");
					if (hash != null) {
						ctx.value.bool = hash.containsKey(parameter);
						return;
					}
				}
				ctx.value.bool = false;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "removeProperty", new WordType[] {WordType.LONG, WordType.STRING}, new int[] {0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					String parameter = (String) arguments[1];
					Map<String, String> hash = (Map<String, String>) component.getClientProperty("component-properties");
					if (hash != null) {
						hash.remove(parameter);
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setProperty", new WordType[] {WordType.LONG, WordType.STRING, WordType.STRING, WordType.STRING}, new int[] {0, 0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					String type = (String) arguments[1];
					String parameter = (String) arguments[2];
					String value = (String) arguments[3];
					component.setClientProperty(type, parameter, value);
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getProperty", new WordType[] {WordType.LONG, WordType.STRING, WordType.STRING}, new int[] {0, 0, 0}, WordType.STRING) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					String type = (String) arguments[1];
					String parameter = (String) arguments[2];
					String value = component.getClientProperty(type, parameter);
					if (value != null) {
						ctx.value.string = value;
						return;
					}
				}
				ctx.value.string = "";
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "hasProperty", new WordType[] {WordType.LONG, WordType.STRING, WordType.STRING}, new int[] {0, 0, 0}, WordType.BOOLEAN) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					String type = (String) arguments[1];
					String parameter = (String) arguments[2];
					String value = component.getClientProperty(type, parameter);
					ctx.value.bool = value != null;
					return;
				}
				ctx.value.bool = false;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "removeProperty", new WordType[] {WordType.LONG, WordType.STRING, WordType.STRING}, new int[] {0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					String type = (String) arguments[1];
					String parameter = (String) arguments[2];
					component.setClientProperty(type, parameter, null);
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setProperty", new WordType[] {WordType.STRING, WordType.STRING}, new int[] {0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				String parameter = (String) arguments[0];
				String value = (String) arguments[1];
				properties.put(parameter, value);
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getProperty", new WordType[] {WordType.STRING}, new int[] {0}, WordType.STRING) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				String parameter = (String) arguments[0];
				String value = properties.get(parameter);
				ctx.value.string = value != null ? value : "";
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "hasProperty", new WordType[] {WordType.STRING}, new int[] {0}, WordType.BOOLEAN) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				String parameter = (String) arguments[0];
				ctx.value.bool = properties.containsKey(parameter);
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "removeProperty", new WordType[] {WordType.STRING}, new int[] {0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				String parameter = (String) arguments[0];
				properties.remove(parameter);
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setFillColor", new WordType[] {WordType.LONG, WordType.INT}, new int[] {0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					int color = (Integer) arguments[1];
					FillPainter filler = shape.getPainter(FillPainter.class);
					if (filler != null) {
						filler.setPaint(new ColorDraw(new Color(color, true)));
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getFillColor", new WordType[] {WordType.LONG}, new int[] {0}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					FillPainter filler = shape.getPainter(FillPainter.class);
					if (filler != null) {
						Draw draw = filler.getPaint();
						if (draw instanceof ColorDraw) {
							ColorDraw colorDraw = (ColorDraw) draw;
							Color color = colorDraw.getResource().getResource();
							ctx.value.intNumber = color.getRGB();
							return;
						}
					}
				}
				ctx.value.intNumber = -1;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setOutlineColor", new WordType[] {WordType.LONG, WordType.INT}, new int[] {0, 0, 0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					int color = (Integer) arguments[1];
					OutlinePainter outline = shape.getPainter(OutlinePainter.class);
					if (outline != null) {
						outline.setPaint(new ColorDraw(new Color(color, true)));
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getOutlineColor", new WordType[] {WordType.LONG}, new int[] {0}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					OutlinePainter outline = shape.getPainter(OutlinePainter.class);
					if (outline != null) {
						Draw draw = outline.getPaint();
						if (draw instanceof ColorDraw) {
							ColorDraw colorDraw = (ColorDraw) draw;
							Color color = colorDraw.getResource().getResource();
							ctx.value.intNumber = color.getRGB();
							return;
						}
					}
				}
				ctx.value.intNumber = -1;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setStroke", new WordType[] {WordType.LONG, WordType.FLOAT, WordType.INT, WordType.INT, WordType.FLOAT, WordType.FLOAT, WordType.FLOAT}, new int[] {0, 0, 0, 0, 0, 1, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					OutlinePainter outline = shape.getPainter(OutlinePainter.class);
					if (outline != null) {
						float width = (Float) arguments[1];
						int cap = (Integer) arguments[2];
						int join = (Integer) arguments[3];
						float miterlimit = (Float) arguments[4];
						float[] dash = (float[]) arguments[5];
						float dash_phase = (Float) arguments[6];
						outline.setStroke(width, cap, join, miterlimit, dash, dash_phase);
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getStrokeWidth", new WordType[] {WordType.LONG}, new int[] {0}, WordType.FLOAT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					OutlinePainter outline = shape.getPainter(OutlinePainter.class);
					if (outline != null) {
						BasicStroke stroke = (BasicStroke) outline.getStroke().getResource();
						ctx.value.floatNumber = stroke.getLineWidth();
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getStrokeCap", new WordType[] {WordType.LONG}, new int[] {0}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					OutlinePainter outline = shape.getPainter(OutlinePainter.class);
					if (outline != null) {
						BasicStroke stroke = (BasicStroke) outline.getStroke().getResource();
						ctx.value.intNumber = stroke.getEndCap();
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getStrokeJoin", new WordType[] {WordType.LONG}, new int[] {0}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					OutlinePainter outline = shape.getPainter(OutlinePainter.class);
					if (outline != null) {
						BasicStroke stroke = (BasicStroke) outline.getStroke().getResource();
						ctx.value.intNumber = stroke.getLineJoin();
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getStrokeMiterLimit", new WordType[] {WordType.LONG}, new int[] {0}, WordType.FLOAT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					OutlinePainter outline = shape.getPainter(OutlinePainter.class);
					if (outline != null) {
						BasicStroke stroke = (BasicStroke) outline.getStroke().getResource();
						ctx.value.floatNumber = stroke.getMiterLimit();
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getStrokeDash", new WordType[] {WordType.LONG}, new int[] {0}, WordType.FLOAT, 1) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					OutlinePainter outline = shape.getPainter(OutlinePainter.class);
					if (outline != null) {
						BasicStroke stroke = (BasicStroke) outline.getStroke().getResource();
						ctx.value.array = stroke.getDashArray();
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getStrokeDashPhase", new WordType[] {WordType.LONG}, new int[] {0}, WordType.FLOAT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					OutlinePainter outline = shape.getPainter(OutlinePainter.class);
					if (outline != null) {
						BasicStroke stroke = (BasicStroke) outline.getStroke().getResource();
						ctx.value.floatNumber = stroke.getDashPhase();
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "transform", new WordType[] {WordType.LONG, WordType.DOUBLE}, new int[] {0, 1}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				double[] matrix = (double[]) arguments[1];
				if (matrix.length == 6) {
					JVGShape shape = (JVGShape) getComponent(arguments);
					if (shape != null) {
						shape.transform(new AffineTransform(matrix));
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "transform", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0, 0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				double[] matrix = {(Double) arguments[1], (Double) arguments[2], (Double) arguments[3], (Double) arguments[4], (Double) arguments[5], (Double) arguments[6]};
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					shape.transform(new AffineTransform(matrix));
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "scale", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					double scaleX = (Double) arguments[1];
					double scaleY = (Double) arguments[2];
					shape.transform(AffineTransform.getScaleInstance(scaleX, scaleY));
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "translate", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					double translateX = (Double) arguments[1];
					double translateY = (Double) arguments[2];
					shape.transform(AffineTransform.getTranslateInstance(translateX, translateY));
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "shear", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					double shearX = (Double) arguments[1];
					double shearY = (Double) arguments[2];
					shape.transform(AffineTransform.getShearInstance(shearX, shearY));
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "rotate", new WordType[] {WordType.LONG, WordType.DOUBLE}, new int[] {0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					double angle = (Double) arguments[1];
					shape.transform(AffineTransform.getRotateInstance(angle));
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "rotate", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					double angle = (Double) arguments[1];
					double centerX = (Double) arguments[2];
					double centerY = (Double) arguments[3];
					shape.transform(AffineTransform.getRotateInstance(angle, centerX, centerY));
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getX", new WordType[] {WordType.LONG}, new int[] {0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent shape = getComponent(arguments);
				if (shape != null) {
					ctx.value.doubleNumber = shape.getRectangleBounds().getX();
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getY", new WordType[] {WordType.LONG}, new int[] {0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent shape = getComponent(arguments);
				if (shape != null) {
					ctx.value.doubleNumber = shape.getRectangleBounds().getY();
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getWidth", new WordType[] {WordType.LONG}, new int[] {0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent shape = getComponent(arguments);
				if (shape != null) {
					ctx.value.doubleNumber = shape.getRectangleBounds().getWidth();
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getHeight", new WordType[] {WordType.LONG}, new int[] {0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent shape = getComponent(arguments);
				if (shape != null) {
					ctx.value.doubleNumber = shape.getRectangleBounds().getHeight();
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getInitialX", new WordType[] {WordType.LONG}, new int[] {0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					ctx.value.doubleNumber = shape.getInitialBounds().getBounds().getX();
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getInitialY", new WordType[] {WordType.LONG}, new int[] {0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					ctx.value.doubleNumber = shape.getInitialBounds().getBounds().getY();
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getInitialWidth", new WordType[] {WordType.LONG}, new int[] {0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					ctx.value.doubleNumber = shape.getInitialBounds().getBounds().getWidth();
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getInitialHeight", new WordType[] {WordType.LONG}, new int[] {0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					ctx.value.doubleNumber = shape.getInitialBounds().getBounds().getHeight();
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		// Path
		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "toPath", new WordType[] {WordType.LONG}, new int[] {0}, WordType.LONG) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					JVGPath path = ToPathAction.convertToPath(shape, false);
					if (path != null) {
						ctx.value.longNumber = path.getId();
					} else {
						ctx.value.longNumber = Long.MIN_VALUE;
					}
				} else {
					ctx.value.longNumber = 0;
				}
			}
		});

		addPathOperationMethod(PathOperationAction.INTERSECTION, "intersect");
		addPathOperationMethod(PathOperationAction.SUBTRACTION, "subtract");
		addPathOperationMethod(PathOperationAction.UNION, "union");
		addPathOperationMethod(PathOperationAction.XOR, "xor");

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getPathCoordCount", new WordType[] {WordType.LONG}, new int[] {0}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				MutableGeneralPath path = shape.getTransformedShape();
				ctx.value.intNumber = path.numCoords;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getPathCurvesCount", new WordType[] {WordType.LONG}, new int[] {0}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					MutableGeneralPath path = shape.getTransformedShape();
					ctx.value.intNumber = path.numTypes;
				} else {
					ctx.value.intNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getPathCoords", new WordType[] {WordType.LONG}, new int[] {0}, WordType.DOUBLE, 1) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					MutableGeneralPath path = shape.getTransformedShape();
					ctx.value.array = path.pointCoords;
				} else {
					ctx.value.array = null;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getPathCoord", new WordType[] {WordType.LONG, WordType.INT}, new int[] {0, 0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					int index = (Integer) arguments[1];
					MutableGeneralPath path = shape.getTransformedShape();
					ctx.value.doubleNumber = path.pointCoords[index];
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getInitialPathCoord", new WordType[] {WordType.LONG, WordType.INT}, new int[] {0, 0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					int index = (Integer) arguments[1];
					ctx.value.doubleNumber = shape.getCoord(index);
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getPathCurveTypes", new WordType[] {WordType.LONG}, new int[] {0}, WordType.INT, 1) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGShape shape = (JVGShape) getComponent(arguments);
				if (shape != null) {
					MutableGeneralPath path = shape.getTransformedShape();
					ctx.value.array = path.pointTypes;
				} else {
					ctx.value.array = null;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setPathCoord", new WordType[] {WordType.LONG, WordType.INT, WordType.DOUBLE}, new int[] {0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					int index = (Integer) arguments[1];
					double coord = (Double) arguments[2];
					shape.setCoord(index, coord);
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setPathCoords", new WordType[] {WordType.LONG, WordType.INT, WordType.DOUBLE}, new int[] {0, 1, 1}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					int[] indexes = (int[]) arguments[1];
					double[] coords = (double[]) arguments[2];
					int count = Math.min(indexes.length, coords.length);
					int numcoords = shape.getCoordsCount();
					for (int i = 0; i < count; i++) {
						if (indexes[i] >= 0 && indexes[i] < numcoords) {
							shape.setCoord(indexes[i], coords[i]);
						}
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "movePathCoords", new WordType[] {WordType.LONG, WordType.INT, WordType.DOUBLE}, new int[] {0, 1, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					int[] indexes = (int[]) arguments[1];
					double delta = (Double) arguments[2];
					int numcoords = shape.getCoordsCount();
					for (int i = 0; i < indexes.length; i++) {
						if (indexes[i] >= 0 && indexes[i] < numcoords) {
							double coord = shape.getCoord(indexes[i]);
							shape.setCoord(indexes[i], (coord + delta));
						}
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setPathPoint", new WordType[] {WordType.LONG, WordType.INT, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					int index = (Integer) arguments[1];
					double x = (Double) arguments[2];
					double y = (Double) arguments[3];
					shape.setPoint(index, x, y);
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "movePathPoints", new WordType[] {WordType.LONG, WordType.INT, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 1, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					int[] indexes = (int[]) arguments[1];
					double dx = (Double) arguments[2];
					double dy = (Double) arguments[3];
					int numPoints = shape.getCoordsCount() / 2;
					for (int i = 0; i < indexes.length; i++) {
						if (indexes[i] >= 0 && indexes[i] < numPoints) {
							double px = shape.getCoord(2 * indexes[i]);
							double py = shape.getCoord(2 * indexes[i] + 1);
							shape.setPoint(2 * indexes[i], (px + dx), (py + dy));
						}
					}
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "toInitial", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0, 0, 0, 0, 0}, WordType.DOUBLE, 1) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);

				double x = (Double) arguments[1];
				double y = (Double) arguments[2];
				double dx = (Double) arguments[3];
				double dy = (Double) arguments[4];
				double directionX = (Double) arguments[5];
				double directionY = (Double) arguments[6];

				double koef = (dx * directionX + dy * directionY) / (directionX * directionX + directionY * directionY);
				dx = koef * directionX;
				dy = koef * directionY;

				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					double[] point = new double[] {x, y, x + dx, y + dy};
					shape.getInverseTransform().transform(point, 0, point, 0, 2);
					dx = point[2] - point[0];
					dy = point[3] - point[1];
				}
				ctx.value.array = new double[] {dx, dy};
			}
		});
		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "proect", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0, 0, 0, 0, 0}, WordType.DOUBLE) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					double x = (Double) arguments[1];
					double y = (Double) arguments[2];
					double dx = (Double) arguments[3];
					double dy = (Double) arguments[4];
					double directionX = (Double) arguments[5];
					double directionY = (Double) arguments[6];

					JVGPath shape = (JVGPath) component;
					double[] point = new double[] {x, y, x + dx, y + dy, x + directionX, y + directionY};
					shape.getInverseTransform().transform(point, 0, point, 0, 3);
					x = point[0];
					y = point[1];
					dx = point[2] - point[0];
					dy = point[3] - point[1];
					directionX = point[4] - point[0];
					directionY = point[5] - point[1];

					ctx.value.doubleNumber = (dx * directionX + dy * directionY) / Math.sqrt(directionX * directionX + directionY * directionY);
				} else {
					ctx.value.doubleNumber = 0;
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "closePath", new WordType[] {WordType.LONG}, new int[] {0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					shape.closePath();
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "moveTo", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					double x = (Double) arguments[1];
					double y = (Double) arguments[2];
					shape.moveTo(x, y);
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "lineTo", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					double x = (Double) arguments[1];
					double y = (Double) arguments[2];
					shape.lineTo(x, y);
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "quadTo", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					double cx = (Double) arguments[1];
					double cy = (Double) arguments[2];
					double x = (Double) arguments[3];
					double y = (Double) arguments[4];
					shape.quadTo(cx, cy, x, y);
				}
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "curveTo", new WordType[] {WordType.LONG, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE, WordType.DOUBLE}, new int[] {0, 0, 0, 0, 0, 0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGPath) {
					JVGPath shape = (JVGPath) component;
					double cx1 = (Double) arguments[1];
					double cy1 = (Double) arguments[2];
					double cx2 = (Double) arguments[3];
					double cy2 = (Double) arguments[4];
					double x = (Double) arguments[5];
					double y = (Double) arguments[6];
					shape.curveTo(cx1, cy1, cx2, cy2, x, y);
				}
			}
		});

		// Component
		addMethod("getToolTipText", new Class[] {}, WordType.STRING);
		addMethod("setToolTipText", new Class[] {String.class}, WordType.VOID);
		addMethod("isVisible", new Class[] {}, WordType.BOOLEAN);
		addMethod("setVisible", new Class[] {boolean.class}, WordType.VOID);
		addMethod("toFront", new Class[] {}, WordType.VOID);
		addMethod("toBack", new Class[] {}, WordType.VOID);
		addMethod("toUp", new Class[] {}, WordType.VOID);
		addMethod("toDown", new Class[] {}, WordType.VOID);
		addMethod("isClipped", new Class[] {}, WordType.BOOLEAN);
		addMethod("setClipped", new Class[] {boolean.class}, WordType.VOID);
		addMethod("isFocused", new Class[] {}, WordType.BOOLEAN);
		addMethod("requestFocus", new Class[] {}, WordType.VOID);
		addMethod("repaint", new Class[] {}, WordType.VOID);
		addMethod("getName", new Class[] {}, WordType.STRING);
		addMethod("setName", new Class[] {String.class}, WordType.VOID);
		addMethod("contains", new Class[] {double.class, double.class}, WordType.BOOLEAN);
		addMethod("isAntialias", new Class[] {}, WordType.BOOLEAN);
		addMethod("setAntialias", new Class[] {boolean.class}, WordType.VOID);
		addMethod("isFill", new Class[] {}, WordType.BOOLEAN);
		addMethod("setFill", new Class[] {boolean.class}, WordType.VOID);
		addMethod("isFocusable", new Class[] {}, WordType.BOOLEAN);
		addMethod("setFocusable", new Class[] {boolean.class}, WordType.VOID);
		addMethod("removeAll", new Class[] {}, WordType.VOID);
		addMethod("setSelected", new Class[] {boolean.class, boolean.class}, WordType.VOID);
		addMethod("isSelectable", new Class[] {}, WordType.BOOLEAN);
		addMethod("setSelectable", new Class[] {boolean.class}, WordType.VOID);
		addMethod("setID", new Class[] {long.class}, WordType.VOID);
		addMethod("getID", new Class[] {}, WordType.LONG);

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getCursor", new WordType[] {WordType.LONG}, new int[] {0}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					Cursor cursor = component.getCursor();
					if (cursor != null) {
						ctx.value.intNumber = cursor.getType();
						return;
					}
				}
				ctx.value.intNumber = Cursor.DEFAULT_CURSOR;
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "setCursor", new WordType[] {WordType.LONG, WordType.INT}, new int[] {0, 0}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component != null) {
					int type = (Integer) arguments[1];
					if (type >= Cursor.DEFAULT_CURSOR && type <= Cursor.MOVE_CURSOR) {
						Cursor cursor = Cursor.getPredefinedCursor(type);
						component.setCursor(cursor);
					}
				}
			}
		});

		// pane methods
		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getPaneX", new WordType[] {}, new int[] {}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				ctx.value.intNumber = pane.getX();
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getPaneY", new WordType[] {}, new int[] {}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				ctx.value.intNumber = pane.getY();
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getPaneWidth", new WordType[] {}, new int[] {}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				ctx.value.intNumber = pane.getWidth();
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getPaneHeight", new WordType[] {}, new int[] {}, WordType.INT) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				ctx.value.intNumber = pane.getHeight();
			}
		});

		// Group
		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "group", new WordType[] {WordType.LONG}, new int[] {1}, WordType.LONG) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent[] shapes = getShapes(arguments);
				JVGGroup group = GroupAction.group(pane, shapes);
				ctx.value.longNumber = group.getId();
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "ungroup", new WordType[] {WordType.LONG}, new int[] {0}, WordType.LONG, 1) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent component = getComponent(arguments);
				if (component instanceof JVGGroup) {
					JVGGroup group = (JVGGroup) component;
					JVGComponent[] components = GroupAction.ungroup(pane, group);
					ctx.value.array = getComponents(components);
				}
			}
		});

		// Selection
		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "clearSelection", new WordType[] {}, new int[] {}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				pane.getSelectionManager().clearSelection();
			}
		});

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, "getSelection", new WordType[] {}, new int[] {}, WordType.LONG, 1) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				JVGComponent[] selection = pane.getSelectionManager().getSelection();
				ctx.value.array = getComponents(selection);
			}
		});
	}

	private JVGComponent getComponent(Object... arguments) {
		long id = (Long) arguments[0];
		JVGComponent component = pane.getComponent(id);
		return component;
	}

	private JVGShape[] getShapes(Object... arguments) {
		long[] ids = (long[]) arguments[0];
		JVGShape[] shapes = getShapes(pane, ids);
		return shapes;
	}

	private void addPathOperationMethod(final int type, String name) throws ExecuteException {
		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, name, new WordType[] {WordType.LONG, WordType.LONG}, new int[] {0, 1}, WordType.VOID) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				long focusID = (Long) arguments[0];
				long[] ids = (long[]) arguments[1];
				if (ids.length > 0) {
					JVGShape[] shapes = getShapes(pane, ids);
					PathOperationAction.doOperation(pane, pane.getComponent(focusID), type, shapes);
				}
			}
		});
	}

	private void addMethod(String name, final Class<?>[] types, WordType returnType) throws ExecuteException {
		int argumentsCount = types.length + 1;
		WordType[] argsTypes = new WordType[argumentsCount];
		int[] argsDimensions = new int[argumentsCount];
		argsTypes[0] = WordType.LONG;
		argsDimensions[0] = 0;
		for (int i = 1; i < argumentsCount; i++) {
			argsTypes[i] = Types.getType(types[i - 1]);
			argsDimensions[i] = Types.getDimension(types[i - 1]);
		}

		ctx.addMethod(new ru.nest.hiscript.pol.model.Method(NAMESPACE, name, argsTypes, argsDimensions, returnType) {
			@Override
			public void invoke(RuntimeContext ctx, Node node, Object... arguments) throws ExecuteException {
				super.invoke(ctx, node, arguments);
				Long id = (Long) arguments[0];
				if (id != null) {
					JVGComponent component = pane.getComponent(id);
					try {
						java.lang.reflect.Method method = component.getClass().getMethod(getName(), types);

						Object[] arg = new Object[arguments.length - 1];
						for (int i = 0; i < arg.length; i++) {
							arg[i] = arguments[i + 1];
						}

						Object returnedValue = method.invoke(component, arg);
						ctx.value.setValue(returnedValue, getReturnType());
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		});
	}

	private String getScript(Script.Type scriptType, JVGComponent c) {
		String s = null;
		if (scriptType != null) {
			ScriptResource script = (ScriptResource) c.getClientProperty(scriptType.getActionName());
			s = getScript(script);
		}
		return s;
	}

	private String getScript(ScriptResource script) {
		String s = null;
		while (script != null) {
			if (s == null) {
				s = script.getResource().getData();
			} else {
				s += '\n' + script.getResource().getData();
			}
			script = script.getPostScript();
		}
		return s;
	}

	public void executeScript(Script.Type scriptType, JVGKeyEvent e) {
		String s = getScript(scriptType, e.getSource());
		if (s != null) {
			ru.nest.hiscript.pol.Compiler p = ru.nest.hiscript.pol.Compiler.getDefaultCompiler(s);
			try {
				Node node = p.build();
				if (node != null) {
					addConstants(node);

					node.addVariable(new Variable(NAMESPACE, "id", WordType.LONG, 0)).getValue().setValue(e.getSource().getId(), WordType.LONG);
					node.addVariable(new Variable(NAMESPACE, "keyChar", WordType.CHAR, 0)).getValue().setValue(e.getKeyChar(), WordType.CHAR);
					node.addVariable(new Variable(NAMESPACE, "keyCode", WordType.INT, 0)).getValue().setValue(e.getKeyCode(), WordType.INT);
					node.addVariable(new Variable(NAMESPACE, "modifiers", WordType.INT, 0)).getValue().setValue(e.getModifiers(), WordType.INT);
					node.addVariable(new Variable(NAMESPACE, "when", WordType.LONG, 0)).getValue().setValue(e.getWhen(), WordType.LONG);
					node.addVariable(new Variable(NAMESPACE, "isAltDown", WordType.BOOLEAN, 0)).getValue().setValue(e.isAltDown(), WordType.BOOLEAN);
					node.addVariable(new Variable(NAMESPACE, "isControlDown", WordType.BOOLEAN, 0)).getValue().setValue(e.isControlDown(), WordType.BOOLEAN);
					node.addVariable(new Variable(NAMESPACE, "isShiftDown", WordType.BOOLEAN, 0)).getValue().setValue(e.isShiftDown(), WordType.BOOLEAN);

					node.compile();
					node.execute(ctx);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public void executeScript(Script.Type scriptType, JVGComponentEvent e) {
		String s = getScript(scriptType, e.getSource());
		if (s != null) {
			ru.nest.hiscript.pol.Compiler p = ru.nest.hiscript.pol.Compiler.getDefaultCompiler(s);
			try {
				Node node = p.build();
				if (node != null) {
					addConstants(node);
					node.addVariable(new Variable(NAMESPACE, "id", WordType.LONG, 0)).getValue().setValue(e.getSource().getId(), WordType.LONG);
					node.compile();
					node.execute(ctx);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public void executeScript(Script.Type scriptType, JVGPeerEvent e) {
		String s = getScript(scriptType, e.getSource());
		if (s != null) {
			ru.nest.hiscript.pol.Compiler p = ru.nest.hiscript.pol.Compiler.getDefaultCompiler(s);
			try {
				Node node = p.build();
				if (node != null) {
					addConstants(node);
					node.addVariable(new Variable(NAMESPACE, "id", WordType.LONG, 0)).getValue().setValue(e.getSource().getId(), WordType.LONG);
					node.compile();
					node.execute(ctx);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public void executeScript(Script.Type scriptType, JVGPropertyChangeEvent e) {
		String s = getScript(scriptType, e.getSource());
		if (s != null) {
			ru.nest.hiscript.pol.Compiler p = ru.nest.hiscript.pol.Compiler.getDefaultCompiler(s);
			try {
				Node node = p.build();
				if (node != null) {
					addConstants(node);

					node.addVariable(new Variable(NAMESPACE, "id", WordType.LONG, 0)).getValue().setValue(e.getSource().getId(), WordType.LONG);
					node.addVariable(new Variable(NAMESPACE, "property", WordType.STRING, 0)).getValue().setValue(e.getPropertyName(), WordType.STRING);

					node.compile();
					node.execute(ctx);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public void executeScript(JVGComponent c, Graphics2D g) {
		String s = getScript(Script.PAINT, c);
		if (s != null) {
			ru.nest.hiscript.pol.Compiler p = ru.nest.hiscript.pol.Compiler.getDefaultCompiler(s);
			try {
				Node node = p.build();
				if (node != null) {
					graphicsSupport.setGraphics(g);
					node.addVariable(new Variable(NAMESPACE, "id", WordType.LONG, 0)).getValue().setValue(c.getId(), WordType.LONG);
					node.compile();
					node.execute(ctx);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	private Map<Long, double[]> lastPos = new HashMap<>();

	// TODO optimize - use object ScriptContext (String script, Node compilation)
	public void executeScript(Script.Type scriptType, JVGMouseEvent e) {
		String s = getScript(scriptType, e.getSource());
		long id = e.getSource().getId();
		double dx = 0, dy = 0;
		double adx = 0, ady = 0;
		double ix = e.getX(), iy = e.getY(), iax = e.getAdjustedX(), iay = e.getAdjustedY(), idx = 0, idy = 0, iadx = 0, iady = 0;
		if (e.getID() == JVGMouseEvent.MOUSE_DRAGGED || e.getID() == JVGMouseEvent.MOUSE_MOVED) {
			double[] pos = lastPos.get(id);
			if (pos != null) {
				dx = e.getX() - pos[0];
				dy = e.getY() - pos[1];
				adx = e.getAdjustedX() - pos[2];
				ady = e.getAdjustedY() - pos[3];

				if (e.getSource() instanceof JVGActionArea) {
					JVGShape shape = (JVGShape) e.getSource().getParent();
					double[] point = new double[] {ix, iy, ix + dx, iy + dy, ix + adx, iy + ady, iax, iay};
					shape.getInverseTransform().transform(point, 0, point, 0, 4);
					ix = point[0];
					iy = point[1];
					idx = point[2] - ix;
					idy = point[3] - iy;
					iadx = point[4] - ix;
					iady = point[5] - iy;
					iax = point[6];
					iay = point[7];
				}
			}
		}

		if (s != null) {
			ru.nest.hiscript.pol.Compiler p = ru.nest.hiscript.pol.Compiler.getDefaultCompiler(s);
			try {
				Node node = p.build();
				if (node != null) {
					addConstants(node);

					node.addVariable(new Variable(NAMESPACE, "id", WordType.LONG, 0)).getValue().setValue(id, WordType.LONG);
					node.addVariable(new Variable(NAMESPACE, "modifiers", WordType.INT, 0)).getValue().setValue(e.getModifiers(), WordType.INT);
					node.addVariable(new Variable(NAMESPACE, "clickCount", WordType.INT, 0)).getValue().setValue(e.getClickCount(), WordType.INT);
					node.addVariable(new Variable(NAMESPACE, "button", WordType.INT, 0)).getValue().setValue(e.getButton(), WordType.INT);
					node.addVariable(new Variable(NAMESPACE, "when", WordType.LONG, 0)).getValue().setValue(e.getWhen(), WordType.LONG);
					node.addVariable(new Variable(NAMESPACE, "isAltDown", WordType.BOOLEAN, 0)).getValue().setValue(e.isAltDown(), WordType.BOOLEAN);
					node.addVariable(new Variable(NAMESPACE, "isControlDown", WordType.BOOLEAN, 0)).getValue().setValue(e.isControlDown(), WordType.BOOLEAN);
					node.addVariable(new Variable(NAMESPACE, "isShiftDown", WordType.BOOLEAN, 0)).getValue().setValue(e.isShiftDown(), WordType.BOOLEAN);

					node.addVariable(new Variable(NAMESPACE, "x", WordType.DOUBLE, 0)).getValue().setValue(e.getX(), WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "y", WordType.DOUBLE, 0)).getValue().setValue(e.getY(), WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "adjustedx", WordType.DOUBLE, 0)).getValue().setValue(e.getAdjustedX(), WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "adjustedy", WordType.DOUBLE, 0)).getValue().setValue(e.getAdjustedY(), WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "dx", WordType.DOUBLE, 0)).getValue().setValue(dx, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "dy", WordType.DOUBLE, 0)).getValue().setValue(dy, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "adjusteddx", WordType.DOUBLE, 0)).getValue().setValue(adx, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "adjusteddy", WordType.DOUBLE, 0)).getValue().setValue(ady, WordType.DOUBLE);

					node.addVariable(new Variable(NAMESPACE, "ix", WordType.DOUBLE, 0)).getValue().setValue(ix, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "iy", WordType.DOUBLE, 0)).getValue().setValue(iy, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "iadjustedx", WordType.DOUBLE, 0)).getValue().setValue(iax, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "iadjustedy", WordType.DOUBLE, 0)).getValue().setValue(iay, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "idx", WordType.DOUBLE, 0)).getValue().setValue(idx, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "idy", WordType.DOUBLE, 0)).getValue().setValue(idy, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "iadjusteddx", WordType.DOUBLE, 0)).getValue().setValue(iadx, WordType.DOUBLE);
					node.addVariable(new Variable(NAMESPACE, "iadjusteddy", WordType.DOUBLE, 0)).getValue().setValue(iady, WordType.DOUBLE);

					if (e instanceof JVGMouseWheelEvent) {
						JVGMouseWheelEvent we = (JVGMouseWheelEvent) e;
						node.addVariable(new Variable(NAMESPACE, "scrollType", WordType.INT, 0)).getValue().setValue(we.getScrollType(), WordType.INT);
						node.addVariable(new Variable(NAMESPACE, "scrollAmount", WordType.INT, 0)).getValue().setValue(we.getScrollAmount(), WordType.INT);
						node.addVariable(new Variable(NAMESPACE, "wheelRotation", WordType.INT, 0)).getValue().setValue(we.getWheelRotation(), WordType.INT);
					}

					node.compile();
					node.execute(ctx);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}

		if (e.getID() == JVGMouseEvent.MOUSE_DRAGGED || e.getID() == JVGMouseEvent.MOUSE_MOVED || e.getID() == JVGMouseEvent.MOUSE_PRESSED) {
			double[] pos = lastPos.get(id);
			if (pos == null) {
				pos = new double[4];
				lastPos.put(id, pos);
			}
			pos[0] = e.getX();
			pos[1] = e.getY();
			pos[2] = e.getAdjustedX();
			pos[3] = e.getAdjustedY();
		}
	}

	public void executeScript(ScriptResource scriptResource) {
		String s = getScript(scriptResource);
		if (s != null) {
			ru.nest.hiscript.pol.Compiler p = ru.nest.hiscript.pol.Compiler.getDefaultCompiler(s);
			try {
				Node node = p.build();
				if (node != null) {
					addConstants(node);
					node.compile();
					node.execute(ctx);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public void executeScript(ScriptResource scriptResource, Long id) {
		String s = getScript(scriptResource);
		if (s != null) {
			ru.nest.hiscript.pol.Compiler p = ru.nest.hiscript.pol.Compiler.getDefaultCompiler(s);
			try {
				Node node = p.build();
				if (node != null) {
					addConstants(node);
					node.addVariable(new Variable(NAMESPACE, "id", WordType.LONG, 0)).getValue().setValue(id, WordType.LONG);
					node.compile();
					node.execute(ctx);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public static JVGShape[] getShapes(JVGPane pane, long[] ids) {
		List<JVGShape> shapesList = new ArrayList<>();
		for (int i = 0; i < ids.length; i++) {
			JVGComponent component = pane.getComponent(ids[i]);
			if (component instanceof JVGShape) {
				shapesList.add((JVGShape) component);
			}
		}

		JVGShape[] shapes = new JVGShape[shapesList.size()];
		shapesList.toArray(shapes);
		return shapes;
	}

	public static long[] getComponents(JVGComponent[] components) {
		long[] array = new long[components.length];
		for (int i = 0; i < components.length; i++) {
			array[i] = components[i].getId();
		}
		return array;
	}

	private void addConstants(Node node) throws ExecuteException {
		addCursorTypeConstants(node);
		addCurveTypeConstants(node);
		swingSupport.exportConstants(node);
	}

	// TODO register constants in ctx
	private void addCursorTypeConstants(Node node) throws ExecuteException {
		node.addVariable(new Variable(NAMESPACE, "CROSSHAIR_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.CROSSHAIR_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "DEFAULT_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.DEFAULT_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "E_RESIZE_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.E_RESIZE_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "HAND_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.HAND_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "MOVE_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.MOVE_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "N_RESIZE_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.N_RESIZE_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "NE_RESIZE_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.NE_RESIZE_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "NW_RESIZE_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.NW_RESIZE_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "S_RESIZE_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.S_RESIZE_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "SE_RESIZE_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.SE_RESIZE_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "SW_RESIZE_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.SW_RESIZE_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "TEXT_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.TEXT_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "W_RESIZE_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.W_RESIZE_CURSOR, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "WAIT_CURSOR", WordType.INT, 0)).getValue().setValue(Cursor.WAIT_CURSOR, WordType.INT);
	}

	private void addCurveTypeConstants(Node node) throws ExecuteException {
		node.addVariable(new Variable(NAMESPACE, "SEG_CLOSE", WordType.INT, 0)).getValue().setValue(PathIterator.SEG_CLOSE, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "SEG_CUBICTO", WordType.INT, 0)).getValue().setValue(PathIterator.SEG_CUBICTO, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "SEG_LINETO", WordType.INT, 0)).getValue().setValue(PathIterator.SEG_LINETO, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "SEG_MOVETO", WordType.INT, 0)).getValue().setValue(PathIterator.SEG_MOVETO, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "SEG_QUADTO", WordType.INT, 0)).getValue().setValue(PathIterator.SEG_QUADTO, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "WIND_EVEN_ODD", WordType.INT, 0)).getValue().setValue(PathIterator.WIND_EVEN_ODD, WordType.INT);
		node.addVariable(new Variable(NAMESPACE, "WIND_NON_ZERO", WordType.INT, 0)).getValue().setValue(PathIterator.WIND_NON_ZERO, WordType.INT);
	}

	public static String getColors(Resource<Color>... colors) {
		StringBuilder buf = new StringBuilder();
		buf.append("int[]{");
		if (colors != null) {
			for (int i = 0; i < colors.length; i++) {
				buf.append(colors[i].getResource().getRGB());
				if (i != colors.length - 1) {
					buf.append(", ");
				}
			}
		}
		buf.append("}");
		return buf.toString();
	}

	public static String getArray(float[] array) {
		StringBuilder buf = new StringBuilder();
		buf.append("float[]{");
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i != array.length - 1) {
					buf.append(", ");
				}
			}
		}
		buf.append("}");
		return buf.toString();
	}

	public static String getArray(int[] array) {
		StringBuilder buf = new StringBuilder();
		buf.append("int[]{");
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i != array.length - 1) {
					buf.append(", ");
				}
			}
		}
		buf.append("}");
		return buf.toString();
	}

	public static String getArray(double[] array) {
		StringBuilder buf = new StringBuilder();
		buf.append("double[]{");
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i != array.length - 1) {
					buf.append(", ");
				}
			}
		}
		buf.append("}");
		return buf.toString();
	}
}
