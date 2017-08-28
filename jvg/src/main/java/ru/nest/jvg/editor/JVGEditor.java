package ru.nest.jvg.editor;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.IntegerTextField;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwitchPanel;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.layout.VerticalBagLayout;
import javax.swing.menu.WMenuItem;

import org.jdom2.Element;

import com.sun.java.swing.plaf.motif.MotifInternalFrameUI;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGCopyContext;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.JVGTransferHandler;
import ru.nest.jvg.JVGTransferable;
import ru.nest.jvg.complexshape.ComplexShapeParser;
import ru.nest.jvg.editor.clipboard.JVGClipboard;
import ru.nest.jvg.editor.clipboard.JVGClipboardContext;
import ru.nest.jvg.editor.clipboard.JVGClipboardPanel;
import ru.nest.jvg.editor.editoraction.AddShapeEditorAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.event.JVGEvent;
import ru.nest.jvg.event.JVGFocusEvent;
import ru.nest.jvg.event.JVGListener;
import ru.nest.jvg.event.JVGSelectionEvent;
import ru.nest.jvg.event.JVGSelectionListener;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParser;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.shape.JVGComplexShape;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.paint.Draw;

public class JVGEditor extends JFrame {
	private static final long serialVersionUID = 6614457751885122625L;

	public final static String VERSION = "1.0";

	public static String fileName = null;

	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private JDesktopPane desktop = new JDesktopPane();

	private SwitchPanel shapesTabsPanel = new SwitchPanel();

	private File configDir;

	private int editorCloseOperation = JFrame.EXIT_ON_CLOSE;

	public JDesktopPane getDesktop() {
		return desktop;
	}

	// current values
	public boolean getCurrentAntialias() {
		return editorActions.actionAntialias.isSelected();
	}

	public int getCurrentShadowType() {
		return editorActions.btnShadowType.getType();
	}

	public float getCurrentOutlineWidth() {
		Float width = (Float) editorActions.cmbOutlineWidth.getSelectedItem();
		if (width != null) {
			return width;
		} else {
			return 1f;
		}
	}

	public Draw getCurrentOutlineDraw() {
		return editorActions.btnOutlineColor.getDraw();
	}

	public Draw getCurrentFillDraw() {
		return editorActions.btnFillColor.getDraw();
	}

	public float[] getCurrentOutlinePattern() {
		return editorActions.cmbOutlinePattern.getDashArray();
	}

	public Draw getCurrentShadowDraw() {
		return editorActions.btnShadowColor.getDraw();
	}

	public Draw getCurrentEndingsDraw() {
		return editorActions.btnEndingsColor.getDraw();
	}

	public int getCurrentEndingsType() {
		return editorActions.cmbEndingsPattern.getType();
	}

	public int getCurrentFillTransparency() {
		Integer transparency = (Integer) editorActions.btnFillTransparency.getSelectedItem();
		if (transparency != null) {
			return transparency;
		} else {
			return 255;
		}
	}

	private JPanel pnlInfo = new JPanel();

	private JLabel lblCoord = new JLabel();

	private JLabel lblSelection = new JLabel();

	private ImageIcon icon = new ImageIcon(JVGEditor.class.getResource("img/icon.png"));

	private static JVGEditor editorInstance;

	private JVGEditorActions editorActions;

	private JVGClipboard clipboard;

	public static JVGEditor getInstance() {
		return editorInstance;
	}

	public JVGEditor() {
		this(null);
	}

	public JVGEditor(File configDir) {
		if (configDir == null) {
			configDir = new File(System.getProperty("user.home"), "jvg");
		}
		configDir.mkdirs();

		clipboard = new JVGClipboard(configDir);
		clipboardPanel = new JVGClipboardPanel(clipboard);

		editorInstance = this;
		editorActions = new JVGEditorActions(this);

		setTitle(lm.getValue("title", "Schema Editor"));
		setIconImage(icon.getImage());

		final JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);

		desktop.setBackground(Color.gray);
		desktop.setBorder(BorderFactory.createLoweredBevelBorder());
		contentPane.add(desktop, BorderLayout.CENTER);

		editorActions.init();
		contentPane.add(editorActions.toolBars, BorderLayout.NORTH);

