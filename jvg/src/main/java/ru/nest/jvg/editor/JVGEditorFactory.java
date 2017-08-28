package ru.nest.jvg.editor;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.event.UndoableEditEvent;

import ru.nest.awt.strokes.TextStroke;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.action.EditPathAction;
import ru.nest.jvg.action.JVGAction;
import ru.nest.jvg.action.SetPathTextStrokeAction;
import ru.nest.jvg.actionarea.Constants;
import ru.nest.jvg.actionarea.JVGAbstractConnectionActionArea;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.actionarea.JVGAddSubPathActionArea;
import ru.nest.jvg.actionarea.JVGCustomActionArea;
import ru.nest.jvg.actionarea.JVGFreeConnectionActionArea;
import ru.nest.jvg.actionarea.JVGMoveSubPathActionArea;
import ru.nest.jvg.actionarea.JVGOutlineConnectionActionArea;
import ru.nest.jvg.actionarea.JVGPathConnectionActionArea;
import ru.nest.jvg.actionarea.JVGRootScaleActionArea;
import ru.nest.jvg.actionarea.JVGRotateActionArea;
import ru.nest.jvg.actionarea.JVGScaleActionArea;
import ru.nest.jvg.actionarea.JVGShearActionArea;
import ru.nest.jvg.actionarea.JVGTextWrapActionArea;
import ru.nest.jvg.actionarea.JVGVectorActionArea;
import ru.nest.jvg.actionarea.MoveMouseListener;
import ru.nest.jvg.complexshape.ComplexShapeParser;
import ru.nest.jvg.editor.JVGEditPane.InputTextHandler;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGComplexShape;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGImage;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.JVGSubPath;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class JVGEditorFactory extends JVGFactory {
	protected final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(JVGEditorFactory.class.getResource("/ru/nest/jvg/actionarea/cursors/cursor_move.png")).getImage(), new Point(15, 15), "move");

	public JVGEditorFactory() {
	}

	@Override
	public <V extends JVGComponent> V createComponent(Class<V> clazz, Object... params) {
		int size = params != null ? params.length : 0;
		JVGComponent c = null;
		try {
			if (clazz != null) {
				if (clazz == JVGGroup.class) {
					if (size == 0) {
						JVGGroup group = new JVGGroup();
						addGeneralShapeActions(group);
						addGeneralConnections(group);
						c = group;
					}
				} else if (clazz == JVGImage.class) {
					JVGImage image = null;
					if (size == 1) {
						if (params[0] instanceof URL) {
							image = new JVGImage((URL) params[0]);
						}
						if (params[0] instanceof Resource<?>) {
							image = new JVGImage((Resource<ImageIcon>) params[0]);
						}
					}

					if (image != null) {
						addGeneralShapeActions(image);
						image.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 0.5f, 0.5f));
						addGeneralConnections(image);
						image.removeAllPainters();
					}

					c = image;
				} else if (clazz == JVGSubPath.class) {
					if (size > 0 && params[0] instanceof Double) {
						Double position = (Double) params[0];
						JVGSubPath path = new JVGSubPath(position);
						path.setSelectable(true);
						path.add(new JVGMoveSubPathActionArea());
						return (V) path;
					}
				} else if (clazz == JVGGroupPath.class) {
					Shape shape = null;
					boolean coordinable = true;
					if (size > 0 && params[0] instanceof Shape) {
						shape = (Shape) params[0];
					}
					if (size > 1 && params[1] instanceof Boolean) {
						coordinable = (Boolean) params[1];
					}

					if (shape == null) {
						shape = new MutableGeneralPath();
					}

					JVGGroupPath path = new JVGGroupPath(shape, coordinable);
					addGeneralShapeActions(path);
					path.add(new JVGOutlineConnectionActionArea());
					path.add(new JVGPathConnectionActionArea(JVGAbstractConnectionActionArea.CLIENT));
					path.add(new JVGAddSubPathActionArea());
					c = path;
				} else if (clazz == JVGPath.class) {
					Shape shape = null;
					if (size > 0 && params[0] instanceof Shape) {
						shape = (Shape) params[0];
					}
					if (shape == null) {
						shape = new MutableGeneralPath();
					}

					boolean coordinable = true;
					if (size > 1 && params[1] instanceof Boolean) {
						coordinable = (Boolean) params[1];
					}

					Resource<Stroke> pathStroke = null;
					if (size > 2 && params[2] instanceof Resource) {
						pathStroke = (Resource<Stroke>) params[2];
					}

					final JVGPath path = new JVGPath(shape, coordinable);
					addGeneralShapeActions(path);
					path.add(new JVGOutlineConnectionActionArea());
					path.add(new JVGPathConnectionActionArea(JVGAbstractConnectionActionArea.CLIENT));

					if (pathStroke != null) {
						path.setPathStroke(pathStroke);
						path.setAntialias(true);
						path.setPainter(new FillPainter(ColorResource.black));
						path.setOriginalBounds(true);
						path.addMouseListener(new JVGMouseAdapter() {
							@Override
							public void mouseReleased(JVGMouseEvent e) {
								if (e.getButton() == JVGMouseEvent.BUTTON1 && e.getClickCount() == 2 && path.getPathStroke() != null) {
									JVGPane pane = path.getPane();
									if (pane instanceof JVGEditPane) {
										JVGEditPane epane = (JVGEditPane) pane;
										TextStroke stroke = (TextStroke) path.getPathStroke().getResource();
										epane.showInputText(path, stroke.getText(), new InputTextHandler() {
											@Override
											public void setValue(String value) {
												JVGAction action = new SetPathTextStrokeAction(value, SetPathTextStrokeAction.TYPE_TEXT);
												action.doAction();
											}
										});
									}
								}
							}
						});
						EditPathAction.setEdited(path, true, false);
					} else {
						path.setFill(false);
					}
					c = path;
				} else if (clazz == JVGTextField.class) {
					String s = "";
					Font font = null;
					if (size > 0 && params[0] instanceof String) {
						s = (String) params[0];
					}
					if (size > 1 && params[1] instanceof Font) {
						font = (Font) params[1];
					}

					final JVGTextField text = new JVGTextField(s, font);
					text.addMouseListener(new JVGMouseAdapter() {
						@Override
						public void mouseReleased(JVGMouseEvent e) {
							if (e.getButton() == JVGMouseEvent.BUTTON1 && e.getClickCount() == 2) {
								JVGPane pane = text.getPane();
								if (pane instanceof JVGEditPane) {
									final JVGEditPane epane = (JVGEditPane) pane;
									epane.showInputText(text, text.getText(), new InputTextHandler() {
										@Override
										public void setValue(String value) {
											String oldText = text.getText();
											if (!value.equals(oldText)) {
												text.setText(value);
												epane.fireUndoableEditUpdate(new UndoableEditEvent(epane, new PropertyUndoRedo("text", epane, text, "setText", oldText, value)));
												epane.repaint();
											}
										}
									});
								}
							}
						}
					});

					addGeneralShapeActions(text);
					text.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 0.5f, 0.5f));
					addGeneralConnections(text);
					c = text;
				} else if (clazz == JVGStyledText.class) {
					String s = "";
					if (size > 0 && params[0] instanceof String) {
						s = (String) params[0];
					}

					Resource<Font> font = null;
					if (size > 1) {
						if (params[1] instanceof Font) {
							font = new FontResource((Font) params[1]);
						} else if (params[1] instanceof Resource) {
							font = (Resource<Font>) params[1];
						}
					}

					JVGStyledText text = new JVGStyledText(s, font);

					text.add(new JVGTextWrapActionArea());

					MoveMouseListener.install(text);

					text.add(new JVGScaleActionArea(Constants.TOP));
					text.add(new JVGScaleActionArea(Constants.LEFT));
					text.add(new JVGScaleActionArea(Constants.BOTTOM));
					text.add(new JVGScaleActionArea(Constants.TOP_LEFT));
					text.add(new JVGScaleActionArea(Constants.TOP_RIGHT));
					text.add(new JVGScaleActionArea(Constants.BOTTOM_LEFT));
					text.add(new JVGScaleActionArea(Constants.BOTTOM_RIGHT));

					add(text, new JVGVectorActionArea(Constants.BOTTOM), false);
					add(text, new JVGVectorActionArea(Constants.LEFT), false);
					add(text, new JVGVectorActionArea(Constants.RIGHT), false);
					add(text, new JVGVectorActionArea(Constants.TOP), false);

					add(text, new JVGShearActionArea(Constants.TOP), false);
					add(text, new JVGShearActionArea(Constants.LEFT), false);
					add(text, new JVGShearActionArea(Constants.BOTTOM), false);
					add(text, new JVGShearActionArea(Constants.RIGHT), false);

					text.add(new JVGRotateActionArea(Math.PI / 24.0));

					addGeneralConnections(text);

					text.setEditable(true);
					c = text;
				} else if (clazz == JVGComplexShape.class) {
					JVGComplexShape shape = null;
					if (size == 1 && params[0] instanceof URL) {
						shape = new JVGComplexShape((URL) params[0]);
					}
					if (size == 1 && params[0] instanceof ComplexShapeParser) {
						shape = new JVGComplexShape((ComplexShapeParser) params[0]);
					}

					if (shape != null) {
						MoveMouseListener.install(shape);

						shape.add(shape.getContext().getActions());
						shape.add(new JVGRotateActionArea(Math.PI / 12.0));
						shape.add(shape.getContext().getConnections());

						c = shape;
					}
				} else if (clazz == JVGRoot.class) {
					if (size == 1 && (params[0] instanceof JVGPane || params[0] == null)) {
						JVGRoot root = new JVGRoot((JVGPane) params[0]);
						root.setSelectionType(JVGRoot.SELECTION_AREA);

						add(root, new JVGRootScaleActionArea(Constants.TOP), true);
						add(root, new JVGRootScaleActionArea(Constants.LEFT), true);
						add(root, new JVGRootScaleActionArea(Constants.BOTTOM), true);
						add(root, new JVGRootScaleActionArea(Constants.RIGHT), true);
						add(root, new JVGRootScaleActionArea(Constants.TOP_LEFT), true);
						add(root, new JVGRootScaleActionArea(Constants.TOP_RIGHT), true);
						add(root, new JVGRootScaleActionArea(Constants.BOTTOM_LEFT), true);
						add(root, new JVGRootScaleActionArea(Constants.BOTTOM_RIGHT), true);
						return (V) root;
					}
				} else if (clazz == JVGCustomActionArea.class) {
					JVGCustomActionArea control = new JVGCustomActionArea();
					c = control;
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		if (c != null) {
			c.setCursor(cursor);
		}
		return (V) c;
	}

	protected void addGeneralShapeActions(JVGShape shape) {
		MoveMouseListener.install(shape);

		add(shape, new JVGRotateActionArea(Math.PI / 24.0), true);

		add(shape, new JVGScaleActionArea(Constants.TOP), true);
		add(shape, new JVGScaleActionArea(Constants.LEFT), true);
		add(shape, new JVGScaleActionArea(Constants.BOTTOM), true);
		add(shape, new JVGScaleActionArea(Constants.RIGHT), true);
		add(shape, new JVGScaleActionArea(Constants.TOP_LEFT), true);
		add(shape, new JVGScaleActionArea(Constants.TOP_RIGHT), true);
		add(shape, new JVGScaleActionArea(Constants.BOTTOM_LEFT), true);
		add(shape, new JVGScaleActionArea(Constants.BOTTOM_RIGHT), true);

		add(shape, new JVGVectorActionArea(Constants.BOTTOM), false);
		add(shape, new JVGVectorActionArea(Constants.LEFT), false);
		add(shape, new JVGVectorActionArea(Constants.RIGHT), false);
		add(shape, new JVGVectorActionArea(Constants.TOP), false);

		add(shape, new JVGShearActionArea(Constants.TOP), false);
		add(shape, new JVGShearActionArea(Constants.LEFT), false);
		add(shape, new JVGShearActionArea(Constants.BOTTOM), false);
		add(shape, new JVGShearActionArea(Constants.RIGHT), false);
	}

	protected JVGContainer add(JVGContainer shape, JVGActionArea action, boolean visible) {
		shape.add(action);
		action.setVisible(visible);
		return shape;
	}

	protected void addGeneralConnections(JVGContainer shape) {
		shape.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 0.0f, 0.0f));
		shape.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 1.0f, 0.0f));
		shape.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 1.0f, 1.0f));
		shape.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 0.0f, 1.0f));
		shape.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 0.5f, 0.0f));
		shape.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 0.0f, 0.5f));
		shape.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 0.5f, 1.0f));
		shape.add(new JVGFreeConnectionActionArea(JVGAbstractConnectionActionArea.SERVER, 1.0f, 0.5f));
	}
}
