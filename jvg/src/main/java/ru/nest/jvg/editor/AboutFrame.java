package ru.nest.jvg.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import ru.nest.jvg.editor.resources.JVGLocaleManager;

public class AboutFrame extends JDialog {
	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	public AboutFrame() {
		setModal(true);
		setTitle(lm.getValue("about.title", "about"));

		JLabel lbl = new JLabel(new ImageIcon(AboutFrame.class.getResource("img/about_screen.png")));
		// lbl.setBorder(new EmptyBorder(20, 20, 20, 20));
		// lbl.setOpaque(false);
		// lbl.setBackground(Color.white);
		// lbl.setText("<html><body><center><h2>" +
		// lm.getValue("title", "Shema Editor") + " " +
		// JVGEditor.VERSION +
		// "</h2></center><br><br>" +
		// lm.getValue("about.author", "Author") + ": " +
		// lm.getValue("about.author.name", "Nesterov Evgeny") +
		// "<br>" +
		// "<br>" +
		// "</body></html>");

		JButton btnClose = new JButton(lm.getValue("about.button.close", "Close"));
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		JPanel pnlButtons = new JPanel();
		pnlButtons.setOpaque(false);
		pnlButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		pnlButtons.add(btnClose);

		JPanel pnlContent = new JPanel();
		// {
		// public void paintComponent(Graphics g)
		// {
		// Util.paintGradient(g, getWidth(), getHeight());
		// super.paintComponent(g);
		// }
		// };
		pnlContent.setOpaque(false);
		pnlContent.setLayout(new BorderLayout());
		pnlContent.add(lbl, BorderLayout.CENTER);
		// pnlContent.add(pnlButtons, BorderLayout.SOUTH);
		setContentPane(pnlContent);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
}
