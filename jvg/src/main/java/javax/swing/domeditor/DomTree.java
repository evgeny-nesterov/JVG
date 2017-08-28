package javax.swing.domeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SliderField;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class DomTree extends JTree {
	private DomTreeModel model;

	public DomTree(Element root) {
		model = new DomTreeModel(root);
		setModel(model);
		setCellRenderer(new DomTreeRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	public Element getSelectedElement() {
		TreePath selectedPath = getSelectionPath();
		Element selectedElement = null;
		if (selectedPath != null) {
			selectedElement = (Element) selectedPath.getLastPathComponent();
		}
		return selectedElement;
	}

	public static void main(String[] args) {
		try {
			ru.nest.jvg.editor.Util.installDefaultFont();

			InputStreamReader reader = new InputStreamReader(new FileInputStream("c:/example.xml"), "UTF8");
			SAXBuilder builder = new SAXBuilder();
			Document xml = builder.build(reader);
			Element rootElement = xml.getRootElement();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setBounds(200, 100, 600, 600);
			f.getContentPane().setLayout(new BorderLayout());
			SliderField s = new SliderField(0, 100, 25);
			s.setPreferredSize(new Dimension(30, 18));
			f.getContentPane().add(new JScrollPane(new DomTree(rootElement)));
			f.setVisible(true);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
