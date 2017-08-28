package ru.nest.jvg.editor.editoraction;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.ButtonGroup;
import javax.swing.IconToggleButton;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
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

import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.editor.Images;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.editor.JVGEditor;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.geom.GeomUtil;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.RemoveUndoRedo;
import ru.nest.jvg.undoredo.ShapeChangedUndoRedo;

import com.sun.java.swing.plaf.motif.MotifInternalFrameUI;

public class MarkerEditorAction extends EditorAction {
	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	// TODO: cursor
	private final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(MarkerEditorAction.class.getResource("/ru/nest/jvg/actionarea/cursors/cursor_eraser.png")).getImage(), new Point(15, 15), "marker");

	public final static int SUBTRACT = 0;

	public final static int UNION = 1;

	private double dx = 0;

	private double dy = 5;

	private JVGShape[] components;

	public int type;

	public GeneralPath add;

	private double mx, my;

	private boolean isActive = false;

	private CompoundUndoRedo edit;

	public MarkerEditorAction(String name, int type) {
		super(name);
		this.type = type;
		consumeMouseEvents(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		components = getShapes(e);
		super.actionPerformed(e);
	}

	@Override
	public void mousePressed(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		JVGEditPane editorPane = getEditorPane();
		edit = new CompoundUndoRedo(getName(), editorPane);

		if (components == null) {
			JVGEditor editor = editorPane.getEditor();

			JVGPath component = editorPane.getEditorKit().getFactory().createComponent(JVGPath.class, new Object[] { new MutableGeneralPath(), false });
			component.setFill(true);
			component.setPainter(new FillPainter((Draw) editor.getCurrentFillDraw().clone()));
			component.setAlfa(editor.getCurrentFillTransparency());
			component.setAntialias(editor.getCurrentAntialias());

			editorPane.getRoot().add(component);
			edit.add(new AddUndoRedo(editorPane, component, null, -1, editorPane.getRoot(), -1));

			components = new JVGShape[] { component };
		}

		mx = x;
		my = y;
	}

	private void modificate() {
		switch (type) {
			case SUBTRACT:
				subtract();
				break;

			case UNION:
				union();
				break;
		}
	}

	private void subtract() {
		if (components != null) {
			JVGEditPane editorPane = getEditorPane();
			for (JVGShape component : components) {
				Area a = new Area(component.getTransformedShape());
				a.subtract(new Area(add));

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
					edit.add(new ShapeChangedUndoRedo("add", editorPane, component, component.getShape(), a));
					component.setShape(a, false);
				}
			}
		}
	}

	private void union() {
		if (components != null) {
			JVGEditPane editorPane = getEditorPane();
			for (JVGShape component : components) {
				Area a = new Area(component.getTransformedShape());
				a.add(new Area(add));

				a.transform(component.getInverseTransform());
				edit.add(new ShapeChangedUndoRedo("union", editorPane, component, component.getShape(), a));
				component.setShape(a, false);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (edit != null && !edit.isEmpty()) {
			JVGEditPane editorPane = getEditorPane();
			editorPane.fireUndoableEditUpdate(new UndoableEditEvent(editorPane, edit));
			editorPane.repaint();
		}
		edit = null;
	}

	@Override
	public void mouseDragged(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (add != null) {
			double newx = x;
			double newy = y;
			if (dx == 0 && newx == mx) {
				newx++;
			}
			if (dy == 0 && newy == my) {
				newy++;
			}

			add.reset();
			add.moveTo((float) (mx - dx), (float) (my - dy));
			add.lineTo((float) (mx + dx), (float) (my + dy));
			add.lineTo((float) (newx + dx), (float) (newy + dy));
			add.lineTo((float) (newx - dx), (float) (newy - dy));

			modificate();

			JVGEditPane editorPane = getEditorPane();
			editorPane.repaint();

			mx = x;
			my = y;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		mx = x;
		my = y;

		JVGEditPane editorPane = getEditorPane();
		editorPane.repaint();
	}

	private final static Color markerColor = new Color(0, 0, 0);

	@Override
	public void paint(Graphics2D g) {
		if (isActive) {
			AffineTransform transform = getEditorPane().getTransform();
			try {
				g.transform(transform.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(markerColor);
			g.draw(transform.createTransformedShape(new Line2D.Double(mx - dx, my - dy, mx + dx, my + dy)));

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.transform(transform);
		}
	}

	@Override
	public void start() {
		isActive = true;
		add = new GeneralPath();

		JVGEditPane editorPane = getEditorPane();
		editorPane.setCursor(cursor);
		editorPane.getEditor().getEditorActions().enablePathActions();

		showTools();
	}

	@Override
	public void finish() {
		isActive = false;
		add = null;
		components = null;

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

		IconToggleButton btnSubtract = new IconToggleButton(Images.getImage("subtraction.gif"));
		btnSubtract.setSelected(type == SUBTRACT);
		btnSubtract.setPreferredSize(new Dimension(20, 20));
		btnSubtract.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = SUBTRACT;
			}
		});

		IconToggleButton btnUnion = new IconToggleButton(Images.getImage("union.gif"));
		btnUnion.setPreferredSize(new Dimension(20, 20));
		btnUnion.setSelected(type == UNION);
		btnUnion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = UNION;
			}
		});

		ButtonGroup grpType = new ButtonGroup();
		grpType.add(btnSubtract);
		grpType.add(btnUnion);

		final JSlider sliderSize = new JSlider(SwingConstants.HORIZONTAL, 0, 20, 5);
		final JSlider sliderAngle = new JSlider(SwingConstants.HORIZONTAL, 0, 180, 90);

		// TODO NullPointerException
		// sliderSize.setUI(new MetalSliderUI());

		sliderSize.setPaintLabels(true);
		sliderSize.setPaintTicks(true);
		sliderSize.setPaintTrack(true);
		sliderSize.setSnapToTicks(true);
		sliderSize.setValueIsAdjusting(true);
		sliderSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double angle = sliderAngle.getValue() / 180.0 * Math.PI;
				double size = sliderSize.getValue();
				setMarker(size, angle);
			}
		});

		// sliderAngle.setUI(new MetalSliderUI());
		sliderAngle.setPaintLabels(true);
		sliderAngle.setPaintTicks(true);
		sliderAngle.setPaintTrack(true);
		sliderAngle.setSnapToTicks(true);
		sliderAngle.setValueIsAdjusting(true);
		sliderAngle.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double angle = sliderAngle.getValue() / 180.0 * Math.PI;
				double size = sliderSize.getValue();
				setMarker(size, angle);
			}
		});

		pnlTools.add(btnSubtract, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 20, 5, 5), 0, 0));
		pnlTools.add(btnUnion, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		pnlTools.add(new JLabel(lm.getValue("marker.size", "Size")), new GridBagConstraints(0, 1, 10, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(sliderSize, new GridBagConstraints(10, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(new JLabel(lm.getValue("marker.angle", "Angle")), new GridBagConstraints(0, 2, 10, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlTools.add(sliderAngle, new GridBagConstraints(10, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		toolsFrame = new JInternalFrame(lm.getValue("marker.title", "Marker"), false, true, false, false);
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

	public void setMarker(double size, double angle) {
		dx = size * Math.cos(angle);
		dy = size * Math.sin(angle);

		JVGEditPane editorPane = getEditorPane();
		editorPane.repaint();
	}

	public void hideTools() {
		if (toolsFrame != null) {
			toolsFrame.setVisible(false);
		}
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 700, 700);
		f.setContentPane(new JLabel() {
			Area path = new Area();

			int dx = 7;

			int dy = 7;

			int mx;

			int my;
			{
				setOpaque(true);
				setBackground(Color.white);

				addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						mx = e.getX();
						my = e.getY();
					}

					@Override
					public void mouseReleased(MouseEvent e) {
					}
				});

				addMouseMotionListener(new MouseMotionAdapter() {
					GeneralPath add = new GeneralPath();

					@Override
					public void mouseDragged(MouseEvent e) {
						int x = e.getX();
						int y = e.getY();
						if (dx == 0 && x == mx) {
							x++;
						}
						if (dy == 0 && y == my) {
							y++;
						}

						add.reset();
						add.moveTo(mx - dx, my - dy);
						add.lineTo(mx + dx, my + dy);
						add.lineTo(x + dx, y + dy);
						add.lineTo(x - dx, y - dy);

						Area a = new Area(add);
						path.add(a);
						repaint();

						mx = e.getX();
						my = e.getY();
					}
				});
			}

			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Color.black);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.fill(path);
			}
		});
		f.setVisible(true);
	}

	@Override
	public boolean isCustomActionsManager() {
		return true;
	}
}
