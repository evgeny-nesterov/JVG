package ru.nest.jvg.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.IconButton;
import javax.swing.IconToggleButton;
import javax.swing.ImageIcon;
import javax.swing.IntegerTextField;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SliderField;
import javax.swing.SwingConstants;
import javax.swing.ToolSeparator;
import javax.swing.ToolbarLayout;
import javax.swing.WComboBox;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.menu.EmptyIcon;
import javax.swing.menu.WBarMenu;
import javax.swing.menu.WCheckBoxMenuItem;
import javax.swing.menu.WMenu;
import javax.swing.menu.WMenuItem;
import javax.swing.menu.WSeparator;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.toolbar.WToolBar;

import ru.nest.fonts.FontComboBox;
import ru.nest.fonts.Fonts;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.action.ActionAreaAction;
import ru.nest.jvg.action.AddCustomActionAreaAction;
import ru.nest.jvg.action.AlfaLayerAction;
import ru.nest.jvg.action.AntialiasAction;
import ru.nest.jvg.action.EditPathAction;
import ru.nest.jvg.action.ExclusiveActionAreaAction;
import ru.nest.jvg.action.FlatPathAction;
import ru.nest.jvg.action.FontFamilyAction;
import ru.nest.jvg.action.FontSizeAction;
import ru.nest.jvg.action.GroupPaintOrderAction;
import ru.nest.jvg.action.InitialBoundsAction;
import ru.nest.jvg.action.JVGAction;
import ru.nest.jvg.action.ParagraphBulletsAction;
import ru.nest.jvg.action.ParagraphBulletsIndentAction;
import ru.nest.jvg.action.PathAddCurveAction;
import ru.nest.jvg.action.PathOperationAction;
import ru.nest.jvg.action.RemoveTransformAction;
import ru.nest.jvg.action.ScriptAction;
import ru.nest.jvg.action.SetPathArrowStrokeAction;
import ru.nest.jvg.action.ShadowAction;
import ru.nest.jvg.action.ShapeDraggingAction;
import ru.nest.jvg.action.SmoothPathAction;
import ru.nest.jvg.action.StrokePathAction;
import ru.nest.jvg.action.TextBackgroundAction;
import ru.nest.jvg.action.TextForegroundAction;
import ru.nest.jvg.action.TextLineSpacingAction;
import ru.nest.jvg.action.TextParagraphSpacingAction;
import ru.nest.jvg.action.TextWrapAction;
import ru.nest.jvg.action.ToPathAction;
import ru.nest.jvg.actionarea.JVGAbstractConnectionActionArea;
import ru.nest.jvg.actionarea.JVGCoordinateActionArea;
import ru.nest.jvg.actionarea.JVGRotateActionArea;
import ru.nest.jvg.actionarea.JVGScaleActionArea;
import ru.nest.jvg.actionarea.JVGShearActionArea;
import ru.nest.jvg.actionarea.JVGVectorActionArea;
import ru.nest.jvg.actionarea.MoveMouseListener;
import ru.nest.jvg.editor.editoraction.AddShapeEditorAction;
import ru.nest.jvg.editor.editoraction.EraserEditorAction;
import ru.nest.jvg.editor.editoraction.LassoEditorAction;
import ru.nest.jvg.editor.editoraction.MarkerEditorAction;
import ru.nest.jvg.editor.editoraction.PencilEditorAction;
import ru.nest.jvg.editor.editoraction.SimpleShapeEditorAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGImage;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.JVGSubPath;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.EndingsPainter;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.paint.ShadowPainter;
import ru.nest.jvg.shape.text.JVGStyleConstants;
import ru.nest.strokes.ArrowStroke;
import ru.nest.strokes.TextStroke;

public class JVGEditorActions implements ActionListener, ChangeListener, ItemListener {
	private String configFileName = "jvgeditor.properties";

	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private boolean freezeActions = false;

	private JVGEditor editor;

	public JVGEditorActions(JVGEditor editor) {
		this.editor = editor;
	}

	protected Map<String, ComponentAction> actionsMap = new HashMap<>();

	public ComponentAction getAction(String actionCommand) {
		return actionsMap.get(actionCommand);
	}

	// toolbars
	protected JPanel toolBars = new JPanel();

	protected List<WToolBar> toolbarsList = new ArrayList<>();

	private List<WCheckBoxMenuItem> toolbarMenus = new ArrayList<>();

	protected WToolBar editorToolbar;

	protected WToolBar insertToolbar;

	protected WToolBar textToolbar;

	protected WToolBar paragraphToolbar;

	protected WToolBar alignToolbar;

	protected WToolBar sameToolbar;

	protected WToolBar transformToolbar;

	protected WToolBar outlineToolbar;

	protected WToolBar pathOperationsToolbar;

	protected WToolBar idToolbar;

	protected WToolBar displayToolbar;

	protected WToolBar actionAreaToolbar;

	protected WToolBar arrowsToolbar;

	public void showAllToolbars() {
		for (WCheckBoxMenuItem item : toolbarMenus) {
			item.setSelected(true);
		}

		for (WToolBar toolbar : toolbarsList) {
			toolbar.setVisible(true);
		}
	}

	public void hideAllToolbars() {
		for (WCheckBoxMenuItem item : toolbarMenus) {
			item.setSelected(false);
		}

		for (WToolBar toolbar : toolbarsList) {
			toolbar.setVisible(false);
		}
	}

	public void hideToolbar(String name) {
		for (WCheckBoxMenuItem item : toolbarMenus) {
			if (name.equals(item.getName())) {
				item.setSelected(false);
			}
		}
	}

	// menus
	protected JMenuBar menuBar = new JMenuBar();

	protected WMenu menuRecent = new WMenu(lm.getValue("button.recent-files", "Open Recent File"));

