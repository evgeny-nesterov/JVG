package ru.nest.jvg.editor;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.menu.WMenu;
import javax.swing.menu.WSeparator;

import ru.nest.jvg.ComponentOrderComparator;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.actionarea.JVGAbstractConnectionActionArea;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.conn.ConnectionManager;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.event.JVGComponentEvent;
import ru.nest.jvg.event.JVGContainerEvent;
import ru.nest.jvg.event.JVGEvent;
import ru.nest.jvg.event.JVGKeyEvent;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.parser.DocumentFormat;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParser;
import ru.nest.jvg.shape.JVGShape;

public class JVGEditPane extends JVGPane {
	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private DocumentFormat documentFormat = DocumentFormat.jvg;

	public JVGEditPane(JVGEditor editor) {
		this(editor, 0, 0);
	}

	public JVGEditPane(JVGEditor editor, int documentWidth, int documentHeight) {
		this.editor = editor;

		setDocumentInsets(new Insets(600, 600, 600, 600));
		setDocumentSize(new Dimension(documentWidth, documentHeight));
		updateDocumentSize();
		updateTransform();
		setIncrement(5);
		setLayout(new JVGEditorPaneLayout());

		setEditable(true);
		setOpaque(false);
		setBackground(Color.white);
		setKeyboardActions();
		getEditorKit().setFactory(JVGFactory.createEditor());
		setScriptingEnabled(false);

		try {
			robot = new Robot();
			robot.setAutoDelay(0);
		} catch (AWTException exc) {
			exc.printStackTrace();
		}
	}

	private JVGEditor editor;

	public JVGEditor getEditor() {
		return editor;
	}

	private Robot robot;

	public Robot getRobot() {
		return robot;
	}

	private Rectangle projectionBounds = null;

	public Rectangle getProjectionBounds() {
		return projectionBounds;
	}

	public void setProjectionBounds(Rectangle projectionBounds) {
		Rectangle oldProjectionBounds = this.projectionBounds;
		this.projectionBounds = projectionBounds;
		firePropertyChange("projection-bounds", oldProjectionBounds, projectionBounds);
	}

	public void moveCursor(int newx, int newy) {
		Point screenLocation = getLocationOnScreen();
		newx += screenLocation.x;
		newy += screenLocation.y;
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.mouseMove(newx, newy);
		robot.mousePress(InputEvent.BUTTON1_MASK);
	}

	/**
	 * InputEvent.BUTTON1_MASK InputEvent.BUTTON2_MASK InputEvent.BUTTON3_MASK
	 */
	public void mousePress(int button) {
		robot.mousePress(button);
	}

	public void mouseRelease(int button) {
		robot.mouseRelease(button);
	}

	public void moveMouse(int x, int y) {
		Point screenLocation = getLocationOnScreen();
		x += screenLocation.x;
		y += screenLocation.y;
		robot.mouseMove(x, y);
	}

	private Editor editorAction;

	public void setEditor(Editor editorAction) {
		if (this.editorAction != null) {
			this.editorAction.finish();
		}

		this.editorAction = editorAction;
		setCursor(Cursor.getDefaultCursor());
		getEditor().getEditorActions().updateDependedOnSelectionAndFocus();

		if (this.editorAction != null) {
			getEditor().getEditorActions().setFreezeActions(this.editorAction.isCustomActionsManager());
			this.editorAction.start();
		} else {
			getEditor().getEditorActions().setFreezeActions(false);
		}
	}

	public void processNativeMouseEvent(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		super.processMouseEvent(e, x, y, adjustedX, adjustedY);
	}

	private double mx, my;

	private JViewport parentScroll = null;

	@Override
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (inputs.size() > 0) {
			return;
		}

		if (editorAction != null) {
			editorAction.processMouseEvent(e, x, y, adjustedX, adjustedY);
			if (e.isConsumed()) {
				// TODO allow process mouse exit event
				return;
			}
		}

		super.processMouseEvent(e, x, y, adjustedX, adjustedY);

