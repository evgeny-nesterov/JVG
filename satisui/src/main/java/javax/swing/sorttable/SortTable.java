package javax.swing.sorttable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JTable;
import javax.swing.TableUtil;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class SortTable extends JTable {
	private static final long serialVersionUID = 1L;

	public SortTable(SortTableModel model) {
		super(model);

		TableColumnModel cm = getColumnModel();
		for (int i = 0; i < cm.getColumnCount(); i++) {
			TableColumn column = cm.getColumn(i);
			column.setHeaderValue(new SortHeaderObject(model.getColumnId(column.getModelIndex())));
		}
		setTableHeader(new SortTableHeader(cm));

		createDefaultColumnsFromModel();
	}

	private Map<Object, TableColumn> all_columns = null;

	@Override
	public TableColumn getColumn(Object columnID) {
		return all_columns.get(columnID);
	}

	private Map<Object, Integer> sequence = null;

	@Override
	public void createDefaultColumnsFromModel() {
		SortTableModel model = (SortTableModel) getModel();
		if (model != null) {
			TableColumnModel cm = getColumnModel();
			Map<Integer, TableColumn> columns = new TreeMap<Integer, TableColumn>();
			List<TableColumn> adjust = null;

			if (all_columns == null) {
				all_columns = new HashMap<Object, TableColumn>();
				sequence = new HashMap<Object, Integer>();

				for (int i = 0; i < model.getColumnCount(); i++) {
					Object columnId = model.getColumnId(i);
					SortHeaderObject head_value = model.getHeaderObject(i);
					SortHeaderRenderer header_renderer = new SortHeaderRenderer();

					TableColumn column = new TableColumn(i);
					column.setIdentifier(columnId);
					column.setHeaderValue(head_value);
					column.setHeaderRenderer(header_renderer);

					if (adjust == null) {
						adjust = new ArrayList<TableColumn>(model.getColumnCount());
					}
					adjust.add(column);

					all_columns.put(columnId, column);
					sequence.put(columnId, i);

					if (head_value.isVisible()) {
						columns.put(i, column);
					}
				}
			} else {
				ArrayList<Integer> old_indexes = new ArrayList<Integer>();
				for (int i = 0; i < cm.getColumnCount(); i++) {
					TableColumn column = cm.getColumn(i);
					Object columnID = column.getIdentifier();
					int old_index = sequence.get(columnID);
					old_indexes.add(old_index);
				}
				Collections.sort(old_indexes);
				for (int i = 0; i < cm.getColumnCount(); i++) {
					TableColumn column = cm.getColumn(i);
					Object columnID = column.getIdentifier();
					int new_index = old_indexes.get(i);
					sequence.put(columnID, new_index);
				}

				for (int i = 0; i < model.getColumnCount(); i++) {
					Object columnID = model.getColumnId(i);
					TableColumn column = all_columns.get(columnID);
					SortHeaderObject header_value = (SortHeaderObject) column.getHeaderValue();

					if (header_value.isVisible()) {
						int index = sequence.get(columnID);
						columns.put(index, column);
					} else {
						header_value.setPressed(false);
					}
				}
			}

			while (cm.getColumnCount() > 0) {
				cm.removeColumn(cm.getColumn(0));
			}

			for (TableColumn column : columns.values()) {
				addColumn(column);
			}

			if (adjust != null) {
				for (TableColumn column : adjust) {
					try {
						TableUtil.adjustColumnWidth(this, column.getModelIndex(), 10, 1000);
					} catch (Exception exc) {
					}
				}
			}
		}
	}
}
