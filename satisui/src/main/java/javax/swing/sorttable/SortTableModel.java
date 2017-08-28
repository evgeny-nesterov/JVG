package javax.swing.sorttable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public abstract class SortTableModel extends AbstractTableModel {
	public final static int LEFT = SwingConstants.LEFT;

	public final static int CENTER = SwingConstants.CENTER;

	public final static int RIGHT = SwingConstants.RIGHT;

	protected List<String> columnIds = new ArrayList<String>();

	private Map<String, String> names = new HashMap<String, String>(10);

	private Map<String, SortHeaderObject> heads = new HashMap<String, SortHeaderObject>();

	protected List<String> grouping = new ArrayList<String>();

	protected Map<Integer, String> groupIndexes = new TreeMap<Integer, String>();

	public abstract void sort(String columnId, boolean isDesc);

	public void activateHeader(SortHeaderObject activeHeader) {
		for (SortHeaderObject header : heads.values()) {
			header.setActive(header == activeHeader);
			header.setPressed(false);
		}
	}

	public SortHeaderObject getActiveHeaderObject() {
		for (int column = 0; column < getColumnCount(); column++) {
			SortHeaderObject head_value = getHeaderObject(column);
			if (head_value.isActive()) {
				return head_value;
			}
		}
		return null;
	}

	public int getAlignment(int col) {
		return LEFT;
	}

	@Override
	public int getColumnCount() {
		return columnIds.size();
	}

	public String getColumnId(int col) {
		return columnIds.get(col);
	}

	@Override
	public String getColumnName(int col) {
		String columnId = getColumnId(col);
		if (names.containsKey(columnId)) {
			return names.get(columnId);
		} else {
			return columnId;
		}
	}

	public SortHeaderObject getHeaderObject(int column) {
		String columnId = getColumnId(column);
		return getHeaderObject(columnId);
	}

	public SortHeaderObject getHeaderObject(String columnId) {
		if (columnId == null) {
			return null;
		} else if (heads.containsKey(columnId)) {
			return heads.get(columnId);
		} else {
			SortHeaderObject head_value = new SortHeaderObject(columnId, false, false);
			heads.put(columnId, head_value);
			return head_value;
		}
	}

	public int getIndexById(String columnId) {
		return columnIds.indexOf(columnId);
	}

	public void group() {
		// do nothing on default
	}

	public boolean isGrouping(String columnId) {
		return groupIndexes.containsValue(columnId);
	}

	public void setColumnName(int col, String name) {
		String columnId = getColumnId(col);
		names.put(columnId, name);
	}

	public void addColumnName(String columnId, String name) {
		names.put(columnId, name);
		columnIds.add(columnId);
	}

	public void setGrouping(String columnId, boolean active, int index) {
		int old_index = Integer.MAX_VALUE, i = 0;
		for (Integer id : groupIndexes.keySet()) {
			String group = groupIndexes.get(id);
			if (columnId.equals(group)) {
				groupIndexes.remove(id);
				old_index = i;
				break;
			}
			i++;
		}

		if (active) {
			if (groupIndexes.containsKey(index)) {
				Map<Integer, String> new_group_indexes = new TreeMap<Integer, String>();
				boolean isShiftToRight = index < old_index;

				for (int cur_index : groupIndexes.keySet()) {
					String id = groupIndexes.get(cur_index);
					if (isShiftToRight) {
						if (cur_index >= index && cur_index < old_index) {
							cur_index++;
						}
					} else {
						if (cur_index <= index && cur_index > old_index) {
							cur_index--;
						}
					}

					new_group_indexes.put(cur_index, id);
				}

				groupIndexes = new_group_indexes;
			}

			groupIndexes.put(index, columnId);
		}

		grouping.clear();
		grouping.addAll(groupIndexes.values());

		group();
		fireTableStructureChanged();
	}
}
