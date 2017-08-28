package javax.swing.toolbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.IconButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class WDragWindow extends Window {
	private JLabel header;

	private JPanel content;

	private int mx;

	private int my;

	private boolean pressed = false;

	private WToolBar toolbar;

	public WDragWindow(final WToolBar toolbar, int x, int y) {
		super((Window) null);

		this.toolbar = toolbar;

		IconButton btnClose = new IconButton(new CloseIcon(8));
		btnClose.setPreferredSize(new Dimension(10, 10));
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				restoreParent();
			}
		});

		header = new JLabel(toolbar.getTitle());
		header.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 1));
		header.add(btnClose);
		header.setForeground(Color.white);
		header.setFont(new Font("SanSerif", Font.PLAIN, 10));
		header.setOpaque(true);
		header.setPreferredSize(new Dimension(12, 12));
		header.setBackground(Color.darkGray);
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					mx = e.getX();
					my = e.getY();
					pressed = true;
					toFront();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					pressed = false;
				}
			}
		});

		header.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (pressed) {
					int dx = e.getX() - mx;
					int dy = e.getY() - my;
					setLocation(getX() + dx, getY() + dy);
				}
			}
		});

		content = new JPanel();
		content.setLayout(new BorderLayout());
		content.add(header, BorderLayout.NORTH);
		content.add(toolbar, BorderLayout.CENTER);

		setAlwaysOnTop(true);
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		setLocation(x, y);
	}

	public void showHeader() {
		Point p = toolbar.getLocationOnScreen();
		header.setVisible(true);
		content.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		setLocation(p.x - 1, p.y - header.getHeight() - 1);
		pack();
	}

	public void hideHeader() {
		Point p = toolbar.getLocationOnScreen();
		header.setVisible(false);
		content.setBorder(null);
		setLocation(p);
		pack();
	}

	public void restoreParent() {
		if (toolbar.getLastParent() != null) {
			int realIndex = -1;
			int lastIndex = Integer.MIN_VALUE;
			for (int i = 0; i < toolbar.getLastParent().getComponentCount(); i++) {
				WToolBar c = (WToolBar) toolbar.getLastParent().getComponent(i);
				int ind = c.getLastIndex();
				if (toolbar.getLastIndex() > lastIndex && toolbar.getLastIndex() < ind) {
					realIndex = i;
					break;
				}
				lastIndex = ind;
			}
			toolbar.setParent(toolbar.getLastParent(), realIndex);
		}
	}
}
