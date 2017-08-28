package ru.nest.jvg.editor.editoraction;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import javax.swing.IconButton;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.SelectionRotater;
import ru.nest.jvg.editor.Images;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.editor.JVGEditor;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.geom.CoordinablePath;
import ru.nest.jvg.geom.GeomUtil;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.RemoveUndoRedo;
import ru.nest.jvg.undoredo.ShapeChangedUndoRedo;

import com.sun.java.swing.plaf.motif.MotifInternalFrameUI;

public class LassoEditorAction extends EditorAction {
	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(LassoEditorAction.class.getResource("/ru/nest/jvg/actionarea/cursors/cursor_eraser.png")).getImage(), new Point(15, 15), "pencil");

	public LassoEditorAction() {
		super("lasso-eraser");
		consumeMouseEvents(true);
	}

	private MutableGeneralPath path;

	private JVGShape[] components;

	@Override
	public void actionPerformed(ActionEvent e) {
		components = getShapes(e);
		if (components != null && components.length > 0) {
			super.actionPerformed(e);
		}
	}

	@Override
	public void mousePressed(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (lastAdjustedX == adjustedX && lastAdjustedY == adjustedY) {
			return;
		} else {
			lastAdjustedX = adjustedX;
			lastAdjustedY = adjustedY;
		}

		path = new MutableGeneralPath();
		path.moveTo(adjustedX, adjustedY);

		JVGEditPane editorPane = getEditorPane();
		editorPane.repaint();
	}

	private void subtract() {
		if (path != null) {
			JVGEditPane editorPane = getEditorPane();
			editorPane.requestFocus();
			CompoundUndoRedo edit = new CompoundUndoRedo("lasso-subtract", editorPane);
			Area pathArea = new Area(path);

			if (components != null) {
				for (JVGShape component : components) {
					Area a = new Area(component.getTransformedShape());
					a.subtract(pathArea);

					if (a.isEmpty()) {
						// delete
						JVGContainer parent = component.getParent();
						if (parent != null) {
							edit.add(new RemoveUndoRedo(editorPane, component, parent, parent.getChildIndex(component)));
							parent.remove(component);
						}
					} else if (!GeomUtil.equals(a, component.getTransformedShape())) {
						// change shape
						a.transform(component.getInverseTransform());
						edit.add(new ShapeChangedUndoRedo("change-shape", editorPane, component, component.getShape(), a));
						component.setShape(a, false);
					}
				}
			}

			if (!edit.isEmpty()) {
				editorPane.fireUndoableEditUpdate(new UndoableEditEvent(editorPane, edit));
				components = getShapes(editorPane);
			}
		}
	}

	private void union() {
		if (path != null) {
			JVGEditPane editorPane = getEditorPane();
			editorPane.requestFocus();
			CompoundUndoRedo edit = new CompoundUndoRedo("lasso-union", editorPane);
			Area pathArea = new Area(path);

			if (components != null) {
				for (JVGShape component : components) {
					Area a = new Area(component.getTransformedShape());

					Area additionalArea = new Area(path);
					additionalArea.add(a);

					if (!additionalArea.isEmpty()) {
						a.add(pathArea);
						a.transform(component.getInverseTransform());

						edit.add(new ShapeChangedUndoRedo("change-shape", editorPane, component, component.getShape(), a));
						component.setShape(a, false);
					}
				}
			}

			if (!edit.isEmpty()) {
				editorPane.fireUndoableEditUpdate(new UndoableEditEvent(editorPane, edit));
				components = getShapes(editorPane);
			}
		}
	}

	private void intersect() {
		if (path != null) {
			JVGEditPane editorPane = getEditorPane();
			editorPane.requestFocus();
			CompoundUndoRedo edit = new CompoundUndoRedo("lasso-intersect", editorPane);
			Area pathArea = new Area(path);

			if (components != null) {
				for (JVGShape component : components) {
					Area a = new Area(component.getTransformedShape());
					a.intersect(pathArea);

					if (a.isEmpty()) {
						// delete
						JVGContainer parent = component.getParent();
						if (parent != null) {
							edit.add(new RemoveUndoRedo(editorPane, component, parent, parent.getChildIndex(component)));
							parent.remove(component);
						}
					} else {
						a.transform(component.getInverseTransform());

						edit.add(new ShapeChangedUndoRedo("change-shape", editorPane, component, component.getShape(), a));
						component.setShape(a, false);
					}
				}
			}

			if (!edit.isEmpty()) {
				editorPane.fireUndoableEditUpdate(new UndoableEditEvent(editorPane, edit));
				components = getShapes(editorPane);
			}
		}
	}

	private void separate() {
		if (path != null) {
			JVGEditPane editorPane = getEditorPane();
			editorPane.requestFocus();
			CompoundUndoRedo edit = new CompoundUndoRedo("lasso-separate", editorPane);
			Area pathArea = new Area(path);

			if (components != null) {
				for (final JVGShape component : components) {
					final Area a2 = new Area(component.getTransformedShape());
					a2.intersect(pathArea);

					if (!a2.isEmpty()) {
						a2.transform(component.getInverseTransform());

						JVGContainer parent = component.getParent();
						int index = parent.getChildIndex(component);

						Area a1 = new Area(component.getTransformedShape());
						a1.subtract(pathArea);
						a1.transform(component.getInverseTransform());

						// change shape
						edit.add(new ShapeChangedUndoRedo("change-shape", editorPane, component, component.getShape(), a1));
						component.setShape(a1, false);

						// add new shape
						JVGPath pathShape = editorPane.getEditorKit().getFactory().createComponent(JVGPath.class, new Object[] { a2, component.getShape() instanceof CoordinablePath });
						parent.add(pathShape, index);
						component.copyTo(pathShape);
						pathShape.setSelected(true, false);
						pathShape.transform(component.getTransform());
						edit.add(new AddUndoRedo(editorPane, pathShape, null, -1, parent, index));
					}
				}
			}

			if (!edit.isEmpty()) {
				editorPane.fireUndoableEditUpdate(new UndoableEditEvent(editorPane, edit));
				components = getShapes(editorPane);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		lastAdjustedX = Double.MAX_VALUE;
		lastAdjustedY = Double.MAX_VALUE;

		path.closePath();

		JVGEditPane editorPane = getEditorPane();
		editorPane.repaint();
	}

	private double lastAdjustedX = Double.MAX_VALUE, lastAdjustedY = Double.MAX_VALUE;

	@Override
	public void mouseDragged(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (lastAdjustedX == adjustedX && lastAdjustedY == adjustedY) {
			return;
		} else {
			lastAdjustedX = adjustedX;
			lastAdjustedY = adjustedY;
		}

		path.lineTo(adjustedX, adjustedY);

		JVGEditPane editorPane = getEditorPane();
		editorPane.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
		super.processKeyEvent(e);

		if (e.getID() == KeyEvent.KEY_PRESSED) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_RIGHT:
					if (path == null || path.numTypes == 0) {
						return;
					}

					JVGEditPane editorPane = getEditorPane();
					double incrementx = editorPane.isGridAlign() && !e.isControlDown() ? editorPane.getIncrement() * editorPane.getScaleX() : 1;
					double incrementy = editorPane.isGridAlign() && !e.isControlDown() ? editorPane.getIncrement() * editorPane.getScaleY() : 1;

					if (path.pointTypes[path.numTypes - 1] == PathIterator.SEG_CLOSE) {
						path.deleteLast();
					}

					double[] point = new double[] { path.pointCoords[path.numCoords - 2], path.pointCoords[path.numCoords - 1] };
					editorPane.getTransform().transform(point, 0, point, 0, 1);
					double x = point[0];
					double y = point[1];
					boolean translated = false;

					switch (e.getKeyCode()) {
						// move point
						case KeyEvent.VK_UP:
							y -= incrementy;
							translated = true;
							break;

						case KeyEvent.VK_LEFT:
							x -= incrementx;
							translated = true;
							break;

						case KeyEvent.VK_DOWN:
							y += incrementy;
							translated = true;
							break;

						case KeyEvent.VK_RIGHT:
							x += incrementx;
							translated = true;
							break;
					}

					if (translated) {
						point[0] = x;
						point[1] = y;
						try {
							editorPane.getTransform().inverseTransform(point, 0, point, 0, 1);
							x = point[0];
							y = point[1];
							path.lineTo(x, y);
							editorPane.repaint();
						} catch (NoninvertibleTransformException exc) {
							exc.printStackTrace();
						}
					}
					break;
			}
		}
		e.consume();
	}

	private final static Color background = new Color(0, 0, 0, 16);

	@Override
	public void paint(Graphics2D g) {
		if (path != null && path.numTypes > 0) {
			AffineTransform transform = getEditorPane().getTransform();
			try {
				g.transform(transform.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (path.numTypes == 1 || (path.numTypes == 2 && path.pointTypes[1] == PathIterator.SEG_CLOSE)) {
				double x = path.pointCoords[0];
				double y = path.pointCoords[1];
				double w = 3 / transform.getScaleX();
				double h = 3 / transform.getScaleY();

				Shape rect = transform.createTransformedShape(new Rectangle2D.Double(x - w / 2, y - h / 2, w, h));
				g.setColor(Color.white);
				g.fill(rect);
				g.setColor(Color.black);
				g.draw(rect);
			} else {
				Shape s = transform.createTransformedShape(path);
				if (path.pointTypes[path.numTypes - 1] == PathIterator.SEG_CLOSE) {
					g.setColor(background);
					g.fill(s);
				}

				g.setColor(Color.white);
				g.draw(s);

				Stroke oldStroke = g.getStroke();
				g.setStroke(rotater.getStroke());
				g.setColor(Color.black);
				g.draw(s);
				g.setStroke(oldStroke);
			}

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.transform(transform);
		}
	}

	private SelectionRotater rotater;

	@Override
	public void start() {
		JVGEditPane editorPane = getEditorPane();
		editorPane.setCursor(cursor);

		showTools();

		rotater = new SelectionRotater(editorPane.getRoot());
		rotater.start();
	}

	@Override
	public void finish() {
		rotater.stopRotate();
		rotater = null;

		path = null;
		hideTools();

		JVGEditPane editorPane = getEditorPane();
		editorPane.repaint();
	}

	private JInternalFrame toolsFrame = null;

	public void showTools() {
		if (toolsFrame != null) {
			toolsFrame.setVisible(true);
			toolsFrame.toFront();
			return;
		}

		JPanel pnlTools = new JPanel();
		pnlTools.setLayout(new GridBagLayout());

		IconButton btnSubtract = new IconButton(Images.getImage("subtraction.gif"));
		btnSubtract.setRequestFocusEnabled(false);
		btnSubtract.setFocusable(false);
		btnSubtract.setPreferredSize(new Dimension(20, 20));
		btnSubtract.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				subtract();
			}
		});

		IconButton btnUnion = new IconButton(Images.getImage("union.gif"));
		btnUnion.setRequestFocusEnabled(false);
		btnUnion.setFocusable(false);
		btnUnion.setPreferredSize(new Dimension(20, 20));
		btnUnion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				union();
			}
		});

		IconButton btnIntersect = new IconButton(Images.getImage("intersection.gif"));
		btnIntersect.setRequestFocusEnabled(false);
		btnIntersect.setFocusable(false);
		btnIntersect.setPreferredSize(new Dimension(20, 20));
		btnIntersect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				intersect();
			}
		});

		IconButton btnSeparate = new IconButton(Images.getImage("union.gif"));
		btnSeparate.setRequestFocusEnabled(false);
		btnSeparate.setFocusable(false);
		btnSeparate.setPreferredSize(new Dimension(20, 20));
		btnSeparate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				separate();
			}
		});

		pnlTools.add(btnSubtract, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(btnUnion, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(btnIntersect, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(btnSeparate, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		toolsFrame = new JInternalFrame(lm.getValue("eraser.title", "Eraser"), false, true, false, false);
		toolsFrame.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				JVGEditPane editorPane = getEditorPane();
				editorPane.requestFocus();
			}
		});
		toolsFrame.setRequestFocusEnabled(false);
		toolsFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		toolsFrame.setContentPane(pnlTools);
		toolsFrame.setUI(new MotifInternalFrameUI(toolsFrame));
		toolsFrame.pack();

		JVGEditPane pane = getEditorPane();
		JVGEditor editor = pane.getEditor();
		JDesktopPane desktop = editor.getDesktop();
		desktop.add(toolsFrame);
		desktop.setLayer(toolsFrame, 3);

		toolsFrame.setLocation((desktop.getWidth() - toolsFrame.getWidth()) / 2, (desktop.getHeight() - toolsFrame.getHeight()) / 2);
		toolsFrame.setVisible(true);

		JVGEditPane editorPane = getEditorPane();
		editorPane.requestFocus();
	}

	public void hideTools() {
		toolsFrame.setVisible(false);
	}

	@Override
	public boolean isCustomActionsManager() {
		return false;
	}
}
