package satis.iface.graph.axis;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import satis.iface.graph.Bounds;
import satis.iface.graph.GraphListener;
import satis.iface.graph.Group;
import satis.iface.graph.StaticGraphArea;
import satis.iface.graph.grid.Grid;

public class Axis extends JComponent implements GraphListener {
	private static final long serialVersionUID = 1L;

	public Axis(Group group, int orientation) {
		this(group, orientation, true);
	}

	public Axis(Group group, int orientation, boolean enableMouseEvents) {
		this.group = group;
		this.orientation = orientation;

		switch (orientation) {
			case Grid.Y_AXIS:
				grid = group.getGridHor();
				break;

			case Grid.X_AXIS:
				grid = group.getGridVer();
				break;
		}

		enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
		if (enableMouseEvents) {
			enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		}

		setOpaque(false);
	}

	private int orientation;

	private Grid grid;

	public Grid getGrid() {
		return grid;
	}

	// It's not allowed to change group
	private Group group;

	public Bounds getGroupInnerBounds() {
		return group.getInnerBounds();
	}

	private Object format = null;

	/*
	 * Format classes: satis.iface.Format java.text.Format
	 */
	public void setFormat(Object format) {
		this.format = format;
		repaint();
	}

	public Object getFormat() {
		return format;
	}

	private Color labelsColor = Color.black;

	public void setLabelsColor(Color labelsColor) {
		this.labelsColor = labelsColor;
	}

	public Color getLabelsColor() {
		return labelsColor;
	}

