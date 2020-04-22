package ru.nest.hiscript.pol;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.ExecuteException;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.RuntimeContext;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class Compiler {
	private Tokenizer tokenizer;

	public Compiler(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public Node build() throws TokenizerException, ParseException, ExecuteException {
		Node node = rule.visit(tokenizer);
		return node;
	}

	public boolean compile(CompileHandler handler) throws TokenizerException {
		return rule.visit(tokenizer, handler);
	}

	private ParseRule<?> rule = RootParseRule.getInstance();

	public void setRule(ParseRule<?> rule) {
		this.rule = rule;
	}

	public static Compiler getDefaultCompiler(String s) {
		Tokenizer t = Tokenizer.getDefaultTokenizer(s);
		Compiler p = new Compiler(t);
		p.setRule(RootParseRule.getInstance());
		return p;
	}

	public static void main(String[] args) {
		testExecutor();
	}

	public static void testExecutor() {
		StringBuilder buf = new StringBuilder();
		try {
			InputStream is = Compiler.class.getResourceAsStream("testScript.txt");
			int c;
			while ((c = is.read()) != -1) {
				buf.append((char) c);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		final JTextPane txtScript = new JTextPane();
		txtScript.setText(buf.toString());
		final JTextArea txtConsole = new JTextArea();

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setTopComponent(new JScrollPane(txtScript));
		split.setBottomComponent(new JScrollPane(txtConsole));
		split.setDividerLocation(400);

		JToolBar toolBar = new JToolBar();
		JButton btnExecute = new JButton("Execute");
		btnExecute.addActionListener(new ActionListener() {
			private SimpleAttributeSet defaultAttr = new SimpleAttributeSet();

			private SimpleAttributeSet errorAttr = new SimpleAttributeSet();

			{
				StyleConstants.setForeground(defaultAttr, Color.black);
				StyleConstants.setItalic(defaultAttr, false);
				StyleConstants.setBold(defaultAttr, false);

				StyleConstants.setBackground(errorAttr, new Color(255, 150, 150));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				txtConsole.setText("");
				String script = txtScript.getText();

				final StyledDocument d = txtScript.getStyledDocument();
				d.setCharacterAttributes(0, d.getLength(), defaultAttr, true);

				// precompile
				final ArrayList<String> errors = new ArrayList<String>();
				Compiler p = getDefaultCompiler(script);
				try {
					p.compile(new CompileHandler() {
						@Override
						public void errorOccured(int line, int offset, int length, String msg) {
							line++;

							d.setCharacterAttributes(offset, length, errorAttr, false);
							txtConsole.append("line " + line + ", " + offset + " - " + length + ": " + msg + "\n");
							System.out.println("line " + line + ", " + offset + " - " + length + ": " + msg);

							errors.add(msg);
						}
					});
				} catch (TokenizerException exc) {
					exc.printStackTrace();
				}

				// execute
				if (errors.size() == 0) {
					p = getDefaultCompiler(script);
					try {
						Node node = p.build();
						if (node != null) {
							node.compile();
							RuntimeContext ctx = new RuntimeContext();
							node.execute(ctx);
						}
					} catch (TokenizerException exc) {
						exc.printStackTrace();
					} catch (ParseException exc) {
						exc.printStackTrace();
					} catch (ExecuteException exc) {
						exc.printStackTrace();
					}
				}
			}
		});
		toolBar.add(btnExecute);

		JPanel pnlContent = new JPanel();
		pnlContent.setLayout(new BorderLayout());
		pnlContent.add(toolBar, BorderLayout.NORTH);
		pnlContent.add(split, BorderLayout.CENTER);

		JFrame f = new JFrame();
		f.setContentPane(pnlContent);
		f.setBounds(500, 0, 780, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
