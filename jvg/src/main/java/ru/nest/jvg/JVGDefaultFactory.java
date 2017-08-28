package ru.nest.jvg;

import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.net.URL;

import javax.swing.ImageIcon;

import ru.nest.jvg.actionarea.JVGCustomActionArea;
import ru.nest.jvg.complexshape.ComplexShapeParser;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGComplexShape;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGImage;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.JVGSubPath;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.shape.paint.FillPainter;

public class JVGDefaultFactory extends JVGFactory {
	protected JVGDefaultFactory() {
	}

	@Override
	public <V extends JVGComponent> V createComponent(Class<V> clazz, Object... params) {
		int size = params != null ? params.length : 0;
		try {
			if (clazz != null) {
				if (clazz == JVGGroup.class) {
					if (size == 0) {
						JVGGroup group = new JVGGroup();
						return (V) group;
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
						image.removeAllPainters();
					}
					return (V) image;
				} else if (clazz == JVGSubPath.class) {
					if (size > 0 && params[0] instanceof Double) {
						Double position = (Double) params[0];
						JVGSubPath path = new JVGSubPath(position);
						path.setSelectable(false);
						return (V) path;
					}
				} else if (clazz == JVGGroupPath.class) {
					Shape shape = null;
					if (size > 0 && params[0] instanceof Shape) {
						shape = (Shape) params[0];
					}

					if (shape == null) {
						shape = new MutableGeneralPath();
					}

					JVGGroupPath path = new JVGGroupPath(shape, false);
					path.setFill(false);
					return (V) path;
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

					JVGPath path = new JVGPath(shape, false);
					if (pathStroke != null) {
						path.setPathStroke(pathStroke);
						path.setAntialias(true);
						path.setPainter(new FillPainter(ColorResource.black));
						path.setOriginalBounds(true);
					} else {
						path.setFill(false);
					}
					return (V) path;
				} else if (clazz == JVGTextField.class) {
					String s = "";
					Font font = null;
					if (size > 0 && params[0] instanceof String) {
						s = (String) params[0];
					}
					if (size > 1 && params[1] instanceof Font) {
						font = (Font) params[1];
					}

					JVGTextField text = new JVGTextField(s, font);
					return (V) text;
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
					text.setEditable(false);
					text.getCaret().setSelectionVisible(true);
					return (V) text;
				} else if (clazz == JVGComplexShape.class) {
					if (size == 1 && params[0] instanceof URL) {
						JVGComplexShape shape = new JVGComplexShape((URL) params[0]);
						return (V) shape;
					} else if (size == 1 && params[0] instanceof ComplexShapeParser) {
						JVGComplexShape shape = new JVGComplexShape((ComplexShapeParser) params[0]);
						return (V) shape;
					}
				} else if (clazz == JVGRoot.class) {
					if (size == 1 && (params[0] instanceof JVGPane || params[0] == null)) {
						JVGRoot root = new JVGRoot((JVGPane) params[0]);
						root.setSelectionType(JVGRoot.SELECTION_NONE);
						return (V) root;
					}
				} else if (clazz == JVGCustomActionArea.class) {
					// control is only for editor
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}
}
