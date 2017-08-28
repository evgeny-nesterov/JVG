package ru.nest.jvg.editor.clipboard;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.menu.WMenuItem;

import ru.nest.jvg.JVGDefaultFactory;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.JVGTransferHandler;
import ru.nest.jvg.JVGTransferable;
import ru.nest.jvg.editor.Util;
import ru.nest.jvg.editor.editoraction.InsertEditorAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParser;

public class JVGClipboardPanel extends JList implements JVGClipboardListener {
	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private JVGTransferHandler th = new JVGTransferHandler();

	private ClipboardListModel model = new ClipboardListModel();

	private JVGClipboard clipboard;

	public JVGClipboardPanel(JVGClipboard clipboard) {
		this.clipboard = clipboard;

		setModel(model);
		setOpaque(false);
		setCellRenderer(new ClipboardListRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		setTransferHandler(th);
		getDropTarget().setActive(true);

		DragSource source = DragSource.getDefaultDragSource();
		source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, new DragGestureListener() {
			@Override
			public void dragGestureRecognized(DragGestureEvent dge) {
				int index = locationToIndex(dge.getDragOrigin());
				JVGClipboardContext ctx = index != -1 ? (JVGClipboardContext) model.getElementAt(index) : null;
				if (ctx != null) {
					dge.startDrag(null, new JVGTransferable(ctx.getData(), ctx.getWidth(), ctx.getHeight()));
				}
			}
		});
		addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					JVGClipboardContext ctx = (JVGClipboardContext) getSelectedValue();
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int index = locationToIndex(e.getPoint());
					JVGClipboardContext ctx = index != -1 ? (JVGClipboardContext) model.getElementAt(index) : null;
					if (ctx != null) {
						if (e.getClickCount() == 1) {
							th.setDragImage(ctx.getSnapshot().getImage());
							th.setDragImageOffset(new Point(ctx.getSnapshot().getIconWidth() / 2, ctx.getSnapshot().getIconHeight() / 2));
							th.exportAsDrag((JComponent) e.getSource(), e, TransferHandler.COPY);
						} else if (e.getClickCount() == 2) {
							preview(ctx);
						}
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					int index = locationToIndex(e.getPoint());
					setSelectedIndex(index);
					if (index != -1) {
						showPopup(e.getX(), e.getY());
					}
				}
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					int selectedIndex = getSelectedIndex();
					if (selectedIndex != -1) {
						removeSelected();
						e.consume();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					JVGClipboardContext ctx = (JVGClipboardContext) getSelectedValue();
					preview(ctx);
				}
			}
		});

		clipboard.addListener(this);
	}

	public void showPopup(int x, int y) {
		JPopupMenu popup = new JPopupMenu();

		WMenuItem menuPreview = new WMenuItem(lm.getValue("clipboard.preview", "Preview"));
		menuPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JVGClipboardContext ctx = (JVGClipboardContext) getSelectedValue();
				preview(ctx);
			}
		});
		popup.add(menuPreview);

		WMenuItem menuInsert = new WMenuItem(lm.getValue("clipboard.insert", "Insert"));
		menuInsert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JVGClipboardContext ctx = (JVGClipboardContext) getSelectedValue();
				if (ctx != null) {
					InsertEditorAction action = new InsertEditorAction(ctx.getData());
					action.actionPerformed(null);
				}
			}
		});
		popup.add(menuInsert);

		WMenuItem menuDelete = new WMenuItem(lm.getValue("clipboard.delete", "Delete"));
		menuDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeSelected();
			}
		});
		popup.add(menuDelete);

		popup.show(this, x, y);
	}

	public void removeSelected() {
		int selectedIndex = getSelectedIndex();
		if (selectedIndex != -1) {
			JVGClipboardContext ctx = (JVGClipboardContext) model.getElementAt(selectedIndex);
			remove(ctx);

			if (selectedIndex == model.getSize()) {
				selectedIndex = model.getSize() - 1;
			}
			setSelectedIndex(selectedIndex);
		}
	}

	public void remove(JVGClipboardContext ctx) {
		if (ctx != null) {
			model.remove(ctx);
			if (ctx.getFile() != null) {
				try {
					new File(ctx.getFile()).delete();
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}

	public void clear() {
		clipboard.removeListener(this);
		model.clear();

		// TODO delete all files
	}

	@Override
	public void dataAddedToClipboard(JVGClipboardEvent event) {
		model.add(event.getContext());
		ensureIndexIsVisible(model.getSize() - 1);
	}

	@Override
	public void dataRemovedFromClipboard(JVGClipboardEvent event) {
		remove(event.getContext());
	}

	@Override
	public void dataSaved(JVGClipboardEvent event) {
	}

	@Override
	public void paintComponent(Graphics g) {
		Util.paintFormBackground(g, getWidth(), getHeight());
		super.paintComponent(g);
	}

	public void preview(JVGClipboardContext ctx) {
		if (ctx != null) {
			try {
				JVGPane pane = new JVGPane();
				pane.setScriptingEnabled(true);
				JVGEditorKit editorKit = pane.getEditorKit();
				editorKit.setFactory(JVGFactory.createDefault());

				JVGParser parser = new JVGParser(editorKit.getFactory());
				JVGRoot root = parser.parse(new ByteArrayInputStream(ctx.getData().getBytes()));
				pane.setRoot(root);

				Rectangle2D bounds = root.getRectangleBounds();
				double x = bounds.getX() - 100;
				double y = bounds.getY() - 100;
				double w = bounds.getWidth() + 200;
				double h = bounds.getHeight() + 200;

				AffineTransform transform = AffineTransform.getTranslateInstance(-x, -y);
				pane.setTransform(transform);
				pane.setPreferredSize(new Dimension((int) w, (int) h));

				JFrame previewFrame = ru.nest.jvg.editor.Util.showSchema(pane);
				previewFrame.setTitle(lm.getValue("clipboard.preview", "Preview"));
			} catch (JVGParseException exc) {
				exc.printStackTrace();
			}
		}
	}
}
