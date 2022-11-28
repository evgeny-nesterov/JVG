package ru.nest.jvg.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.IconButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.image.ImagePanel;

public class ImageViewPanel extends JPanel implements ActionListener {
	private JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);

	private ImagePanel imagePanel = new ImagePanel();

	private JScrollPane scrollImage = new JScrollPane(imagePanel);

	public ImageViewPanel() {
		this(SwingConstants.VERTICAL);
	}

	public ImageViewPanel(int toolbarOrientation) {
		toolbar = new JToolBar(toolbarOrientation);

		ToolbarButton btnPlus = new ToolbarButton("Plus.gif", "enlarge");
		toolbar.add(btnPlus);

		ToolbarButton btnMinus = new ToolbarButton("Minus.gif", "reduce");
		toolbar.add(btnMinus);

		ToolbarButton btnActualSize = new ToolbarButton("1to1.gif", "actual-size");
		toolbar.add(btnActualSize);

		ToolbarButton btnFit = new ToolbarButton("fit.gif", "fit");
		toolbar.add(btnFit);

		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.WEST);
		add(scrollImage, BorderLayout.CENTER);
	}

	class ToolbarButton extends IconButton {
		public ToolbarButton(String icon, String command) {
			super(new ImageIcon(ImageViewPanel.class.getResource("img/" + icon)));
			setActionCommand(command);
			addActionListener(ImageViewPanel.this);
			setRequestFocusEnabled(false);
			setPreferredSize(new Dimension(24, 24));
			setMinimumSize(new Dimension(24, 24));
			setMaximumSize(new Dimension(24, 24));
		}
	}

	public ImagePanel getImagePanel() {
		return imagePanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("enlarge".equals(cmd)) {
			imagePanel.scale(1.2);
		} else if ("reduce".equals(cmd)) {
			imagePanel.scale(1 / 1.2);
		} else if ("actual-size".equals(cmd)) {
			imagePanel.setActualSize();
		} else if ("fit".equals(cmd)) {
			imagePanel.centrate();
		}
	}

	public static void main(String[] args) {
		ImageViewPanel pnl = new ImageViewPanel();
		pnl.getImagePanel().setImage(new ImageIcon("C:/Users/john/Desktop/Zuratkul big/Аттестация 002.jpg"));

		JFrame f = new JFrame();
		f.setContentPane(pnl);
		f.setBounds(200, 200, 800, 500);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
