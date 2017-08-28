package javax.swing.domeditor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdom2.Attribute;
import org.jdom2.Element;

public class ElementEditor extends JPanel {
	private Element e;

	public ElementEditor() {
		setLayout(new GridBagLayout());
		setBackground(Color.white);
	}

	public Element getElement() {
		return e;
	}

	public JComponent getEditor(final Attribute a) {
		final JTextField editor = new JTextField(a.getValue());
		editor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				a.setValue(editor.getText());
			}
		});
		editor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				a.setValue(editor.getText());
			}
		});
		return editor;
	}

	public JComponent getTextEditor(final Element element) {
		final JTextArea editor = new JTextArea(element.getText());
		editor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				save();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				save();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			public void save() {
				element.setText(editor.getText());
			}
		});

		JScrollPane scroll = new JScrollPane(editor);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		return scroll;
	}

	public void setElement(Element e) {
		removeAll();

		this.e = e;
		if (e != null) {
			int y = 0;
			for (Object o : e.getAttributes()) {
				Attribute a = (Attribute) o;
				add(new JLabel(a.getName()), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 10, 1, 5), 0, 0));

				add(getEditor(a), new GridBagConstraints(1, y, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 5, 1, 10), 0, 0));
				y++;
			}

			add(getTextEditor(e), new GridBagConstraints(0, y, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		}

		validate();
		repaint();
	}
}