		pnlInfo.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		lblCoord.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelection.setHorizontalAlignment(SwingConstants.CENTER);
		addInfo(lblCoord, 150);
		addInfo(lblSelection, 200);
		contentPane.add(pnlInfo, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				editorOpened();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				checkOnSave();
			}
		});

		Toolkit.getDefaultToolkit().setDynamicLayout(true);

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.addFlavorListener(new FlavorListener() {
			private Set<Integer> hash = new HashSet<Integer>();

			@Override
			public void flavorsChanged(FlavorEvent e) {
				try {
					Clipboard clipboard = (Clipboard) e.getSource();
					for (DataFlavor flavor : clipboard.getAvailableDataFlavors()) {
						if (JVGCopyContext.class.equals(flavor.getRepresentationClass())) {
							editorActions.actionPaste.setEnabled(true);
							try {
								JVGCopyContext data = (JVGCopyContext) clipboard.getData(flavor);
								if (data.getData() != null) {
									if (hash.contains(data.getData().hashCode())) {
										// TODO ???
										hash.remove(data.getData().hashCode());
										return;
									}
									hash.add(data.getData().hashCode());

									showClipboard(clipboardFrame != null && clipboardFrame.isVisible());
									JVGEditor.this.clipboard.add(new JVGClipboardContext(data.getData(), data.getWidth(), data.getHeight()));
								}
							} catch (Exception exc) {
								exc.printStackTrace();
							}
							return;
						}
					}
					editorActions.actionPaste.setEnabled(false);
				} catch (Exception exc) {
				}
			}
		});

		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				if (JVGEditor.this.isFocused()) {
					KeyEvent ke = (KeyEvent) event;
					if (ke.getID() == KeyEvent.KEY_RELEASED && ke.getKeyCode() == KeyEvent.VK_TAB) {
						if (ke.isControlDown()) {
							showNext(true);
						} else if (ke.isShiftDown()) {
							showNext(false);
						}
					}
				}
			}
		}, AWTEvent.KEY_EVENT_MASK);
	}

	protected void editorOpened() {
		showShapes(true);
		createNew(documentWidth, documentHeight);
		showZoomPreview(true);
		showResources(true);
		editorActions.loadEditorConfig();
	}

	public JVGEditorActions getEditorActions() {
		return editorActions;
	}

	public void showNext(boolean direct) {
		JVGPaneInternalFrame frame = getCurrentFrame();
		if (frame != null) {
			JInternalFrame[] frames = desktop.getAllFramesInLayer(1);
			if (frames.length <= 1) {
				return;
			}

			Arrays.sort(frames);

			int index = -1;
			for (int i = 0; i < frames.length; i++) {
				if (frame == frames[i]) {
					index = i;
					break;
				}
			}

			if (index != -1) {
				if (direct) {
					index = index != frames.length - 1 ? index + 1 : 0;
				} else {
					index = index != 0 ? index - 1 : frames.length - 1;
				}

				frames[index].toFront();
				try {
					frames[index].setSelected(true);
				} catch (PropertyVetoException ex) {
				}
			}
		}
	}

	private void addInfo(JComponent c, int width) {
		Dimension s = c.getPreferredSize();
		s.width = width;
		s.height = 18;
		c.setPreferredSize(s);
		c.setBorder(new LineBorder(Color.white, 1));
		c.setFont(c.getFont().deriveFont(Font.PLAIN, 12));
		pnlInfo.add(c);
	}

	protected int documentWidth = 700, documentHeight = 500;

	public JVGPaneInternalFrame createNew() {
		Dimension dimension = chooseDocumentSize();
		if (dimension != null) {
			return createNew(dimension.width, dimension.height);
		} else {
			return null;
		}
	}

	private Dimension chooseDocumentSize() {
		JTextField txtWidth = new IntegerTextField(Integer.toString(documentWidth), 5);
		txtWidth.setHorizontalAlignment(SwingConstants.RIGHT);

		JTextField txtHeight = new IntegerTextField(Integer.toString(documentHeight), 5);
		txtHeight.setHorizontalAlignment(SwingConstants.RIGHT);

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		pnl.add(new JLabel(lm.getValue("create.width", "Width") + " "));
		pnl.add(txtWidth);
		pnl.add(new JLabel("   " + lm.getValue("create.height", "Height") + " "));
		pnl.add(txtHeight);

		while (true) {
			if (JOptionPane.showConfirmDialog(this, pnl, lm.getValue("create.title", "Create new..."), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				try {
					documentWidth = Integer.parseInt(txtWidth.getText());
					documentHeight = Integer.parseInt(txtHeight.getText());
					if (documentWidth <= 1 || documentHeight <= 1) {
						// invalid values
						continue;
					}

					return new Dimension(documentWidth, documentHeight);
				} catch (NumberFormatException exc) {
					// invalid format
					continue;
				}
			} else {
				return null;
			}
		}
	}

	public JVGPaneInternalFrame[] getAllDocuments() {
		List<JVGPaneInternalFrame> list = new ArrayList<JVGPaneInternalFrame>();
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				list.add((JVGPaneInternalFrame) frame);
			}
		}
		return list.toArray(new JVGPaneInternalFrame[0]);
	}

	private boolean checkOnSave() {
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				if (!checkOnSave((JVGPaneInternalFrame) frame)) {
					return false;
				}
				frame.setVisible(false);
				frame.dispose();
				switcherPanel.removeSchema((JVGPaneInternalFrame) frame);
			}
		}
		return true;
	}

	private boolean checkOnSave(JVGPaneInternalFrame frame) {
		if (frame.isUnsaved()) {
			String message = lm.getValue("editor.message.check.unsaved.schema", "Schema '{!name}' is not saved.\nSave it?\nTo cancel closing window press 'Cancel'.");
			message = message.replaceAll("\\{\\!name\\}", frame.getTitle());

			int option = JOptionPane.showConfirmDialog(null, message, lm.getValue("attention", "Attention"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			switch (option) {
				case JOptionPane.YES_OPTION:
					save();
					frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					setDefaultCloseOperation(editorCloseOperation);
					break;

				case JOptionPane.NO_OPTION:
					frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					setDefaultCloseOperation(editorCloseOperation);
					break;

				case JOptionPane.CANCEL_OPTION:
					frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
					setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
					return false;
			}
		}

		return true;
	}

	// TOO fix name
	protected void processShemaFrameActivation(JVGPaneInternalFrame frame) {
		setCurrentFrame(frame);
		updateSaveButtons();
		editorActions.updateUndoRedoButtons();
		editorActions.updateDependedOnSelectionAndFocus();
		switcherPanel.selectSchema(frame);

		// update depedencies on toolbars
		int selectionType = frame.getPane().getRoot().getSelectionType();
		editorActions.cmbSelectionType.setSelectedIndex(selectionType);
	}

	protected void processSchemaClosing(JVGPaneInternalFrame frame) {
		processSchemaClosing(frame, true);
	}

	protected void processSchemaClosing(JVGPaneInternalFrame frame, boolean checkOnSave) {
		if (checkOnSave && !checkOnSave(frame)) {
			return;
		}

		switcherPanel.removeSchema(frame);

		if (currentFrame == frame) {
			setCurrentFrame(null);
			updateSaveButtons();
			editorActions.updateUndoRedoButtons();
			editorActions.updateDependedOnSelectionAndFocus();

			// clear info bar
			clearInfoBar();
		}
	}

	private JVGPaneInternalFrame currentFrame = null;

	private void setCurrentFrame(JVGPaneInternalFrame currentFrame) {
		this.currentFrame = currentFrame;
		zoomPreviewPane.setPane(currentFrame != null ? currentFrame.getPane() : null);
	}

	protected JVGPaneInternalFrame createInternalFrame(int documentWidth, int documentHeight) {
		return new JVGPaneInternalFrame(this, documentWidth, documentHeight);
	}

	public JVGPaneInternalFrame createNew(int documentWidth, int documentHeight) {
		final JVGPaneInternalFrame frame = createInternalFrame(documentWidth, documentHeight);
		frame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameOpened(InternalFrameEvent e) {
				updateSaveButtons();
			}

			@Override
			public void internalFrameActivated(InternalFrameEvent e) {
				processShemaFrameActivation(frame);
			}

			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				processSchemaClosing(frame);
			}
		});

		final JVGEditPane pane = frame.getPane();
		pane.setGridAlign(isGridAlign());
		pane.setDrawGrid(isDrawGrid());
		pane.setConnectionsEnabled(isConnectionsEnabled());
		pane.setDrawGridAbove(isDrawGridAbove());

		pane.setLastUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				// can't invoke update methods as isUnsaved() may return old
				// value

				editorActions.actionSave.setEnabled(true);
				editorActions.actionSaveAs.setEnabled(true);
				editorActions.actionSaveAll.setEnabled(true);

				editorActions.updateUndoRedoButtons();
			}
		});

		pane.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if ("zoom".equals(e.getPropertyName())) {
					editorActions.cmbZoom.repaint();
				}
			}
		});

		// pane keyboard actions
		ActionMap am = pane.getActionMap();
		InputMap im = pane.getInputMap();

		am.put("save-to-file", new AbstractAction("save-to-file") {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "save-to-file");

		// undo / redo
		am.put("undo", new AbstractAction("undo") {
			@Override
			public void actionPerformed(ActionEvent e) {
				undo(e);
			}
		});
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK), "undo");

		am.put("redo", new AbstractAction("redo") {
			@Override
			public void actionPerformed(ActionEvent e) {
				redo(e);
			}
		});
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK | Event.SHIFT_MASK), "redo");

		JVGSelectionModel selectionModel = pane.getSelectionManager();
		selectionModel.addSelectionListener(new JVGSelectionListener() {
			@Override
			public void selectionChanged(JVGSelectionEvent event) {
				editorActions.updateDependedOnSelectionAndFocus();
			}
		});

		pane.addJVGListener(new JVGListener() {
			@Override
			public void eventOccured(JVGEvent e) {
				if (e.getID() == JVGFocusEvent.FOCUS_GAINED || e.getID() == JVGFocusEvent.FOCUS_LOST) {
					editorActions.updateDependedOnSelectionAndFocus();
				}
			}
		});

		pane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				lblSelection.setText("");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				clearInfoBar();
			}
		});

		pane.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				updateCoordInfo(e.getX(), e.getY());
				updateSelectionInfo();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				updateCoordInfo(e.getX(), e.getY());
			}

			private NumberFormat f = new DecimalFormat("0.##");

			private void updateCoordInfo(int x, int y) {
				Insets insets = pane.getDocumentInsets();
				Dimension size = pane.getDocumentSize();
				double zoom = pane.getZoom();
				double zx = x / zoom - insets.left;
				double zy = insets.top + size.height - y / zoom;
				lblCoord.setText(lm.getValue("pane.info.pointer.x", "x=") + f.format(zx) + " " + lm.getValue("pane.info.pointer.y", "y=") + f.format(zy));
			}

			private void updateSelectionInfo() {
				Rectangle2D bounds = pane.getRoot().getSelectionBounds();
				if (bounds != null && (bounds.getWidth() != 0 || bounds.getHeight() != 0)) {
					Dimension size = pane.getDocumentSize();
					lblSelection.setText(lm.getValue("pane.info.selection.x", "x=") + (int) bounds.getX() + " " + lm.getValue("pane.info.selection.y", "y=") + (int) (size.height - bounds.getY() - bounds.getHeight()) + " " + lm.getValue("pane.info.selection.width", "w=") + (int) bounds.getWidth() + " " + lm.getValue("pane.info.selection.height", "h=") + (int) bounds.getHeight());
				} else {
					lblSelection.setText("");
				}
			}
		});

		frame.getScrollPane().getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (isShowZoomPreview()) {
					zoomPreviewPane.update();
				}
			}
		});
		pane.addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				if (isShowZoomPreview()) {
					zoomPreviewPane.update();
				}
			}
		});

		if (shapesFrame != null) {
			frame.setBounds(shapesFrame.getWidth(), 0, desktop.getWidth() - shapesFrame.getWidth() - 200, desktop.getHeight());
		} else {
			frame.setBounds(0, 0, desktop.getWidth(), desktop.getHeight());
		}
		desktop.add(frame);
		desktop.setLayer(frame, 1);
		frame.setVisible(true);

		switcherPanel.addSchema(frame);

		return frame;
	}

	public void clearInfoBar() {
		lblSelection.setText("");
		lblCoord.setText("");
	}

	public JVGPaneInternalFrame getCurrentFrame() {
		return currentFrame;
	}

	public JVGEditPane getCurrentPane() {
		JVGPaneInternalFrame frame = getCurrentFrame();
		if (frame != null) {
			return frame.getPane();
		} else {
			return null;
		}
	}

	public JVGShape getFocusedShape() {
		JVGPane pane = getCurrentPane();
		if (pane != null) {
			JVGComponent focusOwner = pane.getFocusOwner();
			if (focusOwner instanceof JVGShape) {
				return (JVGShape) focusOwner;
			}
		}
		return null;
	}

	private int pathAppendType = PathIterator.SEG_LINETO;

	public int getPathAppendType() {
		return pathAppendType;
	}

	public void setPathAppendType(int pathAppendType) {
		this.pathAppendType = pathAppendType;
		if (editorActions.cmbCurveTo.getSelectedIndex() != pathAppendType) {
			editorActions.cmbCurveTo.setSelectedIndex(pathAppendType);
		}
	}

	protected JInternalFrame shapesFrame;

	private JPanel shapesPanel;

	public JInternalFrame getShapesFrame() {
		return shapesFrame;
	}

	public boolean isShowShapes() {
		return shapesFrame != null && shapesFrame.isVisible();
	}

	public void showShapes(boolean show) {
		if (shapesFrame != null) {
			if (show) {
				shapesFrame.toFront();
				shapesFrame.setVisible(true);
			} else {
				shapesFrame.setVisible(false);
			}
			editorActions.updateViewMenus();
			return;
		}

		if (!show) {
			return;
		}

		shapesPanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				Util.paintFormBackground(g, getWidth(), getHeight());
				super.paintComponent(g);
			}
		};
		shapesPanel.setOpaque(false);
		shapesPanel.setLayout(new VerticalBagLayout());

		addShape("line");
		addShape("doubleline");
		addShape("quadcurve");
		addShape("cubiccurve");

		addShape("circle");
		addShape("ellipse");
		addShape("ring");

		addShape("square");
		addShape("rectangle");
		addShape("romb");
		addShape("rectangle3d");
		addShape("roundrectangle");
		addShape("quadrangle");

		addShape("frame");
		addShape("triangle");
		addShape("righttriangle");
		addShape("parallelogramm");
		addShape("trapeze");
		addShape("pentagon");
		addShape("star");
		addShape("hexagon");
		addShape("cross");
		addShape("cylinder");

		addShape("arrow");
		addShape("doublearrow");
		addShape("rightarrow");
		addShape("lightning");
		addShape("thickline");
		addShape("rightangle");
		addShape("angle");

		addShape("doubletreesquare");
		addShape("doubletreesloped");
		addShape("roundangle");

		addShapesPanel(lm.getValue("shapes.common-shapes", "Standard"), shapesPanel);

		shapesFrame = new JInternalFrame(lm.getValue("shapes.title", "Shapes"), true, true, false, true);
		desktop.add(shapesFrame);
		desktop.setLayer(shapesFrame, 2);

		shapesFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		shapesFrame.setContentPane(shapesTabsPanel);
		shapesFrame.setLocation(0, 0);
		shapesFrame.setSize(new Dimension(250, desktop.getHeight() - 200));
		shapesFrame.setFocusable(false);
		shapesFrame.setRequestFocusEnabled(false);
		shapesFrame.setFocusCycleRoot(true);
		shapesFrame.setUI(new MotifInternalFrameUI(shapesFrame));
		shapesFrame.setVisible(true);

		editorActions.updateViewMenus();
	}

	public void addShapesPanel(String tabName, Component c) {
		JScrollPane scroll = new JScrollPane(c);
		shapesTabsPanel.addTab(tabName, scroll);
	}

	private void addShape(String name) {
		JButton btn = getComplexShapeButton(name);
		if (btn != null) {
			shapesPanel.add(btn, new GridBagConstraints(0, shapesPanel.getComponentCount(), 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 3, 2, 3), 0, 0));
		} else {
			System.err.println("Can't add shape button '" + name + "'!");
		}
	}

	private ShapeCreator creator = null;

	public ShapeCreator getCreator() {
		return creator;
	}

	public void setCreator(ShapeCreator creator) {
		this.creator = creator;
	}

	public void setMouse() {
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				((JVGPaneInternalFrame) frame).getPane().setEditor(null);
			}
		}
		creator = null;
	}

	private JButton getComplexShapeButton(String name) {
		return getComplexShapeButton(name, "/ru/nest/jvg/complexshape/sources/" + name + ".xml");
	}

	private JButton getComplexShapeButton(String name, String path) {
		URL url = null;
		if (name != null) {
			try {
				url = JVGEditor.class.getResource(path);
			} catch (Exception exc) {
				try {
					url = new URL(path);
				} catch (Exception exc2) {
				}
			}
		}

		if (url == null) {
			return null;
		}

		Icon i = null;
		try {
			i = new ImageIcon(JVGEditor.class.getResource("/ru/nest/jvg/complexshape/sources/img/" + name + ".png"));
		} catch (Exception exc) {
			System.err.println("Can't find shape icon: " + name);
		}

		final Icon icon = i;
		try {
			final ComplexShapeParser ctx = new ComplexShapeParser();
			ctx.parse(url);

			final AddShapeEditorAction action = new AddShapeEditorAction("add", new ShapeCreator() {
				@Override
				public JVGShape create(JVGPane pane) {
					try {
						JVGComplexShape shape = pane.getEditorKit().getFactory().createComponent(JVGComplexShape.class, ctx);
						return shape;
					} catch (Exception exc) {
						return null;
					}
				}
			});

			class ShapeButton extends JButton {
				private JVGTransferHandler th;

				private boolean isSelected = false;

				private boolean isOver = false;

				private JLabel lbl;

				private boolean exported = false;

				public ShapeButton(final Icon icon) {
					super(icon);

					setPreferredSize(new Dimension(44, 44));
					setHorizontalAlignment(SwingConstants.LEFT);
					setOpaque(false);
					setContentAreaFilled(false);
					setBorderPainted(false);

					if (icon != null) {
						BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
						icon.paintIcon(null, img.getGraphics(), 0, 0);

						th = new JVGTransferHandler();
						th.setDragImage(img);
						setTransferHandler(th);
					}

					//getDropTarget().setActive(true);
					DragSource source = DragSource.getDefaultDragSource();
					source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, new DragGestureListener() {
						@Override
						public void dragGestureRecognized(DragGestureEvent dge) {
							getCurrentPane().setEditor(null);
							isSelected = false;
							repaint();
							dge.startDrag(null, new JVGTransferable(ctx.getURL()));
						}
					});

					addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							if (isEnabled() && e.getButton() == MouseEvent.BUTTON1) {
								isSelected = true;
								repaint();
							}
						}

						@Override
						public void mouseReleased(MouseEvent e) {
							isSelected = false;
							exported = false;
							if (isEnabled()) {
								repaint();
								action.doAction();
							}
						}

						@Override
						public void mouseEntered(MouseEvent e) {
							if (isEnabled()) {
								isOver = true;
								repaint();
							}
						}

						@Override
						public void mouseExited(MouseEvent e) {
							isOver = false;
							if (isEnabled()) {
								repaint();
							}
						}
					});
					addMouseMotionListener(new MouseAdapter() {
						@Override
						public void mouseDragged(MouseEvent e) {
							if (isSelected) {
								if (!exported) {
									exported = true;
									if (th != null) {
										th.setDragImageOffset(new Point(icon.getIconWidth() / 2, icon.getIconHeight() / 2));
										th.exportAsDrag((JComponent) e.getComponent(), e, TransferHandler.COPY);
									}
								}
							}
						}
					});

					lbl = new JLabel();
					lbl.setVerticalAlignment(SwingConstants.TOP);
					lbl.setText("<html><body><b>" + ctx.getName() + "<b><br><font color='#707070' size='2'>" + ctx.getDescription() + "</font></body></html>");
					setToolTipText(lbl.getText());
				}

				@Override
				public void paint(Graphics g) {
					int w = 0;
					int h = 0;
					if (icon != null) {
						w = icon.getIconWidth();
						h = icon.getIconHeight();
					}
					int W = getWidth();
					int H = getHeight();
					int x = 5;
					int y = (H - h) / 2;

					if (isSelected) {
						x++;
						y++;
					}

					if (!isSelected) {
						Util.drawGlassRect(g, W, H);
						g.setColor(new Color(205, 205, 205));
						g.drawLine(0, H - 1, W, H - 1);
					} else {
						g.setColor(new Color(220, 220, 220));
						g.fillRect(0, 0, W, H);
					}
					if (isSelected || isOver) {
						g.setColor(new Color(205, 205, 205));
						g.drawRect(0, 0, W - 1, H - 1);
					}
					if (icon != null) {
						icon.paintIcon(this, g, x, y);
					}

					lbl.setBounds(x + w + 5, 0, W - w - 5, 1000);
					g.clipRect(x + w + 5, 2, W - w - 5, H - 4);
					g.translate(x + w + 5, 2);
					lbl.paint(g);
				}
			}
			return new ShapeButton(icon);
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}

	public void open() {
		JFileChooser chooser = new JFileChooser(fileName);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null) {
				if (open(file) != null) {
					fileName = file.getAbsolutePath();
				}
			}
		}
	}

	private JVGPaneInternalFrame _open(File file) {
		JVGComponent.count = 0;

		JVGPaneInternalFrame frame = null;
		try {
			frame = createNew(0, 0);
			frame.load(file);

			//			if (!frame.getPane().isDocumentSizeSet()) {
			//				Dimension dimension = chooseDocumentSize();
			//				if (dimension != null) {
			//					frame.getPane().setDocumentSize(dimension.width, dimension.height);
			//					frame.centrate();
			//				}
			//			}
		} catch (JVGParseException exc) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(null, "Can't open. Error: " + exc.toString(), "Error", JOptionPane.ERROR_MESSAGE);

			frame = null;
		}

		System.out.println("LOADED COMPONENTS> count=" + JVGComponent.count);
		return frame;
	}

	public JVGPaneInternalFrame open(String title, String document) {
		JVGComponent.count = 0;
		JVGPaneInternalFrame frame = null;
		try {
			frame = createNew(0, 0);
			frame.load(title, document);
		} catch (JVGParseException exc) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(null, "Can't open. Error: " + exc.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			frame = null;
		}
		System.out.println("LOADED COMPONENTS> count=" + JVGComponent.count);
		return frame;
	}

	public JVGPaneInternalFrame open(final File file) {
		final JVGPaneInternalFrame frame = _open(file);
		if (frame == null) {
			return null;
		}

		// add menu
		final WMenuItem menuOpenRenentFile = new WMenuItem(frame.getTitle());
		frame.addPropertyChangeListener(JInternalFrame.TITLE_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				menuOpenRenentFile.setText(frame.getTitle());
			}
		});
		menuOpenRenentFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_open(file) == null) {
					editorActions.menuRecent.remove(menuOpenRenentFile);
				}
			}
		});
		editorActions.menuRecent.setEnabled(true);
		editorActions.menuRecent.add(menuOpenRenentFile);
		return frame;
	}

	public void export() {
		JVGEditPane pane = getCurrentPane();
		if (pane != null) {
			JFileChooser chooser = new JFileChooser(fileName);
			if (chooser.showOpenDialog(pane) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (file != null) {
					fileName = file.getAbsolutePath();

					try {
						IntegerTextField txtWidth = new IntegerTextField((int) pane.getDocumentSize().getWidth(), 5, false);
						IntegerTextField txtHeight = new IntegerTextField((int) pane.getDocumentSize().getHeight(), 5, false);

						JPanel pnl = new JPanel();
						pnl.setLayout(new GridBagLayout());
						pnl.add(new JLabel(lm.getValue("dialog.save-image.width", "Width: ")), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						pnl.add(txtWidth, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						pnl.add(new JLabel(lm.getValue("dialog.save-image.height", "Height: ")), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						pnl.add(txtHeight, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

						JOptionPane.showMessageDialog(this, pnl, lm.getValue("dialog.save-image.title", "Save image settings"), JOptionPane.PLAIN_MESSAGE);

						int width = Integer.parseInt(txtWidth.getText());
						int height = Integer.parseInt(txtHeight.getText());

						ImageIcon image = Util.getSnapshot(pane, width, height, 0);
						Util.saveImage(file, image.getImage());
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		}
	}

	public void load(String xml) throws JVGParseException {
		load(getCurrentPane(), xml);
	}

	public void load(JVGEditPane pane, String xml) throws JVGParseException {
		if (pane != null && xml != null) {
			JVGParser parser = new JVGParser(pane.getEditorKit().getFactory());
			JVGRoot root = parser.parse(xml);
			pane.setRoot(root);
			if (parser.getDocumentSize() != null) {
				pane.setDocumentSize(parser.getDocumentSize());
			}

			getResources().addResources(parser.getResources());

			root.invalidate();
			pane.repaint();
		}
	}

	public void load(JVGEditPane pane, Element rootElement) throws JVGParseException {
		if (pane != null && rootElement != null) {
			JVGParser parser = new JVGParser(pane.getEditorKit().getFactory());
			JVGRoot root = parser.parse(rootElement);
			pane.setRoot(root);

			getResources().addResources(parser.getResources());

			root.invalidate();
			pane.repaint();
		}
	}

	public void exit() {
		System.exit(0);
	}

	public void editXML() {
		JVGEditPane pane = getCurrentPane();
		if (pane != null) {
			JVGSourceEditor sourceEditor = new JVGSourceEditor(this, pane);
			sourceEditor.setVisible(true);
		}
	}

	public void editDOM() {
		JVGEditPane pane = getCurrentPane();
		if (pane != null) {
			JVGDomEditor domEditor = new JVGDomEditor(this, pane);
			domEditor.setVisible(true);
		}
	}

	public void chooseDocumentColor() {
		JVGPaneInternalFrame frame = getCurrentFrame();
		if (frame != null) {
			Color color = JColorChooser.showDialog(this, lm.getValue("chooser.color.more.choose", "Choose color"), frame.getPane().getBackground());
			if (color != null) {
				frame.getPane().setBackground(color);
				getZoomPreviewPane().repaint();
			}
		}
	}

	public void close() {
		close(getCurrentFrame());
	}

	public void close(JVGPaneInternalFrame frame) {
		if (frame != null) {
			frame.dispose();
			frame.setVisible(false);
			processSchemaClosing(frame);
		}
	}

	public void closeAll(boolean checkOnSave) {
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				JVGPaneInternalFrame f = (JVGPaneInternalFrame) frame;
				processSchemaClosing(f, checkOnSave);
			}
		}
	}

	private boolean isFullScreen = false;

	public boolean isFullScreen() {
		return isFullScreen;
	}

	private int oldState;

	private Rectangle oldBounds;

	public void setFullScreen(boolean isFullScreen) {
		if (isFullScreen == this.isFullScreen) {
			return;
		}
		this.isFullScreen = isFullScreen;

		if (isFullScreen) {
			oldState = getExtendedState();
			oldBounds = getBounds();

			if (oldState != MAXIMIZED_BOTH) {
				setExtendedState(MAXIMIZED_BOTH);
			}

			editorActions.menuBar.setPreferredSize(new Dimension(0, 0));
		} else {
			setExtendedState(oldState);
			setBounds(oldBounds);
			editorActions.menuBar.setPreferredSize(null);
		}

		dispose();
		setUndecorated(isFullScreen);
		setVisible(true);
	}

	private boolean isGridAlign = true;

	public boolean isGridAlign() {
		return isGridAlign;
	}

	public void setGridAlign(boolean isGridAlign) {
		this.isGridAlign = isGridAlign;
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				((JVGPaneInternalFrame) frame).getPane().setGridAlign(isGridAlign);
			}
		}
	}

	public void resizeDocument() {
		JVGPaneInternalFrame frame = getCurrentFrame();
		if (frame != null) {
			Dimension size = chooseDocumentSize();
			if (size != null) {
				frame.getPane().setDocumentSize(size);
				frame.centrate();
				if (isShowZoomPreview()) {
					zoomPreviewPane.update();
				}
			}
		}
	}

	public void preview() {
		JVGPaneInternalFrame frame = getCurrentFrame();
		if (frame != null) {
			JVGPreviewFrame f = new JVGPreviewFrame(frame.getTitle());
			f.setSource(frame.getPane());
			f.setVisible(true);
		}
	}

	protected void about() {
		AboutFrame d = new AboutFrame();
		d.pack();
		d.setLocationRelativeTo(this);

		// TODO update
		// d.setVisible(true);
	}

	protected void undo(ActionEvent e) {
		JVGPane pane = getCurrentPane();
		if (pane != null) {
			if (pane.getUndoManager().canUndo()) {
				pane.getUndoAction().actionPerformed(e);

				if (isShowZoomPreview()) {
					zoomPreviewPane.update();
				}
			}
			editorActions.updateUndoRedoButtons();
		}
	}

	protected void redo(ActionEvent e) {
		JVGPane pane = getCurrentPane();
		if (pane != null) {
			if (pane.getUndoManager().canRedo()) {
				pane.getRedoAction().actionPerformed(e);

				if (isShowZoomPreview()) {
					zoomPreviewPane.update();
				}
			}
			editorActions.updateUndoRedoButtons();
		}
	}

	public void saveAll() {
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				((JVGPaneInternalFrame) frame).save();
			}
		}
		updateSaveButtons();
	}

	public void save() {
		JVGPaneInternalFrame frame = getCurrentFrame();
		if (frame != null) {
			frame.save();
		}
		updateSaveButtons();
	}

	public void saveAs() {
		if (getCurrentFrame() != null) {
			getCurrentFrame().saveAs();
		}
		updateSaveButtons();
	}

	public void saveSelection() {
		if (getCurrentFrame() != null) {
			getCurrentFrame().saveSelection();
		}
	}

	protected void updateSaveButtons() {
		// save current
		JVGPaneInternalFrame frame = getCurrentFrame();
		editorActions.actionSave.setEnabled(frame != null && frame.isUnsaved());
		editorActions.actionSaveAs.setEnabled(frame != null && frame.isUnsaved());

		// save all
		boolean allSaved = true;
		for (JInternalFrame f : desktop.getAllFramesInLayer(1)) {
			if (f instanceof JVGPaneInternalFrame) {
				allSaved &= !((JVGPaneInternalFrame) f).isUnsaved();
			}
		}
		editorActions.actionSaveAll.setEnabled(!allSaved);
	}

	private boolean showRules = true;

	public boolean isShowRules() {
		return showRules;
	}

	public void setShowRules(boolean showRules) {
		this.showRules = showRules;
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				((JVGPaneInternalFrame) frame).showRules(showRules);
			}
		}
	}

	public List<JVGEditPane> getAllPanes() {
		List<JVGEditPane> panes = new ArrayList<JVGEditPane>();
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				panes.add(((JVGPaneInternalFrame) frame).getPane());
			}
		}
		return panes;
	}

	private boolean drawGrid = true;

	public boolean isDrawGrid() {
		return drawGrid;
	}

	public void setDrawGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				((JVGPaneInternalFrame) frame).getPane().setDrawGrid(drawGrid);
			}
		}
	}

	private boolean isConnectionsEnabled = false;

	public boolean isConnectionsEnabled() {
		return isConnectionsEnabled;
	}

	public void setConnectionsEnabled(boolean isConnectionsEnabled) {
		this.isConnectionsEnabled = isConnectionsEnabled;
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				((JVGPaneInternalFrame) frame).getPane().setConnectionsEnabled(isConnectionsEnabled);
			}
		}
	}

	public void defineConnections(JVGEditPane pane) {
		if (pane != null) {
			pane.defineConnections();
		}
	}

	private boolean drawGridAbove = false;

	public boolean isDrawGridAbove() {
		return drawGridAbove;
	}

	public void setDrawGridAbove(boolean drawGridAbove) {
		this.drawGridAbove = drawGridAbove;
		for (JInternalFrame frame : desktop.getAllFramesInLayer(1)) {
			if (frame instanceof JVGPaneInternalFrame) {
				((JVGPaneInternalFrame) frame).getPane().setDrawGridAbove(drawGridAbove);
			}
		}
	}

	private JInternalFrame switcherFrame;

	private SwitcherPanel switcherPanel = new SwitcherPanel();

	public boolean isShowSwitcher() {
		return switcherFrame != null && switcherFrame.isVisible();
	}

	public void showSwitcher(boolean show) {
		if (switcherFrame != null) {
			if (show) {
				switcherFrame.toFront();
				switcherFrame.setVisible(true);
			} else {
				switcherFrame.setVisible(false);
			}
			editorActions.updateViewMenus();
			return;
		}

		if (!show) {
			return;
		}

		JScrollPane scroll = new JScrollPane(switcherPanel);

		switcherFrame = new JInternalFrame(lm.getValue("switcher.title", "Switcher"), true, false, false, true);
		desktop.add(switcherFrame);
		desktop.setLayer(switcherFrame, 2);

		switcherFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		switcherFrame.setContentPane(scroll);
		switcherFrame.setBounds(0, shapesFrame.getHeight(), shapesFrame.getWidth(), desktop.getHeight() - shapesFrame.getHeight());
		switcherFrame.setFocusable(false);
		switcherFrame.setRequestFocusEnabled(false);
		switcherFrame.setFocusCycleRoot(true);
		switcherFrame.setUI(new MotifInternalFrameUI(switcherFrame));
		switcherFrame.setVisible(true);
		editorActions.updateViewMenus();
	}

	private JInternalFrame zoomPreviewFrame;

	public JInternalFrame getZoomPreviewFrame() {
		return zoomPreviewFrame;
	}

	private ZoomPreviewPane zoomPreviewPane = new ZoomPreviewPane();

	public ZoomPreviewPane getZoomPreviewPane() {
		return zoomPreviewPane;
	}

	public boolean isShowZoomPreview() {
		return zoomPreviewFrame != null && zoomPreviewFrame.isVisible();
	}

	public void showZoomPreview(boolean show) {
		if (zoomPreviewFrame != null) {
			if (show) {
				zoomPreviewFrame.toFront();
				zoomPreviewFrame.setVisible(true);
			} else {
				zoomPreviewFrame.setVisible(false);
			}
			editorActions.updateViewMenus();
			return;
		}

		if (!show) {
			return;
		}

		zoomPreviewFrame = new JInternalFrame(lm.getValue("zoom-preview.title", "Zoom preview"), true, true, false, true);
		desktop.add(zoomPreviewFrame);
		desktop.setLayer(zoomPreviewFrame, 2);

		zoomPreviewFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		zoomPreviewFrame.setContentPane(zoomPreviewPane);
		zoomPreviewFrame.pack();
		if (shapesFrame != null) {
			zoomPreviewFrame.setBounds(0, shapesFrame.getHeight(), shapesFrame.getWidth(), 200);
		} else {
			zoomPreviewFrame.setBounds(0, desktop.getHeight() - 200, 250, 200);
		}
		zoomPreviewFrame.setFocusable(false);
		zoomPreviewFrame.setRequestFocusEnabled(false);
		zoomPreviewFrame.setFocusCycleRoot(true);
		zoomPreviewFrame.setUI(new MotifInternalFrameUI(switcherFrame));
		zoomPreviewFrame.setVisible(true);
		editorActions.updateViewMenus();
	}

	private JInternalFrame clipboardFrame;

	private JVGClipboardPanel clipboardPanel;

	public boolean isShowClipboard() {
		return clipboardFrame != null && clipboardFrame.isVisible();
	}

	public void showClipboard(boolean show) {
		if (clipboardFrame != null) {
			if (show) {
				clipboardFrame.toFront();
				clipboardFrame.setVisible(true);
			} else {
				clipboardFrame.setVisible(false);
			}
			editorActions.updateViewMenus();
			return;
		}

		if (!show) {
			return;
		}

		JScrollPane scroll = new JScrollPane(clipboardPanel);
		scroll.setPreferredSize(new Dimension(JVGClipboardContext.SNAPSHOT_WIDTH + (int) scroll.getVerticalScrollBar().getPreferredSize().getWidth(), 0));
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		clipboardFrame = new JInternalFrame(lm.getValue("clipboard.title", "Clipboard"), false, false, false, true);
		desktop.add(clipboardFrame);
		desktop.setLayer(clipboardFrame, 2);

		clipboardFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		clipboardFrame.setContentPane(scroll);
		clipboardFrame.pack();
		clipboardFrame.setBounds(desktop.getWidth() - 200, 0, 200, 350);
		clipboardFrame.setFocusable(false);
		clipboardFrame.setRequestFocusEnabled(false);
		clipboardFrame.setFocusCycleRoot(true);
		clipboardFrame.setUI(new MotifInternalFrameUI(switcherFrame));
		clipboardFrame.setResizable(true);
		clipboardFrame.setVisible(true);
		editorActions.updateViewMenus();

		clipboard.load();
	}

	private ResourceManager resourcesFrame;

	public boolean isShowResources() {
		return resourcesFrame != null && resourcesFrame.isVisible();
	}

	public void showResources(boolean show) {
		if (resourcesFrame != null) {
			if (show) {
				resourcesFrame.toFront();
				resourcesFrame.setVisible(true);
			} else {
				resourcesFrame.setVisible(false);
			}
			editorActions.updateViewMenus();
			return;
		}

		if (!show) {
			return;
		}

		resourcesFrame = new ResourceManager(getResources());
		desktop.add(resourcesFrame);
		desktop.setLayer(resourcesFrame, 2);

		resourcesFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		resourcesFrame.pack();
		resourcesFrame.setBounds(desktop.getWidth() - 200, 0, 200, desktop.getHeight());
		resourcesFrame.setFocusable(false);
		resourcesFrame.setRequestFocusEnabled(false);
		resourcesFrame.setFocusCycleRoot(true);
		resourcesFrame.setUI(new MotifInternalFrameUI(switcherFrame));
		resourcesFrame.setResizable(true);
		resourcesFrame.setVisible(true);
		editorActions.updateViewMenus();
	}

	private JVGFindFrame findFrame;

	public void find() {
		if (findFrame != null) {
			findFrame.setVisible(true);
			findFrame.toFront();
			return;
		}

		findFrame = new JVGFindFrame(this);
		desktop.add(findFrame);
		desktop.setLayer(findFrame, 2);

		findFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		findFrame.pack();
		findFrame.setLocation((desktop.getWidth() - findFrame.getWidth()) / 2, (desktop.getHeight() - findFrame.getHeight()) / 2);
		findFrame.setFocusable(false);
		findFrame.setClosable(true);
		findFrame.setRequestFocusEnabled(false);
		findFrame.setFocusCycleRoot(true);
		findFrame.setUI(new MotifInternalFrameUI(switcherFrame));
		findFrame.setVisible(true);
	}

	private JVGResources resources;

	public JVGResources getResources() {
		if (resources == null) {
			resources = new JVGResources();
		}
		return resources;
	}

	public static void main(String[] args) {
		Map<String, String> arguments = Util.getArguments(args);

		String lang = Util.getStringArgument(arguments, JVGLocaleManager.DEFUALT, "-l", "-lang");
		JVGLocaleManager.getInstance().setLanguage(lang);

		Util.installDefaultFont();

		JVGEditor e = createEditor();
		e.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		e.setVisible(true);
	}

	public static JVGEditor createEditor() {
		JVGEditor e = new JVGEditor();

		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(e.getGraphicsConfiguration());
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

		e.setBounds(screenInsets.left, screenInsets.top, size.width - screenInsets.left - screenInsets.right, size.height - screenInsets.top - screenInsets.bottom);
		e.setExtendedState(MAXIMIZED_BOTH);
		return e;
	}

	public File getConfigDir() {
		return configDir;
	}

	public void setConfigDir(File configDir) {
		this.configDir = configDir;
	}

	public JVGClipboard getClipboard() {
		return clipboard;
	}

	public int getEditorCloseOperation() {
		return editorCloseOperation;
	}

	public void setEditorCloseOperation(int editorCloseOperation) {
		this.editorCloseOperation = editorCloseOperation;
	}
}