	// file actions
	protected ButtonAction actionNew = new ButtonAction(lm.getValue("button.new", "New"), "new", this, "new.gif", KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));

	protected ButtonAction actionSave = new ButtonAction(lm.getValue("button.save", "Save"), "save", this, "save.gif", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));

	protected ButtonAction actionSaveAs = new ButtonAction(lm.getValue("button.saveas", "Save As..."), "save-as", this, "saveas.gif");

	protected ButtonAction actionSaveSelection = new ButtonAction(lm.getValue("button.save-selection", "Save Fragment..."), "save-selection", this, "saveselection.png");

	protected ButtonAction actionSaveAll = new ButtonAction(lm.getValue("button.saveall", "Save All"), "save-all", this, "saveall.gif");

	protected ButtonAction actionExport = new ButtonAction(lm.getValue("button.export", "Export..."), "export", this, "export.png");

	protected ButtonAction actionOpen = new ButtonAction(lm.getValue("button.open", "Open"), "open", this, "open.gif", KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));

	protected ButtonAction actionClose = new ButtonAction(lm.getValue("button.close", "Close"), "close", this, "close.gif", KeyStroke.getKeyStroke(KeyEvent.VK_F4, Event.CTRL_MASK));

	protected ButtonAction actionExit = new ButtonAction(lm.getValue("button.exit", "Exit"), "exit", this, "exit.gif");

	protected ButtonAction actionSaveEditorConfig = new ButtonAction(lm.getValue("button.save-editor-config", "Save editor config"), "save-editor-config", this, "saveeditorconfig.png");

	// document actions
	protected ButtonAction actionResizeDocument = new ButtonAction(lm.getValue("button.resize-document", "Resize"), "resize-document", this, "resize_document.gif");

	protected ButtonAction actionDocumentColor = new ButtonAction(lm.getValue("button.document-color", "Color"), "document-color", this, "color.png");

	protected ButtonAction actionPreview = new ButtonAction(lm.getValue("button.preview", "Preview"), "preview", this, "preview.gif", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

	protected ButtonAction actionEditXML = new ButtonAction(lm.getValue("button.source", "Source"), "edit-xml", this, "schemamap.gif", KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));

	protected ButtonAction actionEditDOM = new ButtonAction(lm.getValue("button.dom", "DOM"), "edit-dom", this, "xml.png", KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

	protected ButtonAction actionFind = new ButtonAction(lm.getValue("button.find", "Find"), "find", this, "find.gif", KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));

	// edit actions
	protected ButtonAction actionUndo = new ButtonAction(lm.getValue("button.undo", "Undo"), "undo", this, "undo.gif", KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK));

	protected ButtonAction actionRedo = new ButtonAction(lm.getValue("button.redo", "Redo"), "redo", this, "redo.gif", KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK | Event.SHIFT_MASK));

	protected ButtonAction actionCut = new ButtonAction(lm.getValue("button.cut", "Cut"), "cut", JVGEditorKit.getAction(JVGEditorKit.CUT_ACTION), "cut.gif", KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));

	protected ButtonAction actionCopy = new ButtonAction(lm.getValue("button.copy", "Copy"), "copy", JVGEditorKit.getAction(JVGEditorKit.COPY_ACTION), "copy.gif", KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));

	protected ButtonAction actionPaste = new ButtonAction(lm.getValue("button.paste", "Paste"), "paste", JVGEditorKit.getAction(JVGEditorKit.PASTE_ACTION), "paste.gif", KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));

	protected ButtonAction actionDelete = new ButtonAction(lm.getValue("button.delete", "Delete"), "delete", JVGEditorKit.getAction(JVGEditorKit.REMOVE_ACTION), "delete.gif", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

	protected ButtonAction actionSelectAll = new ButtonAction(lm.getValue("button.selectall", "Select All"), "select-all", JVGEditorKit.getAction(JVGEditorKit.SELECT_ALL_ACTION), "select_all.gif", KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));

	protected WCheckBoxMenuItem menuSelectionNone = new WCheckBoxMenuItem(lm.getValue("button.selectiontype.none", "None"));

	protected WCheckBoxMenuItem menuSelectionArea = new WCheckBoxMenuItem(lm.getValue("button.selectiontype.area", "Area"));

	protected WCheckBoxMenuItem menuSelectionLasso = new WCheckBoxMenuItem(lm.getValue("button.selectiontype.lasso", "Lasso"));

	protected ToggleButtonAction actionInitialBounds = new ToggleButtonAction(lm.getValue("button.bounds.initial", "Initial bounds"), "initial-bounds", new InitialBoundsAction(), "initial_bounds.png");

	protected ButtonAction actionMouse = new ButtonAction(lm.getValue("button.mouse", "Mouse"), "set-mouse", this, "cursor.gif");

	protected ButtonAction actionLine = new ButtonAction(lm.getValue("button.line", "Line"), "add-line", new SimpleShapeEditorAction("line", new Line2D.Double(0, 0, 100, 100), false, ExclusiveActionAreaAction.VECTOR), "line.png");

	protected ButtonAction actionRect = new ButtonAction(lm.getValue("button.rectangle", "Rectangle"), "add-rect", new SimpleShapeEditorAction("rectangle", new Rectangle2D.Double(0, 0, 100, 100), true), "rectangle.png");

	protected ButtonAction actionEllipse = new ButtonAction(lm.getValue("button.ellipse", "Ellipse"), "add-ellipse", new SimpleShapeEditorAction("ellipse", new Arc2D.Double(0, 0, 100, 100, 0, 360, Arc2D.OPEN), true), "ellipse.png");

	protected ButtonAction actionText = new ButtonAction(lm.getValue("button.text", "Text"), "add-text", new AddShapeEditorAction("add-text", new ShapeCreator() {
		@Override
		public JVGShape create(JVGPane pane) {
			final JVGStyledText text = pane.getEditorKit().getFactory().createComponent(JVGStyledText.class, new Object[] { "" });
			text.addCaretListener(new CaretListener() {
				@Override
				public void caretUpdate(CaretEvent e) {
					update(text);
				}
			});
			return text;
		}
	}), "text.gif");

	protected ButtonAction actionTextField = new ButtonAction(lm.getValue("button.textfield", "Text field"), "add-text-field", new AddShapeEditorAction("add-text-field", new ShapeCreator() {
		@Override
		public JVGShape create(JVGPane pane) {
			JTextField txt = new JTextField(30);
			int option = JOptionPane.showConfirmDialog(null, txt, lm.getValue("editor.textfield.entertext", "Enter text"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (option == JOptionPane.OK_OPTION) {
				String text = txt.getText();
				if (text.length() != 0) {
					return pane.getEditorKit().getFactory().createComponent(JVGTextField.class, new Object[] { text });
				}
			}
			return null;
		}
	}), "textfield.png");

	protected ButtonAction actionImage = new ButtonAction(lm.getValue("button.image", "Image"), "add-image", new AddShapeEditorAction("add-image", new ShapeCreator() {
		@Override
		public JVGShape create(JVGPane pane) {
			JFileChooser chooser = new JFileChooser(JVGEditor.fileName);
			if (chooser.showOpenDialog(pane) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (file != null) {
					JVGEditor.fileName = file.getAbsolutePath();
					try {
						JVGImage image = pane.getEditorKit().getFactory().createComponent(JVGImage.class, new Object[] { file.toURI().toURL() });
						return image;
					} catch (Exception exc) {
					}
				}
			}
			return null;
		}
	}), "picture.gif");

	protected ButtonAction actionPath = new ButtonAction(lm.getValue("button.path", "Path"), "add-path", new AddShapeEditorAction("add-path", new ShapeCreator() {
		@Override
		public JVGShape create(JVGPane pane) {
			JVGPath path = editor.getCurrentPane().getEditorKit().getFactory().createComponent(JVGPath.class, new Object[] { new MutableGeneralPath(), false });
			return path;
		}
	}), "path.gif");

	protected ButtonAction actionTextPath = new ButtonAction(lm.getValue("button.text-path", "Text path"), "add-text-path", new AddShapeEditorAction("add-text-path", new ShapeCreator() {
		@Override
		public JVGShape create(JVGPane pane) {
			JTextField txt = new JTextField(30);
			int option = JOptionPane.showConfirmDialog(null, txt, lm.getValue("", "Enter text"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (option == JOptionPane.OK_OPTION) {
				String text = txt.getText();
				if (text.length() != 0) {
					Resource<? extends Stroke> stroke = new StrokeResource<>(text, getFont(), true, false);
					JVGPath path = editor.getCurrentPane().getEditorKit().getFactory().createComponent(JVGPath.class, new Object[] { new MutableGeneralPath(), false, stroke });
					return path;
				}
			}
			return null;
		}
	}), "path.gif");

	protected ButtonAction actionMultiArrowPath = new ButtonAction(lm.getValue("button.multi-arrow-path", "Multi arrow path"), "add-multi-arrow-path", new AddShapeEditorAction("add-multi-arrow-path", new ShapeCreator() {
		@Override
		public JVGShape create(JVGPane pane) {
			JVGGroupPath path = editor.getCurrentPane().getEditorKit().getFactory().createComponent(JVGGroupPath.class, new Object[] { new MutableGeneralPath(), false });
			return path;
		}
	}), "multiarrowpath.png");

	protected ButtonAction actionAddSubArrow = new ButtonAction(lm.getValue("button.multi-arrow-path.add-sub-arrow", "Add subarrow"), "add-sub-arrow", this, "addsubarrow.png");

	protected ButtonAction actionMoveSubArrow = new ButtonAction(lm.getValue("button.multi-arrow-path.move-sub-arrow", "Move subarrow"), "move-sub-arrow", this, "movesubarrow.png");

	protected SliderChooser btnSubArrowWidth = new SliderChooser(new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.translate(x, y);
			g.setColor(Color.darkGray);
			g.drawLine(3, 5, 3, 7);
			g.drawLine(7, 5, 7, 7);
			g.drawLine(1, 4, 5, 0);
			g.drawLine(9, 4, 5, 0);
			g.translate(-x, -y);
		}

		@Override
		public int getIconWidth() {
			return 16;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	}, 1, 50) {
		@Override
		public void setValue(double value) {
			super.setValue(value);

			JVGPane pane = editor.getCurrentPane();
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			JVGComponent[] selection = selectionModel.getSelection();
			if (selection != null) {
				for (JVGComponent c : selection) {
					if (c instanceof JVGPath) {
						JVGPath path = (JVGPath) c;
						if (path.getPathStroke() != null && path.getPathStroke().getResource() instanceof ArrowStroke) {
							ArrowStroke stroke = (ArrowStroke) path.getPathStroke().getResource();
							double scale = value / stroke.getWidth();
							SetPathArrowStrokeAction action = new SetPathArrowStrokeAction(value, stroke.getArrowWidth() * scale, stroke.getArrowLength() * scale);
							action.doAction();
						}
					}
				}
			}
		}

		@Override
		public void commit(double oldValue, double newValue) {
		}
	};

	protected SliderChooser btnSubArrowEndCapWidth = new SliderChooser(new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.translate(x, y);
			g.setColor(Color.darkGray);
			g.drawLine(3, 5, 3, 7);
			g.drawLine(7, 5, 7, 7);
			g.drawLine(1, 4, 5, 0);
			g.drawLine(9, 4, 5, 0);
			g.translate(-x, -y);
		}

		@Override
		public int getIconWidth() {
			return 16;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	}, 1, 100) {
		@Override
		public void setValue(double value) {
			super.setValue(value);

			JVGPane pane = editor.getCurrentPane();
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			JVGComponent[] selection = selectionModel.getSelection();
			if (selection != null) {
				for (JVGComponent c : selection) {
					if (c instanceof JVGPath) {
						JVGPath path = (JVGPath) c;
						if (path.getPathStroke() != null && path.getPathStroke().getResource() instanceof ArrowStroke) {
							ArrowStroke stroke = (ArrowStroke) path.getPathStroke().getResource();
							double scale = value / stroke.getArrowWidth();
							SetPathArrowStrokeAction action = new SetPathArrowStrokeAction(stroke.getWidth(), value, stroke.getArrowLength() * scale);
							action.doAction();
						}
					}
				}
			}
		}

		@Override
		public void commit(double oldValue, double newValue) {
		}
	};

	protected SliderChooser btnSubArrowEndCapLength = new SliderChooser(new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.translate(x, y);
			g.setColor(Color.darkGray);
			g.drawLine(3, 5, 3, 7);
			g.drawLine(7, 5, 7, 7);
			g.drawLine(1, 4, 5, 0);
			g.drawLine(9, 4, 5, 0);
			g.translate(-x, -y);
		}

		@Override
		public int getIconWidth() {
			return 16;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	}, 1, 100) {
		@Override
		public void setValue(double value) {
			super.setValue(value);

			JVGPane pane = editor.getCurrentPane();
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			JVGComponent[] selection = selectionModel.getSelection();
			if (selection != null) {
				for (JVGComponent c : selection) {
					if (c instanceof JVGPath) {
						JVGPath path = (JVGPath) c;
						if (path.getPathStroke() != null && path.getPathStroke().getResource() instanceof ArrowStroke) {
							SetPathArrowStrokeAction action = new SetPathArrowStrokeAction(value, SetPathArrowStrokeAction.TYPE_END_CAP_LENGTH);
							action.doAction();
						}
					}
				}
			}
		}

		@Override
		public void commit(double oldValue, double newValue) {
		}
	};

	protected ArrowDirectionChooseButton btnArrowDirectionChoose = new ArrowDirectionChooseButton();

	protected ButtonAction actionClosePath = new ButtonAction(lm.getValue("button.path.close", "Close Path"), "close-path", new PathAddCurveAction(PathIterator.SEG_CLOSE, null, null), "close_path.gif");

	protected ButtonAction actionPencilAdjusted = new ButtonAction(lm.getValue("button.pencil.adjusted", "Adjusted Pencil"), "adjusted-pencil", new PencilEditorAction(true), "pencil_adjusted.gif");

	protected ButtonAction actionPencil = new ButtonAction(lm.getValue("button.pencil", "Pencil"), "pencil", new PencilEditorAction(false), "pencil.gif");

	protected ButtonAction actionMarker = new ButtonAction(lm.getValue("button.marker", "marker"), "marker", new MarkerEditorAction("Marker", MarkerEditorAction.UNION), "marker.png");

	protected ButtonAction actionToPath = new ButtonAction(lm.getValue("button.path.topath", "To Path"), "to-path", new ToPathAction(), "to_path.png");

	protected ButtonAction actionStrokePath = new ButtonAction(lm.getValue("button.path.stroke-path", "Stroke Path"), "stroke-path", new StrokePathAction(), "stroke_path.png");

	protected ButtonAction actionFlatPath = new ButtonAction(lm.getValue("button.path.flat-path", "Flat Path"), "flat-path", new FlatPathAction(), "flat_path.png");

	protected ButtonAction actionSmoothPath = new ButtonAction(lm.getValue("button.path.smooth", "Smooth Path"), "smooth-path", new SmoothPathAction(0.1f), "smooth_path.png");

	protected ButtonAction actionRemoveTransformPath = new ButtonAction(lm.getValue("button.path.remove-transform", "Remove transform"), "remove-transform-path", new RemoveTransformAction(), "remove_transform_path.png");

	protected ToggleButtonAction actionEditPath = new ToggleButtonAction(lm.getValue("button.path.edit", "Edit Path"), "edit-path", new EditPathAction(), "edit_path.png");

	protected ButtonAction actionUnion = new ButtonAction(lm.getValue("button.path.union", "Union"), "union", new PathOperationAction(PathOperationAction.UNION), "union.gif");

	protected ButtonAction actionIntersection = new ButtonAction(lm.getValue("button.path.intersection", "Intersection"), "intersection", new PathOperationAction(PathOperationAction.INTERSECTION), "intersection.gif");

	protected ButtonAction actionSubtraction = new ButtonAction(lm.getValue("button.path.subtraction", "Subtraction"), "subtraction", new PathOperationAction(PathOperationAction.SUBTRACTION), "subtraction.gif");

	protected ButtonAction actionExclusiveOr = new ButtonAction(lm.getValue("button.path.xor", "Exclusive OR"), "xor", new PathOperationAction(PathOperationAction.XOR), "xor.gif");

	protected ButtonAction actionEraser = new ButtonAction(lm.getValue("button.path.eraser", "Eraser"), "eraser", new EraserEditorAction("Eraser", new Rectangle(0, 0, 20, 20)), "eraser.gif");

	protected ButtonAction actionEraserLasso = new ButtonAction(lm.getValue("button.path.eraser-lasso", "Lasso eraser"), "lasso-eraser", new LassoEditorAction(), "eraser_lasso.png");

	protected ToggleButtonAction actionAntialias = new ToggleButtonAction(lm.getValue("button.antialias", "Antialias"), "antialias", new AntialiasAction(), "antialias.png");

	// transform actions
	protected ButtonAction actionAlignByFocusTop = new ButtonAction(lm.getValue("button.align.relative.focus.top", "Top"), "align-group-top", JVGEditorKit.getAction(JVGEditorKit.TOP_GROUP_ALIGNMENT_ACTION), "align_top.gif");

	protected ButtonAction actionAlignByFocusLeft = new ButtonAction(lm.getValue("button.align.relative.focus.left", "Left"), "align-group-left", JVGEditorKit.getAction(JVGEditorKit.LEFT_GROUP_ALIGNMENT_ACTION), "align_left.gif");

	protected ButtonAction actionAlignByFocusBottom = new ButtonAction(lm.getValue("button.align.relative.focus.bottom", "Bottom"), "align-group-bottom", JVGEditorKit.getAction(JVGEditorKit.BOTTOM_GROUP_ALIGNMENT_ACTION), "align_bottom.gif");

	protected ButtonAction actionAlignByFocusRight = new ButtonAction(lm.getValue("button.align.relative.focus.right", "Right"), "align-group-right", JVGEditorKit.getAction(JVGEditorKit.RIGHT_GROUP_ALIGNMENT_ACTION), "align_right.gif");

	protected ButtonAction actionAlignByFocusCenter = new ButtonAction(lm.getValue("button.align.relative.focus.center", "Center"), "align-group-center", JVGEditorKit.getAction(JVGEditorKit.CENTER_GROUP_ALIGNMENT_ACTION), "align_center.gif");

	protected ButtonAction actionAlignByFocusCenterHor = new ButtonAction(lm.getValue("button.align.relative.focus.center.x", "Center along the X axis"), "align-group-center-hor", JVGEditorKit.getAction(JVGEditorKit.CENTER_HOR_GROUP_ALIGNMENT_ACTION), "align_center_hor.gif");

	protected ButtonAction actionAlignByFocusCenterVer = new ButtonAction(lm.getValue("button.align.relative.focus.center.y", "Center along the Y axis"), "align-group-center-ver", JVGEditorKit.getAction(JVGEditorKit.CENTER_VER_GROUP_ALIGNMENT_ACTION), "align_center_ver.gif");

	protected ButtonAction actionToFront = new ButtonAction(lm.getValue("button.order.to.front", "to Front"), "order-to-front", JVGEditorKit.getAction(JVGEditorKit.TO_FRONT_ACTION), "order_to_front.gif", KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, Event.CTRL_MASK));

	protected ButtonAction actionToUp = new ButtonAction(lm.getValue("button.order.to.up", "to Up"), "order-to-up", JVGEditorKit.getAction(JVGEditorKit.TO_UP_ACTION), "order_to_up.gif", KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK));

	protected ButtonAction actionToDown = new ButtonAction(lm.getValue("button.order.to.down", "to Down"), "order-to-down", JVGEditorKit.getAction(JVGEditorKit.TO_DOWN_ACTION), "order_to_down.gif", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK));

	protected ButtonAction actionToBack = new ButtonAction(lm.getValue("button.order.to.back", "to Back"), "order-to-back", JVGEditorKit.getAction(JVGEditorKit.TO_BACK_ACTION), "order_to_back.gif", KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, Event.CTRL_MASK));

	protected ButtonAction actionAlignTop = new ButtonAction(lm.getValue("button.align.relative.pane.top", "Top"), "align-top", JVGEditorKit.getAction(JVGEditorKit.TOP_ALIGNMENT_ACTION), "align_top.gif", KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.SHIFT_MASK));

	protected ButtonAction actionAlignLeft = new ButtonAction(lm.getValue("button.align.relative.pane.left", "Left"), "align-left", JVGEditorKit.getAction(JVGEditorKit.LEFT_ALIGNMENT_ACTION), "align_left.gif", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.SHIFT_MASK));

	protected ButtonAction actionAlignBottom = new ButtonAction(lm.getValue("button.align.relative.pane.bottom", "Bottom"), "align-bottom", JVGEditorKit.getAction(JVGEditorKit.BOTTOM_ALIGNMENT_ACTION), "align_bottom.gif", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.SHIFT_MASK));

	protected ButtonAction actionAlignRight = new ButtonAction(lm.getValue("button.align.relative.pane.right", "Right"), "align-right", JVGEditorKit.getAction(JVGEditorKit.RIGHT_ALIGNMENT_ACTION), "align_right.gif", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.SHIFT_MASK));

	protected ButtonAction actionAlignCenter = new ButtonAction(lm.getValue("button.align.relative.pane.center", "Center"), "align-center", JVGEditorKit.getAction(JVGEditorKit.CENTER_ALIGNMENT_ACTION), "align_center.gif", KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.SHIFT_MASK));

	protected ButtonAction actionAlignCenterHor = new ButtonAction(lm.getValue("button.align.relative.pane.center.x", "Center along the X axis"), "align-center-hor", JVGEditorKit.getAction(JVGEditorKit.CENTER_HOR_ALIGNMENT_ACTION), "align_center_hor.gif");

	protected ButtonAction actionAlignCenterVer = new ButtonAction(lm.getValue("button.align.relative.pane.center.y", "Center along the Y axis"), "align-center-ver", JVGEditorKit.getAction(JVGEditorKit.CENTER_VER_ALIGNMENT_ACTION), "align_center_ver.gif");

	protected ButtonAction actionSameWidth = new ButtonAction(lm.getValue("button.same.width", "Width"), "same-width", JVGEditorKit.getAction(JVGEditorKit.SAME_WIDTH_ACTION), "align_same_width.gif", KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.SHIFT_MASK | Event.CTRL_MASK));

	protected ButtonAction actionSameHeight = new ButtonAction(lm.getValue("button.same.height", "Height"), "same-height", JVGEditorKit.getAction(JVGEditorKit.SAME_HEIGHT_ACTION), "align_same_height.gif", KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.SHIFT_MASK | Event.CTRL_MASK));

	protected ButtonAction actionSameSize = new ButtonAction(lm.getValue("button.same.size", "Size"), "same-size", JVGEditorKit.getAction(JVGEditorKit.SAME_SIZE_ACTION), "align_same_size.gif");

	protected ButtonAction actionSameSpacesHor = new ButtonAction(lm.getValue("button.same.spaces.hor", "Horizontal"), "same-spaces-hor", JVGEditorKit.getAction(JVGEditorKit.SAME_SPACES_HORIZONTAL_ACTION), "align_same_spaces_hor.gif", KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK | Event.ALT_MASK));

	protected ButtonAction actionSameSpacesVer = new ButtonAction(lm.getValue("button.same.spaces.ver", "Vertical"), "same-spaces-ver", JVGEditorKit.getAction(JVGEditorKit.SAME_SPACES_VERTICAL_ACTION), "align_same_spaces_ver.gif", KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK | Event.ALT_MASK));

	protected ButtonAction actionFlipHor = new ButtonAction(lm.getValue("button.flip.hor", "Horizontal"), "flip-hor", JVGEditorKit.getAction(JVGEditorKit.FLIP_HORIZONTAL_ACTION), "flip_hor.gif", KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));

	protected ButtonAction actionFlipVer = new ButtonAction(lm.getValue("button.flip.ver", "Vertical"), "flip-ver", JVGEditorKit.getAction(JVGEditorKit.FLIP_VERTICAL_ACTION), "flip_ver.gif", KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.SHIFT_MASK));

	protected ButtonAction actionTranslateUp = new ButtonAction(lm.getValue("button.translate.up", "Up"), "translate-up", JVGEditorKit.getAction(JVGEditorKit.TRANSLATE_UP_ACTION), "translate_up.gif");

	protected ButtonAction actionTranslateLeft = new ButtonAction(lm.getValue("button.translate.left", "Left"), "translate-left", JVGEditorKit.getAction(JVGEditorKit.TRANSLATE_LEFT_ACTION), "translate_left.gif");

	protected ButtonAction actionTranslateDown = new ButtonAction(lm.getValue("button.translate.down", "Down"), "translate-down", JVGEditorKit.getAction(JVGEditorKit.TRANSLATE_DOWN_ACTION), "translate_down.gif");

	protected ButtonAction actionTranslateRight = new ButtonAction(lm.getValue("button.translate.right", "Right"), "translate-right", JVGEditorKit.getAction(JVGEditorKit.TRANSLATE_RIGHT_ACTION), "translate_right.gif");

	protected ButtonAction actionRotate180 = new ButtonAction(lm.getValue("button.rotate.180", "180"), "rotate-180", JVGEditorKit.getAction(JVGEditorKit.ROTATE_180_ACTION), "rotate_180.gif");

	protected ButtonAction actionRotate90 = new ButtonAction(lm.getValue("button.rotate.90", "90"), "rotate-90", JVGEditorKit.getAction(JVGEditorKit.ROTATE_90_ACTION), "rotate_90.gif", KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK));

	protected ButtonAction actionRotateMinus90 = new ButtonAction(lm.getValue("button.rotate.-90", "-90"), "rotate-minus-90", JVGEditorKit.getAction(JVGEditorKit.ROTATE_MINUS_90_ACTION), "rotate_minus_90.gif");

	protected ToggleButtonAction actionGroup = new ToggleButtonAction(lm.getValue("button.group", "Group"), "group", JVGEditorKit.getAction(JVGEditorKit.GROUP_ACTION), "group.gif", KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK));

	protected WMenu menuGroupPaintOrder = new WMenu(lm.getValue("menu.group.paint-order", "Paint order"));

	// text actions
	protected ToggleButtonAction actionTextWrap = new ToggleButtonAction(lm.getValue("button.text.wrap", "Wrap"), "text-wrap", new TextWrapAction(), "text_wrap.gif");

	protected FontComboBox cmbFontFamily = new FontComboBox();

	protected JComboBox cmbFontSize = new WComboBox();

	protected ToggleButtonAction actionBold = new ToggleButtonAction(lm.getValue("button.text.bold", "Bold"), "font-bold", JVGEditorKit.getAction(JVGEditorKit.FONT_BOLD_ACTION), "text_bold.gif");

	protected ToggleButtonAction actionItalic = new ToggleButtonAction(lm.getValue("button.text.italic", "Italic"), "font-italic", JVGEditorKit.getAction(JVGEditorKit.FONT_ITALIC_ACTION), "text_italic.gif");

	protected ToggleButtonAction actionUnderline = new ToggleButtonAction(lm.getValue("button.text.underline", "Underline"), "font-underline", JVGEditorKit.getAction(JVGEditorKit.FONT_UNDERLINE_ACTION), "text_underline.gif");

	protected ToggleButtonAction actionStrike = new ToggleButtonAction(lm.getValue("button.text.strike", "Strike Through"), "font-strike", JVGEditorKit.getAction(JVGEditorKit.FONT_STRIKE_ACTION), "text_strikethrough.gif");

	protected ToggleButtonAction actionSuperscript = new ToggleButtonAction(lm.getValue("button.text.superscript", "Superscript"), "font-superscript", JVGEditorKit.getAction(JVGEditorKit.FONT_SUPERSCRIPT_ACTION), "text_superscript.gif");

	protected ToggleButtonAction actionSubscript = new ToggleButtonAction(lm.getValue("button.text.subscript", "Subscript"), "font-subscript", JVGEditorKit.getAction(JVGEditorKit.FONT_SUBSCRIPT_ACTION), "text_subscript.gif");

	protected ChooseFillerButton btnTextBackground;

	protected ChooseFillerButton btnTextForeground;

	public <T> Resource getEditorDrawResource(Class painterClass, Class<T> clazz) {
		JVGShape shape = editor.getFocusedShape();
		if (shape != null) {
			int count = shape.getPaintersCount();
			for (int i = count - 1; i >= 0; i--) {
				Painter p = shape.getPainter(i);
				if (p.getClass() != painterClass) {
					continue;
				}

				if (p.getPaint() != null) {
					Resource resource = p.getPaint().getResource();
					Class valueClass = resource != null ? resource.getResource().getClass() : null;
					if (valueClass == clazz) {
						return resource;
					}
				}
			}
		}
		return null;
	}

	protected ToggleButtonAction actionLeftAlignment = new ToggleButtonAction(lm.getValue("button.text.align.left", "Left"), "text-align-left", JVGEditorKit.getAction(JVGEditorKit.TEXT_ALIGNMENT_LEFT_ACTION), "text_align_left.gif");

	protected ToggleButtonAction actionCenterAlignment = new ToggleButtonAction(lm.getValue("button.text.align.center", "Center"), "text-align-center", JVGEditorKit.getAction(JVGEditorKit.TEXT_ALIGNMENT_CENTER_ACTION), "text_align_center.gif");

	protected ToggleButtonAction actionRightAlignment = new ToggleButtonAction(lm.getValue("button.text.align.right", "Right"), "text-align-right", JVGEditorKit.getAction(JVGEditorKit.TEXT_ALIGNMENT_RIGHT_ACTION), "text_align_right.gif");

	protected ToggleButtonAction actionJustifyAlignment = new ToggleButtonAction(lm.getValue("button.text.align.justify", "Justift"), "text-align-justify", JVGEditorKit.getAction(JVGEditorKit.TEXT_ALIGNMENT_JUSTIFY_ACTION), "text_align_justify.gif");

	protected ButtonAction actionIncreaseLineSpacing = new ButtonAction(lm.getValue("button.text.line.spacing.increase", "Increase Line Spacing"), "text-increase-line-spacing", new TextLineSpacingAction("increase", 0.1f), "text_increase_line_spacing.gif");

	protected ButtonAction actionDecreaseLineSpacing = new ButtonAction(lm.getValue("button.text.line.spacing.decrease", "Decrease Line Spacing"), "text-decrease-line-spacing", new TextLineSpacingAction("decrease", -0.1f), "text_decrease_line_spacing.gif");

	protected ButtonAction actionBullets = new ButtonAction(lm.getValue("button.text.bullets", "Bullets"), "bullets-point", new ParagraphBulletsAction(JVGStyleConstants.BULLETS_CIRCLE), "bullets.png");

	protected ButtonAction actionBulletsNumber = new ButtonAction(lm.getValue("button.text.bullets.numeric", "Numeric Bullets"), "bullets-numeric", new ParagraphBulletsAction(JVGStyleConstants.BULLETS_NUMBER), "bullets_numeric.png");

	protected ButtonAction actionBulletsShiftLeft = new ButtonAction(lm.getValue("button.text.bullets.shift.left", "Shift left bullets"), "bullets-shift-left", new ParagraphBulletsIndentAction(ParagraphBulletsIndentAction.LEFT), "text_decrease_paragraph_left_indent.gif");

	protected ButtonAction actionBulletsShiftRight = new ButtonAction(lm.getValue("button.text.bullets.shift.right", "Shift right bullets"), "bullets-shift-right", new ParagraphBulletsIndentAction(ParagraphBulletsIndentAction.RIGHT), "text_increase_paragraph_left_indent.gif");

	protected ButtonAction actionIncreaseParagraphSpacing = new ButtonAction(lm.getValue("button.text.paragraph.spacing.increase", "Increase Paragraph Spacing"), "text-increase-paragraph-spacing", new TextParagraphSpacingAction("increase", TextParagraphSpacingAction.INCREASE, 0f, 0f, 1f, 0f), "text_increase_paragraph_spacing.gif");

	protected ButtonAction actionDecreaseParagraphSpacing = new ButtonAction(lm.getValue("button.text.paragraph.spacing.decrease", "Decrease Paragraph Spacing"), "text-decrease-paragraph-spacing", new TextParagraphSpacingAction("decrease", TextParagraphSpacingAction.INCREASE, 0f, 0f, -1f, 0f), "text_decrease_paragraph_spacing.gif");

	protected ButtonAction actionIncreaseParagraphLeftIndent = new ButtonAction(lm.getValue("button.text.paragraph.indent.left.increase", "Increase Paragraph Left Indent"), "text-increase-paragraph-left-indend", new TextParagraphSpacingAction("increase", TextParagraphSpacingAction.INCREASE, 0f, 1f, 0f, 0f), "text_increase_paragraph_left_indent.gif");

	protected ButtonAction actionDecreaseParagraphLeftIndent = new ButtonAction(lm.getValue("button.text.paragraph.indent.left.decrease", "Decrease Paragraph Left Indent"), "text-decrease-paragraph-left-indent", new TextParagraphSpacingAction("decrease", TextParagraphSpacingAction.INCREASE, 0f, -1f, 0f, 0f), "text_decrease_paragraph_left_indent.gif");

	// script
	protected ButtonAction actionScriptStart = getScriptActionButton(Script.START);

	protected ButtonAction actionScriptMouseClicked = getScriptActionButton(Script.MOUSE_CLICKED);

	protected ButtonAction actionScriptMousePressed = getScriptActionButton(Script.MOUSE_PRESSED);

	protected ButtonAction actionScriptMouseReleased = getScriptActionButton(Script.MOUSE_RELEASED);

	protected ButtonAction actionScriptMouseEntered = getScriptActionButton(Script.MOUSE_ENTERED);

	protected ButtonAction actionScriptMouseExited = getScriptActionButton(Script.MOUSE_EXITED);

	protected ButtonAction actionScriptMouseMoved = getScriptActionButton(Script.MOUSE_MOVED);

	protected ButtonAction actionScriptMouseDragged = getScriptActionButton(Script.MOUSE_DRAGGED);

	protected ButtonAction actionScriptMouseWheel = getScriptActionButton(Script.MOUSE_WHEEL);

	protected ButtonAction actionScriptKeyPressed = getScriptActionButton(Script.KEY_PRESSED);

	protected ButtonAction actionScriptKeyReleased = getScriptActionButton(Script.KEY_RELEASED);

	protected ButtonAction actionScriptKeyTyped = getScriptActionButton(Script.KEY_TYPED);

	protected ButtonAction actionScriptComponentShown = getScriptActionButton(Script.COMPONENT_SHOWN);

	protected ButtonAction actionScriptComponentHidden = getScriptActionButton(Script.COMPONENT_HIDDEN);

	protected ButtonAction actionScriptComponentGeometryChanged = getScriptActionButton(Script.COMPONENT_GEOMETRY_CHANGED);

	protected ButtonAction actionScriptComponentTransformed = getScriptActionButton(Script.COMPONENT_TRANSFORMED);

	protected ButtonAction actionScriptComponentAdded = getScriptActionButton(Script.COMPONENT_ADDED);

	protected ButtonAction actionScriptComponentRemoved = getScriptActionButton(Script.COMPONENT_REMOVED);

	protected ButtonAction actionScriptComponentOrderChanged = getScriptActionButton(Script.COMPONENT_ORDER_CHANGED);

	protected ButtonAction actionScriptComponentConnectedToPeer = getScriptActionButton(Script.COMPONENT_CONNECTED_TO_PEER);

	protected ButtonAction actionScriptComponentDisconnectedFromPeer = getScriptActionButton(Script.COMPONENT_DISCONNECTED_FROM_PEER);

	protected ButtonAction actionScriptFocusLost = getScriptActionButton(Script.FOCUS_LOST);

	protected ButtonAction actionScriptFocusGained = getScriptActionButton(Script.FOCUS_GAINED);

	protected ButtonAction actionScriptPropertyChanged = getScriptActionButton(Script.PROPERTY_CHANGED);

	protected ButtonAction actionScriptValidation = getScriptActionButton(Script.VALIDATION);

	protected ButtonAction actionScriptPaint = getScriptActionButton(Script.PAINT);

	protected ButtonAction actionCustomActionArea = new ButtonAction(lm.getValue("button.add-custom-action-area", "Add custom action area"), "add-custom-action-area", new AddCustomActionAreaAction(), "custom-action-area.png");

	protected ButtonAction getScriptActionButton(Script.Type type) {
		return new ButtonAction(type.getDescr(), type.getActionName(), new ScriptAction(type), "");
	}

	// display
	protected boolean sliderAlfaLayerActionEnabled = true;

	protected SliderField sliderAlfaLayer = new SliderField(0, 100, 100);

	// action area
	protected ToggleButtonAction noActionActionArea = new ToggleButtonAction(lm.getValue("button.action.area.no", "No Action Area"), "action-area-no", new ExclusiveActionAreaAction(ExclusiveActionAreaAction.NONE), "action_no.png");

	protected ToggleButtonAction actionScaleActionArea = new ToggleButtonAction(lm.getValue("button.action.area.scale", "Scale Action Area"), "action-area-scale", new ExclusiveActionAreaAction(ExclusiveActionAreaAction.SCALE), "action_scale.png");

	protected ToggleButtonAction actionShearActionArea = new ToggleButtonAction(lm.getValue("button.action.area.shear", "Shear Action Area"), "action-area-shear", new ExclusiveActionAreaAction(ExclusiveActionAreaAction.SHEAR), "action_shear.png");

	protected ToggleButtonAction actionVectorActionArea = new ToggleButtonAction(lm.getValue("button.action.area.vector", "Vector Action Area"), "action-area-vector", new ExclusiveActionAreaAction(ExclusiveActionAreaAction.VECTOR), "action_vector.png");

	protected ToggleButtonAction actionRotateActionArea = new ToggleButtonAction(lm.getValue("button.action.area.rotate", "Rotate Action Area"), "action-area-rotate", new ActionAreaAction<>(JVGRotateActionArea.class), "action_rotate.png");

	protected ToggleButtonAction actionCoordinateActionArea = new ToggleButtonAction(lm.getValue("button.action.area.coordinate", "Coordinate Action Area"), "action-area-coordinate", new ActionAreaAction<>(JVGCoordinateActionArea.class), "action_coordinate.png");

	protected ToggleButtonAction actionConnectionActionArea = new ToggleButtonAction(lm.getValue("button.action.area.connection", "Connection Action Area"), "action-area-connection", new ActionAreaAction<>(JVGAbstractConnectionActionArea.class), "action_connection.png");

	protected ToggleButtonAction actionMoveActionArea = new ToggleButtonAction(lm.getValue("button.action.area.move", "Shape drag enabled"), "action-area-move", new ShapeDraggingAction(), "action_move.png");

	// shape appearance
	protected OutlineWidthChooser cmbOutlineWidth = new OutlineWidthChooser();

	public OutlineWidthChooser getOutlineWidth() {
		return cmbOutlineWidth;
	}

	protected OutlinePatternChooser cmbOutlinePattern = new OutlinePatternChooser();

	public OutlinePatternChooser getOutlinePattern() {
		return cmbOutlinePattern;
	}

	protected ChooseFillerButton btnOutlineColor;

	public ChooseFillerButton getOutlineDraw() {
		return btnOutlineColor;
	}

	protected StrokeEndCapChooser cmbStrokeCap = new StrokeEndCapChooser();

	protected StrokeLineJoinChooser cmbStrokeJoin = new StrokeLineJoinChooser();

	protected ChooseFillerButton btnFillColor;

	public ChooseFillerButton getFillDraw() {
		return btnFillColor;
	}

	protected ChooseFillerButton btnShadowColor;

	protected ChooseFillerButton btnEndingsColor;

	protected EndingsPatternChooser cmbEndingsPattern = new EndingsPatternChooser();

	protected FillerTransparencyChooser btnFillTransparency = new FillerTransparencyChooser();

	public FillerTransparencyChooser getFillTransparency() {
		return btnFillTransparency;
	}

	protected ChooseShadowButton btnShadowType = new ChooseShadowButton() {
		{
			final Action action = new ShadowAction();

			JMenuItem menuNoShadow = new JMenuItem(lm.getValue("button.shadow.no", "No Shadow"));
			menuNoShadow.addActionListener(action);
			menuNoShadow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setAction(action);
				}
			});
			getPopup().add(menuNoShadow, new GridBagConstraints(0, 0, 4, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}
	};

	private JTextField txtID = new JTextField(6);

	public void init() {
		initCustomComponents();
		initGroups();
		initToolbars();
		initMenus();
	}

	protected void initCustomComponents() {
		btnOutlineColor = new ChooseFillerButton(editor.getResources(), Painter.OUTLINE, new ImageIcon(JVGEditor.class.getResource("img/brush.png")), new ColorDraw(ColorResource.black)) {
			@Override
			public <T> Resource getEditorResource(Class<T> clazz) {
				return getEditorDrawResource(OutlinePainter.class, clazz);
			}
		};

		btnFillColor = new ChooseFillerButton(editor.getResources(), Painter.FILL, new ImageIcon(JVGEditor.class.getResource("img/fill_color.png")), new ColorDraw(new Color(230, 230, 230))) {
			@Override
			public <T> Resource getEditorResource(Class<T> clazz) {
				return getEditorDrawResource(FillPainter.class, clazz);
			}
		};

		btnTextBackground = new ChooseFillerButton(editor.getResources(), new ImageIcon(JVGEditor.class.getResource("img/background.png")), new ColorDraw(ColorResource.white)) {
			@Override
			protected JVGAction createAction(Resource resource) {
				return new TextBackgroundAction("text-background", resource);
			}

			@Override
			public <T> Resource getEditorResource(Class<T> clazz) {
				return null;
			}
		};

		btnTextForeground = new ChooseFillerButton(editor.getResources(), new ImageIcon(JVGEditor.class.getResource("img/foreground.png")), new ColorDraw(ColorResource.black)) {
			@Override
			protected JVGAction createAction(Resource resource) {
				return new TextForegroundAction("text-foreground", resource);
			}

			@Override
			public <T> Resource getEditorResource(Class<T> clazz) {
				return null;
			}
		};

		btnShadowColor = new ChooseFillerButton(editor.getResources(), Painter.SHADOW, new ImageIcon(JVGEditor.class.getResource("img/fill_color.png")), new ColorDraw(ShadowPainter.DEFAULT_COLOR)) {
			@Override
			public <T> Resource getEditorResource(Class<T> clazz) {
				return getEditorDrawResource(ShadowPainter.class, clazz);
			}
		};

		btnEndingsColor = new ChooseFillerButton(editor.getResources(), Painter.ENDINGS, new ImageIcon(JVGEditor.class.getResource("img/fill_color.png")), null) {
			@Override
			public <T> Resource getEditorResource(Class<T> clazz) {
				return getEditorDrawResource(EndingsPainter.class, clazz);
			}
		};

		//
		cmbOutlinePattern.setPreferredSize(new Dimension(36, 24));
		cmbStrokeCap.setPreferredSize(new Dimension(31, 20));
		cmbStrokeJoin.setPreferredSize(new Dimension(31, 20));
		cmbEndingsPattern.setPreferredSize(new Dimension(36, 24));
		btnOutlineColor.setPreferredSize(new Dimension(36, 24));
		btnFillColor.setPreferredSize(new Dimension(36, 24));
		btnTextForeground.setPreferredSize(new Dimension(36, 24));
		btnTextBackground.setPreferredSize(new Dimension(36, 24));
		btnShadowType.setPreferredSize(new Dimension(36, 24));
		btnShadowColor.setPreferredSize(new Dimension(36, 24));
		btnEndingsColor.setPreferredSize(new Dimension(36, 24));

		actionTextWrap.setSelected(true);
		actionPaste.setEnabled(false);
	}

	protected void initGroups() {
		EditorGroup grpAlignment = new EditorGroup();
		grpAlignment.add(actionLeftAlignment);
		grpAlignment.add(actionCenterAlignment);
		grpAlignment.add(actionRightAlignment);
		grpAlignment.add(actionJustifyAlignment);

		EditorGroup actionAlignment = new EditorGroup();
		actionAlignment.add(noActionActionArea);
		actionAlignment.add(actionScaleActionArea);
		actionAlignment.add(actionShearActionArea);
		actionAlignment.add(actionVectorActionArea);
	}

	protected void initToolbars() {
		toolBars.setLayout(new ToolbarLayout());
		toolBars.setMinimumSize(new Dimension(16, 16));
		toolBars.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
					WBarMenu menuView = new WBarMenu(lm.getValue("menubar.view", "View"));
					addViewMenus(menuView);
					menuView.getPopupMenu().show(toolBars, e.getX(), e.getY());
				}
			}
		});

		toolBars.add(editorToolbar = getEditorToolBar());
		toolBars.add(insertToolbar = getInsertToolBar());
		toolBars.add(textToolbar = getTextToolBar());
		toolBars.add(paragraphToolbar = getParagraphToolBar());
		toolBars.add(alignToolbar = getAlignToolBar());
		toolBars.add(sameToolbar = getSameToolBar());
		toolBars.add(transformToolbar = getTransformToolBar());
		toolBars.add(outlineToolbar = getOutlineToolBar());
		toolBars.add(pathOperationsToolbar = getPathOperationsToolBar());
		toolBars.add(idToolbar = getIDToolBar());
		toolBars.add(displayToolbar = getDisplayToolBar());
		toolBars.add(actionAreaToolbar = getActionAreaToolBar());
		toolBars.add(arrowsToolbar = getArrowsToolBar());

		toolbarsList.add(editorToolbar);
		toolbarsList.add(insertToolbar);
		toolbarsList.add(textToolbar);
		toolbarsList.add(paragraphToolbar);
		toolbarsList.add(alignToolbar);
		toolbarsList.add(sameToolbar);
		toolbarsList.add(transformToolbar);
		toolbarsList.add(outlineToolbar);
		toolbarsList.add(pathOperationsToolbar);
		toolbarsList.add(idToolbar);
		toolbarsList.add(displayToolbar);
		toolbarsList.add(actionAreaToolbar);
		toolbarsList.add(arrowsToolbar);

		editorToolbar.setVisible(true);
		insertToolbar.setVisible(true);
		textToolbar.setVisible(true);
		paragraphToolbar.setVisible(false);
		alignToolbar.setVisible(false);
		sameToolbar.setVisible(false);
		transformToolbar.setVisible(false);
		outlineToolbar.setVisible(true);
		pathOperationsToolbar.setVisible(false);
		idToolbar.setVisible(false);
		displayToolbar.setVisible(false);
		actionAreaToolbar.setVisible(false);
		arrowsToolbar.setVisible(false);
	}

	public void addToolbar(WToolBar toolbar) {
		toolBars.add(toolbar);
		toolBars.revalidate();
		toolBars.repaint();
	}

	protected void initMenus() {
		editor.setJMenuBar(menuBar);

		WBarMenu menuFile = new WBarMenu(lm.getValue("menubar.file", "File"));
		addFileMenus(menuFile);
		menuBar.add(menuFile);

		WBarMenu menuEdit = new WBarMenu(lm.getValue("menubar.edit", "Edit"));
		addEditMenus(menuEdit);
		menuBar.add(menuEdit);

		WBarMenu menuDocument = new WBarMenu(lm.getValue("menubar.document", "Document"));
		addDocumentMenus(menuDocument);
		menuBar.add(menuDocument);

		WBarMenu menuView = new WBarMenu(lm.getValue("menubar.view", "View"));
		addViewMenus(menuView);
		menuBar.add(menuView);

		WBarMenu menuInsert = new WBarMenu(lm.getValue("menubar.insert", "Insert"));
		addInsertMenus(menuInsert);
		menuBar.add(menuInsert);

		WBarMenu menuTransforms = new WBarMenu(lm.getValue("menubar.transforms", "Transforms"));
		addTransformMenus(menuTransforms);
		menuBar.add(menuTransforms);

		WBarMenu menuHelp = new WBarMenu(lm.getValue("menubar.help", "Help"));
		addHelpMenus(menuHelp);
		menuBar.add(menuHelp);
	}

	protected void addFileMenus(JMenu menu) {
		menuRecent.setEnabled(false);

		menu.add(actionNew.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionOpen.getMenuItem());
		menu.add(menuRecent);

		menu.add(new WSeparator());

		menu.add(actionSave.getMenuItem());
		menu.add(actionSaveAs.getMenuItem());
		menu.add(actionSaveSelection.getMenuItem());
		menu.add(actionSaveAll.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionExport.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionSaveEditorConfig.getMenuItem());
		menu.add(actionClose.getMenuItem());
		menu.add(actionExit.getMenuItem());
	}

	protected void addEditMenus(JMenu menu) {
		WMenu menuSelectionType = new WMenu(lm.getValue("button.selectiontype", "Selection Type"));
		menuSelectionType.add(menuSelectionNone);
		menuSelectionArea.setSelected(true);
		menuSelectionType.add(menuSelectionArea);
		menuSelectionType.add(menuSelectionLasso);

		menuSelectionNone.setActionCommand("update-selection-type");
		menuSelectionNone.addActionListener(this);
		menuSelectionArea.setActionCommand("update-selection-type");
		menuSelectionArea.addActionListener(this);
		menuSelectionLasso.setActionCommand("update-selection-type");
		menuSelectionLasso.addActionListener(this);

		ButtonGroup grpSelectionType = new ButtonGroup();
		grpSelectionType.add(menuSelectionNone);
		grpSelectionType.add(menuSelectionArea);
		grpSelectionType.add(menuSelectionLasso);

		// text
		WMenu menuTextActions = new WMenu(lm.getValue("button.text", "Text"));
		menuTextActions.add(actionTextWrap.getMenuItem());

		menuTextActions.add(new WSeparator());

		menuTextActions.add(actionBold.getMenuItem());
		menuTextActions.add(actionItalic.getMenuItem());

		menuTextActions.add(new WSeparator());

		menuTextActions.add(actionUnderline.getMenuItem());
		menuTextActions.add(actionStrike.getMenuItem());
		menuTextActions.add(actionSubscript.getMenuItem());
		menuTextActions.add(actionSuperscript.getMenuItem());

		menuTextActions.add(new WSeparator());

		menuTextActions.add(actionLeftAlignment.getMenuItem());
		menuTextActions.add(actionCenterAlignment.getMenuItem());
		menuTextActions.add(actionRightAlignment.getMenuItem());
		menuTextActions.add(actionJustifyAlignment.getMenuItem());

		menuTextActions.add(new WSeparator());

		menuTextActions.add(actionBullets.getMenuItem());
		menuTextActions.add(actionBulletsNumber.getMenuItem());
		menuTextActions.add(actionBulletsShiftLeft.getMenuItem());
		menuTextActions.add(actionBulletsShiftRight.getMenuItem());

		menuTextActions.add(new WSeparator());

		menuTextActions.add(actionIncreaseLineSpacing.getMenuItem());
		menuTextActions.add(actionDecreaseLineSpacing.getMenuItem());
		menuTextActions.add(actionIncreaseParagraphLeftIndent.getMenuItem());
		menuTextActions.add(actionDecreaseParagraphLeftIndent.getMenuItem());
		menuTextActions.add(actionIncreaseParagraphSpacing.getMenuItem());
		menuTextActions.add(actionDecreaseParagraphSpacing.getMenuItem());

		// action areas
		WMenu menuActionAreas = new WMenu(lm.getValue("button.action.areas", "Actoin areas"));
		menuActionAreas.add(noActionActionArea.getMenuItem());
		menuActionAreas.add(actionScaleActionArea.getMenuItem());
		menuActionAreas.add(actionShearActionArea.getMenuItem());
		menuActionAreas.add(actionVectorActionArea.getMenuItem());
		menuActionAreas.add(new WSeparator());
		menuActionAreas.add(actionRotateActionArea.getMenuItem());
		menuActionAreas.add(actionCoordinateActionArea.getMenuItem());
		menuActionAreas.add(actionConnectionActionArea.getMenuItem());
		menuActionAreas.add(actionMoveActionArea.getMenuItem());

		// path
		WMenu menuPathOperations = new WMenu(lm.getValue("button.path.operations", "Path Operations"));
		menuPathOperations.add(actionUnion.getMenuItem());
		menuPathOperations.add(actionSubtraction.getMenuItem());
		menuPathOperations.add(actionIntersection.getMenuItem());
		menuPathOperations.add(actionExclusiveOr.getMenuItem());
		menuPathOperations.add(new WSeparator());
		menuPathOperations.add(actionClosePath.getMenuItem());
		menuPathOperations.add(actionSmoothPath.getMenuItem());
		menuPathOperations.add(actionStrokePath.getMenuItem());
		menuPathOperations.add(actionFlatPath.getMenuItem());

		WMenu menuScripts = new WMenu(lm.getValue("button.scripts", "Scripts"));
		menuScripts.add(actionScriptStart.getMenuItem());
		menuScripts.add(new WSeparator());
		menuScripts.add(actionScriptMouseClicked.getMenuItem());
		menuScripts.add(actionScriptMousePressed.getMenuItem());
		menuScripts.add(actionScriptMouseReleased.getMenuItem());
		menuScripts.add(actionScriptMouseEntered.getMenuItem());
		menuScripts.add(actionScriptMouseExited.getMenuItem());
		menuScripts.add(actionScriptMouseMoved.getMenuItem());
		menuScripts.add(actionScriptMouseDragged.getMenuItem());
		menuScripts.add(actionScriptMouseWheel.getMenuItem());
		menuScripts.add(new WSeparator());
		menuScripts.add(actionScriptKeyPressed.getMenuItem());
		menuScripts.add(actionScriptKeyReleased.getMenuItem());
		menuScripts.add(actionScriptKeyTyped.getMenuItem());
		menuScripts.add(new WSeparator());
		menuScripts.add(actionScriptFocusLost.getMenuItem());
		menuScripts.add(actionScriptFocusGained.getMenuItem());
		menuScripts.add(new WSeparator());
		menuScripts.add(actionScriptComponentShown.getMenuItem());
		menuScripts.add(actionScriptComponentHidden.getMenuItem());
		menuScripts.add(actionScriptComponentGeometryChanged.getMenuItem());
		menuScripts.add(actionScriptComponentTransformed.getMenuItem());
		menuScripts.add(actionScriptComponentAdded.getMenuItem());
		menuScripts.add(actionScriptComponentRemoved.getMenuItem());
		menuScripts.add(actionScriptComponentOrderChanged.getMenuItem());
		menuScripts.add(actionScriptComponentConnectedToPeer.getMenuItem());
		menuScripts.add(actionScriptComponentDisconnectedFromPeer.getMenuItem());
		menuScripts.add(new WSeparator());
		menuScripts.add(actionScriptPropertyChanged.getMenuItem());
		menuScripts.add(actionScriptValidation.getMenuItem());
		menuScripts.add(actionScriptPaint.getMenuItem());

		// --- create menu ---
		menu.add(actionUndo.getMenuItem());
		menu.add(actionRedo.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionCut.getMenuItem());
		menu.add(actionCopy.getMenuItem());
		menu.add(actionPaste.getMenuItem());
		menu.add(actionDelete.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionSelectAll.getMenuItem());
		menu.add(menuSelectionType);
		menu.add(actionInitialBounds.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionAntialias.getMenuItem());

		menu.add(new WSeparator());

		menu.add(menuActionAreas);

		menu.add(new WSeparator());

		menu.add(actionToPath.getMenuItem());
		menu.add(actionEditPath.getMenuItem());
		menu.add(actionRemoveTransformPath.getMenuItem());

		menu.add(actionPencilAdjusted.getMenuItem());
		menu.add(actionPencil.getMenuItem());
		menu.add(actionMarker.getMenuItem());

		menu.add(menuPathOperations);

		menu.add(actionEraser.getMenuItem());
		menu.add(actionEraserLasso.getMenuItem());

		menu.add(new WSeparator());

		menu.add(menuTextActions);

		menu.add(new WSeparator());

		menu.add(menuScripts);
		menu.add(actionCustomActionArea.getMenuItem());
	}

	protected void addDocumentMenus(JMenu menu) {
		menu.add(actionResizeDocument.getMenuItem());
		menu.add(actionDocumentColor.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionPreview.getMenuItem());
		menu.add(actionEditXML.getMenuItem());
		menu.add(actionEditDOM.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionFind.getMenuItem());
	}

	class JVGToolbar {
		public JVGToolbar(String name) {
			WCheckBoxMenuItem menu = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar." + name, "Editor"));
			menu.setName(name);
			menu.setSelected(editorToolbar.isVisible());
			menu.setActionCommand(name + "-toolbar");
			menu.addActionListener(JVGEditorActions.this);
			toolbarMenus.add(menu);
		}
	}

	private WCheckBoxMenuItem menuShowEditorToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.editor", "Editor"));

	private WCheckBoxMenuItem menuShowInsertToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.insert", "Insert"));

	private WCheckBoxMenuItem menuShowOutlineToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.outline", "Outline"));

	private WCheckBoxMenuItem menuShowTextToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.text", "Text"));

	private WCheckBoxMenuItem menuShowParagraphToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.paragraph", "Paragraph"));

	private WCheckBoxMenuItem menuShowAlignToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.align", "Align"));

	private WCheckBoxMenuItem menuShowSameToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.same", "Same"));

	private WCheckBoxMenuItem menuShowTransfotmToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.transform", "Transform"));

	private WCheckBoxMenuItem menuShowPathOperationsToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.path.operations", "Path operations"));

	private WCheckBoxMenuItem menuShowIDToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.ID", "Object ID"));

	private WCheckBoxMenuItem menuShowDisplayToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.display", "Display"));

	private WCheckBoxMenuItem menuShowActionAreasToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.action.areas", "Action areas"));

	private WCheckBoxMenuItem menuShowArrowsToolbar = new WCheckBoxMenuItem(lm.getValue("button.show.toolbar.arrows", "Arrows"));

	private WMenuItem menuShowAll = new WMenuItem(lm.getValue("button.show.toolbar.all.show", "Show All"));

	private WMenuItem menuHideAll = new WMenuItem(lm.getValue("button.show.toolbar.all.hide", "Hide All"));

	private WCheckBoxMenuItem menuShowShapesPanel = new WCheckBoxMenuItem(lm.getValue("button.show.panel.shapes", "Shapes panel"));

	private WCheckBoxMenuItem menuShowSwitcherPanel = new WCheckBoxMenuItem(lm.getValue("button.show.panel.switcher", "Switcher panel"));

	private WCheckBoxMenuItem menuShowClipboardPanel = new WCheckBoxMenuItem(lm.getValue("button.show.panel.clipboard", "Clipboard panel"));

	private WCheckBoxMenuItem menuShowResourcesPanel = new WCheckBoxMenuItem(lm.getValue("button.show.panel.resources", "Resources panel"));

	private WCheckBoxMenuItem menuShowZoomPreviewPanel = new WCheckBoxMenuItem(lm.getValue("button.show.panel.zoom-preview", "Zoom preview panel"));

	private WCheckBoxMenuItem menuShowRules = new WCheckBoxMenuItem(lm.getValue("button.show.rules", "Rules"));

	private WCheckBoxMenuItem menuDrawGrid = new WCheckBoxMenuItem(lm.getValue("button.show.grid", "Show Grid"));

	private WCheckBoxMenuItem menuAlignToGrid = new WCheckBoxMenuItem(lm.getValue("button.show.grid.align", "Align to Grid"));

	private WCheckBoxMenuItem menuDrawGridAbove = new WCheckBoxMenuItem(lm.getValue("button.show.grid.above", "Draw Grid Above"));

	private WCheckBoxMenuItem menuFullScreen = new WCheckBoxMenuItem(lm.getValue("button.show.full-screen", "Full Screen"));

	private WCheckBoxMenuItem menuEnableConnections = new WCheckBoxMenuItem(lm.getValue("button.show.connections.enable", "Connections"));

	private WMenuItem menuDefineConnections = new WMenuItem(lm.getValue("button.show.connections.define", "Define connections"));

	private List<WCheckBoxMenuItem> viewCheckMenus = new ArrayList<>();

	public void updateViewMenus() {
		ignoreEvent = true;
		menuShowEditorToolbar.setSelected(editorToolbar.isVisible());
		menuShowInsertToolbar.setSelected(insertToolbar.isVisible());
		menuShowOutlineToolbar.setSelected(outlineToolbar.isVisible());
		menuShowTextToolbar.setSelected(textToolbar.isVisible());
		menuShowParagraphToolbar.setSelected(paragraphToolbar.isVisible());
		menuShowAlignToolbar.setSelected(alignToolbar.isVisible());
		menuShowSameToolbar.setSelected(sameToolbar.isVisible());
		menuShowTransfotmToolbar.setSelected(transformToolbar.isVisible());
		menuShowPathOperationsToolbar.setSelected(pathOperationsToolbar.isVisible());
		menuShowIDToolbar.setSelected(idToolbar.isVisible());
		menuShowDisplayToolbar.setSelected(displayToolbar.isVisible());
		menuShowActionAreasToolbar.setSelected(actionAreaToolbar.isVisible());
		menuShowArrowsToolbar.setSelected(arrowsToolbar.isVisible());
		menuShowShapesPanel.setSelected(editor.isShowShapes());
		menuShowSwitcherPanel.setSelected(editor.isShowSwitcher());
		menuShowClipboardPanel.setSelected(editor.isShowClipboard());
		menuShowResourcesPanel.setSelected(editor.isShowResources());
		menuShowZoomPreviewPanel.setSelected(editor.isShowZoomPreview());
		menuShowRules.setSelected(editor.isShowRules());
		menuDrawGrid.setSelected(editor.isDrawGrid());
		menuAlignToGrid.setSelected(editor.isGridAlign());
		menuDrawGridAbove.setSelected(editor.isDrawGridAbove());
		menuEnableConnections.setSelected(editor.isConnectionsEnabled());
		menuFullScreen.setSelected(editor.isFullScreen());

		menuDefineConnections.setEnabled(editor.isConnectionsEnabled());
		ignoreEvent = false;
	}

	protected void addViewMenus(JMenu menu) {
		menuShowEditorToolbar.setName("button.show.toolbar.editor");
		menuShowInsertToolbar.setName("button.show.toolbar.insert");
		menuShowOutlineToolbar.setName("button.show.toolbar.outline");
		menuShowTextToolbar.setName("button.show.toolbar.text");
		menuShowParagraphToolbar.setName("button.show.toolbar.paragraph");
		menuShowAlignToolbar.setName("button.show.toolbar.align");
		menuShowSameToolbar.setName("button.show.toolbar.same");
		menuShowTransfotmToolbar.setName("button.show.toolbar.transform");
		menuShowPathOperationsToolbar.setName("button.show.toolbar.path.operations");
		menuShowIDToolbar.setName("button.show.toolbar.ID");
		menuShowDisplayToolbar.setName("button.show.toolbar.display");
		menuShowActionAreasToolbar.setName("button.show.toolbar.action.areas");
		menuShowArrowsToolbar.setName("button.show.toolbar.arrows");
		menuShowAll.setName("button.show.toolbar.all.show");
		menuHideAll.setName("button.show.toolbar.all.hide");
		menuShowShapesPanel.setName("button.show.panel.shapes");
		menuShowSwitcherPanel.setName("button.show.panel.switcher");
		menuShowClipboardPanel.setName("button.show.panel.clipboard");
		menuShowResourcesPanel.setName("button.show.panel.resources");
		menuShowZoomPreviewPanel.setName("button.show.panel.zoom-preview");
		menuShowRules.setName("button.show.rules");
		menuDrawGrid.setName("button.show.grid");
		menuAlignToGrid.setName("button.show.grid.align");
		menuDrawGridAbove.setName("button.show.grid.above");
		menuFullScreen.setName("button.show.full-screen");
		menuEnableConnections.setName("button.show.connections.enable");
		menuDefineConnections.setName("button.show.connections.define");

		viewCheckMenus.add(menuShowShapesPanel);
		viewCheckMenus.add(menuShowSwitcherPanel);
		viewCheckMenus.add(menuShowClipboardPanel);
		viewCheckMenus.add(menuShowResourcesPanel);
		viewCheckMenus.add(menuShowZoomPreviewPanel);
		viewCheckMenus.add(menuShowRules);
		viewCheckMenus.add(menuDrawGrid);
		viewCheckMenus.add(menuAlignToGrid);
		viewCheckMenus.add(menuDrawGridAbove);
		viewCheckMenus.add(menuFullScreen);
		viewCheckMenus.add(menuEnableConnections);

		menuFullScreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

		toolbarMenus.add(menuShowEditorToolbar);
		toolbarMenus.add(menuShowInsertToolbar);
		toolbarMenus.add(menuShowOutlineToolbar);
		toolbarMenus.add(menuShowTextToolbar);
		toolbarMenus.add(menuShowParagraphToolbar);
		toolbarMenus.add(menuShowAlignToolbar);
		toolbarMenus.add(menuShowSameToolbar);
		toolbarMenus.add(menuShowTransfotmToolbar);
		toolbarMenus.add(menuShowPathOperationsToolbar);
		toolbarMenus.add(menuShowIDToolbar);
		toolbarMenus.add(menuShowDisplayToolbar);
		toolbarMenus.add(menuShowActionAreasToolbar);
		toolbarMenus.add(menuShowArrowsToolbar);

		updateViewMenus();

		menuShowEditorToolbar.setActionCommand("show-editor-toolbar");
		menuShowInsertToolbar.setActionCommand("show-insert-toolbar");
		menuShowOutlineToolbar.setActionCommand("show-outline-toolbar");
		menuShowTextToolbar.setActionCommand("show-text-toolbar");
		menuShowParagraphToolbar.setActionCommand("show-paragraph-toolbar");
		menuShowAlignToolbar.setActionCommand("show-align-toolbar");
		menuShowSameToolbar.setActionCommand("show-same-toolbar");
		menuShowTransfotmToolbar.setActionCommand("show-transform-toolbar");
		menuShowPathOperationsToolbar.setActionCommand("show-path-operations-toolbar");
		menuShowIDToolbar.setActionCommand("show-id-toolbar");
		menuShowDisplayToolbar.setActionCommand("show-display-toolbar");
		menuShowActionAreasToolbar.setActionCommand("show-actionarea-toolbar");
		menuShowArrowsToolbar.setActionCommand("show-arrows-toolbar");
		menuShowAll.setActionCommand("show-all-toolbars");
		menuHideAll.setActionCommand("hide-all-toolbars");
		menuShowShapesPanel.setActionCommand("show-shapes-frame");
		menuDrawGridAbove.setActionCommand("draw-grid-above");
		menuAlignToGrid.setActionCommand("align-to-grid");
		menuEnableConnections.setActionCommand("enable-connections");
		menuDefineConnections.setActionCommand("define-connections");
		menuFullScreen.setActionCommand("full-screen");
		menuShowSwitcherPanel.setActionCommand("show-switcher");
		menuShowClipboardPanel.setActionCommand("show-clipboard");
		menuShowResourcesPanel.setActionCommand("show-resources");
		menuShowZoomPreviewPanel.setActionCommand("show-zoom-preview");
		menuShowRules.setActionCommand("show-rules");
		menuDrawGrid.setActionCommand("show-grid");

		menuShowEditorToolbar.addItemListener(this);
		menuShowInsertToolbar.addItemListener(this);
		menuShowOutlineToolbar.addItemListener(this);
		menuShowTextToolbar.addItemListener(this);
		menuShowParagraphToolbar.addItemListener(this);
		menuShowAlignToolbar.addItemListener(this);
		menuShowSameToolbar.addItemListener(this);
		menuShowTransfotmToolbar.addItemListener(this);
		menuShowShapesPanel.addItemListener(this);
		menuShowSwitcherPanel.addItemListener(this);
		menuShowClipboardPanel.addItemListener(this);
		menuShowResourcesPanel.addItemListener(this);
		menuShowZoomPreviewPanel.addItemListener(this);
		menuShowRules.addItemListener(this);
		menuDrawGrid.addItemListener(this);
		menuAlignToGrid.addItemListener(this);
		menuShowPathOperationsToolbar.addItemListener(this);
		menuShowIDToolbar.addItemListener(this);
		menuShowDisplayToolbar.addItemListener(this);
		menuShowActionAreasToolbar.addItemListener(this);
		menuShowArrowsToolbar.addItemListener(this);
		menuShowAll.addActionListener(this);
		menuHideAll.addActionListener(this);
		menuDrawGridAbove.addItemListener(this);
		menuEnableConnections.addItemListener(this);
		menuDefineConnections.addItemListener(this);
		menuFullScreen.addItemListener(this);

		WMenu menuToolbars = new WMenu(lm.getValue("button.toolbars", "Toolbars"));
		menuToolbars.add(menuShowEditorToolbar);
		menuToolbars.add(menuShowInsertToolbar);
		menuToolbars.add(menuShowOutlineToolbar);
		menuToolbars.add(menuShowTextToolbar);
		menuToolbars.add(menuShowParagraphToolbar);
		menuToolbars.add(menuShowAlignToolbar);
		menuToolbars.add(menuShowSameToolbar);
		menuToolbars.add(menuShowTransfotmToolbar);
		menuToolbars.add(menuShowPathOperationsToolbar);
		menuToolbars.add(menuShowIDToolbar);
		menuToolbars.add(menuShowDisplayToolbar);
		menuToolbars.add(menuShowActionAreasToolbar);
		menuToolbars.add(menuShowArrowsToolbar);
		menuToolbars.add(new WSeparator());
		menuToolbars.add(menuShowAll);
		menuToolbars.add(menuHideAll);

		menuToolbars.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				boolean selected = true;
				boolean hided = true;
				for (WCheckBoxMenuItem item : toolbarMenus) {
					selected &= item.isSelected();
					hided &= !item.isSelected();
				}
				menuShowAll.setEnabled(!selected);
				menuHideAll.setEnabled(!hided);
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});

		menuEnableConnections.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuDefineConnections.setEnabled(menuEnableConnections.isSelected());
			}
		});

		// --- create menu ---
		menu.add(menuToolbars);

		menu.add(new WSeparator());

		menu.add(menuShowShapesPanel);
		menu.add(menuShowSwitcherPanel);
		menu.add(menuShowClipboardPanel);
		menu.add(menuShowResourcesPanel);
		menu.add(menuShowZoomPreviewPanel);

		menu.add(new WSeparator());

		menu.add(menuShowRules);
		menu.add(menuDrawGrid);
		menu.add(menuAlignToGrid);
		menu.add(menuDrawGridAbove);

		menu.add(new WSeparator());

		menu.add(menuEnableConnections);
		menu.add(menuDefineConnections);

		menu.add(new WSeparator());

		menu.add(menuFullScreen);
	}

	protected void addInsertMenus(JMenu menu) {
		// create menu
		menu.add(actionMouse.getMenuItem());
		menu.add(actionText.getMenuItem());
		menu.add(actionTextField.getMenuItem());
		menu.add(actionImage.getMenuItem());
		menu.add(actionPath.getMenuItem());
		menu.add(actionTextPath.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionMultiArrowPath.getMenuItem());
		menu.add(actionAddSubArrow.getMenuItem());

		menu.add(new WSeparator());

		menu.add(actionLine.getMenuItem());
		menu.add(actionRect.getMenuItem());
		menu.add(actionEllipse.getMenuItem());
	}

	protected void addTransformMenus(JMenu menu) {
		// align selection relative to the focused component
		WMenu menuAlignByFocus = new WMenu(lm.getValue("button.align.relative.focus", "Align relative focus"));
		menuAlignByFocus.add(actionAlignByFocusTop.getMenuItem());
		menuAlignByFocus.add(actionAlignByFocusLeft.getMenuItem());
		menuAlignByFocus.add(actionAlignByFocusBottom.getMenuItem());
		menuAlignByFocus.add(actionAlignByFocusRight.getMenuItem());
		menuAlignByFocus.add(actionAlignByFocusCenter.getMenuItem());
		menuAlignByFocus.add(actionAlignByFocusCenterHor.getMenuItem());
		menuAlignByFocus.add(actionAlignByFocusCenterVer.getMenuItem());

		// same size
		WMenu menuSame = new WMenu(lm.getValue("button.same", "Set to the same"));
		menuSame.add(actionSameWidth.getMenuItem());
		menuSame.add(actionSameHeight.getMenuItem());
		menuSame.add(actionSameSize.getMenuItem());

		// spacing
		WMenu menuSpaces = new WMenu(lm.getValue("button.same.spaces", "Spacing"));
		menuSpaces.add(actionSameSpacesVer.getMenuItem());
		menuSpaces.add(actionSameSpacesHor.getMenuItem());

		// align selection relative to the pane
		WMenu menuAlign = new WMenu(lm.getValue("button.align.relative.pane", "Align relative pane"));
		menuAlign.add(actionAlignLeft.getMenuItem());
		menuAlign.add(actionAlignRight.getMenuItem());
		menuAlign.add(actionAlignTop.getMenuItem());
		menuAlign.add(actionAlignBottom.getMenuItem());
		menuAlign.add(actionAlignCenter.getMenuItem());
		menuAlign.add(actionAlignCenterHor.getMenuItem());
		menuAlign.add(actionAlignCenterVer.getMenuItem());

		// order
		WMenu menuOrder = new WMenu(lm.getValue("button.order", "Order"));
		menuOrder.add(actionToFront.getMenuItem());
		menuOrder.add(actionToUp.getMenuItem());
		menuOrder.add(actionToDown.getMenuItem());
		menuOrder.add(actionToBack.getMenuItem());

		// rotate
		WMenu menuRotate = new WMenu(lm.getValue("button.rotate", "Rotate"));
		menuRotate.add(actionRotate90.getMenuItem());
		menuRotate.add(actionRotateMinus90.getMenuItem());
		menuRotate.add(actionRotate180.getMenuItem());

		// flip
		WMenu menuFlip = new WMenu(lm.getValue("button.flip", "Flip"));
		menuFlip.add(actionFlipHor.getMenuItem());
		menuFlip.add(actionFlipVer.getMenuItem());

		// create menu
		menu.add(menuAlignByFocus);
		menu.add(menuSame);
		menu.add(menuSpaces);

		menu.add(new WSeparator());

		menu.add(menuAlign);
		menu.add(menuOrder);
		menu.add(menuRotate);
		menu.add(menuFlip);

		menu.add(new WSeparator());

		WMenuItem menuGroupBy0 = new WMenuItem(lm.getValue("menu.group.action-order.components", "By components"));
		WMenuItem menuGroupBy1 = new WMenuItem(lm.getValue("menu.group.action-order.first-outline", "First outlines"));
		WMenuItem menuGroupBy2 = new WMenuItem(lm.getValue("menu.group.action-order.first-fill", "First fill"));
		int index = 0;
		for (WMenuItem m : new WMenuItem[] { menuGroupBy0, menuGroupBy1, menuGroupBy2 }) {
			final int paintOrderType = index;
			m.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					GroupPaintOrderAction action = new GroupPaintOrderAction(paintOrderType);
					action.doAction();
				};
			});
			menuGroupPaintOrder.add(m);
			index++;
		}

		menu.add(actionGroup.getMenuItem());
		menu.add(menuGroupPaintOrder);
	}

	protected void addHelpMenus(JMenu menu) {
		WMenuItem menuAbout = new WMenuItem(lm.getValue("button.about", "About"));
		menuAbout.setActionCommand("about");
		menuAbout.addActionListener(this);
		menu.add(menuAbout);
	}

	private void setToolComponent(JComponent c) {
		Dimension s = c.getPreferredSize();
		s.height = 20;
		c.setPreferredSize(s);
		c.setRequestFocusEnabled(false);
	}

	public abstract class ComponentAction implements ActionListener {
		protected String actionCommand;

		protected ActionListener actionListener;

		protected Icon icon;

		protected KeyStroke accelerator;

		public ComponentAction(String name, String actionCommand, ActionListener actionListener, String iconName, KeyStroke accelerator) {
			String path = "/ru/nest/jvg/editor/img/" + iconName;
			Icon icon = null;
			try {
				icon = new ImageIcon(JVGEditor.class.getResource(path));
			} catch (Exception exc) {
				System.err.println("Can't find image: " + path);
			}
			set(name, actionCommand, actionListener, icon, accelerator);
		}

		public ComponentAction(String name, String actionCommand, ActionListener actionListener, Icon icon, KeyStroke accelerator) {
			set(name, actionCommand, actionListener, icon, accelerator);
		}

		protected void set(String name, String actionCommand, ActionListener actionListener, Icon icon, KeyStroke accelerator) {
			this.name = name;
			this.actionCommand = actionCommand;
			this.actionListener = actionListener;
			this.accelerator = accelerator;

			actionsMap.put(actionCommand, this);

			if (icon == null || icon.getIconHeight() == -1 || icon.getIconWidth() == -1) {
				icon = new EmptyIcon();
			}
			this.icon = icon;
		}

		private AbstractButton btn;

		public AbstractButton getButton() {
			if (btn == null) {
				btn = createButton();
				if (btn.getToolTipText() == null) {
					btn.setToolTipText(name);
				}
				btn.addActionListener(this);

				btn.setEnabled(enabled);
				btn.setSelected(selected);
			}
			return btn;
		}

		private AbstractButton menu;

		public AbstractButton getMenuItem() {
			if (menu == null) {
				menu = createMenu();
				menu.setActionCommand(actionCommand);
				menu.addActionListener(actionListener);
				menu.addActionListener(this);
				if (accelerator != null && menu instanceof JMenuItem) {
					JMenuItem item = (JMenuItem) menu;
					item.setAccelerator(accelerator);
				}

				menu.setEnabled(enabled);
				menu.setSelected(selected);
			}
			return menu;
		}

		private AbstractButton menuContext;

		public AbstractButton getContextMenuItem() {
			if (menuContext == null) {
				menuContext = createMenu();
				menuContext.setActionCommand(actionCommand);
				menuContext.addActionListener(actionListener);
				menuContext.addActionListener(this);

				menuContext.setEnabled(enabled);
				menuContext.setSelected(selected);
			}
			return menuContext;
		}

		protected String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;

			if (btn != null) {
				btn.setToolTipText(name);
			}

			if (menu != null) {
				menu.setText(name);
			}

			if (menuContext != null) {
				menuContext.setText(name);
			}
		}

		private boolean enabled = true;

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;

			if (btn != null) {
				btn.setEnabled(enabled);
			}

			if (menu != null) {
				menu.setEnabled(enabled);
			}

			if (menuContext != null) {
				menuContext.setEnabled(enabled);
			}
		}

		public boolean isEnabled() {
			return enabled;
		}

		private boolean selected = false;

		public void setSelected(boolean selected) {
			this.selected = selected;

			if (btn != null) {
				btn.setSelected(selected);
			}

			if (menu != null) {
				menu.setSelected(selected);
			}

			if (menuContext != null) {
				menuContext.setSelected(selected);
			}
		}

		public boolean isSelected() {
			return selected;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btn) {
				selected = btn.isSelected();
			} else if (e.getSource() == menu) {
				selected = menu.isSelected();
			} else if (e.getSource() == menuContext) {
				selected = menuContext.isSelected();
			}

			if (btn != null && btn.isSelected() != selected) {
				btn.setSelected(selected);
			}

			if (menu != null && menu.isSelected() != selected) {
				menu.setSelected(selected);
			}

			if (menuContext != null && menuContext.isSelected() != selected) {
				menuContext.setSelected(selected);
			}
		}

		public void addActionListener(ActionListener actionListener) {
			if (btn != null) {
				btn.addActionListener(actionListener);
			}

			if (menu != null) {
				menu.addActionListener(actionListener);
			}

			if (menuContext != null) {
				menuContext.addActionListener(actionListener);
			}
		}

		public void removeActionListener(ActionListener actionListener) {
			if (btn != null) {
				btn.removeActionListener(actionListener);
			}

			if (menu != null) {
				menu.removeActionListener(actionListener);
			}

			if (menuContext != null) {
				menuContext.removeActionListener(actionListener);
			}
		}

		public abstract AbstractButton createButton();

		public abstract AbstractButton createMenu();
	}

	public class ButtonAction extends ComponentAction {
		public ButtonAction(String name, String actionCommand, ActionListener actionListener, Icon icon) {
			super(name, actionCommand, actionListener, icon, null);
		}

		public ButtonAction(String name, String actionCommand, ActionListener actionListener, String iconName) {
			super(name, actionCommand, actionListener, iconName, null);
		}

		public ButtonAction(String name, String actionCommand, ActionListener actionListener, Icon icon, KeyStroke accelerator) {
			super(name, actionCommand, actionListener, icon, accelerator);
		}

		public ButtonAction(String name, String actionCommand, ActionListener actionListener, String iconName, KeyStroke accelerator) {
			super(name, actionCommand, actionListener, iconName, accelerator);
		}

		@Override
		public AbstractButton createButton() {
			ToolButton btn = new ToolButton(icon, actionCommand, actionListener);
			if (btn.getToolTipText() == null) {
				btn.setToolTipText(name);
			}
			return btn;
		}

		@Override
		public AbstractButton createMenu() {
			return new WMenuItem(name, icon);
		}
	}

	public class ToggleButtonAction extends ComponentAction {
		public ToggleButtonAction(String name, String actionCommand, ActionListener actionListener, String iconName) {
			super(name, actionCommand, actionListener, iconName, null);
		}

		public ToggleButtonAction(String name, String actionCommand, ActionListener actionListener, String iconName, KeyStroke accelerator) {
			super(name, actionCommand, actionListener, iconName, accelerator);
		}

		@Override
		public AbstractButton createButton() {
			return new ToolToggleButton(icon, actionCommand, actionListener);
		}

		@Override
		public AbstractButton createMenu() {
			return new WCheckBoxMenuItem(name, icon);
		}
	}

	public class ToolButton extends IconButton {
		public ToolButton(Icon icon, String actionCommand, ActionListener actionListener) {
			super(icon);
			setActionCommand(actionCommand);
			addActionListener(actionListener);
			setPreferredSize(new Dimension(24, 24));
			setRequestFocusEnabled(false);
		}
	}

	public class ToolToggleButton extends IconToggleButton {
		public ToolToggleButton(Icon icon, String actionCommand, ActionListener actionListener) {
			super(icon);
			setActionCommand(actionCommand);
			addActionListener(actionListener);
			setPreferredSize(new Dimension(24, 24));
			setRequestFocusEnabled(false);
		}
	}

	public class EditorGroup {
		private ButtonGroup groupButton = new ButtonGroup();

		private ButtonGroup groupMenu = new ButtonGroup();

		private ButtonGroup groupContextMenu = new ButtonGroup();

		public void add(ComponentAction action) {
			groupButton.add(action.getButton());
			groupMenu.add(action.getMenuItem());
			groupContextMenu.add(action.getContextMenuItem());
		}
	}

	protected void update(JVGStyledText t) {
		StyledDocument doc = t.getDocument();
		int pos = Math.min(t.getCaretPosition(), doc.getLength() - 1);
		AttributeSet attr = doc.getCharacterElement(pos == 0 ? 0 : (pos - 1)).getAttributes();

		String fontFamily = StyleConstants.getFontFamily(attr);
		for (int i = 0; i < cmbFontFamily.getItemCount(); i++) {
			Font font = cmbFontFamily.getItemAt(i);
			if (font.getFamily().equalsIgnoreCase(fontFamily)) {
				Util.selectWithoutEvent(cmbFontFamily, i);
				break;
			}
		}

		String fontSize = Integer.toString(StyleConstants.getFontSize(attr));
		for (int i = 0; i < cmbFontSize.getItemCount(); i++) {
			String val = cmbFontSize.getItemAt(i).toString();
			if (val.equals(fontSize)) {
				Util.selectWithoutEvent(cmbFontSize, i);
				break;
			}
		}

		actionBold.setSelected(StyleConstants.isBold(attr));
		actionItalic.setSelected(StyleConstants.isItalic(attr));
		actionUnderline.setSelected(JVGStyleConstants.getUnderline(attr) != JVGStyleConstants.UNDERLINE_NONE);
		actionStrike.setSelected(StyleConstants.isStrikeThrough(attr));
		actionSubscript.setSelected(StyleConstants.isSubscript(attr));
		actionSuperscript.setSelected(StyleConstants.isSuperscript(attr));

		attr = doc.getParagraphElement(pos).getAttributes();
		int alignment = StyleConstants.getAlignment(attr);
		actionLeftAlignment.setSelected(false);
		actionCenterAlignment.setSelected(false);
		actionRightAlignment.setSelected(false);
		actionJustifyAlignment.setSelected(false);
		switch (alignment) {
			case StyleConstants.ALIGN_LEFT:
				actionLeftAlignment.setSelected(true);
				break;

			case StyleConstants.ALIGN_CENTER:
				actionCenterAlignment.setSelected(true);
				break;

			case StyleConstants.ALIGN_RIGHT:
				actionRightAlignment.setSelected(true);
				break;

			case StyleConstants.ALIGN_JUSTIFIED:
				actionJustifyAlignment.setSelected(true);
				break;
		}
	}

	// toolbars
	private String[] selectionTypeValues = { lm.getValue("selection.type.none", "No"), lm.getValue("selection.type.area", "Area"), lm.getValue("selection.type.lasso", "Lasso") };

	private Icon[] selectionTypeIcons = { new ImageIcon(JVGEditor.class.getResource("/ru/nest/jvg/editor/img/selection_none.png")), new ImageIcon(JVGEditor.class.getResource("/ru/nest/jvg/editor/img/selection_rect.png")), new ImageIcon(JVGEditor.class.getResource("/ru/nest/jvg/editor/img/selection_lasso.png")) };

	protected JComboBox<String> cmbSelectionType = new WComboBox<>(selectionTypeValues);

	protected JComboBox<String> cmbCurveTo = new WComboBox<>(new String[] { lm.getValue("path.moveto", "Move"), lm.getValue("path.lineto", "Line"), lm.getValue("path.quadto", "Quad"), lm.getValue("path.curveto", "Cubic") });

	private final static int[] ZOOMINGS = { 10, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 500, 1000, -1 };

	protected JComboBox<String> cmbZoom = new WComboBox<>(new String[] { " " + lm.getValue("zoom.10%", "10%") + " ", //
			" " + lm.getValue("zoom.25%", "25%") + " ", //
			" " + lm.getValue("zoom.50%", "50%") + " ", //
			" " + lm.getValue("zoom.75%", "75%") + " ", //
			" " + lm.getValue("zoom.100%", "100%") + " ", //
			" " + lm.getValue("zoom.125%", "125%") + " ", //
			" " + lm.getValue("zoom.150%", "150%") + " ", //
			" " + lm.getValue("zoom.175%", "175%") + " ", //
			" " + lm.getValue("zoom.200%", "200%") + " ", //
			" " + lm.getValue("zoom.225%", "225%") + " ", //
			" " + lm.getValue("zoom.250%", "250%") + " ", //
			" " + lm.getValue("zoom.500%", "500%") + " ", //
			" " + lm.getValue("zoom.1000%", "1000%") + " ", //
			" " + lm.getValue("zoom.other", "Other") + " " });

	private int getZoom(int index) {
		if (index >= 0 && index < ZOOMINGS.length - 1) {
			return ZOOMINGS[index];
		} else if (index == ZOOMINGS.length - 1) {
			JVGPaneInternalFrame frame = editor.getCurrentFrame();
			int zoom = (int) (100 * frame.getPane().getZoom());

			JTextField txt = new IntegerTextField(zoom, false);
			txt.setFocusCycleRoot(true);
			txt.selectAll();
			int option = JOptionPane.showConfirmDialog(null, txt, JVGLocaleManager.getInstance().getValue("zoom.dialog.title", "Enter zoom value"), JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				try {
					int value = Integer.parseInt(txt.getText());
					return value;
				} catch (Exception exc) {
					exc.printStackTrace();
					return 100;
				}
			} else {
				return -1;
			}
		}
		return 100;
	}

	public void setZoom(double zoomPercent) {
		double zoom = zoomPercent / 100.0;
		if (zoom < 0) {
			return;
		}

		Point2D percentCenterPoint = editor.getCurrentFrame().getCenterPointInPercent();

		editor.getCurrentPane().setZoom(zoom);
		editor.getZoomPreviewPane().repaint();

		int selectedIndex = cmbZoom.getSelectedIndex();
		for (int i = 0; i < ZOOMINGS.length; i++) {
			if (zoomPercent == ZOOMINGS[i]) {
				if (i != selectedIndex) {
					selectedIndex = i;

					ignoreZoomAction = true;
					cmbZoom.setSelectedIndex(selectedIndex);
					ignoreZoomAction = false;
				}
				break;
			}
		}

		if (selectedIndex == ZOOMINGS.length - 1) {
			selectedIndex = -1;

			ignoreZoomAction = true;
			cmbZoom.setSelectedIndex(selectedIndex);
			ignoreZoomAction = false;
		}

		editor.getCurrentFrame().setCenterPointInPercent(percentCenterPoint);
		cmbZoom.repaint();
	}

	private boolean ignoreZoomAction = false;

	private WToolBar getEditorToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.editor", "Editor"));
		toolBar.setName("toolbar.editor");

		cmbZoom.setSelectedIndex(4);
		cmbZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!ignoreZoomAction && editor.getCurrentFrame() != null) {
					int selectedIndex = cmbZoom.getSelectedIndex();
					if (selectedIndex != -1 && selectedIndex < ZOOMINGS.length) {
						int zoomPercent = getZoom(selectedIndex);
						setZoom(zoomPercent);
					}
				}
			}
		});
		final ListCellRenderer zoomRenderer = cmbZoom.getRenderer();
		cmbZoom.setRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel lbl = (JLabel) zoomRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				lbl.setHorizontalAlignment(SwingConstants.RIGHT);
				if (index == -1) {
					JVGEditPane pane = editor.getCurrentPane();
					if (pane != null) {
						lbl.setText(Integer.toString((int) (100 * editor.getCurrentPane().getZoom())) + "%");
					}
				}
				return lbl;
			}
		});
		setToolComponent(cmbZoom);

		cmbSelectionType.setSelectedIndex(1);
		cmbSelectionType.setActionCommand("update-selection-type");
		cmbSelectionType.addActionListener(this);
		final ListCellRenderer renderer = cmbSelectionType.getRenderer();
		cmbSelectionType.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel l = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (index == -1) {
					for (int i = 0; i < selectionTypeValues.length; i++) {
						if (selectionTypeValues[i].equals(value)) {
							index = i;
							break;
						}
					}
				}
				if (index != -1) {
					l.setIcon(selectionTypeIcons[index]);
				}
				return l;
			}
		});
		setToolComponent(cmbSelectionType);

		// --- create tool bar ---
		toolBar.add(actionNew.getButton());
		toolBar.add(actionOpen.getButton());
		toolBar.add(actionSave.getButton());
		toolBar.add(actionSaveAll.getButton());
		toolBar.add(actionUndo.getButton());
		toolBar.add(actionRedo.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(actionCut.getButton());
		toolBar.add(actionCopy.getButton());
		toolBar.add(actionPaste.getButton());
		toolBar.add(actionDelete.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(cmbZoom);
		toolBar.add(actionSelectAll.getButton());
		toolBar.add(cmbSelectionType);
		toolBar.add(actionInitialBounds.getButton());

		return toolBar;
	}

	private WToolBar getInsertToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.insert", "Insert"));
		toolBar.setName("toolbar.insert");

		cmbCurveTo.setSelectedIndex(editor.getPathAppendType());
		cmbCurveTo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editor.setPathAppendType(cmbCurveTo.getSelectedIndex());
			}
		});
		setToolComponent(cmbCurveTo);

		// --- create tool bar ---
		toolBar.add(actionMouse.getButton());
		toolBar.add(actionText.getButton());
		toolBar.add(actionTextField.getButton());
		toolBar.add(actionImage.getButton());
		toolBar.add(actionPath.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(actionPencilAdjusted.getButton());
		toolBar.add(actionPencil.getButton());
		toolBar.add(actionMarker.getButton());
		toolBar.add(actionEraser.getButton());
		toolBar.add(actionEraserLasso.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(actionLine.getButton());
		toolBar.add(actionRect.getButton());
		toolBar.add(actionEllipse.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(actionGroup.getButton());
		return toolBar;
	}

	private WToolBar getTextToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.text", "Text"));
		toolBar.setName("toolbar.text");

		// font family
		cmbFontFamily.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cmb = (JComboBox) e.getSource();
				Font font = (Font) cmb.getSelectedItem();
				FontFamilyAction action = new FontFamilyAction(font.getFamily(), font.getFamily());
				action.actionPerformed(e);
			}
		});
		setToolComponent(cmbFontFamily);

		// font size
		for (int i = 1; i <= 100; i++) {
			cmbFontSize.addItem(new FontSizeAction(Integer.toString(i), i));
		}
		cmbFontSize.setSelectedIndex(13);
		cmbFontSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cmb = (JComboBox) e.getSource();
				FontSizeAction action = (FontSizeAction) cmb.getSelectedItem();
				action.actionPerformed(e);
			}
		});
		setToolComponent(cmbFontSize);

		// --- create tool bar ---
		toolBar.add(actionTextWrap.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(cmbFontFamily);
		toolBar.add(cmbFontSize);
		toolBar.add(actionBold.getButton());
		toolBar.add(actionItalic.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(actionUnderline.getButton());
		toolBar.add(actionStrike.getButton());
		toolBar.add(actionSubscript.getButton());
		toolBar.add(actionSuperscript.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(btnTextBackground);
		toolBar.add(btnTextForeground);

		return toolBar;
	}

	private WToolBar getParagraphToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.paragraph", "Paragraph"));
		toolBar.setName("toolbar.paragraph");

		// --- create tool bar ---
		toolBar.add(actionLeftAlignment.getButton());
		toolBar.add(actionCenterAlignment.getButton());
		toolBar.add(actionRightAlignment.getButton());
		toolBar.add(actionJustifyAlignment.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(actionBullets.getButton());
		toolBar.add(actionBulletsNumber.getButton());
		toolBar.add(actionBulletsShiftLeft.getButton());
		toolBar.add(actionBulletsShiftRight.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(actionDecreaseLineSpacing.getButton());
		toolBar.add(actionIncreaseLineSpacing.getButton());
		toolBar.add(actionIncreaseParagraphLeftIndent.getButton());
		toolBar.add(actionDecreaseParagraphLeftIndent.getButton());
		toolBar.add(actionDecreaseParagraphSpacing.getButton());
		toolBar.add(actionIncreaseParagraphSpacing.getButton());

		return toolBar;
	}

	private WToolBar getAlignToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.align", "Align"));
		toolBar.setName("toolbar.align");

		toolBar.add(actionAlignLeft.getButton());
		toolBar.add(actionAlignCenterHor.getButton());
		toolBar.add(actionAlignRight.getButton());
		toolBar.add(actionAlignTop.getButton());
		toolBar.add(actionAlignCenterVer.getButton());
		toolBar.add(actionAlignBottom.getButton());

		return toolBar;
	}

	private WToolBar getSameToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.same", "Same"));
		toolBar.setName("toolbar.same");

		toolBar.add(actionSameWidth.getButton());
		toolBar.add(actionSameHeight.getButton());
		toolBar.add(actionSameSpacesHor.getButton());
		toolBar.add(actionSameSpacesVer.getButton());

		return toolBar;
	}

	private WToolBar getIDToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.id", "ID"));
		toolBar.setName("toolbar.id");

		txtID.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		txtID.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JVGPane pane = editor.getCurrentPane();
				if (pane != null) {
					JVGComponent focusOwner = pane.getFocusOwner();
					if (focusOwner != null) {
						focusOwner.setName(txtID.getText());
					}
					pane.requestFocus();
				}
			}
		});
		txtID.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				JVGPane pane = editor.getCurrentPane();
				if (pane != null) {
					JVGComponent focusOwner = pane.getFocusOwner();
					if (focusOwner != null) {
						focusOwner.setName(txtID.getText());
					}
				}
			}
		});

		toolBar.add(new JLabel(lm.getValue("toolbar.id.name", "Name: ")));
		toolBar.add(txtID);
		return toolBar;
	}

	private WToolBar getDisplayToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.display", "Display"));
		toolBar.setName("toolbar.display");

		sliderAlfaLayer.setRequestFocusEnabled(false);
		sliderAlfaLayer.setOpaque(false);
		sliderAlfaLayer.setPreferredSize(new Dimension(35, 18));
		sliderAlfaLayer.getSlider().setPreferredSize(new Dimension(100, 20));
		sliderAlfaLayer.getSlider().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JVGPane pane = editor.getCurrentPane();
				if (pane != null && sliderAlfaLayerActionEnabled) {
					Action action = new AlfaLayerAction((int) (sliderAlfaLayer.getValue() * 2.55));
					action.actionPerformed(null);
					pane.requestFocus();
				}
			}
		});

		toolBar.add(new JLabel(lm.getValue("toolbar.display.opacity", "Opacity: ")));
		toolBar.add(sliderAlfaLayer);
		return toolBar;
	}

	private WToolBar getActionAreaToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.action-area", "Action Areas"));
		toolBar.setName("toolbar.action-area");

		toolBar.add(noActionActionArea.getButton());
		toolBar.add(actionScaleActionArea.getButton());
		toolBar.add(actionShearActionArea.getButton());
		toolBar.add(actionVectorActionArea.getButton());
		toolBar.add(new ToolSeparator(10, 24));
		toolBar.add(actionRotateActionArea.getButton());
		toolBar.add(actionCoordinateActionArea.getButton());
		toolBar.add(actionConnectionActionArea.getButton());
		toolBar.add(actionMoveActionArea.getButton());
		return toolBar;
	}

	private WToolBar getTransformToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.transform", "Transform"));
		toolBar.setName("toolbar.transform");

		toolBar.add(actionFlipHor.getButton());
		toolBar.add(actionFlipVer.getButton());
		toolBar.add(actionTranslateUp.getButton());
		toolBar.add(actionTranslateLeft.getButton());
		toolBar.add(actionTranslateDown.getButton());
		toolBar.add(actionTranslateRight.getButton());
		toolBar.add(actionRotate180.getButton());
		toolBar.add(actionRotate90.getButton());
		toolBar.add(actionRotateMinus90.getButton());
		return toolBar;
	}

	private WToolBar getOutlineToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.outline", "Outline"));
		toolBar.setName("toolbar.outline");

		toolBar.add(actionAntialias.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(cmbOutlineWidth);
		toolBar.add(cmbOutlinePattern);
		toolBar.add(btnOutlineColor);
		toolBar.add(cmbStrokeCap);
		toolBar.add(cmbStrokeJoin);

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(btnFillColor);
		toolBar.add(btnFillTransparency);

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(cmbEndingsPattern);
		toolBar.add(btnEndingsColor);

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(btnShadowType);
		toolBar.add(btnShadowColor);
		return toolBar;
	}

	private WToolBar getPathOperationsToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.path.operations", "Path Operations"));
		toolBar.setName("toolbar.path.operations");

		toolBar.add(actionToPath.getButton());
		toolBar.add(actionStrokePath.getButton());
		toolBar.add(actionFlatPath.getButton());
		toolBar.add(actionRemoveTransformPath.getButton());
		toolBar.add(actionEditPath.getButton());
		toolBar.add(actionSmoothPath.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(cmbCurveTo);
		toolBar.add(actionClosePath.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(actionUnion.getButton());
		toolBar.add(actionSubtraction.getButton());
		toolBar.add(actionIntersection.getButton());
		toolBar.add(actionExclusiveOr.getButton());
		return toolBar;
	}

	private WToolBar getArrowsToolBar() {
		WToolBar toolBar = new WToolBar(lm.getValue("toolbar.arrows", "Arrows"));
		toolBar.setName("toolbar.arrows");

		toolBar.add(actionMultiArrowPath.getButton());
		toolBar.add(actionAddSubArrow.getButton());
		toolBar.add(actionMoveSubArrow.getButton());

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(btnSubArrowWidth);
		toolBar.add(btnSubArrowEndCapWidth);
		toolBar.add(btnSubArrowEndCapLength);
		btnSubArrowWidth.setToolTipText(lm.getValue("button.multi-arrow-path.arrow-width.tooltip", "Arrow size"));
		btnSubArrowEndCapWidth.setToolTipText(lm.getValue("button.multi-arrow-path.arrow-end-cap-width.tooltip", "Arrow size"));
		btnSubArrowEndCapLength.setToolTipText(lm.getValue("button.multi-arrow-path.arrow-end-cap-length.tooltip", "Arrow size"));

		toolBar.add(new ToolSeparator(10, 24));

		toolBar.add(btnArrowDirectionChoose);
		btnArrowDirectionChoose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SetPathArrowStrokeAction action = new SetPathArrowStrokeAction(btnArrowDirectionChoose.getSelectedIndex());
				action.doAction();
			}
		});
		return toolBar;
	}

	// TODO do update throw timer(1sec)
	// TODO 2nd way - update only on change (isPathFocused, isPathEdited,...)
	public void updateDependedOnSelectionAndFocus() {
		if (freezeActions) {
			return;
		}
		forceUpdateDependedOnSelectionAndFocus();
	}

	public void forceUpdateDependedOnSelectionAndFocus() {
		actionGroup.setEnabled(false);
		actionGroup.setSelected(false);
		menuGroupPaintOrder.setEnabled(false);

		JVGShape focusedShape = null;
		int selectionSize = 0;
		int pathesCount = 0;
		int textFieldsCount = 0;
		int groupPathesCount = 0;
		boolean isPathFocused = false;
		boolean isTextPathFocused = false;
		boolean isTextFieldFocused = false;
		boolean isPathEdited = false;
		boolean isTextEdited = false, isTextWrap = false;
		boolean isAntialias = false;
		boolean isInitialBounds = false;
		boolean thereAreFocus = false;

		JVGPane pane = editor.getCurrentPane();
		if (pane != null) {
			JVGComponent focusOwner = pane.getFocusOwner();

			isPathFocused = focusOwner instanceof JVGPath;
			if (isPathFocused) {
				JVGPath path = (JVGPath) focusOwner;
				isPathEdited = EditPathAction.isEdited((JVGPath) focusOwner);
				isTextPathFocused = path.getPathStroke() != null && path.getPathStroke().getResource() instanceof TextStroke;
			}

			isTextEdited = focusOwner instanceof JVGStyledText;
			if (isTextEdited) {
				isTextWrap = ((JVGStyledText) focusOwner).isWrap();
			}

			isTextFieldFocused = focusOwner instanceof JVGTextField;

			JVGSelectionModel selectionModel = pane.getSelectionManager();
			selectionSize = selectionModel.getSelectionCount();

			JVGComponent[] selection = null;
			if (selectionSize == 1) {
				selection = selectionModel.getSelection();
				JVGComponent c = selection[0];
				if (c instanceof JVGGroup) {
					// ungroup
					actionGroup.setEnabled(true);
					actionGroup.setSelected(true);
					menuGroupPaintOrder.setEnabled(true);
				} else if (c instanceof JVGPath) {
					pathesCount = 1;
					if (c instanceof JVGGroupPath) {
						groupPathesCount = 1;
					}
				} else if (c instanceof JVGTextField) {
					textFieldsCount++;
				}
			} else if (selectionSize > 1) {
				// group
				actionGroup.setEnabled(true);
				actionGroup.setSelected(false);
				menuGroupPaintOrder.setEnabled(true);

				selection = selectionModel.getSelection();
				for (JVGComponent component : selection) {
					if (component instanceof JVGPath) {
						pathesCount++;
						if (component instanceof JVGGroupPath) {
							groupPathesCount++;
						}
					} else if (component instanceof JVGTextField) {
						textFieldsCount++;
					}
				}
			}

			if (focusOwner != null && focusOwner instanceof JVGShape) {
				focusedShape = (JVGShape) focusOwner;
			} else if (selectionSize > 0 && selection[0] instanceof JVGShape) {
				focusedShape = (JVGShape) selection[0];
			}
			thereAreFocus = focusedShape != null;

			if (focusedShape != null) {
				isAntialias = focusedShape.isAntialias();
				isInitialBounds = focusedShape.isOriginalBounds();
			}
		}

		boolean thereAre = selectionSize > 0;
		boolean moreOne = selectionSize > 1;
		boolean moreTwo = selectionSize > 2;

		actionSaveSelection.setEnabled(thereAre);
		actionCut.setEnabled(thereAre);
		actionCopy.setEnabled(thereAre);
		actionDelete.setEnabled(thereAre);

		actionAntialias.setEnabled(thereAre);
		actionAntialias.setSelected(isAntialias);

		actionInitialBounds.setEnabled(thereAre);
		actionInitialBounds.setSelected(isInitialBounds);

		actionAlignTop.setEnabled(thereAre);
		actionAlignLeft.setEnabled(thereAre);
		actionAlignBottom.setEnabled(thereAre);
		actionAlignRight.setEnabled(thereAre);
		actionAlignCenter.setEnabled(thereAre);
		actionAlignCenterHor.setEnabled(thereAre);
		actionAlignCenterVer.setEnabled(thereAre);
		actionSameWidth.setEnabled(moreOne && thereAreFocus);
		actionSameHeight.setEnabled(moreOne && thereAreFocus);
		actionSameSize.setEnabled(moreOne && thereAreFocus);
		actionSameSpacesHor.setEnabled(moreTwo);
		actionSameSpacesVer.setEnabled(moreTwo);
		actionFlipHor.setEnabled(thereAre);
		actionFlipVer.setEnabled(thereAre);
		actionTranslateUp.setEnabled(thereAre);
		actionTranslateLeft.setEnabled(thereAre);
		actionTranslateDown.setEnabled(thereAre);
		actionTranslateRight.setEnabled(thereAre);
		actionRotate180.setEnabled(thereAre);
		actionRotate90.setEnabled(thereAre);
		actionRotateMinus90.setEnabled(thereAre);

		actionAlignByFocusLeft.setEnabled(moreOne && thereAreFocus);
		actionAlignByFocusRight.setEnabled(moreOne && thereAreFocus);
		actionAlignByFocusTop.setEnabled(moreOne && thereAreFocus);
		actionAlignByFocusBottom.setEnabled(moreOne && thereAreFocus);
		actionAlignByFocusCenter.setEnabled(moreOne && thereAreFocus);
		actionAlignByFocusCenterHor.setEnabled(moreOne && thereAreFocus);
		actionAlignByFocusCenterVer.setEnabled(moreOne && thereAreFocus);

		actionToFront.setEnabled(thereAre);
		actionToUp.setEnabled(thereAre);
		actionToDown.setEnabled(thereAre);
		actionToBack.setEnabled(thereAre);

		// action areas
		noActionActionArea.setEnabled(thereAreFocus);
		actionScaleActionArea.setEnabled(thereAreFocus);
		actionShearActionArea.setEnabled(thereAreFocus);
		actionVectorActionArea.setEnabled(thereAreFocus);

		noActionActionArea.setSelected(false);
		actionScaleActionArea.setSelected(false);
		actionShearActionArea.setSelected(false);
		actionVectorActionArea.setSelected(false);

		if (actionScaleActionArea.isEnabled() && focusedShape.isVisibleAnyChild(JVGScaleActionArea.class)) {
			actionScaleActionArea.setSelected(true);
		} else if (actionShearActionArea.isEnabled() && focusedShape.isVisibleAnyChild(JVGShearActionArea.class)) {
			actionShearActionArea.setSelected(true);
		} else if (actionVectorActionArea.isEnabled() && focusedShape.isVisibleAnyChild(JVGVectorActionArea.class)) {
			actionVectorActionArea.setSelected(true);
		} else {
			noActionActionArea.setSelected(true);
		}

		actionRotateActionArea.setEnabled(thereAreFocus && focusedShape.containsChild(JVGRotateActionArea.class));
		actionRotateActionArea.setSelected(actionRotateActionArea.isEnabled() && focusedShape.isVisibleAnyChild(JVGRotateActionArea.class));

		actionCoordinateActionArea.setEnabled(thereAreFocus && focusedShape.containsChild(JVGCoordinateActionArea.class));
		actionCoordinateActionArea.setSelected(actionCoordinateActionArea.isEnabled() && focusedShape.isVisibleAnyChild(JVGCoordinateActionArea.class));

		actionConnectionActionArea.setEnabled(thereAreFocus && focusedShape.containsChild(JVGAbstractConnectionActionArea.class));
		actionConnectionActionArea.setSelected(actionConnectionActionArea.isEnabled() && focusedShape.isVisibleAnyChild(JVGAbstractConnectionActionArea.class));

		actionMoveActionArea.setEnabled(thereAreFocus);
		actionMoveActionArea.setSelected(thereAreFocus && MoveMouseListener.getListener(focusedShape) != null);

		// path
		boolean thereArePathes = pathesCount > 1;
		boolean thereArePath = pathesCount > 0;

		actionToPath.setEnabled(selectionSize - pathesCount > 0);
		actionStrokePath.setEnabled(thereArePath);
		actionFlatPath.setEnabled(thereArePath);
		actionRemoveTransformPath.setEnabled(thereArePath || textFieldsCount > 0);

		actionEditPath.setEnabled(thereArePath);
		actionEditPath.setSelected(isPathEdited);

		actionSmoothPath.setEnabled(thereArePath);
		actionUnion.setEnabled(thereArePathes);
		actionSubtraction.setEnabled(thereArePathes && isPathFocused);
		actionIntersection.setEnabled(thereArePathes);
		actionExclusiveOr.setEnabled(thereArePathes);
		actionClosePath.setEnabled(isPathFocused || pathesCount == 1);
		actionEraser.setEnabled(thereArePath);
		actionEraserLasso.setEnabled(thereArePath);
		btnArrowDirectionChoose.setEnabled(thereArePath);

		// group path
		boolean thereAreGroupPathes = groupPathesCount > 1;
		boolean thereAreGroupPath = groupPathesCount > 0;
		actionAddSubArrow.setEnabled(thereAreGroupPath);
		actionMoveSubArrow.setEnabled(thereAreGroupPath);
		btnSubArrowWidth.setEnabled(thereArePath);
		btnSubArrowEndCapWidth.setEnabled(thereArePath);
		btnSubArrowEndCapLength.setEnabled(thereArePath);

		// text
		actionTextWrap.setEnabled(isTextEdited);
		actionTextWrap.setSelected(isTextWrap);

		cmbFontFamily.setEnabled(isTextEdited || isTextPathFocused || isTextFieldFocused);
		cmbFontSize.setEnabled(isTextEdited || isTextPathFocused || isTextFieldFocused);
		actionBold.setEnabled(isTextEdited || isTextPathFocused || isTextFieldFocused);
		actionItalic.setEnabled(isTextEdited || isTextPathFocused || isTextFieldFocused);
		actionUnderline.setEnabled(isTextEdited);
		actionStrike.setEnabled(isTextEdited);
		actionSubscript.setEnabled(isTextEdited);
		actionSuperscript.setEnabled(isTextEdited);
		btnTextBackground.setEnabled(isTextEdited);
		btnTextForeground.setEnabled(isTextEdited);
		actionLeftAlignment.setEnabled(isTextEdited);
		actionCenterAlignment.setEnabled(isTextEdited);
		actionRightAlignment.setEnabled(isTextEdited);
		actionJustifyAlignment.setEnabled(isTextEdited);
		actionIncreaseLineSpacing.setEnabled(isTextEdited);
		actionDecreaseLineSpacing.setEnabled(isTextEdited);
		actionIncreaseParagraphLeftIndent.setEnabled(isTextEdited);
		actionDecreaseParagraphLeftIndent.setEnabled(isTextEdited);
		actionIncreaseParagraphSpacing.setEnabled(isTextEdited);
		actionDecreaseParagraphSpacing.setEnabled(isTextEdited);
		actionBullets.setEnabled(isTextEdited);
		actionBulletsNumber.setEnabled(isTextEdited);
		actionBulletsShiftLeft.setEnabled(isTextEdited);
		actionBulletsShiftRight.setEnabled(isTextEdited);

		// appearance
		cmbOutlineWidth.setEnabled(thereAre);
		cmbOutlinePattern.setEnabled(thereAre);
		cmbEndingsPattern.setEnabled(thereAre);
		btnOutlineColor.setEnabled(thereAre);
		cmbStrokeCap.setEnabled(thereAre);
		cmbStrokeJoin.setEnabled(thereAre);
		btnFillColor.setEnabled(thereAre);
		btnFillTransparency.setEnabled(thereAre);
		btnShadowType.setEnabled(thereAre);
		btnShadowColor.setEnabled(thereAre);
		btnEndingsColor.setEnabled(thereAre);

		// name
		txtID.setEnabled(thereAreFocus);
		txtID.setText(thereAreFocus ? focusedShape.getName() : "");

		// transparency
		sliderAlfaLayer.setEnabled(thereAre);
		if (thereAreFocus) {
			sliderAlfaLayerActionEnabled = false;
			sliderAlfaLayer.setValue(focusedShape.getAlfa());
			sliderAlfaLayerActionEnabled = true;
		}
	}

	public void updateUndoRedoButtons() {
		JVGPaneInternalFrame frame = editor.getCurrentFrame();
		if (frame != null) {
			updateUndoRedoText(frame.getPane());
			actionUndo.setEnabled(frame.getPane().getUndoManager().canUndo());
			actionRedo.setEnabled(frame.getPane().getUndoManager().canRedo());
		} else {
			actionUndo.setEnabled(false);
			actionRedo.setEnabled(false);
		}
	}

	private void updateUndoRedoText(JVGEditPane pane) {
		if (pane != null) {
			if (pane.getUndoManager().canUndo()) {
				actionUndo.setName(lm.getValue("button.undo", "Undo") + ": " + pane.getUndoManager().getUndoPresentationName());
			} else {
				actionUndo.setName(lm.getValue("button.undo", "Undo"));
			}

			if (pane.getUndoManager().canRedo()) {
				actionRedo.setName(lm.getValue("button.redo", "Redo") + ": " + pane.getUndoManager().getRedoPresentationName());
			} else {
				actionRedo.setName(lm.getValue("button.redo", "Redo"));
			}
		} else {
			actionUndo.setName(lm.getValue("button.undo", "Undo"));
			actionRedo.setName(lm.getValue("button.redo", "Redo"));
		}
	}

	public boolean isFreezeActions() {
		return freezeActions;
	}

	public void setFreezeActions(boolean freezeActions) {
		this.freezeActions = freezeActions;
	}

	public void enablePathActions() {
		btnOutlineColor.setEnabled(true);
		cmbOutlineWidth.setEnabled(true);
		cmbOutlinePattern.setEnabled(true);
		cmbStrokeCap.setEnabled(true);
		cmbStrokeJoin.setEnabled(true);

		btnFillColor.setEnabled(true);
		btnFillTransparency.setEnabled(true);
	}

	public Stroke getStroke() {
		float width = getOutlineWidth().getSelectedWidth();
		float[] dashArray = getOutlinePattern().getDashArray();
		Stroke stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dashArray, 0f);
		return stroke;
	}

	public Painter getOutlinePainter() {
		if (getOutlineDraw().getDraw() != null) {
			Draw<?> draw = (Draw<?>) getOutlineDraw().getDraw().clone();
			Stroke stroke = getStroke();
			Painter painter = new OutlinePainter(new StrokeResource<>(stroke), draw);
			return painter;
		} else {
			return null;
		}
	}

	public Painter getFillPainter() {
		return new FillPainter((Draw<?>) btnFillColor.getDraw().clone());
	}

	public void paintOutline(Graphics2D g, Shape shape) {
		Draw<?> draw = btnOutlineColor.getDraw();
		if (draw != null) {
			Stroke stroke = getStroke();
			g.setPaint(draw.getPaint(null, shape, null));
			g.setStroke(stroke);
			g.draw(shape);
		}
	}

	private void updateSelectionType(ActionEvent e) {
		if (editor.getCurrentFrame() != null) {
			int type = 0;
			if (e.getSource() == cmbSelectionType) {
				type = cmbSelectionType.getSelectedIndex();
			} else {
				if (menuSelectionArea.isSelected()) {
					type = 1;
				} else if (menuSelectionLasso.isSelected()) {
					type = 2;
				}
			}

			if (editor.getCurrentPane().getRoot().getSelectionType() != type) {
				editor.getCurrentPane().getRoot().setSelectionType(type);
				if (e.getSource() == cmbSelectionType) {
					switch (type) {
						case 0:
							menuSelectionNone.setSelected(true);
							break;

						case 1:
							menuSelectionArea.setSelected(true);
							break;

						case 2:
							menuSelectionLasso.setSelected(true);
							break;
					}
				} else {
					cmbSelectionType.setSelectedIndex(type);
				}
			}
		}
	}

	private boolean ignoreEvent;

	@Override
	public void actionPerformed(ActionEvent e) {
		if (ignoreEvent) {
			return;
		}

		String cmd = e.getActionCommand();
		if ("new".equals(cmd)) {
			editor.createNew();
		} else if ("save".equals(cmd)) {
			editor.save();
		} else if ("save-all".equals(cmd)) {
			editor.saveAll();
		} else if ("save-as".equals(cmd)) {
			editor.saveAs();
		} else if ("save-selection".equals(cmd)) {
			editor.saveSelection();
		} else if ("open".equals(cmd)) {
			editor.open();
		} else if ("export".equals(cmd)) {
			editor.export();
		} else if ("exit".equals(cmd)) {
			editor.exit();
		} else if ("close".equals(cmd)) {
			editor.close();
		} else if ("edit-xml".equals(cmd)) {
			editor.editXML();
		} else if ("edit-dom".equals(cmd)) {
			editor.editDOM();
		} else if ("undo".equals(cmd)) {
			editor.undo(e);
		} else if ("redo".equals(cmd)) {
			editor.redo(e);
		} else if ("set-mouse".equals(cmd)) {
			editor.setMouse();
		} else if ("update-selection-type".equals(cmd)) {
			updateSelectionType(e);
		}

		else if ("show-editor-toolbar".equals(cmd)) {
			editorToolbar.setVisible(!editorToolbar.isVisible());
		} else if ("show-insert-toolbar".equals(cmd)) {
			insertToolbar.setVisible(!insertToolbar.isVisible());
		} else if ("show-text-toolbar".equals(cmd)) {
			textToolbar.setVisible(!textToolbar.isVisible());
		} else if ("show-paragraph-toolbar".equals(cmd)) {
			paragraphToolbar.setVisible(!paragraphToolbar.isVisible());
		} else if ("show-align-toolbar".equals(cmd)) {
			alignToolbar.setVisible(!alignToolbar.isVisible());
		} else if ("show-same-toolbar".equals(cmd)) {
			sameToolbar.setVisible(!sameToolbar.isVisible());
		} else if ("show-transform-toolbar".equals(cmd)) {
			transformToolbar.setVisible(!transformToolbar.isVisible());
		} else if ("show-outline-toolbar".equals(cmd)) {
			outlineToolbar.setVisible(!outlineToolbar.isVisible());
		} else if ("show-path-operations-toolbar".equals(cmd)) {
			pathOperationsToolbar.setVisible(!pathOperationsToolbar.isVisible());
		} else if ("show-id-toolbar".equals(cmd)) {
			idToolbar.setVisible(!idToolbar.isVisible());
		} else if ("show-display-toolbar".equals(cmd)) {
			displayToolbar.setVisible(!displayToolbar.isVisible());
		} else if ("show-actionarea-toolbar".equals(cmd)) {
			actionAreaToolbar.setVisible(!actionAreaToolbar.isVisible());
		} else if ("show-arrows-toolbar".equals(cmd)) {
			arrowsToolbar.setVisible(!arrowsToolbar.isVisible());
		} else if ("show-all-toolbars".equals(cmd)) {
			showAllToolbars();
		} else if ("hide-all-toolbars".equals(cmd)) {
			hideAllToolbars();
		}

		else if ("show-shapes-frame".equals(cmd)) {
			editor.showShapes(!editor.isShowShapes());
		} else if ("show-grid".equals(cmd)) {
			editor.setDrawGrid(!editor.isDrawGrid());
		} else if ("draw-grid-above".equals(cmd)) {
			editor.setDrawGridAbove(!editor.isDrawGridAbove());
		} else if ("show-rules".equals(cmd)) {
			editor.setShowRules(!editor.isShowRules());
		} else if ("align-to-grid".equals(cmd)) {
			editor.setGridAlign(!editor.isGridAlign());
		} else if ("about".equals(cmd)) {
			editor.about();
		} else if ("preview".equals(cmd)) {
			editor.preview();
		} else if ("resize-document".equals(cmd)) {
			editor.resizeDocument();
		} else if ("document-color".equals(cmd)) {
			editor.chooseDocumentColor();
		} else if ("enable-connections".equals(cmd)) {
			editor.setConnectionsEnabled(!editor.isConnectionsEnabled());
		} else if ("define-connections".equals(cmd)) {
			editor.defineConnections(editor.getCurrentPane());
		} else if ("show-switcher".equals(cmd)) {
			editor.showSwitcher(!editor.isShowSwitcher());
		} else if ("show-clipboard".equals(cmd)) {
			editor.showClipboard(!editor.isShowClipboard());
		} else if ("show-resources".equals(cmd)) {
			editor.showResources(!editor.isShowResources());
		} else if ("show-zoom-preview".equals(cmd)) {
			editor.showZoomPreview(!editor.isShowZoomPreview());
		} else if ("full-screen".equals(cmd)) {
			editor.setFullScreen(!editor.isFullScreen());
		} else if ("find".equals(cmd)) {
			editor.find();
		} else if ("save-editor-config".equals(cmd)) {
			saveEditorConfig();
		} else if ("add-sub-arrow".equals(cmd)) {
			JVGComponent c = editor.getCurrentPane().getFocusOwner();
			JVGGroupPath p = null;
			if (c instanceof JVGGroupPath) {
				p = (JVGGroupPath) c;
			} else if (c instanceof JVGSubPath) {
				JVGSubPath s = (JVGSubPath) c;
				p = (JVGGroupPath) s.getParent();
			}

			if (p != null) {
				p.setChildrenSelected(false);
				p.setEditMode(JVGGroupPath.EDIT_MODE__ADD_SUBPATH);
				p.setSelected(false);
				p.repaint();
			}
		} else if ("move-sub-arrow".equals(cmd)) {
			JVGComponent c = editor.getCurrentPane().getFocusOwner();
			JVGGroupPath p = null;
			if (c instanceof JVGGroupPath) {
				p = (JVGGroupPath) c;
			} else if (c instanceof JVGSubPath) {
				JVGSubPath s = (JVGSubPath) c;
				p = (JVGGroupPath) s.getParent();
			}

			if (p != null) {
				p.setEditMode(JVGGroupPath.EDIT_MODE__MOVE_SUBPATH);
				JVGComponent[] childs = p.getChilds(JVGSubPath.class);
				p.getPane().getSelectionManager().setSelection(childs);
				childs[childs.length - 1].setFocused(true);
				p.repaint();
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JComponent c = (JComponent) e.getSource();
		if (c instanceof AbstractButton) {
			AbstractButton btn = (AbstractButton) c;
			actionPerformed(new ActionEvent(e.getSource(), 0, btn.getActionCommand()));
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JComponent c = (JComponent) e.getSource();
		if (c instanceof AbstractButton) {
			AbstractButton btn = (AbstractButton) c;
			actionPerformed(new ActionEvent(e.getSource(), 0, btn.getActionCommand()));
		}
	}

	public void saveEditorConfig() {
		try {
			Properties p = new Properties();

			// toolbars
			for (WToolBar t : toolbarsList) {
				p.setProperty(t.getName(), t.isVisible() ? "yes" : "no");
			}

			// view menu
			for (WCheckBoxMenuItem m : viewCheckMenus) {
				p.setProperty(m.getName(), m.isSelected() ? "yes" : "no");
			}

			File file = new File(editor.getConfigDir(), configFileName);
			Writer w = new FileWriter(file);
			p.store(w, "JVG Editor properties file");
			w.close();
		} catch (FileNotFoundException exc) {
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void loadEditorConfig() {
		try {
			File file = new File(editor.getConfigDir(), configFileName);

			Properties p = new Properties();
			Reader r = new FileReader(file);
			p.load(r);
			r.close();

			// toolbars
			for (WToolBar t : toolbarsList) {
				if (p.containsKey(t.getName())) {
					t.setVisible("yes".equals(p.get(t.getName())));
				}
			}

			// view menu
			for (WCheckBoxMenuItem m : viewCheckMenus) {
				if (p.containsKey(m.getName())) {
					m.setSelected("yes".equals(p.get(m.getName())));
				}
			}
		} catch (FileNotFoundException exc) {
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public Font getFont() {
		String family = ((Font) cmbFontFamily.getSelectedItem()).getFamily();
		int size = ((FontSizeAction) cmbFontSize.getSelectedItem()).getSize();

		boolean bold = actionBold.getButton().isSelected();
		boolean italic = actionItalic.getButton().isSelected();
		int style = Font.PLAIN;
		if (bold) {
			style |= Font.BOLD;
		}
		if (italic) {
			style |= Font.ITALIC;
		}
		return Fonts.getFont(family, style, size);
	}
}
