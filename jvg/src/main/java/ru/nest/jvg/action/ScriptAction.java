package ru.nest.jvg.action;

import ru.nest.hiscript.pol.CompileHandler;
import ru.nest.hiscript.pol.Compiler;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.ScriptResource;
import ru.nest.jvg.shape.JVGShape;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ScriptAction extends JVGAction {
	private boolean chooseScript;

	private Script.Type type;

	private ScriptResource script;

	public ScriptAction(Script.Type type) {
		super(type.getName());
		this.type = type;
		chooseScript = false;
	}

	public ScriptAction(ScriptResource script) {
		super("script");
		this.script = script;
		chooseScript = true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (chooseScript) {
			if (script != null) {
				Script newScript = chooseScript(null, script);
				if (newScript != null) {
					script.setResource(newScript.getData().getResource());
				}
			}
		} else {
			JVGShape[] shapes = getShapes(e);
			if (shapes != null && shapes.length > 0) {
				JVGComponent focus = getComponent(e);
				JVGShape focusedShape;
				if (focus instanceof JVGShape) {
					focusedShape = (JVGShape) focus;
				} else {
					focusedShape = shapes[0];
				}

				if (type == null) {
					type = Script.START;
				}

				ScriptResource scriptResource = (ScriptResource) focusedShape.getClientProperty(type.getActionName());
				Script script = chooseScript(type, scriptResource);
				if (script != null && script.getType() != null) {
					type = script.getType();
					scriptResource = script.getData();

					if (scriptResource.getResource().getData().length() > 0) {
						for (JVGShape shape : shapes) {
							shape.setClientProperty(type.getActionName(), scriptResource);
						}
					} else {
						for (JVGShape shape : shapes) {
							shape.setClientProperty(type.getActionName(), null);
						}
					}
				}
			}
		}
	}

	public static Script chooseScript(Script.Type type, ScriptResource script) {
		JVGLocaleManager lm = JVGLocaleManager.getInstance();
		final BasicTextEditor txt = new BasicTextEditor();
		txt.setText(script != null ? script.getResource().getData() : "");
		txt.activateUndoRedo();
		txt.setPreferredSize(new Dimension(800, 600));

		final JTextArea txtErrors = new JTextArea();
		txtErrors.setEditable(false);
		final JScrollPane scrollErrors = new JScrollPane(txtErrors);
		scrollErrors.setVisible(false);
		scrollErrors.setPreferredSize(new Dimension(800, 80));

		JScrollPane scroll = new JScrollPane(txt);
		scroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		final JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		pnl.add(scroll, BorderLayout.CENTER);
		pnl.add(scrollErrors, BorderLayout.SOUTH);

		JComboBox cmbType = new WComboBox(Script.types);
		if (type != null) {
			cmbType.setSelectedItem(Script.START);
			cmbType.setBackground(Color.gray);
			pnl.add(cmbType, BorderLayout.NORTH);
		}

		txt.getDocument().addDocumentListener(new DocumentListener() {
			private SimpleAttributeSet defaultAttr = new SimpleAttributeSet();

			private SimpleAttributeSet errorAttr = new SimpleAttributeSet();

			{
				StyleConstants.setForeground(defaultAttr, Color.black);
				StyleConstants.setItalic(defaultAttr, false);
				StyleConstants.setBold(defaultAttr, false);
				StyleConstants.setBackground(errorAttr, new Color(255, 150, 150));
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				check();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				check();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			private void check() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						final StyledDocument d = txt.getStyledDocument();
						d.setCharacterAttributes(0, d.getLength(), defaultAttr, true);
						txtErrors.setText("");
						scrollErrors.setVisible(false);

						Compiler p = Compiler.getDefaultCompiler(txt.getText());
						try {
							p.compile(new CompileHandler() {
								@Override
								public void errorOccurred(int line, int offset, int length, String msg) {
									scrollErrors.setVisible(true);
									d.setCharacterAttributes(offset, length, errorAttr, false);
									txtErrors.append(offset + " - " + length + ": " + msg + "\n");
								}
							});
						} catch (TokenizerException te) {
							te.printStackTrace();
						}

						pnl.revalidate();
						pnl.repaint();
					}
				});
			}
		});

		String title = lm.getValue("script.title", "Script");
		if (type != null) {
			title += ": " + type.getDescr();
		}

		int choose = JOptionPane.showConfirmDialog(null, pnl, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choose == JOptionPane.OK_OPTION) {
			return new Script(new ScriptResource(txt.getText()), (Script.Type) cmbType.getSelectedItem());
		} else {
			return null;
		}
	}
}
