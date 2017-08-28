package javax.swing.treetable;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

public class TreeTableModelAdapter extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private JTree tree;

	private TreeTableModel treeTableModel;

	public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree) {
		this.tree = tree;
		this.treeTableModel = treeTableModel;

		tree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				rowInserted(event.getPath());
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				rowDeleted(event.getPath());
			}
		});

		treeTableModel.addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeNodesChanged(TreeModelEvent e) {
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				rowInserted(e.getTreePath());
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				rowDeleted(e.getTreePath());
			}

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				rowInserted(e.getTreePath());
			}
		});
	}

	public void rowInserted(TreePath path) {
		int row = tree.getRowForPath(path);
		tree.setSelectionRow(row);
		try {
			fireTableRowsInserted(row + 1, getRowCount()); // row +
			// tree.getModel().getChildCount(event.getPath().getLastPathComponent()));
		} catch (Exception Exc) {
		}
	}

	public void rowDeleted(TreePath path) {
		int row = tree.getRowForPath(path);
		tree.setSelectionRow(row);
		try {
			fireTableRowsDeleted(row + 1, getRowCount()); // row +
			// tree.getModel().getChildCount(event.getPath().getLastPathComponent()));
		} catch (Exception Exc) {
		}
	}

	@Override
	public int getColumnCount() {
		return treeTableModel.getColumnCount();
	}

	@Override
	public String getColumnName(int column) {
		return treeTableModel.getColumnName(column);
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return treeTableModel.getColumnClass(column);
	}

	@Override
	public int getRowCount() {
		return tree.getRowCount();
	}

	protected Object nodeForRow(int row) {
		TreePath treePath = tree.getPathForRow(row);
		if (treePath != null) {
			return treePath.getLastPathComponent();
		} else {
			return null;
		}
	}

	@Override
	public Object getValueAt(int row, int column) {
		return treeTableModel.getValueAt(nodeForRow(row), column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return treeTableModel.isCellEditable(nodeForRow(row), column);
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		treeTableModel.setValueAt(value, nodeForRow(row), column);
		fireTableCellUpdated(row, column);
	}
}
