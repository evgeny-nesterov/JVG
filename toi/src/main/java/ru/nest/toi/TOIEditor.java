package ru.nest.toi;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.nest.fonts.FontComboBox;
import ru.nest.imagechooser.IFileFilter;
import ru.nest.layout.VerticalFlowLayout;
import ru.nest.swing.IconButton;
import ru.nest.swing.IconToggleButton;
import ru.nest.swing.ToolSeparator;
import ru.nest.toi.editcontroller.TOIAddImageController;
import ru.nest.toi.editcontroller.TOIAddPathController;
import ru.nest.toi.editcontroller.TOIAddPathElementController;
import ru.nest.toi.editcontroller.TOIAddTextController;
import ru.nest.toi.editcontroller.TOIGeometryController;
import ru.nest.toi.factory.TOIEditorFactory;
import ru.nest.toi.fonts.Fonts;
import ru.nest.toi.objectcontrol.TOIRotateObjectControl;
import ru.nest.toi.objectcontrol.TOIScaleObjectControl;
import ru.nest.toi.objects.TOIArrowPathElement;
import ru.nest.toi.objects.TOIGroup;
import ru.nest.toi.objects.TOIMultiArrowPath;
import ru.nest.toi.objects.TOIText;
import ru.nest.toi.objects.TOITextPath;

public class TOIEditor extends JPanel {
	private static final long serialVersionUID = 4384229142714810316L;

	protected TOIEditPane pane;

	private JLayeredPane panePanel;

	private String preferredFormat = "png";

