package javax.swing.sorttable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu.Separator;
import javax.swing.TableUtil;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class SortTableHeader extends JTableHeader {
	private static final long serialVersionUID = 1L;
	private int pressedX, pressedY;

	public SortTableHeader(TableColumnModel cm) {
		super(cm);
	}

	@Override
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				pressMouse(e.getX(), e.getY());
			} else if (e.getButton() == MouseEvent.BUTTON3) {
				showPopup(e.getX(), e.getY());
			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			releaseMouse(e.getX(), e.getY());
		}
	}

	public void showPopup(final int x, final int y) {
		final TableColumnModel cm = getTable().getColumnModel();
		final int columnIndex = cm.getColumnIndexAtX(x);
		final TableColumn column = cm.getColumn(columnIndex);
		final int columnCount = getTable().getColumnCount();
		final SortTableModel model = (SortTableModel) getTable().getModel();
		final String columnID = (String) column.getIdentifier();

		JMenu popup = new JMenu();
		JMenuItem menuAdjust = new JMenuItem("Adjust Width");
		menuAdjust.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TableUtil.adjustColumnWidth(getTable(), columnIndex, 0, 1000);
			}
		});
		popup.add(menuAdjust);

		JMenuItem menuAdjustAll = new JMenuItem("Adjust All");
		menuAdjustAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < columnCount; i++) {
					TableUtil.adjustColumnWidth(getTable(), i, 0, 1000);
				}
			}
		});
		popup.add(menuAdjustAll);

		popup.add(new Separator());

		JMenuItem menuCollapse = new JMenuItem("Collapse");
		menuCollapse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TableUtil.setColumnWidth(getTable(), columnIndex, 16);
			}
		});
		popup.add(menuCollapse);

		JMenuItem menuCollapseAll = new JMenuItem("Collapse all");
		menuCollapseAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < columnCount; i++) {
					TableUtil.setColumnWidth(getTable(), i, 16);
				}
			}
		});
		popup.add(menuCollapseAll);

		popup.add(new Separator());

		JMenuItem menuDelete = new JMenuItem("Remove Column");
		menuDelete.setEnabled(cm.getColumnCount() != 0);
		menuDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.getHeaderObject(columnID).setVisible(false);
				model.fireTableStructureChanged();
			}
		});
		popup.add(menuDelete);

		JMenu menuAdd = new JMenu("Add");
		menuAdd.setEnabled(cm.getColumnCount() != model.getColumnCount());
		if (menuAdd.isEnabled()) {
			FOR: for (int i = 0; i < model.getColumnCount(); i++) {
				final String id = model.getColumnId(i);
				for (int j = 0; j < cm.getColumnCount(); j++) {
					if (cm.getColumn(j).getIdentifier().equals(id)) {
						continue FOR;
					}
				}

				JMenuItem menuAddColumn = new JMenuItem(model.getColumnName(i));
				menuAddColumn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						model.getHeaderObject(id).setVisible(true);
						model.fireTableStructureChanged();
					}
				});
				menuAdd.add(menuAddColumn);
			}
		}
		popup.add(menuAdd);

		popup.getPopupMenu().show(getTable().getTableHeader(), x, y);
	}

	private void pressMouse(int x, int y) {
		pressedX = x;
		pressedY = y;

		int columnIndex = getTable().getColumnModel().getColumnIndexAtX(x);
		TableColumn tableColumn = getTable().getColumnModel().getColumn(columnIndex);

		if (getTable().getTableHeader().getResizingColumn() != null) {
			pressedX = -1;
			pressedY = -1;
			return;
		}

		SortHeaderObject curHeader = (SortHeaderObject) tableColumn.getHeaderValue();
		curHeader.setPressed(true);

		getTable().getTableHeader().resizeAndRepaint();
	}

	private void releaseMouse(int x, int y) {
		TableColumnModel columnModel = getTable().getColumnModel();
		int columnIndex = columnModel.getColumnIndexAtX(x);
		if (columnIndex != -1) {
			TableColumn tableColumn = columnModel.getColumn(columnIndex);
			SortHeaderObject curHeader = (SortHeaderObject) tableColumn.getHeaderValue();
			curHeader.setPressed(false);

			if (x == pressedX && y == pressedY) {
				if (curHeader.isActive()) {
					curHeader.setRised(!curHeader.isRised());
				}

				SortTableModel model = (SortTableModel) getTable().getModel();
				model.activateHeader(curHeader);
				String columnID = model.getColumnId(tableColumn.getModelIndex());
				model.sort(columnID, !curHeader.isRised());

				// fireTableStructureChanged() - if column has to be invisible after current event
				model.fireTableDataChanged();
			}
		}

		getTable().getTableHeader().resizeAndRepaint();
	}
}
