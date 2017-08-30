package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import com.sun.java.swing.plaf.motif.MotifInternalFrameUI;

import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.parser.JVGParseException;

public class JVGPaneInternalFrame extends JInternalFrame implements Comparable<JVGPaneInternalFrame> {
	private JVGColumnHeader columnHeader;

	private JVGRowHeader rowHeader;

	private JComponent corner = new JLabel() {
		{
			setOpaque(true);
			setBackground(Color.white);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(Color.black);
			g.drawLine(0, 0, getWidth(), getHeight());
		}
	};

	private long openTime;

	public JVGPaneInternalFrame(JVGEditor editor) {
		this(editor, 0, 0);
	}

	public JVGPaneInternalFrame(JVGEditor editor, int documentWidth, int documentHeight) {
		super(JVGLocaleManager.getInstance().getValue("pane.default.name", "Untitled*"), true, true, true, true);

		this.editor = editor;
		openTime = System.currentTimeMillis();

		pane = createPane(documentWidth, documentHeight);
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
		pane.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				columnHeader.setMarkerPosition(e.getX());
				rowHeader.setMarkerPosition(e.getY());
				columnHeader.repaint();
				rowHeader.repaint();
			}
		});

		scrollPane = new JScrollPane(pane);
		scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		scrollPane.setWheelScrollingEnabled(true);

		columnHeader = new JVGColumnHeader(pane);
		rowHeader = new JVGRowHeader(pane);
		showRules(editor.isShowRules());

		setContentPane(scrollPane);

		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameOpened(InternalFrameEvent e) {
				centrate();
			}
		});

		pane.addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				isUnsaved = true;
			}
		});

		setUI(new MotifInternalFrameUI(this));
	}

	private boolean isUnsaved = false;

	public boolean isUnsaved() {
		return isUnsaved;
	}

	public void setUnsaved(boolean isUnsaved) {
		this.isUnsaved = isUnsaved;
	}

	public void centrate() {
		JViewport v = scrollPane.getViewport();
		scrollPane.invalidate();
		v.invalidate();
		revalidate();

		Dimension visibleSize = v.getSize();
		Insets documentInsets = getPane().getDocumentInsets();
		Dimension documentSize = getPane().getDocumentSize();

		int x = documentInsets.left + (documentSize.width - visibleSize.width) / 2;
		int y = documentInsets.top + (documentSize.height - visibleSize.height) / 2;

		v.setViewSize(getPane().getPreferredSize());
		v.setViewPosition(new Point(x, y));

		scrollPane.invalidate();
		v.invalidate();
		revalidate();
		repaint();
	}

	public Point2D getCenterPointInPercent() {
		JViewport v = scrollPane.getViewport();
		Dimension vs = pane.getPreferredSize();
		Dimension s = v.getSize();
		Point p = v.getViewPosition();

		double x = p.x + s.width / 2.0;
		double y = p.y + s.height / 2.0;
		return new Point2D.Double(x / vs.width, y / vs.height);
	}

	public void setCenterPointInPercent(Point2D percentCenterPoint) {
		JViewport v = scrollPane.getViewport();
		Dimension vs = pane.getPreferredSize();
		Dimension s = v.getSize();

		int x = (int) (percentCenterPoint.getX() * vs.width - s.width / 2.0);
		int y = (int) (percentCenterPoint.getY() * vs.height - s.height / 2.0);
		v.setViewPosition(new Point(x, y));

		scrollPane.invalidate();
		v.invalidate();
		revalidate();
		repaint();
	}

	public Dimension getViewSize() {
		JViewport v = scrollPane.getViewport();
		return v.getViewSize();
	}

	public Point getViewPosition() {
		JViewport v = scrollPane.getViewport();
		return v.getViewPosition();
	}

	private JVGEditPane pane;

	public JVGEditPane getPane() {
		return pane;
	}

	private JScrollPane scrollPane;

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public void scrollSelectionToVisible() {
		Rectangle2D r = pane.getSelectionManager().getSelectionBounds();
		if (r != null) {
			r = pane.getTransform().createTransformedShape(r).getBounds2D();

			JViewport v = scrollPane.getViewport();
			Dimension vs = v.getSize();
			Point pos = new Point((int) (r.getX() + (r.getWidth() - vs.width) / 2), (int) (r.getY() + (r.getHeight() - vs.height) / 2));
			v.setViewPosition(pos);

			scrollPane.invalidate();
			v.invalidate();
			revalidate();
			repaint();
		}
	}

	public void showRect(Rectangle r) {
		scrollPane.scrollRectToVisible(r);
	}

	private JVGEditor editor;

	public JVGEditor getEditor() {
		return editor;
	}

	public File save() {
		File file = pane.save();
		if (file != null) {
			setTitle(file.getName());
			isUnsaved = false;
		}
		return file;
	}

	public File saveAs() {
		File file = pane.saveAs();
		if (file != null) {
			setTitle(file.getName());
			isUnsaved = false;
		}
		return file;
	}

	public File saveSelection() {
		return pane.saveSelection();
	}

	public void load(File file) throws JVGParseException {
		setTitle(file.getName());
		pane.load(file);
		centrate();
		isUnsaved = false;
	}

	public void load(String title, String document) throws JVGParseException {
		setTitle(title);
		pane.load(document);
		centrate();
		isUnsaved = false;
	}

	public void showRules(boolean showRules) {
		if (showRules) {
			scrollPane.setColumnHeaderView(columnHeader);
			scrollPane.setRowHeaderView(rowHeader);
			scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, corner);
		} else {
			scrollPane.setColumnHeaderView(null);
			scrollPane.setRowHeaderView(null);
			scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, null);
		}
	}

	@Override
	public int compareTo(JVGPaneInternalFrame f) {
		return (int) (openTime - f.openTime);
	}

	protected JVGEditPane createPane(int documentWidth, int documentHeight) {
		return new JVGEditPane(editor, documentWidth, documentHeight);
	}
}
