package ru.nest.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;

public class TableLayout implements LayoutManager2 {
	private final static int PREFFERED = 0;

	private final static int MINIMUM = 1;

	private final static Dimension DEFAULT_SIZE = new Dimension(20, 20);

	public final static int CENTER = 0;

	public final static int TOP = 1;

	public final static int LEFT = 2;

	public final static int BOTTOM = 3;

	public final static int RIGTH = 4;

	public boolean isPrefferedX = false;

	public boolean isPrefferedY = false;

	public int alignmentX = LEFT;

	public int alignmentY = TOP;

	public Dimension intercellSpacing = new Dimension(1, 1);

	// ---
	public final static int EMPTY = 0;

	public final static int CELL_CONTINUE = 1;

	public final static int CELL = 2;

	public int rows, columns;

	public int[] rowsStates = new int[0];

	public int[] columnsStates = new int[0];

	double width, heigth; // weigt

	public double wPixel, hPixel; // pixel

	public double[] wx = new double[0]; // weigt

	public double[] wy = new double[0]; // weigt

	public double[] sx = new double[0]; // pixel

	public double[] sy = new double[0]; // pixel

	public double[] posx = new double[0]; // pixel

	public double[] posy = new double[0]; // pixel

	private Object[] o = new Object[0];

	public void updateGrid(boolean isPrefferedX, boolean isPrefferedY, int sizeType) {
		boolean loadComponents = isPrefferedX | isPrefferedY;
		TableConstraints cs = null;
		Component c = null;
		Dimension size = null;

		// --- length ---
		rows = 0;
		columns = 0;
		if (o.length != map.size()) {
			o = map.values().toArray();
		} else {
			o = map.values().toArray(o);
		}

		for (int i = 0; i < o.length; i++) {
			cs = (TableConstraints) o[i];
			columns = Math.max(columns, cs.x + cs.wx.length);
			rows = Math.max(rows, cs.y + cs.wy.length);
		}

		if (columns == 0 || rows == 0) {
			return;
		}

		// --- states ---
		if (columnsStates.length != columns) {
			columnsStates = new int[columns];
		}
		Arrays.fill(columnsStates, 0);
		if (rowsStates.length != rows) {
			rowsStates = new int[rows];
		}
		Arrays.fill(rowsStates, 0);
		for (int i = 0; i < o.length; i++) {
			cs = (TableConstraints) o[i];

			columnsStates[cs.x] = CELL;
			for (int j = 1; j < cs.wx.length; j++) {
				columnsStates[cs.x + j] = Math.max(columnsStates[cs.x + j], CELL_CONTINUE);
			}

			rowsStates[cs.y] = CELL;
			for (int j = 1; j < cs.wy.length; j++) {
				rowsStates[cs.y + j] = Math.max(rowsStates[cs.y + j], CELL_CONTINUE);
			}
		}

		// for(int i = 0; i < columnsStates.length; i++)
		// if(columnsStates[i] != EMPTY)
		// columns++;
		//
		// for(int i = 0; i < rowsStates.length; i++)
		// if(rowsStates[i] != EMPTY)
		// rows++;

		// --- weigh ---
		if (wx.length != columns) {
			wx = new double[columns];
		} else {
			Arrays.fill(wx, 0);
		}

		if (wy.length != rows) {
			wy = new double[rows];
		} else {
			Arrays.fill(wy, 0);
		}

		if (!isPrefferedX || !isPrefferedY) {
			for (int i = 0; i < o.length; i++) {
				cs = (TableConstraints) o[i];

				if (cs.wx.length > 0 && !isPrefferedX) {
					for (int j = 0; j < cs.wx.length; j++) {
						wx[cs.x + j] = Math.max(wx[cs.x + j], cs.wx[j]);
					}
				}

				if (cs.wy.length > 0 && !isPrefferedY) {
					for (int j = 0; j < cs.wy.length; j++) {
						wy[cs.y + j] = Math.max(wy[cs.y + j], cs.wy[j]);
					}
				}
			}
		}

		// --- check on load components ---
		for (int i = 0; i < wx.length; i++) {
			if (wx[i] == 0) {
				loadComponents = true;
				break;
			}
		}
		if (!loadComponents) {
			for (int i = 0; i < wy.length; i++) {
				if (wy[i] == 0) {
					loadComponents = true;
					break;
				}
			}
		}

		if (sx.length != columns) {
			sx = new double[columns];
		} else {
			Arrays.fill(sx, 0);
		}

		if (sy.length != rows) {
			sy = new double[rows];
		} else {
			Arrays.fill(sy, 0);
		}

		// --- sizes ---
		if (loadComponents) {
			if (o.length != map.size()) {
				o = map.keySet().toArray();
			} else {
				o = map.keySet().toArray(o);
			}

			// --- sizes ---
			for (int i = 0; i < o.length; i++) {
				c = (Component) o[i];
				cs = map.get(c);
				size = null;

				if (cs.wx.length > 0) {
					double weigt = 0;
					for (int j = 0; j < cs.wx.length; j++) {
						weigt += wx[cs.x + j];
					}

					if (weigt == 0) {
						size = getSize(c, sizeType);

						double w = size.width + intercellSpacing.width;
						if (cs.insets != null) {
							w += cs.insets.left + cs.insets.right;
						}
						w /= cs.wx.length;

						for (int j = 0; j < cs.wx.length; j++) {
							sx[cs.x + j] = Math.max(sx[cs.x + j], w);
						}
					}
				}

				if (cs.wy.length > 0) {
					double weigt = 0;
					for (int j = 0; j < cs.wy.length; j++) {
						weigt += wy[cs.y + j];
					}

					if (weigt == 0) {
						if (size == null) {
							size = getSize(c, sizeType);
						}
						double h = size.height + intercellSpacing.height;
						if (cs.insets != null) {
							h += cs.insets.top + cs.insets.bottom;
						}
						h /= cs.wy.length;

						for (int j = 0; j < cs.wy.length; j++) {
							sy[cs.y + j] = Math.max(sy[cs.y + j], h);
						}
					}
				}
			}
		}

		// --- total weigh and size, normalize ---
		width = 0;
		wPixel = 0;
		for (int i = 0; i < wx.length; i++) {
			width += wx[i];
			wPixel += sx[i];
		}
		if (width != 0) {
			for (int i = 0; i < wx.length; i++) {
				wx[i] /= width;
			}
		}

		heigth = 0;
		hPixel = 0;
		for (int i = 0; i < wy.length; i++) {
			heigth += wy[i];
			hPixel += sy[i];
		}
		if (heigth != 0) {
			for (int i = 0; i < wy.length; i++) {
				wy[i] /= heigth;
			}
		}
	}

