package javax.swing.file;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.CenterLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.WComboBox;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;

public class FileComboBox extends WComboBox {
	private TreeListCellRenderer renderer;

	private TreeComboBoxModel model;

	private FileComboBoxEditor editor;

	public FileComboBox() {
		this(null);
	}

	public FileComboBox(FilenameFilter filter) {
		setBackground(Color.white);
		setEditable(true);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray, 1), BorderFactory.createEmptyBorder(1, 1, 1, 1)));

		tree = new FileTree(filter);
		model = new TreeComboBoxModel(tree);
		renderer = new TreeListCellRenderer(tree);
		editor = new FileComboBoxEditor(tree, this);

		setRenderer(renderer);
		setEditor(editor);
		setModel(model);
	}

	private FileTree tree;

	public FileTree getTree() {
		return tree;
	}

	public void collapseAll() {
		for (int i = tree.getRowCount() - 1; i >= 1; i--) {
			tree.collapseRow(i);
		}
		model.update();
		repaint();
	}

	public void expand(int row) {
		TreePath path = tree.getPathForRow(row);
		expand(path);
	}

	public void expand(TreePath path) {
		tree.expandPath(path);
		model.update();
		repaint();
	}

	public void makeVisible(TreePath path) {
		tree.makeVisible(path);
		model.update();
		repaint();
	}

	public File getSelectedFile() {
		Object o = getSelectedItem();
		if (o instanceof File) {
			return (File) o;
		}
		return null;
	}

	public static void main(String[] args) {
		ru.nest.jvg.editor.Util.installDefaultFont();

		FileComboBox cmb = new FileComboBox();
		cmb.setPreferredSize(new Dimension(300, 20));

		cmb.expand(1);

		JPanel pnl = new JPanel();
		pnl.setLayout(new CenterLayout());
		pnl.add(cmb);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setBounds(300, 100, 600, 400);
		f.setContentPane(pnl);
		f.setVisible(true);
	}
}

class TreeComboBoxModel extends AbstractListModel implements ComboBoxModel {
	private JTree tree;

	private Object selectedObject;

	public TreeComboBoxModel(JTree tree) {
		this.tree = tree;
		if (getSize() > 0) {
			selectedObject = getElementAt(0);
		}
	}

	public void update() {
		fireContentsChanged(this, 0, 10000);
	}

	@Override
	public int getSize() {
		return tree.getRowCount();
	}

	@Override
	public Object getElementAt(int index) {
		return tree.getPathForRow(index).getLastPathComponent();
	}

	@Override
	public void setSelectedItem(Object anObject) {
		if ((selectedObject != null && !selectedObject.equals(anObject)) || selectedObject == null && anObject != null) {
			selectedObject = anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	@Override
	public Object getSelectedItem() {
		return selectedObject;
	}
}

class TreeListCellRenderer extends JLabel implements ListCellRenderer {
	private JTree tree;

	public TreeListCellRenderer(JTree tree) {
		this.tree = tree;
		setOpaque(tree.isOpaque());
		setBackground(tree.getBackground());
	}

	private int index;

	private Rectangle bounds;

	private boolean isSelected;

	private boolean cellHasFocus;

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		this.index = index;
		this.isSelected = isSelected;
		this.cellHasFocus = cellHasFocus;

		if (isSelected) {
			tree.setSelectionRow(index);
		} else {
			tree.clearSelection();
		}

		if (index != -1) {
			bounds = tree.getRowBounds(index);
		} else {
			TreePath path = null;
			int selectedIndex = list.getSelectedIndex();
			if (selectedIndex != -1) {
				path = tree.getPathForRow(selectedIndex);
			}
			if (path == null) {
				path = tree.getPathForRow(0);
			}
			bounds = tree.getPathBounds(path);
		}
		Dimension size = new Dimension(bounds.x + bounds.width, bounds.height);
		setPreferredSize(size);
		tree.setSize(size);
		return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.translate(0, -bounds.y);
		tree.print(g);
	}
}

class FileComboBoxEditor implements ComboBoxEditor {
	private JTextField editor;

	private JLabel lblIcon;

	private JPanel pnl;

	private JComboBox comboBox;

	private JTree tree;

	public FileComboBoxEditor(JTree tree, JComboBox comboBox) {
		this.comboBox = comboBox;
		this.tree = tree;

		editor = new JTextField() {
			@Override
			public void setText(String s) {
				if (getText().equals(s)) {
					return;
				}
				super.setText(s);
			}

			@Override
			public void setBorder(Border b) {
			}
		};
		editor.setOpaque(false);

		lblIcon = new JLabel();
		lblIcon.setOpaque(false);
		lblIcon.setText(" ");

		pnl = new JPanel();
		pnl.setOpaque(comboBox.isOpaque());
		pnl.setBackground(comboBox.getBackground());
		pnl.setLayout(new BorderLayout());
		pnl.add(editor, BorderLayout.CENTER);
		pnl.add(lblIcon, BorderLayout.WEST);
	}

	@Override
	public Component getEditorComponent() {
		return pnl;
	}

	private Object oldValue;

	@Override
	public void setItem(Object anObject) {
		if (anObject != null) {
			int index = -1;
			ComboBoxModel model = comboBox.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				if (model.getElementAt(i).equals(anObject)) {
					index = i;
					break;
				}
			}

			if (index == -1) {
				Icon icon = FileTreeCellRenderer.getIcon(anObject, false);
				lblIcon.setIcon(icon);
			} else {
				TreePath path = tree.getPathForRow(index);
				Object node = path.getLastPathComponent();
				JLabel lbl = (JLabel) tree.getCellRenderer().getTreeCellRendererComponent(tree, node, false, false, false, index, false);
				lblIcon.setIcon(lbl.getIcon());
			}

			String text = FileTreeCellRenderer.getText(anObject, true);
			editor.setText(text);

			oldValue = anObject;
		} else {
			editor.setText("");
			lblIcon.setIcon(null);
		}
	}

	@Override
	public Object getItem() {
		String text = editor.getText();
		return new File(text);
	}

	@Override
	public void selectAll() {
		editor.selectAll();
		editor.requestFocus();
	}

	@Override
	public void addActionListener(ActionListener l) {
		editor.addActionListener(l);
	}

	@Override
	public void removeActionListener(ActionListener l) {
		editor.removeActionListener(l);
	}
}
