package javax.swing.file;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import ru.nest.jvg.CommonUtil;

public class ExplorerPanel extends JPanel {
	private FilenameFilter filter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return new File(dir, name).isDirectory();
		}
	};

	private boolean fireAddressEvent = true;

	public ExplorerPanel() {
		tree = new IFileTree(filter);
		list = new IFileList();
		addressComboBox = new FileComboBox(filter);

		JScrollPane scrollTree = new JScrollPane(tree);
		scrollTree.getVerticalScrollBar().setPreferredSize(new Dimension(13, 13));
		scrollTree.setBorder(BorderFactory.createEtchedBorder());
		scrollTree.setPreferredSize(new Dimension(0, 0));
		scrollTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JScrollPane scrollList = new JScrollPane(list);
		scrollList.getHorizontalScrollBar().setPreferredSize(new Dimension(13, 13));
		scrollList.setBorder(BorderFactory.createEtchedBorder());
		scrollList.setPreferredSize(new Dimension(0, 0));

		split = new JSplitPane();
		split.setLeftComponent(scrollTree);
		split.setRightComponent(scrollList);
		split.setDividerLocation(200);
		split.setDividerSize(3);

		addressComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (fireAddressEvent) {
					File file = addressComboBox.getSelectedFile();
					if (file != null) {
						fireAddressEvent = false;
						setSelectedFile(file, false);
						fireAddressEvent = true;
					}
				}
			}
		});

		JPanel pnlAddress = new JPanel();
		pnlAddress.setOpaque(false);
		pnlAddress.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		pnlAddress.setLayout(new BorderLayout());
		pnlAddress.add(new JLabel(" " + System.getProperty("explorer.panel.address", "Address: ")), BorderLayout.WEST);
		pnlAddress.add(addressComboBox, BorderLayout.CENTER);

		// set content
		setLayout(new BorderLayout());
		add(pnlAddress, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
	}

	private JSplitPane split;

	public JSplitPane getSplit() {
		return split;
	}

	private FileComboBox addressComboBox;

	private FileComboBox getAddressComboBox() {
		return addressComboBox;
	}

	private IFileTree tree;

	public FileTree getTree() {
		return tree;
	}

	private IFileList list;

	public IFileList getList() {
		return list;
	}

	public class IFileTree extends FileTree {
		public IFileTree(FilenameFilter filter) {
			super(filter);

			addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					TreePath path = getSelectionPath();
					if (path != null) {
						Object node = path.getLastPathComponent();
						list.setRoot(node instanceof File ? (File) node : null);
					}

					if (isFocusOwner()) {
						list.clearSelection();
					}
					_setSelectedFile(_getSelectedFile());

					if (fireAddressEvent) {
						addressComboBox.expand(path);
						addressComboBox.getEditor().setItem(path.getLastPathComponent());
					}
				}
			});
		}

		@Override
		protected void filePressed(MouseEvent e, TreePath path) {
			list.clearSelection();
			_setSelectedFile(_getSelectedFile());
		}
	}

	class IFileList extends FileList {
		public IFileList() {
			addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						File file = _getSelectedFile();
						updateTreeSelection();
						_setSelectedFile(file);

						if (fireAddressEvent) {
							addressComboBox.expand(tree.getSelectionPath());
							addressComboBox.getEditor().setItem(file.isDirectory() ? file : file.getParentFile());
						}
					}
				}
			});
		}

		@Override
		public boolean openSelected() {
			if (super.openSelected()) {
				updateTreeSelection();
				_setSelectedFile(_getSelectedFile());
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean moveToUp() {
			if (super.moveToUp()) {
				updateTreeSelection();
				_setSelectedFile(_getSelectedFile());
				return true;
			} else {
				return false;
			}
		}

		public void updateTreeSelection() {
			FileListModel model = (FileListModel) getModel();
			TreePath path = tree.getPath(model.getRoot());
			tree.makeVisible(path);
			tree.setSelectionPath(path);
			tree.scrollPathToVisible(path);
		}
	}

	private File _getSelectedFile() {
		File file = list.getSelectedFile();
		if (file != null) {
			return file;
		}

		TreePath path = tree.getSelectionPath();
		if (path != null) {
			Object node = path.getLastPathComponent();
			if (node instanceof File) {
				return (File) node;
			}
		}

		return null;
	}

	private File selectedFile;

	public File getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(File file) {
		setSelectedFile(file, true);
	}

	public void setSelectedFile(File file, boolean parentToTree) {
		FileListModel model = (FileListModel) list.getModel();
		if (file != null) {
			if (parentToTree || !file.isDirectory()) {
				model.setRoot(file.getParentFile());
				list.setSelectedValue(file, true);

				TreePath path = tree.getPath(file).getParentPath();
				tree.makeVisible(path);
				tree.setSelectionPath(path);
				tree.scrollPathToVisible(path);
			} else {
				model.setRoot(file);
				list.clearSelection();

				TreePath path = tree.getPath(file);
				tree.makeVisible(path);
				tree.setSelectionPath(path);
				tree.scrollPathToVisible(path);
			}
		} else {
			model.setRoot(file);
			list.clearSelection();

			TreePath path = tree.getPath(file);
			tree.setSelectionPath(path);
		}

		_setSelectedFile(file);
	}

	public void clearSelection() {
		tree.clearSelection();
		list.clearSelection();

		_setSelectedFile(null);
	}

	private void _setSelectedFile(File file) {
		if (!CommonUtil.equals(selectedFile, file)) {
			fireFileSelected(file);
			selectedFile = file;
		}
	}

	public void fileSelected(File file) {
		System.out.println("selected: " + file);

		TreePath path = tree.getPath(file);
		if (list.getSelectedFile() != null) {
			TreePath parentPath = path.getParentPath();
			if (parentPath != null) {
				path = parentPath;
			}
		}

		addressComboBox.collapseAll();
		addressComboBox.makeVisible(path);
	}

	// listeners
	private List<Listener> listeners = new ArrayList<>();

	public void addListener(Listener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeListener(Listener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected void fireFileSelected(File file) {
		fileSelected(file);

		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.fileSelected(file);
			}
		}
	}

	public interface Listener {
		void fileSelected(File file);
	}

	public static void main(String[] args) {
		ru.nest.jvg.editor.Util.installDefaultFont();

		ExplorerPanel ex = new ExplorerPanel();
		ex.getList().showOnlyFiles();

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setBounds(300, 100, 600, 400);
		f.setContentPane(ex);
		f.setVisible(true);
	}
}
