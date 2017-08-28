package javax.swing.treetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class JTreeTable extends JTable {
	private static final long serialVersionUID = 1L;
	protected JTree tree;

	public JTreeTable(TreeTableModel treeTableModel) {
		setTreeTableModel(treeTableModel);
		init();
	}

	public JTreeTable() {
		super();
		init();
	}

	private TreeTableModel treeTableModel;

	public TreeTableModel getTreeTableModel() {
		return treeTableModel;
	}

	public void setTreeTableModel(TreeTableModel treeTableModel) {
		this.treeTableModel = treeTableModel;
		tree = (JTree) createTreeTableCellRenderer();
		super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

		ListToTreeSelectionModelWrapper selectionWrapper = new ListToTreeSelectionModelWrapper();
		tree.setSelectionModel(selectionWrapper);
		setSelectionModel(selectionWrapper.getListSelectionModel());

		setDefaultRenderer(TreeTableModel.class, (TableCellRenderer) tree);
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
	}

	// return JTree implements TableCellRenderer
	protected TableCellRenderer createTreeTableCellRenderer() {
		return new TreeTableCell(treeTableModel, this);
	}

	private void init() {
		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));
	}

	@Override
	public void setOpaque(boolean opaque) {
		super.setOpaque(opaque);
		if (tree != null) {
			tree.setOpaque(opaque);
		}
	}

	@Override
	public void setBackground(Color background) {
		super.setBackground(background);
		if (tree != null) {
			tree.setBackground(background);
		}
	}

	@Override
	public void setForeground(Color foreground) {
		super.setForeground(foreground);
		if (tree != null) {
			tree.setForeground(foreground);
		}
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (tree != null) {
			tree.setFont(font);
		}
	}

	@Override
	public int getEditingRow() {
		return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		if (tree != null) {
			tree.updateUI();
		}
		LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
	}

	@Override
	public void setRowHeight(int rowHeight) {
		super.setRowHeight(rowHeight);
		if (tree != null && tree.getRowHeight() != rowHeight) {
			tree.setRowHeight(getRowHeight());
		}
	}

	public JTree getTree() {
		return tree;
	}

	public static class TreeTableCell extends JTree implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		private int row;

		private JTreeTable treeTable;

		public TreeTableCell(TreeModel model, JTreeTable treeTable) {
			super(model);
			this.treeTable = treeTable;
		}

		@Override
		public void updateUI() {
			super.updateUI();
			TreeCellRenderer tcr = getCellRenderer();
			if (tcr instanceof DefaultTreeCellRenderer) {
				DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
				dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
				dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
			}
		}

		@Override
		public void setRowHeight(int rowHeight) {
			if (rowHeight > 0) {
				super.setRowHeight(rowHeight);
				if (treeTable != null && treeTable.getRowHeight() != rowHeight) {
					treeTable.setRowHeight(getRowHeight());
				}
			}
		}

		@Override
		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, 0, w, treeTable.getHeight());
		}

		@Override
		public void paint(Graphics g) {
			g.translate(0, -row * getRowHeight());

			super.paint(g);
			if (treeTable.getShowHorizontalLines()) {
				Rectangle r = getBounds();
				g.setColor(Color.black);
				g.drawRect(0, 0, (int) r.getWidth(), treeTable.getHeight());
			}
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			this.row = row;
			setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			setOpaque(isSelected ? true : table.isOpaque());
			return this;
		}
	}

	public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
			return tree;
		}

		@Override
		public boolean isCellEditable(EventObject e) {
			if (e instanceof MouseEvent) {
				for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
					if (getColumnClass(counter) == TreeTableModel.class) {
						MouseEvent me = (MouseEvent) e;
						MouseEvent newME = new MouseEvent(tree, me.getID(), me.getWhen(), me.getModifiers(), me.getX() - getCellRect(0, counter, true).x, me.getY(), me.getClickCount(), me.isPopupTrigger());
						tree.dispatchEvent(newME);
						break;
					}
				}
			}
			return false;
		}
	}

	class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {
		private static final long serialVersionUID = 1L;
		protected boolean updatingListSelectionModel;

		public ListToTreeSelectionModelWrapper() {
			super();
			getListSelectionModel().addListSelectionListener(createListSelectionListener());
		}

		ListSelectionModel getListSelectionModel() {
			return listSelectionModel;
		}

		@Override
		public void resetRowSelection() {
			if (!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				try {
					super.resetRowSelection();
				} finally {
					updatingListSelectionModel = false;
				}
			}
		}

		protected ListSelectionListener createListSelectionListener() {
			return new ListSelectionHandler();
		}

		protected void updateSelectedPathsFromSelectedRows() {
			if (!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				try {
					int min = listSelectionModel.getMinSelectionIndex();
					int max = listSelectionModel.getMaxSelectionIndex();

					clearSelection();
					if (min != -1 && max != -1) {
						for (int counter = min; counter <= max; counter++) {
							if (listSelectionModel.isSelectedIndex(counter)) {
								TreePath selPath = tree.getPathForRow(counter);
								if (selPath != null) {
									addSelectionPath(selPath);
								}
							}
						}
					}
				} finally {
					updatingListSelectionModel = false;
				}
			}
		}

		class ListSelectionHandler implements ListSelectionListener {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateSelectedPathsFromSelectedRows();
			}
		}
	}
}
