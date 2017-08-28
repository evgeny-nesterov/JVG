package javax.swing.dock.grid;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GridBagLayoutHelper {
	public static boolean fillEmptyAreas(Container container) {
		if (container.getLayout() instanceof GridBagLayout) {
			boolean result = false;
			while (_fillEmptyAreas(container)) {
				result = true;
			}
			return result;
		} else {
			return false;
		}
	}

	public static boolean _fillEmptyAreas(Container container) {
		if (container.getComponentCount() == 0) {
			return false;
		}

		GridBagLayout layout = (GridBagLayout) container.getLayout();
		Set<Integer> horIndexesSet = new HashSet<Integer>();
		Set<Integer> verIndexesSet = new HashSet<Integer>();
		for (int i = 0; i < container.getComponentCount(); i++) {
			Component c = container.getComponent(i);
			GridBagConstraints constr = layout.getConstraints(c);
			horIndexesSet.add(constr.gridx);
			horIndexesSet.add(constr.gridx + constr.gridwidth - 1);
			verIndexesSet.add(constr.gridy);
			verIndexesSet.add(constr.gridy + constr.gridheight - 1);
		}

		// indexes arrays
		Integer[] horIndexes = new Integer[horIndexesSet.size()];
		horIndexesSet.toArray(horIndexes);
		Arrays.sort(horIndexes);

		Integer[] verIndexes = new Integer[verIndexesSet.size()];
		verIndexesSet.toArray(verIndexes);
		Arrays.sort(verIndexes);

		// pos to index arrays
		int[] horPosToIndex = new int[horIndexes[horIndexes.length - 1] + 1];
		Arrays.fill(horPosToIndex, -1);
		for (int i = 0; i < horIndexes.length; i++) {
			horPosToIndex[horIndexes[i]] = i;
		}

		int[] verPosToIndex = new int[verIndexes[verIndexes.length - 1] + 1];
		Arrays.fill(verPosToIndex, -1);
		for (int i = 0; i < verIndexes.length; i++) {
			verPosToIndex[verIndexes[i]] = i;
		}

		// build matrix of components (keep index of component)
		int[][] matrix = new int[horIndexes.length][verIndexes.length];
		for (int ix = 0; ix < horIndexes.length; ix++) {
			for (int iy = 0; iy < verIndexes.length; iy++) {
				matrix[ix][iy] = -1;
			}
		}
		for (int i = 0; i < container.getComponentCount(); i++) {
			Component c = container.getComponent(i);
			GridBagConstraints constr = layout.getConstraints(c);
			for (int x = constr.gridx; x < constr.gridx + constr.gridwidth; x++) {
				if (horPosToIndex[x] != -1) {
					for (int y = constr.gridy; y < constr.gridy + constr.gridheight; y++) {
						if (verPosToIndex[y] != -1) {
							matrix[horPosToIndex[x]][verPosToIndex[y]] = i;
						}
					}
				}
			}
		}

		// fill blank areas
		boolean changed = false;
		boolean[] proceed = new boolean[container.getComponentCount()];
		for (int ix = 0; ix < horIndexes.length; ix++) {
			for (int iy = 0; iy < verIndexes.length; iy++) {
				// process every component only once
				int compIndex = matrix[ix][iy];
				if (compIndex != -1 && !proceed[compIndex]) {
					Component c = container.getComponent(compIndex);
					GridBagConstraints constr = layout.getConstraints(c);
					proceed[compIndex] = true;

					int ix1 = horPosToIndex[constr.gridx];
					int iy1 = verPosToIndex[constr.gridy];
					int ix2 = horPosToIndex[constr.gridx + constr.gridwidth - 1];
					int iy2 = verPosToIndex[constr.gridy + constr.gridheight - 1];

					boolean updateConstraints = false;

					// try fill left
					if (ix1 > 0) {
						for (int x = ix1 - 1; x >= 0; x--) {
							boolean expand = true;
							for (int y = iy1; y <= iy2; y++) {
								if (matrix[x][y] != -1) {
									expand = false;
									break;
								}
							}
							if (expand) {
								for (int y = iy1; y <= iy2; y++) {
									matrix[x][y] = compIndex;
								}
								constr.gridx--;
								constr.gridwidth++;
								updateConstraints = true;
							} else {
								break;
							}
						}
					}

					// try fill top
					if (iy1 > 0) {
						for (int y = iy1 - 1; y >= 0; y--) {
							boolean expand = true;
							for (int x = ix1; x <= ix2; x++) {
								if (matrix[x][y] != -1) {
									expand = false;
									break;
								}
							}
							if (expand) {
								for (int x = ix1; x <= ix2; x++) {
									matrix[x][y] = compIndex;
								}
								constr.gridy--;
								constr.gridheight++;
								updateConstraints = true;
							} else {
								break;
							}
						}
					}

					// try fill right
					if (ix2 < horIndexes.length) {
						for (int x = ix2 + 1; x < horIndexes.length; x++) {
							boolean expand = true;
							for (int y = iy1; y <= iy2; y++) {
								if (matrix[x][y] != -1) {
									expand = false;
									break;
								}
							}
							if (expand) {
								for (int y = iy1; y <= iy2; y++) {
									matrix[x][y] = compIndex;
								}
								constr.gridwidth++;
								updateConstraints = true;
							} else {
								break;
							}
						}
					}

					// try fill bottom
					if (iy2 < verIndexes.length) {
						for (int y = iy2 + 1; y < verIndexes.length; y++) {
							boolean expand = true;
							for (int x = ix1; x <= ix2; x++) {
								if (matrix[x][y] != -1) {
									expand = false;
									break;
								}
							}
							if (expand) {
								for (int x = ix1; x <= ix2; x++) {
									matrix[x][y] = compIndex;
								}
								constr.gridheight++;
								updateConstraints = true;
							} else {
								break;
							}
						}
					}

					if (updateConstraints) {
						layout.setConstraints(c, constr);
						changed = true;
					}
				}
			}
		}
		return changed;
	}

	public static void shiftToZeroPoint(Container container) {
		if (container.getLayout() instanceof GridBagLayout) {
			GridBagLayout layout = (GridBagLayout) container.getLayout();
			int[] gridBounds = getGridBounds(container);
			int minx = gridBounds[0];
			int miny = gridBounds[1];
			if (minx != 0 || miny != 0) {
				for (int i = 0; i < container.getComponentCount(); i++) {
					Component c = container.getComponent(i);
					GridBagConstraints constr = layout.getConstraints(c);
					constr.gridx -= minx;
					constr.gridy -= miny;
					layout.setConstraints(c, constr);
				}
			}
		}
	}

	public static int[] getGridBounds(Container container) {
		if (container.getLayout() instanceof GridBagLayout) {
			GridBagLayout layout = (GridBagLayout) container.getLayout();
			if (container.getComponentCount() == 0) {
				return new int[] { 0, 0, 0, 0 };
			}
			int minx = Integer.MAX_VALUE;
			int miny = Integer.MAX_VALUE;
			int maxx = -Integer.MAX_VALUE;
			int maxy = -Integer.MAX_VALUE;
			for (int i = 0; i < container.getComponentCount(); i++) {
				Component c = container.getComponent(i);
				GridBagConstraints constr = layout.getConstraints(c);
				minx = Math.min(minx, constr.gridx);
				maxx = Math.max(maxx, constr.gridx + constr.gridwidth);
				miny = Math.min(miny, constr.gridy);
				maxy = Math.max(maxy, constr.gridy + constr.gridheight);
			}
			return new int[] { minx, miny, maxx, maxy };
		} else {
			return new int[4];
		}
	}
}
