package ru.nest.swing.domeditor;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import ru.nest.swing.SliderField;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class DomEditor extends JPanel {
	private DomTree tree;

	private ElementEditor editor;

	private JSplitPane split;

	public DomEditor(Element root) {
		setTree(new DomTree(root));
		setEditor(new ElementEditor());

		split = new JSplitPane();
		split.setDividerLocation(150);
		split.setDividerSize(5);
		split.setLeftComponent(new JScrollPane(getTree()));
		split.setRightComponent(new JScrollPane(getEditor()));

		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);

		getTree().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Element selectedElement = getTree().getSelectedElement();
				getEditor().setElement(selectedElement);
			}
		});
	}

	public void setTree(DomTree tree) {
		this.tree = tree;
	}

	public DomTree getTree() {
		return tree;
	}

	public void setEditor(ElementEditor editor) {
		this.editor = editor;
	}

	public ElementEditor getEditor() {
		return editor;
	}

	public Element getRootElement() {
		return (Element) tree.getModel().getRoot();
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
			f.getContentPane().add(new JScrollPane(new DomEditor(rootElement)), BorderLayout.CENTER);
			f.setVisible(true);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
