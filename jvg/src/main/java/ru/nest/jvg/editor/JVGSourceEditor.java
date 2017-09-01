package ru.nest.jvg.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.parser.DocumentFormat;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.xmleditor.XMLEditor;
import ru.nest.xmleditor.XMLParseException;

public class JVGSourceEditor extends JDialog {
	public final static int OPTION_OK = 0;

	public final static int OPTION_CANCEL = 1;

	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private JPanel pnlEditor = new JPanel();

	private XMLEditor textPane;

	private JScrollPane scrollEditor;

	private JTextArea txtError = new JTextArea();

	private JScrollPane scrollErrors = new JScrollPane(txtError);

	private JPanel pnlManage = new JPanel();

	private DocumentFormat documentFormat = DocumentFormat.jvg;

	public JVGSourceEditor() {
		this(null, null);
	}

	public JVGSourceEditor(final JVGEditor editor, final JVGEditPane pane) {
		JPanel pnl = null;
		try {
			setTitle(lm.getValue("source.editor.title", "Edit Source"));
			setIconImage(new ImageIcon(JVGSourceEditor.class.getResource("img/schemamap.gif")).getImage());
			documentFormat = pane.getDocumentFormat();

			String xml = "";
			if (pane != null) {
				JVGBuilder builder = JVGBuilder.create(documentFormat);
				xml = builder.build(pane.getRoot().getChildren(), "UTF8");
			}

			scrollErrors.setPreferredSize(new Dimension(100, 100));
			txtError.setForeground(Color.red);
			txtError.setWrapStyleWord(true);
			txtError.setEditable(false);
			txtError.getCaret().setVisible(false);

			textPane = new XMLEditor() {
				@Override
				public void error(String message, int offset, int length) throws XMLParseException {
					super.error(message, offset, length);

					int line = getLine(offset);
					txtError.append(message + ", " + lm.getValue("source.editor.error.line", "line") + " " + (line + 1) + "\n");

					scrollErrors.setVisible(true);
					pnlEditor.revalidate();
					pnlEditor.repaint();
				}

				@Override
				public void startParseXML() {
					txtError.setText("");
				}

				@Override
				public void stopParseXML() {
					if (txtError.getDocument().getLength() == 0) {
						scrollErrors.setVisible(false);
						pnlEditor.revalidate();
						pnlEditor.repaint();
					}
				}
			};
			textPane.setText(xml);
			textPane.activateUndoRedo();
			textPane.setCaretPosition(0);
			textPane.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));

			scrollEditor = new JScrollPane(textPane);
			addRules();

			pnlEditor.setLayout(new BorderLayout());
			pnlEditor.add(scrollEditor, BorderLayout.CENTER);
			pnlEditor.add(scrollErrors, BorderLayout.SOUTH);

			JButton btnApply = null;
			if (editor != null && pane != null) {
				btnApply = new JButton(lm.getValue("source.editor.button.apply", "Apply"));
				btnApply.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							editor.load(pane, textPane.getText());
						} catch (JVGParseException exc) {
							exc.printStackTrace();
							JOptionPane.showMessageDialog(JVGSourceEditor.this, lm.getValue("source.editor.error", "Error") + ": \n" + exc.getMessage(), lm.getValue("source.editor.message.cant.apply", "Can't Apply"), JOptionPane.ERROR_MESSAGE);
						}
					}
				});
			}

			JButton btnOK = new JButton(lm.getValue("source.editor.button.ok", "OK"));
			btnOK.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();

					if (editor != null && pane != null) {
						try {
							editor.load(pane, textPane.getText());
						} catch (JVGParseException exc) {
							exc.printStackTrace();
							JOptionPane.showMessageDialog(JVGSourceEditor.this, lm.getValue("source.editor.error", "Error") + ": \n" + exc.getMessage(), lm.getValue("source.editor.message.cant.apply", "Can't Apply"), JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});

			JButton btnClose = new JButton(lm.getValue("source.editor.button.close", "Close"));
			btnClose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});

			pnlManage.setLayout(new FlowLayout(FlowLayout.CENTER));
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

		setBounds(0, 0, 600, 800);
		setContentPane(pnl);
	}

	public JPanel getOptionPanel() {
		return pnlManage;
	}

	public void setScript(String script) {
		if (script != null) {
			textPane.setText(script);
			textPane.setCaretPosition(0);
		}
	}

	private int option = OPTION_CANCEL;

	public int getOption() {
		return option;
	}

	public String getSource() {
		return textPane.getText();
	}

	private void addRules() {
		JLabel lblRow = new JLabel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				g.setColor(Color.black);
				g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);

				JViewport view = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, textPane);
				if (view != null) {
					int viewY1 = -view.getView().getY();
					int viewY2 = viewY1 + view.getParent().getHeight();
					int h = g.getFontMetrics().getHeight();
					int y = h - g.getFontMetrics().getDescent();
					int row = 1;
					while (y <= viewY2) {
						String s = Integer.toString(row);
						int w = g.getFontMetrics().stringWidth(s);
						if (y >= viewY1) {
							g.drawString(s, getWidth() - 3 - w, y);
						}
						y += h;
						row++;
					}
				}
			}

			@Override
			public Dimension getPreferredSize() {
				int charWidth = getFontMetrics(getFont()).charWidth('#');
				int digits = 1 + (int) Math.floor(Math.log10(textPane.getLineCounts()));
				if (digits < 3) {
					digits = 3;
				}
				return new Dimension(digits * charWidth + 6, textPane.getPreferredSize().height);
			}
		};
		lblRow.setFont(textPane.getFont());

		JLabel lblCol = new JLabel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				g.setColor(Color.black);
				g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

				JViewport view = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, textPane);
				if (view != null) {
					int viewX1 = -view.getView().getX();
					int viewX2 = viewX1 + view.getParent().getWidth();
					int y = getHeight() - 6;
					int charWidth = getFontMetrics(getFont()).charWidth('#');
					double x = charWidth / 2 - 1;
					int col = 0;
					while (x <= viewX2) {
						String s = Integer.toString(col);
						int w = g.getFontMetrics().stringWidth(s);
						if (x >= viewX1) {
							if (col % 5 == 0) {
								g.drawLine((int) x, getHeight(), (int) x, getHeight() - 5);
								if (col % 10 == 0) {
									g.drawString(s, (int) x - ((col > 0) ? w / 2 : 0), y);
								}
							} else {
								g.drawLine((int) x, getHeight(), (int) x, getHeight() - 3);
							}
						}
						x += charWidth;
						col++;
					}
				}
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(textPane.getPreferredSize().width, 16);
			}
		};
		lblCol.setFont(textPane.getFont());

		scrollEditor.setRowHeaderView(lblRow);
		scrollEditor.setColumnHeaderView(lblCol);
	}
}
