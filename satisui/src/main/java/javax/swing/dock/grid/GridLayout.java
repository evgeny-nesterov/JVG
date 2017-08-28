package javax.swing.dock.grid;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Hashtable;

public class GridLayout implements LayoutManager2, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	static final int EMPIRICMULTIPLIER = 2;

	/**
	 * This field is no longer used to reserve arrays and keeped for backward compatibility. Previously, this was the maximum number of grid
	 * positions (both horizontal and vertical) that could be laid out by the grid bag layout. Current implementation doesn't impose any limits
	 * on the size of a grid.
	 */
	protected static final int MAXGRIDSIZE = 512;

	/**
	 * The smallest grid that can be laid out by the grid bag layout.
	 */
	protected static final int MINSIZE = 1;

	/**
	 * The preferred grid size that can be laid out by the grid bag layout.
	 */
	protected static final int PREFERREDSIZE = 2;

	/**
	 * This hashtable maintains the association between a component and its Grid constraints. The Keys in <code>comptable</code> are the
	 * components and the values are the instances of <code>GridConstraints</code>. * @serial
	 * 
	 * @see java.awt.GridConstraints
	 */
	protected Hashtable<Component, GridConstraints> comptable;

	/**
	 * This field holds a Grid constraints instance containing the default values, so if a component does not have Grid constraints associated
	 * with it, then the component will be assigned a copy of the <code>defaultConstraints</code>.
	 * 
	 * @serial
	 * @see #getConstraints(Component)
	 * @see #setConstraints(Component, GridConstraints)
	 * @see #lookupConstraints(Component)
	 */
	protected GridConstraints defaultConstraints;

	/**
	 * This field holds the layout information for the Grid. The information in this field is based on the most recent validation of the Grid. If
	 * <code>layoutInfo</code> is <code>null</code> this indicates that there are no components in the Grid or if there are components, they have
	 * not yet been validated.
	 * 
	 * @serial
	 * @see #getLayoutInfo(Container, int)
	 */
	protected GridLayoutInfo layoutInfo;

	/**
	 * This field holds the overrides to the column minimum width. If this field is non-<code>null</code> the values are applied to the Grid
	 * after all of the minimum columns widths have been calculated. If columnWidths has more elements than the number of columns, columns are
	 * added to the Grid to match the number of elements in columnWidth.
	 * 
	 * @serial
	 * @see #getLayoutDimensions()
	 */
	public int columnWidths[];

	/**
	 * This field holds the overrides to the row minimum heights. If this field is non-<code>null</code> the values are applied to the Grid after
	 * all of the minimum row heights have been calculated. If <code>rowHeights</code> has more elements than the number of rows, rowa are added
	 * to the Grid to match the number of elements in <code>rowHeights</code>.
	 * 
	 * @serial
	 * @see #getLayoutDimensions()
	 */
	public int rowHeights[];

	/**
	 * This field holds the overrides to the column weights. If this field is non-<code>null</code> the values are applied to the Grid after all
	 * of the columns weights have been calculated. If <code>columnWeights[i]</code> &gt; weight for column i, then column i is assigned the
	 * weight in <code>columnWeights[i]</code>. If <code>columnWeights</code> has more elements than the number of columns, the excess elements
	 * are ignored - they do not cause more columns to be created.
	 * 
	 * @serial
	 */
	public double columnWeights[];

	/**
	 * This field holds the overrides to the row weights. If this field is non-<code>null</code> the values are applied to the Grid after all of
	 * the rows weights have been calculated. If <code>rowWeights[i]</code> &gt; weight for row i, then row i is assigned the weight in
	 * <code>rowWeights[i]</code>. If <code>rowWeights</code> has more elements than the number of rows, the excess elements are ignored - they
	 * do not cause more rows to be created.
	 * 
	 * @serial
	 */
	public double rowWeights[];

	/**
	 * The component being positioned. This is set before calling into <code>adjustForGravity</code>.
	 */
	private Component componentAdjusting;

	/**
	 * Creates a grid bag layout manager.
	 */
	public GridLayout() {
		comptable = new Hashtable<Component, GridConstraints>();
		defaultConstraints = new GridConstraints();
	}

	/**
	 * Sets the constraints for the specified component in this layout.
	 * 
	 * @param comp
	 *            the component to be modified
	 * @param constraints
	 *            the constraints to be applied
	 */
	public void setConstraints(Component comp, GridConstraints constraints) {
		comptable.put(comp, (GridConstraints) constraints.clone());
	}

	/**
	 * Gets the constraints for the specified component. A copy of the actual <code>GridConstraints</code> object is returned.
	 * 
	 * @param comp
	 *            the component to be queried
	 * @return the constraint for the specified component in this grid bag layout; a copy of the actual constraint object is returned
	 */
	public GridConstraints getConstraints(Component comp) {
		GridConstraints constraints = comptable.get(comp);
		if (constraints == null) {
			setConstraints(comp, defaultConstraints);
			constraints = comptable.get(comp);
		}
		return (GridConstraints) constraints.clone();
	}

	/**
	 * Retrieves the constraints for the specified component. The return value is not a copy, but is the actual <code>GridConstraints</code>
	 * object used by the layout mechanism.
	 * <p>
	 * If <code>comp</code> is not in the <code>GridLayout</code>, a set of default <code>GridConstraints</code> are returned. A
	 * <code>comp</code> value of <code>null</code> is invalid and returns <code>null</code>.
	 * 
	 * @param comp
	 *            the component to be queried
	 * @return the contraints for the specified component
	 */
	protected GridConstraints lookupConstraints(Component comp) {
		GridConstraints constraints = comptable.get(comp);
		if (constraints == null) {
			setConstraints(comp, defaultConstraints);
			constraints = comptable.get(comp);
		}
		return constraints;
	}

	/**
	 * Removes the constraints for the specified component in this layout
	 * 
	 * @param comp
	 *            the component to be modified
	 */
	private void removeConstraints(Component comp) {
		comptable.remove(comp);
	}

	/**
	 * Determines the origin of the layout area, in the graphics coordinate space of the target container. This value represents the pixel
	 * coordinates of the top-left corner of the layout area regardless of the <code>ComponentOrientation</code> value of the container. This is
	 * distinct from the grid origin given by the cell coordinates (0,0). Most applications do not call this method directly.
	 * 
	 * @return the graphics origin of the cell in the top-left corner of the layout grid
	 * @see java.awt.ComponentOrientation
	 * @since JDK1.1
	 */
	public Point getLayoutOrigin() {
		Point origin = new Point(0, 0);
		if (layoutInfo != null) {
			origin.x = layoutInfo.startx;
			origin.y = layoutInfo.starty;
		}
		return origin;
	}

	/**
	 * Determines column widths and row heights for the layout grid.
	 * <p>
	 * Most applications do not call this method directly.
	 * 
	 * @return an array of two arrays, containing the widths of the layout columns and the heights of the layout rows
	 * @since JDK1.1
	 */
	public int[][] getLayoutDimensions() {
		if (layoutInfo == null)
			return new int[2][0];

		int dim[][] = new int[2][];
		dim[0] = new int[layoutInfo.width];
		dim[1] = new int[layoutInfo.height];

		System.arraycopy(layoutInfo.minWidth, 0, dim[0], 0, layoutInfo.width);
		System.arraycopy(layoutInfo.minHeight, 0, dim[1], 0, layoutInfo.height);

		return dim;
	}

	/**
	 * Determines the weights of the layout grid's columns and rows. Weights are used to calculate how much a given column or row stretches
	 * beyond its preferred size, if the layout has extra room to fill.
	 * <p>
	 * Most applications do not call this method directly.
	 * 
	 * @return an array of two arrays, representing the horizontal weights of the layout columns and the vertical weights of the layout rows
	 * @since JDK1.1
	 */
	public double[][] getLayoutWeights() {
		if (layoutInfo == null)
			return new double[2][0];

		double weights[][] = new double[2][];
		weights[0] = new double[layoutInfo.width];
		weights[1] = new double[layoutInfo.height];

		System.arraycopy(layoutInfo.weightX, 0, weights[0], 0, layoutInfo.width);
		System.arraycopy(layoutInfo.weightY, 0, weights[1], 0, layoutInfo.height);

		return weights;
	}

	/**
	 * Determines which cell in the layout grid contains the point specified by <code>(x,&nbsp;y)</code>. Each cell is identified by its column
	 * index (ranging from 0 to the number of columns minus 1) and its row index (ranging from 0 to the number of rows minus 1).
	 * <p>
	 * If the <code>(x,&nbsp;y)</code> point lies outside the grid, the following rules are used. The column index is returned as zero if
	 * <code>x</code> lies to the left of the layout for a left-to-right container or to the right of the layout for a right-to-left container.
	 * The column index is returned as the number of columns if <code>x</code> lies to the right of the layout in a left-to-right container or to
	 * the left in a right-to-left container. The row index is returned as zero if <code>y</code> lies above the layout, and as the number of
	 * rows if <code>y</code> lies below the layout. The orientation of a container is determined by its <code>ComponentOrientation</code>
	 * property.
	 * 
	 * @param x
	 *            the <i>x</i> coordinate of a point
	 * @param y
	 *            the <i>y</i> coordinate of a point
	 * @return an ordered pair of indexes that indicate which cell in the layout grid contains the point (<i>x</i>,&nbsp;<i>y</i>).
	 * @see java.awt.ComponentOrientation
	 * @since JDK1.1
	 */
	public Point location(int x, int y) {
		Point loc = new Point(0, 0);
		int i, d;

		if (layoutInfo == null)
			return loc;

		d = layoutInfo.startx;
		if (!rightToLeft) {
			for (i = 0; i < layoutInfo.width; i++) {
				d += layoutInfo.minWidth[i];
				if (d > x)
					break;
			}
		} else {
			for (i = layoutInfo.width - 1; i >= 0; i--) {
				if (d > x)
					break;
				d += layoutInfo.minWidth[i];
			}
			i++;
		}
		loc.x = i;

		d = layoutInfo.starty;
		for (i = 0; i < layoutInfo.height; i++) {
			d += layoutInfo.minHeight[i];
			if (d > y)
				break;
		}
		loc.y = i;

		return loc;
	}

	/**
	 * Has no effect, since this layout manager does not use a per-component string.
	 */
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	/**
	 * Adds the specified component to the layout, using the specified <code>constraints</code> object. Note that constraints are mutable and
	 * are, therefore, cloned when cached.
	 * 
	 * @param comp
	 *            the component to be added
	 * @param constraints
	 *            an object that determines how the component is added to the layout
	 * @exception IllegalArgumentException
	 *                if <code>constraints</code> is not a <code>GridConstraint</code>
	 */
	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if (constraints instanceof GridConstraints) {
			setConstraints(comp, (GridConstraints) constraints);
		} else if (constraints != null) {
			throw new IllegalArgumentException("cannot add to layout: constraints must be a GridConstraint");
		}
	}

	/**
	 * Removes the specified component from this layout.
	 * <p>
	 * Most applications do not call this method directly.
	 * 
	 * @param comp
	 *            the component to be removed.
	 * @see java.awt.Container#remove(java.awt.Component)
	 * @see java.awt.Container#removeAll()
	 */
	@Override
	public void removeLayoutComponent(Component comp) {
		removeConstraints(comp);
	}

	/**
	 * Determines the preferred size of the <code>parent</code> container using this grid bag layout.
	 * <p>
	 * Most applications do not call this method directly.
	 * 
	 * @param parent
	 *            the container in which to do the layout
	 * @see java.awt.Container#getPreferredSize
	 * @return the preferred size of the <code>parent</code> container
	 */
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		GridLayoutInfo info = getLayoutInfo(parent, PREFERREDSIZE);
		return getMinSize(parent, info);
	}

	/**
	 * Determines the minimum size of the <code>parent</code> container using this grid bag layout.
	 * <p>
	 * Most applications do not call this method directly.
	 * 
	 * @param parent
	 *            the container in which to do the layout
	 * @see java.awt.Container#doLayout
	 * @return the minimum size of the <code>parent</code> container
	 */
	@Override
	public Dimension minimumLayoutSize(Container parent) {
		GridLayoutInfo info = getLayoutInfo(parent, MINSIZE);
		return getMinSize(parent, info);
	}

	/**
	 * Returns the maximum dimensions for this layout given the components in the specified target container.
	 * 
	 * @param target
	 *            the container which needs to be laid out
	 * @see Container
	 * @see #minimumLayoutSize(Container)
	 * @see #preferredLayoutSize(Container)
	 * @return the maximum dimensions for this layout
	 */
	@Override
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Returns the alignment along the x axis. This specifies how the component would like to be aligned relative to other components. The value
	 * should be a number between 0 and 1 where 0 represents alignment along the origin, 1 is aligned the furthest away from the origin, 0.5 is
	 * centered, etc.
	 * <p>
	 * 
	 * @return the value <code>0.5f</code> to indicate centered
	 */
	@Override
	public float getLayoutAlignmentX(Container parent) {
		return 0.5f;
	}

	/**
	 * Returns the alignment along the y axis. This specifies how the component would like to be aligned relative to other components. The value
	 * should be a number between 0 and 1 where 0 represents alignment along the origin, 1 is aligned the furthest away from the origin, 0.5 is
	 * centered, etc.
	 * <p>
	 * 
	 * @return the value <code>0.5f</code> to indicate centered
	 */
	@Override
	public float getLayoutAlignmentY(Container parent) {
		return 0.5f;
	}

	/**
	 * Invalidates the layout, indicating that if the layout manager has cached information it should be discarded.
	 */
	@Override
	public void invalidateLayout(Container target) {
	}

	/**
	 * Lays out the specified container using this grid bag layout. This method reshapes components in the specified container in order to
	 * satisfy the contraints of this <code>GridLayout</code> object.
	 * <p>
	 * Most applications do not call this method directly.
	 * 
	 * @param parent
	 *            the container in which to do the layout
	 * @see java.awt.Container
	 * @see java.awt.Container#doLayout
	 */
	@Override
	public void layoutContainer(Container parent) {
		arrangeGrid(parent);
	}

	/**
	 * Returns a string representation of this grid bag layout's values.
	 * 
	 * @return a string representation of this grid bag layout.
	 */
	@Override
	public String toString() {
		return getClass().getName();
	}

	/**
	 * Fills in an instance of <code>GridLayoutInfo</code> for the current set of managed children. This requires three passes through the set of
	 * children:
	 * <ol>
	 * <li>Figure out the dimensions of the layout grid.
	 * <li>Determine which cells the components occupy.
	 * <li>Distribute the weights and min sizes amoung the rows/columns.
	 * </ol>
	 * This also caches the minsizes for all the children when they are first encountered (so subsequent loops don't need to ask again).
	 * <p>
	 * This method should only be used internally by <code>GridLayout</code>.
	 * 
	 * @param parent
	 *            the layout container
	 * @param sizeflag
	 *            either <code>PREFERREDSIZE</code> or <code>MINSIZE</code>
	 * @return the <code>GridLayoutInfo</code> for the set of children
	 * @since 1.4
	 */
	protected GridLayoutInfo getLayoutInfo(Container parent, int sizeflag) {
		return GetLayoutInfo(parent, sizeflag);
	}

	/*
	 * Calculate maximum array sizes to allocate arrays without ensureCapacity we may use preCalculated sizes in whole class because of upper
	 * estimation of maximumArrayXIndex and maximumArrayYIndex.
	 */

	private long[] preInitMaximumArraySizes(Container parent) {
		Component components[] = parent.getComponents();
		Component comp;
		GridConstraints constraints;
		int curX, curY;
		int curWidth, curHeight;
		int preMaximumArrayXIndex = 0;
		int preMaximumArrayYIndex = 0;
		long[] returnArray = new long[2];

		for (int compId = 0; compId < components.length; compId++) {
			comp = components[compId];
			if (!comp.isVisible()) {
				continue;
			}

			constraints = lookupConstraints(comp);
			curX = constraints.gridx;
			curY = constraints.gridy;
			curWidth = constraints.gridwidth;
			curHeight = constraints.gridheight;

			// -1==RELATIVE, means that column|row equals to previously added component,
			// since each next Component with gridx|gridy == RELATIVE starts from
			// previous position, so we should start from previous component which
			// already used in maximumArray[X|Y]Index calculation. We could just increase
			// maximum by 1 to handle situation when component with gridx=-1 was added.
			if (curX < 0) {
				curX = ++preMaximumArrayYIndex;
			}
			if (curY < 0) {
				curY = ++preMaximumArrayXIndex;
			}
			// gridwidth|gridheight may be equal to RELATIVE (-1) or REMAINDER (0)
			// in any case using 1 instead of 0 or -1 should be sufficient to for
			// correct maximumArraySizes calculation
			if (curWidth <= 0) {
				curWidth = 1;
			}
			if (curHeight <= 0) {
				curHeight = 1;
			}

			preMaximumArrayXIndex = Math.max(curY + curHeight, preMaximumArrayXIndex);
			preMaximumArrayYIndex = Math.max(curX + curWidth, preMaximumArrayYIndex);
		} // for (components) loop
			// Must specify index++ to allocate well-working arrays.
			/*
			 * fix for 4623196. now return long array instead of Point
			 */
		returnArray[0] = preMaximumArrayXIndex;
		returnArray[1] = preMaximumArrayYIndex;
		return returnArray;
	} // PreInitMaximumSizes

	/**
	 * This method is obsolete and supplied for backwards compatability only; new code should call
	 * {@link #getLayoutInfo(java.awt.Container, int) getLayoutInfo} instead. This method is the same as <code>getLayoutInfo</code>; refer to
	 * <code>getLayoutInfo</code> for details on parameters and return value.
	 */
	protected GridLayoutInfo GetLayoutInfo(Container parent, int sizeflag) {
		synchronized (parent.getTreeLock()) {
			GridLayoutInfo r;
			Component comp;
			GridConstraints constraints;
			Dimension d;
			Component components[] = parent.getComponents();
			// Code below will address index curX+curWidth in the case of yMaxArray, weightY
			// ( respectively curY+curHeight for xMaxArray, weightX ) where
			// curX in 0 to preInitMaximumArraySizes.y
			// Thus, the maximum index that could
			// be calculated in the following code is curX+curX.
			// EmpericMultier equals 2 because of this.

			int layoutWidth, layoutHeight;
			int[] xMaxArray;
			int[] yMaxArray;
			int compindex, i, k, px, py, pixels_diff, nextSize;
			int curX = 0; // constraints.gridx
			int curY = 0; // constraints.gridy
			int curWidth = 1; // constraints.gridwidth
			int curHeight = 1; // constraints.gridheight
			int curRow, curCol;
			double weight_diff, weight;
			int maximumArrayXIndex = 0;
			int maximumArrayYIndex = 0;
			int anchor;

			/*
			 * Pass #1 Figure out the dimensions of the layout grid (use a value of 1 for zero or negative widths and heights).
			 */

			layoutWidth = layoutHeight = 0;
			curRow = curCol = -1;
			long[] arraySizes = preInitMaximumArraySizes(parent);

			/*
			 * fix for 4623196. If user try to create a very big grid we can get NegativeArraySizeException because of integer value overflow
			 * (EMPIRICMULTIPLIER*gridSize might be more then Integer.MAX_VALUE). We need to detect this situation and try to create a grid with
			 * Integer.MAX_VALUE size instead.
			 */
			maximumArrayXIndex = (EMPIRICMULTIPLIER * arraySizes[0] > Integer.MAX_VALUE) ? Integer.MAX_VALUE : EMPIRICMULTIPLIER * (int) arraySizes[0];
			maximumArrayYIndex = (EMPIRICMULTIPLIER * arraySizes[1] > Integer.MAX_VALUE) ? Integer.MAX_VALUE : EMPIRICMULTIPLIER * (int) arraySizes[1];

			if (rowHeights != null) {
				maximumArrayXIndex = Math.max(maximumArrayXIndex, rowHeights.length);
			}
			if (columnWidths != null) {
				maximumArrayYIndex = Math.max(maximumArrayYIndex, columnWidths.length);
			}

			xMaxArray = new int[maximumArrayXIndex];
			yMaxArray = new int[maximumArrayYIndex];

			boolean hasBaseline = false;
			for (compindex = 0; compindex < components.length; compindex++) {
				comp = components[compindex];
				if (!comp.isVisible())
					continue;
				constraints = lookupConstraints(comp);

				curX = constraints.gridx;
				curY = constraints.gridy;
				curWidth = constraints.gridwidth;
				if (curWidth <= 0)
					curWidth = 1;
				curHeight = constraints.gridheight;
				if (curHeight <= 0)
					curHeight = 1;

				/* If x or y is negative, then use relative positioning: */
				if (curX < 0 && curY < 0) {
					if (curRow >= 0)
						curY = curRow;
					else if (curCol >= 0)
						curX = curCol;
					else
						curY = 0;
				}
				if (curX < 0) {
					px = 0;
					for (i = curY; i < (curY + curHeight); i++) {
						px = Math.max(px, xMaxArray[i]);
					}

					curX = px - curX - 1;
					if (curX < 0)
						curX = 0;
				} else if (curY < 0) {
					py = 0;
					for (i = curX; i < (curX + curWidth); i++) {
						py = Math.max(py, yMaxArray[i]);
					}
					curY = py - curY - 1;
					if (curY < 0)
						curY = 0;
				}

				/*
				 * Adjust the grid width and height fix for 5005945: unneccessary loops removed
				 */
				px = curX + curWidth;
				if (layoutWidth < px) {
					layoutWidth = px;
				}
				py = curY + curHeight;
				if (layoutHeight < py) {
					layoutHeight = py;
				}

				/* Adjust xMaxArray and yMaxArray */
				for (i = curX; i < (curX + curWidth); i++) {
					yMaxArray[i] = py;
				}
				for (i = curY; i < (curY + curHeight); i++) {
					xMaxArray[i] = px;
				}

				/* Cache the current slave's size. */
				if (sizeflag == PREFERREDSIZE)
					d = comp.getPreferredSize();
				else
					d = comp.getMinimumSize();
				constraints.minWidth = d.width;
				constraints.minHeight = d.height;
				if (calculateBaseline(comp, constraints, d)) {
					hasBaseline = true;
				}

				/*
				 * Zero width and height must mean that this is the last item (or else something is wrong).
				 */
				if (constraints.gridheight == 0 && constraints.gridwidth == 0)
					curRow = curCol = -1;

				/* Zero width starts a new row */
				if (constraints.gridheight == 0 && curRow < 0)
					curCol = curX + curWidth;

				/* Zero height starts a new column */
				else if (constraints.gridwidth == 0 && curCol < 0)
					curRow = curY + curHeight;
			} // for (components) loop

			/*
			 * Apply minimum row/column dimensions
			 */
			if (columnWidths != null && layoutWidth < columnWidths.length)
				layoutWidth = columnWidths.length;
			if (rowHeights != null && layoutHeight < rowHeights.length)
				layoutHeight = rowHeights.length;

			r = new GridLayoutInfo(layoutWidth, layoutHeight);

			/*
			 * Pass #2 Negative values for gridX are filled in with the current x value. Negative values for gridY are filled in with the current
			 * y value. Negative or zero values for gridWidth and gridHeight end the current row or column, respectively.
			 */

			curRow = curCol = -1;

			Arrays.fill(xMaxArray, 0);
			Arrays.fill(yMaxArray, 0);

			int[] maxAscent = null;
			int[] maxDescent = null;
			short[] baselineType = null;

			if (hasBaseline) {
				r.maxAscent = maxAscent = new int[layoutHeight];
				r.maxDescent = maxDescent = new int[layoutHeight];
				r.baselineType = baselineType = new short[layoutHeight];
				r.hasBaseline = true;
			}

			for (compindex = 0; compindex < components.length; compindex++) {
				comp = components[compindex];
				if (!comp.isVisible())
					continue;
				constraints = lookupConstraints(comp);

				curX = constraints.gridx;
				curY = constraints.gridy;
				curWidth = constraints.gridwidth;
				curHeight = constraints.gridheight;

				/* If x or y is negative, then use relative positioning: */
				if (curX < 0 && curY < 0) {
					if (curRow >= 0)
						curY = curRow;
					else if (curCol >= 0)
						curX = curCol;
					else
						curY = 0;
				}

				if (curX < 0) {
					if (curHeight <= 0) {
						curHeight += r.height - curY;
						if (curHeight < 1)
							curHeight = 1;
					}

					px = 0;
					for (i = curY; i < (curY + curHeight); i++)
						px = Math.max(px, xMaxArray[i]);

					curX = px - curX - 1;
					if (curX < 0)
						curX = 0;
				} else if (curY < 0) {
					if (curWidth <= 0) {
						curWidth += r.width - curX;
						if (curWidth < 1)
							curWidth = 1;
					}

					py = 0;
					for (i = curX; i < (curX + curWidth); i++) {
						py = Math.max(py, yMaxArray[i]);
					}

					curY = py - curY - 1;
					if (curY < 0)
						curY = 0;
				}

				if (curWidth <= 0) {
					curWidth += r.width - curX;
					if (curWidth < 1)
						curWidth = 1;
				}

				if (curHeight <= 0) {
					curHeight += r.height - curY;
					if (curHeight < 1)
						curHeight = 1;
				}

				px = curX + curWidth;
				py = curY + curHeight;

				for (i = curX; i < (curX + curWidth); i++) {
					yMaxArray[i] = py;
				}
				for (i = curY; i < (curY + curHeight); i++) {
					xMaxArray[i] = px;
				}

				/* Make negative sizes start a new row/column */
				if (constraints.gridheight == 0 && constraints.gridwidth == 0)
					curRow = curCol = -1;
				if (constraints.gridheight == 0 && curRow < 0)
					curCol = curX + curWidth;
				else if (constraints.gridwidth == 0 && curCol < 0)
					curRow = curY + curHeight;

				/* Assign the new values to the Grid slave */
				constraints.tempX = curX;
				constraints.tempY = curY;
				constraints.tempWidth = curWidth;
				constraints.tempHeight = curHeight;

				anchor = constraints.anchor;
				if (hasBaseline) {
					switch (anchor) {
						case GridConstraints.BASELINE:
						case GridConstraints.BASELINE_LEADING:
						case GridConstraints.BASELINE_TRAILING:
							if (constraints.ascent >= 0) {
								if (curHeight == 1) {
									maxAscent[curY] = Math.max(maxAscent[curY], constraints.ascent);
									maxDescent[curY] = Math.max(maxDescent[curY], constraints.descent);
								} else {
									if (constraints.baselineResizeBehavior == Component.BaselineResizeBehavior.CONSTANT_DESCENT) {
										maxDescent[curY + curHeight - 1] = Math.max(maxDescent[curY + curHeight - 1], constraints.descent);
									} else {
										maxAscent[curY] = Math.max(maxAscent[curY], constraints.ascent);
									}
								}
								if (constraints.baselineResizeBehavior == Component.BaselineResizeBehavior.CONSTANT_DESCENT) {
									baselineType[curY + curHeight - 1] |= (1 << constraints.baselineResizeBehavior.ordinal());
								} else {
									baselineType[curY] |= (1 << constraints.baselineResizeBehavior.ordinal());
								}
							}
							break;
						case GridConstraints.ABOVE_BASELINE:
						case GridConstraints.ABOVE_BASELINE_LEADING:
						case GridConstraints.ABOVE_BASELINE_TRAILING:
							// Component positioned above the baseline.
							// To make the bottom edge of the component aligned
							// with the baseline the bottom inset is
							// added to the descent, the rest to the ascent.
							pixels_diff = constraints.minHeight + constraints.insets.top + constraints.ipady;
							maxAscent[curY] = Math.max(maxAscent[curY], pixels_diff);
							maxDescent[curY] = Math.max(maxDescent[curY], constraints.insets.bottom);
							break;
						case GridConstraints.BELOW_BASELINE:
						case GridConstraints.BELOW_BASELINE_LEADING:
						case GridConstraints.BELOW_BASELINE_TRAILING:
							// Component positioned below the baseline.
							// To make the top edge of the component aligned
							// with the baseline the top inset is
							// added to the ascent, the rest to the descent.
							pixels_diff = constraints.minHeight + constraints.insets.bottom + constraints.ipady;
							maxDescent[curY] = Math.max(maxDescent[curY], pixels_diff);
							maxAscent[curY] = Math.max(maxAscent[curY], constraints.insets.top);
							break;
					}
				}
			}

			r.weightX = new double[maximumArrayYIndex];
			r.weightY = new double[maximumArrayXIndex];
			r.minWidth = new int[maximumArrayYIndex];
			r.minHeight = new int[maximumArrayXIndex];

			/*
			 * Apply minimum row/column dimensions and weights
			 */
			if (columnWidths != null)
				System.arraycopy(columnWidths, 0, r.minWidth, 0, columnWidths.length);
			if (rowHeights != null)
				System.arraycopy(rowHeights, 0, r.minHeight, 0, rowHeights.length);
			if (columnWeights != null)
				System.arraycopy(columnWeights, 0, r.weightX, 0, Math.min(r.weightX.length, columnWeights.length));
			if (rowWeights != null)
				System.arraycopy(rowWeights, 0, r.weightY, 0, Math.min(r.weightY.length, rowWeights.length));

			/*
			 * Pass #3 Distribute the minimun widths and weights:
			 */

			nextSize = Integer.MAX_VALUE;

			for (i = 1; i != Integer.MAX_VALUE; i = nextSize, nextSize = Integer.MAX_VALUE) {
				for (compindex = 0; compindex < components.length; compindex++) {
					comp = components[compindex];
					if (!comp.isVisible())
						continue;
					constraints = lookupConstraints(comp);

					if (constraints.tempWidth == i) {
						px = constraints.tempX + constraints.tempWidth; /* right column */

						/*
						 * Figure out if we should use this slave\'s weight. If the weight is less than the total weight spanned by the width of
						 * the cell, then discard the weight. Otherwise split the difference according to the existing weights.
						 */

						weight_diff = constraints.weightx;
						for (k = constraints.tempX; k < px; k++)
							weight_diff -= r.weightX[k];
						if (weight_diff > 0.0) {
							weight = 0.0;
							for (k = constraints.tempX; k < px; k++)
								weight += r.weightX[k];
							for (k = constraints.tempX; weight > 0.0 && k < px; k++) {
								double wt = r.weightX[k];
								double dx = (wt * weight_diff) / weight;
								r.weightX[k] += dx;
								weight_diff -= dx;
								weight -= wt;
							}
							/* Assign the remainder to the rightmost cell */
							r.weightX[px - 1] += weight_diff;
						}

						/*
						 * Calculate the minWidth array values. First, figure out how wide the current slave needs to be. Then, see if it will
						 * fit within the current minWidth values. If it will not fit, add the difference according to the weightX array.
						 */

						pixels_diff = constraints.minWidth + constraints.ipadx + constraints.insets.left + constraints.insets.right;

						for (k = constraints.tempX; k < px; k++)
							pixels_diff -= r.minWidth[k];
						if (pixels_diff > 0) {
							weight = 0.0;
							for (k = constraints.tempX; k < px; k++)
								weight += r.weightX[k];
							for (k = constraints.tempX; weight > 0.0 && k < px; k++) {
								double wt = r.weightX[k];
								int dx = (int) ((wt * (pixels_diff)) / weight);
								r.minWidth[k] += dx;
								pixels_diff -= dx;
								weight -= wt;
							}
							/* Any leftovers go into the rightmost cell */
							r.minWidth[px - 1] += pixels_diff;
						}
					} else if (constraints.tempWidth > i && constraints.tempWidth < nextSize)
						nextSize = constraints.tempWidth;

					if (constraints.tempHeight == i) {
						py = constraints.tempY + constraints.tempHeight; /* bottom row */

						/*
						 * Figure out if we should use this slave's weight. If the weight is less than the total weight spanned by the height of
						 * the cell, then discard the weight. Otherwise split it the difference according to the existing weights.
						 */

						weight_diff = constraints.weighty;
						for (k = constraints.tempY; k < py; k++)
							weight_diff -= r.weightY[k];
						if (weight_diff > 0.0) {
							weight = 0.0;
							for (k = constraints.tempY; k < py; k++)
								weight += r.weightY[k];
							for (k = constraints.tempY; weight > 0.0 && k < py; k++) {
								double wt = r.weightY[k];
								double dy = (wt * weight_diff) / weight;
								r.weightY[k] += dy;
								weight_diff -= dy;
								weight -= wt;
							}
							/* Assign the remainder to the bottom cell */
							r.weightY[py - 1] += weight_diff;
						}

						/*
						 * Calculate the minHeight array values. First, figure out how tall the current slave needs to be. Then, see if it will
						 * fit within the current minHeight values. If it will not fit, add the difference according to the weightY array.
						 */

						pixels_diff = -1;
						if (hasBaseline) {
							switch (constraints.anchor) {
								case GridConstraints.BASELINE:
								case GridConstraints.BASELINE_LEADING:
								case GridConstraints.BASELINE_TRAILING:
									if (constraints.ascent >= 0) {
										if (constraints.tempHeight == 1) {
											pixels_diff = maxAscent[constraints.tempY] + maxDescent[constraints.tempY];
										} else if (constraints.baselineResizeBehavior != Component.BaselineResizeBehavior.CONSTANT_DESCENT) {
											pixels_diff = maxAscent[constraints.tempY] + constraints.descent;
										} else {
											pixels_diff = constraints.ascent + maxDescent[constraints.tempY + constraints.tempHeight - 1];
										}
									}
									break;
								case GridConstraints.ABOVE_BASELINE:
								case GridConstraints.ABOVE_BASELINE_LEADING:
								case GridConstraints.ABOVE_BASELINE_TRAILING:
									pixels_diff = constraints.insets.top + constraints.minHeight + constraints.ipady + maxDescent[constraints.tempY];
									break;
								case GridConstraints.BELOW_BASELINE:
								case GridConstraints.BELOW_BASELINE_LEADING:
								case GridConstraints.BELOW_BASELINE_TRAILING:
									pixels_diff = maxAscent[constraints.tempY] + constraints.minHeight + constraints.insets.bottom + constraints.ipady;
									break;
							}
						}
						if (pixels_diff == -1) {
							pixels_diff = constraints.minHeight + constraints.ipady + constraints.insets.top + constraints.insets.bottom;
						}
						for (k = constraints.tempY; k < py; k++)
							pixels_diff -= r.minHeight[k];
						if (pixels_diff > 0) {
							weight = 0.0;
							for (k = constraints.tempY; k < py; k++)
								weight += r.weightY[k];
							for (k = constraints.tempY; weight > 0.0 && k < py; k++) {
								double wt = r.weightY[k];
								int dy = (int) ((wt * (pixels_diff)) / weight);
								r.minHeight[k] += dy;
								pixels_diff -= dy;
								weight -= wt;
							}
							/* Any leftovers go into the bottom cell */
							r.minHeight[py - 1] += pixels_diff;
						}
					} else if (constraints.tempHeight > i && constraints.tempHeight < nextSize)
						nextSize = constraints.tempHeight;
				}
			}
			return r;
		}
	} // getLayoutInfo()

	/**
	 * Calculate the baseline for the specified component. If {@code c} is positioned along it's baseline, the baseline is obtained and the
	 * {@code constraints} ascent, descent and baseline resize behavior are set from the component; and true is returned. Otherwise false is
	 * returned.
	 */
	private boolean calculateBaseline(Component c, GridConstraints constraints, Dimension size) {
		int anchor = constraints.anchor;
		if (anchor == GridConstraints.BASELINE || anchor == GridConstraints.BASELINE_LEADING || anchor == GridConstraints.BASELINE_TRAILING) {
			// Apply the padding to the component, then ask for the baseline.
			int w = size.width + constraints.ipadx;
			int h = size.height + constraints.ipady;
			constraints.ascent = c.getBaseline(w, h);
			if (constraints.ascent >= 0) {
				// Component has a baseline
				int baseline = constraints.ascent;
				// Adjust the ascent and descent to include the insets.
				constraints.descent = h - constraints.ascent + constraints.insets.bottom;
				constraints.ascent += constraints.insets.top;
				constraints.baselineResizeBehavior = c.getBaselineResizeBehavior();
				constraints.centerPadding = 0;
				if (constraints.baselineResizeBehavior == Component.BaselineResizeBehavior.CENTER_OFFSET) {
					// Component has a baseline resize behavior of
					// CENTER_OFFSET, calculate centerPadding and
					// centerOffset (see the description of
					// CENTER_OFFSET in the enum for detais on this
					// algorithm).
					int nextBaseline = c.getBaseline(w, h + 1);
					constraints.centerOffset = baseline - h / 2;
					if (h % 2 == 0) {
						if (baseline != nextBaseline) {
							constraints.centerPadding = 1;
						}
					} else if (baseline == nextBaseline) {
						constraints.centerOffset--;
						constraints.centerPadding = 1;
					}
				}
			}
			return true;
		} else {
			constraints.ascent = -1;
			return false;
		}
	}

	/**
	 * Adjusts the x, y, width, and height fields to the correct values depending on the constraint geometry and pads. This method should only be
	 * used internally by <code>GridLayout</code>.
	 * 
	 * @param constraints
	 *            the constraints to be applied
	 * @param r
	 *            the <code>Rectangle</code> to be adjusted
	 * @since 1.4
	 */
	protected void adjustForGravity(GridConstraints constraints, Rectangle r) {
		AdjustForGravity(constraints, r);
	}

	/**
	 * This method is obsolete and supplied for backwards compatability only; new code should call
	 * {@link #adjustForGravity(java.awt.GridConstraints, java.awt.Rectangle) adjustForGravity} instead. This method is the same as
	 * <code>adjustForGravity</code>; refer to <code>adjustForGravity</code> for details on parameters.
	 */
	protected void AdjustForGravity(GridConstraints constraints, Rectangle r) {
		int diffx, diffy;
		int cellY = r.y;
		int cellHeight = r.height;

		if (!rightToLeft) {
			r.x += constraints.insets.left;
		} else {
			r.x -= r.width - constraints.insets.right;
		}
		r.width -= (constraints.insets.left + constraints.insets.right);
		r.y += constraints.insets.top;
		r.height -= (constraints.insets.top + constraints.insets.bottom);

		diffx = 0;
		if ((constraints.fill != GridConstraints.HORIZONTAL && constraints.fill != GridConstraints.BOTH) && (r.width > (constraints.minWidth + constraints.ipadx))) {
			diffx = r.width - (constraints.minWidth + constraints.ipadx);
			r.width = constraints.minWidth + constraints.ipadx;
		}

		diffy = 0;
		if ((constraints.fill != GridConstraints.VERTICAL && constraints.fill != GridConstraints.BOTH) && (r.height > (constraints.minHeight + constraints.ipady))) {
			diffy = r.height - (constraints.minHeight + constraints.ipady);
			r.height = constraints.minHeight + constraints.ipady;
		}

		switch (constraints.anchor) {
			case GridConstraints.BASELINE:
				r.x += diffx / 2;
				alignOnBaseline(constraints, r, cellY, cellHeight);
				break;
			case GridConstraints.BASELINE_LEADING:
				if (rightToLeft) {
					r.x += diffx;
				}
				alignOnBaseline(constraints, r, cellY, cellHeight);
				break;
			case GridConstraints.BASELINE_TRAILING:
				if (!rightToLeft) {
					r.x += diffx;
				}
				alignOnBaseline(constraints, r, cellY, cellHeight);
				break;
			case GridConstraints.ABOVE_BASELINE:
				r.x += diffx / 2;
				alignAboveBaseline(constraints, r, cellY, cellHeight);
				break;
			case GridConstraints.ABOVE_BASELINE_LEADING:
				if (rightToLeft) {
					r.x += diffx;
				}
				alignAboveBaseline(constraints, r, cellY, cellHeight);
				break;
			case GridConstraints.ABOVE_BASELINE_TRAILING:
				if (!rightToLeft) {
					r.x += diffx;
				}
				alignAboveBaseline(constraints, r, cellY, cellHeight);
				break;
			case GridConstraints.BELOW_BASELINE:
				r.x += diffx / 2;
				alignBelowBaseline(constraints, r, cellY, cellHeight);
				break;
			case GridConstraints.BELOW_BASELINE_LEADING:
				if (rightToLeft) {
					r.x += diffx;
				}
				alignBelowBaseline(constraints, r, cellY, cellHeight);
				break;
			case GridConstraints.BELOW_BASELINE_TRAILING:
				if (!rightToLeft) {
					r.x += diffx;
				}
				alignBelowBaseline(constraints, r, cellY, cellHeight);
				break;
			case GridConstraints.CENTER:
				r.x += diffx / 2;
				r.y += diffy / 2;
				break;
			case GridConstraints.PAGE_START:
			case GridConstraints.NORTH:
				r.x += diffx / 2;
				break;
			case GridConstraints.NORTHEAST:
				r.x += diffx;
				break;
			case GridConstraints.EAST:
				r.x += diffx;
				r.y += diffy / 2;
				break;
			case GridConstraints.SOUTHEAST:
				r.x += diffx;
				r.y += diffy;
				break;
			case GridConstraints.PAGE_END:
			case GridConstraints.SOUTH:
				r.x += diffx / 2;
				r.y += diffy;
				break;
			case GridConstraints.SOUTHWEST:
				r.y += diffy;
				break;
			case GridConstraints.WEST:
				r.y += diffy / 2;
				break;
			case GridConstraints.NORTHWEST:
				break;
			case GridConstraints.LINE_START:
				if (rightToLeft) {
					r.x += diffx;
				}
				r.y += diffy / 2;
				break;
			case GridConstraints.LINE_END:
				if (!rightToLeft) {
					r.x += diffx;
				}
				r.y += diffy / 2;
				break;
			case GridConstraints.FIRST_LINE_START:
				if (rightToLeft) {
					r.x += diffx;
				}
				break;
			case GridConstraints.FIRST_LINE_END:
				if (!rightToLeft) {
					r.x += diffx;
				}
				break;
			case GridConstraints.LAST_LINE_START:
				if (rightToLeft) {
					r.x += diffx;
				}
				r.y += diffy;
				break;
			case GridConstraints.LAST_LINE_END:
				if (!rightToLeft) {
					r.x += diffx;
				}
				r.y += diffy;
				break;
			default:
				throw new IllegalArgumentException("illegal anchor value");
		}
	}

	/**
	 * Positions on the baseline.
	 * 
	 * @param cellY
	 *            the location of the row, does not include insets
	 * @param cellHeight
	 *            the height of the row, does not take into account insets
	 * @param r
	 *            available bounds for the component, is padded by insets and ipady
	 */
	private void alignOnBaseline(GridConstraints cons, Rectangle r, int cellY, int cellHeight) {
		if (cons.ascent >= 0) {
			if (cons.baselineResizeBehavior == Component.BaselineResizeBehavior.CONSTANT_DESCENT) {
				// Anchor to the bottom.
				// Baseline is at (cellY + cellHeight - maxDescent).
				// Bottom of component (maxY) is at baseline + descent
				// of component. We need to subtract the bottom inset here
				// as the descent in the constraints object includes the
				// bottom inset.
				int maxY = cellY + cellHeight - layoutInfo.maxDescent[cons.tempY + cons.tempHeight - 1] + cons.descent - cons.insets.bottom;
				if (!cons.isVerticallyResizable()) {
					// Component not resizable, calculate y location
					// from maxY - height.
					r.y = maxY - cons.minHeight;
					r.height = cons.minHeight;
				} else {
					// Component is resizable. As brb is constant descent,
					// can expand component to fill region above baseline.
					// Subtract out the top inset so that components insets
					// are honored.
					r.height = maxY - cellY - cons.insets.top;
				}
			} else {
				// BRB is not constant_descent
				int baseline; // baseline for the row, relative to cellY
				// Component baseline, includes insets.top
				int ascent = cons.ascent;
				if (layoutInfo.hasConstantDescent(cons.tempY)) {
					// Mixed ascent/descent in same row, calculate position
					// off maxDescent
					baseline = cellHeight - layoutInfo.maxDescent[cons.tempY];
				} else {
					// Only ascents/unknown in this row, anchor to top
					baseline = layoutInfo.maxAscent[cons.tempY];
				}
				if (cons.baselineResizeBehavior == Component.BaselineResizeBehavior.OTHER) {
					// BRB is other, which means we can only determine
					// the baseline by asking for it again giving the
					// size we plan on using for the component.
					boolean fits = false;
					ascent = componentAdjusting.getBaseline(r.width, r.height);
					if (ascent >= 0) {
						// Component has a baseline, pad with top inset
						// (this follows from calculateBaseline which
						// does the same).
						ascent += cons.insets.top;
					}
					if (ascent >= 0 && ascent <= baseline) {
						// Components baseline fits within rows baseline.
						// Make sure the descent fits within the space as well.
						if (baseline + (r.height - ascent - cons.insets.top) <= cellHeight - cons.insets.bottom) {
							// It fits, we're good.
							fits = true;
						} else if (cons.isVerticallyResizable()) {
							// Doesn't fit, but it's resizable. Try
							// again assuming we'll get ascent again.
							int ascent2 = componentAdjusting.getBaseline(r.width, cellHeight - cons.insets.bottom - baseline + ascent);
							if (ascent2 >= 0) {
								ascent2 += cons.insets.top;
							}
							if (ascent2 >= 0 && ascent2 <= ascent) {
								// It'll fit
								r.height = cellHeight - cons.insets.bottom - baseline + ascent;
								ascent = ascent2;
								fits = true;
							}
						}
					}
					if (!fits) {
						// Doesn't fit, use min size and original ascent
						ascent = cons.ascent;
						r.width = cons.minWidth;
						r.height = cons.minHeight;
					}
				}
				// Reset the components y location based on
				// components ascent and baseline for row. Because ascent
				// includes the baseline
				r.y = cellY + baseline - ascent + cons.insets.top;
				if (cons.isVerticallyResizable()) {
					switch (cons.baselineResizeBehavior) {
						case CONSTANT_ASCENT:
							r.height = Math.max(cons.minHeight, cellY + cellHeight - r.y - cons.insets.bottom);
							break;
						case CENTER_OFFSET: {
							int upper = r.y - cellY - cons.insets.top;
							int lower = cellY + cellHeight - r.y - cons.minHeight - cons.insets.bottom;
							int delta = Math.min(upper, lower);
							delta += delta;
							if (delta > 0 && (cons.minHeight + cons.centerPadding + delta) / 2 + cons.centerOffset != baseline) {
								// Off by 1
								delta--;
							}
							r.height = cons.minHeight + delta;
							r.y = cellY + baseline - (r.height + cons.centerPadding) / 2 - cons.centerOffset;
						}
							break;
						case OTHER:
							// Handled above
							break;
						default:
							break;
					}
				}
			}
		} else {
			centerVertically(cons, r, cellHeight);
		}
	}

	/**
	 * Positions the specified component above the baseline. That is the bottom edge of the component will be aligned along the baseline. If the
	 * row does not have a baseline, this centers the component.
	 */
	private void alignAboveBaseline(GridConstraints cons, Rectangle r, int cellY, int cellHeight) {
		if (layoutInfo.hasBaseline(cons.tempY)) {
			int maxY; // Baseline for the row
			if (layoutInfo.hasConstantDescent(cons.tempY)) {
				// Prefer descent
				maxY = cellY + cellHeight - layoutInfo.maxDescent[cons.tempY];
			} else {
				// Prefer ascent
				maxY = cellY + layoutInfo.maxAscent[cons.tempY];
			}
			if (cons.isVerticallyResizable()) {
				// Component is resizable. Top edge is offset by top
				// inset, bottom edge on baseline.
				r.y = cellY + cons.insets.top;
				r.height = maxY - r.y;
			} else {
				// Not resizable.
				r.height = cons.minHeight + cons.ipady;
				r.y = maxY - r.height;
			}
		} else {
			centerVertically(cons, r, cellHeight);
		}
	}

	/**
	 * Positions below the baseline.
	 */
	private void alignBelowBaseline(GridConstraints cons, Rectangle r, int cellY, int cellHeight) {
		if (layoutInfo.hasBaseline(cons.tempY)) {
			if (layoutInfo.hasConstantDescent(cons.tempY)) {
				// Prefer descent
				r.y = cellY + cellHeight - layoutInfo.maxDescent[cons.tempY];
			} else {
				// Prefer ascent
				r.y = cellY + layoutInfo.maxAscent[cons.tempY];
			}
			if (cons.isVerticallyResizable()) {
				r.height = cellY + cellHeight - r.y - cons.insets.bottom;
			}
		} else {
			centerVertically(cons, r, cellHeight);
		}
	}

	private void centerVertically(GridConstraints cons, Rectangle r, int cellHeight) {
		if (!cons.isVerticallyResizable()) {
			r.y += Math.max(0, (cellHeight - cons.insets.top - cons.insets.bottom - cons.minHeight - cons.ipady) / 2);
		}
	}

	/**
	 * Figures out the minimum size of the master based on the information from <code>getLayoutInfo</code>. This method should only be used
	 * internally by <code>GridLayout</code>.
	 * 
	 * @param parent
	 *            the layout container
	 * @param info
	 *            the layout info for this parent
	 * @return a <code>Dimension</code> object containing the minimum size
	 * @since 1.4
	 */
	protected Dimension getMinSize(Container parent, GridLayoutInfo info) {
		return GetMinSize(parent, info);
	}

	/**
	 * This method is obsolete and supplied for backwards compatability only; new code should call
	 * {@link #getMinSize(java.awt.Container, GridLayoutInfo) getMinSize} instead. This method is the same as <code>getMinSize</code>; refer to
	 * <code>getMinSize</code> for details on parameters and return value.
	 */
	protected Dimension GetMinSize(Container parent, GridLayoutInfo info) {
		Dimension d = new Dimension();
		int i, t;
		Insets insets = parent.getInsets();

		t = 0;
		for (i = 0; i < info.width; i++)
			t += info.minWidth[i];
		d.width = t + insets.left + insets.right;

		t = 0;
		for (i = 0; i < info.height; i++)
			t += info.minHeight[i];
		d.height = t + insets.top + insets.bottom;

		return d;
	}

	transient boolean rightToLeft = false;

	/**
	 * This method is obsolete and supplied for backwards compatability only; new code should call {@link #arrangeGrid(Container) arrangeGrid}
	 * instead. This method is the same as <code>arrangeGrid</code>; refer to <code>arrangeGrid</code> for details on the parameter.
	 */
	protected void arrangeGrid(Container parent) {
		Component comp;
		int compindex;
		GridConstraints constraints;
		Insets insets = parent.getInsets();
		Component components[] = parent.getComponents();
		Dimension d;
		Rectangle r = new Rectangle();
		int i, diffw, diffh;
		double weight;
		GridLayoutInfo info;

		rightToLeft = !parent.getComponentOrientation().isLeftToRight();

		/*
		 * If the parent has no slaves anymore, then don't do anything at all: just leave the parent's size as-is.
		 */
		if (components.length == 0 && (columnWidths == null || columnWidths.length == 0) && (rowHeights == null || rowHeights.length == 0)) {
			return;
		}

		/*
		 * Pass #1: scan all the slaves to figure out the total amount of space needed.
		 */

		info = getLayoutInfo(parent, PREFERREDSIZE);
		d = getMinSize(parent, info);

		// if (parent.getWidth() < d.width || parent.getHeight() < d.height) {
		// info = getLayoutInfo(parent, MINSIZE);
		// d = getMinSize(parent, info);
		// }

		layoutInfo = info;
		r.width = d.width;
		r.height = d.height;

		/*
		 * If the current dimensions of the window don't match the desired dimensions, then adjust the minWidth and minHeight arrays according to
		 * the weights.
		 */

		diffw = parent.getWidth() - r.width;
		if (diffw != 0) {
			weight = 0.0;
			for (i = 0; i < info.width; i++)
				weight += info.weightX[i];
			if (weight > 0.0) {
				for (i = 0; i < info.width; i++) {
					int dx = (int) (((diffw) * info.weightX[i]) / weight);
					info.minWidth[i] += dx;
					r.width += dx;
					if (info.minWidth[i] < 0) {
						r.width -= info.minWidth[i];
						info.minWidth[i] = 0;
					}
				}
			}
			diffw = parent.getWidth() - r.width;
		} else {
			diffw = 0;
		}

		diffh = parent.getHeight() - r.height;
		if (diffh != 0) {
			weight = 0.0;
			for (i = 0; i < info.height; i++)
				weight += info.weightY[i];
			if (weight > 0.0) {
				for (i = 0; i < info.height; i++) {
					int dy = (int) (((diffh) * info.weightY[i]) / weight);
					info.minHeight[i] += dy;
					r.height += dy;
					if (info.minHeight[i] < 0) {
						r.height -= info.minHeight[i];
						info.minHeight[i] = 0;
					}
				}
			}
			diffh = parent.getHeight() - r.height;
		} else {
			diffh = 0;
		}

		/*
		 * Now do the actual layout of the slaves using the layout information that has been collected.
		 */

		info.startx = diffw / 2 + insets.left;
		info.starty = diffh / 2 + insets.top;

		for (compindex = 0; compindex < components.length; compindex++) {
			comp = components[compindex];
			if (!comp.isVisible()) {
				continue;
			}
			constraints = lookupConstraints(comp);

			if (!rightToLeft) {
				r.x = info.startx;
				for (i = 0; i < constraints.tempX; i++)
					r.x += info.minWidth[i];
			} else {
				r.x = parent.getWidth() - (diffw / 2 + insets.right);
				for (i = 0; i < constraints.tempX; i++)
					r.x -= info.minWidth[i];
			}

			r.y = info.starty;
			for (i = 0; i < constraints.tempY; i++)
				r.y += info.minHeight[i];

			r.width = 0;
			for (i = constraints.tempX; i < (constraints.tempX + constraints.tempWidth); i++) {
				r.width += info.minWidth[i];
			}

			r.height = 0;
			for (i = constraints.tempY; i < (constraints.tempY + constraints.tempHeight); i++) {
				r.height += info.minHeight[i];
			}

			componentAdjusting = comp;
			adjustForGravity(constraints, r);

			if (r.x < 0) {
				r.width += r.x;
				r.x = 0;
			}

			if (r.y < 0) {
				r.height += r.y;
				r.y = 0;
			}

			/*
			 * If the window is too small to be interesting then unmap it. Otherwise configure it and then make sure it's mapped.
			 */
			if ((r.width <= 0) || (r.height <= 0)) {
				comp.setBounds(0, 0, 0, 0);
			} else {
				if (comp.getX() != r.x || comp.getY() != r.y || comp.getWidth() != r.width || comp.getHeight() != r.height) {
					comp.setBounds(r.x, r.y, r.width, r.height);
				}
			}
		}

		// Store calculated components sizes to their preferred sizes to avoid auto resize problem
		// (during resizing of any component before preferred sizes of all components are set
		// there may be resizing of parents as layout may use preferred size to calculate weight of component).
		for (compindex = 0; compindex < components.length; compindex++) {
			comp = components[compindex];
			if (!comp.isVisible()) {
				continue;
			}
			if (!comp.isPreferredSizeSet() || !comp.getPreferredSize().equals(comp.getSize())) {
				comp.setPreferredSize(comp.getSize());
			}
		}
	}
}
