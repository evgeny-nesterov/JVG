package ru.nest.jvg.editor.editoraction;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.IconToggleButton;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.plaf.metal.MetalSliderUI;

import com.sun.java.swing.plaf.motif.MotifInternalFrameUI;

import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.editor.Images;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.editor.JVGEditor;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.geom.GeomUtil;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.RemoveUndoRedo;
import ru.nest.jvg.undoredo.ShapeChangedUndoRedo;

public class EraserEditorAction extends EditorAction {
	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(EraserEditorAction.class.getResource("/ru/nest/jvg/actionarea/cursors/cursor_eraser.png")).getImage(), new Point(15, 15), "pencil");

	public final static int SUBTRACT = 0;

	public final static int UNION = 1;

	public EraserEditorAction(String name, Shape shape) {
		this(name, shape, SUBTRACT);
	}

	public EraserEditorAction(String name, Shape shape, int type) {
		super(name);
		this.shape = shape;
		this.type = type;
		consumeMouseEvents(true);
	}

	private double size = 20;

	private Shape shape;

	private Area eraser;

	private JVGShape[] components;

	public int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		components = getShapes(e);
		if (components != null && components.length > 0) {
			super.actionPerformed(e);
		}
	}

	private CompoundUndoRedo edit;

	@Override
	public void mousePressed(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		adjustedX = e.isControlDown() ? x : adjustedX;
		adjustedY = e.isControlDown() ? y : adjustedY;

		if (lastAdjustedX == adjustedX && lastAdjustedY == adjustedY) {
			return;
		} else {
			lastAdjustedX = adjustedX;
			lastAdjustedY = adjustedY;
		}

		JVGEditPane editorPane = getEditorPane();
		edit = new CompoundUndoRedo(getName(), editorPane);
		moveEraser(adjustedX, adjustedY);
		modificate(false);
		editorPane.repaint();
	}

	private void modificate(boolean connect) {
		switch (type) {
			case SUBTRACT:
				subtract(connect);
				break;

			case UNION:
				union(connect);
				break;
		}
	}

	private Area getEraser(boolean connect) {
		Area a = eraser;
		if (connect) {
			List<Point2D> p = new ArrayList<Point2D>();
			double cx = 0, cy = 0;

			PathIterator it = new FlatteningPathIterator(eraser.getPathIterator(null), 1);
			float points[] = new float[6];
			while (!it.isDone()) {
				int type = it.currentSegment(points);
				switch (type) {
					case PathIterator.SEG_MOVETO:
					case PathIterator.SEG_LINETO:
						p.add(new Point2D.Double(points[0], points[1]));
						cx += points[0];
						cy += points[1];
						break;
				}
				it.next();
			}

			cx /= p.size();
			cy /= p.size();
			double cx0 = cx - dx;
			double cy0 = cy - dy;

			for (Point2D pnt : p) {
				double x1 = pnt.getX();
				double y1 = pnt.getY();
				double x0 = x1 - dx;
				double y0 = y1 - dy;

				Path2D path = new Path2D.Double();
				path.moveTo(cx, cy);
				path.lineTo(cx0, cy0);
				path.lineTo(x0, y0);
				path.lineTo(x1, y1);
				path.closePath();

				if (a == eraser) {
					a = new Area(a);
				}
				a.add(new Area(path));
			}
		}
		return a;
	}

	private void subtract(boolean connect) {
		JVGEditPane editorPane = getEditorPane();
		Area eraser = getEraser(connect);
		for (JVGShape component : components) {
			Area a = new Area(component.getTransformedShape());
			a.subtract(eraser);
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
				edit.add(new ShapeChangedUndoRedo("subtract", editorPane, component, component.getShape(), a));
				component.setShape(a, false);
			}
		}
	}

	private void union(boolean connect) {
		JVGEditPane editorPane = getEditorPane();
		Area eraser = getEraser(connect);
		for (JVGShape component : components) {
			Area a = new Area(component.getTransformedShape());
			a.add(eraser);
			a.transform(component.getInverseTransform());
			edit.add(new ShapeChangedUndoRedo("union", editorPane, component, component.getShape(), a));
			component.setShape(a, false);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		adjustedX = e.isControlDown() ? x : adjustedX;
		adjustedY = e.isControlDown() ? y : adjustedY;

		if (edit != null && !edit.isEmpty()) {
			JVGEditPane editorPane = getEditorPane();
			editorPane.fireUndoableEditUpdate(new UndoableEditEvent(editorPane, edit));
			editorPane.repaint();
		}

		lastAdjustedX = Double.MAX_VALUE;
		lastAdjustedY = Double.MAX_VALUE;
		edit = null;
	}

	private double lastAdjustedX = Double.MAX_VALUE, lastAdjustedY = Double.MAX_VALUE;

	@Override
	public void mouseDragged(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		adjustedX = e.isControlDown() ? x : adjustedX;
		adjustedY = e.isControlDown() ? y : adjustedY;

		if (lastAdjustedX == adjustedX && lastAdjustedY == adjustedY) {
			return;
		} else {
			lastAdjustedX = adjustedX;
			lastAdjustedY = adjustedY;
		}

		JVGEditPane editorPane = getEditorPane();
		moveEraser(adjustedX, adjustedY);
		modificate(true);
		editorPane.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		adjustedX = e.isControlDown() ? x : adjustedX;
		adjustedY = e.isControlDown() ? y : adjustedY;

		JVGEditPane editorPane = getEditorPane();
		moveEraser(adjustedX, adjustedY);
		editorPane.repaint();
	}

	private double x, dx;

	private double y, dy;

	private void moveEraser(double x, double y) {
		if (eraser != null) {
			dx = x - this.x;
			dy = y - this.y;

			Rectangle2D bounds = eraser.getBounds2D();
			eraser.transform(AffineTransform.getTranslateInstance(x - bounds.getX() - bounds.getWidth() / 2.0, y - bounds.getY() - bounds.getHeight() / 2.0));
		}
		this.x = x;
		this.y = y;
	}

	private final static Color background = new Color(0, 0, 0, 200);

	@Override
	public void paint(Graphics2D g) {
		AffineTransform transform = getEditorPane().getTransform();
		try {
			g.transform(transform.createInverse());
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Shape s = transform.createTransformedShape(eraser);
		Paint oldPaint = g.getPaint();
		g.setColor(background);
		g.fill(s);

		g.setXORMode(Color.gray);
		g.draw(s);
		g.setPaint(oldPaint);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.transform(transform);
	}

	@Override
	public void start() {
		eraser = new Area(shape);
		JVGEditPane editorPane = getEditorPane();
		editorPane.setCursor(cursor);

		showTools();
	}

	@Override
	public void finish() {
		eraser = null;
		hideTools();
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

		IconToggleButton btnSquare = new IconToggleButton(Images.getImage("eraser_square.png"));
		btnSquare.setPreferredSize(new Dimension(20, 20));
		btnSquare.setSelected(true);
		btnSquare.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setEraser(new Rectangle2D.Double(0, 0, size, size));
			}
		});

		IconToggleButton btnCircle = new IconToggleButton(Images.getImage("eraser_circle.png"));
		btnCircle.setPreferredSize(new Dimension(20, 20));
		btnCircle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setEraser(new Arc2D.Double(0, 0, size, size, 0, 360, Arc2D.OPEN));
			}
		});

		IconToggleButton btnTrangle = new IconToggleButton(Images.getImage("eraser_triangle.png"));
		btnTrangle.setPreferredSize(new Dimension(20, 20));
		btnTrangle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MutableGeneralPath triangle = new MutableGeneralPath();
				triangle.moveTo(size / 2.0, 0.0);
				triangle.lineTo(0.0, size);
				triangle.lineTo(size, size);
				triangle.closePath();

				setEraser(triangle);
			}
		});

		ButtonGroup grpShape = new ButtonGroup();
		grpShape.add(btnCircle);
		grpShape.add(btnSquare);
		grpShape.add(btnTrangle);

		IconToggleButton btnSubtract = new IconToggleButton(Images.getImage("subtraction.gif"));
		btnSubtract.setSelected(type == SUBTRACT);
		btnSubtract.setPreferredSize(new Dimension(20, 20));
		btnSubtract.setSelected(true);
		btnSubtract.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = SUBTRACT;
			}
		});

		IconToggleButton btnUnion = new IconToggleButton(Images.getImage("union.gif"));
		btnUnion.setPreferredSize(new Dimension(20, 20));
		btnUnion.setSelected(type == UNION);
		btnUnion.setSelected(true);
		btnUnion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = UNION;
			}
		});

		ButtonGroup grpType = new ButtonGroup();
		grpType.add(btnSubtract);
		grpType.add(btnUnion);

		final JSlider sliderScale = new JSlider(SwingConstants.HORIZONTAL, 1, 400, 10);
		sliderScale.setUI(new MetalSliderUI());
		sliderScale.setPaintLabels(true);
		sliderScale.setPaintTicks(true);
		sliderScale.setPaintTrack(true);
		sliderScale.setSnapToTicks(true);
		sliderScale.setValueIsAdjusting(true);
		sliderScale.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				scale = sliderScale.getValue() / 10.0;
				transformEraser();
			}
		});

		final JSlider sliderRotate = new JSlider(SwingConstants.HORIZONTAL, 0, 360, 0);
		sliderRotate.setUI(new MetalSliderUI());
		sliderRotate.setPaintLabels(true);
		sliderRotate.setPaintTicks(true);
		sliderRotate.setPaintTrack(true);
		sliderRotate.setSnapToTicks(true);
		sliderRotate.setValueIsAdjusting(true);
		sliderRotate.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				angle = sliderRotate.getValue() / 180.0 * Math.PI;
				transformEraser();
			}
		});

		pnlTools.add(btnSquare, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(btnCircle, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(btnTrangle, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(btnSubtract, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 20, 5, 5), 0, 0));
		pnlTools.add(btnUnion, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		pnlTools.add(new JLabel(lm.getValue("eraser.scale", "Scale")), new GridBagConstraints(0, 1, 10, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(sliderScale, new GridBagConstraints(10, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(new JLabel(lm.getValue("eraser.rotate", "Rotate")), new GridBagConstraints(0, 2, 10, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(sliderRotate, new GridBagConstraints(10, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		toolsFrame = new JInternalFrame(lm.getValue("eraser.title", "Eraser"), false, true, false, false);
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
	}

	public void setEraser(Shape shape) {
		this.shape = shape;
		transformEraser();
	}

	private double scale = 1.0;

	private double angle = 0;

	private void transformEraser() {
		Rectangle2D bounds = shape.getBounds2D();
		double dx = bounds.getX() + bounds.getWidth() / 2.0;
		double dy = bounds.getY() + bounds.getHeight() / 2.0;

		JVGEditPane editorPane = getEditorPane();
		AffineTransform transform = AffineTransform.getTranslateInstance(-dx, -dy);
		transform.preConcatenate(AffineTransform.getRotateInstance(angle));
		transform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		transform.preConcatenate(AffineTransform.getScaleInstance(scale / editorPane.getScaleX(), scale / editorPane.getScaleY()));

		transformEraser(transform);
	}

	public void transformEraser(AffineTransform transform) {
		eraser = new Area(transform.createTransformedShape(shape));

		JVGEditPane editorPane = getEditorPane();
		editorPane.repaint();
	}

	public void hideTools() {
		toolsFrame.setVisible(false);
	}

	@Override
	public boolean isCustomActionsManager() {
		return false;
	}
}
