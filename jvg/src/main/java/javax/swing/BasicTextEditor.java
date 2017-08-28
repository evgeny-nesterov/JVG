package javax.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.menu.WMenu;
import javax.swing.menu.WMenuItem;
import javax.swing.menu.WSeparator;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import ru.nest.jvg.editor.resources.JVGLocaleManager;

/**
 * CTRL+X - cut action, CTRL+C - copy action, CTRL+V - paste action, CTRL+F - find action, CTRL+R - replace action, CTRL+G - go to line action,
 * CTRL+Z - undo action, CTRL+SHIFT+Z - redo action. Find and replace actions shows the same dialog but pressing enter perform corresponding
 * action. Created on Feb 27, 2007
 * 
 * @author E.Nesterov
 */
public class BasicTextEditor extends JTextPane implements UndoableEditListener {
	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private static Vector<String> searchList = new Vector<String>();

	private static Vector<String> replaceList = new Vector<String>();

	private UndoManager undo = new UndoManager();

	public BasicTextEditor() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					showPopup(e.getX(), e.getY());
				}
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_F3) {
					findNext(true);
				}
			}
		});

		undo.setLimit(10000);
		addKeymapBindings();

		// set not opaque to view selected row
		setOpaque(false);

		getCaret().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int caretPosition = getCaretPosition();
				int newLine = getLine(caretPosition);
				if (line != newLine) {
					line = newLine;
					repaint();
				}
			}
		});
	}

	private boolean undoRedoActivated = false;

	public void activateUndoRedo() {
		if (!undoRedoActivated) {
			getDocument().addUndoableEditListener(this);
			undoRedoActivated = true;
		}
	}

	private int line = -1;

	private final static Color rowColor = new Color(245, 245, 245);

	@Override
	public void paintComponent(Graphics g) {
		// component is opaque
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		// draw row
		int caretPosition = getCaretPosition();
		try {
			Rectangle r = modelToView(caretPosition);
			g.setColor(rowColor);
			g.fillRect(0, r.y, getWidth(), r.height);
		} catch (BadLocationException exc) {
		}

		super.paintComponent(g);
	}

	private Action cutAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			cut();
		}
	};

	private Action copyAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			copy();
		}
	};

	private Action pasteAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			paste();
		}
	};

	private Action findAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			find(false);
		}
	};

	private Action replaceAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			find(true);
		}
	};

	private Action gotoAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			goToLine();
		}
	};

	private UndoAction undoAction = new UndoAction();

	private RedoAction redoAction = new RedoAction();

	private KeyStroke findKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);

	private KeyStroke replaceKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK);

	private KeyStroke gotoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK);

	private KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);

	private KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK | Event.SHIFT_MASK);

	protected void addKeymapBindings() {
		Keymap keymap = JTextComponent.addKeymap("MyBindings", getKeymap());

		keymap.addActionForKeyStroke(findKeyStroke, findAction);
		keymap.addActionForKeyStroke(replaceKeyStroke, replaceAction);
		keymap.addActionForKeyStroke(gotoKeyStroke, gotoAction);
		keymap.addActionForKeyStroke(undoKeyStroke, undoAction);
		keymap.addActionForKeyStroke(redoKeyStroke, redoAction);

		setKeymap(keymap);
	}

	public void showPopup(int x, int y) {
		WMenu popup = new WMenu();

		int start = getSelectionStart();
		int end = getSelectionEnd();
		boolean thereIsSelection = start != end;

		WMenuItem menu = new WMenuItem(lm.getValue("editor.text.button.cut", "Cut"));
		menu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
		menu.addActionListener(cutAction);
		menu.setEnabled(thereIsSelection);
		popup.add(menu);

		menu = new WMenuItem(lm.getValue("editor.text.button.copy", "Copy"));
		menu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
		menu.addActionListener(copyAction);
		menu.setEnabled(thereIsSelection);
		popup.add(menu);

		menu = new WMenuItem(lm.getValue("editor.text.button.paste", "Paste"));
		menu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
		menu.addActionListener(pasteAction);
		popup.add(menu);

		popup.add(new WSeparator());

		menu = new WMenuItem(lm.getValue("editor.text.button.find", "Find"));
		menu.setAccelerator(findKeyStroke);
		menu.addActionListener(findAction);
		popup.add(menu);

		menu = new WMenuItem(lm.getValue("editor.text.button.replace", "Replace"));
		menu.setAccelerator(replaceKeyStroke);
		menu.addActionListener(replaceAction);
		popup.add(menu);

		menu = new WMenuItem(lm.getValue("editor.text.button.goto", "Go to Line"));
		menu.setAccelerator(gotoKeyStroke);
		menu.addActionListener(gotoAction);
		popup.add(menu);

		popup.add(new WSeparator());

		menu = new WMenuItem(lm.getValue("editor.text.button.undo", "Undo"));
		menu.setAccelerator(undoKeyStroke);
		menu.addActionListener(undoAction);
		menu.setEnabled(undo.canUndo());
		popup.add(menu);

		menu = new WMenuItem(lm.getValue("editor.text.button.redo", "Redo"));
		menu.setAccelerator(redoKeyStroke);
		menu.addActionListener(redoAction);
		menu.setEnabled(undo.canRedo());
		popup.add(menu);

		popup.getPopupMenu().show(this, x, y);
	}

	private void requestFocus(final JTextField txt) {
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(100);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							txt.requestFocus();
							txt.selectAll();
						}
					});
				} catch (InterruptedException exc) {
				}
			}
		}.start();
	}

	private String searchText = "";

	private String replaceText = "";

	public void find(final boolean isReplace) {
		String selectedText = getSelectedText();

		final WComboBox txtFind = new WComboBox(searchList);
		txtFind.setBackground(Color.gray);
		txtFind.setEditable(true);
		txtFind.setPreferredSize(new Dimension(200, 20));
		txtFind.setSelectedItem((selectedText != null && selectedText.length() > 0) ? selectedText : searchText);

		final WComboBox txtReplace = new WComboBox(replaceList);
		txtReplace.setBackground(Color.gray);
		txtReplace.setEditable(true);
		txtReplace.setPreferredSize(new Dimension(200, 20));
		txtReplace.setSelectedItem(replaceText);

		final JDialog dialog = new JDialog();

		JPanel pnl = new JPanel();
		pnl.setLayout(new GridBagLayout());
		pnl.add(new JLabel(lm.getValue("editor.text.find.text", "Text to find: ")), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 5, 3, 0), 1, 0));
		pnl.add(txtFind, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(3, 0, 3, 0), 1, 0));
		pnl.add(new JLabel(lm.getValue("editor.text.find.replace", "Replace with: ")), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 5, 3, 0), 1, 0));
		pnl.add(txtReplace, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(3, 0, 3, 0), 1, 0));

		JButton btnFind = new JButton(lm.getValue("editor.text.find.button.find", "Find"));
		if (!isReplace) {
			btnFind.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.darkGray), btnFind.getBorder()));
		}
		final ActionListener findAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String txt = (String) txtFind.getEditor().getItem();
				if (txt.length() == 0) {
					JOptionPane.showMessageDialog(dialog, lm.getValue("editor.text.find.error.message", "Can not search for a blank string"), lm.getValue("editor.text.find.error", "Search Error"), JOptionPane.ERROR_MESSAGE);
					JTextField textField = (JTextField) txtFind.getEditor().getEditorComponent();
					textField.selectAll();
					textField.requestFocus();
					return;
				}

				dialog.dispose();
				find(txt);
			}
		};
		btnFind.addActionListener(findAction);
		pnl.add(btnFind, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 5, 3, 5), 1, 0));

		JButton btnReplace = new JButton(lm.getValue("editor.text.find.button.replace", "Replace"));
		if (isReplace) {
			btnReplace.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.darkGray), btnReplace.getBorder()));
		}
		final ActionListener replaceAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String txt = (String) txtFind.getEditor().getItem();
				if (txt.length() == 0) {
					JOptionPane.showMessageDialog(dialog, lm.getValue("editor.text.find.error.message", "Can not search for a blank string"), lm.getValue("editor.text.find.error", "Search Error"), JOptionPane.ERROR_MESSAGE);
					JTextField textField = (JTextField) txtFind.getEditor().getEditorComponent();
					textField.selectAll();
					textField.requestFocus();
					return;
				}

				dialog.dispose();
				replace(txt, (String) txtReplace.getEditor().getItem());
			}
		};
		btnReplace.addActionListener(replaceAction);
		pnl.add(btnReplace, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 5, 3, 5), 1, 0));

		JButton btnCancel = new JButton(lm.getValue("editor.text.find.button.cancel", "Cancel"));
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		pnl.add(btnCancel, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 5, 3, 5), 1, 0));

		if (isReplace) {
			txtFind.getEditor().addActionListener(replaceAction);
			txtReplace.getEditor().addActionListener(replaceAction);
		} else {
			txtFind.getEditor().addActionListener(findAction);
			txtReplace.getEditor().addActionListener(findAction);
		}

		dialog.setTitle(lm.getValue("editor.text.find.title", "Find Text"));
		dialog.setContentPane(pnl);
		dialog.setAlwaysOnTop(true);
		dialog.setModal(true);

		dialog.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setLocation((screenSize.width - dialog.getWidth()) / 2, (screenSize.height - dialog.getHeight()) / 2);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				JTextField textField = (JTextField) txtFind.getEditor().getEditorComponent();
				textField.selectAll();
				textField.requestFocus();
			}
		});
		dialog.setVisible(true);
	}

	public void find(String searchText) {
		this.searchText = searchText;

		if (!searchList.contains(searchText)) {
			searchList.add(searchText);
		}

		setCaretPosition(0);
		findNext(true);
	}

	public void replace(String searchText, final String replaceText) {
		this.searchText = searchText;
		this.replaceText = replaceText;

		if (!searchList.contains(searchText)) {
			searchList.add(searchText);
		}

		if (!replaceList.contains(replaceText)) {
			replaceList.add(replaceText);
		}

		setCaretPosition(0);

		if (findNext(false)) {
			final JDialog dialog = new JDialog();

			JButton btnYes = new JButton(lm.getValue("editor.text.replace.button.yes", "Yes"));
			btnYes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int start = getSelectionStart();
					int end = getSelectionEnd();
					try {
						getDocument().remove(start, end - start);
						getDocument().insertString(start, replaceText, null);
					} catch (BadLocationException exc) {
					}

					if (!findNext(false)) {
						dialog.dispose();
					}
				}
			});

			JButton btnNo = new JButton(lm.getValue("editor.text.replace.button.no", "No"));
			btnNo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!findNext(false)) {
						dialog.dispose();
					}
				}
			});

			JButton btnAll = new JButton(lm.getValue("editor.text.replace.button.all", "All"));
			btnAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					do {
						int start = getSelectionStart();
						int end = getSelectionEnd();
						try {
							getDocument().remove(start, end - start);
							getDocument().insertString(start, replaceText, null);
						} catch (BadLocationException exc) {
						}
					} while (findNext(false));

					dialog.dispose();
				}
			});

			JButton btnDone = new JButton(lm.getValue("editor.text.replace.button.done", "Done"));
			btnDone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});

			JPanel pnlManage = new JPanel();
			pnlManage.setLayout(new GridLayout(1, 4, 10, 10));
			pnlManage.add(btnYes);
			pnlManage.add(btnNo);
			pnlManage.add(btnAll);
			pnlManage.add(btnDone);

			JPanel pnl = new JPanel();
			pnl.setLayout(new BorderLayout());
			JLabel lbl = new JLabel(lm.getValue("editor.text.replace.message.replace.occurence", "Replace this occurence of") + " '" + searchText + "'?");
			lbl.setPreferredSize(new Dimension(300, 50));
			lbl.setVerticalTextPosition(SwingConstants.TOP);
			lbl.setHorizontalTextPosition(SwingConstants.LEFT);
			pnl.add(lbl, BorderLayout.CENTER);
			pnl.add(pnlManage, BorderLayout.SOUTH);

			dialog.setTitle(lm.getValue("editor.text.replace.message.confirm", "Confirm replace"));
			dialog.setContentPane(pnl);
			dialog.setAlwaysOnTop(true);
			dialog.setModal(true);
			dialog.pack();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			dialog.setLocation((screenSize.width - dialog.getWidth()) / 2, (screenSize.height - dialog.getHeight()) / 2);
			dialog.setVisible(true);
		}
	}

	public boolean findNext(boolean cyclic) {
		if (searchText.length() > 0) {
			int search_index = getCaretPosition();
			if (search_index >= 0) {
				int length = getDocument().getLength();
				try {
					int index = getDocument().getText(0, length).indexOf(searchText, search_index);
					if (index == -1) {
						if (search_index > 0) {
							if (cyclic) {
								if (search_index < length) {
									setCaretPosition(length);
								} else {
									setCaretPosition(0);
									return findNext(cyclic);
								}
							}
						} else {
							JOptionPane.showMessageDialog(this, lm.getValue("editor.text.find.next.string", "String") + " '" + searchText + "' " + lm.getValue("editor.text.find.next.not.found", "not found"), lm.getValue("editor.text.find.next.message.search.failed", "Search Failed"), JOptionPane.INFORMATION_MESSAGE);
						}
					} else {
						setSelectionStart(index);
						setSelectionEnd(index + searchText.length());
						return true;
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}

		return false;
	}

	public int getLine(int pos) {
		if (pos >= 0) {
			Element element = getStyledDocument().getCharacterElement(pos);
			Element parent = element.getParentElement();
			while (parent != null) {
				Element newParent = parent.getParentElement();
				if (newParent == null) {
					break;
				}
				element = parent;
				parent = newParent;
			}
			return parent.getElementIndex(element.getStartOffset());
		} else {
			return 0;
		}
	}

	public void goToLine() {
		int caret_position = getCaretPosition();
		JTextField txt = new IntegerTextField(Integer.toString(getLine(caret_position) + 1), 10);
		requestFocus(txt);

		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		pnl.add(new JLabel(lm.getValue("editor.text.goto.line.number", "Line number: ")), BorderLayout.WEST);
		pnl.add(txt, BorderLayout.CENTER);

		if (JOptionPane.showConfirmDialog(null, pnl, lm.getValue("editor.text.goto.title", "Go To Line Number"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
			gotoline(Integer.parseInt(txt.getText()) - 1);
		}
	}

	public void gotoline(int line) {
		if (line < 0) {
			line = 0;
		} else {
			int lines = getLineCounts();
			if (line >= lines - 1) {
				line = lines - 1;
			}
		}

		Element rootElement = getDocument().getDefaultRootElement();
		Element lineElement = rootElement.getElement(line);
		int linePosition = lineElement.getStartOffset();
		setCaretPosition(linePosition);
	}

	public int getLineCounts() {
		Element map = getDocument().getDefaultRootElement();
		return map.getElementCount();
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		undo.addEdit(e.getEdit());
		undoAction.updateUndoState();
		redoAction.updateRedoState();
	}

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			} catch (CannotUndoException exc) {
			}
			updateUndoState();
			redoAction.updateRedoState();
		}

		protected void updateUndoState() {
			if (undo.canUndo()) {
				putValue(Action.NAME, undo.getUndoPresentationName());
			} else {
				putValue(Action.NAME, "Undo");
			}
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			} catch (CannotRedoException exc) {
			}
			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState() {
			if (undo.canRedo()) {
				putValue(Action.NAME, undo.getRedoPresentationName());
			} else {
				putValue(Action.NAME, "Redo");
			}
		}
	}
}