	private Dimension getSize(Component c, int sizeType) {
		Dimension size = null;
		if (sizeType == PREFFERED) {
			size = c.getPreferredSize();
		} else {
			size = c.getMinimumSize();
		}

		if (size == null) {
			size = DEFAULT_SIZE;
		}

		return size;
	}

	@Override
	public void layoutContainer(Container parent) {
		updateGrid(isPrefferedX, isPrefferedY, PREFFERED);

		// --- inner bounds ---
		Insets insets = parent.getInsets();
		int X = insets.left + intercellSpacing.width;
		int W = parent.getWidth() - X - insets.right;
		if (width == 0) {
			if (alignmentX == CENTER) {
				X += (W - wPixel) / 2;
			} else if (alignmentX == RIGTH) {
				X += (W - wPixel);
			}
		}

		int Y = insets.top + intercellSpacing.height;
		int H = parent.getHeight() - Y - insets.bottom;
		if (heigth == 0) {
			if (alignmentY == CENTER) {
				Y += (H - hPixel) / 2;
			} else if (alignmentY == BOTTOM) {
				Y += (H - hPixel);
			}
		}

		int count = parent.getComponentCount();
		int x, y, w, h;
		Component comp;
		TableConstraints cs;

		// --- cell sizes ---
		for (int i = 0; i < sx.length; i++) {
			if (wx[i] == 0) {
				W -= sx[i];
			}
		}
		for (int i = 0; i < sx.length; i++) {
			if (wx[i] != 0) {
				sx[i] = wx[i] * W;
			}
		}

		for (int i = 0; i < sy.length; i++) {
			if (wy[i] == 0) {
				H -= sy[i];
			}
		}
		for (int i = 0; i < sy.length; i++) {
			if (wy[i] != 0) {
				sy[i] = wy[i] * H;
			}
		}

		// --- pos ---
		double pos = X;
		if (posx.length != columns + 1) {
			posx = new double[columns + 1];
		}
		posx[0] = pos;
		for (int i = 0; i < wx.length; i++) {
			pos += sx[i];
			posx[i + 1] = pos;
		}

		pos = Y;
		if (posy.length != rows + 1) {
			posy = new double[rows + 1];
		}
		posy[0] = pos;
		for (int i = 0; i < wy.length; i++) {
			pos += sy[i];
			posy[i + 1] = pos;
		}

		// --- resize ---
		for (int i = 0; i < count; i++) {
			comp = parent.getComponent(i);
			cs = map.get(comp);

			if (cs != null && cs.wx.length > 0 && cs.wy.length > 0) {
				x = (int) posx[cs.x];
				w = (int) (posx[cs.x + cs.wx.length]) - (int) (posx[cs.x]) - intercellSpacing.width;
				y = (int) posy[cs.y];
				h = (int) (posy[cs.y + cs.wy.length]) - (int) (posy[cs.y]) - intercellSpacing.height;

				if (cs.insets != null) {
					x += cs.insets.left;
					y += cs.insets.top;
					w -= cs.insets.left + cs.insets.right;
					h -= cs.insets.top + cs.insets.bottom;
				}

				comp.setBounds(x, y, w, h);
			}
		}
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		updateGrid(true, true, PREFFERED);

		Insets insets = parent.getInsets();
		int w = (int) wPixel + intercellSpacing.width + insets.left + insets.right;
		int h = (int) hPixel + intercellSpacing.height + insets.top + insets.bottom;

		return new Dimension(w, h);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		updateGrid(true, true, MINIMUM);

		Insets insets = parent.getInsets();
		int w = (int) wPixel + intercellSpacing.width + insets.left + insets.right;
		int h = (int) hPixel + intercellSpacing.height + insets.top + insets.bottom;

		return new Dimension(w, h);
	}

