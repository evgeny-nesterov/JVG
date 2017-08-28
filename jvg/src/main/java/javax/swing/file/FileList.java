package javax.swing.file;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

public class FileList extends JList {
	private FileListModel model;

	private FileListCellRenderer renderer;

	public FileList() {
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setFixedCellHeight(18);
		setVisibleRowCount(0);
		setLayoutOrientation(JList.VERTICAL_WRAP);

		model = new FileListModel();
		setModel(model);

		renderer = new FileListCellRenderer();
		setCellRenderer(renderer);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getClickCount() == 2) {
						openSelected();
					}
				}
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					openSelected();
				} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					deleteSelected();
				} else if (e.getKeyCode() == KeyEvent.VK_F2) {
					editCell();
				} else if (e.getKeyCode() == KeyEvent.VK_F5) {
					refresh();
				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					moveToUp();
				} else {
					char keyChar = Character.toLowerCase(e.getKeyChar());
					selectNextCell(keyChar);
				}
			}
		});
	}

	public void setIconByExtention(IconByExtention iconByExtention) {
		renderer.setIconByExtention(iconByExtention);
	}

	public FilenameFilter getFilter() {
		return model.getFilter();
	}

	public void setFilter(FilenameFilter filter) {
		model.setFilter(filter);
	}

	public void showOnlyFiles() {
		setFilter(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (dir != null) {
					File file = name != null ? new File(dir, name) : dir;
					return file.isFile();
				} else {
					return true;
				}
			}
		});
	}

	public void showAll() {
		setFilter(null);
	}

	public File getSelectedFile() {
		return (File) getSelectedValue();
	}

	public void refresh() {
		model.refresh();
		repaint();
	}

	private HashMap<String, Integer> indexes = new HashMap<String, Integer>();

	public boolean openSelected() {
		String rootPath = model.getRoot() != null ? model.getRoot().getAbsolutePath() : null;
		int selectedIndex = getSelectedIndex();
		if (open(selectedIndex)) {
			indexes.put(rootPath, selectedIndex);

			if (model.getSize() > 0) {
				setSelectedIndex(0);
			} else {
				clearSelection();
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean open(int index) {
		if (model.open(index)) {
			repaint();
			return true;
		} else {
			return false;
		}
	}

	public boolean moveToUp() {
		if (model.moveToUp()) {
			int selectedIndex = 0;
			String rootPath = model.getRoot() != null ? model.getRoot().getAbsolutePath() : null;
			if (indexes.containsKey(rootPath)) {
				selectedIndex = indexes.get(rootPath);
			}
			setSelectedIndex(selectedIndex);
			repaint();
			return true;
		} else {
			return false;
		}
	}

	public void delete(int index) {
		model.delete(index);
	}

	public void setFile(int index, File file) {
		model.getFiles()[index] = file;
	}

	public void setRoot(File root) {
		model.setRoot(root);
	}

	public void update(int index) {
		model.update(index);
	}

	private int editingRow = -1;

	public int getEditingRow() {
		return editingRow;
	}

	private JTextField txtEditor = null;

	public JTextField getEditorComponent() {
		return txtEditor;
	}

	public void editCell(final int index) {
		if (index >= 0 && index < model.getSize()) {
			if (index == editingRow) {
				return;
			}

			final File file = (File) model.getElementAt(index);
			JLabel lbl = (JLabel) getCellRenderer().getListCellRendererComponent(this, file, index, false, false);
			Rectangle b = getUI().getCellBounds(this, index, index);

			removeCellEditor();

			txtEditor = new JTextField();
			txtEditor.setText(lbl.getText());
			txtEditor.setHorizontalAlignment(lbl.getHorizontalAlignment());
			txtEditor.setFont(lbl.getFont());
			txtEditor.setBounds(b.x + lbl.getIcon().getIconWidth() + lbl.getIconTextGap() - 1, b.y, b.width - lbl.getIcon().getIconWidth() - lbl.getIconTextGap() + 2, b.height);

			txtEditor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String text = txtEditor.getText();
					File newFile = new File(file.getParent(), text);

					file.renameTo(newFile);
					setFile(index, newFile);

					removeCellEditor();
					update(index);
				}
			});

			txtEditor.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					removeCellEditor();
				}
			});

			txtEditor.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						removeCellEditor();
					}
				}
			});
			txtEditor.selectAll();

			add(txtEditor);
			revalidate();
			repaint();
			txtEditor.requestFocus();
			editingRow = index;
		}
	}

	public void removeCellEditor() {
		if (txtEditor != null) {
			remove(txtEditor);
			txtEditor = null;
			editingRow = -1;

			revalidate();
			repaint();
		}
	}

	private void deleteSelected() {
		final File file = (File) getSelectedValue();
		if (file != null) {
			int index = getSelectedIndex();
			if (JOptionPane.showConfirmDialog(this, System.getProperty("file.list.message.delete", "Confirm"), System.getProperty("file.list.message.delete.title", "Delete file"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
				if (file.delete()) {
					delete(index);
					repaint();
				} else {
					JOptionPane.showMessageDialog(this, System.getProperty("file.list.message.cant-delete", "File is not deleted"), System.getProperty("file.list.message.cant-delete.title", "Can't delete"), JOptionPane.WARNING_MESSAGE);
				}
			}
		}
	}

	public void editCell() {
		int index = getSelectedIndex();
		if (index != -1) {
			editCell(index);
		}
	}

	public void selectNextCell(char keyChar) {
		for (int i = 0; i < model.getSize(); i++) {
			int index = (i + getSelectedIndex() + 1) % model.getSize();

			File file = (File) model.getElementAt(index);
			String name = file.getName();
			if (name.length() > 0) {
				char firstChar = Character.toLowerCase(name.charAt(0));
				if (firstChar == keyChar) {
					setSelectedIndex(index);
					return;
				}
			}
		}
	}

	public static void main(String[] args) {
		ru.nest.jvg.editor.Util.installDefaultFont();

		FileList list = new FileList();
		list.setRoot(null);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setBounds(300, 100, 600, 400);
		f.setContentPane(new JScrollPane(list));
		f.setVisible(true);
	}
}