		if (!e.isConsumed() && getFocusOwner() == null) {
			int selectionType = getRoot().getSelectionType();
			if (selectionType == JVGRoot.SELECTION_NONE) {
				switch (e.getID()) {
					case MouseEvent.MOUSE_PRESSED:
						if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
							Container parent = getParent();
							if (parent instanceof JViewport) {
								parentScroll = (JViewport) parent;
								mx = e.getX();
								my = e.getY();
								setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							}
						}
						break;

					case MouseEvent.MOUSE_RELEASED:
						parentScroll = null;
						setCursor(Cursor.getDefaultCursor());
						break;

					case MouseEvent.MOUSE_DRAGGED:
						if (parentScroll != null) {
							int dx = (int) (e.getX() - mx);
							int dy = (int) (e.getY() - my);
							if (dx != 0 || dy != 0) {
								Point p = parentScroll.getViewPosition();
								p.x -= dx;
								p.y -= dy;
								if (p.x < 0) {
									p.x = 0;
								}
								if (p.y < 0) {
									p.y = 0;
								}

								parentScroll.setViewPosition(p);
								parentScroll.validate();
							}
						}
						break;
				}
			}
		}
	}

	private int oldSelectionType = JVGRoot.SELECTION_NONE;

	@Override
	public void processKeyEvent(KeyEvent e) {
		if (inputs.size() > 0) {
			return;
		}

		if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				// инструмент "Рука" с нажатой клавишей "Пробел"
				if (oldSelectionType == JVGRoot.SELECTION_NONE) {
					oldSelectionType = getRoot().getSelectionType();
					getRoot().setSelectionType(JVGRoot.SELECTION_NONE);
				}
			}
		} else if (e.getID() == KeyEvent.KEY_RELEASED) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				// инструмент "Рука" с нажатой клавишей "Пробел"
				if (oldSelectionType != JVGRoot.SELECTION_NONE) {
					getRoot().setSelectionType(oldSelectionType);
					oldSelectionType = JVGRoot.SELECTION_NONE;
				}
			}
		}

		if (editorAction != null) {
			editorAction.processKeyEvent(e);
			if (e.isConsumed()) {
				return;
			}
		}
		super.processKeyEvent(e);
	}

	@Override
	public void processJVGEvent(JVGEvent e) {
		super.processJVGEvent(e);

		if (e instanceof JVGKeyEvent) {
			if (e.isConsumed()) {
				return;
			}

			JVGKeyEvent ke = (JVGKeyEvent) e;
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
					if (ke.getModifiers() == 0) {
						getEditorKit();
						JVGEditorKit.getAction(JVGEditorKit.REMOVE_ACTION).doAction();
					}
				} else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
					getEditorKit();
					JVGEditorKit.getAction(JVGEditorKit.UNSELECT_ALL_ACTION).doAction();
					setFocusOwner(null);
				} else if (ke.getKeyCode() == KeyEvent.VK_TAB) {
					getEditorKit();
					JVGEditorKit.getAction(JVGEditorKit.NEXT_ACTION).doAction();
				}
			}
		} else if (e.getID() == JVGMouseEvent.MOUSE_PRESSED) {
		} else if (e.getID() == JVGMouseEvent.MOUSE_RELEASED) {
			setProjectionBounds(null);

			if (isConnectionsEnabled()) {
				if (ConnectionManager.postAdjust(this)) {
					repaint();
				}
			}

			JVGMouseEvent me = (JVGMouseEvent) e;
			if (me.getButton() == JVGMouseEvent.BUTTON3) {
				showPopupMenu(me);
			}
		} else if (e.getID() == JVGMouseEvent.MOUSE_DRAGGED) {
			JVGComponent shape = null;
			if (e.getSource() instanceof JVGShape) {
				shape = e.getSource();
			} else if (e.getSource() instanceof JVGActionArea) {
				shape = e.getSource().getParent();
			}

			if (shape != null) {
				setProjectionBounds(getTransform().createTransformedShape(shape.getRectangleBounds()).getBounds());
			}
		} else if (e.getID() == JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED || e.getID() == JVGComponentEvent.COMPONENT_HIDDEN || e.getID() == JVGComponentEvent.COMPONENT_SHOWN || e.getID() == JVGComponentEvent.COMPONENT_TRANSFORMED || e.getID() == JVGContainerEvent.COMPONENT_ADDED || e.getID() == JVGContainerEvent.COMPONENT_REMOVED) {
			if (isConnectionsEnabled()) {
				JVGComponent src = e.getSource();
				if (src instanceof JVGShape) {
					JVGShape shape = (JVGShape) src;
					if (hasConnection(shape)) {
						ConnectionManager.accept(this, shape);
						// System.out.println(src.getName() + "> changed");
					}
				}
			}
		}
	}

	public void defineConnections() {
		if (isConnectionsEnabled()) {
			JVGRoot root = getRoot();
			boolean repaint = false;

			int componentsCount = root.getChildCount();
			JVGComponent[] components = root.getChildren();
			for (int i = 0; i < componentsCount; i++) {
				if (components[i] instanceof JVGShape) {
					JVGShape shape = (JVGShape) components[i];
					if (hasConnection(shape)) {
						ConnectionManager.accept(this, shape);
						repaint |= ConnectionManager.postAdjust(this);
					}
				}
			}

			if (repaint) {
				repaint();
			}
		}
	}

	private boolean isConnectionsEnabled = false;

	@Override
	public boolean isConnectionsEnabled() {
		return isConnectionsEnabled;
	}

	public void setConnectionsEnabled(boolean isConnectionsEnabled) {
		if (this.isConnectionsEnabled != isConnectionsEnabled) {
			boolean oldValue = this.isConnectionsEnabled;
			this.isConnectionsEnabled = isConnectionsEnabled;

			if (!isConnectionsEnabled) {
				ConnectionManager.clear(this);
			}

			firePropertyChange("connections-enabled", oldValue, isConnectionsEnabled);
		}
	}

	public boolean hasConnection(JVGShape shape) {
		int childsCount = shape.getChildCount();
		JVGComponent[] childs = shape.getChildren();
		for (int i = 0; i < childsCount; i++) {
			if (childs[i] instanceof JVGAbstractConnectionActionArea) {
				return true;
			}
		}

		return false;
	}

	private void showPopupMenu(final JVGMouseEvent me) {
		if (me.isConsumed()) {
			return;
		}

		WMenu popup = new WMenu();

		WMenu menuPathOperations = new WMenu(lm.getValue("button.path.operations", "Path Operations"));
		menuPathOperations.add(editor.getEditorActions().getAction("union").getContextMenuItem());
		menuPathOperations.add(editor.getEditorActions().getAction("subtraction").getContextMenuItem());
		menuPathOperations.add(editor.getEditorActions().getAction("intersection").getContextMenuItem());
		menuPathOperations.add(editor.getEditorActions().getAction("xor").getContextMenuItem());
		menuPathOperations.add(new WSeparator());
		menuPathOperations.add(editor.getEditorActions().getAction("close-path").getContextMenuItem());
		menuPathOperations.add(editor.getEditorActions().getAction("smooth-path").getContextMenuItem());
		menuPathOperations.add(editor.getEditorActions().getAction("stroke-path").getContextMenuItem());
		menuPathOperations.add(editor.getEditorActions().getAction("flat-path").getContextMenuItem());

		// --- create menu ---
		popup.add(editor.getEditorActions().getAction("undo").getContextMenuItem());
		popup.add(editor.getEditorActions().getAction("redo").getContextMenuItem());

		popup.add(new WSeparator());

		popup.add(editor.getEditorActions().getAction("cut").getContextMenuItem());
		popup.add(editor.getEditorActions().getAction("copy").getContextMenuItem());
		popup.add(editor.getEditorActions().getAction("paste").getContextMenuItem());
		popup.add(editor.getEditorActions().getAction("delete").getContextMenuItem());

		popup.add(new WSeparator());

		popup.add(editor.getEditorActions().getAction("edit-path").getContextMenuItem());
		popup.add(editor.getEditorActions().getAction("to-path").getContextMenuItem());
		popup.add(menuPathOperations);

		popup.add(new WSeparator());

		popup.add(editor.getEditorActions().getAction("group").getContextMenuItem());

		// show popup
		double[] point = { me.getX(), me.getY() };
		getTransform().transform(point, 0, point, 0, 1);
		popup.getPopupMenu().show(this, (int) point[0], (int) point[1]);
	}

	@Override
	public void paintTransformed(Graphics2D g) {
		super.paintTransformed(g);

		if (editorAction != null) {
			editorAction.paint(g);
		}
	}

	@Override
	protected void paintBackground(Graphics2D g) {
		AffineTransform t = getTransform();

		Dimension documentSize = getDocumentSize();
		Shape r = t.createTransformedShape(new Rectangle2D.Double(0, 0, documentSize.width, documentSize.height));
		g.setColor(getBackground());
		g.fill(r);

		g.setColor(Color.gray);
		double w = 5 / t.getScaleX();
		Shape r1 = t.createTransformedShape(new Rectangle2D.Double(documentSize.width, w, w, documentSize.height));
		Shape r2 = t.createTransformedShape(new Rectangle2D.Double(w, documentSize.height, documentSize.width, w));
		g.fill(r1);
		g.fill(r2);
	}

	@Override
	public void paintGrid(Graphics2D g) {
		AffineTransform t = getTransform();

		Dimension documentSize = getDocumentSize();
		Shape r = t.createTransformedShape(new Rectangle2D.Double(0, 0, documentSize.width, documentSize.height));

		Shape oldClip = g.getClip();
		g.clip(r);

		super.paintGrid(g);

		g.setClip(oldClip);
		g.setColor(Color.darkGray);
		g.draw(r);
	}

	public void setKeyboardActions() {
		ActionMap am = getActionMap();
		InputMap im = getInputMap();

		getEditorKit();
		// select
		am.put(JVGEditorKit.SELECT_ALL_ACTION, JVGEditorKit.getAction(JVGEditorKit.SELECT_ALL_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK), JVGEditorKit.SELECT_ALL_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.SELECT_FOCUSED_ACTION, JVGEditorKit.getAction(JVGEditorKit.SELECT_FOCUSED_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), JVGEditorKit.SELECT_FOCUSED_ACTION);

		getEditorKit();
		// focus
		am.put(JVGEditorKit.NEXT_ACTION, JVGEditorKit.getAction(JVGEditorKit.NEXT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK), JVGEditorKit.NEXT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.PREV_ACTION, JVGEditorKit.getAction(JVGEditorKit.PREV_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK), JVGEditorKit.PREV_ACTION);

		getEditorKit();
		// order
		am.put(JVGEditorKit.TO_FRONT_ACTION, JVGEditorKit.getAction(JVGEditorKit.TO_FRONT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, Event.CTRL_MASK), JVGEditorKit.TO_FRONT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.TO_BACK_ACTION, JVGEditorKit.getAction(JVGEditorKit.TO_BACK_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, Event.CTRL_MASK), JVGEditorKit.TO_BACK_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.TO_UP_ACTION, JVGEditorKit.getAction(JVGEditorKit.TO_UP_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK), JVGEditorKit.TO_UP_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.TO_DOWN_ACTION, JVGEditorKit.getAction(JVGEditorKit.TO_DOWN_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK), JVGEditorKit.TO_DOWN_ACTION);

		getEditorKit();
		// alignment
		am.put(JVGEditorKit.BOTTOM_ALIGNMENT_ACTION, JVGEditorKit.getAction(JVGEditorKit.BOTTOM_ALIGNMENT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.SHIFT_MASK), JVGEditorKit.BOTTOM_ALIGNMENT_ACTION);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, Event.SHIFT_MASK), JVGEditorKit.BOTTOM_ALIGNMENT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.TOP_ALIGNMENT_ACTION, JVGEditorKit.getAction(JVGEditorKit.TOP_ALIGNMENT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.SHIFT_MASK), JVGEditorKit.TOP_ALIGNMENT_ACTION);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, Event.SHIFT_MASK), JVGEditorKit.BOTTOM_ALIGNMENT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.LEFT_ALIGNMENT_ACTION, JVGEditorKit.getAction(JVGEditorKit.LEFT_ALIGNMENT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.SHIFT_MASK), JVGEditorKit.LEFT_ALIGNMENT_ACTION);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, Event.SHIFT_MASK), JVGEditorKit.BOTTOM_ALIGNMENT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.RIGHT_ALIGNMENT_ACTION, JVGEditorKit.getAction(JVGEditorKit.RIGHT_ALIGNMENT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.SHIFT_MASK), JVGEditorKit.RIGHT_ALIGNMENT_ACTION);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, Event.SHIFT_MASK), JVGEditorKit.BOTTOM_ALIGNMENT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.CENTER_ALIGNMENT_ACTION, JVGEditorKit.getAction(JVGEditorKit.CENTER_ALIGNMENT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.SHIFT_MASK), JVGEditorKit.CENTER_ALIGNMENT_ACTION);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, Event.SHIFT_MASK), JVGEditorKit.CENTER_ALIGNMENT_ACTION);

		getEditorKit();
		// remove
		am.put(JVGEditorKit.REMOVE_ACTION, JVGEditorKit.getAction(JVGEditorKit.REMOVE_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JVGEditorKit.REMOVE_ACTION);

		getEditorKit();
		// translate
		am.put(JVGEditorKit.TRANSLATE_DOWN_ACTION, JVGEditorKit.getAction(JVGEditorKit.TRANSLATE_DOWN_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.ALT_MASK), JVGEditorKit.TRANSLATE_DOWN_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.TRANSLATE_UP_ACTION, JVGEditorKit.getAction(JVGEditorKit.TRANSLATE_UP_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.ALT_MASK), JVGEditorKit.TRANSLATE_UP_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.TRANSLATE_LEFT_ACTION, JVGEditorKit.getAction(JVGEditorKit.TRANSLATE_LEFT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.ALT_MASK), JVGEditorKit.TRANSLATE_LEFT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.TRANSLATE_RIGHT_ACTION, JVGEditorKit.getAction(JVGEditorKit.TRANSLATE_RIGHT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.ALT_MASK), JVGEditorKit.TRANSLATE_RIGHT_ACTION);

		getEditorKit();
		// rotate
		am.put(JVGEditorKit.ROTATE_90_ACTION, JVGEditorKit.getAction(JVGEditorKit.ROTATE_90_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK), JVGEditorKit.ROTATE_90_ACTION);

		getEditorKit();
		// flip
		am.put(JVGEditorKit.FLIP_HORIZONTAL_ACTION, JVGEditorKit.getAction(JVGEditorKit.FLIP_HORIZONTAL_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), JVGEditorKit.FLIP_HORIZONTAL_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.FLIP_VERTICAL_ACTION, JVGEditorKit.getAction(JVGEditorKit.FLIP_VERTICAL_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.SHIFT_MASK), JVGEditorKit.FLIP_VERTICAL_ACTION);

		getEditorKit();
		// border
		am.put(JVGEditorKit.MOVE_DOWN__BOTTOM_BORDER_ACTION, JVGEditorKit.getAction(JVGEditorKit.MOVE_DOWN__BOTTOM_BORDER_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.SHIFT_MASK), JVGEditorKit.MOVE_DOWN__BOTTOM_BORDER_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.MOVE_UP__BOTTOM_BORDER_ACTION, JVGEditorKit.getAction(JVGEditorKit.MOVE_UP__BOTTOM_BORDER_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.SHIFT_MASK), JVGEditorKit.MOVE_UP__BOTTOM_BORDER_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.MOVE_DOWN__TOP_BORDER_ACTION, JVGEditorKit.getAction(JVGEditorKit.MOVE_DOWN__TOP_BORDER_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.SHIFT_MASK), JVGEditorKit.MOVE_DOWN__TOP_BORDER_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.MOVE_UP__TOP_BORDER_ACTION, JVGEditorKit.getAction(JVGEditorKit.MOVE_UP__TOP_BORDER_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.SHIFT_MASK), JVGEditorKit.MOVE_UP__TOP_BORDER_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.MOVE_LEFT__LEFT_BORDER_ACTION, JVGEditorKit.getAction(JVGEditorKit.MOVE_LEFT__LEFT_BORDER_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.ALT_MASK), JVGEditorKit.MOVE_LEFT__LEFT_BORDER_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.MOVE_RIGHT__LEFT_BORDER_ACTION, JVGEditorKit.getAction(JVGEditorKit.MOVE_RIGHT__LEFT_BORDER_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.ALT_MASK), JVGEditorKit.MOVE_RIGHT__LEFT_BORDER_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.MOVE_LEFT__RIGHT_BORDER_ACTION, JVGEditorKit.getAction(JVGEditorKit.MOVE_LEFT__RIGHT_BORDER_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.ALT_MASK), JVGEditorKit.MOVE_LEFT__RIGHT_BORDER_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.MOVE_RIGHT__RIGHT_BORDER_ACTION, JVGEditorKit.getAction(JVGEditorKit.MOVE_RIGHT__RIGHT_BORDER_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.ALT_MASK), JVGEditorKit.MOVE_RIGHT__RIGHT_BORDER_ACTION);

		getEditorKit();
		// same size
		am.put(JVGEditorKit.SAME_WIDTH_ACTION, JVGEditorKit.getAction(JVGEditorKit.SAME_WIDTH_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.SHIFT_MASK | Event.CTRL_MASK), JVGEditorKit.SAME_WIDTH_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.SAME_HEIGHT_ACTION, JVGEditorKit.getAction(JVGEditorKit.SAME_HEIGHT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.SHIFT_MASK | Event.CTRL_MASK), JVGEditorKit.SAME_HEIGHT_ACTION);

		getEditorKit();
		// group
		am.put(JVGEditorKit.GROUP_ACTION, JVGEditorKit.getAction(JVGEditorKit.GROUP_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK), JVGEditorKit.GROUP_ACTION);

		getEditorKit();
		// group alignment
		am.put(JVGEditorKit.TOP_GROUP_ALIGNMENT_ACTION, JVGEditorKit.getAction(JVGEditorKit.TOP_GROUP_ALIGNMENT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK | Event.ALT_MASK), JVGEditorKit.TOP_GROUP_ALIGNMENT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.LEFT_GROUP_ALIGNMENT_ACTION, JVGEditorKit.getAction(JVGEditorKit.LEFT_GROUP_ALIGNMENT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_J, Event.CTRL_MASK | Event.ALT_MASK), JVGEditorKit.LEFT_GROUP_ALIGNMENT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.BOTTOM_GROUP_ALIGNMENT_ACTION, JVGEditorKit.getAction(JVGEditorKit.BOTTOM_GROUP_ALIGNMENT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK | Event.ALT_MASK), JVGEditorKit.BOTTOM_GROUP_ALIGNMENT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.RIGHT_GROUP_ALIGNMENT_ACTION, JVGEditorKit.getAction(JVGEditorKit.RIGHT_GROUP_ALIGNMENT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK | Event.ALT_MASK), JVGEditorKit.RIGHT_GROUP_ALIGNMENT_ACTION);

		getEditorKit();
		// same spaces
		am.put(JVGEditorKit.SAME_SPACES_HORIZONTAL_ACTION, JVGEditorKit.getAction(JVGEditorKit.SAME_SPACES_HORIZONTAL_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK | Event.ALT_MASK), JVGEditorKit.SAME_SPACES_HORIZONTAL_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.SAME_SPACES_VERTICAL_ACTION, JVGEditorKit.getAction(JVGEditorKit.SAME_SPACES_VERTICAL_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK | Event.ALT_MASK), JVGEditorKit.SAME_SPACES_VERTICAL_ACTION);

		getEditorKit();
		// cut / copy / paste
		am.put(JVGEditorKit.CUT_ACTION, JVGEditorKit.getAction(JVGEditorKit.CUT_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK), JVGEditorKit.CUT_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.COPY_ACTION, JVGEditorKit.getAction(JVGEditorKit.COPY_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK), JVGEditorKit.COPY_ACTION);

		getEditorKit();
		am.put(JVGEditorKit.PASTE_ACTION, JVGEditorKit.getAction(JVGEditorKit.PASTE_ACTION));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK), JVGEditorKit.PASTE_ACTION);

		getEditorKit();
		// editor pane
		am.put(JVGEditorKit.PANE_ZOOM_IN, JVGEditorKit.getAction(JVGEditorKit.PANE_ZOOM_IN));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, Event.CTRL_MASK), JVGEditorKit.PANE_ZOOM_IN); // NUMPAD +
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, Event.CTRL_MASK), JVGEditorKit.PANE_ZOOM_IN);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Event.CTRL_MASK), JVGEditorKit.PANE_ZOOM_IN);

		getEditorKit();
		am.put(JVGEditorKit.PANE_ZOOM_OUT, JVGEditorKit.getAction(JVGEditorKit.PANE_ZOOM_OUT));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, Event.CTRL_MASK), JVGEditorKit.PANE_ZOOM_OUT); // NUMPAD -
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Event.CTRL_MASK), JVGEditorKit.PANE_ZOOM_OUT);
	}

	public File save() {
		if (fileName != null) {
			try {
				File file = new File(fileName);
				save(file);
				return file;
			} catch (Exception exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(JVGEditPane.this, exc, lm.getValue("pane.message.cant.save", "Can't Save"), JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JVGPaneInternalFrame frame = (JVGPaneInternalFrame) SwingUtilities.getAncestorOfClass(JVGPaneInternalFrame.class, JVGEditPane.this);
			if (frame != null) {
				return frame.saveAs();
			}
		}

		return null;
	}

	public File saveAs() {
		return save(getRoot().getChildren());
	}

	public File saveSelection() {
		JVGComponent[] selection = getSelectionManager().getSelection();
		Arrays.sort(selection, new ComponentOrderComparator());

		return save(selection);
	}

	private String fileName = null;

	public File save(JVGComponent[] components) {
		JFileChooser chooser = new JFileChooser(fileName);
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null) {
				try {
					save(file, components);
					fileName = file.getAbsolutePath();
					return file;
				} catch (IOException exc) {
					exc.printStackTrace();
				} catch (JVGParseException exc) {
					exc.printStackTrace();
				}
			}
		}
		return null;
	}

	public void save(File file) throws IOException, JVGParseException {
		save(file, getRoot().getChildren());
	}

	public void save(File file, JVGComponent[] components) throws IOException, JVGParseException {
		DocumentFormat documentFormat = this.documentFormat;
		String ext = file.getName().toLowerCase();
		if (ext.endsWith("." + DocumentFormat.svg)) {
			documentFormat = DocumentFormat.svg;
		} else if (ext.endsWith("." + DocumentFormat.jvg) || ext.endsWith(".xml")) {
			documentFormat = DocumentFormat.jvg;
		} else {
			file = new File(file.getParent(), file.getName() + "." + DocumentFormat.jvg);
		}

		FileOutputStream os = new FileOutputStream(file);

		JVGBuilder builder = JVGBuilder.create(documentFormat);
		builder.setDocument(this);
		builder.build(components, os);
		os.close();
		os.flush();

		getRoot().invalidate();
		repaint();

		setDocumentFormat(documentFormat);
	}

	public String getDocument() throws IOException, JVGParseException {
		JVGBuilder builder = JVGBuilder.create(documentFormat);
		builder.setDocument(this);
		return builder.build(getRoot().getChildren(), "utf-8");
	}

	public void load(File file) throws JVGParseException {
		JVGParser parser = new JVGParser(getEditorKit().getFactory());
		parser.setPane(this);

		JVGRoot root = parser.parse(file);
		editor.getResources().addResources(parser.getResources());

		parser.init(this);
		setRoot(root);
		setDocumentFormat(parser.getFormat());
		root.invalidate();

		updateDocumentSize();
		updateTransform();
		repaint();

		fileName = file.getAbsolutePath();
	}

	public void load(String document) throws JVGParseException {
		JVGParser parser = new JVGParser(getEditorKit().getFactory());

		JVGRoot root = parser.parse(document);
		editor.getResources().addResources(parser.getResources());

		parser.init(this);
		setRoot(root);
		setDocumentFormat(parser.getFormat());
		root.invalidate();

		updateDocumentSize();
		updateTransform();
		repaint();
	}

	public static interface InputTextHandler {
		public void setValue(String value);
	}

	private class InputComponent {
		JTextField field = new JTextField() {
			@Override
			public void paintComponent(Graphics g) {
				g.setColor(Color.lightGray);
				for (int i = -getHeight(); i < getWidth(); i += 3) {
					g.drawLine(i, 0, i + getHeight(), getHeight());
				}

				g.setColor(Color.white);
				g.fillRect(4, 4, getWidth() - 8, getHeight() - 8);
				g.setColor(Color.lightGray);
				g.drawRect(4, 4, getWidth() - 8, getHeight() - 8);
				super.paintComponent(g);
			}
		};

		JVGComponent c;

		InputTextHandler handler;

		public InputComponent() {
			field.setOpaque(false);
			field.setBorder(new EmptyBorder(4, 4, 4, 4));
			field.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						remove();
					}
				}
			});
			field.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					invalidate();
					validate();
					repaint();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					invalidate();
					validate();
					repaint();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
				}
			});

			field.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					remove();
					handler.setValue(field.getText());
				}
			});
		}

		public void remove() {
			inputs.remove(this);
			JVGEditPane.this.remove(field);
			JVGEditPane.this.invalidate();
			JVGEditPane.this.validate();
			JVGEditPane.this.repaint();
		}
	}

	private List<InputComponent> inputs = new ArrayList<>();

	public void showInputText(JVGComponent c, String text, InputTextHandler handler) {
		for (InputComponent i : inputs) {
			remove(i.field);
		}
		inputs.clear();

		InputComponent i = new InputComponent();
		i.c = c;
		i.handler = handler;
		i.field.setText(text);
		inputs.add(i);
		add(i.field);
		revalidate();
		repaint();

		i.field.requestFocus();
		i.field.selectAll();
		i.field.setCaretPosition(0);
	}

	// TODO
	//	public void paint(Graphics g) {
	//		super.paint(g);
	//		if (inputs.size() > 0) {
	//			Graphics2D g2d = getGraphics2D(g);
	//			for (InputComponent i : inputs) {
	//
	//			}
	//		}
	//	}

	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}

	public void setDocumentFormat(DocumentFormat documentFormat) {
		DocumentFormat oldValue = this.documentFormat;
		this.documentFormat = documentFormat;
		firePropertyChange("document-format", oldValue, documentFormat);
	}

	class JVGEditorPaneLayout implements LayoutManager {
		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(0, 0);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(0, 0);
		}

		@Override
		public void layoutContainer(Container parent) {
			for (InputComponent i : inputs) {
				Dimension s = i.field.getPreferredSize();
				if (s.width < 100) {
					s.width = 100;
				}
				if (s.width > 600) {
					s.width = 600;
				}
				if (s.height < 26) {
					s.height = 26;
				}

				Rectangle r = getTransform().createTransformedShape(i.c.getRectangleBounds()).getBounds();
				int x = (int) r.getCenterX() - s.width / 2;
				int y = (int) r.getCenterY() - s.height / 2;

				i.field.setBounds(x, y, s.width, s.height);
			}
		}
	}
}