	@Override
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);

		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			requestFocus();

			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				switch (orientation) {
					case Grid.X_AXIS:
						pressOnAxisX(e);
						break;

					case Grid.Y_AXIS:
						pressOnAxisY(e);
						break;
				}
			}
		}
	}

	public void pressOnAxisX(MouseEvent e) {
		int type = -1;
		Bounds b = group.getInnerBounds();

		// --- Left ---
		boolean onLabel = false;
		double min = grid.getValue(0);
		int coord = (int) ((getWidth() - 1) * (min - b.x) / b.w);
		String val = (String) grid.getRenderer().formatValue(min, format);
		Component c = grid.getRenderer().getGridLabel(grid, group, val, false);
		Dimension size = c.getPreferredSize();

		int x = coord - size.width / 2;
		if (x < 0) {
			x = 0;
		}

		if (e.getX() > x && e.getX() < x + size.width) {
			onLabel = true;
			type = AxisTextField.X_MIN;
		}

		// --- Right ---
		if (!onLabel) {
			double max = grid.getValue(grid.getVisibleLinesCount() - 1);
			coord = (int) ((getWidth() - 1) * (max - b.x) / b.w);
			val = (String) grid.getRenderer().formatValue(max, format);
			c = grid.getRenderer().getGridLabel(grid, group, val, false);
			size = c.getPreferredSize();

			x = coord - size.width / 2;
			if (x > getWidth() - size.width - 1) {
				x = getWidth() - size.width - 1;
			}

			if (e.getX() > x && e.getX() < x + size.width) {
				onLabel = true;
				type = AxisTextField.X_MAX;
			}
		}

		if (onLabel) {
			AxisTextField txt = new AxisTextField(type);
			Border border = BorderFactory.createLoweredBevelBorder();
			Insets insets = border.getBorderInsets(txt);
			txt.setBorder(border);
			txt.setText(val);
			size.width += insets.left + insets.right;
			size.height += insets.top + insets.bottom;
			txt.setSize(size);

			JPanel glassPanel = new JPanel();
			glassPanel.setOpaque(false);
			glassPanel.setLayout(null);
			glassPanel.setRequestFocusEnabled(true);
			glassPanel.add(txt);

			JFrame f = (JFrame) SwingUtilities.getRoot(this);
			f.setGlassPane(glassPanel);
			txt.setLocation(SwingUtilities.convertPoint(this, x - insets.left, -insets.top, glassPanel));

			glassPanel.setVisible(true);
			txt.requestFocus();
			txt.setCaretPosition(0);
		}
	}

	public void pressOnAxisY(MouseEvent e) {
		int type = -1;
		Bounds b = group.getInnerBounds();

		// --- Bottom ---
		boolean onLabel = false;
		double min = grid.getValue(0);
		int coord = (int) ((getHeight() - 1) * (1 - (min - b.y) / b.h));
		String val = (String) grid.getRenderer().formatValue(min, format);
		Component c = grid.getRenderer().getGridLabel(grid, group, val, false);
		Dimension size = c.getPreferredSize();

		int y = coord - size.height / 2;
		if (y > getHeight() - size.height - 1) {
			y = getHeight() - size.height - 1;
		}

		if (e.getY() > y && e.getY() < y + size.height) {
			onLabel = true;
			type = AxisTextField.Y_MIN;
		}

		// --- Top ---
		if (!onLabel) {
			double max = grid.getValue(grid.getVisibleLinesCount() - 1);
			coord = (int) ((getHeight() - 1) * (1 - (max - b.y) / b.h));
			val = (String) grid.getRenderer().formatValue(max, format);
			c = grid.getRenderer().getGridLabel(grid, group, val, false);
			size = c.getPreferredSize();

			y = coord - size.height / 2;
			if (y < 0) {
				y = 0;
			}

			if (e.getY() > y && e.getY() < y + size.height) {
				onLabel = true;
				type = AxisTextField.Y_MAX;
			}
		}

		if (onLabel) {
			AxisTextField txt = new AxisTextField(type);
			Border border = BorderFactory.createLoweredBevelBorder();
			Insets insets = border.getBorderInsets(txt);
			txt.setBorder(border);
			txt.setText(val);
			size.width += insets.left + insets.right;
			size.height += insets.top + insets.bottom;
			txt.setSize(size);

			JPanel glassPanel = new JPanel();
			glassPanel.setOpaque(false);
			glassPanel.setLayout(null);
			glassPanel.setRequestFocusEnabled(true);
			glassPanel.add(txt);

			JFrame f = (JFrame) SwingUtilities.getRoot(this);
			f.setGlassPane(glassPanel);
			txt.setLocation(SwingUtilities.convertPoint(this, -insets.left, y - insets.top, glassPanel));

			glassPanel.setVisible(true);
			txt.requestFocus();
			txt.setCaretPosition(0);
		}
	}

	class AxisTextField extends JTextField {
		private static final long serialVersionUID = 1L;

		public final static int X_MIN = 0;

		public final static int X_MAX = 1;

		public final static int Y_MIN = 2;

		public final static int Y_MAX = 3;

		private int type;

		public AxisTextField(int type) {
			super();
			this.type = type;

			setOpaque(true);
			addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					enter();
				}
			});

			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						enter();
					} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						cancel();
					}
				}
			});
		}

		public void enter() {
			try {
				double value = parse(getText());
				Bounds b = group.getInnerBounds();
				switch (type) {
					case X_MIN:
						if (value < b.x + b.w) {
							b.w += b.x - value;
							b.x = value;
							group.fireInnerBoundsChanged();
						}
						break;

					case X_MAX:
						if (value > b.x) {
							b.w = value - b.x;
							group.fireInnerBoundsChanged();
						}
						break;

					case Y_MIN:
						if (value < b.y + b.h) {
							b.h += b.y - value;
							b.y = value;
							group.fireInnerBoundsChanged();
						}
						break;

					case Y_MAX:
						if (value > b.y) {
							b.h = value - b.y;
							group.fireInnerBoundsChanged();
						}
						break;
				}
			} catch (Exception Exc) {
			} finally {
				cancel();
			}
		}

		public void cancel() {
			JPanel glassPanel = (JPanel) getParent();
			if (glassPanel != null) {
				glassPanel.setVisible(false);
			}
		}

		public double parse(String val) {
			if (format instanceof DateFormat) {
				DateFormat f = (DateFormat) format;
				Date date = f.parse(val, new ParsePosition(0));
				return date.getTime();
			} else {
				return Double.parseDouble(val);
			}
		}
	}

	@Override
	public void processComponentEvent(ComponentEvent e) {
		super.processComponentEvent(e);

		if (e.getID() == ComponentEvent.COMPONENT_RESIZED || e.getID() == ComponentEvent.COMPONENT_SHOWN) {
			repaint();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (grid != null) {
			switch (grid.getOrientation()) {
				case Grid.X_AXIS:
					paintAxisX(g, group);
					break;

				case Grid.Y_AXIS:
					paintAxisY(g, group);
					break;
			}
		}
	}

	public void paintAxisX(Graphics g, Group group) {
		int maxHeight = getHeight(), maxWidth = 0;
		double x = 0, old = 0;
		Bounds b = group.getInnerBounds();
		double min = b.x, max = b.x + b.w, w = b.w;

		// --- Prepare labels ---
		List<GridLabel> labels = new ArrayList<GridLabel>();
		int len = grid.getVisibleLinesCount();
		if (len > 100) {
			len = 100;
		}

		for (int index = 0; index < len; index++) {
			double val = grid.getValue(index);
			if (val > max) {
				break;
			}

			if (val >= min) {
				Component c = grid.getRenderer().getGridLabel(grid, group, grid.getRenderer().formatValue(grid.getValue(index), format), false);
				Dimension size = c.getPreferredSize();
				maxHeight = Math.max(maxHeight, size.height);
				maxWidth = Math.max(maxWidth, size.width);
				x = (getWidth() - 1) * (val - min) / w;

				if (x >= 0 && x < getWidth()) {
					GridLabel l = new GridLabel();
					l.index = index;
					l.coord = x;
					l.size = size.width;

					labels.add(l);
				}
			}
		}

		if (labels.size() > 0) {
			GridLabel firstLabel = labels.get(0);
			double firstCoord = firstLabel.coord - firstLabel.size / 2;
			if (firstCoord < 0) {
				firstCoord = 0;
			}
			firstCoord += firstLabel.size;

			GridLabel endLabel = labels.get(labels.size() - 1);
			double endCoord = endLabel.coord + firstLabel.size / 2;
			if (endCoord > getWidth()) {
				endCoord = getWidth();
			}
			endCoord -= endLabel.size;

			// --- Draw labels ---
			boolean isDraw, isLast;
			g.setColor(labelsColor);
			len = labels.size();
			for (int i = 0; i < len; i++) {
				GridLabel l = labels.get(i);
				isLast = (i == 0 || i == labels.size() - 1);

				Component c = grid.getRenderer().getGridLabel(grid, group, grid.getRenderer().formatValue(grid.getValue(l.index), format), isLast);

				x = l.coord;
				if (!isLast) {
					x -= l.size / 2.0;
					isDraw = (x - l.size > old && x > firstCoord && x + l.size < endCoord);
				} else {
					if (i == 0) {
						x = firstCoord - l.size;
						isDraw = true;
					} else {
						x = endCoord;
						isDraw = true;
					}
				}

				if (isDraw) {
					g.translate((int) x, 0);
					c.paint(g);
					g.translate(-(int) x, 0);
					old = x;
				}
			}
		}

		Dimension prefSize = getPreferredSize();
		maxHeight = preferredSize > 0 ? preferredSize : maxHeight;
		if (prefSize.height != maxHeight) {
			prefSize.height = maxHeight;
			setPreferredSize(prefSize);
			revalidate();
		}
	}

	public void paintAxisY(Graphics g, Group group) {
		int maxHeight = 0, maxWidth = getWidth();
		double y = 0, old = getHeight() + 1;
		Bounds b = group.getInnerBounds();
		double min = b.y, max = b.y + b.h, h = b.h;

		// --- Prepare labels ---
		List<GridLabel> labels = new ArrayList<GridLabel>();
		for (int index = 0; index < grid.getVisibleLinesCount(); index++) {
			double val = grid.getValue(index);
			if (val > max) {
				break;
			}

			if (val >= min) {
				Component c = grid.getRenderer().getGridLabel(grid, group, grid.getRenderer().formatValue(grid.getValue(index), format), false);
				Dimension size = c.getPreferredSize();
				maxHeight = Math.max(maxHeight, size.height);
				maxWidth = Math.max(maxWidth, size.width + 5);
				y = (getHeight() - 1) * (1 - (val - min) / h);

				if (y >= 0 && y < getHeight()) {
					GridLabel l = new GridLabel();
					l.index = index;
					l.coord = y;
					l.size = size.height;
					labels.add(l);
				}
			}
		}

		if (labels.size() > 0) {
			GridLabel firstLabel = labels.get(0);
			double firstCoord = firstLabel.coord + firstLabel.size / 2;
			if (firstCoord > getHeight()) {
				firstCoord = getHeight();
			}
			firstCoord -= firstLabel.size;

			GridLabel endLabel = labels.get(labels.size() - 1);
			double endCoord = endLabel.coord - firstLabel.size / 2;
			if (endCoord < 0) {
				endCoord = 0;
			}
			endCoord += endLabel.size;

			// --- Draw labels ---
			boolean isDraw, isLast;
			g.setColor(labelsColor);
			for (int i = 0; i < labels.size(); i++) {
				GridLabel l = labels.get(i);
				isLast = (i == 0 || i == labels.size() - 1);
				Component c = grid.getRenderer().getGridLabel(grid, group, grid.getRenderer().formatValue(grid.getValue(l.index), format), isLast);

				y = l.coord;
				if (!isLast) {
					y -= l.size / 2.0;
					isDraw = (y + l.size < old && y > endCoord && y + l.size < firstCoord);
				} else {
					if (i == 0) {
						y = firstCoord;
						isDraw = true;
					} else {
						y = endCoord - l.size;
						isDraw = true;
					}
				}

				if (isDraw) {
					g.translate(2, (int) y);
					c.paint(g);
					g.translate(-2, -(int) y);
					old = y;
				}
			}
		}

		Dimension prefSize = getPreferredSize();
		maxWidth = preferredSize > 0 ? preferredSize : maxWidth;
		if (prefSize.width != maxWidth) {
			prefSize.width = maxWidth;
			setPreferredSize(prefSize);
			revalidate();
		}
	}

	private int preferredSize = 0;

	public void setPreferred(int preferredSize) {
		this.preferredSize = preferredSize;
	}

	@Override
	public void graphResized(StaticGraphArea g) {
		// Do nothing
	}

	@Override
	public void groupRescaled(StaticGraphArea g, Group group) {
		repaint();
	}

	@Override
	public void groupAdded(StaticGraphArea graph, Group group) {
		// Do nothing
	}

	@Override
	public void groupRemoved(StaticGraphArea graph, Group group) {
		// Do nothing
	}
}

class GridLabel {
	public int index;

	public double coord;

	public int size;

	public boolean isLast;
}