	public TOIEditor() {
		pane = createPane();
		pane.setEditable(true);
		pane.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				JViewport v = scrollPane.getViewport();
				Point p = v.getViewPosition();
				if (!e.isControlDown()) {
					p.y += e.getUnitsToScroll() * 10;
				} else {
					p.x += e.getUnitsToScroll() * 10;
				}
				v.setViewPosition(p);

				scrollPane.invalidate();
				v.invalidate();
				revalidate();
				repaint();
			}
		});

		scrollPane = createScrollPane();
		scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);

		panePanel = new JLayeredPane();
		panePanel.setLayout(new LayoutManager() {
			@Override
			public void addLayoutComponent(String name, Component comp) {
			}

			@Override
			public void removeLayoutComponent(Component comp) {
			}

			@Override
			public Dimension preferredLayoutSize(Container parent) {
				int w = 0, h = 0;
				for (int i = 0; i < parent.getComponentCount(); i++) {
					Dimension s = parent.getComponent(i).getPreferredSize();
					w = Math.max(w, s.width);
					h = Math.max(h, s.height);
				}
				return new Dimension(w, h);
			}

			@Override
			public Dimension minimumLayoutSize(Container parent) {
				int w = 0, h = 0;
				for (int i = 0; i < parent.getComponentCount(); i++) {
					Dimension s = parent.getComponent(i).getMinimumSize();
					w = Math.max(w, s.width);
					h = Math.max(h, s.height);
				}
				return new Dimension(w, h);
			}

			@Override
			public void layoutContainer(Container parent) {
				for (int i = parent.getComponentCount() - 1; i >= 0; i--) {
					parent.getComponent(i).setBounds(0, 0, parent.getWidth(), parent.getHeight());
				}
			}
		});
		panePanel.add(scrollPane);
		panePanel.setLayer(scrollPane, 0, 0);

		setFocusCycleRoot(true);
		setLayout(new BorderLayout());
		add(panePanel, BorderLayout.CENTER);

		controlButtonsGroup = new ButtonGroup();
		initToolbar();
		initZoomToolbar();

		addHierarchyBoundsListener(new HierarchyBoundsListener() {
			@Override
			public void ancestorResized(HierarchyEvent e) {
				if (getWidth() != 0) {
					centrate();
					removeHierarchyBoundsListener(this);
				}
			}

			@Override
			public void ancestorMoved(HierarchyEvent e) {
			}
		});
	}

	public void centrate() {
		JViewport v = scrollPane.getViewport();
		scrollPane.invalidate();
		v.invalidate();
		invalidate();
		validate();

		Dimension vs = v.getViewSize();
		Dimension s = v.getSize();
		v.setViewPosition(new Point((vs.width - s.width) / 2, (vs.height - s.height) / 2));

		scrollPane.invalidate();
		v.invalidate();
		invalidate();
		validate();
		repaint();
	}

	protected JScrollPane createScrollPane() {
		return new JScrollPane(pane);
	}

	private JScrollPane scrollPane;

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public void addUnderPane(JComponent c) {
		panePanel.add(c);
		panePanel.setLayer(scrollPane, 1);

		panePanel.remove(c);

		panePanel.add(c);
		panePanel.setLayer(scrollPane, 1);

		panePanel.invalidate();
		panePanel.validate();
		panePanel.repaint();
	}

	public void removeUnderPane(JComponent c) {
		panePanel.remove(c);
		panePanel.invalidate();
		panePanel.validate();
		panePanel.repaint();
	}

	protected TOIEditPane createPane() {
		return new TOIEditPane();
	}

	private static String copy;

	public void copy() {
		List<TOIObject> objects = pane.getSelectedObjects();
		if (objects.size() > 0) {
			try {
				copy = new TOIBuilder(pane.getFactory()).export(objects);
				System.out.println(copy);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void paste() {
		if (copy != null) {
			try {
				List<TOIObject> objects = new TOIBuilder(pane.getFactory()).load(copy);
				pane.clearSelection();
				for (TOIObject o : objects) {
					o.translate(20, 20);
					pane.addObject(o);
					pane.select(o, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void deleteSelected() {
		List<TOIObject> objects = pane.getSelectedObjects();
		pane.clearSelection();
		for (TOIObject o : objects) {
			pane.removeObject(o);
		}
	}

	public static class ToolButton extends IconButton {
		private static final long serialVersionUID = 1L;

		public ToolButton(Icon icon) {
			super(icon);
			setRequestFocusEnabled(false);
			setFocusable(false);
		}
	}

	public static class ToolToggleButton extends IconToggleButton {
		private static final long serialVersionUID = 1L;

		public ToolToggleButton(Icon icon) {
			super(icon);
			setRequestFocusEnabled(false);
			setFocusable(false);
		}
	}

	private String currentDir;

	public void saveImage() {
		try {
			int initialFormat = IFileFilter.ImageFileFilter.PNG;
			if ("jpg".equals(preferredFormat)) {
				initialFormat = IFileFilter.ImageFileFilter.PNG;
			} else if ("gif".equals(preferredFormat)) {
				initialFormat = IFileFilter.ImageFileFilter.GIF;
			} else if ("bmp".equals(preferredFormat)) {
				initialFormat = IFileFilter.ImageFileFilter.BMP;
			} else if ("tif".equals(preferredFormat)) {
				initialFormat = IFileFilter.ImageFileFilter.TIF;
			}

			JFileChooser chooser = new JFileChooser(currentDir);
			IFileFilter.ImageFileFilter ff_initial = new IFileFilter.ImageFileFilter(initialFormat);
			chooser.addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.PNG));
			chooser.addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.GIF));
			chooser.addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.TIF));
			chooser.addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.JPG));
			chooser.addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.BMP));
			chooser.setFileFilter(ff_initial);

			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setDialogTitle("Экпорт изображения");
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);

			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				String ext = TOIUtil.getExtension(file);
				if (ext == null) {
					IFileFilter.ImageFileFilter ff = (IFileFilter.ImageFileFilter) chooser.getFileFilter();
					ext = ff.getExtension();
					if (ext == null) {
						ext = preferredFormat;
					}
					file = new File(file.getPath() + "." + ext);
				}

				if (ext != null) {
					Dimension docSize = pane.getDocumentSize();
					TOIPainter painter = new TOIPainter(pane.getObjects());
					painter.setColorRenderer(pane.getColorRenderer());
					painter.exportImage(ext, docSize.width, docSize.height, new FileOutputStream(file));
					currentDir = file.getParentFile().getPath();
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public TOIEditPane getPane() {
		return pane;
	}

	protected JToolBar toolbar1 = new JToolBar();

	public JToolBar getToolbar1() {
		return toolbar1;
	}

	protected ButtonGroup controlButtonsGroup;

	public ButtonGroup getControlButtonsGroup() {
		return controlButtonsGroup;
	}

	// common controls
	protected ToolToggleButton btnLock = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/lock.png")));

	protected ToolToggleButton btnChangeGeometry = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/changegeometry.png")));

	protected ToolToggleButton btnAddArrow = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addarrow.png")));

	protected ToolToggleButton btnAddIcon = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/image.png")));

	protected ToolToggleButton btnAddText = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/text.png")));

	protected ToolToggleButton btnAddPathLine = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addarrowline.png")));

	protected ToolToggleButton btnAddPathQuad = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addarrowquad.png")));

	protected ToolToggleButton btnAddPathCube = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addarrowcubic.png")));

	protected ToolToggleButton btnAddMultiArrowPathLine = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addmultiarrowline.png")));

	protected ToolToggleButton btnAddMultiArrowPathQuad = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addmultiarrowquad.png")));

	protected ToolToggleButton btnAddMultiArrowPathCube = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addmultiarrowcubic.png")));

	protected ToolToggleButton btnAddTextPathLine = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addtextline.png")));

	protected ToolToggleButton btnAddTextPathQuad = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addtextquad.png")));

	protected ToolToggleButton btnAddTextPathCube = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/addtextcubic.png")));

	protected ToolButton btnSaveImage = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/exportimage.png")));

	protected ToolButton btnCopy = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/copy.png")));

	protected ToolButton btnPaste = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/paste.png")));

	protected ToolButton btnSelectAll = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/selectall.png")));

	protected ToolButton btnDeleteAll = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/deleteall.gif")));

	protected ToolButton btnToFront = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/order_to_front.gif")));

	protected ToolButton btnUp = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/order_to_up.gif")));

	protected ToolButton btnDown = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/order_to_down.gif")));

	protected ToolButton btnToBack = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/order_to_back.gif")));

	protected ToolButton btnRotate90 = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/rotate_90.gif")));

	protected ToolButton btnRotateMinus90 = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/rotate_minus_90.gif")));

	protected ToolButton btnRotateMinus180 = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/rotate_180.gif")));

	protected ToolButton btnFlipHor = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/flip_hor.gif")));

	protected ToolButton btnFlipVer = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/flip_ver.gif")));

	protected ToolToggleButton btnRotateControl = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/action_rotate.png")));

	protected ToolToggleButton btnScaleControl = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/action_scale.png")));

	private ColorChooserButton btnColor = new ColorChooserButton();

	protected ToolToggleButton btnGroup = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/group.gif")));

	private ArrowDirectionChooseButton btnArrowDirection = new ArrowDirectionChooseButton();

	// text controls
	private JTextField txtText = new JTextField();

	private FontComboBox cmbFont = new FontComboBox(Fonts.getPixelFont());

	private JComboBox<Integer> cmbFontSize = new JComboBox<Integer>(new Integer[] { 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 18, 20, 24, 28, 32, 36, 40, 45, 50, 60, 70, 80, 90, 100 });

	private ToolToggleButton btnBold = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/text_bold.gif")));

	private ToolToggleButton btnItalic = new ToolToggleButton(new ImageIcon(TOIEditor.class.getResource("img/text_italic.gif")));

	// zoom controls
	private JSlider zoomSlider = new JSlider(JSlider.VERTICAL);

	//-----------------------------------------------------------------

	private double arrowScale = 1;

	private double arrowEndScale = 1;

	private double arrowEndLengthScale = 1;

	private ToolButton btnScaleArrow = new ToolButton(new Icon() {
		Font font = new Font("Dialog", Font.PLAIN, 10);

		NumberFormat format = new DecimalFormat("##.#");

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.translate(x, y);

			g.setColor(Color.darkGray);
			g.drawLine(3, 7, 3, 12);
			g.drawLine(7, 7, 7, 12);
			g.drawLine(1, 6, 5, 2);
			g.drawLine(9, 6, 5, 2);

			g.setColor(Color.black);
			g.setFont(font);
			g.drawString(format.format(14 * arrowScale), 10, 12);
		}

		@Override
		public int getIconWidth() {
			return 32;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	});

	private ToolButton btnScaleArrowEnd = new ToolButton(new Icon() {
		Font font = new Font("Dialog", Font.PLAIN, 10);

		NumberFormat format = new DecimalFormat("##.#");

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.translate(x, y);

			g.setColor(Color.darkGray);
			g.drawLine(3, 7, 3, 12);
			g.drawLine(7, 7, 7, 12);
			g.drawLine(1, 6, 5, 2);
			g.drawLine(9, 6, 5, 2);

			g.setColor(Color.black);
			g.setFont(font);
			g.drawString(format.format(12 * arrowScale * arrowEndScale), 10, 12);
		}

		@Override
		public int getIconWidth() {
			return 32;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	});

	private ToolButton btnScaleArrowEndLength = new ToolButton(new Icon() {
		Font font = new Font("Dialog", Font.PLAIN, 10);

		NumberFormat format = new DecimalFormat("##.#");

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.translate(x, y);

			g.setColor(Color.darkGray);
			g.drawLine(3, 7, 3, 12);
			g.drawLine(7, 7, 7, 12);
			g.drawLine(1, 6, 5, 2);
			g.drawLine(9, 6, 5, 2);

			g.setColor(Color.black);
			g.setFont(font);
			g.drawString(format.format(6 * arrowScale * arrowEndScale * arrowEndLengthScale), 10, 12);
		}

		@Override
		public int getIconWidth() {
			return 32;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	});

	protected void initToolbar() {
		btnSaveImage.setToolTipText("Сохранить изображение");
		btnSaveImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		});
		toolbar1.add(btnSaveImage);

		toolbar1.add(new ToolSeparator(14));

		btnCopy.setToolTipText("Скопировать в буфер [ctrl-c]");
		btnCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copy();
			}
		});
		toolbar1.add(btnCopy);

		btnPaste.setToolTipText("Вставить из буфера [ctrl-v]");
		btnPaste.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		});
		toolbar1.add(btnPaste);

		toolbar1.add(new ToolSeparator(14));

		btnSelectAll.setToolTipText("Выделить все [ctrl-a]");
		btnSelectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.selectAll();
			}
		});
		toolbar1.add(btnSelectAll);

		ToolButton btnDelete = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/delete.gif")));
		btnDelete.setToolTipText("Удалить выделенные объекты [del]");
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelected();
			}
		});
		toolbar1.add(btnDelete);

		btnDeleteAll.setToolTipText("Удалить все объекты [ctrl-a, del]");
		btnDeleteAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int option = JOptionPane.showConfirmDialog(TOIEditor.this, "Удалить все объекты?");
				if (option == JOptionPane.OK_OPTION) {
					pane.removeAllObjects();
				}
			}
		});
		toolbar1.add(btnDeleteAll);

		btnLock.setToolTipText("Заблокировать [exc]");
		btnLock.setSelected(true);
		btnLock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(null);
			}
		});
		toolbar1.add(btnLock);
		controlButtonsGroup.add(btnLock);

		btnChangeGeometry.setToolTipText("Редактировать геометрию объектов [F1]");
		btnChangeGeometry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIGeometryController(), btnChangeGeometry));
			}
		});
		toolbar1.add(btnChangeGeometry);
		controlButtonsGroup.add(btnChangeGeometry);

		btnAddArrow.setToolTipText("Добавить / Удалить стрелки [F8]");
		btnAddArrow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathElementController(), btnAddArrow));
			}
		});
		toolbar1.add(btnAddArrow);
		controlButtonsGroup.add(btnAddArrow);

		btnAddIcon.setToolTipText("Добавить картинку");
		btnAddIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddImageController(), btnAddIcon));
			}
		});
		toolbar1.add(btnAddIcon);
		controlButtonsGroup.add(btnAddIcon);

		btnAddText.setToolTipText("Добавить текст");
		btnAddText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddTextController(), btnAddText));
			}
		});
		toolbar1.add(btnAddText);
		controlButtonsGroup.add(btnAddText);

		toolbar1.add(new ToolSeparator(14));

		btnAddPathLine.setToolTipText("Добавить стрелку в виде прямой линии [F2]");
		btnAddPathLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathController(TOIArrowPathElement.class, TOIAddPathController.LINE), btnAddPathLine));
			}
		});
		toolbar1.add(btnAddPathLine);
		controlButtonsGroup.add(btnAddPathLine);

		btnAddPathQuad.setToolTipText("Добавить стрелку в виде квадратичной кривой [F3]");
		btnAddPathQuad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathController(TOIArrowPathElement.class, TOIAddPathController.QUAD), btnAddPathQuad));
			}
		});
		toolbar1.add(btnAddPathQuad);
		controlButtonsGroup.add(btnAddPathQuad);

		btnAddPathCube.setToolTipText("Добавить стрелку в виде кубической кривой [F4]");
		btnAddPathCube.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathController(TOIArrowPathElement.class, TOIAddPathController.CUBIC), btnAddPathCube));
			}
		});
		toolbar1.add(btnAddPathCube);
		controlButtonsGroup.add(btnAddPathCube);

		btnArrowDirection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (TOIObject o : pane.getSelectedObjects()) {
					if (o instanceof TOIArrowPathElement) {
						TOIArrowPathElement a = (TOIArrowPathElement) o;
						a.setDirection(btnArrowDirection.getDirection());
					}
				}
				pane.repaint();
			}
		});
		toolbar1.add(btnArrowDirection);

		toolbar1.add(new ToolSeparator(14));

		btnAddMultiArrowPathLine.setToolTipText("Добавить стрелку в виде прямой линии [Ctrl-F2]");
		btnAddMultiArrowPathLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathController(TOIMultiArrowPath.class, TOIAddPathController.LINE), btnAddMultiArrowPathLine));
			}
		});
		toolbar1.add(btnAddMultiArrowPathLine);
		controlButtonsGroup.add(btnAddMultiArrowPathLine);

		btnAddMultiArrowPathQuad.setToolTipText("Добавить стрелку в виде квадратичной кривой [Ctrl-F3]");
		btnAddMultiArrowPathQuad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathController(TOIMultiArrowPath.class, TOIAddPathController.QUAD), btnAddMultiArrowPathQuad));
			}
		});
		toolbar1.add(btnAddMultiArrowPathQuad);
		controlButtonsGroup.add(btnAddMultiArrowPathQuad);

		btnAddMultiArrowPathCube.setToolTipText("Добавить стрелку в виде кубической кривой [Ctrl-F4]");
		btnAddMultiArrowPathCube.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathController(TOIMultiArrowPath.class, TOIAddPathController.CUBIC), btnAddMultiArrowPathCube));
			}
		});
		toolbar1.add(btnAddMultiArrowPathCube);
		controlButtonsGroup.add(btnAddMultiArrowPathCube);

		toolbar1.add(new ToolSeparator(14));

		btnAddTextPathLine.setToolTipText("Добавить текст по прямой линии [F5]");
		btnAddTextPathLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathController(TOITextPath.class, TOIAddPathController.LINE), btnAddTextPathLine));
			}
		});
		toolbar1.add(btnAddTextPathLine);
		controlButtonsGroup.add(btnAddTextPathLine);

		btnAddTextPathQuad.setToolTipText("Добавить текст по квадратичной кривой [F6]");
		btnAddTextPathQuad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathController(TOITextPath.class, TOIAddPathController.QUAD), btnAddTextPathQuad));
			}
		});
		toolbar1.add(btnAddTextPathQuad);
		controlButtonsGroup.add(btnAddTextPathQuad);

		btnAddTextPathCube.setToolTipText("Добавить текст по кубической кривой [F7]");
		btnAddTextPathCube.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setController(new TOIEditController(new TOIAddPathController(TOITextPath.class, TOIAddPathController.CUBIC), btnAddTextPathCube));
			}
		});
		toolbar1.add(btnAddTextPathCube);
		controlButtonsGroup.add(btnAddTextPathCube);

		toolbar1.add(new ToolSeparator(14));

		btnToFront.setToolTipText("Переместить выделенный на верхний уровень [ctrl-pageup]");
		btnToFront.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.toFront(pane.getFocusedObject());
			}
		});
		toolbar1.add(btnToFront);

		btnUp.setToolTipText("Переместить выделенный объект на уровень выше [ctrl-up]");
		btnUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.toUp(pane.getFocusedObject());
			}
		});
		toolbar1.add(btnUp);

		btnDown.setToolTipText("Переместить выделенный объект на уровень ниже [ctrl-down]");
		btnDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.toDown(pane.getFocusedObject());
			}
		});
		toolbar1.add(btnDown);

		btnToBack.setToolTipText("Переместить выделенный на нижний уровень [ctrl-pagedown]");
		btnToBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.toBack(pane.getFocusedObject());
			}
		});
		toolbar1.add(btnToBack);

		toolbar1.add(new ToolSeparator(14));

		btnRotate90.setToolTipText("Повернуть на 90 градусов по часовой стрелке");
		btnRotate90.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.rotateSelection(Math.PI / 2);
			}
		});
		toolbar1.add(btnRotate90);

		btnRotateMinus90.setToolTipText("Повернуть на 90 градусов против часовой стрелке");
		btnRotateMinus90.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.rotateSelection(-Math.PI / 2);
			}
		});
		toolbar1.add(btnRotateMinus90);

		btnRotateMinus180.setToolTipText("Повернуть на 180 градусов");
		btnRotateMinus180.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.rotateSelection(Math.PI);
			}
		});
		toolbar1.add(btnRotateMinus180);

		btnFlipHor.setToolTipText("Перевернуть по горизонтали");
		btnFlipHor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.scaleSelection(-1, 1);
			}
		});
		toolbar1.add(btnFlipHor);

		btnFlipVer.setToolTipText("Перевернуть по вертикали");
		btnFlipVer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.scaleSelection(1, -1);
			}
		});
		toolbar1.add(btnFlipVer);

		toolbar1.add(new ToolSeparator(14));

		btnRotateControl.setToolTipText("Разрешить поворот объектов");
		btnRotateControl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.repaint();
			}
		});
		btnRotateControl.setSelected(true);
		toolbar1.add(btnRotateControl);

		btnScaleControl.setToolTipText("Разрешить масштабирование объектов");
		btnScaleControl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.repaint();
			}
		});
		btnScaleControl.setSelected(false);
		toolbar1.add(btnScaleControl);

		toolbar1.add(new ToolSeparator(14));

		btnScaleArrow.setToolTipText("Ширина стрелки");
		btnScaleArrow.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				final JSlider slider = new JSlider();
				slider.setMinimum(10);
				slider.setMaximum(500);
				slider.setValue((int) (10 * 14 * arrowScale));
				slider.setPreferredSize(new Dimension(500, 18));
				slider.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						arrowScale = slider.getValue() / 10.0 / 14.0;
						btnScaleArrow.repaint();
						btnScaleArrowEnd.repaint();
						btnScaleArrowEndLength.repaint();

						List<TOIObject> objects = pane.getSelectedObjects();
						for (TOIObject o : objects) {
							if (o instanceof Arrow) {
								Arrow a = (Arrow) o;
								a.setScale(arrowScale, arrowScale * arrowEndScale, arrowScale * arrowEndScale * arrowEndLengthScale);
							}
						}
						pane.repaint();
					}
				});

				JPopupMenu popup = new JPopupMenu();
				popup.setLayout(new BorderLayout());
				popup.add(slider, BorderLayout.CENTER);
				popup.pack();
				popup.show(btnScaleArrow, 0, btnScaleArrow.getHeight());
			}
		});
		toolbar1.add(btnScaleArrow);

		btnScaleArrowEnd.setToolTipText("Размер наконечника стрелки");
		btnScaleArrowEnd.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				final JSlider slider = new JSlider();
				slider.setMinimum(10);
				slider.setMaximum(1000);
				slider.setValue((int) (10 * 12 * arrowEndScale));
				slider.setPreferredSize(new Dimension(500, 18));
				slider.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						arrowEndScale = slider.getValue() / 10.0 / 12.0;
						btnScaleArrow.repaint();
						btnScaleArrowEnd.repaint();
						btnScaleArrowEndLength.repaint();

						List<TOIObject> objects = pane.getSelectedObjects();
						for (TOIObject o : objects) {
							if (o instanceof Arrow) {
								Arrow a = (Arrow) o;
								a.setScale(arrowScale, arrowScale * arrowEndScale, arrowScale * arrowEndScale * arrowEndLengthScale);
							}
						}
						pane.repaint();
					}
				});

				JPopupMenu popup = new JPopupMenu();
				popup.setLayout(new BorderLayout());
				popup.add(slider, BorderLayout.CENTER);
				popup.pack();
				popup.show(btnScaleArrowEnd, 0, btnScaleArrowEnd.getHeight());
			}
		});
		toolbar1.add(btnScaleArrowEnd);

		btnScaleArrowEndLength.setToolTipText("Длина наконечника стрелки");
		btnScaleArrowEndLength.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				final JSlider slider = new JSlider();
				slider.setMinimum(10);
				slider.setMaximum(1000);
				slider.setValue((int) (10 * 6 * arrowEndLengthScale));
				slider.setPreferredSize(new Dimension(500, 18));
				slider.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						arrowEndLengthScale = slider.getValue() / 10.0 / 6.0;
						btnScaleArrow.repaint();
						btnScaleArrowEnd.repaint();
						btnScaleArrowEndLength.repaint();

						List<TOIObject> objects = pane.getSelectedObjects();
						for (TOIObject o : objects) {
							if (o instanceof Arrow) {
								Arrow a = (Arrow) o;
								a.setScale(arrowScale, arrowScale * arrowEndScale, arrowScale * arrowEndScale * arrowEndLengthScale);
							}
						}
						pane.repaint();
					}
				});

				JPopupMenu popup = new JPopupMenu();
				popup.setLayout(new BorderLayout());
				popup.add(slider, BorderLayout.CENTER);
				popup.pack();
				popup.show(btnScaleArrowEndLength, 0, btnScaleArrowEndLength.getHeight());
			}
		});
		toolbar1.add(btnScaleArrowEndLength);

		// second toolbar
		JToolBar toolbar2 = new JToolBar();

		txtText.setEnabled(false);
		txtText.setPreferredSize(new Dimension(150, 18));
		txtText.setMinimumSize(new Dimension(40, 18));
		txtText.setMaximumSize(new Dimension(150, 18));
		txtText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pane.getFocusedObject() instanceof TOIText) {
					((TOIText) pane.getFocusedObject()).setText(txtText.getText());
					pane.repaint();
				} else if (pane.getFocusedObject() instanceof TOITextPath) {
					((TOITextPath) pane.getFocusedObject()).setText(txtText.getText());
					pane.repaint();
				}
			}
		});
		toolbar2.add(txtText);
		toolbar2.add(new JLabel("  "));

		cmbFont.setEnabled(false);
		cmbFont.setToolTipText("Фонт");
		cmbFont.setRequestFocusEnabled(false);
		cmbFont.setFocusable(false);
		cmbFont.setPreferredSize(new Dimension(100, 18));
		cmbFont.setMinimumSize(new Dimension(100, 18));
		cmbFont.setMaximumSize(new Dimension(100, 18));
		cmbFont.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pane.getFocusedObject() instanceof TOIText || pane.getFocusedObject() instanceof TOITextPath) {
					TOIObject t = pane.getFocusedObject();
					if (cmbFont.getSelectedItem() != null) {
						t.setFontDeep(((Font) cmbFont.getSelectedItem()).deriveFont(t.getFont().getStyle(), t.getFont().getSize()));
						pane.repaint();
					}
				}
			}
		});
		toolbar2.add(cmbFont);
		toolbar2.add(new JLabel("  "));

		cmbFontSize.setEnabled(false);
		cmbFontSize.setToolTipText("Размер фонта");
		cmbFontSize.setRequestFocusEnabled(false);
		cmbFontSize.setFocusable(false);
		cmbFontSize.setPreferredSize(new Dimension(45, 18));
		cmbFontSize.setMinimumSize(new Dimension(45, 18));
		cmbFontSize.setMaximumSize(new Dimension(45, 18));
		cmbFontSize.setEditable(true);
		cmbFontSize.setSelectedItem(12);
		cmbFontSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pane.getFocusedObject() instanceof TOIText || pane.getFocusedObject() instanceof TOITextPath) {
					TOIObject t = pane.getFocusedObject();
					t.setFontDeep(t.getFont().deriveFont((float) (Integer) cmbFontSize.getSelectedItem()));
					pane.repaint();
				}
			}
		});
		toolbar2.add(cmbFontSize);
		toolbar2.add(new JLabel("  "));

		btnBold.setEnabled(false);
		btnBold.setToolTipText("Жирный текст");
		btnBold.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pane.getFocusedObject() instanceof TOIText || pane.getFocusedObject() instanceof TOITextPath) {
					TOIObject t = pane.getFocusedObject();
					t.setFontDeep(t.getFont().deriveFont((btnBold.isSelected() ? Font.BOLD : Font.PLAIN) | (t.getFont().isItalic() ? Font.ITALIC : Font.PLAIN)));
					pane.repaint();
				}
			}
		});
		toolbar2.add(btnBold);

		btnItalic.setEnabled(false);
		btnItalic.setToolTipText("Курсивный текст");
		btnItalic.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pane.getFocusedObject() instanceof TOIText || pane.getFocusedObject() instanceof TOITextPath) {
					TOIObject t = pane.getFocusedObject();
					t.setFontDeep(t.getFont().deriveFont((btnItalic.isSelected() ? Font.ITALIC : Font.PLAIN) | (t.getFont().isBold() ? Font.BOLD : Font.PLAIN)));
					pane.repaint();
				}
			}
		});
		toolbar2.add(btnItalic);

		toolbar2.add(new ToolSeparator(14));

		btnColor.setRequestFocusEnabled(false);
		btnColor.setFocusable(false);
		btnColor.setToolTipText("Цвет объекта");
		btnColor.setEnabled(false);
		btnColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (TOIObject o : pane.getSelectedObjects()) {
					o.setColorDeep(btnColor.getColor());
				}
				pane.repaint();
			}
		});
		toolbar2.add(btnColor);

		btnGroup.setToolTipText("Группировка объектов");
		btnGroup.setEnabled(false);
		btnGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<TOIObject> selection = pane.getSelectedObjects();
				if (selection.size() > 0) {
					pane.clearSelection();

					final List<TOIObject> objects = pane.getObjects();
					Collections.sort(selection, new Comparator<TOIObject>() {
						@Override
						public int compare(TOIObject o1, TOIObject o2) {
							int i1 = objects.indexOf(o1);
							int i2 = objects.indexOf(o2);
							return i1 - i2;
						}
					});

					if (selection.size() > 1) {
						int index = -1;
						for (TOIObject o : selection) {
							index = Math.max(index, objects.indexOf(o));
						}

						TOIObject nextObject = null;
						if (index == objects.size() - 1) {
							index = -1;
						} else {
							nextObject = objects.get(index + 1);
						}

						TOIGroup group = pane.getFactory().create(TOIGroup.class);
						group.setObjects(selection);
						pane.removeObjects(selection);

						if (nextObject != null) {
							index = pane.getObjects().indexOf(nextObject);
						}
						pane.insertObject(group, index);
						pane.select(group, true);
					} else if (selection.get(0) instanceof TOIGroup) {
						TOIGroup group = (TOIGroup) selection.get(0);
						int index = objects.indexOf(group);
						pane.removeObject(group);

						List<TOIObject> groupedObjects = group.getObjects();
						for (TOIObject o : groupedObjects) {
							pane.insertObject(o, index);
							pane.select(o, true);
							index++;
						}
					}
				}
			}
		});
		toolbar2.add(btnGroup);

		JPanel pnlToolbars = new JPanel();
		pnlToolbars.setLayout(new BorderLayout());
		pnlToolbars.add(toolbar1, BorderLayout.NORTH);
		pnlToolbars.add(toolbar2, BorderLayout.CENTER);

		add(pnlToolbars, BorderLayout.NORTH);

		toolbar1.setFocusCycleRoot(true);
		for (int i = 0; i < toolbar1.getComponentCount(); i++) {
			((JComponent) toolbar1.getComponent(i)).setRequestFocusEnabled(false);
			((JComponent) toolbar1.getComponent(i)).setFocusable(false);
		}
		toolbar2.setFocusCycleRoot(true);
		for (int i = 0; i < toolbar2.getComponentCount(); i++) {
			((JComponent) toolbar2.getComponent(i)).setRequestFocusEnabled(false);
			((JComponent) toolbar2.getComponent(i)).setFocusable(false);
		}

		txtText.setRequestFocusEnabled(true);
		txtText.setFocusable(true);
	}

	protected void initZoomToolbar() {
		JToolBar toolbar3 = new JToolBar(SwingConstants.VERTICAL);
		toolbar3.setLayout(new BorderLayout());

		JPanel pnlSliderTop = new JPanel();
		pnlSliderTop.setBorder(null);
		pnlSliderTop.setOpaque(false);
		pnlSliderTop.setLayout(new VerticalFlowLayout());
		toolbar3.add(pnlSliderTop, BorderLayout.NORTH);

		ToolButton btnZoomIn = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/plus.gif")));
		btnZoomIn.setPreferredSize(new Dimension(24, 24));
		btnZoomIn.setToolTipText("Увеличить масштаб [+]");
		btnZoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double zoom = pane.getZoom();
				zoom = (zoom - zoom % 0.25) + 0.25;
				setZoom(zoom);
			}
		});
		pnlSliderTop.add(btnZoomIn);

		zoomSlider.setRequestFocusEnabled(false);
		zoomSlider.setFocusable(false);
		zoomSlider.setMinimum(0);
		zoomSlider.setMaximum(1000);
		zoomSlider.setValue(0);
		zoomSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!ignoreZoomSliderEvent) {
					double zoom = pane.getMinZoom() + (pane.getMaxZoom() - pane.getMinZoom()) * zoomSlider.getValue() / 1000.0;
					setZoom(zoom);
				}
			}
		});
		toolbar3.add(zoomSlider, BorderLayout.CENTER);

		JPanel pnlSliderBottom = new JPanel();
		pnlSliderBottom.setBorder(null);
		pnlSliderBottom.setOpaque(false);
		pnlSliderBottom.setLayout(new VerticalFlowLayout());
		toolbar3.add(pnlSliderBottom, BorderLayout.SOUTH);

		ToolButton btnZoomOut = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/minus.gif")));
		btnZoomOut.setPreferredSize(new Dimension(24, 24));
		btnZoomOut.setToolTipText("Уменьшить масштаб [-]");
		btnZoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double zoom = pane.getZoom();
				zoom = (zoom - zoom % 0.25) - 0.25;
				setZoom(zoom);
			}
		});
		pnlSliderBottom.add(btnZoomOut, BorderLayout.SOUTH);

		ToolButton btnStretchToFit = new ToolButton(new ImageIcon(TOIEditor.class.getResource("img/fit.gif")));
		btnStretchToFit.setPreferredSize(new Dimension(24, 24));
		btnStretchToFit.setToolTipText("Уменьшить масштаб [-]");
		btnStretchToFit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stretchToFit();
			}
		});
		pnlSliderBottom.add(btnStretchToFit, BorderLayout.SOUTH);

		lblZoom.setFont(lblZoom.getFont().deriveFont(10f));
		pnlSliderBottom.add(lblZoom, BorderLayout.SOUTH);
		add(toolbar3, BorderLayout.WEST);
	}

	private JLabel lblZoom = new JLabel("1x");

	public void stretchToFit() {
		Dimension s = scrollPane.getViewport().getSize();
		double zoom = Math.min(s.getWidth() / pane.getDocumentSize().getWidth(), s.getHeight() / pane.getDocumentSize().getHeight());
		setZoom(zoom);
	}

	private boolean ignoreZoomSliderEvent = false;

	private NumberFormat zoomFormat = new DecimalFormat("#.#x");

	private void setZoom(double zoom) {
		pane.setZoom(zoom);
		panePanel.revalidate();
		centrate();

		lblZoom.setText(zoomFormat.format(pane.getZoom()));

		ignoreZoomSliderEvent = true;
		zoomSlider.setValue((int) (1000 * (zoom - pane.getMinZoom()) / (pane.getMaxZoom() - pane.getMinZoom())));
		ignoreZoomSliderEvent = false;
	}

	public static class TOIEditController implements TOIController {
		private TOIController c;

		private ToolToggleButton btn;

		public TOIEditController(TOIController c, ToolToggleButton btn) {
			this.c = c;
			this.btn = btn;
		}

		@Override
		public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
			c.processMouseEvent(e, x, y, adjustX, adjustY);
		}

		@Override
		public void processKeyEvent(KeyEvent e) {
			c.processKeyEvent(e);
		}

		@Override
		public void paint(Graphics2D g, Graphics2D gt, TOIObject o, int type) {
			c.paint(g, gt, o, type);
		}

		@Override
		public void install(TOIPane pane) {
			c.install(pane);
		}

		@Override
		public void uninstall() {
			c.uninstall();
		}
	}

	public class TOIEditPane extends TOIPane {
		private static final long serialVersionUID = 6547397343878035906L;

		public TOIEditPane() {
			setEditable(true);
			setDocumentInsets(new Insets(800, 800, 800, 800));
			updateDocumentSize();
			updateTransform();
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					updateTransform();
					repaint();
				}
			});
			setControlFilter(controlFilter);
			setFactory(new TOIEditorFactory() {
				public <O extends TOIObject> O create(Class<O> type) {
					O o = super.create(type);
					o.setColor(btnColor.getColor());

					if (o instanceof Arrow) {
						Arrow a = (Arrow) o;
						a.setScale(arrowScale, arrowScale * arrowEndScale, arrowScale * arrowEndScale * arrowEndLengthScale);
					}

					if (o instanceof Text) {
						Text t = (Text) o;
						Font font;
						if (cmbFont.getSelectedItem() != null) {
							font = (Font) cmbFont.getSelectedItem();
						} else {
							font = t.getFont();
						}

						float size = font.getSize();
						if (cmbFontSize.getSelectedItem() != null) {
							size = (float) (Integer) cmbFontSize.getSelectedItem();
						}

						int style = (btnItalic.isSelected() ? Font.ITALIC : Font.PLAIN) | (btnBold.isSelected() ? Font.BOLD : Font.PLAIN);

						font = font.deriveFont(style, size);
						t.setFont(font);
					}

					if (o instanceof TOIArrowPathElement) {
						TOIArrowPathElement a = (TOIArrowPathElement) o;
						a.setDirection(btnArrowDirection.getDirection());
					}
					return o;
				}
			});
			enableEvents(AWTEvent.KEY_EVENT_MASK);
		}

		private ControlFilter controlFilter = new ControlFilter() {
			@Override
			public boolean pass(TOIObjectControl c) {
				if (c instanceof TOIRotateObjectControl && !btnRotateControl.isSelected()) {
					return false;
				}
				if (c instanceof TOIScaleObjectControl && !btnScaleControl.isSelected()) {
					return false;
				}
				return true;
			}
		};

		@Override
		protected void focusChanged(TOIObject focusedObject) {
			if (focusedObject != null) {
				btnColor.setColor(focusedObject.getColor(), false);
				btnColor.setEnabled(true);
			} else {
				btnColor.setEnabled(false);
			}

			if (focusedObject instanceof Arrow) {
				Arrow a = (Arrow) focusedObject;
				arrowScale = a.getWidth() / 14.0;
				arrowEndScale = a.getArrowWidth() / 12.0 / arrowScale;
				arrowEndLengthScale = a.getArrowLength() / 6.0 / arrowEndScale / arrowScale;

				btnScaleArrow.repaint();
				btnScaleArrowEnd.repaint();
				btnScaleArrowEndLength.repaint();
			}

			if (focusedObject instanceof TOIArrowPathElement) {
				TOIArrowPathElement a = (TOIArrowPathElement) focusedObject;
				btnArrowDirection.setDirection(a.getDirection(), false);
			}

			if (focusedObject instanceof Text) {
				txtText.setText(focusedObject.getText());
				txtText.setEnabled(true);

				cmbFont.setSelectedFont(focusedObject.getFont());
				cmbFont.setEnabled(true);

				cmbFontSize.setSelectedItem(focusedObject.getFont().getSize());
				cmbFontSize.setEnabled(true);

				btnBold.setSelected(focusedObject.getFont().isBold());
				btnBold.setEnabled(true);

				btnItalic.setSelected(focusedObject.getFont().isItalic());
				btnItalic.setEnabled(true);
			} else {
				txtText.setText("");
				txtText.setEnabled(false);
				cmbFont.setEnabled(false);
				cmbFontSize.setEnabled(false);
				btnBold.setEnabled(false);
				btnItalic.setEnabled(false);
			}
		}

		@Override
		protected void selectionChanged() {
			btnGroup.setSelectedNoEvent(getSelectionSize() == 1 && getSelectedObjects().get(0) instanceof TOIGroup && getSelectedObjects().get(0).isFocused());
			btnGroup.setEnabled(getSelectionSize() > 0 && !(getSelectionSize() == 1 && !(getSelectedObjects().get(0) instanceof TOIGroup)));
		}

		@Override
		protected void drawBackground(Graphics2D g) {
			g.setColor(new Color(230, 230, 230));
			g.fillRect(0, 0, getWidth(), getHeight());

			g.transform(getTransform());

			Dimension docSize = getDocumentSize();
			g.setColor(getBackground());
			g.fillRect(0, 0, docSize.width, docSize.height);
			g.setColor(Color.lightGray);
			g.drawRect(0, 0, docSize.width, docSize.height);

			try {
				g.transform(getTransform().createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}

		private double mx, my;

		private JViewport parentScroll = null;

		@Override
		public void processMouseEvent(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
			super.processMouseEvent(e, x, y, adjustedX, adjustedY);

			if (e.getID() == MouseEvent.MOUSE_RELEASED && e.getButton() == MouseEvent.BUTTON3) {
				showPopup(e, x, y, adjustedX, adjustedY);
			}

			if (!e.isConsumed() && getFocusedObject() == null && getController() == null) {
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

		@Override
		public void setController(TOIController controller) {
			super.setController(controller);
			if (controller == null) {
				btnLock.setSelected(true);
			} else if (controller instanceof TOIEditController) {
				TOIEditController c = (TOIEditController) controller;
				c.btn.setSelected(true);
			}
		}

		public void processKeyEvent(KeyEvent e) {
			super.processKeyEvent(e);
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					deleteSelected();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					pane.clearSelection();
					pane.setFocusedObject(null);
				} else if (e.getKeyCode() == KeyEvent.VK_F1) {
					pane.setController(new TOIEditController(new TOIGeometryController(), btnChangeGeometry));
				} else if (e.getKeyCode() == KeyEvent.VK_F2 && e.isControlDown()) {
					pane.setController(new TOIEditController(new TOIAddPathController(TOIMultiArrowPath.class, TOIAddPathController.LINE), btnAddMultiArrowPathLine));
				} else if (e.getKeyCode() == KeyEvent.VK_F3 && e.isControlDown()) {
					pane.setController(new TOIEditController(new TOIAddPathController(TOIMultiArrowPath.class, TOIAddPathController.QUAD), btnAddMultiArrowPathQuad));
				} else if (e.getKeyCode() == KeyEvent.VK_F4 && e.isControlDown()) {
					pane.setController(new TOIEditController(new TOIAddPathController(TOIMultiArrowPath.class, TOIAddPathController.CUBIC), btnAddMultiArrowPathCube));
				} else if (e.getKeyCode() == KeyEvent.VK_F2) {
					pane.setController(new TOIEditController(new TOIAddPathController(TOIArrowPathElement.class, TOIAddPathController.LINE), btnAddPathLine));
				} else if (e.getKeyCode() == KeyEvent.VK_F3) {
					pane.setController(new TOIEditController(new TOIAddPathController(TOIArrowPathElement.class, TOIAddPathController.QUAD), btnAddPathQuad));
				} else if (e.getKeyCode() == KeyEvent.VK_F4) {
					pane.setController(new TOIEditController(new TOIAddPathController(TOIArrowPathElement.class, TOIAddPathController.CUBIC), btnAddPathCube));
				} else if (e.getKeyCode() == KeyEvent.VK_F5) {
					pane.setController(new TOIEditController(new TOIAddPathController(TOITextPath.class, TOIAddPathController.LINE), btnAddTextPathLine));
				} else if (e.getKeyCode() == KeyEvent.VK_F6) {
					pane.setController(new TOIEditController(new TOIAddPathController(TOITextPath.class, TOIAddPathController.QUAD), btnAddTextPathQuad));
				} else if (e.getKeyCode() == KeyEvent.VK_F7) {
					pane.setController(new TOIEditController(new TOIAddPathController(TOITextPath.class, TOIAddPathController.CUBIC), btnAddTextPathCube));
				} else if (e.getKeyCode() == KeyEvent.VK_F8) {
					pane.setController(new TOIEditController(new TOIAddPathElementController(), btnAddArrow));
				} else if (e.getKeyCode() == KeyEvent.VK_F9) {
					pane.setController(new TOIAddTextController());
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					if (e.isControlDown()) {
						JFileChooser d = new JFileChooser();
						int option = d.showSaveDialog(TOIEditor.this);
						if (option == JFileChooser.APPROVE_OPTION) {
							try {
								File f = d.getSelectedFile();
								pane.export(new FileWriter(f));
							} catch (Exception exc) {
								exc.printStackTrace();
							}
						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_A) {
					if (e.isControlDown()) {
						pane.selectAll();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_C) {
					if (e.isControlDown()) {
						copy();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_V) {
					if (e.isControlDown()) {
						paste();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					if (e.isControlDown()) {
						pane.toUp(pane.getFocusedObject());
					}
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					if (e.isControlDown()) {
						pane.toDown(pane.getFocusedObject());
					}
				} else if (e.getKeyCode() == KeyEvent.VK_P) {
					if (e.isControlDown()) {
						try {
							String xml = new TOIBuilder(pane.getFactory()).export(getObjects());
							System.out.println(xml);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void showPopup(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		JPopupMenu popup = new JPopupMenu();

		TOIObject o = pane.getObject(x, y);
		if (o instanceof TOIGroup) {
			final TOIGroup g = (TOIGroup) o;
			JCheckBoxMenuItem menuCombine = new JCheckBoxMenuItem("Объединять стрелки", g.isCombinePathes());
			menuCombine.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					g.setCombinePathes(!g.isCombinePathes());
					pane.repaint();
				}
			});
			popup.add(menuCombine);
		}

		if (popup.getComponentCount() > 0) {
			popup.show(pane, e.getX(), e.getY());
		}
	}

	public String getPreferredFormat() {
		return preferredFormat;
	}

	public void setPreferredFormat(String preferredFormat) {
		this.preferredFormat = preferredFormat;
	}

	public static void main(String[] args) {
		TOIEditor e = new TOIEditor();
		JFrame f = new JFrame();
		f.setContentPane(e);
		f.setBounds(200, 50, 800, 700);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