	// -------------
	private Map<Component, TableConstraints> map = new HashMap<>();

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if (comp != null && constraints != null && constraints instanceof TableConstraints) {
			map.put(comp, (TableConstraints) constraints);
		}
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		if (comp != null) {
			map.remove(comp);
		}
	}

	public TableConstraints getConstraints(Component comp) {
		if (comp != null) {
			return map.get(comp);
		} else {
			return null;
		}
	}

	@Override
	public Dimension maximumLayoutSize(Container parent) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// empty
	}

	@Override
	public void invalidateLayout(Container target) {
		// empty
	}

	@Override
	public float getLayoutAlignmentX(Container parent) {
		return 0.5f;
	}

	@Override
	public float getLayoutAlignmentY(Container parent) {
		return 0.5f;
	}

	public void removeEmpty(Container parent, int column, int row) {
		removeEmptyColumn(parent, column);
		removeEmptyRow(parent, row);
	}

	// --- Usefull ---
	public int getGridX(Container parent, int x) {
		for (int i = 0; i < posx.length; i++) {
			if (x >= posx[i] - 2 && x <= posx[i] + 2) {
				return i;
			}
		}

		return -1;
	}

	public int getX(Container parent, int x) {
		for (int i = 0; i < posx.length - 1; i++) {
			if (x >= posx[i] && x <= posx[i + 1]) {
				return i;
			}
		}

		return -1;
	}

	public int getGridY(Container parent, int y) {
		for (int i = 0; i < posy.length; i++) {
			if (y >= posy[i] - 2 && y <= posy[i] + 2) {
				return i;
			}
		}

		return -1;
	}

	public int getY(Container parent, int y) {
		for (int i = 0; i < posy.length - 1; i++) {
			if (y >= posy[i] && y <= posy[i + 1]) {
				return i;
			}
		}

		return -1;
	}

	public boolean isGridX(Container parent, int gx, int y) {
		int gy = getY(parent, y);
		for (Component c : parent.getComponents()) {
			TableConstraints cs = getConstraints(c);
			if (cs != null && cs.y <= gy && cs.y + cs.wy.length > gy) {
				if (cs.x < gx && cs.x + cs.wx.length > gx) {
					// log.debug("isGridX> false: gy=" + gy + ", cs.y=" + cs.y +
					// ", cs.wy.length=" + cs.wy.length);
					return false;
				} else if (cs.x == gx || cs.x + cs.wx.length == gx) {
					// log.debug("isGridX> true: gy=" + gy + ", cs.y=" + cs.y +
					// ", cs.wy.length=" + cs.wy.length);
					return true;
				}
			}
		}

		// log.debug("isGridX> false: not found");
		return false;
	}

	public boolean isGridY(Container parent, int gy, int x) {
		int gx = getX(parent, x);
		for (Component c : parent.getComponents()) {
			TableConstraints cs = getConstraints(c);
			if (cs != null && cs.x <= gx && cs.x + cs.wx.length > gx) {
				if (cs.y < gy && cs.y + cs.wy.length > gy) {
					return false;
				} else if (cs.y == gy || cs.y + cs.wy.length == gy) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isPointOnGridX(Container parent, int x, int y) {
		return isGridX(parent, getGridX(parent, x), y);
	}

	public boolean isPointOnGridY(Container parent, int x, int y) {
		return isGridY(parent, getGridY(parent, y), x);
	}

	public boolean isPointOnGrid(Container parent, int x, int y) {
		return isGridX(parent, getGridX(parent, x), y) || isGridY(parent, getGridY(parent, y), x);
	}

	public void checkDelta(Container parent, int gx, int gy, Point delta) // check
	// components
	// on
	// min
	// sizes
	{
		boolean checkX = gx > 0 && gx < columns && delta.x != 0;
		boolean checkY = gy > 0 && gy < rows && delta.y != 0;
		if (!checkX && !checkY) {
			delta.x = 0;
			delta.y = 0;
			return;
		}

		int dx = delta.x, dy = delta.y;
		if (checkX) {
			if (dx < 0 && posx[gx] + dx <= posx[gx - 1]) {
				dx = (int) (posx[gx - 1] - posx[gx]);
			} else if (dx > 0 && posx[gx] + dx >= posx[gx + 1]) {
				dx = (int) (posx[gx + 1] - posx[gx]);
			}
			delta.x = dx;
			checkX &= dx != 0;
		}
		if (checkY) {
			if (dy < 0 && posy[gy] + dy <= posy[gy - 1]) {
				dy = (int) (posy[gy - 1] - posy[gy]);
			} else if (dy > 0 && posy[gy] + dy >= posy[gy + 1]) {
				dy = (int) (posy[gy + 1] - posy[gy]);
			}
			delta.y = dy;
			checkY &= dy != 0;
		}
		if (!checkX && !checkY) {
			return;
		}

		int minW = 0, minH = 0, w = Integer.MAX_VALUE, h = Integer.MAX_VALUE;
		for (Component c : parent.getComponents()) {
			TableConstraints cs = getConstraints(c);
			if (cs != null) {
				Dimension min = c.getMinimumSize();

				if (checkX && ((dx < 0 && cs.x == gx - 1) || (dx > 0 && cs.x == gx) && minW < min.width)) {
					minW = min.width;
					w = c.getWidth();
				}

				if (checkY && ((dy < 0 && cs.y == gy - 1) || (dy > 0 && cs.y == gy) && minH < min.height)) {
					minH = min.height;
					h = c.getHeight();
				}
			}
		}

		if (checkX && w - Math.abs(dx) < minW) {
			delta.x = w - minW;
			if (dx < 0) {
				delta.x = -delta.x;
			}
		}
		if (checkY && h - Math.abs(dy) < minH) {
			delta.y = h - minH;
			if (dy < 0) {
				delta.y = -delta.y;
			}
		}
	}

	public void resize(Container parent, int gx, int gy, int dx, int dy) // delta
	// in
	// pixels
	{
		boolean resizeX = gx > 0 && gx < columns && dx != 0 && width != 0;
		boolean resizeY = gy > 0 && gy < rows && dy != 0 && heigth != 0;
		if (!resizeX && !resizeY) {
			return;
		}

		double dX = dx * width / parent.getWidth();
		double dY = dy * heigth / parent.getHeight();
		for (Component c : parent.getComponents()) {
			TableConstraints cs = getConstraints(c);
			if (cs != null) {
				if (resizeX) {
					if (cs.x + cs.wx.length == gx) {
						cs.wx[cs.wx.length - 1] += dX;
					} else if (cs.x == gx) {
						cs.wx[0] -= dX;
					} else if (gx > cs.x && gx < cs.x + cs.wx.length) {
						cs.wx[gx - cs.x - 1] += dX;
						cs.wx[gx - cs.x] -= dX;
					}
				}

				if (resizeY) {
					if (cs.y + cs.wy.length == gy) {
						cs.wy[cs.wy.length - 1] += dY;
					} else if (cs.y == gy) {
						cs.wy[0] -= dY;
					} else if (gy > cs.y && gy < cs.y + cs.wy.length) {
						cs.wy[gy - cs.y - 1] += dY;
						cs.wy[gy - cs.y] -= dY;
					}
				}
			}
		}
	}

	public int[] getDeltaIndexX(Container parent, int gy, int count) {
		int[] deltaIndex = new int[columns];
		for (Component c : parent.getComponents()) {
			TableConstraints cs = getConstraints(c);
			if (cs != null) {
				if (cs.y >= gy) {
					cs.y += count;
				} else if (cs.y < gy && cs.y + cs.wy.length > gy) {
					deltaIndex[cs.x] = cs.y + cs.wy.length - gy;
				}
			}
		}
		return deltaIndex;
	}

	public int[] getDeltaIndexY(Container parent, int gx, int count) {
		int[] deltaIndex = new int[rows];
		for (Component c : parent.getComponents()) {
			TableConstraints cs = getConstraints(c);
			if (cs != null) {
				if (cs.x >= gx) {
					cs.x += count;
				} else if (cs.x < gx && cs.x + cs.wx.length > gx) {
					deltaIndex[cs.y] = cs.x + cs.wx.length - gx;
				}
			}
		}

		return deltaIndex;
	}

	public boolean remove(Container parent, int gx, int gy, Point p) {
		if (gx >= 0 && gy == -1) {
			return removeColumn(parent, gx, p);
		} else if (gx == -1 && gy >= 0) {
			return removeRow(parent, gy, p);
		}

		return false;
	}

	public boolean removeColumn(Container parent, int gx, Point p) {
		Component left = parent.getComponentAt(p.x - 5, p.y);
		Component rigth = parent.getComponentAt(p.x + 5, p.y);

		TableConstraints constraintsLeft = getConstraints(left);
		TableConstraints constraintsRigth = getConstraints(rigth);

		if (gx > 0 && gx < columns) {
			if (constraintsLeft != null && constraintsRigth != null && constraintsLeft.y == constraintsRigth.y && constraintsLeft.wy.length == constraintsRigth.wy.length) {
				constraintsLeft.addWidth(constraintsRigth.wx, true);
				parent.remove(rigth);
				return true;
			}

			if (constraintsLeft == null && constraintsRigth != null) {
				constraintsRigth.addWidth(1, false);
				parent.remove(left);
				return true;
			}

			if (constraintsLeft != null && constraintsRigth == null) {
				constraintsLeft.addWidth(1, true);
				parent.remove(rigth);
				return true;
			}
		}

		if (gx == 0 && constraintsLeft.wx.length == columns) {
			parent.remove(rigth);
			return true;
		}

		if (gx == columns && constraintsRigth.wx.length == columns) {
			parent.remove(left);
			return true;
		}

		return false;
	}

	public boolean removeRow(Container parent, int gy, Point p) {
		Component top = parent.getComponentAt(p.x, p.y - 5);
		Component bottom = parent.getComponentAt(p.x, p.y + 5);

		TableConstraints constraintsTop = getConstraints(top);
		TableConstraints constraintsBottom = getConstraints(bottom);

		if (gy > 0 && gy < rows) {
			if (constraintsTop != null && constraintsBottom != null && constraintsTop.x == constraintsBottom.x && constraintsTop.wx.length == constraintsBottom.wx.length) {
				constraintsTop.addHeigth(constraintsBottom.wy, true);
				parent.remove(bottom);
				return true;
			}

			if (constraintsTop == null && constraintsBottom != null) {
				constraintsBottom.addHeigth(1, false);
				parent.remove(top);
				return true;
			}

			if (constraintsTop != null && constraintsBottom == null) {
				constraintsTop.addHeigth(1, true);
				parent.remove(bottom);
				return true;
			}
		}

		if (gy == 0 && constraintsBottom != null && constraintsBottom.wx.length == columns) {
			parent.remove(bottom);
			return true;
		}

		if (gy == rows && constraintsTop != null && constraintsTop.wx.length == columns) {
			parent.remove(top);
			return true;
		}

		return false;
	}

	public void removeEmptyRow(Container parent, int row) {
		if (row >= 0 && row < rowsStates.length && rowsStates[row] == CELL_CONTINUE) {
			for (Component comp : parent.getComponents()) {
				TableConstraints cs = map.get(comp);
				if (cs != null) {
					if (row > cs.y && row < cs.y + cs.wy.length) {
						double[] wy = cs.wy;
						// cs.wy.length--;
						cs.wy = new double[cs.wy.length - 1];

						int len = row - cs.y;
						System.arraycopy(wy, 0, cs.wy, 0, len);
						cs.wy[len - 1] += wy[len];
						System.arraycopy(wy, len + 1, cs.wy, len, cs.wy.length - len);
					} else if (row <= cs.y) {
						cs.y--;
					}
				}
			}
		}
	}

	public void removeEmptyColumn(Container parent, int column) {
		if (column >= 0 && column < columnsStates.length && columnsStates[column] == CELL_CONTINUE) {
			for (Component comp : parent.getComponents()) {
				TableConstraints cs = map.get(comp);
				if (cs != null) {
					if (column > cs.x && column < cs.x + cs.wx.length) {
						double[] wx = cs.wx;
						// cs.wx.length--;
						cs.wx = new double[cs.wx.length - 1];

						int len = column - cs.x;
						System.arraycopy(wx, 0, cs.wx, 0, len);
						cs.wx[len - 1] += wx[len];
						System.arraycopy(wx, len + 1, cs.wx, len, cs.wx.length - len);
					} else if (column <= cs.x) {
						cs.x--;
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 1000, 800);
		// f.setContentPane(new EditableContainer(new DocumentEditor()));

		TableLayout l = new TableLayout();
		l.isPrefferedX = false;
		l.isPrefferedY = true;
		l.alignmentX = TableLayout.CENTER;
		l.alignmentY = TableLayout.CENTER;
		f.getContentPane().setLayout(l);

		f.getContentPane().add(new JButton("0,0"), new TableConstraints(0, 0, 1, 1, 0.0, 0.0, null));
		f.getContentPane().add(new JButton("1,0"), new TableConstraints(1, 0, 1, 1, 0.0, 0.0, null));
		f.getContentPane().add(new JButton("2,0"), new TableConstraints(2, 0, 1, 1, 0.0, 0.0, null));

		f.getContentPane().add(new JButton("0,1"), new TableConstraints(0, 1, 1, 1, 0.0, 0.0, null));
		f.getContentPane().add(new JButton("1,1"), new TableConstraints(1, 1, 1, 1, 0.0, 0.0, null));
		f.getContentPane().add(new JButton("2,1"), new TableConstraints(2, 1, 1, 1, 0.0, 0.0, null));

		f.getContentPane().add(new JButton("0,2"), new TableConstraints(0, 2, 1, 1, 0.0, 0.0, null));
		f.getContentPane().add(new JButton("1,2"), new TableConstraints(1, 2, 1, 1, 0.0, 0.0, null));
		f.getContentPane().add(new JButton("2,2"), new TableConstraints(2, 2, 1, 1, 0.0, 0.0, null));

		f.setVisible(true);
	}
}
