package javax.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class JAnyButton extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	public JAnyButton() {
		init();
	}

	private JButton btnAction = new JButton() {
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			paintButton(g, this);
		}
	};

	private JButton btnChoose = new JButton() {
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			int x = getWidth() / 2 - 2, y = getHeight() / 2 - 1;
			g.setColor(Color.black);
			g.drawLine(x, y, x + 4, y);
			y++;
			g.drawLine(x + 1, y, x + 3, y);
			y++;
			g.drawLine(x + 2, y, x + 2, y);
		}
	};

	private void init() {
		btnAction.setOpaque(false);
		btnAction.setActionCommand("action");
		btnAction.addActionListener(this);
		btnAction.setBorderPainted(false);

		btnChoose.setPreferredSize(new Dimension(10, 0));
		btnChoose.setOpaque(false);
		btnChoose.setActionCommand("choose");
		btnChoose.addActionListener(this);
		btnChoose.setBorderPainted(false);

		setLayout(new BorderLayout());
		add(btnAction, BorderLayout.CENTER);
		add(btnChoose, BorderLayout.EAST);
	}

	private JPopupMenu popup = new JPopupMenu();

	public void hidePopup() {
		if (popup.isVisible()) {
			if (action != null)
				action.actionPerformed(null);
			popup.setVisible(false);
		}
	}

	public void showPopup() {
		popup.show(this, 0, getHeight());
	}

	public JPopupMenu getPopup() {
		return popup;
	}

	private ActionListener action = null;

	public void setAction(ActionListener action) {
		this.action = action;
	}

	public ActionListener getAction() {
		return action;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("choose")) {
			hidePopup();
			popup.removeAll();
			fillPopup();
			showPopup();
		} else if (cmd.equals("action")) {
			if (action != null)
				action.actionPerformed(e);
		}
	}

	public abstract void paintButton(Graphics g, JButton btn);

	public abstract void fillPopup();

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 100, 100);
		f.getContentPane().setLayout(new FlowLayout());
		JAnyButton btn = new JAnyButton() {
			private static final long serialVersionUID = 1L;
			Color c = Color.white;

			@Override
			public void paintButton(Graphics g, JButton btn) {
				g.setColor(c);
				g.fillRect(3, 3, btn.getWidth() - 6, btn.getHeight() - 6);
			}

			@Override
			public void fillPopup() {
				getPopup().add(new AbstractAction("black") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						c = Color.black;
						setAction(this);
						hidePopup();
						repaint();
					}
				});

				getPopup().add(new AbstractAction("while") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						c = Color.white;
						setAction(this);
						hidePopup();
						repaint();
					}
				});
			}
		};
		btn.setPreferredSize(new Dimension(40, 20));
		f.getContentPane().add(btn);
		f.setVisible(true);
	}
}
