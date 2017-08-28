package javax.swing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

public class DefaultTableModel extends AbstractTableModel implements Serializable {
	private static final long serialVersionUID = -1844325307458375708L;

	protected List<String> columnIdentifiers;

	protected List<List<Object>> dataList;

	public DefaultTableModel() {
		this(0, 0);
	}

	private static List<List<Object>> newList(int size) {
		List<List<Object>> v = new ArrayList<List<Object>>(size);
		return v;
	}

	public DefaultTableModel(int rowCount, int columnCount) {
		this(new ArrayList<String>(columnCount), rowCount);
	}

	public DefaultTableModel(List<String> columnNames, int rowCount) {
		setDataList(newList(rowCount), columnNames);
	}

	public DefaultTableModel(String[] columnNames, int rowCount) {
		this(convertToList(columnNames), rowCount);
	}

	public DefaultTableModel(List<List<Object>> data, List<String> columnNames) {
		setDataList(data, columnNames);
	}

	public DefaultTableModel(Object[][] data, String[] columnNames) {
		setDataList(data, columnNames);
	}

	public List<List<Object>> getDataList() {
		return dataList;
	}

	public void setDataList(List<List<Object>> dataList, List<String> columnIdentifiers) {
		this.dataList = dataList != null ? dataList : new ArrayList<List<Object>>();
		this.columnIdentifiers = columnIdentifiers != null ? columnIdentifiers : new ArrayList<String>();
		justifyRows(0, getRowCount());
		fireTableStructureChanged();
	}

	public void setDataList(Object[][] dataList, String[] columnIdentifiers) {
		setDataList(convertToList(dataList), convertToList(columnIdentifiers));
	}

	public void newDataAvailable(TableModelEvent event) {
		fireTableChanged(event);
	}

	private void justifyRows(int from, int to) {
		for (int i = from; i < to; i++) {
			if (dataList.get(i) == null) {
				dataList.set(i, new ArrayList<Object>());
			}
		}
	}

	public void newRowsAdded(TableModelEvent e) {
		justifyRows(e.getFirstRow(), e.getLastRow() + 1);
		fireTableChanged(e);
	}

	public void rowsRemoved(TableModelEvent event) {
		fireTableChanged(event);
	}

	public void setNumRows(int rowCount) {
		int old = getRowCount();
		if (old == rowCount) {
			return;
		}
		if (rowCount <= old) {
			fireTableRowsDeleted(rowCount, old - 1);
		} else {
			justifyRows(old, rowCount);
			fireTableRowsInserted(old, rowCount - 1);
		}
	}

	public void setRowCount(int rowCount) {
		setNumRows(rowCount);
	}

	public void addRow(List<Object> rowData) {
		insertRow(getRowCount(), rowData);
	}

	public void addRow(Object[] rowData) {
		addRow(convertToList(rowData));
	}

	public void insertRow(int row, List<Object> rowData) {
		dataList.add(row, rowData);
		justifyRows(row, row + 1);
		fireTableRowsInserted(row, row);
	}

	public void insertRow(int row, Object[] rowData) {
		insertRow(row, convertToList(rowData));
	}

	private static int gcd(int i, int j) {
		return (j == 0) ? i : gcd(j, i % j);
	}

	private static void rotate(List<List<Object>> v, int a, int b, int shift) {
		int size = b - a;
		int r = size - shift;
		int g = gcd(size, r);
		for (int i = 0; i < g; i++) {
			int to = i;
			List<Object> tmp = v.get(a + to);
			for (int from = (to + r) % size; from != i; from = (to + r) % size) {
				v.set(a + to, v.get(a + from));
				to = from;
			}
			v.set(a + to, tmp);
		}
	}

	public void moveRow(int start, int end, int to) {
		int shift = to - start;
		int first, last;
		if (shift < 0) {
			first = to;
			last = end;
		} else {
			first = start;
			last = to + end - start;
		}
		rotate(dataList, first, last + 1, shift);
		fireTableRowsUpdated(first, last);
	}

	public void removeRow(int row) {
		dataList.remove(row);
		fireTableRowsDeleted(row, row);
	}

	public void setColumnIdentifiers(List<String> columnIdentifiers) {
		setDataList(dataList, columnIdentifiers);
	}

	public void setColumnIdentifiers(String[] newIdentifiers) {
		setColumnIdentifiers(convertToList(newIdentifiers));
	}

	public void addColumn(String columnName) {
		addColumn(columnName, (List<Object>) null);
	}

	public void addColumn(String columnName, List<Object> columnData) {
		columnIdentifiers.add(columnName);
		if (columnData != null) {
			int columnSize = columnData.size();
			justifyRows(0, getRowCount());
			int newColumn = getColumnCount() - 1;
			for (int i = 0; i < columnSize; i++) {
				List<Object> row = (List<Object>) dataList.get(i);
				row.set(newColumn, columnData.get(i));
			}
		} else {
			justifyRows(0, getRowCount());
		}

		fireTableStructureChanged();
	}

	public void addColumn(String columnName, Object[] columnData) {
		addColumn(columnName, convertToList(columnData));
	}

	public int getRowCount() {
		return dataList.size();
	}

	public int getColumnCount() {
		return columnIdentifiers.size();
	}

	public String getColumnName(int column) {
		Object id = null;
		if (column < columnIdentifiers.size() && (column >= 0)) {
			id = columnIdentifiers.get(column);
		}
		return (id == null) ? super.getColumnName(column) : id.toString();
	}

	public boolean isCellEditable(int row, int column) {
		return true;
	}

	public Object getValueAt(int row, int column) {
		List<Object> rowList = (List<Object>) dataList.get(row);
		return rowList.get(column);
	}

	public void setValueAt(Object aValue, int row, int column) {
		List<Object> rowList = (List<Object>) dataList.get(row);
		rowList.set(column, aValue);
		fireTableCellUpdated(row, column);
	}

	protected static <O> List<O> convertToList(O[] anArray) {
		if (anArray == null) {
			return null;
		}
		List<O> v = new ArrayList<O>(anArray.length);
		for (O o : anArray) {
			v.add(o);
		}
		return v;
	}

	protected static <O> List<List<O>> convertToList(O[][] anArray) {
		if (anArray == null) {
			return null;
		}
		List<List<O>> v = new ArrayList<List<O>>(anArray.length);
		for (O[] o : anArray) {
			v.add(convertToList(o));
		}
		return v;
	}
}
