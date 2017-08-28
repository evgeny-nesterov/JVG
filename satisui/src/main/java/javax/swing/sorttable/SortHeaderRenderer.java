package javax.swing.sorttable;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class SortHeaderRenderer implements TableCellRenderer {
	private final static ImageIcon imgRised = new ImageIcon(SortHeaderRenderer.class.getResource("img/rised.png"));

	private final static ImageIcon imgLowered = new ImageIcon(SortHeaderRenderer.class.getResource("img/lowered.png"));

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
		JLabel c = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (value instanceof SortHeaderObject) {
			SortHeaderObject header = (SortHeaderObject) value;
			int modelIndex = table.getColumnModel().getColumn(column).getModelIndex();

			SortTableModel model = (SortTableModel) table.getModel();
			String name = model.getColumnName(modelIndex);
			c.setText(name);
			c.setHorizontalAlignment(model.getAlignment(modelIndex));

			if (header.isActive()) {
				c.setIcon(header.isRised() ? imgRised : imgLowered);
			} else {
				c.setIcon(null);
			}
		} else {
			c.setIcon(null);
			c.setText("");
		}

		return c;
	}
}
