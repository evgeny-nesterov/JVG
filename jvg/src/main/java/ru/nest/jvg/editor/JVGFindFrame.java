package ru.nest.jvg.editor;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.swing.IconButton;
import ru.nest.swing.WComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class JVGFindFrame extends JInternalFrame implements ActionListener {
	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private WComboBox cmbPattern;

	private JCheckBox chkRegexp;

	private JCheckBox chkMatchCase;

	private JCheckBox chkWholeWord;

	private JCheckBox chkCurrentDocument;

	private JVGEditor editor;

	// TODO
	private JTable resultsTable;

	public JVGFindFrame(JVGEditor editor) {
		this.editor = editor;

		setTitle(lm.getValue("search.title", "Find objects by name"));

		cmbPattern = new WComboBox();
		cmbPattern.setBackground(Color.gray);
		cmbPattern.setEditable(true);
		cmbPattern.setPreferredSize(new Dimension(200, 18));
		cmbPattern.setActionCommand("option-ok");
		cmbPattern.addActionListener(this);
		cmbPattern.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
				}
			}
		});
		cmbPattern.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
				}
			}
		});

		chkRegexp = new JCheckBox(lm.getValue("search.checkbox.regexp", "Regexp"));
		chkRegexp.setRequestFocusEnabled(false);
		chkRegexp.setOpaque(false);

		chkMatchCase = new JCheckBox(lm.getValue("search.checkbox.match-case", "Match case"));
		chkMatchCase.setRequestFocusEnabled(false);
		chkMatchCase.setOpaque(false);

		chkWholeWord = new JCheckBox(lm.getValue("search.checkbox.whole-word", "Whole word"));
		chkWholeWord.setRequestFocusEnabled(false);
		chkWholeWord.setOpaque(false);

		chkCurrentDocument = new JCheckBox(lm.getValue("search.checkbox.current-document", "Current document"));
		chkCurrentDocument.setRequestFocusEnabled(false);
		chkCurrentDocument.setOpaque(false);
		chkCurrentDocument.setSelected(true);

		IconButton btnFind = new IconButton(new ImageIcon(JVGFindFrame.class.getResource("img/find.gif")), new Insets(0, 6, 0, 0));
		btnFind.setRequestFocusEnabled(false);
		btnFind.setActionCommand("find");
		btnFind.addActionListener(this);

		resultsTable = new JTable();

		// panels
		JPanel pnlPattern = new JPanel();
		pnlPattern.setOpaque(false);
		pnlPattern.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		pnlPattern.setLayout(new BorderLayout());
		pnlPattern.add(new JLabel(lm.getValue("search.label.text-to-find", "Text to find: ")), BorderLayout.WEST);
		pnlPattern.add(cmbPattern, BorderLayout.CENTER);
		pnlPattern.add(btnFind, BorderLayout.EAST);

		JPanel pnlOptions = new JPanel();
		pnlOptions.setOpaque(false);
		pnlOptions.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), lm.getValue("search.panel.options", "Options")));
		pnlOptions.setLayout(new GridBagLayout());
		pnlOptions.add(chkRegexp, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlOptions.add(chkMatchCase, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 0, 0));
		pnlOptions.add(chkWholeWord, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlOptions.add(chkCurrentDocument, new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));

		JPanel pnlResults = new JPanel();
		pnlResults.setOpaque(false);
		pnlResults.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

		JPanel pnlContent = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				Util.paintFormBackground(g, getWidth(), getHeight());
				super.paintComponent(g);
			}
		};
		pnlContent.setOpaque(false);
		pnlContent.setLayout(new BorderLayout());
		pnlContent.add(pnlPattern, BorderLayout.NORTH);
		pnlContent.add(pnlOptions, BorderLayout.CENTER);
		pnlContent.add(pnlResults, BorderLayout.SOUTH);

		setContentPane(pnlContent);
	}

	private void find() {
		JVGPaneInternalFrame[] frames = null;
		boolean isCurrentDocument = chkCurrentDocument.isSelected();
		if (isCurrentDocument) {
			JVGPaneInternalFrame frame = editor.getCurrentFrame();
			if (frame != null) {
				frames = new JVGPaneInternalFrame[] { frame };
			}
		} else {
			frames = editor.getAllDocuments();
		}

		if (frames != null && frames.length > 0) {
			String pattern = cmbPattern.getEditor().getItem().toString();
			if (cmbPattern.getSelectedIndex() == -1) {
				cmbPattern.addItem(pattern);
			}

			boolean isRegexp = chkRegexp.isSelected();
			boolean isMatchCase = chkMatchCase.isSelected();
			boolean isWholeWord = chkWholeWord.isSelected();

			for (JVGPaneInternalFrame frame : frames) {
				JVGPane pane = frame.getPane();
				JVGComponent[] result = pane.getRoot().find(pattern, isMatchCase, isWholeWord, isRegexp);
				JVGSelectionModel selectionManager = pane.getSelectionManager();

				if (result.length > 0) {
					frame.toFront();
					selectionManager.setSelection(result);
					frame.scrollSelectionToVisible();
				} else {
					selectionManager.clearSelection();
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("option-ok".equals(cmd)) {
			find();
			setVisible(false);
		} else if ("option-cancel".equals(cmd)) {
			setVisible(false);
		} else if ("find".equals(cmd)) {
			find();
		}
	}
}
