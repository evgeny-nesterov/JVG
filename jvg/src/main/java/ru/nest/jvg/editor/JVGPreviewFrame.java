package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.swing.CenterLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import ru.nest.jvg.JVGDefaultFactory;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParser;

public class JVGPreviewFrame extends JFrame {
	private JVGPane pane;

	public JVGPreviewFrame(String title) {
		setTitle(JVGLocaleManager.getInstance().getValue("preview.title", "Preview") + " '" + title + "'");
		setIconImage(new ImageIcon(JVGEditor.class.getResource("img/preview.gif")).getImage());
		setLocation(0, 0);
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		pane = new JVGPane();
		pane.setScriptingEnabled(true);

		JVGEditorKit editorKit = pane.getEditorKit();
		editorKit.setFactory(JVGFactory.createDefault());

		JPanel pnl = new JPanel();
		pnl.setBackground(Color.darkGray);
		pnl.setLayout(new CenterLayout());
		pnl.add(pane);
		setContentPane(new JScrollPane(pnl));

		pane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
					dispose();
					dispatchEvent(new WindowEvent(JVGPreviewFrame.this, WindowEvent.WINDOW_CLOSING));
				}
			}
		});
	}

	public void setSource(JVGPane srcPane) {
		pane.setDocumentSize(srcPane.getDocumentSize());
		pane.setBackground(srcPane.getBackground());
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			JVGBuilder builder = JVGBuilder.create();
			builder.build(srcPane, os);
			os.close();
			os.flush();

			JVGEditorKit editorKit = pane.getEditorKit();
			JVGParser parser = new JVGParser(editorKit.getFactory());
			JVGRoot root = parser.parse(new ByteArrayInputStream(os.toByteArray()));
			parser.init(pane);
			pane.setRoot(root);
		} catch (HeadlessException exc) {
			exc.printStackTrace();
		} catch (IOException exc) {
			exc.printStackTrace();
		} catch (JVGParseException exc) {
			exc.printStackTrace();
		}
		pane.update();
	}
}
