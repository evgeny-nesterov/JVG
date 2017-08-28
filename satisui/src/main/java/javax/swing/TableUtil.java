package javax.swing;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.StyledDocument;

public class TableUtil {
	public static void adjustRowHights(JTable table) {
		TableCellRenderer renderer = table.getDefaultRenderer(StyledDocument.class);
		TableModel model = table.getModel();
		for (int r = 0; r < table.getRowCount(); r++) {
			int h = 0;
			for (int i = 0; i < table.getColumnCount(); i++) {
				Component c = renderer.getTableCellRendererComponent(table, model.getValueAt(r, i), false, false, r, i);
				h = Math.max(h, c.getPreferredSize().height);
			}

			if (table.getRowHeight(r) != h) {
				table.setRowHeight(r, h);
			}
		}
	}

	public static void adjustRowHight(JTable table, int row) {
		TableCellRenderer renderer = table.getDefaultRenderer(StyledDocument.class);
		TableModel model = table.getModel();

		int h = 0;
		for (int i = 0; i < table.getColumnCount(); i++) {
			Component c = renderer.getTableCellRendererComponent(table, model.getValueAt(row, i), false, false, row, i);
			h = Math.max(h, c.getPreferredSize().height);
		}

		if (table.getRowHeight(row) != h) {
			table.setRowHeight(row, h);
		}
	}

	public static void adjustColumnsWidth(JTable tbl, int minWidth, int maxWidth) {
		if (tbl != null) {
			for (int i = 0; i < tbl.getColumnCount(); i++) {
				adjustColumnWidth(tbl, i, 50, 1000);
			}
		}
	}

	public static void setColumnWidth(JTable tbl, int column, int width) {
		TableColumn tCol = tbl.getColumnModel().getColumn(column);
		tCol.setPreferredWidth(width);
		tCol.setWidth(width);
	}

	public static void adjustColumnWidth(JTable tbl, int column, int minWidth, int maxWidth) {
		TableColumn tCol = tbl.getColumnModel().getColumn(column);
		int viewColumn = column; // tCol.getModelIndex();

		Dimension dim;
		int width; // , height;
		TableCellRenderer renderer;

		// compute the sizes
		if (tbl.getTableHeader() != null) {
			renderer = tCol.getHeaderRenderer();
			if (renderer == null) {
				renderer = tbl.getTableHeader().getDefaultRenderer();
			}
			dim = renderer.getTableCellRendererComponent(tbl, tCol.getHeaderValue(), true, true, 0, viewColumn).getPreferredSize();
			width = dim.width;

			// make sure the table header gets sized correctly
			// height = dim.height;
			// if(height + tbl.getRowMargin() >
			// tbl.getTableHeader().getPreferredSize().height)
			// tbl.getTableHeader().setPreferredSize(new
			// Dimension(tbl.getTableHeader().getPreferredSize().width,
			// height));
			int marginWidth = tbl.getColumnModel().getColumnMargin();
			if (marginWidth > 0) {
				width += marginWidth;
			}
		} else {
			width = 0;
		}

		renderer = tCol.getCellRenderer();
		if (renderer == null) {
			renderer = tbl.getDefaultRenderer(tbl.getColumnClass(column));
		}
		if (renderer != null) {
			for (int row = 0; row < tbl.getRowCount(); row++) {
				dim = renderer.getTableCellRendererComponent(tbl, tbl.getValueAt(row, column), false, false, row, viewColumn).getPreferredSize();
				width = Math.max(width, dim.width);
				// height = dim.height;
			}
		}

		int marginWidth = tbl.getColumnModel().getColumnMargin();
		if (marginWidth > 0) {
			width += marginWidth;
		}
		width += 4;
		if (width < minWidth) {
			width = minWidth;
		} else if (width > maxWidth) {
			width = maxWidth;
		}

		tCol.setPreferredWidth(width);
		tCol.setWidth(width);
	}
}
