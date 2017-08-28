package ru.nest.jvg.actionarea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BasicTextEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.menu.WMenu;
import javax.swing.menu.WMenuItem;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.action.AddCustomActionAreaAction;
import ru.nest.jvg.editor.Util;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGPeerEvent;
import ru.nest.jvg.event.JVGPeerListener;
import ru.nest.jvg.event.JVGSelectionEvent;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.ScriptResource;
import ru.nest.jvg.shape.JVGShape;
import script.pol.CompileHandler;
import script.pol.Compiler;
import script.tokenizer.TokenizerException;

// TODO JVGCustomActionArea -> JVGControl, realize independently from action area
public class JVGCustomActionArea extends JVGActionArea implements JVGPeerListener {
	public JVGCustomActionArea() {
		super(VISIBILITY_TYPE_FOCUSED);
		setScriptingEnabled(true);
		addPeerListener(this);
	}

	@Override
	public void mousePressed(JVGMouseEvent e) {
		super.mousePressed(e);
	}

	@Override
	public void mouseReleased(JVGMouseEvent e) {
		super.mouseReleased(e);

		// TODO fireUndoableEditUpdate
		// edit.add(new ShapeChangedUndoRedo(getName(), pane, path, oldShape, newShape));
		// pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));

		if (e.getButton() == JVGMouseEvent.BUTTON3) {
			// TODO add menus to manage by scripts
			WMenu menuPopup = new WMenu();
			JPopupMenu popup = menuPopup.getPopupMenu();

			WMenuItem menuScript = new WMenuItem(lm.getValue("control-action-area.menu.edit", "Edit"));
			menuScript.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					modify();
				}
			});
			popup.add(menuScript);

			MouseEvent oe = (MouseEvent) e.getOriginEvent();
			popup.show(oe.getComponent(), oe.getX(), oe.getY());
			e.consume();
		}
	}

	private void modify() {
		final JPanel pnlScripts = new JPanel();
		pnlScripts.setLayout(new GridBagLayout());

		final Map<Script.Type, BasicTextEditor> map = new HashMap<Script.Type, BasicTextEditor>();
		int y = 0;
		for (final Script.Type type : Script.types) {
			final BasicTextEditor txt = new BasicTextEditor();
			ScriptResource script = (ScriptResource) getClientProperty(type.getActionName());
			txt.setText(script != null ? script.getResource().getData() : "");
			txt.activateUndoRedo();
			map.put(type, txt);

			final JTextArea txtErrors = new JTextArea();
			txtErrors.setEditable(false);

			final JScrollPane scrollErrors = new JScrollPane(txtErrors);
			scrollErrors.setVisible(false);
			scrollErrors.setPreferredSize(new Dimension(50, 40));

			JScrollPane scroll = new JScrollPane(txt);

			final JPanel pnl = new JPanel();
			pnl.setPreferredSize(new Dimension(50, 150));
			pnl.setLayout(new BorderLayout());
			pnl.add(scroll, BorderLayout.CENTER);
			pnl.add(scrollErrors, BorderLayout.SOUTH);

			final JLabel header = new JLabel(type.getDescr()) {
				@Override
				public void paintComponent(Graphics g) {
					Util.drawGlassRect(g, getWidth(), getHeight());
					g.setColor(new Color(220, 220, 220));
					g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
					super.paintComponent(g);
				}
			};
			header.setOpaque(false);
			header.setFont(header.getFont().deriveFont(Font.BOLD, 14));
			header.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						pnl.setVisible(!pnl.isVisible());
						if (!pnl.isVisible()) {
							header.setText("[+]   " + type.getDescr() + (txt.getText().length() > 0 ? "   [*]" : ""));
						} else {
							header.setText("[-]   " + type.getDescr());
						}

						pnlScripts.revalidate();
						pnlScripts.repaint();
					}
				}
			});

			if (txt.getText().length() == 0) {
				pnl.setVisible(false);
				header.setText("[+]   " + type.getDescr());
			} else {
				header.setText("[-]   " + type.getDescr());
			}

			pnlScripts.add(header, new GridBagConstraints(0, y++, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 0, 2), 0, 0));
			pnlScripts.add(pnl, new GridBagConstraints(0, y++, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 2, 2, 2), 0, 0));

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
									public void errorOccured(int line, int offset, int length, String msg) {
										scrollErrors.setVisible(true);
										d.setCharacterAttributes(offset, length, errorAttr, false);
										txtErrors.append(offset + " - " + length + ": " + msg + "\n");
									}
								});
							} catch (TokenizerException exc) {
								exc.printStackTrace();
							}

							pnl.revalidate();
							pnl.repaint();
						}
					});
				}
			});
		}
		pnlScripts.add(new JLabel(), new GridBagConstraints(0, y++, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		final JDialog d = new JDialog();

		JButton btnOK = new JButton(lm.getValue("control-action-area.button.ok", "OK"));
		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (final Script.Type type : Script.types) {
					String script = map.get(type).getText();
					setClientProperty(type.getActionName(), new ScriptResource(script));
				}
				invalidate();
				repaint();
				d.setVisible(false);
			}
		});

		JButton btnApply = new JButton(lm.getValue("control-action-area.button.apply", "Apply"));
		btnApply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (final Script.Type type : Script.types) {
					String script = map.get(type).getText();
					setClientProperty(type.getActionName(), new ScriptResource(script));
				}
				invalidate();
				repaint();
			}
		});

		JButton btnCancel = new JButton(lm.getValue("control-action-area.button.cancel", "Cancel"));
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				d.setVisible(false);
			}
		});

		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		pnlButtons.add(btnOK);
		pnlButtons.add(btnApply);
		pnlButtons.add(btnCancel);

		JPanel pnlContent = new JPanel();
		pnlContent.setLayout(new BorderLayout());
		pnlContent.add(new JScrollPane(pnlScripts), BorderLayout.CENTER);
		pnlContent.add(pnlButtons, BorderLayout.SOUTH);

		d.setLocation(0, 0);
		d.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		d.setTitle(lm.getValue("control-action-area.title", "Control configuration"));
		d.setContentPane(pnlContent);
		d.setModal(false);
		d.setVisible(true);
	}

	@Override
	public Shape computeActionBounds() {
		JVGComponent p = getParent();
		if (p instanceof JVGShape) {
			try {
				String xvalue = getClientProperty(AddCustomActionAreaAction.PROPERTY_TYPE, "x");
				String yvalue = getClientProperty(AddCustomActionAreaAction.PROPERTY_TYPE, "y");
				if (xvalue != null && yvalue != null) {
					return new Rectangle2D.Double(Double.parseDouble(xvalue) - 2, Double.parseDouble(yvalue) - 2, 4, 4);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Cursor getCursor() {
		return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	}

	@Override
	public void selectionChanged(JVGSelectionEvent event) {
		JVGContainer selectionChangeParent = null;
		for (JVGComponent c : event.getChange()) {
			JVGContainer parent = this.parent;
			while (parent != null) {
				if (parent == c) {
					selectionChangeParent = parent;
					break;
				}
				parent = parent.getParent();
			}
		}

		if (selectionChangeParent != null) {
			if (selectionChangeParent.isSelected()) {
				setActive(true);
			} else {
				setActive(false);
			}
		}
	}

	private boolean registered = false;

	@Override
	protected void registerListener() {
		JVGPane pane = getPane();
		if (pane != null && !registered) {
			pane.getSelectionManager().addSelectionListener(this);
			registered = true;
		}
	}

	@Override
	protected void unregisterListener() {
		JVGPane pane = getPane();
		if (pane != null) {
			registered = false;
			pane.getSelectionManager().removeSelectionListener(this);
		}
	}

	@Override
	public void paint(Graphics2D g) {
		if (isDrawAction()) {
			// TODO transform
			paintScript(g);
		}
	}

	@Override
	public void connectedToPeer(JVGPeerEvent e) {
		registerListener();
	}

	@Override
	public void disconnectedFromPeer(JVGPeerEvent e) {
		unregisterListener();
	}
}
