package ru.nest.jvg.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.domeditor.DomEditor;

import org.jdom2.Attribute;
import org.jdom2.Element;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGParseException;

public class JVGDomEditor extends JDialog {
	public final static int OPTION_OK = 0;

	public final static int OPTION_CANCEL = 1;

	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private JPanel pnlEditor = new JPanel();

	private DomEditor domEditor;

	private JPanel pnlManage = new JPanel();

	private JVGEditor editor;

	private JVGEditPane pane;

	public JVGDomEditor() {
		this(null, null);
	}

	public JVGDomEditor(final JVGEditor editor, final JVGEditPane pane) {
		this.editor = editor;
		this.pane = pane;

		JPanel pnl = null;
		try {
			setTitle(lm.getValue("dom.editor.title", "Edit DOM"));
			setIconImage(new ImageIcon(JVGDomEditor.class.getResource("img/xml.png")).getImage());

			JVGBuilder builder = JVGBuilder.create(pane.getDocumentFormat());
			Element rootElement = builder.build(pane.getRoot().getChildren());

			domEditor = new DomEditor(rootElement);

			pnlEditor.setLayout(new BorderLayout());
			pnlEditor.add(domEditor, BorderLayout.CENTER);

			JButton btnUpdate = null;
			if (editor != null && pane != null) {
				btnUpdate = new JButton(lm.getValue("dom.editor.button.update", "Update"));
				btnUpdate.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						update();
					}
				});
			}

			JButton btnApply = null;
			if (editor != null && pane != null) {
				btnApply = new JButton(lm.getValue("dom.editor.button.apply", "Apply"));
				btnApply.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						apply();
						update();
					}
				});
			}

			JButton btnOK = new JButton(lm.getValue("dom.editor.button.ok", "OK"));
			btnOK.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
					apply();
				}
			});

			JButton btnClose = new JButton(lm.getValue("dom.editor.button.close", "Close"));
			btnClose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});

			pnlManage.setLayout(new FlowLayout(FlowLayout.CENTER));
			if (btnUpdate != null) {
				pnlManage.add(btnUpdate);
			}
			if (btnApply != null) {
				pnlManage.add(btnApply);
			}
			pnlManage.add(btnOK);
			pnlManage.add(btnClose);

			pnl = new JPanel();
			pnl.setLayout(new BorderLayout());
			pnl.add(pnlEditor, BorderLayout.CENTER);
			pnl.add(pnlManage, BorderLayout.SOUTH);
		} catch (JVGParseException exc) {
			exc.printStackTrace();
		}

		domEditor.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					Element selectedElement = domEditor.getTree().getSelectedElement();
					while (selectedElement != null && !"component".equals(selectedElement.getName())) {
						selectedElement = (Element) selectedElement.getParent();
					}

					if (selectedElement == null) {
						return;
					}

					try {
						JVGPaneInternalFrame window = pane.getWindow();
						window.toFront();

						pane.unselectAll();

						String idValue = selectedElement.getAttributeValue("id");
						Long id = Long.parseLong(idValue);
						JVGComponent componentToSelect = pane.getRoot().findByID(id);
						if (componentToSelect != null) {
							pane.getSelectionManager().setSelection(componentToSelect);
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		});

		setBounds(0, 0, 600, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		setContentPane(pnl);
	}

	public void apply() {
		if (editor != null && pane != null) {
			try {
				editor.load(pane, domEditor.getRootElement());
			} catch (JVGParseException exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(JVGDomEditor.this, lm.getValue("dom.editor.error", "Error") + ": \n" + exc.getMessage(), lm.getValue("dom.editor.message.cant.apply", "Can't Apply"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void update() {
		try {
			JVGBuilder builder = JVGBuilder.create(pane.getDocumentFormat());
			Element rootElement = builder.build(pane.getRoot().getChildren());

			copy(rootElement, domEditor.getRootElement());
			domEditor.getTree().repaint();

			Element selectedElement = domEditor.getTree().getSelectedElement();
			domEditor.getEditor().setElement(selectedElement);
		} catch (JVGParseException exc) {
			exc.printStackTrace();
		}
	}

	public void copy(Element e1, Element e2) {
		List l1 = e1.getAttributes();
		List l2 = e2.getAttributes();
		for (int i = 0; i < l1.size(); i++) {
			Attribute a1 = (Attribute) l1.get(i);
			Attribute a2 = (Attribute) l2.get(i);

			a2.setName(a1.getName());
			a2.setValue(a1.getValue());
		}

		List cs1 = e1.getChildren();
		List cs2 = e2.getChildren();
		int childsCount = Math.min(cs1.size(), cs2.size());
		for (int i = 0; i < childsCount; i++) {
			Element c1 = (Element) cs1.get(i);
			Element c2 = (Element) cs2.get(i);
			copy(c1, c2);
		}
	}

	public JPanel getOptionPanel() {
		return pnlManage;
	}

	private int option = OPTION_CANCEL;

	public int getOption() {
		return option;
	}

	public Element getRootElement() {
		return domEditor.getRootElement();
	}
}
